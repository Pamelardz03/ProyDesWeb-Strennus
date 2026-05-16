plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.estudiante.strennus_proyweb"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.estudiante.strennus_proyweb"
        minSdk = 24
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // --- NUEVO: Networking ---
    implementation(libs.retrofit)                      // Cliente HTTP
    implementation(libs.converter.gson)                // Convertidor JSON -> Kotlin
    implementation(platform(libs.okhttp.bom))          // BOM para versiones compatibles
    implementation(libs.okhttp)                        // OkHttp (transporte HTTP)
    implementation(libs.okhttp.logging)                // Logging de peticiones

    // --- NUEVO: Carga de imagenes ---
    implementation(libs.coil)                          // Coil

    // --- NUEVO: Coroutines ---
    implementation(libs.kotlinx.coroutines.android)    // Operaciones asincronas

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}