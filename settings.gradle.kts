pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "Premo"

include(":premo")
include(":premo-navigation")
include(":sample:app-common")
include(":sample:app-common-compose-ui")
include(":sample:app-android")
include(":sample:app-desktop")

