import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")
}

val sdkVersion = findProperty("grocksAds.sdkVersion")?.toString() ?: "0.1.0"
val githubOwner = findProperty("grocksAds.github.owner")?.toString() ?: "1111andGRocks"
val githubRepo = findProperty("grocksAds.github.repo")?.toString() ?: "AdsSDK-Android"

fun loadGithubProperties(): Properties {
    val p = Properties()
    val f = rootProject.file("github.properties")
    if (f.exists()) {
        f.inputStream().use { p.load(it) }
    }
    return p
}

fun githubProp(name: String, gh: Properties): String? =
    gh.getProperty(name)?.takeIf { it.isNotBlank() }

android {
    namespace = "com.grocks.ads"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}

afterEvaluate {
    val gh = loadGithubProperties()
    val gprUser =
        githubProp("gpr.user", gh)
            ?: findProperty("gpr.user")?.toString()
            ?: System.getenv("GPR_USER")
            ?: System.getenv("GITHUB_ACTOR")
    val gprKey =
        githubProp("gpr.key", gh)
            ?: findProperty("gpr.key")?.toString()
            ?: System.getenv("GPR_TOKEN")
            ?: System.getenv("GITHUB_TOKEN")

    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.grocks.ads"
                artifactId = "grocks-ads"
                version = sdkVersion
                from(components["release"])
                pom {
                    name.set("GrocksAds")
                    description.set("Grocks Ads Android SDK (WebView mediation UI).")
                    url.set("https://github.com/$githubOwner/$githubRepo")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/$githubOwner/$githubRepo")
                credentials {
                    username = gprUser ?: ""
                    password = gprKey ?: ""
                }
            }
        }
    }
}
