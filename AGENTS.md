# Conventions for contributors and coding agents

Read [PLAN.md](PLAN.md) first — it contains the design, the locked-in
decisions, and the roadmap. Don't deviate from locked decisions without asking.

## Project layout (target state)

```
flashcards/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/vocab/          # bundled deck CSVs (see below)
│       ├── java/io/levanov/flashcards/
│       │   ├── MainActivity.kt
│       │   ├── data/              # CSV parsing, Room entities/DAO/DB
│       │   ├── srs/               # Leitner engine (pure Kotlin, unit-tested)
│       │   └── ui/                # Compose screens: home, study, stats
│       └── res/
├── scripts/
│   └── sync-decks.sh              # copies decks from ../swedish-study
├── docs/
├── PLAN.md
└── .github/workflows/release.yml  # Phase 5
```

## Code conventions

- Kotlin, Jetpack Compose, Material 3. Single activity + Compose navigation.
- `minSdk 26`, `compileSdk 37` (bump deliberately, not casually).
- No network permission. The app is fully offline — do not add
  `android.permission.INTERNET`.
- No analytics, crash reporting, or third-party trackers.
- The SRS engine (`srs/`) must stay pure Kotlin with no Android dependencies
  so it can be unit-tested on the JVM.
- Follow the design in [PLAN.md](PLAN.md) for UI and interaction.

## Deck conventions (assets/vocab/)

Decks are **copied, not moved or edited here** — the source of truth is
`../swedish-study/vocab/`. Use `scripts/sync-decks.sh` (rsync-based) to copy.

CSV format (must stay compatible with `swedish-study/cards/flashcards.py`):

- Columns: `swedish,english,example` — exactly 3, in this order.
- First row may be a header (`swedish,english,...`) — skip it if present.
- Ignore blank lines and rows whose first cell starts with `#`.
- Fields containing commas are quoted (standard CSV).
- Deck name = path under `vocab/` minus `.csv`, `/`-separated
  (`core/adjectives`, `rivstart/kapitel-01`).
- Card key = `<deck>::<swedish>` — this key is the Room primary key, so
  **renaming a deck or a Swedish term orphans its SRS state** (acceptable for
  a personal app, but be aware).

## SRS rules (do not change without updating PLAN.md)

- Leitner boxes 1–6, intervals in days: `{1: 0, 2: 1, 3: 3, 4: 7, 5: 16, 6: 35}`.
- Correct → box + 1 (cap 6). Wrong → box 1. `due = today + interval[box]`.
- Session = due cards (shuffled) + up to N new cards (shuffled; N from
  settings, default 10).

## Build & release conventions

- Build: `./gradlew assembleDebug` / `installDebug` (phone over adb).
- Signing config reads credentials from environment variables / gradle
  properties — **never hardcode or commit keystore or passwords**. See
  [docs/RELEASING.md](docs/RELEASING.md).
- Every release: bump `versionCode` (monotonic) and `versionName`, then run
  the Release workflow manually from the Actions tab, entering the new
  version number (plain integer). The workflow builds the signed APK and
  creates the GitHub release + tag.
- Keep `app-release.apk` filename stable in CI output (`svenska-flashcards-<tag>.apk`)
  so Obtanium's APK matching keeps working.

## Commit conventions

This repo uses [Conventional Commits](https://www.conventionalcommits.org/):

- Format: `<type>(<optional scope>): <description>`.
- Common types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `build`, `ci`.
- Description in imperative mood, lowercase, no trailing period
  (e.g. `feat(study): add card flip animation`, `docs: fix setup typo`).
- Releases are created by running the Release workflow manually; versions
  are plain integers (`1`, `2`, …) — see
  [docs/RELEASING.md](docs/RELEASING.md).

## Gotchas

- Fedora: system Java may be a headless JRE. Gradle must use JDK 21 via
  `org.gradle.java.home` in `~/.gradle/gradle.properties` (see
  [docs/SETUP.md](docs/SETUP.md)) — do not commit machine-specific paths to
  the repo's `gradle.properties`.
- `assets/` files are case-sensitive on device but the build machine FS may
  not be — keep deck filenames lowercase.
- Swedish characters (å/ä/ö): all CSVs and source files are UTF-8.
