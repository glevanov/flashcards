# Svenska Flashcards

A personal, offline Android flashcard app for studying Swedish vocabulary.
Native Kotlin + Jetpack Compose.
No accounts, no network, no ads.

Vocabulary decks come from the companion repo [`../swedish-study`](../swedish-study)
and are bundled into the APK as assets.
Target level is B1/B2, some vocabulary is based on Rivstart curriculum.

Distributed via GitHub releases and [Obtanium](https://obtainium.imranr.dev/).

Pronunciation is bundled in-app via Piper Alma (`sv_SE`) + sherpa-onnx,
so it works fully offline without relying on Android system voice packs.
The `espeak-ng-data` assets are trimmed to the Swedish-only subset needed at runtime.

## Documentation

| Doc | Contents |
|---|---|
| [CHANGELOG.md](CHANGELOG.md) | Release history |
| [docs/SETUP.md](docs/SETUP.md) | Local dev environment setup (Fedora) |
| [docs/RELEASING.md](docs/RELEASING.md) | CI pipeline, signing, Obtanium distribution |
| [AGENTS.md](AGENTS.md) | Conventions for humans and coding agents |
