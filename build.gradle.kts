plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.googleDagger) apply false
    alias(libs.plugins.jlleitschuhKtlint)
}

subprojects {
    // KtLint
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        debug.set(true)
        verbose.set(true)
        android.set(true)
        ignoreFailures.set(false)
        outputToConsole.set(true)
        outputColorName.set("RED")
        coloredOutput.set(true)
        additionalEditorconfig.set(
            mapOf(
                "max_line_length" to "120",
            ),
        )
    }
}
