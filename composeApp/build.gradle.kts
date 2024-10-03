import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
//            implementation(libs.androidx.lifecycle.viewmodel)
//            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "eu.heha.cyclone"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "eu.heha.cyclone"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures.compose = true
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    dependencies {
        implementation(libs.kotlinx.coroutines)
        testImplementation(libs.junit)
        coreLibraryDesugaring(libs.core.desugar)

        implementation(libs.androidx.activity)
        implementation(libs.androidx.core)
        implementation(libs.androidx.lifecycle.lifecycle)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.androidx.navigation.compose)
        implementation(libs.androidx.hilt.navigation.compose)
        implementation(libs.androidx.webkit)

        val composeBom = platform(libs.compose.bom)
        implementation(composeBom)

        implementation(libs.compose.material3)
        implementation(libs.compose.material.navigation)
        implementation(libs.compose.ui.tooling.preview)
        debugImplementation(libs.compose.ui.tooling)
        implementation(libs.compose.material.icons)

        implementation(libs.coil)
        implementation(libs.coil.network)
        implementation(libs.coil.compose)
        implementation(libs.material)

        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.cio)

        implementation(libs.ksoup)

        implementation(libs.napier)

        implementation(platform(libs.koin.bom))
        implementation(libs.koin.core)
        implementation(libs.koin.compose)
        implementation(libs.koin.compose.navigation)

        testImplementation(kotlin("test"))
        testImplementation(libs.junit)

        androidTestImplementation(libs.androidx.test.ext.junit)
        androidTestImplementation(libs.espresso.core)
        androidTestImplementation(composeBom)
        androidTestImplementation(libs.androidx.ui.test.junit4)

        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
    }
}

compose.desktop {
    application {
        mainClass = "eu.heha.cyclone.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "eu.heha.cyclone"
            packageVersion = "1.0.0"
        }
    }
}
