plugins {
    java
    id("com.mobidevelop.robovm")
}

val gdxVersion: String by project
val roboVMVersion: String by project

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-robovm:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios")
    implementation("com.mobidevelop.robovm:robovm-rt:$roboVMVersion")
    implementation("com.mobidevelop.robovm:robovm-cocoatouch:$roboVMVersion")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

// Ресурсы и архитектуры описаны в robovm.xml — там же, где их ждёт RoboVM.
