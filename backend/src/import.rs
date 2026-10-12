//! Loads a dump of the PHP backend's MySQL database (one `<table>.sql.zst`
//! per table, as `mysqldump --complete-insert` writes them) into an empty
//! database of this server.
//!
//! Seven tables are copied row for row. The eighth, `views`, holds one row
//! per answer ever given; it is folded into one row of [Votes] per user and
//! into the counters on the questions.

use std::collections::HashSet;
use std::fs::File;
use std::io::{self, BufReader, Read};
use std::path::Path;

use anyhow::{Context, bail, ensure};
use sqlx::PgPool;
use sqlx::postgres::PgPoolCopyExt;

use crate::votes::{Vote, Votes};

/// Reads the rows out of the `INSERT INTO … VALUES (…),(…);` statements of a
/// dump, one at a time, without holding more than one row in memory.
pub struct Dump<R> {
    input: R,
    buffer: Vec<u8>,
    position: usize,
    filled: usize,
    in_values: bool,
    header: Vec<u8>,
    values: Vec<Vec<u8>>,
    nulls: Vec<bool>,
    width: usize,
}

impl Dump<zstd::Decoder<'static, BufReader<File>>> {
    pub fn open(path: &Path) -> anyhow::Result<Self> {
        let file = File::open(path).with_context(|| format!("cannot open {}", path.display()))?;
        Ok(Self::new(zstd::Decoder::new(file)?))
    }
}

impl<R: Read> Dump<R> {
    pub fn new(input: R) -> Self {
        Self {
            input,
            buffer: vec![0; 1 << 16],
            position: 0,
            filled: 0,
            in_values: false,
            header: Vec::new(),
            values: Vec::new(),
            nulls: Vec::new(),
            width: 0,
        }
    }

    #[inline]
    fn byte(&mut self) -> io::Result<Option<u8>> {
        if self.position == self.filled {
            self.filled = self.input.read(&mut self.buffer)?;
            self.position = 0;
            if self.filled == 0 {
                return Ok(None);
            }
        }
        self.position += 1;
        Ok(Some(self.buffer[self.position - 1]))
    }

    fn expect(&mut self) -> io::Result<u8> {
        self.byte()?.ok_or_else(|| io::Error::new(io::ErrorKind::UnexpectedEof, "the dump ends inside a row"))
    }

    /// Moves to the next row. False at the end of the dump.
    pub fn next_row(&mut self) -> io::Result<bool> {
        loop {
            if !self.in_values {
                // Between statements: read up to the VALUES keyword.
                self.header.clear();
                loop {
                    let Some(byte) = self.byte()? else { return Ok(false) };
                    self.header.push(byte);
                    if self.header.ends_with(b" VALUES ") {
                        break;
                    }
                }
                self.in_values = true;
            }
            match self.expect()? {
                b'(' => break,
                b';' => self.in_values = false,
                _ => {}
            }
        }

        self.width = 0;
        loop {
            if self.values.len() == self.width {
                self.values.push(Vec::new());
                self.nulls.push(false);
            }
            let mut value = std::mem::take(&mut self.values[self.width]);
            value.clear();
            let mut first = self.expect()?;

            let last = if first == b'\'' {
                loop {
                    match self.expect()? {
                        b'\\' => value.push(match self.expect()? {
                            b'0' => 0,
                            b'b' => 8,
                            b'n' => b'\n',
                            b'r' => b'\r',
                            b't' => b'\t',
                            b'Z' => 26,
                            other => other,
                        }),
                        b'\'' => {
                            first = self.expect()?;
                            if first != b'\'' {
                                break;
                            }
                            value.push(b'\'');
                        }
                        other => value.push(other),
                    }
                }
                self.nulls[self.width] = false;
                first
            } else {
                while first != b',' && first != b')' {
                    value.push(first);
                    first = self.expect()?;
                }
                self.nulls[self.width] = value == b"NULL";
                first
            };
            self.values[self.width] = value;
            self.width += 1;
            if last == b')' {
                return Ok(true);
            }
        }
    }

    /// The column names of the statement the current row belongs to.
    pub fn columns(&self) -> Vec<String> {
        // The header also holds whatever came before the statement: comments,
        // `LOCK TABLES "words" WRITE;` and the like.
        let header = String::from_utf8_lossy(&self.header);
        let statement = header.rsplit("INSERT INTO").next().unwrap_or_default();
        let list = statement.split_once('(').map(|(_, list)| list).unwrap_or_default();
        list.split('"').skip(1).step_by(2).map(str::to_owned).collect()
    }

    /// A value of the current row; `None` for SQL NULL.
    pub fn get(&self, column: usize) -> Option<&[u8]> {
        (column < self.width && !self.nulls[column]).then(|| self.values[column].as_slice())
    }

    pub fn int(&self, column: usize) -> i64 {
        self.get(column).and_then(|value| std::str::from_utf8(value).ok()?.parse().ok()).unwrap_or(0)
    }
}

/// Writes one value in the text format of `COPY`.
fn put(line: &mut Vec<u8>, value: Option<&[u8]>) {
    if !line.is_empty() && *line.last().unwrap() != b'\n' {
        line.push(b'\t');
    }
    let Some(value) = value else { return line.extend_from_slice(b"\\N") };
    for &byte in value {
        match byte {
            b'\\' => line.extend_from_slice(b"\\\\"),
            b'\t' => line.extend_from_slice(b"\\t"),
            b'\n' => line.extend_from_slice(b"\\n"),
            b'\r' => line.extend_from_slice(b"\\r"),
            0 => {}
            other => line.push(other),
        }
    }
}

fn put_int(line: &mut Vec<u8>, value: i64) {
    put(line, Some(value.to_string().as_bytes()));
}

// MySQL's "no date" is not a date anywhere else.
fn timestamp(value: Option<&[u8]>) -> Option<&[u8]> {
    match value {
        Some(value) if !value.starts_with(b"0000") => Some(value),
        _ => Some(b"1970-01-01 00:00:00"),
    }
}

// A MySQL SET may hold several values; the first one is kept.
fn first_of_set(value: Option<&[u8]>) -> &[u8] {
    value.unwrap_or_default().split(|&byte| byte == b',').next().unwrap_or_default()
}

/// Copies one table of the dump. `line` turns a row into a line of `COPY`
/// input, or leaves the buffer untouched to drop the row.
async fn copy_table<F>(
    db: &PgPool,
    directory: &Path,
    table: &str,
    columns: &[&str],
    statement: &str,
    mut line: F,
) -> anyhow::Result<u64>
where
    F: FnMut(&Dump<zstd::Decoder<'static, BufReader<File>>>, &mut Vec<u8>),
{
    let mut dump = Dump::open(&directory.join(format!("{table}.sql.zst")))?;
    let mut copy = db.copy_in_raw(statement).await?;
    let mut lines = Vec::with_capacity(1 << 20);
    let mut checked = false;
    while dump.next_row()? {
        if !checked {
            ensure!(dump.columns() == columns, "unexpected columns in {table}: {:?}", dump.columns());
            checked = true;
        }
        let before = lines.len();
        line(&dump, &mut lines);
        if lines.len() > before {
            lines.push(b'\n');
        }
        if lines.len() >= 1 << 20 {
            copy.send(lines.as_slice()).await?;
            lines.clear();
        }
    }
    copy.send(lines.as_slice()).await?;
    let rows = copy.finish().await?;
    tracing::info!(table, rows, "copied");
    Ok(rows)
}

pub async fn run(db: &PgPool, directory: &Path) -> anyhow::Result<()> {
    let existing: i64 =
        sqlx::query_scalar("SELECT (SELECT count(*) FROM users) + (SELECT count(*) FROM items)").fetch_one(db).await?;
    if existing > 0 {
        bail!("the database already has users or questions; import only into an empty one");
    }
    // The dump's timestamps carry no zone; they are UTC.
    sqlx::query("SELECT set_config('TimeZone', 'UTC', false)").execute(db).await?;

    copy_table(db, directory, "words", &["word", "pos"], "COPY words (word, pos) FROM STDIN", |row, line| {
        put(line, row.get(0));
        put(line, row.get(1));
    })
    .await?;

    copy_table(
        db,
        directory,
        "users",
        &["id", "secret", "uniqid", "client", "name", "section", "created"],
        "COPY users (id, secret, uniqid, client, name, created) FROM STDIN",
        |row, line| {
            for column in 0..5 {
                put(line, row.get(column));
            }
            put(line, timestamp(row.get(6)));
        },
    )
    .await?;

    // The old table had no primary key; a repeated id is dropped.
    let mut seen = HashSet::new();
    copy_table(
        db,
        directory,
        "items",
        &["id", "user_id", "first_text", "last_text", "status", "reason", "section", "created"],
        "COPY items (id, user_id, first_text, last_text, status, reason, created) FROM STDIN",
        |row, line| {
            if !seen.insert(row.int(0)) {
                return;
            }
            for column in 0..5 {
                put(line, row.get(column));
            }
            put(line, Some(first_of_set(row.get(5))));
            put(line, timestamp(row.get(7)));
        },
    )
    .await?;

    copy_table(
        db,
        directory,
        "comments",
        &["id", "user_id", "item_id", "message", "parent", "abuse", "created"],
        "COPY comments (id, user_id, item_id, message, parent, abuse, created) FROM STDIN",
        |row, line| {
            for column in 0..6 {
                put(line, row.get(column));
            }
            put(line, timestamp(row.get(6)));
        },
    )
    .await?;

    copy_table(
        db,
        directory,
        "favorite",
        &["id", "user_id", "item_id"],
        "COPY favorites (id, user_id, item_id) FROM STDIN",
        |row, line| {
            for column in 0..3 {
                put(line, row.get(column));
            }
        },
    )
    .await?;

    let mut seen = HashSet::new();
    copy_table(
        db,
        directory,
        "reports",
        &["id", "user_id", "item_id", "reason"],
        "COPY reports (user_id, item_id, reason) FROM STDIN",
        |row, line| {
            let reason = first_of_set(row.get(3));
            if reason.is_empty() || !seen.insert((row.int(1), row.int(2), reason.to_vec())) {
                return;
            }
            put(line, row.get(1));
            put(line, row.get(2));
            put(line, Some(reason));
        },
    )
    .await?;

    copy_table(
        db,
        directory,
        "audit",
        &["id", "user_id", "item_id", "vote"],
        "COPY audit (item_id, user_id, vote) FROM STDIN",
        |row, line| {
            let vote = first_of_set(row.get(3));
            if vote.is_empty() {
                return;
            }
            put(line, row.get(2));
            put(line, row.get(1));
            put(line, Some(vote));
        },
    )
    .await?;

    import_views(db, directory).await?;

    sqlx::query(
        "UPDATE items i SET comments_count = c.count
         FROM (SELECT item_id, count(*)::int4 AS count FROM comments GROUP BY item_id) c WHERE i.id = c.item_id",
    )
    .execute(db)
    .await?;
    // New rows continue after the imported ids.
    for statement in [
        "SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT coalesce(max(id), 1) FROM users))",
        "SELECT setval(pg_get_serial_sequence('items', 'id'), (SELECT coalesce(max(id), 1) FROM items))",
        "SELECT setval(pg_get_serial_sequence('comments', 'id'), (SELECT coalesce(max(id), 1) FROM comments))",
        "SELECT setval(pg_get_serial_sequence('favorites', 'id'), (SELECT coalesce(max(id), 1) FROM favorites))",
        "ANALYZE",
    ] {
        sqlx::query(statement).execute(db).await?;
    }
    tracing::info!("import finished");
    Ok(())
}

/// Folds the answers into one [Votes] per user and two counters per question.
async fn import_views(db: &PgPool, directory: &Path) -> anyhow::Result<()> {
    let mut dump = Dump::open(&directory.join("views.sql.zst"))?;
    // One answer in eight bytes: user, question, vote. Sorting them groups a
    // user's answers together with the questions in ascending order.
    let mut answers: Vec<u64> = Vec::new();
    while dump.next_row()? {
        if answers.is_empty() {
            ensure!(dump.columns() == ["id", "user_id", "item_id", "vote"], "unexpected columns in views");
        }
        let vote = match dump.get(3) {
            Some(b"first") => 0,
            Some(b"last") => 1,
            _ => 2,
        };
        answers.push((dump.int(1) as u64) << 32 | (dump.int(2) as u64 & 0x3FFF_FFFF) << 2 | vote);
        if answers.len().is_multiple_of(20_000_000) {
            tracing::info!(answers = answers.len(), "reading views");
        }
    }
    answers.sort_unstable();
    answers.dedup_by_key(|answer| *answer >> 2);
    tracing::info!(answers = answers.len(), "views read and sorted");

    let mut counters: Vec<[u32; 2]> = Vec::new();
    let mut copy = db.copy_in_raw("COPY user_votes (user_id, first, last, skip) FROM STDIN").await?;
    let mut lines = Vec::with_capacity(1 << 20);
    let mut users = 0u64;
    for group in answers.chunk_by(|a, b| a >> 32 == b >> 32) {
        let mut votes = Votes::default();
        for answer in group {
            let question = (answer >> 2) as u32 & 0x3FFF_FFFF;
            let vote = [Vote::First, Vote::Last, Vote::Skip][(answer & 3) as usize];
            votes.insert(question, vote);
            if vote != Vote::Skip {
                if counters.len() <= question as usize {
                    counters.resize(question as usize + 1, [0, 0]);
                }
                counters[question as usize][(answer & 3) as usize] += 1;
            }
        }
        put_int(&mut lines, (group[0] >> 32) as i64);
        for column in votes.encode() {
            lines.extend_from_slice(b"\t\\\\x");
            for byte in column {
                lines.extend_from_slice(format!("{byte:02x}").as_bytes());
            }
        }
        lines.push(b'\n');
        users += 1;
        if lines.len() >= 1 << 20 {
            copy.send(lines.as_slice()).await?;
            lines.clear();
        }
    }
    copy.send(lines.as_slice()).await?;
    copy.finish().await?;
    drop(answers);
    tracing::info!(users, "votes written");

    let ids: Vec<i32> = (0..counters.len() as i32).collect();
    let firsts: Vec<i32> = counters.iter().map(|counter| counter[0] as i32).collect();
    let lasts: Vec<i32> = counters.iter().map(|counter| counter[1] as i32).collect();
    sqlx::query(
        "UPDATE items i SET first_votes = c.first, last_votes = c.last
         FROM UNNEST($1::int4[], $2::int4[], $3::int4[]) AS c(id, first, last) WHERE i.id = c.id",
    )
    .bind(ids)
    .bind(firsts)
    .bind(lasts)
    .execute(db)
    .await?;
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    fn rows(sql: &str) -> Vec<Vec<Option<String>>> {
        let mut dump = Dump::new(sql.as_bytes());
        let mut rows = Vec::new();
        while dump.next_row().unwrap() {
            rows.push((0..dump.width).map(|i| dump.get(i).map(|v| String::from_utf8_lossy(v).into_owned())).collect());
        }
        rows
    }

    #[test]
    fn rows_are_read_across_statements() {
        let sql = "-- a comment\nSET NAMES utf8mb4;\n\
                   INSERT INTO \"views\" (\"id\", \"user_id\", \"item_id\", \"vote\") VALUES (1,4,2,'first'),(2,4,3,'skip');\n\
                   INSERT INTO \"views\" (\"id\", \"user_id\", \"item_id\", \"vote\") VALUES (3,5,2,'last');\n";
        let rows = rows(sql);
        assert_eq!(rows.len(), 3);
        assert_eq!(rows[1], vec![Some("2".into()), Some("4".into()), Some("3".into()), Some("skip".into())]);
        assert_eq!(rows[2][3].as_deref(), Some("last"));
    }

    #[test]
    fn strings_keep_their_quotes_commas_and_escapes() {
        let sql = r#"INSERT INTO "items" ("id", "first_text", "name") VALUES (7,'It\'s (a), \"test\"\r\n','O''Neil \\ VALUES ('),(8,'',NULL);"#;
        let rows = rows(sql);
        assert_eq!(rows[0][1].as_deref(), Some("It's (a), \"test\"\r\n"));
        assert_eq!(rows[0][2].as_deref(), Some("O'Neil \\ VALUES ("));
        assert_eq!(rows[1], vec![Some("8".into()), Some(String::new()), None]);
    }

    #[test]
    fn the_column_list_of_a_statement_is_exposed() {
        let sql = "LOCK TABLES \"words\" WRITE;\n/*!40000 ALTER TABLE \"words\" DISABLE KEYS */;\n\
                   INSERT INTO \"words\" (\"word\", \"pos\") VALUES ('a','noun');";
        let mut dump = Dump::new(sql.as_bytes());
        assert!(dump.next_row().unwrap());
        assert_eq!(dump.columns(), ["word", "pos"]);
    }

    #[test]
    fn copy_lines_escape_what_the_format_reserves() {
        let mut line = Vec::new();
        put(&mut line, Some(b"a\tb\\c\nd"));
        put(&mut line, None);
        put_int(&mut line, 42);
        assert_eq!(line, b"a\\tb\\\\c\\nd\t\\N\t42");
    }
}
