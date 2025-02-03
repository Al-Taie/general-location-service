import com.altaie.buildscr.AppConfig
import com.altaie.buildscr.AppConfig.Version


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = AppConfig.LIBRARY_ID
    compileSdk = Version.COMPILE_SDK

    defaultConfig {
        multiDexEnabled = true
        minSdk = Version.MIN_SDK
        testInstrumentationRunner = AppConfig.ANDROID_TEST_INSTRUMENTATION
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = Version.JVM
        targetCompatibility = Version.JVM
    }
    kotlinOptions {
        jvmTarget = Version.JVM.toString()
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("release"))
            }
        }
    }
}

dependencies {
    // Dependencies
    implementation(libs.activity.ktx)
    implementation(libs.timber)
    implementation(libs.gson)
    implementation(libs.coroutines)

    // Google
    implementation(libs.gmsLocation)

    // Huawei
    implementation(libs.hmsLocation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    api(libs.prettycode)
}
