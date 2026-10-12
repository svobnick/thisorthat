//! The server of the This or That game: the API the apps talk to, the
//! moderation rules that used to be cron jobs, and the importer of the old
//! MySQL dump.

pub mod api;
pub mod catalog;
pub mod error;
pub mod import;
pub mod moderation;
pub mod params;
pub mod text;
pub mod votes;

use std::sync::Arc;

use axum::Router;
use sqlx::PgPool;
use tower_http::cors::CorsLayer;

use crate::catalog::Catalog;

pub static MIGRATOR: sqlx::migrate::Migrator = sqlx::migrate!();

pub struct Config {
    /// Prefix of a commenter's picture; the user id is appended. Without it
    /// comments carry no picture and the app draws an initial.
    pub avatar_base: Option<String>,
    /// How long a user waits between two questions, in seconds.
    pub item_cooldown: f64,
    /// How long a user waits between two comments, in seconds.
    pub comment_cooldown: f64,
}

impl Default for Config {
    fn default() -> Self {
        Self { avatar_base: None, item_cooldown: 24.0 * 3600.0, comment_cooldown: 10.0 }
    }
}

#[derive(Clone)]
pub struct AppState {
    pub db: PgPool,
    pub catalog: Arc<Catalog>,
    pub config: Arc<Config>,
}

impl AppState {
    pub async fn new(db: PgPool, config: Config) -> sqlx::Result<Self> {
        let catalog = Arc::new(Catalog::load(&db).await?);
        Ok(Self { db, catalog, config: Arc::new(config) })
    }
}

/// Every API method is a path of one segment, reachable by GET or POST from
/// any origin; one handler tells them apart.
pub fn router(state: AppState) -> Router {
    Router::new().fallback(api::dispatch).layer(CorsLayer::permissive()).with_state(state)
}
