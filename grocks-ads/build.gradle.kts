plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.vanniktech.maven.publish")
}

val mavenGroup = findProperty("grocksAds.mavenGroup")?.toString() ?: "com.grocks.ads"
val mavenArtifact = findProperty("grocksAds.mavenArtifact")?.toString() ?: "grocks-ads"
val sdkVersion = findProperty("grocksAds.sdkVersion")?.toString() ?: "0.1.0"

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

mavenPublishing {
    coordinates(mavenGroup, mavenArtifact, sdkVersion)
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    pom {
        name.set("GrocksAds")
        description.set("Grocks Ads Android SDK (WebView mediation UI).")
        inceptionYear.set("2026")
        url.set("https://github.com/1111andGRocks/AdsSDK-Android")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("1111andGRocks")
                name.set("1111andGRocks")
                url.set("https://github.com/1111andGRocks/")
            }
        }
        scm {
            url.set("https://github.com/1111andGRocks/AdsSDK-Android")
            connection.set("scm:git:git://github.com/1111andGRocks/AdsSDK-Android.git")
            developerConnection.set("scm:git:ssh://git@github.com/1111andGRocks/AdsSDK-Android.git")
        }
    }
}
