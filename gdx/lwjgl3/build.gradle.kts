plugins {
    kotlin("jvm")
    application
}

val gdxVersion: String by project

application {
    mainClass.set("com.first.game.lwjgl3.Lwjgl3LauncherKt")
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

kotlin {
    jvmToolchain(17)
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir.resolve("assets")
    // macOS требует запуска GL на главном потоке.
    if (System.getProperty("os.name").contains("Mac")) jvmArgs("-XstartOnFirstThread")
    // Отладочные ключи пробрасываются как -Pfirst.boot=game -Pfirst.shots=/tmp/shots
    listOf(
        "first.boot", "first.shots", "first.size", "first.autoplay",
        "first.frames", "first.overlay", "first.speed", "first.net", "first.profile",
    ).forEach { key ->
        (project.findProperty(key) as String?)?.let { systemProperty(key, it) }
    }
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes["Main-Class"] = application.mainClass.get() }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(rootProject.projectDir.resolve("assets"))
    archiveBaseName.set("first-game")
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
