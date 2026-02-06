import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val appAbiFilters = "arm64-v8a;armeabi-v7a;x86_64"

plugins {
    jacoco
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

tasks.register<JacocoReport>("jacocoFilteredReport") {
    dependsOn("createDebugAndroidTestCoverageReport")

    group = "Reporting"
    description = "Generates filtered JaCoCo report excluding UI package and other noise"

    val coverageFiles =
        fileTree("$buildDir/outputs/code_coverage/debugAndroidTest/connected") {
            include("**/coverage.ec")
        }

    val kotlinClasses =
        fileTree("$buildDir/tmp/kotlin-classes/debug") {
            exclude(
                "**/ee/ria/DigiDoc/ui/**",
                "**/ee/ria/DigiDoc/fragment/**",
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
            )
        }

    val javaClasses =
        fileTree("$buildDir/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
            exclude(
                "**/ee/ria/DigiDoc/ui/**",
                "**/ee/ria/DigiDoc/fragment/**",
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
            )
        }

    classDirectories.setFrom(files(kotlinClasses, javaClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(coverageFiles)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    // Ignore if no coverage data is present (prevents failure on clean builds)
    onlyIf {
        coverageFiles.files.any { it.exists() }
    }
}

android {
    namespace = "ee.ria.DigiDoc"
    compileSdk = Integer.parseInt(libs.versions.compileSdkVersion.get())

    defaultConfig {
        applicationId = "ee.ria.DigiDoc"
        minSdk = Integer.parseInt(libs.versions.minSdkVersion.get())
        targetSdk = Integer.parseInt(libs.versions.targetSdkVersion.get())
        versionCode = Integer.parseInt(libs.versions.versionCode.get())
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.clear()
            abiFilters.addAll(appAbiFilters.split(';').map { it.trim() })
        }
    }

    signingConfigs {
        getByName("debug") {
            val assetsDir = project.projectDir.resolve("src/main/assets")
            storeFile = file(assetsDir.resolve("keystore/debug.keystore"))
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("debug")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            configure<CrashlyticsExtension> {
                nativeSymbolUploadEnabled = true
                mappingFileUploadEnabled = true
            }
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            configure<CrashlyticsExtension> {
                nativeSymbolUploadEnabled = true
                mappingFileUploadEnabled = true
            }
        }
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

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.preferencex)
    implementation(libs.guava)
    implementation(libs.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.dagger.hilt.android)
    implementation(libs.firebase.crashlytics.ktx)
    kapt(libs.google.dagger.hilt.android.compile)
    implementation(libs.androidx.hilt)
    implementation(libs.kotlinx.coroutines.rx3)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    androidTestImplementation(libs.byte.buddy)
    androidTestImplementation(libs.mockito.android) {
        exclude("net.bytebuddy")
    }
    androidTestImplementation(libs.mockito.kotlin) {
        exclude("net.bytebuddy")
    }

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.lifecycle.runtime.ktx)
    androidTestImplementation(libs.androidx.arch.core.testing)

    implementation(project(":libdigidoc-lib"))
    implementation(project(":mobile-id-lib"))
    implementation(project(":smart-id-lib"))
    implementation(project(":crypto-lib"))
    implementation(project(":config-lib"))
    implementation(project(":networking-lib"))
    implementation(project(":utils-lib"))
    implementation(project(":commons-lib"))
    implementation(project(":id-card-lib"))
    implementation(project(":web-eid-lib"))

    androidTestImplementation(project(":commons-lib:test-files"))
}
