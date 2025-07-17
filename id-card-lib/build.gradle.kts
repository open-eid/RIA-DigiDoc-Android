import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ee.ria.DigiDoc.idCard"
    compileSdk = Integer.parseInt(libs.versions.compileSdkVersion.get())

    defaultConfig {
        minSdk = Integer.parseInt(libs.versions.minSdkVersion.get())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    packaging {
        resources {
            pickFirsts += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    api(project(":id-card-lib:id-lib"))
    api(project(":id-card-lib:smart-lib"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.bouncy.castle)
    implementation(libs.guava)
    implementation(libs.google.dagger.hilt.android)
    kapt(libs.google.dagger.hilt.android.compile)
    implementation(libs.androidx.hilt)
    implementation(libs.kotlinx.coroutines.rx3)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.byte.buddy)
    androidTestImplementation(libs.mockito.android) {
        exclude("net.bytebuddy")
    }
    androidTestImplementation(libs.mockito.kotlin) {
        exclude("net.bytebuddy")
    }

    implementation(project(":libdigidoc-lib"))
    implementation(project(":networking-lib"))
    implementation(project(":commons-lib"))
    implementation(project(":utils-lib"))
    implementation(project(":config-lib"))
    androidTestImplementation(project(":commons-lib:test-files"))
}
