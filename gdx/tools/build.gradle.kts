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
