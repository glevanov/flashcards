# Svenska Flashcards

A personal, offline Android flashcard app for studying Swedish vocabulary.
Native Kotlin + Jetpack Compose.
No accounts, no network, no ads.

Vocabulary decks come from the companion repo [`../swedish-study`](../swedish-study)
and are bundled into the APK as assets.
Target level is B1/B2, some vocabulary is based on Rivstart curriculum.

Distributed via GitHub releases → [Obtanium](https://obtainium.imranr.dev/).
Not published to any app store.

Pronunciation is bundled in-app via Piper Alma (`sv_SE`) + sherpa-onnx,
so it works fully offline without relying on Android system voice packs.
The `espeak-ng-data` assets are trimmed to the Swedish-only subset needed at runtime.

## Documentation

| Doc | Contents |
|---|---|
| [PLAN.md](PLAN.md) | App design, user interaction, decisions, roadmap |
| [docs/SETUP.md](docs/SETUP.md) | Local dev environment setup (Fedora) |
| [docs/RELEASING.md](docs/RELEASING.md) | CI pipeline, signing, Obtanium distribution |
| [AGENTS.md](AGENTS.md) | Conventions for humans and coding agents |

## Debug build
- Connect the phone over USB
- Authorize using USB debugging
- Run `./gradlew installDebug`

## Status

Phase 4 done: study sessions with Leitner SRS + Room persistence, bundled
Swedish Piper TTS pronunciation, session direction choice (Swedish↔English),
stats screen (box distribution), and settings (new-cards-per-day, TTS toggle).
All core features complete — Phase 5 is the GitHub Actions release pipeline +
Obtanium distribution (`./gradlew assembleDebug`).
See [PLAN.md](PLAN.md) for the roadmap.
