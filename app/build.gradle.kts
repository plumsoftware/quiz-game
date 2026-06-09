plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "ru.plumsoftware.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.plumsoftware.game"
        minSdk = 26
        targetSdk = 36
        versionCode = 10
        versionName = "1.2.8"

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
        compose = true
    }
    lint {
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
    
    // Icons
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    
    // Notifications
    implementation(libs.androidx.core.ktx.v1120)
    
    // WorkManager for scheduled notifications
    implementation(libs.androidx.work.runtime.ktx)

    // Ads
    implementation(libs.mobileads)

    // Fonts
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Firebase
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.firebase.config) // Or the latest stable version
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Animation
    implementation("androidx.compose.animation:animation-core")

    // UI enhancements
    implementation(libs.lottie.compose)
    implementation(libs.konfetti.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.splashscreen)
}