# Changelog

## 1

Initial release: bundled decks (core + rivstart), Leitner study sessions, stats,
fully offline Swedish TTS.

- bundle Piper Alma (`sv_SE`) via sherpa-onnx for fully offline Swedish pronunciation
- replace the study-screen-scoped Android `TextToSpeech` instance with a process-wide `TtsManager` singleton that loads once per app process
- add bundled TTS assets, Swedish-only espeak data, and CC BY 4.0 attribution; release APK is ~92 MB (arm64, minified)
