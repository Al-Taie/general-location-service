import com.altaie.buildscr.AppConfig
import com.altaie.buildscr.AppConfig.Version


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id 'com.google.gms.google-services' // Google services Gradle plugin
}

android {
    namespace = AppConfig.APPLICATION_ID
    compileSdk = Version.COMPILE_SDK

    defaultConfig {
        applicationId = AppConfig.APPLICATION_ID
        minSdk = Version.MIN_SDK
        targetSdk = Version.TARGET_SDK
        versionCode = 8
        //versionName = "0.0.${versionCode - 1}"
        testInstrumentationRunner = AppConfig.ANDROID_TEST_INSTRUMENTATION
    }

    buildTypes {
        release {
            isMinifyEnabled = AppConfig.ENABLE_R8_FULL_MODE
            isShrinkResources = AppConfig.ENABLE_R8_FULL_MODE
            isDebuggable = AppConfig.IS_RELEASE_MODE_DEBUGGABLE
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = Version.JVM
        targetCompatibility = Version.JVM
    }
    kotlinOptions {
        jvmTarget = Version.JVM.toString()
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.timber)
    implementation(projects.gls)

    // Google Maps Location Services
    implementation(libs.play.services.maps)
}
