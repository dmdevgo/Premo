@file:Suppress("UnstableApiUsage")

rootProject.name = "Premo"
include(":sample:app-android")
include(":sample:app-common")
include(":premo")
include(":premo-navigation")

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {

        val kotlin = "kotlin"
        val compose = "compose"
        val coroutines = "coroutines"

        create("libs") {
            version(kotlin, "1.6.0")
            version(coroutines, "1.6.0")
            version(compose, "1.1.0-beta04")
            alias("kotlin-stdlib").to("org.jetbrains.kotlin", "kotlin-stdlib-jdk7").versionRef(kotlin)
            alias("coroutines-core").to("org.jetbrains.kotlinx","kotlinx-coroutines-core").versionRef(coroutines)
            alias("kotlinx-serialization-json").to("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

            alias("compose-ui-ui").to("androidx.compose.ui", "ui").versionRef(compose)
            alias("compose-ui-tooling").to("androidx.compose.ui", "ui-tooling").versionRef(compose)
            alias("compose-foundation").to("androidx.compose.foundation", "foundation").versionRef(compose)
            alias("compose-material-material").to("androidx.compose.material", "material").versionRef(compose)
            alias("compose-material-icons-core").to("androidx.compose.material", "material-icons-core").versionRef(compose)
            alias("compose-material-icons-extended").to("androidx.compose.material", "material-icons-extended").versionRef(compose)
            alias("compose-activity").to("androidx.activity:activity-compose:1.4.0")
            alias("accompanist-navigation-animation").to("com.google.accompanist:accompanist-navigation-animation:0.21.4-beta")
            alias("androidx-appcompat-appcompat").to("androidx.appcompat:appcompat:1.4.0")
            alias("androidx-window-window").to("androidx.window:window:1.0.0-beta04")

            alias("android-test-ext-junit").to("androidx.test.ext:junit:1.1.3")
            alias("android-test-espresso-core").to("androidx.test.espresso:espresso-core:3.4.0")
        }
    }
}
