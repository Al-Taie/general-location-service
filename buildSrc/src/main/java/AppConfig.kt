package com.altaie.buildscr

import org.gradle.api.JavaVersion

object AppConfig {
    const val ENABLE_R8_FULL_MODE: Boolean = true
    const val IS_RELEASE_MODE_DEBUGGABLE: Boolean = false

    object Version {
        const val MIN_SDK = 21
        const val TARGET_SDK = 35
        const val COMPILE_SDK = 35
        val JVM = JavaVersion.VERSION_17
    }

    const val APPLICATION_ID = "com.altaie.glsapp"
    const val LIBRARY_ID = "com.altaie.gls"
    const val ANDROID_TEST_INSTRUMENTATION = "androidx.test.runner.AndroidJUnitRunner"
}
