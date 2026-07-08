import ee.ria.DigiDoc.libcdoc.update.LibcdocPlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val appAbiFilters = "arm64-v8a;armeabi-v7a;x86_64".split(';').map { it.trim() }

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

apply<LibcdocPlugin>()

android {
    namespace = "ee.ria.DigiDoc.cryptolib"
    compileSdk = Integer.parseInt(libs.versions.compileSdkVersion.get())

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")
        minSdk = Integer.parseInt(libs.versions.minSdkVersion.get())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.clear()
            abiFilters.addAll(appAbiFilters)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = project.hasProperty("coverageEnabled")
            enableAndroidTestCoverage = project.hasProperty("coverageEnabled")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compile)
    implementation(libs.androidx.hilt)
    implementation(libs.bouncy.castle)
    api(libs.guava)
    implementation(libs.unboundid.ldapsdk)
    implementation(libs.okhttp3)
    implementation(libs.cdoc4j)
    implementation(libs.preferencex)
    implementation(libs.stax.api)

    testImplementation(libs.junit)

    androidTestImplementation(libs.byte.buddy)
    androidTestImplementation(libs.mockito.android) {
        exclude("net.bytebuddy")
    }
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.lifecycle.runtime.ktx)

    api(files("libs/libcdoc.jar"))

    implementation(project(":config-lib"))
    implementation(project(":commons-lib"))
    implementation(project(":networking-lib"))
    implementation(project(":utils-lib"))
    implementation(project(":id-card-lib"))
}
