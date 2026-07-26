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

val gdxVersion: String by project

dependencies {
    // TexturePacker для сборки атласов.
    implementation("com.badlogicgames.gdx:gdx-tools:$gdxVersion")
}

/**
 * Режет сгенерированный лист ассетов на отдельные PNG с прозрачным фоном.
 *
 * ./gradlew tools:sliceSheet -Psheet="../assets_src/cards/Панели и кнопки.png" -Ppreset=panels
 * ./gradlew tools:sliceSheet -Psheet=... -Ppreset=panels -Pdry   — только отчёт
 */
tasks.register<JavaExec>("sliceSheet") {
    group = "assets"
    description = "Нарезает лист ассетов на отдельные файлы"
    mainClass.set("com.first.game.tools.SheetSlicerKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
    argumentProviders.add {
        buildList {
            listOf("sheet", "preset", "names", "out", "tolerance", "minArea", "threshold").forEach { key ->
                (project.findProperty(key) as String?)?.let { add("$key=$it") }
            }
            listOf("dry", "black", "single", "solid", "holes", "cutbg", "keepalpha").forEach { flag ->
                if (project.hasProperty(flag)) add(flag)
            }
        }
    }
}

/** Тёплая цветокоррекция ассета до температуры принятого набора. */
tasks.register<JavaExec>("warmGrade") {
    group = "assets"
    description = "Подтягивает холодные ассеты к тёплой гамме набора"
    mainClass.set("com.first.game.tools.WarmGradeKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir.parentFile
    argumentProviders.add {
        buildList {
            listOf("files", "dir", "prefix", "target").forEach { key ->
                (project.findProperty(key) as String?)?.let { add("$key=$it") }
            }
        }
    }
}

/** Уменьшает и пережимает фоны перед сборкой. */
tasks.register<JavaExec>("prepareBackgrounds") {
    group = "assets"
    description = "Готовит assets_src/bg к сборке: уменьшает и пережимает JPEG"
    mainClass.set("com.first.game.tools.BackgroundPackerKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        rootProject.projectDir.parentFile.resolve("assets_src/bg").absolutePath,
        rootProject.projectDir.resolve("assets/bg").absolutePath,
    )
}

/** Пакует нарезанные ассеты в атласы, которые грузит игра. */
tasks.register<JavaExec>("packAtlases") {
    group = "assets"
    description = "Собирает assets_src/{ui,cards,vfx} в gdx/assets/atlas"
    mainClass.set("com.first.game.tools.AtlasPackerKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        rootProject.projectDir.parentFile.resolve("assets_src").absolutePath,
        rootProject.projectDir.resolve("assets/atlas").absolutePath,
    )
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
