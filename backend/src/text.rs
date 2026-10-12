use std::sync::LazyLock;

use regex::Regex;

/// Tidies a text a player typed: runs of whitespace become one space and a
/// repeated punctuation mark becomes one (`Что???` → `Что?`).
pub fn sanitize(text: &str) -> String {
    let mut out = String::with_capacity(text.len());
    let mut last = None;
    for word in text.split_whitespace() {
        if !out.is_empty() {
            out.push(' ');
            last = Some(' ');
        }
        for ch in word.chars() {
            if "?!.:,;".contains(ch) && last == Some(ch) {
                continue;
            }
            out.push(ch);
            last = Some(ch);
        }
    }
    out
}

/// Whether a text contains Russian or English obscenities.
///
/// This is a short list of roots, anchored at the start of a word so that
/// innocent words containing the same letters pass ("хлеба", "рубля",
/// "застрахуй"). It errs on the side of letting things through: questions
/// are moderated by people before anyone sees them, and comments can be
/// reported.
pub fn has_badwords(text: &str) -> bool {
    let lower = text.to_lowercase();
    ENGLISH.is_match(&lower) || RUSSIAN.is_match(&normalize(&lower))
}

// Undoes the usual disguises: Latin letters and digits standing in for
// Cyrillic ones, and "ё".
fn normalize(lower: &str) -> String {
    lower
        .chars()
        .map(|ch| match ch {
            'ё' => 'е',
            'a' | '@' => 'а',
            'b' | '6' => 'б',
            'c' => 'с',
            'e' => 'е',
            'k' => 'к',
            'o' | '0' => 'о',
            'p' => 'р',
            'x' => 'х',
            'y' => 'у',
            '3' => 'з',
            '1' | 'i' => 'и',
            other => other,
        })
        .collect()
}

static RUSSIAN: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(concat!(
        r"\b(?:",
        r"(?:на|по|ни|до|за|от|о|а)?ху[йеяю]",
        r"|(?:рас|раз|ис|от|до|за|на|вы|при|под|с|о)?п[ие]зд",
        r"|(?:за|вы|у|по|на|до|разъ|съ|отъ|объ|пере|про|при|недо|долбо|зло)?еб(?:а[тлнш]|у[тчн]|и[тс]|л[оаиян]|ну|ыр|ет|ош)",
        r"|бля(?:[дт]|\b)",
        r"|муд(?:ак|ил|озв|ач)",
        r"|пид[оа]р|пидр|г[оа]ндон|залуп|шлюх|дроч",
        r"|манд[аыу]\b|сук[аи]\b|суч(?:к[аи]|ар)",
        r")",
    ))
    .unwrap()
});

static ENGLISH: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"\b(?:fuck|shit\b|bitch|cunt|asshole)").unwrap());

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn sanitize_collapses_spaces_and_repeated_punctuation() {
        assert_eq!(sanitize("  Быть   умным\n\nили нет???  "), "Быть умным или нет?");
        assert_eq!(sanitize("Чай... Кофе!!"), "Чай. Кофе!");
        assert_eq!(sanitize("1,5 или 2.5?!"), "1,5 или 2.5?!");
    }

    #[test]
    fn obscenities_are_caught_in_their_common_disguises() {
        for text in ["иди нахуй", "ХУЙНЯ какая-то", "пиздец", "он заебал", "бля, ну и вопрос", "xyй", "п1здец fuck"]
        {
            assert!(has_badwords(text), "{text}");
        }
    }

    #[test]
    fn innocent_words_with_the_same_letters_pass() {
        for text in [
            "Купить хлеба на сто рубля",
            "Застрахуй машину",
            "Он психует из-за команды",
            "Сидеть на суку и хлебать суп",
            "Оскорблять и употреблять",
            "Мандарин или небо",
            "Сабля, корабля, ребята",
            "Поезд или подъезд",
            "Худой хулиган хуже",
            "Class of assassins",
        ] {
            assert!(!has_badwords(text), "{text}");
        }
    }
}
