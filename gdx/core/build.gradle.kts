plugins {
    kotlin("jvm")
}

val gdxVersion: String by project
val ktxVersion: String by project
val junitVersion: String by project

dependencies {
    // Правила игры живут отдельным мультиплатформенным модулем: тот же код
    // компилируется в JS для сервера мультиплеера.
    api(project(":rules"))

    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    api("io.github.libktx:ktx-app:$ktxVersion")
    api("io.github.libktx:ktx-actors:$ktxVersion")
    api("io.github.libktx:ktx-graphics:$ktxVersion")
    api("io.github.libktx:ktx-collections:$ktxVersion")
    api("io.github.libktx:ktx-scene2d:$ktxVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
