// Правила игры отдельным модулем: тот же исходник компилируется в JVM-байткод
// для игры (десктоп, Android, iOS, а через TeaVM и веб) и в JS для серверной
// функции мультиплеера. Один движок на клиенте и сервере — правила не могут
// разойтись. См. docs/gdx/11-multiplayer-spec.md.
plugins {
    kotlin("multiplatform")
}

val junitVersion: String by project

kotlin {
    jvmToolchain(17)

    jvm {
        // Тесты домена написаны на kotlin.test, но запускаются платформой JUnit 5 —
        // как и остальные тесты проекта.
        testRuns["test"].executionTask.configure { useJUnitPlatform() }
    }

    js(IR) {
        nodejs()
        // Библиотека, а не исполняемый файл: серверная функция импортирует фасад.
        binaries.library()
    }

    sourceSets {
        val jsMain by getting {
            // @JsExport до сих пор помечен экспериментальным, хотя это
            // единственный способ отдать API наружу из Kotlin/JS.
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                runtimeOnly("org.junit.platform:junit-platform-launcher")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            }
        }
    }
}
