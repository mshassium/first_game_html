plugins {
    kotlin("jvm")
    id("com.github.xpenatan.gdx-teavm")
}

dependencies {
    implementation(project(":core"))
}

kotlin {
    jvmToolchain(17)
}

gdxTeaVM {
    assets.from(rootProject.file("assets"))

    js {
        mainClass.set("com.first.game.teavm.TeaVMLauncher")
        htmlTitle.set("F!RST")
        // Отладка веб-сборки: ./gradlew :teavm:gdx_teavm_web_js_build -PwebDebug
        // даёт читаемые имена классов и стек-трейсы ценой размера.
        val debug = project.hasProperty("webDebug")
        obfuscated.set(!debug)
        debugInformation.set(debug)
        sourceMap.set(debug)
        if (debug) optimization.set(org.teavm.gradle.api.OptimizationLevel.NONE)
    }
}
