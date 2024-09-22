/*
 * Copyright (c) 2023.  Müller & Wulff. All rights reserved.
 */

import org.gradle.api.Project

object ReleaseFolders {
    fun Project.aabFolder(buildType: BuildType) =
        "$buildDir/outputs/bundle/${buildType.literal}"

    fun Project.apkFolder(buildType: BuildType = BuildType.DEBUG) =
        "$buildDir/outputs/apk/${buildType.literal}"

    fun Project.mappingFile(buildType: BuildType) =
        "$buildDir/outputs/mapping/${buildType.literal}/mapping.txt"

    enum class BuildType(val literal: String) {
        DEBUG("debug"), RELEASE("release")
    }
}