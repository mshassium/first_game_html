plugins {
    id("com.android.application")
    kotlin("android")
}

val gdxVersion: String by project

android {
    namespace = "com.first.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.first.game"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = project.version.toString()
    }

    sourceSets {
        named("main") {
            // Ассеты общие с остальными платформами — копия не нужна.
            assets.srcDirs(rootProject.file("assets"))
            java.srcDirs("src/main/kotlin")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources.excludes += setOf("META-INF/robovm/**", "META-INF/DEPENDENCIES")
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    for (abi in listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")) {
        runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-$abi")
    }
}
