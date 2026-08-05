# sherpa-onnx ships no consumer rules (empty proguard.txt) and its SWIG JNI
# bindings are resolved by name from native code (Java_com_k2fsa_* symbols),
# so R8 must not rename or strip the bindings or TTS crashes at runtime.
-keep class com.k2fsa.sherpa.onnx.** { *; }
