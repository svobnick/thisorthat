//! The API methods. They keep the contract of the PHP backend this server
//! replaces (same names, same parameters, same JSON), so every existing
//! client works unchanged, and add what the new app needs: `setProfile` and
//! a comment counter on every question.

use std::collections::{HashMap, HashSet};
use std::sync::LazyLock;

use axum::extract::State;
use axum::http::{HeaderMap, StatusCode, Uri};
use axum::response::{IntoResponse, Response};
use bytes::Bytes;
use regex::Regex;
use serde_json::{Value, json};
use sha2::{Digest, Sha256};
use sqlx::postgres::PgRow;
use sqlx::{PgExecutor, PgPool, Row};

use crate::AppState;
use crate::catalog::Wanted;
use crate::error::{ApiError, success};
use crate::params::Params;
use crate::text;
use crate::votes::{Vote, Votes};

type ApiResult = Result<Value, ApiError>;

const FEED_PAGE: usize = 30;
const MAX_VIEWS: usize = 1000;

fn pattern(source: &str) -> Regex {
    Regex::new(source).unwrap()
}

static TOKEN: LazyLock<Regex> = LazyLock::new(|| pattern(r"(?i)^\d{1,10}:[a-z0-9]{32}$"));
static CLIENT: LazyLock<Regex> = LazyLock::new(|| pattern(r"(?i)^[a-z0-9_-]{0,16}$"));
static UNIQID: LazyLock<Regex> = LazyLock::new(|| pattern(r"(?i)^[a-z0-9_-]{0,64}$"));
static STATUS: LazyLock<Regex> = LazyLock::new(|| pattern(r"(?i)^(new|approved)$"));
static REASON: LazyLock<Regex> = LazyLock::new(|| pattern(r"^(typo|abuse|clone|dislike)$"));
static VERDICT: LazyLock<Regex> = LazyLock::new(|| pattern(r"^(approve|decline)$"));
static AGE: LazyLock<Regex> = LazyLock::new(|| pattern(r"^(under10|10-12|13-15|16-17|18-24|25-34|35plus)$"));
static GENDER: LazyLock<Regex> = LazyLock::new(|| pattern(r"^(male|female|other)$"));
static ANYTHING: LazyLock<Regex> = LazyLock::new(|| pattern(r"(?s)^.*$"));

macro_rules! item_columns {
    () => {
        "i.id, i.first_text, i.last_text, i.status, i.reason, i.first_votes, i.last_votes, i.comments_count"
    };
}

pub async fn dispatch(State(state): State<AppState>, uri: Uri, headers: HeaderMap, body: Bytes) -> Response {
    // Method names are not case sensitive.
    let method = uri.path().trim_matches('/').to_ascii_lowercase();
    match run(&state, &method, &uri, &headers, body).await {
        Ok(result) => success(result),
        Err(error) => error.into_response(),
    }
}

async fn run(state: &AppState, method: &str, uri: &Uri, headers: &HeaderMap, body: Bytes) -> ApiResult {
    const METHODS: [&str; 18] = [
        "register",
        "getitems",
        "getmyitems",
        "getcommenteditems",
        "additem",
        "setviewed",
        "getfavorite",
        "addfavorite",
        "deletefavorite",
        "sendreport",
        "cancelreport",
        "getcomments",
        "getmycomments",
        "addcomment",
        "reportcomment",
        "getaudit",
        "setaudit",
        "setprofile",
    ];
    if !METHODS.contains(&method) {
        return Err(ApiError::new(StatusCode::NOT_FOUND, "Метод не найден"));
    }
    let params = Params::read(uri, headers, body).await?;
    if method == "register" {
        return register(state, &params).await;
    }
    let user = authorize(&state.db, &params).await?;
    match method {
        "getitems" => get_items(state, user, &params).await,
        "setviewed" => set_viewed(state, user, &params).await,
        "getmyitems" => get_my_items(state, user, &params).await,
        "getfavorite" => get_favorite(state, user, &params).await,
        "getcommenteditems" => get_commented_items(state, user, &params).await,
        "additem" => add_item(state, user, &params).await,
        "addfavorite" => add_favorite(state, user, &params).await,
        "deletefavorite" => delete_favorite(state, user, &params).await,
        "sendreport" => send_report(state, user, &params).await,
        "cancelreport" => cancel_report(state, user, &params).await,
        "getcomments" => get_comments(state, &params).await,
        "getmycomments" => get_my_comments(state, user, &params).await,
        "addcomment" => add_comment(state, user, &params).await,
        "reportcomment" => report_comment(state, &params).await,
        "getaudit" => get_audit(state, user).await,
        "setaudit" => set_audit(state, user, &params).await,
        _ => set_profile(state, user, &params).await,
    }
}

// ── Users ──────────────────────────────────────────────────────────────────

/// What is stored for a token `<user id>:<marker>`: a slice of the SHA-256
/// of the marker. The PHP backend derived it this way, so its tokens verify.
pub fn secret_of(marker: &str) -> String {
    let hex: String = Sha256::digest(marker.as_bytes()).iter().map(|byte| format!("{byte:02x}")).collect();
    hex[10..42].to_owned()
}

async fn authorize(db: &PgPool, params: &Params) -> Result<i32, ApiError> {
    let token = params.matching("token", &TOKEN).ok_or_else(ApiError::unauthorized)?;
    let (id, marker) = token.split_once(':').ok_or_else(ApiError::unauthorized)?;
    let id: i32 = id.parse().map_err(|_| ApiError::unauthorized())?;
    let secret: Option<String> =
        sqlx::query_scalar("SELECT secret FROM users WHERE id = $1").bind(id).fetch_optional(db).await?;
    if secret.as_deref() == Some(secret_of(marker).as_str()) { Ok(id) } else { Err(ApiError::unauthorized()) }
}

async fn register(state: &AppState, params: &Params) -> ApiResult {
    let client = params
        .matching("client", &CLIENT)
        .ok_or_else(|| ApiError::bad_request("Параметр client не соответствует условиям"))?;
    let uniqid = params
        .matching("uniqid", &UNIQID)
        .ok_or_else(|| ApiError::bad_request("Параметр uniqid не соответствует условиям"))?;
    let marker = format!("{:032x}", rand::random::<u128>());
    let id: i32 = sqlx::query_scalar("INSERT INTO users (secret, uniqid, client) VALUES ($1, $2, $3) RETURNING id")
        .bind(secret_of(&marker))
        .bind(uniqid)
        .bind(client)
        .fetch_one(&state.db)
        .await?;
    Ok(json!({"token": format!("{id}:{marker}")}))
}

/// New with the Flutter app: the onboarding answers. A parameter left out
/// means the player declined to say.
async fn set_profile(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let age = params.matching("age", &AGE);
    let gender = params.matching("gender", &GENDER);
    if params.has("age") && age.is_none() {
        return Err(ApiError::bad_request("Параметр age не соответствует условиям"));
    }
    if params.has("gender") && gender.is_none() {
        return Err(ApiError::bad_request("Параметр gender не соответствует условиям"));
    }
    sqlx::query("UPDATE users SET age = $1, gender = $2 WHERE id = $3")
        .bind(age)
        .bind(gender)
        .bind(user)
        .execute(&state.db)
        .await?;
    Ok(json!(true))
}

// ── Questions ──────────────────────────────────────────────────────────────

fn item_json(row: &PgRow) -> Value {
    let comments: i32 = row.get("comments_count");
    let status: String = row.get("status");
    let mut item = json!({
        "item_id": row.get::<i32, _>("id").to_string(),
        "first_text": row.get::<String, _>("first_text"),
        "last_text": row.get::<String, _>("last_text"),
        "status": status,
        "comments": if comments > 0 { json!("available") } else { Value::Null },
        "comments_count": comments,
        "first_vote": row.get::<i32, _>("first_votes"),
        "last_vote": row.get::<i32, _>("last_votes"),
    });
    if status == "rejected" {
        item["reason"] = json!(row.get::<String, _>("reason"));
    }
    item
}

fn pages(count: usize, offset: i64, total: i64) -> Value {
    json!({"count": count, "offset": offset, "total": total})
}

async fn load_votes<'e>(db: impl PgExecutor<'e>, user: i32) -> Result<Votes, ApiError> {
    let row = sqlx::query("SELECT first, last, skip FROM user_votes WHERE user_id = $1")
        .bind(user)
        .fetch_optional(db)
        .await?;
    let Some(row) = row else { return Ok(Votes::default()) };
    Votes::decode(row.get("first"), row.get("last"), row.get("skip")).map_err(|error| {
        tracing::error!(%error, user, "unreadable votes");
        ApiError::new(StatusCode::INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера")
    })
}

/// Up to 30 questions the user has not answered. The user's answers are one
/// row; which questions are left is worked out in memory against the
/// catalog, and only the questions picked are read from the database.
async fn get_items(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let wanted = match params.matching("status", &STATUS).map(str::to_ascii_lowercase).as_deref() {
        Some("approved") => Wanted::Approved,
        Some(_) => Wanted::Fresh,
        None => Wanted::Any,
    };
    let votes = load_votes(&state.db, user).await?;
    let picked = state.catalog.pick(user as u32, wanted, &|id| votes.contains(id), FEED_PAGE);
    let ids: Vec<i32> = picked.iter().map(|&id| id as i32).collect();

    let rows = sqlx::query(concat!(
        "SELECT ",
        item_columns!(),
        " FROM items i WHERE i.id = ANY($1) AND i.status <> 'rejected'"
    ))
    .bind(&ids)
    .fetch_all(&state.db)
    .await?;
    let mut found: HashMap<i32, Value> = rows.iter().map(|row| (row.get("id"), item_json(row))).collect();
    let items: Vec<Value> = ids.iter().filter_map(|id| found.remove(id)).collect();
    Ok(json!({"items": items}))
}

/// Records answers. Each question counts once per user, however often the
/// same answer is sent: the user's sets are read and written under a lock,
/// and only the answers not in them yet reach the counters.
async fn set_viewed(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let views = params.views();
    if views.is_empty() {
        return Err(ApiError::bad_request("Неверный формат массива с ответами"));
    }
    if views.len() > MAX_VIEWS {
        return Err(ApiError::new(StatusCode::PAYLOAD_TOO_LARGE, "Массив с ответами слишком большой"));
    }

    let mut tx = state.db.begin().await?;
    sqlx::query("SELECT pg_advisory_xact_lock($1)").bind(user as i64).execute(&mut *tx).await?;
    let mut votes = load_votes(&mut *tx, user).await?;

    let fresh: Vec<(u32, Vote)> = views.into_iter().filter(|&(id, _)| !votes.contains(id)).collect();
    if fresh.is_empty() {
        return Ok(json!(true));
    }
    let ids: Vec<i32> = fresh.iter().map(|&(id, _)| id as i32).collect();
    let firsts: Vec<i32> = fresh.iter().map(|&(_, vote)| (vote == Vote::First) as i32).collect();
    let lasts: Vec<i32> = fresh.iter().map(|&(_, vote)| (vote == Vote::Last) as i32).collect();
    // Ids that are not questions come back from nowhere and are dropped.
    let known: Vec<i32> = sqlx::query_scalar(
        "UPDATE items i
         SET first_votes = i.first_votes + answer.first, last_votes = i.last_votes + answer.last
         FROM UNNEST($1::int4[], $2::int4[], $3::int4[]) AS answer(id, first, last)
         WHERE i.id = answer.id
         RETURNING i.id",
    )
    .bind(&ids)
    .bind(&firsts)
    .bind(&lasts)
    .fetch_all(&mut *tx)
    .await?;
    let known: HashSet<i32> = known.into_iter().collect();
    for (id, vote) in fresh {
        if known.contains(&(id as i32)) {
            votes.insert(id, vote);
        }
    }

    let [first, last, skip] = votes.encode();
    sqlx::query(
        "INSERT INTO user_votes (user_id, first, last, skip) VALUES ($1, $2, $3, $4)
         ON CONFLICT (user_id) DO UPDATE SET first = EXCLUDED.first, last = EXCLUDED.last, skip = EXCLUDED.skip",
    )
    .bind(user)
    .bind(first)
    .bind(last)
    .bind(skip)
    .execute(&mut *tx)
    .await?;
    tx.commit().await?;
    Ok(json!(true))
}

async fn get_my_items(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let (limit, offset) = (params.limit(30), params.offset());
    let rows = sqlx::query(concat!(
        "SELECT ",
        item_columns!(),
        " FROM items i WHERE i.user_id = $1 ORDER BY i.id DESC LIMIT $2 OFFSET $3"
    ))
    .bind(user)
    .bind(limit)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;
    let total: i64 =
        sqlx::query_scalar("SELECT count(*) FROM items WHERE user_id = $1").bind(user).fetch_one(&state.db).await?;
    let items: Vec<Value> = rows.iter().map(item_json).collect();
    Ok(json!({"pages": pages(items.len(), offset, total), "items": items}))
}

async fn get_favorite(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let (limit, offset) = (params.limit(30), params.offset());
    let rows = sqlx::query(concat!(
        "SELECT ",
        item_columns!(),
        " FROM favorites f JOIN items i ON i.id = f.item_id
          WHERE f.user_id = $1 ORDER BY f.id DESC LIMIT $2 OFFSET $3"
    ))
    .bind(user)
    .bind(limit)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;
    let total: i64 =
        sqlx::query_scalar("SELECT count(*) FROM favorites f JOIN items i ON i.id = f.item_id WHERE f.user_id = $1")
            .bind(user)
            .fetch_one(&state.db)
            .await?;
    let items: Vec<Value> = rows.iter().map(item_json).collect();
    Ok(json!({"pages": pages(items.len(), offset, total), "items": items}))
}

/// The questions the user commented on, the latest comment first.
async fn get_commented_items(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let (limit, offset) = (params.limit(30), params.offset());
    let rows = sqlx::query(concat!(
        "SELECT ",
        item_columns!(),
        ", mine.comment_id
          FROM (SELECT item_id, max(id) AS comment_id FROM comments WHERE user_id = $1 GROUP BY item_id) mine
          JOIN items i ON i.id = mine.item_id
          ORDER BY mine.comment_id DESC LIMIT $2 OFFSET $3"
    ))
    .bind(user)
    .bind(limit)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;
    let total: i64 = sqlx::query_scalar("SELECT count(DISTINCT item_id) FROM comments WHERE user_id = $1")
        .bind(user)
        .fetch_one(&state.db)
        .await?;
    let items: Vec<Value> = rows
        .iter()
        .map(|row| {
            let mut item = item_json(row);
            item["comment_id"] = json!(row.get::<i32, _>("comment_id").to_string());
            item
        })
        .collect();
    Ok(json!({"pages": pages(items.len(), offset, total), "items": items}))
}

async fn add_item(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let answer = |name: &str| {
        let text = text::sanitize(params.matching(name, &ANYTHING).unwrap_or_default());
        let length = text.chars().count();
        if (4..=150).contains(&length) {
            Ok(text)
        } else {
            Err(ApiError::bad_request("Вопрос должен состоять из 4-150 символов"))
        }
    };
    let (first, last) = (answer("first_text")?, answer("last_text")?);
    if first == last {
        return Err(ApiError::bad_request("Вопросы не могут быть одинаковыми"));
    }
    if text::has_badwords(&first) || text::has_badwords(&last) {
        return Err(ApiError::bad_request("Текст вопроса содержит нецензурную лексику"));
    }
    let recent: bool = sqlx::query_scalar(
        "SELECT EXISTS (SELECT 1 FROM items WHERE user_id = $1 AND created > now() - make_interval(secs => $2))",
    )
    .bind(user)
    .bind(state.config.item_cooldown)
    .fetch_one(&state.db)
    .await?;
    if recent {
        return Err(ApiError::bad_request("Нельзя отправлять вопросы чаще, чем раз в день"));
    }
    let clone: Option<i32> = sqlx::query_scalar(
        "SELECT id FROM items
         WHERE status <> 'rejected' AND (
             (md5(first_text) = md5($1) AND md5(last_text) = md5($2) AND first_text = $1 AND last_text = $2)
             OR (md5(first_text) = md5($2) AND md5(last_text) = md5($1) AND first_text = $2 AND last_text = $1))
         ORDER BY id DESC LIMIT 1",
    )
    .bind(&first)
    .bind(&last)
    .fetch_optional(&state.db)
    .await?;
    if let Some(clone) = clone {
        return Err(
            ApiError::bad_request("В нашей базе уже есть такой вопрос").with(json!({"clone_id": clone.to_string()}))
        );
    }
    let id: i32 =
        sqlx::query_scalar("INSERT INTO items (user_id, first_text, last_text) VALUES ($1, $2, $3) RETURNING id")
            .bind(user)
            .bind(&first)
            .bind(&last)
            .fetch_one(&state.db)
            .await?;
    Ok(json!({"item_id": id.to_string()}))
}

fn item_id(params: &Params) -> Result<i32, ApiError> {
    params.id("item_id").ok_or_else(|| ApiError::bad_request("Параметр item_id не соответствует условиям"))
}

// ── Favorites and reports ──────────────────────────────────────────────────

async fn add_favorite(state: &AppState, user: i32, params: &Params) -> ApiResult {
    sqlx::query(
        "INSERT INTO favorites (user_id, item_id) SELECT $1, id FROM items WHERE id = $2
         ON CONFLICT (user_id, item_id) DO NOTHING",
    )
    .bind(user)
    .bind(item_id(params)?)
    .execute(&state.db)
    .await?;
    Ok(json!(true))
}

async fn delete_favorite(state: &AppState, user: i32, params: &Params) -> ApiResult {
    sqlx::query("DELETE FROM favorites WHERE user_id = $1 AND item_id = $2")
        .bind(user)
        .bind(item_id(params)?)
        .execute(&state.db)
        .await?;
    Ok(json!(true))
}

async fn send_report(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let item = item_id(params)?;
    let reason = params
        .matching("reason", &REASON)
        .ok_or_else(|| ApiError::bad_request("Параметр reason не соответствует условиям"))?;
    sqlx::query("INSERT INTO reports (user_id, item_id, reason) VALUES ($1, $2, $3) ON CONFLICT DO NOTHING")
        .bind(user)
        .bind(item)
        .bind(reason)
        .execute(&state.db)
        .await?;
    Ok(json!(true))
}

async fn cancel_report(state: &AppState, user: i32, params: &Params) -> ApiResult {
    sqlx::query("DELETE FROM reports WHERE user_id = $1 AND item_id = $2")
        .bind(user)
        .bind(item_id(params)?)
        .execute(&state.db)
        .await?;
    Ok(json!(true))
}

// ── Comments ───────────────────────────────────────────────────────────────

fn avatar(state: &AppState, user: i32) -> String {
    state.config.avatar_base.as_ref().map(|base| format!("{base}{user}")).unwrap_or_default()
}

async fn get_comments(state: &AppState, params: &Params) -> ApiResult {
    let item = item_id(params)?;
    let (limit, offset) = (params.limit(50), params.offset());
    let rows = sqlx::query(
        "SELECT c.id, c.user_id, c.parent, c.message, coalesce(u.name, '') AS name
         FROM comments c LEFT JOIN users u ON u.id = c.user_id
         WHERE c.item_id = $1 ORDER BY c.id LIMIT $2 OFFSET $3",
    )
    .bind(item)
    .bind(limit)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;
    let total: i64 =
        sqlx::query_scalar("SELECT count(*) FROM comments WHERE item_id = $1").bind(item).fetch_one(&state.db).await?;
    let comments: Vec<Value> = rows
        .iter()
        .map(|row| {
            let author: i32 = row.get("user_id");
            json!({
                "comment_id": row.get::<i32, _>("id").to_string(),
                "user_id": author.to_string(),
                "parent": row.get::<i32, _>("parent").to_string(),
                "message": row.get::<String, _>("message"),
                "name": row.get::<String, _>("name"),
                "avatar": avatar(state, author),
            })
        })
        .collect();
    Ok(json!({"pages": pages(comments.len(), offset, total), "comments": comments}))
}

async fn get_my_comments(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let (limit, offset) = (params.limit(50), params.offset());
    let rows = sqlx::query(
        "SELECT id, item_id, parent, message FROM comments WHERE user_id = $1 ORDER BY id DESC LIMIT $2 OFFSET $3",
    )
    .bind(user)
    .bind(limit)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;
    let total: i64 =
        sqlx::query_scalar("SELECT count(*) FROM comments WHERE user_id = $1").bind(user).fetch_one(&state.db).await?;
    let comments: Vec<Value> = rows
        .iter()
        .map(|row| {
            json!({
                "comment_id": row.get::<i32, _>("id").to_string(),
                "parent": row.get::<i32, _>("parent").to_string(),
                "message": row.get::<String, _>("message"),
                "item_id": row.get::<i32, _>("item_id").to_string(),
                "avatar": avatar(state, user),
            })
        })
        .collect();
    Ok(json!({"pages": pages(comments.len(), offset, total), "comments": comments}))
}

async fn add_comment(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let item = item_id(params)?;
    let message = params.matching("message", &ANYTHING).unwrap_or_default().trim();
    if !(1..=300).contains(&message.chars().count()) {
        return Err(ApiError::bad_request("Комментарий должен состоять из 1-300 символов"));
    }
    let parent = params.id("parent").unwrap_or(0);
    if text::has_badwords(message) {
        return Err(ApiError::bad_request("Комментарий содержит нецензурную лексику"));
    }
    let recent: bool = sqlx::query_scalar(
        "SELECT EXISTS (SELECT 1 FROM comments WHERE user_id = $1 AND created > now() - make_interval(secs => $2))",
    )
    .bind(user)
    .bind(state.config.comment_cooldown)
    .fetch_one(&state.db)
    .await?;
    if recent {
        return Err(ApiError::bad_request("Нельзя отправлять комментарии так часто"));
    }
    let name = user_name(&state.db, user).await?;

    let mut tx = state.db.begin().await?;
    let counted = sqlx::query("UPDATE items SET comments_count = comments_count + 1 WHERE id = $1")
        .bind(item)
        .execute(&mut *tx)
        .await?
        .rows_affected();
    if counted == 0 {
        return Err(ApiError::bad_request("Параметр item_id не соответствует условиям"));
    }
    let id: i32 = sqlx::query_scalar(
        "INSERT INTO comments (user_id, item_id, parent, message) VALUES ($1, $2, $3, $4) RETURNING id",
    )
    .bind(user)
    .bind(item)
    .bind(parent)
    .bind(message)
    .fetch_one(&mut *tx)
    .await?;
    tx.commit().await?;

    Ok(json!({
        "comment_id": id.to_string(),
        "user_id": user.to_string(),
        "parent": parent.to_string(),
        "message": message,
        "name": name,
        "avatar": avatar(state, user),
    }))
}

/// The name a user comments under. It is made up on their first comment, an
/// adjective and a noun, and is theirs alone from then on.
async fn user_name(db: &PgPool, user: i32) -> Result<String, ApiError> {
    const ADJECTIVES: [&str; 8] = ["весёлый", "сонный", "смелый", "тихий", "умный", "добрый", "хитрый", "ловкий"];
    const NOUNS: [&str; 8] = ["кактус", "пингвин", "ураган", "багажник", "монстр", "ёжик", "чайник", "робот"];

    for attempt in 0..12 {
        let name: Option<String> =
            sqlx::query_scalar("SELECT name FROM users WHERE id = $1").bind(user).fetch_one(db).await?;
        if let Some(name) = name {
            return Ok(name);
        }
        let words: (Option<String>, Option<String>) = sqlx::query_as(
            "SELECT (SELECT word FROM words WHERE pos = 'adjv' ORDER BY random() LIMIT 1),
                    (SELECT word FROM words WHERE pos = 'noun' ORDER BY random() LIMIT 1)",
        )
        .fetch_one(db)
        .await?;
        let pick = |list: &[&str; 8]| list[rand::random::<u32>() as usize % list.len()].to_owned();
        let adjective = words.0.unwrap_or_else(|| pick(&ADJECTIVES));
        let noun = words.1.unwrap_or_else(|| pick(&NOUNS));
        let mut letters = adjective.chars();
        let candidate = match letters.next() {
            Some(initial) => format!("{}{} {noun}", initial.to_uppercase(), letters.as_str()),
            None => noun,
        };
        // A taken name is passed over and the loop tries another pair. When
        // luck runs out, the user's id makes the name theirs for certain.
        let candidate = if attempt < 10 { candidate } else { format!("{candidate} {user}") };
        sqlx::query(
            "UPDATE users SET name = $1 WHERE id = $2 AND name IS NULL
             AND NOT EXISTS (SELECT 1 FROM users WHERE name = $1)",
        )
        .bind(&candidate)
        .bind(user)
        .execute(db)
        .await?;
    }
    Err(ApiError::new(StatusCode::INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"))
}

async fn report_comment(state: &AppState, params: &Params) -> ApiResult {
    let comment = params
        .id("comment_id")
        .ok_or_else(|| ApiError::bad_request("Параметр comment_id не соответствует условиям"))?;
    sqlx::query("UPDATE comments SET abuse = abuse + 1 WHERE id = $1").bind(comment).execute(&state.db).await?;
    Ok(json!(true))
}

// ── Moderation by players ──────────────────────────────────────────────────

async fn get_audit(state: &AppState, user: i32) -> ApiResult {
    let rows = sqlx::query(
        "SELECT i.id, i.first_text, i.last_text FROM items i
         WHERE i.status = 'new'
           AND NOT EXISTS (SELECT 1 FROM audit a WHERE a.item_id = i.id AND a.user_id = $1)
         ORDER BY i.id LIMIT 30",
    )
    .bind(user)
    .fetch_all(&state.db)
    .await?;
    let items: Vec<Value> = rows
        .iter()
        .map(|row| {
            json!({
                "item_id": row.get::<i32, _>("id").to_string(),
                "first_text": row.get::<String, _>("first_text"),
                "last_text": row.get::<String, _>("last_text"),
            })
        })
        .collect();
    Ok(json!({"items": items}))
}

async fn set_audit(state: &AppState, user: i32, params: &Params) -> ApiResult {
    let item = item_id(params)?;
    let vote = params
        .matching("vote", &VERDICT)
        .ok_or_else(|| ApiError::bad_request("Параметр vote не соответствует условиям"))?;
    sqlx::query("INSERT INTO audit (item_id, user_id, vote) VALUES ($1, $2, $3) ON CONFLICT DO NOTHING")
        .bind(item)
        .bind(user)
        .bind(vote)
        .execute(&state.db)
        .await?;
    Ok(json!(true))
}
