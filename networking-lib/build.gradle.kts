plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "ee.ria.DigiDoc.network"
    compileSdk = Integer.parseInt(libs.versions.compileSdkVersion.get())

    defaultConfig {
        minSdk = Integer.parseInt(libs.versions.minSdkVersion.get())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

    api(libs.okhttp3)
    api(libs.okhttp3.logging.interceptor)
    api(libs.retrofit)
    api(libs.retrofit.converter.scalars)
    api(libs.retrofit.converter.gson)
    api(libs.bouncy.castle)
    api(libs.jackson.databind)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.commons.text)
    implementation(libs.preferencex)
    implementation(libs.bouncy.castle)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.lifecycle.runtime.ktx)

    debugImplementation(project(":utils-lib", "debugRuntimeElements"))
    releaseImplementation(project(":utils-lib", "releaseRuntimeElements"))
    debugImplementation(project(":commons-lib", "debugRuntimeElements"))
    releaseImplementation(project(":commons-lib", "releaseRuntimeElements"))
}
