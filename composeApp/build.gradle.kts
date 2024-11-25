import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.sqldelight)
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
            baseName = "CycloneApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)

            implementation(libs.androidx.activity)
            implementation(libs.androidx.core)
            implementation(libs.material)

            implementation(libs.sqldelight.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.ui)
            implementation(compose.components.resources)

            implementation(libs.jetbrains.compose.lifecycle.viewmodel)
            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.jetbrains.compose.material3.window.size.clazz)
            implementation(libs.jetbrains.compose.adaptive)

            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.datetime)

            implementation(libs.coil)
            implementation(libs.coil.network)
            implementation(libs.coil.compose)
            implementation(libs.coil.test)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)

            implementation(libs.sqldelight.coroutines)

            implementation(libs.ksoup)

            implementation(libs.napier)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.sqldelight.sqlite)
            implementation(libs.compose.ui.preview.desktop)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native)
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("eu.heha.cyclone.database")
        }
    }
}

android {
    namespace = AppConfig.groupId
    compileSdk = AppConfig.targetSdk

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = AppConfig.groupId
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName

        testInstrumentationRunner = AppConfig.instrumentationRunner
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
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
    }
}

compose.desktop {
    application {
        mainClass = "eu.heha.cyclone.Cyclone"

        nativeDistributions {
            modules("java.sql")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = AppConfig.groupId
            packageVersion = AppConfig.versionName

            windows {
                iconFile.set(project.file("desktopIcons/ic_cyclone.ico"))
            }
            linux {
                iconFile.set(project.file("desktopIcons/ic_cyclone.png"))
            }
        }
    }
}
