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

rootProject.name = "AssistAR-Demo"
include(":app", ":assistvision")

val useLocalSimpleIdentVision: Boolean = run {
    val defaultValue = false
    val localPropertiesFile = file("local.properties")
    if (localPropertiesFile.exists()) {
        val props = java.util.Properties()
        props.load(localPropertiesFile.inputStream())
        props.getProperty("useLocalSimpleIdentVision")?.toBoolean() ?: defaultValue
    } else {
        defaultValue
    }
}

gradle.rootProject {
    extra["useLocalSimpleIdentVision"] = useLocalSimpleIdentVision
}

if (useLocalSimpleIdentVision) {
    println("Including local module: visioncpp")
    include(":visioncpp")
    project(":visioncpp").projectDir = file("../visioncpp")
}
