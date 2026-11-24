pluginManagement {
    includeBuild("../Kit/build-logic")
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // Versions shared by all the apps, in the kit checked out next to this repository.
    versionCatalogs {
        create("libs") {
            from(files("../Kit/gradle/libs.versions.toml"))
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "cauri"

// Shared modules of the kit, such as its glass surfaces.
includeBuild("../Kit")

include(":app")
include(":feature:onboarding")
include(":feature:home")
include(":feature:cash")
include(":feature:transactions")
include(":feature:places")
include(":feature:settings")
include(":feature:insights")
include(":feature:goals")
include(":core:design")
include(":core:data")
include(":core:capture")
include(":core:parsing")
include(":core:engine")
include(":core:ai")
