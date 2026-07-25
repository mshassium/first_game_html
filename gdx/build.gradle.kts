plugins {
    kotlin("jvm") version "2.2.21" apply false
}

allprojects {
    group = "com.first.game"
    version = property("appVersion") as String
}
