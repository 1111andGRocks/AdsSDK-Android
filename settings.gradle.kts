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

fun loadGithubProperties(rootDir: java.io.File): Properties {
    val p = Properties()
    val f = rootDir.resolve("github.properties")
    if (f.exists()) {
        f.inputStream().use { p.load(it) }
    }
    return p
}

val rootProps = loadRootGradleProperties(settings.rootDir)
val githubOwner = rootProps.getProperty("grocksAds.github.owner") ?: "1111andGRocks"
val githubRepo = rootProps.getProperty("grocksAds.github.repo") ?: "AdsSDK-Android"

val localSdkMarker = settings.rootDir.resolve("grocks-ads/src/main/java/com/grocks/ads/GrocksAds.kt")
val hasLocalSdkSources = localSdkMarker.exists()

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (!hasLocalSdkSources) {
            val gh = loadGithubProperties(settings.rootDir)
            val gprUser =
                gh.getProperty("gpr.user")
                    ?: System.getenv("GPR_USER")
                    ?: System.getenv("GITHUB_ACTOR")
                    ?: ""
            val gprKey =
                gh.getProperty("gpr.key")
                    ?: System.getenv("GPR_TOKEN")
                    ?: System.getenv("GITHUB_TOKEN")
                    ?: ""
            maven {
                name = "GitHubPackagesGrocks"
                url = uri("https://maven.pkg.github.com/$githubOwner/$githubRepo")
                credentials {
                    username = gprUser
                    password = gprKey
                }
            }
        }
    }
}

rootProject.name = "grocks-ads-framework-android"
include(":example")
if (hasLocalSdkSources) {
    include(":grocks-ads")
}
