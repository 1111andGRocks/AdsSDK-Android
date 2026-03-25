import java.util.Properties

gradle.beforeProject {
    if (project.name != "grocks-ads") return@beforeProject
    val f = settings.rootDir.resolve("central-publishing.properties")
    if (!f.exists()) return@beforeProject
    val p = Properties()
    f.inputStream().use { p.load(it) }
    p.forEach { k, v ->
        val key = k.toString()
        if (project.findProperty(key) == null) {
            project.extensions.extraProperties.set(key, v.toString())
        }
    }
}

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
        id("com.vanniktech.maven.publish") version "0.29.0"
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
