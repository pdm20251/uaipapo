plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Video
    alias(libs.plugins.dagger.hilt)
    kotlin("kapt")

    // Para utilizar Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.uaipapo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.uaipapo"
        minSdk = 27
        targetSdk = 36
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Setup do tutorial
    implementation(libs.dagger.hilt.android)
    implementation(libs.firebase.crashlytics.buildtools)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.dagger.hilt.compose)
    implementation(libs.coil)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    // Volley
    implementation("com.android.volley:volley:1.2.1")

    // PDF Viewer
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2") // <-- VERSÃO ALTERADA
/*
    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:2.2.3"))
        // Supabase Core
        implementation("io.github.jan-tennert.supabase:postgrest-kt")
        // Supabase Armazenamento de Arquivos
        implementation("io.github.jan-tennert.supabase:storage-kt")

    // GoogleAPI que envolve GogleCredentials
    implementation("com.google.api-client:google-api-client:2.4.0")*/
}
