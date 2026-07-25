plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.first.game.tools.FontBakerKt")
}

kotlin {
    jvmToolchain(17)
}

/** Печёт иконки приложения. */
tasks.register<JavaExec>("bakeIcons") {
    group = "assets"
    description = "Генерирует иконки приложения в android/src/main/res и assets_src/branding"
    mainClass.set("com.first.game.tools.IconBakerKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
    args = listOf(
        rootProject.projectDir.resolve("android/src/main/res").absolutePath,
        rootProject.projectDir.parentFile.resolve("assets_src/branding").absolutePath,
    )
}

/** Печёт .fnt + .png из TTF. Запускается на десктопе, результат коммитится в assets. */
tasks.register<JavaExec>("bakeFonts") {
    group = "assets"
    description = "Генерирует растровые шрифты в gdx/assets/fonts"
    mainClass.set("com.first.game.tools.FontBakerKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        rootProject.projectDir.parentFile.resolve("assets_src/fonts").absolutePath,
        rootProject.projectDir.resolve("assets/fonts").absolutePath,
    )
}
