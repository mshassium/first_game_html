pluginManagement {
    plugins {
        id("com.github.xpenatan.gdx-teavm") version providers.gradleProperty("gdxTeaVMPluginVersion").get()
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "first-game"

include("core")
include("tools")
include("lwjgl3")
include("teavm")
