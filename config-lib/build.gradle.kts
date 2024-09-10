plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ee.ria.DigiDoc.configuration"
    compileSdk = Integer.parseInt(libs.versions.compileSdkVersion.get())

    defaultConfig {
        minSdk = Integer.parseInt(libs.versions.minSdkVersion.get())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    api(libs.gson)
    api(libs.bouncy.castle)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.google.dagger.hilt.android)
    kapt(libs.google.dagger.hilt.android.compile)
    implementation(libs.androidx.hilt)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.arch.core.testing)

    debugImplementation(project(":networking-lib", "debugRuntimeElements"))
    releaseImplementation(project(":networking-lib", "releaseRuntimeElements"))
    debugImplementation(project(":utils-lib", "debugRuntimeElements"))
    releaseImplementation(project(":utils-lib", "releaseRuntimeElements"))
    testImplementation(project(":utils-lib"))

    androidTestImplementation(project(":commons-lib:test-files"))
}

configurations {
    create("generateMatchers") {
        extendsFrom(configurations["api"], configurations["releaseImplementation"])
        attributes {
            attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
            attribute(Attribute.of("com.android.build.api.attributes.BuildTypeAttr", String::class.java), "release")
        }
    }
}

tasks.register<JavaExec>("fetchAndPackageDefaultConfiguration") {
    dependsOn("build")
    dependsOn(":networking-lib:build")
    classpath(files("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/release"))
    classpath(configurations["generateMatchers"])
    mainClass = "ee.ria.DigiDoc.configuration.task.FetchAndPackageDefaultConfigurationTask"

    doLast {
        copy {
            from(file("$projectDir/src/main/assets/config"))
            into(file("${layout.buildDirectory.get().asFile}/intermediates/library_assets/debug/out/config"))
        }
        copy {
            from(file("$projectDir/src/main/assets/config"))
            into(file("${layout.buildDirectory.get().asFile}/intermediates/library_assets/release/out/config"))
        }
    }
}
