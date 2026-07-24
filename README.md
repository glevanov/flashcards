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

Phase 3 done: CSV decks + deck list with real due/new counts, study session with
card flip + Leitner SRS engine + Room persistence (next: TTS/reverse/stats in Phase 4).
(`./gradlew assembleDebug`).
See [PLAN.md](PLAN.md) for the roadmap.
