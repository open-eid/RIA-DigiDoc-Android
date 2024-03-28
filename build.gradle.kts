// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.jlleitschuhKtlint)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
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

// Run ktlint before build
gradle.projectsEvaluated {
    val ktlint = project.tasks.getByName("ktlintCheck")

    subprojects {
        if (project.tasks.findByName("preBuild") != null) {
            project.tasks.getByName("preBuild").dependsOn(ktlint)
        }
    }
}
