# Svenska Flashcards

A personal, offline Android flashcard app for studying Swedish vocabulary.
Native Kotlin + Jetpack Compose. No accounts, no network, no ads.

Vocabulary decks come from the companion repo
[`../swedish-study`](../swedish-study) and are bundled into the APK as assets.

Distributed via GitHub releases → [Obtanium](https://obtainium.imranr.dev/).
Not published to any app store.

Pronunciation is bundled in-app via Piper Alma (`sv_SE`) + sherpa-onnx, so it
works fully offline without relying on Android system voice packs. The app uses
JitPack for the `com.github.k2-fsa:sherpa-onnx:v1.13.4` AAR because sherpa-onnx
is not published on Maven Central. Runtime TTS assets come from sherpa-onnx's
published `vits-piper-sv_SE-alma-medium` package (needed for metadata,
`tokens.txt`, and `espeak-ng-data`), while the upstream Alma model card and
`.onnx.json` are also bundled for attribution/reference. The `espeak-ng-data`
assets are trimmed to the Swedish-only subset needed at runtime. The bundled
TTS assets add about 62 MB of model/data files; the current debug APK is about
200 MB.

## Documentation

| Doc | Contents |
|---|---|
| [PLAN.md](PLAN.md) | App design, user interaction, decisions, roadmap |
| [docs/SETUP.md](docs/SETUP.md) | Local dev environment setup (Fedora) |
| [docs/RELEASING.md](docs/RELEASING.md) | CI pipeline, signing, Obtanium distribution |
| [AGENTS.md](AGENTS.md) | Conventions for humans and coding agents |

## Status

Phase 4 done: study sessions with Leitner SRS + Room persistence, bundled
Swedish Piper TTS pronunciation, reverse mode, stats screen (box
distribution), and settings (new-cards-per-day, TTS toggle). All core features
complete — Phase 5 is the GitHub Actions release pipeline + Obtanium
distribution (`./gradlew assembleDebug`).
See [PLAN.md](PLAN.md) for the roadmap.
