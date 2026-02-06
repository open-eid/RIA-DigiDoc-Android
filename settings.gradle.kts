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
include(":id-card-lib")
include(":commons-lib:test-files")
include(":id-card-lib:id-lib")
include(":id-card-lib:smart-lib")
include(":web-eid-lib")
