# This or That: the server

The API behind the game, written in Rust (axum, sqlx, PostgreSQL). It replaces the PHP backend at
https://github.com/antonlukin/thisorthat and speaks the same protocol, so the Flutter app in
`../mobile`, the native Android app and the web client all work against it unchanged, and tokens
issued by the PHP backend stay valid after its database is imported.

One binary does everything: it serves the API, runs the moderation rules that used to be cron jobs,
applies the database migrations on start, and imports a dump of the old MySQL database.

## Run it

```sh
docker compose up --build      # the server on http://localhost:8787/ with a database of its own
```

Then point the app at it, from `../mobile`:

```sh
flutter run --dart-define=API_BASE_URL=http://localhost:8787/        # iOS simulator
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8787/         # Android emulator
```

Settings come from the environment:

| Variable          | Default        | Meaning                                                                    |
| ----------------- | -------------- | -------------------------------------------------------------------------- |
| `DATABASE_URL`    | required       | `postgres://user:password@host/database`                                    |
| `BIND`            | `0.0.0.0:8787` | Address to listen on                                                       |
| `DB_CONNECTIONS`  | `8`            | Size of the connection pool                                                |
| `AVATAR_BASE_URL` | none           | Prefix of a commenter's picture; the user id is appended. Without it the app draws an initial |

## Work on it

With Rust installed and a PostgreSQL to test against:

```sh
export DATABASE_URL=postgres://postgres:thisorthat@localhost/thisorthat
cargo fmt && cargo clippy --all-targets && cargo test
cargo run                      # serve
```

The tests in `tests/api.rs` drive the API through the router into a real database; every test gets
a database of its own, created from `DATABASE_URL` and dropped afterwards.

Without Rust on the machine, the same in a container:

```sh
docker network create tot-net
docker run -d --name tot-pg --network tot-net -e POSTGRES_PASSWORD=tot -e POSTGRES_DB=thisorthat postgres:16
docker run --rm --network tot-net -v "$PWD":/app -w /app \
  -e DATABASE_URL=postgres://postgres:tot@tot-pg/thisorthat rust:1-slim cargo test
```

## Import the old database

```sh
thisorthat-api import <directory with the *.sql.zst files of the dump>

# or, with the compose setup above:
docker compose run --rm -v /path/to/dump:/dump:ro api thisorthat-api import /dump
```

It loads only into an empty database. Seven tables are copied as they are; `views`, one row per
answer ever given, is folded into the per-user sets described below, and the vote and comment
counters of every question are computed on the way. The dump of October 2026 (186 million answers,
1.7 million users) takes under two minutes, needs 2 to 3 GB of memory while it runs, and becomes a
database of about 1 GB.

The dump holds the token secrets of real users. It never belongs in git.

## How it is built

**The protocol** is the PHP backend's, documented at https://docs.thisorthat.ru: every method is a
path of one segment (`/getItems`), case does not matter, GET and POST both work, parameters come
from the query, a URL-encoded form or a multipart form, and the answer is `{"ok": true, "result":
…}` or `{"ok": false, "description": "…"}` under a matching status. All seventeen old methods are
there. Two things are new, for the Flutter app:

- `setProfile` stores the onboarding answers, `age` and `gender`; one left out was declined.
- Every question carries `comments_count` next to the old `comments` flag.

**Answers are sets, not rows.** The PHP backend kept a row per answer: 186 million rows, 17 GB,
and a feed query that had to anti-join against them, which is why it split questions and users
into 500 "sections". Here each user has one row in `user_votes` with three roaring bitmaps of
question ids: answered first, answered last, skipped. The whole history of all users is about half
a gigabyte. A feed request reads that one row, walks the list of approved question ids kept in
memory (`src/catalog.rs`) and takes the first 30 the user has not seen; only those 30 are then
read from the database.

**The order of the feed** is each user's own: a full cycle through the approved questions that
starts at a position derived from the user id and steps by a stride coprime to their number. Every
user gets a different shuffle, every question the same share of attention, and nothing is sorted or
stored.

**A vote counts once.** The counters live on the question row. `setViewed` locks the user, drops
the answers already in their sets, and adds the rest to the sets and the counters in one
transaction. Sending the same answer twice, which made the PHP backend count it twice, changes
nothing.

**Moderation** follows the old cron jobs (`src/moderation.rs`), every five minutes: a new question
judged by five players through `getAudit`/`setAudit` is approved on a positive score (approval +1,
decline -2), and a question reported by more than two players is rejected.

## What differs from the PHP backend on purpose

- Lists of one's own things (`getMyItems`, `getFavorite`, `getMyComments`) come newest first.
- A favorite can only be a question that exists.
- The text of a question is tidied before its length is checked, not after.
- The obscenity filter (`src/text.rs`) is a short list of roots, simpler than the Censure library
  the PHP backend used. It lets doubtful cases through: questions are moderated by people and
  comments can be reported.
- There is no Redis and no avatar service. Pictures come from `AVATAR_BASE_URL` if one is set.
