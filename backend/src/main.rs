use std::time::Duration;

use anyhow::{Context, bail};
use sqlx::postgres::PgPoolOptions;
use thisorthat_backend::{AppState, Config, MIGRATOR, moderation, router};

/// `thisorthat-api [serve]` runs the server.
///
/// Settings come from the environment: `DATABASE_URL` (required), `BIND`
/// (`0.0.0.0:8787`), `DB_CONNECTIONS` (8), `AVATAR_BASE_URL` (none).
#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt::init();
    let url = std::env::var("DATABASE_URL").context("DATABASE_URL is not set")?;
    let connections = std::env::var("DB_CONNECTIONS").ok().and_then(|value| value.parse().ok()).unwrap_or(8);
    let db = PgPoolOptions::new().max_connections(connections).connect(&url).await?;
    MIGRATOR.run(&db).await?;

    let arguments: Vec<String> = std::env::args().skip(1).collect();
    match arguments.first().map(String::as_str) {
        None | Some("serve") => serve(db).await,
        Some(other) => bail!("unknown command: {other}"),
    }
}

async fn serve(db: sqlx::PgPool) -> anyhow::Result<()> {
    let config = Config { avatar_base: std::env::var("AVATAR_BASE_URL").ok(), ..Config::default() };
    let state = AppState::new(db, config).await?;
    tokio::spawn(maintain(state.clone()));

    let bind = std::env::var("BIND").unwrap_or_else(|_| "0.0.0.0:8787".to_owned());
    let listener = tokio::net::TcpListener::bind(&bind).await?;
    tracing::info!(%bind, questions = state.catalog.approved_count(), "serving");
    axum::serve(listener, router(state)).with_graceful_shutdown(shutdown()).await?;
    Ok(())
}

/// What cron did for the PHP backend: every five minutes the moderation
/// rules, every thirty seconds a fresh list of the questions to serve.
async fn maintain(state: AppState) {
    let mut timer = tokio::time::interval(Duration::from_secs(30));
    let mut round = 0u32;
    loop {
        timer.tick().await;
        if round.is_multiple_of(10) {
            match moderation::run(&state.db).await {
                Ok(0) => {}
                Ok(changed) => tracing::info!(changed, "moderation"),
                Err(error) => tracing::error!(%error, "moderation failed"),
            }
        }
        if let Err(error) = state.catalog.refresh(&state.db).await {
            tracing::error!(%error, "catalog refresh failed");
        }
        round = round.wrapping_add(1);
    }
}

async fn shutdown() {
    let interrupt = tokio::signal::ctrl_c();
    let mut terminate =
        tokio::signal::unix::signal(tokio::signal::unix::SignalKind::terminate()).expect("signal handler");
    tokio::select! {
        _ = interrupt => {}
        _ = terminate.recv() => {}
    }
}
