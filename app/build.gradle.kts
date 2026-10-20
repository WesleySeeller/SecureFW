plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.wesley.securefw"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wesley.securefw"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

// REMOVA o bloco kapt { ... } completamente

dependencies {
    // Compose e AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Room
    val roomVersion = "2.7.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.53.1")
    ksp("com.google.dagger:hilt-compiler:2.53.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Security Crypto
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.9")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.code.gson:gson:2.11.0")

    // Coroutines para testes
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

// MockK - biblioteca de mocks para Kotlin
    testImplementation("io.mockk:mockk:1.13.13")

// Turbine - para testar Flows
    testImplementation("app.cash.turbine:turbine:1.1.0")

// Truth - asserções mais legíveis
    testImplementation("com.google.truth:truth:1.4.4")
    androidTestImplementation("io.mockk:mockk:1.13.13")
    androidTestImplementation("io.mockk:mockk-android:1.13.13")

    testImplementation("androidx.room:room-testing:2.7.1")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
}