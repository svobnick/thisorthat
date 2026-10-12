use std::sync::{Arc, RwLock};

use sqlx::PgPool;

/// The ids of the questions that can be served, kept in memory so that a
/// feed request never scans the questions table. Refreshed from the
/// database on a timer and after moderation.
#[derive(Default)]
pub struct Catalog {
    lists: RwLock<Lists>,
}

#[derive(Default, Clone)]
struct Lists {
    approved: Arc<[u32]>,
    fresh: Arc<[u32]>,
}

/// Which questions a feed request asks for.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Wanted {
    Approved,
    /// Waiting for moderation.
    Fresh,
    /// Everything not rejected.
    Any,
}

impl Catalog {
    pub async fn load(db: &PgPool) -> sqlx::Result<Self> {
        let catalog = Self::default();
        catalog.refresh(db).await?;
        Ok(catalog)
    }

    pub async fn refresh(&self, db: &PgPool) -> sqlx::Result<()> {
        let rows: Vec<(i32, bool)> =
            sqlx::query_as("SELECT id, status = 'approved' FROM items WHERE status <> 'rejected' ORDER BY id")
                .fetch_all(db)
                .await?;
        let (mut approved, mut fresh) = (Vec::new(), Vec::new());
        for (id, is_approved) in rows {
            if is_approved { approved.push(id as u32) } else { fresh.push(id as u32) }
        }
        *self.lists.write().unwrap() = Lists { approved: approved.into(), fresh: fresh.into() };
        Ok(())
    }

    pub fn approved_count(&self) -> usize {
        self.lists.read().unwrap().approved.len()
    }

    /// Up to `count` questions `user` has not `seen`, in that user's own
    /// order.
    pub fn pick(&self, user: u32, wanted: Wanted, seen: &dyn Fn(u32) -> bool, count: usize) -> Vec<u32> {
        let lists = self.lists.read().unwrap().clone();
        let mut picked = Vec::with_capacity(count);
        if wanted != Wanted::Fresh {
            picked.extend(pick(&lists.approved, user, seen, count));
        }
        if wanted != Wanted::Approved {
            picked.extend(pick(&lists.fresh, user, seen, count - picked.len()));
        }
        picked
    }
}

/// Walks `ids` in an order of the user's own and returns the first `count`
/// the user has not seen.
///
/// The order is a full cycle through the list: start at a position derived
/// from the user and keep stepping by a stride that shares no factor with
/// the length. Every user gets a different shuffle, every question gets the
/// same share of first looks, and nothing is sorted or stored.
pub fn pick(ids: &[u32], user: u32, seen: &dyn Fn(u32) -> bool, count: usize) -> Vec<u32> {
    let len = ids.len() as u64;
    let mut picked = Vec::with_capacity(count.min(ids.len()));
    if len == 0 || count == 0 {
        return picked;
    }
    let hash = mix(user as u64);
    let start = hash % len;
    let mut stride = (hash >> 32) % len;
    while gcd(stride, len) != 1 {
        stride += 1;
    }
    for step in 0..len {
        let id = ids[((start + step * stride) % len) as usize];
        if !seen(id) {
            picked.push(id);
            if picked.len() == count {
                break;
            }
        }
    }
    picked
}

// SplitMix64: spreads consecutive user ids over the whole range.
fn mix(value: u64) -> u64 {
    let mut z = value.wrapping_add(0x9E37_79B9_7F4A_7C15);
    z = (z ^ (z >> 30)).wrapping_mul(0xBF58_476D_1CE4_E5B9);
    z = (z ^ (z >> 27)).wrapping_mul(0x94D0_49BB_1331_11EB);
    z ^ (z >> 31)
}

fn gcd(a: u64, b: u64) -> u64 {
    if b == 0 { a } else { gcd(b, a % b) }
}

#[cfg(test)]
mod tests {
    use std::collections::HashSet;

    use super::*;

    #[test]
    fn the_walk_visits_every_question_exactly_once() {
        for len in [1u32, 2, 7, 30, 1000, 1024] {
            let ids: Vec<u32> = (100..100 + len).collect();
            for user in [1, 2, 99, 1_702_218] {
                let all = pick(&ids, user, &|_| false, usize::MAX);
                assert_eq!(all.len(), ids.len());
                assert_eq!(all.iter().collect::<HashSet<_>>().len(), ids.len(), "len {len}, user {user}");
            }
        }
    }

    #[test]
    fn seen_questions_are_skipped_and_the_rest_keeps_its_order() {
        let ids: Vec<u32> = (1..=100).collect();
        let order = pick(&ids, 42, &|_| false, usize::MAX);
        let seen: HashSet<u32> = order[..40].iter().copied().collect();

        let next = pick(&ids, 42, &|id| seen.contains(&id), 30);

        assert_eq!(next, order[40..70]);
    }

    #[test]
    fn different_users_get_different_orders() {
        let ids: Vec<u32> = (1..=1000).collect();
        assert_ne!(pick(&ids, 1, &|_| false, 30), pick(&ids, 2, &|_| false, 30));
    }

    #[test]
    fn nothing_left_is_an_empty_list() {
        assert!(pick(&[], 1, &|_| false, 30).is_empty());
        assert!(pick(&[5, 6], 1, &|_| true, 30).is_empty());
    }
}
