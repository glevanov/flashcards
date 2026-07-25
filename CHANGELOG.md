# Changelog

## Unreleased

- bundle Piper Alma (`sv_SE`) via sherpa-onnx for fully offline Swedish pronunciation
- replace the study-screen-scoped Android `TextToSpeech` instance with a process-wide `TtsManager` singleton that loads once per app process
- add bundled TTS assets, Swedish-only espeak data, and CC BY 4.0 attribution; current debug APK size is about 200 MB
