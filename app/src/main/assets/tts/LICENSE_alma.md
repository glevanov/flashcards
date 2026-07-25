# Alma voice attribution

Bundled voice: Alma (`sv_SE`)

- Upstream voice collection: `rhasspy/piper-voices`
- Upstream path: `sv/sv_SE/alma/medium`
- Upstream model card: <https://huggingface.co/rhasspy/piper-voices/tree/main/sv/sv_SE/alma/medium>
- License: CC BY 4.0

Implementation note:
- `sv_SE-alma-medium.onnx` in this app comes from the sherpa-onnx `vits-piper-sv_SE-alma-medium` package so it includes the metadata sherpa-onnx needs at runtime.
- `sv_SE-alma-medium.onnx.json` below is the upstream Piper config from `rhasspy/piper-voices`.

Upstream model card:

# Model card for alma (medium)

* Language: sv_SE (Swedish, Sweden)
* Speakers: 1
* Quality: medium
* Samplerate: 22,050Hz
* URL: https://huggingface.co/yeagersthlm/piper-voice-sv-alma

## License

The model weights are released under CC BY 4.0, following the license of the NST training data.

## Training

Trained on the NST Swedish TTS Dataset (Språkbanken, National Library of Norway). The dataset contains high-quality studio recordings of Swedish speech.
