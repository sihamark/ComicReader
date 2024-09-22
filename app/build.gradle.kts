plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.hamark.comicreader"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.hamark.comicreader"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-alpha01"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    testImplementation(libs.junit)
    coreLibraryDesugaring(libs.core.desugar)

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.lifecycle)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    implementation(libs.compose.material3)
    implementation(libs.compose.material.navigation)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material.icons)

    implementation(libs.napier)

    implementation(libs.coil.compose)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    implementation(libs.jsoup)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}