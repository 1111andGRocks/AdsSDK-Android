import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.7.2"
        id("com.android.library") version "8.7.2"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
        id("com.vanniktech.maven.publish") version "0.34.0"
    }
}

fun loadRootGradleProperties(rootDir: java.io.File): Properties {
    val p = Properties()
    val f = rootDir.resolve("gradle.properties")
    if (f.exists()) {
        f.inputStream().use { p.load(it) }
    }
    return p
}

val localSdkMarker = settings.rootDir.resolve("grocks-ads/src/main/java/com/grocks/ads/GrocksAds.kt")
val hasLocalSdkSources = localSdkMarker.exists()

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "grocks-ads-framework-android"
include(":example")
if (hasLocalSdkSources) {
    include(":grocks-ads")
}

