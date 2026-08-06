import java.io.File
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Signing credentials come from environment variables (CI) or Gradle
// properties (-PKEYSTORE_BASE64=...). Never commit keystores or passwords.
// CI provides KEYSTORE_BASE64; a local KEYSTORE_FILE path is also supported.
fun credential(name: String): String? =
    System.getenv(name) ?: (findProperty(name) as String?)

val signingKeystoreFile = credential("KEYSTORE_FILE")?.let(::File)
val signingKeystoreBase64 = credential("KEYSTORE_BASE64")
val signingKeystorePassword = credential("KEYSTORE_PASSWORD")
val signingKeyAlias = credential("KEY_ALIAS")
val signingKeyPassword = credential("KEY_PASSWORD")

android {
    namespace = "io.levanov.flashcards"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.levanov.flashcards"
        minSdk = 26
        targetSdk = 35
        versionCode = (findProperty("versionCode") as String?)?.toIntOrNull() ?: 2
        versionName = (findProperty("versionName") as String?) ?: "2"
    }

    signingConfigs {
        create("release") {
            // NOTE: names must not collide with SigningConfig's own properties
            // (storeFile, keystorePassword, keyAlias, keyPassword) or Kotlin
            // resolves them against the receiver instead of the vals above.
            val file = signingKeystoreFile ?: signingKeystoreBase64?.let { b64 ->
                File.createTempFile("release-keystore", ".p12").apply {
                    writeBytes(Base64.getDecoder().decode(b64))
                    deleteOnExit()
                }
            }
            if (file != null && signingKeystorePassword != null && signingKeyAlias != null && signingKeyPassword != null) {
                storeFile = file
                storePassword = signingKeystorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // Signed only when credentials are present; otherwise the build
            // falls back to an unsigned APK (local builds without secrets).
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Real devices are arm64 (minSdk 26); x86 ABIs are only for
            // emulators, which use the debug build. Saves ~87 MB in the APK.
            ndk {
                abiFilters += listOf("arm64-v8a")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        noCompress += "onnx"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sherpa.onnx)

    testImplementation(libs.junit)
}