# sherpa-onnx ships no consumer rules (empty proguard.txt) and its SWIG JNI
# bindings are resolved by name from native code (Java_com_k2fsa_* symbols),
# so R8 must not rename or strip the bindings or TTS crashes at runtime.
-keep class com.k2fsa.sherpa.onnx.** { *; }

# Keep the app package unrenamed. Renaming breaks runtime name-based lookups:
# (a) type-safe navigation routes (NavGraph.kt) are resolved by fully
#     qualified name via kotlinx.serialization — "Cannot find class with
#     name ..." at launch;
# (b) Kotlin lambdas passed to sherpa-onnx are invoked by native code via
#     GetMethodID("invoke", ...) — renaming them aborts with
#     NoSuchMethodError at the JNI boundary when TTS is used.
# R8 still performs dead-code elimination and optimization, so the APK stays
# small; only names are preserved.
-keep class io.levanov.flashcards.** { *; }
