plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.termlauncher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.termlauncher"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.0.2"
    }

    signingConfigs {
        // Stable release key, fed from CI secrets via env vars so every release
        // is signed with the SAME key and installs as an in-place update (no
        // uninstall, no data loss). Populated only when RELEASE_KEYSTORE_FILE is
        // set; local builds fall back to the debug key below.
        create("release") {
            System.getenv("RELEASE_KEYSTORE_FILE")?.let { path ->
                storeFile = file(path)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the stable release keystore when CI provides it; otherwise the
            // debug key so local `assembleRelease` still works without setup.
            signingConfig = if (System.getenv("RELEASE_KEYSTORE_FILE") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
