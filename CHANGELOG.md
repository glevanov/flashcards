# Changelog

## 2

- fix crash on launch in minified builds: keep navigation route classes
  unrenamed (R8 renamed them, breaking kotlinx.serialization name lookup)
- fix crash when playing TTS in minified builds: keep app class names so
  JNI can resolve the Function1 callback's `invoke` method

## 1

Initial release: bundled decks (core + rivstart), Leitner study sessions, stats,
fully offline Swedish TTS.

- bundle Piper Alma (`sv_SE`) via sherpa-onnx for fully offline Swedish pronunciation
- replace the study-screen-scoped Android `TextToSpeech` instance with a process-wide `TtsManager` singleton that loads once per app process
- add bundled TTS assets, Swedish-only espeak data, and CC BY 4.0 attribution; release APK is ~92 MB (arm64, minified)
