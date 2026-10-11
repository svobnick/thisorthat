# This or That

Mobile client for the Russian "This or That" game («То или Это», https://thisorthat.ru): the player
picks one of two answers to a question and sees how everyone else voted. This repo holds two
versions of it:

- `app/` — the native Android app (Kotlin, Moxy MVP, Dagger 2, Volley), the one published on Google
  Play as `com.svobnick.thisorthat`.
- `mobile/` — the Flutter app for iOS and Android that replaces it. It does everything the native
  app does, in the refreshed design, and is not in the stores yet.

## The idea

The next version replaces the native app with a new one:

- **Flutter, iOS and Android.** One codebase for both stores, built the way the sibling `together`
  and `lino` apps are (`../together`, `../lino`). Done: `mobile/`.
- **A refreshed design.** The current mock-up is `../thisorthatnew/to-ili-eto-v5.html`, outside
  this repo. Done for the screens the mock-up draws; the others (comments, new question, profile)
  are drawn in its style.
- **18 languages.** Russian plus the 17 other locales `together` and `lino` ship: `da`, `de`, `en`,
  `es`, `fi`, `fr`, `it`, `ja`, `ko`, `nb`, `nl`, `pl`, `pt`, `sv`, `tr`, `zh`, `zh_Hant`. Only
  Russian so far; every string is already an ARB key in `mobile/lib/l10n/app_ru.arb`.
- **Ads.** The app earns through advertising. Not started.

What the Flutter app still lacks next to the native one: Firebase (Analytics and Crashlytics). It
needs an iOS app registered in the Firebase console first.

Two things in the mock-up go beyond what the current API can do and wait for the new backend (see
below). The onboarding asks for age and gender: the app keeps the answers on the device
(`profile.json`) and sends them nowhere. Questions with pictures are not built at all: the API has
only the two texts.

## Backend

**A new backend will be written for the Flutter app** (decided 2026-10-12). Until it exists the
Flutter app is worked on with tests and mocks only: it runs on the mock backend built into it,
`mobile/lib/api/mock_backend.dart`, and nothing in development goes to the production API. The mock
answers the same requests with the same JSON as the current API, described here:

- The backend is a separate repository with its own owner:
  https://github.com/antonlukin/thisorthat — a PHP API (`api/`), the React web app (`src/`), cron
  jobs and the API docs. This repo holds only the mobile client.
- Production API is `https://api.thisorthat.ru/`, documented at https://docs.thisorthat.ru. Every
  call the Flutter app makes is in `mobile/lib/api/api_client.dart`; the native app's are in
  `app/src/main/kotlin/com/svobnick/thisorthat/service/ApiRequests.kt`.
- Users are anonymous. On first launch the app calls `/register` and keeps the returned token in
  the file `authToken` under the app's `filesDir`; the token is the only link to a user's favorites
  and own questions. The Flutter app reads the same file on Android, so an install upgraded from
  the native app keeps its user. (The native app registers with the Firebase installation id, the
  Flutter app with a random one; the server does not care.)
- `/setViewed` adds a vote to the public counter every time it receives one, even for a question
  this user already answered. Each question must be sent exactly once.
- `/getItems` returns up to 30 questions the user has not answered. A short or empty list is not
  the end: the server walks its sections one call at a time, so the client asks again.
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
  `flutter test` does not regenerate the strings: run `flutter gen-l10n` after editing an ARB (the
  generated `app_localizations*.dart` are not committed).
- Every feature comes with tests, but only the necessary ones — do not over-test.
- A throwaway test (a screenshot capture, a one-off probe) is deleted, never committed.
- Passing tests do not prove how a screen looks. After a visual change, render it and compare with
  the mock-up.

### Build and run

The Flutter app, from `mobile/`:

```sh
flutter run                    # on the booted simulator, emulator or a connected phone
flutter analyze && flutter test
flutter build apk --debug      # build/app/outputs/flutter-apk/app-debug.apk
flutter build ios --simulator
```

- With no `API_BASE_URL` the app runs on its built-in mock backend: made-up questions, and
  whatever the person does kept in memory until the app closes. The mock keeps its files in a
  `mock/` folder of its own, so it never replaces the token that ties an install to a real user.
- `--dart-define=API_BASE_URL=<url>` points the app at a server instead. The production API still
  speaks this protocol; every vote, comment and question sent there is real.
- Both apps share one application id, so installing one over the other on a device replaces it and
  keeps its data.

The native app, from the repo root:

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
  Flutter app's lives in `mobile/pubspec.yaml` as `X.Y.Z+N`. Every store upload needs a higher number than
  the last one, both platforms of one release carry the same `N`, and `N` carries on from the last
  `versionCode` Google Play has seen.
- The version bump is its own commit, `chore(release): build N`, holding the version file and
  nothing else.
- A store build has to name its server with `--dart-define=API_BASE_URL=…`; without it the app
  ships on the mock.
- There is no release script here yet. `lino` and `audio-library` cut builds with a `release-build`
  skill (`../audio-library/.claude/skills/release-build`); port it once the Flutter app exists.
- The last step — uploading to the Play Console or App Store Connect — is the human's click.
