plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ee.ria.DigiDoc.mobileId"
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

    packaging {
        resources {
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
            pickFirsts += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
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
    implementation(libs.gson)
    implementation(libs.preferencex)
    implementation(libs.google.dagger.hilt.android)
    kapt(libs.google.dagger.hilt.android.compile)
    implementation(libs.androidx.hilt)

    testImplementation(libs.junit)
    androidTestImplementation(libs.retrofit.mock)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.lifecycle.runtime.ktx)
    androidTestImplementation(libs.androidx.arch.core.testing)

    implementation(project(":libdigidoc-lib"))
    implementation(project(":networking-lib"))
    implementation(project(":utils-lib"))
    implementation(project(":commons-lib"))
    implementation(project(":config-lib"))
}
