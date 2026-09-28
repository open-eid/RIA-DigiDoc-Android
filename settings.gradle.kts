pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
sourceControl {
    gitRepository(uri("https://github.com/open-eid/nfc-android-lib.git")) {
        producesModule("ee.ria.DigiDoc:id-card-lib")
        producesModule("ee.ria.DigiDoc:smart-card-reader-lib")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RIA-DigiDoc"
include(":app")
include(":libdigidoc-lib")
include(":mobile-id-lib")
include(":smart-id-lib")
include(":crypto-lib")
include(":config-lib")
include(":networking-lib")
include(":utils-lib")
include(":commons-lib")
include(":id-card-lib")
include(":commons-lib:test-files")
include(":web-eid-lib")
