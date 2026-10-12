use sqlx::PgPool;

/// Applies the two moderation rules, the ones the PHP backend ran from cron.
/// Returns how many questions changed status.
///
/// - A new question judged by at least five players is approved when its
///   score is positive, an approval counting +1 and a decline -2, and
///   rejected otherwise.
/// - A question reported by more than two players is rejected.
pub async fn run(db: &PgPool) -> sqlx::Result<u64> {
    let judged = sqlx::query(
        "UPDATE items i
         SET status = CASE WHEN verdict.score >= 1 THEN 'approved' ELSE 'rejected' END
         FROM (
             SELECT a.item_id, sum(CASE a.vote WHEN 'approve' THEN 1 ELSE -2 END) AS score
             FROM audit a JOIN items n ON n.id = a.item_id AND n.status = 'new'
             GROUP BY a.item_id
             HAVING count(*) >= 5
         ) verdict
         WHERE i.id = verdict.item_id AND i.status = 'new'",
    )
    .execute(db)
    .await?
    .rows_affected();

    let reported = sqlx::query(
        "UPDATE items i
         SET status = 'rejected'
         FROM (SELECT item_id FROM reports GROUP BY item_id HAVING count(*) > 2) reported
         WHERE i.id = reported.item_id AND i.status <> 'rejected'",
    )
    .execute(db)
    .await?
    .rows_affected();

    Ok(judged + reported)
}
