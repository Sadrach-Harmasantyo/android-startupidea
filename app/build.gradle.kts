plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.startupidea"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.startupidea"
        minSdk = 30
        targetSdk = 34
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }
}

dependencies {
    // Jetpack Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Supabase Kotlin SDK
    implementation("io.github.jan-tennert.supabase:supabase-kt:1.3.2")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.3.2")
    implementation("io.github.jan-tennert.supabase:storage-kt:1.3.2") // Tambahkan dependensi Storage
    implementation("io.ktor:ktor-client-okhttp:2.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coil untuk menampilkan gambar
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.androidx.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ViewModel untuk Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Supabase Auth
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.3.2")

    // Coroutines (jika belum ada)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Permission handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
}
