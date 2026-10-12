use std::io;

use roaring::RoaringBitmap;

/// What a player did with a question. The names are the API's.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Vote {
    First,
    Last,
    Skip,
}

impl Vote {
    pub fn parse(value: &str) -> Option<Self> {
        match value {
            "first" => Some(Self::First),
            "last" => Some(Self::Last),
            "skip" => Some(Self::Skip),
            _ => None,
        }
    }
}

/// Everything one user has answered: three sets of question ids that never
/// overlap. This is a row of `user_votes`.
#[derive(Default, Debug, PartialEq)]
pub struct Votes {
    pub first: RoaringBitmap,
    pub last: RoaringBitmap,
    pub skip: RoaringBitmap,
}

impl Votes {
    pub fn decode(first: &[u8], last: &[u8], skip: &[u8]) -> io::Result<Self> {
        Ok(Self { first: decode(first)?, last: decode(last)?, skip: decode(skip)? })
    }

    /// The three columns, in the order of [Votes::decode].
    pub fn encode(&self) -> [Vec<u8>; 3] {
        [encode(&self.first), encode(&self.last), encode(&self.skip)]
    }

    pub fn contains(&self, id: u32) -> bool {
        self.first.contains(id) || self.last.contains(id) || self.skip.contains(id)
    }

    pub fn insert(&mut self, id: u32, vote: Vote) {
        match vote {
            Vote::First => self.first.insert(id),
            Vote::Last => self.last.insert(id),
            Vote::Skip => self.skip.insert(id),
        };
    }

    pub fn len(&self) -> u64 {
        self.first.len() + self.last.len() + self.skip.len()
    }

    pub fn is_empty(&self) -> bool {
        self.len() == 0
    }
}

// An empty set is stored as an empty value rather than a bitmap header.
fn decode(bytes: &[u8]) -> io::Result<RoaringBitmap> {
    if bytes.is_empty() { Ok(RoaringBitmap::new()) } else { RoaringBitmap::deserialize_from(bytes) }
}

fn encode(bitmap: &RoaringBitmap) -> Vec<u8> {
    if bitmap.is_empty() {
        return Vec::new();
    }
    let mut bytes = Vec::with_capacity(bitmap.serialized_size());
    bitmap.serialize_into(&mut bytes).expect("writing to a Vec cannot fail");
    bytes
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn votes_survive_the_round_trip_to_columns() {
        let mut votes = Votes::default();
        votes.insert(7, Vote::First);
        votes.insert(439_000, Vote::Last);
        votes.insert(12, Vote::Skip);

        let [first, last, skip] = votes.encode();
        let back = Votes::decode(&first, &last, &skip).unwrap();

        assert_eq!(back, votes);
        assert!(back.contains(439_000) && !back.contains(8));
        assert_eq!(back.len(), 3);
    }

    #[test]
    fn a_user_with_no_votes_costs_no_bytes() {
        assert_eq!(Votes::default().encode(), [Vec::<u8>::new(), Vec::new(), Vec::new()]);
        assert!(Votes::decode(&[], &[], &[]).unwrap().is_empty());
    }
}
