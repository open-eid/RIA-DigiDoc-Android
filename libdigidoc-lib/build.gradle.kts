import ee.ria.DigiDoc.libdigidoc.update.LibdigidocppPlugin

val appAbiFilters = "arm64-v8a;armeabi-v7a;x86_64"

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

apply<LibdigidocppPlugin>()

android {
    namespace = "ee.ria.DigiDoc.libdigidoclib"
    compileSdk = Integer.parseInt(libs.versions.compileSdkVersion.get())

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")
        minSdk = Integer.parseInt(libs.versions.minSdkVersion.get())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.clear()
            abiFilters.addAll(appAbiFilters.split(';').map { it.trim() })
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.bouncy.castle)
    implementation(libs.google.dagger.hilt.android)
    implementation(libs.preferencex)
    kapt(libs.google.dagger.hilt.android.compile)
    implementation(libs.androidx.hilt)

    testImplementation(libs.junit)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.lifecycle.runtime.ktx)

    api(files("libs/libdigidocpp.jar"))

    implementation(project(":networking-lib"))
    implementation(project(":utils-lib"))
    implementation(project(":commons-lib"))
    implementation(project(":config-lib"))

    androidTestImplementation(project(":commons-lib:test-files"))
}
