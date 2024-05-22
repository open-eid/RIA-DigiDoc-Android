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
}

dependencies {

    api(libs.okhttp3)
    api(libs.retrofit)
    api(libs.logging.interceptor)
    api(libs.converter.scalars)
    api(libs.converter.gson)
    api(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.commons.text)
    implementation(libs.preferencex)
    testImplementation(libs.junit)
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
