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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://nexus-playground.pleos.ai/repository/maven-releases/") {
            content {
                includeGroup("ai.pleos.playground")
            }
        }
        maven("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") {
            content {
                includeGroup("com.kakao.maps.open")
            }
        }
    }
}

rootProject.name = "Picko"
include(":app")
 