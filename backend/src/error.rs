use axum::http::{StatusCode, header};
use axum::response::{IntoResponse, Response};
use serde_json::{Value, json};

/// A refusal, in the shape every client of the API expects:
/// `{"ok": false, "description": "…"}` under a matching HTTP status. The
/// description is shown to the player as it is, so it is in Russian.
#[derive(Debug)]
pub struct ApiError {
    pub status: StatusCode,
    pub description: String,
    pub parameters: Option<Value>,
}

impl ApiError {
    pub fn new(status: StatusCode, description: &str) -> Self {
        Self { status, description: description.to_owned(), parameters: None }
    }

    pub fn bad_request(description: &str) -> Self {
        Self::new(StatusCode::BAD_REQUEST, description)
    }

    pub fn unauthorized() -> Self {
        Self::new(StatusCode::UNAUTHORIZED, "Не удалось авторизовать запрос")
    }

    pub fn with(mut self, parameters: Value) -> Self {
        self.parameters = Some(parameters);
        self
    }
}

impl From<sqlx::Error> for ApiError {
    fn from(error: sqlx::Error) -> Self {
        tracing::error!(%error, "database error");
        Self::new(StatusCode::INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера")
    }
}

impl IntoResponse for ApiError {
    fn into_response(self) -> Response {
        let mut body = json!({"ok": false, "description": self.description});
        if let Some(parameters) = self.parameters {
            body["parameters"] = parameters;
        }
        respond(self.status, &body)
    }
}

/// `{"ok": true, "result": …}`.
pub fn success(result: Value) -> Response {
    respond(StatusCode::OK, &json!({"ok": true, "result": result}))
}

fn respond(status: StatusCode, body: &Value) -> Response {
    (status, [(header::CONTENT_TYPE, "application/json; charset=utf-8")], body.to_string()).into_response()
}
