import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ee.ria.DigiDoc.webEid"
    compileSdk = Integer.parseInt(libs.versions.compileSdkVersion.get())

    defaultConfig {
        minSdk = Integer.parseInt(libs.versions.minSdkVersion.get())
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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
            enableUnitTestCoverage = project.hasProperty("coverageEnabled")
            enableAndroidTestCoverage = project.hasProperty("coverageEnabled")
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
    implementation(libs.gson)
    implementation(libs.preferencex)

    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compile)
    implementation(libs.androidx.hilt)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    implementation(project(":libdigidoc-lib"))
    implementation(project(":networking-lib"))
    implementation(project(":utils-lib"))
    implementation(project(":commons-lib"))
    implementation(project(":config-lib"))

    androidTestImplementation(project(":commons-lib:test-files"))
}
