plugins {
    id("com.android.application") // AGP 9 incluye compilación Kotlin: no aplicar kotlin-android
}

android {
    namespace = "com.example.fotobitacora"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.fotobitacora"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
}
