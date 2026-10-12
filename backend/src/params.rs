use std::collections::HashMap;
use std::sync::LazyLock;

use axum::http::{HeaderMap, Uri, header};
use bytes::Bytes;
use regex::Regex;

use crate::error::ApiError;
use crate::votes::Vote;

/// The parameters of a request, wherever the client put them: the query
/// string, a URL-encoded form or a multipart form. A name given twice takes
/// its value from the body.
pub struct Params(HashMap<String, String>);

static ID: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^\d{1,11}$").unwrap());
static LIMIT: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^(?:[1-9][0-9]?|100)$").unwrap());
static OFFSET: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^[0-9]{1,9}$").unwrap());
static VIEW: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^views\[(\d{1,9})\]$").unwrap());

impl Params {
    pub async fn read(uri: &Uri, headers: &HeaderMap, body: Bytes) -> Result<Self, ApiError> {
        let mut map = HashMap::new();
        let mut extend = |bytes: &[u8]| {
            for (name, value) in form_urlencoded::parse(bytes) {
                map.insert(name.into_owned(), value.into_owned());
            }
        };
        extend(uri.query().unwrap_or_default().as_bytes());

        let content_type = headers.get(header::CONTENT_TYPE).and_then(|value| value.to_str().ok()).unwrap_or_default();
        if content_type.starts_with("application/x-www-form-urlencoded") {
            extend(&body);
        } else if content_type.starts_with("multipart/form-data") {
            let malformed = |_| ApiError::bad_request("Не удалось разобрать запрос");
            let boundary = multer::parse_boundary(content_type).map_err(malformed)?;
            let stream = futures_util::stream::once(async move { Ok::<_, std::io::Error>(body) });
            let mut form = multer::Multipart::new(stream, boundary);
            while let Some(field) = form.next_field().await.map_err(malformed)? {
                let Some(name) = field.name().map(str::to_owned) else { continue };
                if field.file_name().is_none() {
                    map.insert(name, field.text().await.map_err(malformed)?);
                }
            }
        }
        Ok(Self(map))
    }

    #[cfg(test)]
    pub fn of(pairs: &[(&str, &str)]) -> Self {
        Self(pairs.iter().map(|(name, value)| (name.to_string(), value.to_string())).collect())
    }

    /// The value of `name` when it is there and matches `pattern`.
    pub fn matching(&self, name: &str, pattern: &Regex) -> Option<&str> {
        self.0.get(name).map(String::as_str).filter(|value| pattern.is_match(value))
    }

    pub fn has(&self, name: &str) -> bool {
        self.0.contains_key(name)
    }

    /// A question or comment id.
    pub fn id(&self, name: &str) -> Option<i32> {
        self.matching(name, &ID)?.parse().ok()
    }

    /// The page size: 1 to 100, `default` when left out or malformed.
    pub fn limit(&self, default: i64) -> i64 {
        self.matching("limit", &LIMIT).and_then(|value| value.parse().ok()).unwrap_or(default)
    }

    pub fn offset(&self) -> i64 {
        self.matching("offset", &OFFSET).and_then(|value| value.parse().ok()).unwrap_or(0)
    }

    /// The answers of `/setViewed`, sent as `views[<question id>]=first|last|skip`.
    /// Anything that is not one is left out.
    pub fn views(&self) -> Vec<(u32, Vote)> {
        let mut views = Vec::new();
        for (name, value) in &self.0 {
            let Some(id) = VIEW.captures(name).and_then(|found| found[1].parse().ok()) else { continue };
            if let Some(vote) = Vote::parse(value) {
                views.push((id, vote));
            }
        }
        views.sort_unstable_by_key(|&(id, _)| id);
        views
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn views_are_read_from_array_style_names() {
        let params =
            Params::of(&[("views[100]", "first"), ("views[376]", "skip"), ("views[x]", "last"), ("views[5]", "nope")]);
        assert_eq!(params.views(), vec![(100, Vote::First), (376, Vote::Skip)]);
    }

    #[test]
    fn paging_falls_back_to_defaults() {
        assert_eq!(Params::of(&[("limit", "100"), ("offset", "20")]).limit(30), 100);
        assert_eq!(Params::of(&[("limit", "101")]).limit(30), 30);
        assert_eq!(Params::of(&[("limit", "0"), ("offset", "-1")]).offset(), 0);
    }
}
