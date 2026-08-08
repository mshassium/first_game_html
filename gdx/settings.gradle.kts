pluginManagement {
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

include("rules")
include("core")
include("tools")
include("lwjgl3")
include("teavm")
include("android")
include("ios")
