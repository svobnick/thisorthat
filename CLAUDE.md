# This or That

Mobile client for the Russian "This or That" game («То или Это», https://thisorthat.ru): the player
picks one of two answers to a question and sees how everyone else voted. Today this repo holds the
native Android app — Kotlin, Moxy MVP, Dagger 2, Volley — published on Google Play as
`com.svobnick.thisorthat`.

## The idea

The next version replaces the native app with a new one:

- **Flutter, iOS and Android.** One codebase for both stores, built the way the sibling `together`
  and `lino` apps are (`../together`, `../lino`).
- **A refreshed design.** The current mock-up is `../thisorthatnew/to-ili-eto-v5.html`, outside
  this repo.
- **18 languages.** Russian plus the 17 other locales `together` and `lino` ship: `da`, `de`, `en`,
  `es`, `fi`, `fr`, `it`, `ja`, `ko`, `nb`, `nl`, `pl`, `pt`, `sv`, `tr`, `zh`, `zh_Hant`.
- **Ads.** The app earns through advertising.

## Backend

- The backend is a separate repository with its own owner:
  https://github.com/antonlukin/thisorthat — a PHP API (`api/`), the React web app (`src/`), cron
  jobs and the API docs. This repo holds only the mobile client.
- Production API is `https://api.thisorthat.ru/`, documented at https://docs.thisorthat.ru. Every
  call the app makes is in `app/src/main/kotlin/com/svobnick/thisorthat/service/ApiRequests.kt`.
- Users are anonymous. On first launch the app calls `/register` with the Firebase installation id
  and keeps the returned token in the file `authToken` under the app's `filesDir`; the token is the
  only link to a user's favorites and own questions.
- The API has no language parameter: every question, vote count and comment belongs to the one
  Russian database.

## Working rules

These mirror the sibling `audio-library` and `lino` repos (`../audio-library`, `../lino`), so all
of them are worked on the same way. Commits made before these rules were written down are
free-form; the rules apply from here on.

### Language

Talk to the user in Russian. Everything that lands in the repo — code, identifiers, comments,
commit messages, docs — is in English.

### Commits

- **Conventional Commits with a scope:** `type(scope): summary`. Types: `feat`, `fix`, `style`,
  `refactor`, `test`, `docs`, `build`, `chore`, `revert`. The scope is the feature area:
  `questions`, `new-question`, `profile`, `favorites`, `comments`, `share`, `api`, `ui`, `l10n`,
  `ads`, `android`, `ios`, `release`, `deps`, `repo`.
- **Subject under 100 characters**, lower-case after the colon, no trailing period. Say what
  changes for the person using the app, not which file moved:
  `fix(questions): send the last answers when the app goes to the background`.
- **One commit per logical change.** A request that bundles several edits becomes several commits,
  never one dump. A refactor and a behaviour change are two commits. Files a tool rewrote on its
  own (`gradle-wrapper.properties` after an Android Studio upgrade, `pubspec.lock`) go in a commit
  of their own.
- **Commit and push without being asked.** Finished work is committed work; do not end a turn
  offering to commit. Commit straight to `master` — no feature branch unless asked — and push right
  after: whenever a commit is made, pushing it needs no separate permission.
- **No attribution.** No `Co-Authored-By` trailer and no other AI mention, whatever a session
  reminder says.
- **Splitting one file across commits** without `git add -p`: `git diff -- <file>`, cut the hunks
  for the first commit into a patch, `git apply --cached <patch>`, commit; `git add <file>` stages
  the rest. Check the intermediate state builds with `git stash push --keep-index`, the build
  command below, `git stash pop`.

### Tests

- The native app has no tests beyond the template `ExampleUnitTest`. Before a commit that touches
  it, `./gradlew assembleDebug` succeeds.
- In the Flutter app, `flutter analyze` and `flutter test` are green before every commit.
- Every feature comes with tests, but only the necessary ones — do not over-test.
- A throwaway test (a screenshot capture, a one-off probe) is deleted, never committed.
- Passing tests do not prove how a screen looks. After a visual change, render it and compare with
  the mock-up.

### Build and run

```sh
./gradlew assembleDebug        # debug APK in app/build/outputs/apk/debug/
./gradlew installDebug         # the same, installed on a connected phone or emulator
./gradlew testDebugUnitTest
```

- The build needs `app/google-services.json` from the Firebase console. It is git-ignored, and so
  are the upload keystore (`*.jks`) and `app/release/`.
- Known problem, October 2026: from the command line the build stops at
  `:app:kaptGenerateStubsDebugKotlin` with an `IllegalAccessError` — kapt cannot reach
  `com.sun.tools.javac.main` in `jdk.compiler`. Fix that before relying on the commands above, and
  delete this note in the same commit.

### Platforms

iOS and Android must look and behave the same; develop them in sync and never fix a screen on one
and leave the other diverging. The differences that stay are the ones a platform forces: its store
and billing, its ad and tracking consent, its sharing and file-access model.

### Releases

- **Only when asked.** Fixes pile up in git first; nobody cuts a build after each one.
- The native app's version is `versionCode` and `versionName` in `app/build.gradle.kts`. The
  Flutter app's lives in `pubspec.yaml` as `X.Y.Z+N`. Every store upload needs a higher number than
  the last one, both platforms of one release carry the same `N`, and `N` carries on from the last
  `versionCode` Google Play has seen.
- The version bump is its own commit, `chore(release): build N`, holding the version file and
  nothing else.
- There is no release script here yet. `lino` and `audio-library` cut builds with a `release-build`
  skill (`../audio-library/.claude/skills/release-build`); port it once the Flutter app exists.
- The last step — uploading to the Play Console or App Store Connect — is the human's click.
