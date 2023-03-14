/*
 * Copyright (c) 2023.  Müller & Wulff. All rights reserved.
 */

import org.gradle.api.Project

object AppConfig {
    const val minSdk = 26
    const val targetSdk = 33
    const val versionCode = 1
    const val versionName = "1.0.0-dev01"

    const val instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    fun Project.releaseFolder() = "$rootDir/releases"

    fun Project.appName(type: String, versionSuffix: String = "") =
        "${rootProject.name}.$versionName$versionSuffix.$versionCode.$type"
}