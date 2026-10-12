//! The API from the outside: requests go through the router into a real
//! PostgreSQL. `#[sqlx::test]` gives every test a database of its own with
//! the migrations applied; it needs `DATABASE_URL` to point at a server.

use axum::body::Body;
use axum::http::{Request, StatusCode};
use http_body_util::BodyExt;
use serde_json::{Value, json};
use sqlx::PgPool;
use thisorthat_backend::{AppState, Config, api, moderation, router};
use tower::ServiceExt;

struct Api {
    state: AppState,
}

impl Api {
    /// A server with no waiting between questions or comments.
    async fn new(db: PgPool) -> Self {
        Self::with(db, Config { item_cooldown: 0.0, comment_cooldown: 0.0, ..Config::default() }).await
    }

    async fn with(db: PgPool, config: Config) -> Self {
        Self { state: AppState::new(db, config).await.unwrap() }
    }

    async fn send(&self, request: Request<Body>) -> (StatusCode, Value) {
        let response = router(self.state.clone()).oneshot(request).await.unwrap();
        let status = response.status();
        let body = response.into_body().collect().await.unwrap().to_bytes();
        (status, serde_json::from_slice(&body).unwrap())
    }

    async fn call(&self, method: &str, params: &[(&str, &str)]) -> (StatusCode, Value) {
        let body = form_urlencoded::Serializer::new(String::new()).extend_pairs(params).finish();
        let request = Request::post(format!("/{method}"))
            .header("content-type", "application/x-www-form-urlencoded")
            .body(Body::from(body))
            .unwrap();
        self.send(request).await
    }

    /// Calls a method that must succeed and returns its `result`.
    async fn ok(&self, method: &str, params: &[(&str, &str)]) -> Value {
        let (status, body) = self.call(method, params).await;
        assert_eq!((status, &body["ok"]), (StatusCode::OK, &json!(true)), "{method}: {body}");
        body["result"].clone()
    }

    /// Calls a method that must be refused and returns the description.
    async fn refused(&self, method: &str, params: &[(&str, &str)], status: StatusCode) -> Value {
        let (got, body) = self.call(method, params).await;
        assert_eq!((got, &body["ok"]), (status, &json!(false)), "{method}: {body}");
        body
    }

    async fn register(&self) -> String {
        self.ok("register", &[("client", "test"), ("uniqid", "device-1")]).await["token"].as_str().unwrap().to_owned()
    }

    /// Adds `count` approved questions, ids from 1.
    async fn seed(&self, count: i32) {
        sqlx::query(
            "INSERT INTO items (user_id, first_text, last_text, status)
             SELECT 0, 'first ' || n, 'last ' || n, 'approved' FROM generate_series(1, $1) n",
        )
        .bind(count)
        .execute(&self.state.db)
        .await
        .unwrap();
        self.state.catalog.refresh(&self.state.db).await.unwrap();
    }

    async fn feed(&self, token: &str) -> Vec<Value> {
        self.ok("getItems", &[("token", token), ("status", "approved")]).await["items"].as_array().unwrap().clone()
    }

    async fn votes_of(&self, item: i32) -> (i32, i32) {
        sqlx::query_as("SELECT first_votes, last_votes FROM items WHERE id = $1")
            .bind(item)
            .fetch_one(&self.state.db)
            .await
            .unwrap()
    }
}

fn ids(items: &[Value]) -> Vec<i32> {
    items.iter().map(|item| item["item_id"].as_str().unwrap().parse().unwrap()).collect()
}

#[sqlx::test]
async fn a_token_opens_the_api_and_tokens_of_the_php_backend_still_work(db: PgPool) {
    let api = Api::new(db).await;

    let token = api.register().await;
    api.ok("getItems", &[("token", &token)]).await;

    let (id, marker) = token.split_once(':').unwrap();
    let forged = format!("{id}:{}", "0".repeat(32));
    assert_ne!(marker, "0".repeat(32));
    api.refused("getItems", &[("token", &forged)], StatusCode::UNAUTHORIZED).await;
    api.refused("getItems", &[], StatusCode::UNAUTHORIZED).await;

    // A user as the old database holds one: the secret is characters 10 to 42
    // of the hex SHA-256 of the marker, as PHP's `substr(hash(…), 10, 32)`.
    assert_eq!(api::secret_of(&"0".repeat(32)), "a95a34c293f278ac52e45ce537bab5e7");
    sqlx::query("INSERT INTO users (id, secret, uniqid, client) VALUES (900, $1, 'old', 'android')")
        .bind(api::secret_of("abcdef0123456789abcdef0123456789"))
        .execute(&api.state.db)
        .await
        .unwrap();
    api.ok("getItems", &[("token", "900:abcdef0123456789abcdef0123456789")]).await;
}

#[sqlx::test]
async fn the_feed_hands_out_unanswered_questions_and_each_answer_counts_once(db: PgPool) {
    let api = Api::new(db).await;
    api.seed(40).await;
    let token = api.register().await;

    let first_page = api.feed(&token).await;
    assert_eq!(first_page.len(), 30);
    let item = &first_page[0];
    assert_eq!(item["status"], "approved");
    assert_eq!((&item["first_vote"], &item["last_vote"], &item["comments"]), (&json!(0), &json!(0), &Value::Null));
    assert_eq!(item["comments_count"], 0);
    assert!(item["first_text"].as_str().unwrap().starts_with("first "));

    let page = ids(&first_page);
    let (a, b, c) = (page[0], page[1], page[2]);
    let answers = [
        ("token".to_owned(), token.clone()),
        (format!("views[{a}]"), "first".to_owned()),
        (format!("views[{b}]"), "last".to_owned()),
        (format!("views[{c}]"), "skip".to_owned()),
        ("views[999999]".to_owned(), "first".to_owned()),
    ];
    let answers: Vec<(&str, &str)> = answers.iter().map(|(name, value)| (name.as_str(), value.as_str())).collect();
    assert_eq!(api.ok("setViewed", &answers).await, true);
    // The same answers again, and a change of mind: nothing moves.
    api.ok("setViewed", &answers).await;
    api.ok("setViewed", &[("token", &token), (&format!("views[{a}]"), "last")]).await;

    assert_eq!(api.votes_of(a).await, (1, 0));
    assert_eq!(api.votes_of(b).await, (0, 1));
    assert_eq!(api.votes_of(c).await, (0, 0));

    let second_page = ids(&api.feed(&token).await);
    assert_eq!(second_page.len(), 30);
    assert_eq!(second_page[..27], page[3..]);
    assert!(![a, b, c].iter().any(|id| second_page.contains(id)));

    // Another user starts from a place of their own.
    let other = api.register().await;
    assert_ne!(ids(&api.feed(&other).await), page);

    // Answer everything: the feed runs dry.
    let rest: Vec<(String, String)> = (1..=40).map(|id| (format!("views[{id}]"), "skip".to_owned())).collect();
    let mut all: Vec<(&str, &str)> = rest.iter().map(|(name, value)| (name.as_str(), value.as_str())).collect();
    all.push(("token", &token));
    api.ok("setViewed", &all).await;
    assert!(api.feed(&token).await.is_empty());

    api.refused("setViewed", &[("token", &token)], StatusCode::BAD_REQUEST).await;
}

#[sqlx::test]
async fn parameters_are_read_from_the_query_a_form_or_a_multipart_body(db: PgPool) {
    let api = Api::new(db).await;
    api.seed(3).await;
    let token = api.register().await;

    // Method names ignore case; GET carries everything in the query.
    let (status, body) = api.send(Request::get(format!("/GETITEMS?token={token}")).body(Body::empty()).unwrap()).await;
    assert_eq!((status, body["result"]["items"].as_array().unwrap().len()), (StatusCode::OK, 3));

    // What a browser sends for `FormData`.
    let multipart = format!(
        "--xyz\r\nContent-Disposition: form-data; name=\"token\"\r\n\r\n{token}\r\n\
         --xyz\r\nContent-Disposition: form-data; name=\"views[2]\"\r\n\r\nfirst\r\n--xyz--\r\n"
    );
    let request = Request::post("/setViewed")
        .header("content-type", "multipart/form-data; boundary=xyz")
        .body(Body::from(multipart))
        .unwrap();
    assert_eq!(api.send(request).await.1["result"], true);
    assert_eq!(api.votes_of(2).await, (1, 0));

    let (status, body) = api.call("noSuchMethod", &[("token", &token)]).await;
    assert_eq!((status, &body["ok"]), (StatusCode::NOT_FOUND, &json!(false)));
}

#[sqlx::test]
async fn a_new_question_is_checked_and_waits_for_moderation(db: PgPool) {
    let api = Api::new(db).await;
    api.seed(1).await;
    let token = api.register().await;
    let add = |first: &'static str, last: &'static str| {
        let token = token.clone();
        let api = &api;
        async move { api.call("addItem", &[("token", &token), ("first_text", first), ("last_text", last)]).await }
    };

    assert_eq!(add("Да", "Конечно нет").await.1["description"], "Вопрос должен состоять из 4-150 символов");
    assert_eq!(add("Одно и то же", "Одно  и то же").await.1["description"], "Вопросы не могут быть одинаковыми");
    assert_eq!(
        add("Какая-то хуйня", "Что-то другое").await.1["description"],
        "Текст вопроса содержит нецензурную лексику"
    );

    let (status, body) = add("  Ехать на   поезде??", "Лететь на самолёте").await;
    assert_eq!(status, StatusCode::OK, "{body}");
    let id = body["result"]["item_id"].as_str().unwrap().to_owned();

    let mine = api.ok("getMyItems", &[("token", &token)]).await;
    assert_eq!(mine["pages"], json!({"count": 1, "offset": 0, "total": 1}));
    assert_eq!(mine["items"][0]["item_id"], id);
    assert_eq!(
        (&mine["items"][0]["first_text"], &mine["items"][0]["status"]),
        (&json!("Ехать на поезде?"), &json!("new"))
    );
    // Not in the feed of approved questions.
    assert_eq!(ids(&api.feed(&token).await), [1]);

    // The same question again, in either order, points at the original.
    let (status, body) = add("Лететь на самолёте", "Ехать на поезде?").await;
    assert_eq!(status, StatusCode::BAD_REQUEST);
    assert_eq!(body["parameters"]["clone_id"], id);
    assert_eq!(add("first 1", "last 1").await.1["parameters"]["clone_id"], "1");
}

#[sqlx::test]
async fn questions_cannot_be_added_more_often_than_the_cooldown(db: PgPool) {
    let api = Api::with(db, Config::default()).await;
    let token = api.register().await;

    api.ok("addItem", &[("token", &token), ("first_text", "Утро без будильника"), ("last_text", "Вечер без дел")])
        .await;
    let refusal = api
        .refused(
            "addItem",
            &[("token", &token), ("first_text", "Чай с лимоном"), ("last_text", "Кофе с молоком")],
            StatusCode::BAD_REQUEST,
        )
        .await;

    assert_eq!(refusal["description"], "Нельзя отправлять вопросы чаще, чем раз в день");
}

#[sqlx::test]
async fn favorites_are_kept_newest_first(db: PgPool) {
    let api = Api::new(db).await;
    api.seed(5).await;
    let token = api.register().await;

    for item in ["2", "4", "4", "777"] {
        api.ok("addFavorite", &[("token", &token), ("item_id", item)]).await;
    }
    let favorites = api.ok("getFavorite", &[("token", &token), ("limit", "100")]).await;
    assert_eq!(ids(favorites["items"].as_array().unwrap()), [4, 2]);
    assert_eq!(favorites["pages"]["total"], 2);

    api.ok("deleteFavorite", &[("token", &token), ("item_id", "4")]).await;
    let favorites = api.ok("getFavorite", &[("token", &token)]).await;
    assert_eq!(ids(favorites["items"].as_array().unwrap()), [2]);

    api.refused("addFavorite", &[("token", &token), ("item_id", "x")], StatusCode::BAD_REQUEST).await;
}

#[sqlx::test]
async fn a_comment_gives_its_author_a_name_and_counts_on_the_question(db: PgPool) {
    let api = Api::new(db).await;
    api.seed(2).await;
    sqlx::query("INSERT INTO words (word, pos) VALUES ('волосатый', 'adjv'), ('монстр', 'noun')")
        .execute(&api.state.db)
        .await
        .unwrap();
    let token = api.register().await;
    let user = token.split_once(':').unwrap().0;

    let comment =
        api.ok("addComment", &[("token", &token), ("item_id", "1"), ("message", "  Отличный вопрос!  ")]).await;
    assert_eq!(comment["message"], "Отличный вопрос!");
    assert_eq!(
        (&comment["name"], &comment["user_id"], &comment["parent"]),
        (&json!("Волосатый монстр"), &json!(user), &json!("0"))
    );
    assert_eq!(comment["avatar"], "");
    api.ok(
        "addComment",
        &[
            ("token", &token),
            ("item_id", "1"),
            ("message", "И ещё"),
            ("parent", comment["comment_id"].as_str().unwrap()),
        ],
    )
    .await;

    let listed = api.ok("getComments", &[("token", &token), ("item_id", "1"), ("limit", "1"), ("offset", "1")]).await;
    assert_eq!(listed["pages"], json!({"count": 1, "offset": 1, "total": 2}));
    assert_eq!(
        (&listed["comments"][0]["message"], &listed["comments"][0]["name"]),
        (&json!("И ещё"), &json!("Волосатый монстр"))
    );

    let item = api.feed(&token).await.into_iter().find(|item| item["item_id"] == "1").unwrap();
    assert_eq!((&item["comments"], &item["comments_count"]), (&json!("available"), &json!(2)));

    // The only pair of words is taken now, and names are never shared.
    let second = api.register().await;
    let theirs = api.ok("addComment", &[("token", &second), ("item_id", "2"), ("message", "Тоже тут")]).await;
    let their_id = second.split_once(':').unwrap().0;
    assert_eq!(theirs["name"], format!("Волосатый монстр {their_id}"));

    let mine = api.ok("getMyComments", &[("token", &token)]).await;
    assert_eq!(
        (&mine["pages"]["total"], &mine["comments"][0]["message"], &mine["comments"][0]["item_id"]),
        (&json!(2), &json!("И ещё"), &json!("1"))
    );
    let commented = api.ok("getCommentedItems", &[("token", &token)]).await;
    assert_eq!(ids(commented["items"].as_array().unwrap()), [1]);

    for (message, description) in [
        ("", "Комментарий должен состоять из 1-300 символов"),
        (&"я".repeat(301) as &str, "Комментарий должен состоять из 1-300 символов"),
        ("ну ты и мудак", "Комментарий содержит нецензурную лексику"),
    ] {
        let refusal = api
            .refused(
                "addComment",
                &[("token", &token), ("item_id", "1"), ("message", message)],
                StatusCode::BAD_REQUEST,
            )
            .await;
        assert_eq!(refusal["description"], description);
    }
    api.refused("addComment", &[("token", &token), ("item_id", "99"), ("message", "Куда?")], StatusCode::BAD_REQUEST)
        .await;

    api.ok("reportComment", &[("token", &token), ("comment_id", comment["comment_id"].as_str().unwrap())]).await;
    let abuse: i32 =
        sqlx::query_scalar("SELECT abuse FROM comments ORDER BY id LIMIT 1").fetch_one(&api.state.db).await.unwrap();
    assert_eq!(abuse, 1);
}

#[sqlx::test]
async fn comments_cannot_be_sent_faster_than_the_cooldown(db: PgPool) {
    let api = Api::with(db, Config::default()).await;
    api.seed(1).await;
    let token = api.register().await;

    api.ok("addComment", &[("token", &token), ("item_id", "1"), ("message", "Раз")]).await;
    let refusal = api
        .refused("addComment", &[("token", &token), ("item_id", "1"), ("message", "Два")], StatusCode::BAD_REQUEST)
        .await;

    assert_eq!(refusal["description"], "Нельзя отправлять комментарии так часто");
}

#[sqlx::test]
async fn players_approve_and_reject_questions(db: PgPool) {
    let api = Api::new(db).await;
    let author = api.register().await;
    for (first, last) in
        [("Хороший вопрос", "Тоже хороший"), ("Плохой вопрос", "Совсем плохой"), ("Пока неясно", "Совсем неясно")]
    {
        api.ok("addItem", &[("token", &author), ("first_text", first), ("last_text", last)]).await;
    }
    api.seed(1).await; // question 4, approved from the start

    let mut judges = Vec::new();
    for _ in 0..5 {
        judges.push(api.register().await);
    }
    assert_eq!(ids(api.ok("getAudit", &[("token", &judges[0])]).await["items"].as_array().unwrap()), [1, 2, 3]);
    for (index, judge) in judges.iter().enumerate() {
        api.ok("setAudit", &[("token", judge), ("item_id", "1"), ("vote", "approve")]).await;
        // Three for and two against: a decline weighs twice.
        api.ok(
            "setAudit",
            &[("token", judge), ("item_id", "2"), ("vote", if index < 3 { "approve" } else { "decline" })],
        )
        .await;
        if index < 4 {
            api.ok("setAudit", &[("token", judge), ("item_id", "3"), ("vote", "approve")]).await;
        }
    }
    assert_eq!(ids(api.ok("getAudit", &[("token", &judges[0])]).await["items"].as_array().unwrap()), [] as [i32; 0]);
    // Three players report the approved question.
    for judge in &judges[..3] {
        api.ok("sendReport", &[("token", judge), ("item_id", "4"), ("reason", "abuse")]).await;
    }
    api.ok("cancelReport", &[("token", &judges[4]), ("item_id", "4")]).await;

    assert_eq!(moderation::run(&api.state.db).await.unwrap(), 3);
    api.state.catalog.refresh(&api.state.db).await.unwrap();

    let statuses: Vec<(i32, String)> =
        sqlx::query_as("SELECT id, status FROM items ORDER BY id").fetch_all(&api.state.db).await.unwrap();
    let statuses: Vec<(i32, &str)> = statuses.iter().map(|(id, status)| (*id, status.as_str())).collect();
    assert_eq!(statuses, [(1, "approved"), (2, "rejected"), (3, "new"), (4, "rejected")]);
    assert_eq!(ids(&api.feed(&judges[0]).await), [1]);
    assert_eq!(moderation::run(&api.state.db).await.unwrap(), 0);
}

#[sqlx::test]
async fn the_onboarding_answers_are_stored_with_the_user(db: PgPool) {
    let api = Api::new(db).await;
    let token = api.register().await;
    let profile = || async {
        sqlx::query_as::<_, (Option<String>, Option<String>)>("SELECT age, gender FROM users ORDER BY id LIMIT 1")
            .fetch_one(&api.state.db)
            .await
            .unwrap()
    };

    api.ok("setProfile", &[("token", &token), ("age", "13-15"), ("gender", "female")]).await;
    assert_eq!(profile().await, (Some("13-15".into()), Some("female".into())));

    // Declined answers are simply left out.
    api.ok("setProfile", &[("token", &token), ("age", "35plus")]).await;
    assert_eq!(profile().await, (Some("35plus".into()), None));

    api.refused("setProfile", &[("token", &token), ("age", "ancient")], StatusCode::BAD_REQUEST).await;
}
