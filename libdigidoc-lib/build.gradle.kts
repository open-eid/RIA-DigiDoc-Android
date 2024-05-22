import ee.ria.DigiDoc.libdigidoc.update.LibdigidocppPlugin

val appAbiFilters = "arm64-v8a;armeabi-v7a;x86_64"

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.guava)

    implementation(libs.pdfbox.android) {
        exclude(group = "org.bouncycastle")
    }
    implementation(project(":networking-lib"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(files("libs/libdigidocpp.jar"))

    implementation(project(":utils-lib"))
    implementation(project(":commons-lib"))
    implementation(project(":config-lib"))
}
