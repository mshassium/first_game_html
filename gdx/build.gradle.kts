// Все плагины объявляются здесь и применяются в модулях: так они попадают
// в один загрузчик классов, иначе kotlin-android не видит классы AGP.
plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("android") version "2.2.21" apply false
    id("com.android.application") version "8.11.2" apply false
    id("com.github.xpenatan.gdx-teavm") version "1.6.0" apply false
    id("com.mobidevelop.robovm") version "2.3.25" apply false
}

allprojects {
    group = "com.first.game"
    version = property("appVersion") as String
}
