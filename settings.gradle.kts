rootProject.name = "Premo"
include(":sample:app-android")
include(":sample:app-common")
include(":premo")

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {

        val kotlin = "kotlin"
        val compose = "compose"

        create("libs") {
            version(kotlin, "1.5.21")
            version(compose, "1.0.1")
            alias("kotlin-stdlib").to("org.jetbrains.kotlin", "kotlin-stdlib-jdk7").versionRef(kotlin)
            alias("coroutines-core").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
            alias("coroutines-android").to("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")
            alias("kotlinx-serialization-json").to("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

            alias("compose-ui-ui").to("androidx.compose.ui", "ui").versionRef(compose)
            alias("compose-ui-tooling").to("androidx.compose.ui", "ui-tooling").versionRef(compose)
            alias("compose-foundation").to("androidx.compose.foundation", "foundation").versionRef(compose)
            alias("compose-material-material").to("androidx.compose.material", "material").versionRef(compose)
            alias("compose-material-icons-core").to("androidx.compose.material", "material-icons-core").versionRef(compose)
            alias("compose-material-icons-extended").to("androidx.compose.material", "material-icons-extended").versionRef(compose)
            alias("compose-activity").to("androidx.activity:activity-compose:1.3.1")

            alias("appcompat-appcompat").to("androidx.appcompat:appcompat:1.4.0-alpha03")

            alias("android-test-ext-junit").to("androidx.test.ext:junit:1.1.3")
            alias("android-test-espresso-core").to("androidx.test.espresso:espresso-core:3.4.0")
        }
    }
}