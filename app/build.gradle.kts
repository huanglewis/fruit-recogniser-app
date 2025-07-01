plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Compose plugin is intentionally omitted
}

android {
    namespace = "iss.nus.edu.sg.fragments.workshop.fruitrecogniserapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "iss.nus.edu.sg.fragments.workshop.fruitrecogniserapp"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        // Compose is disabled for XML layout usage
        // compose = true
    }

    // Ensure ABI splits are enabled and cover common Android devices
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    // Critical: Ensures TFLite native .so libs (e.g. task_vision_jni.so) are included in APK
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // Core AndroidX and Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Required for AppCompat + XML themes
    implementation("androidx.appcompat:appcompat:1.6.1")

    // TensorFlow Lite (match versions to ensure compatibility)
    implementation("org.tensorflow:tensorflow-lite:2.9.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.3.1")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.3.1")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
