// settings.gradle

pluginManagement {
    repositories {
        google() // Versión simplificada
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PasteleriaMilSabores"
include(":app")
 