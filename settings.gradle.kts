pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()   // ✅ 반드시 있어야 함
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Gamsung2"
include(":app")
