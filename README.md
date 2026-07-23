# Svenska Flashcards

A personal, offline Android flashcard app for studying Swedish vocabulary.
Native Kotlin + Jetpack Compose. No accounts, no network, no ads.

Vocabulary decks come from the companion repo
[`../swedish-study`](../swedish-study) and are bundled into the APK as assets.

Distributed via GitHub releases → [Obtanium](https://obtainium.imranr.dev/).
Not published to any app store.

## Documentation

| Doc | Contents |
|---|---|
| [PLAN.md](PLAN.md) | App design, user interaction, decisions, roadmap |
| [docs/SETUP.md](docs/SETUP.md) | Local dev environment setup (Fedora) |
| [docs/RELEASING.md](docs/RELEASING.md) | CI pipeline, signing, Obtanium distribution |
| [AGENTS.md](AGENTS.md) | Conventions for humans and coding agents |

## Status

Phase 2 done: bundled 27 CSV decks, CSV parser + deck list screen (real card counts; SRS/study session next)
(`./gradlew assembleDebug`). Decks and SRS engine land in later phases.
See [PLAN.md](PLAN.md) for the roadmap.
