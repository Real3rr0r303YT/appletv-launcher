plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.custom.appletvlauncher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.custom.appletvlauncher"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
    // Compose compiler 1.5.8 matches Kotlin 1.9.22
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}

dependencies {
    // Compose BOM 2024.02.00 = Compose 1.6.1 (compatible with compileSdk 34)
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    // Stable TV Material 3 (1.0.0-alpha11 was never published for tv-material)
    implementation("androidx.tv:tv-material:1.0.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // Accent colours for tiles and the Top Shelf backdrop
    implementation("androidx.palette:palette-ktx:1.0.0")
}
