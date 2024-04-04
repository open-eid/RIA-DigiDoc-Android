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
include(":smart-card-reader-lib")
include(":nfc-lib")
