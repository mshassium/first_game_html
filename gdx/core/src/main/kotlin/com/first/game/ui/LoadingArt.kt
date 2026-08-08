package com.first.game.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Disposable

/**
 * Картинка экрана загрузки: каменная стена, щит Ордена и бронзовая полоса.
 *
 * Отдельно от того, кто считает прогресс: в вебе его даёт очередь загрузчика
 * бэкенда, а на десктопе — отладочный ключ `first.boot=loading`, которым эту
 * картинку и смотрят. Общий код на оба случая, потому что иначе увидеть веб-экран
 * до выкладки было бы негде.
 *
 * Ассеты игры в этот момент ещё не загружены: атласа, шрифтов и темы нет. Поэтому
 * экран тянет свои два файла сам, а полосу рисует прямоугольниками из палитры.
 */
class LoadingArt : Disposable {

    private var background: Texture? = null
    private var emblem: Texture? = null
    private var white: Texture? = null

    /** Файлы уже должны лежать на месте: в вебе их догружают до вызова. */
    fun load() {
        background = texture(BACKGROUND)
        emblem = texture(EMBLEM)
        white = Pixmap(1, 1, Pixmap.Format.RGBA8888).let { pixmap ->
            pixmap.setColor(Color.WHITE)
            pixmap.fill()
            Texture(pixmap).also { pixmap.dispose() }
        }
    }

    /** Картинка могла не догрузиться: тогда экран просто обойдётся без неё. */
    private fun texture(path: String): Texture? = runCatching {
        Texture(Gdx.files.internal(path)).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }.getOrNull()

    fun draw(batch: Batch, width: Float, height: Float, progress: Float) {
        batch.setColor(Color.WHITE)
        background?.let { batch.drawCover(it, width, height) }

        val barWidth = MathUtils.clamp(width * BAR_WIDTH, 200f, 900f)
        val barHeight = MathUtils.clamp(height * BAR_HEIGHT, 12f, 40f)
        val barY = height * BAR_Y

        emblem?.let {
            // Щит ужимается по меньшей стороне экрана: иначе на узком он вылезает
            // за края, а на низком наезжает на саму полосу.
            val emblemHeight = minOf(height * EMBLEM_HEIGHT, width * EMBLEM_WIDTH * it.height / it.width)
            val emblemWidth = emblemHeight * it.width / it.height
            batch.draw(
                it, (width - emblemWidth) / 2f, barY + barHeight + height * EMBLEM_GAP,
                emblemWidth, emblemHeight,
            )
        }
        drawBar(batch, (width - barWidth) / 2f, barY, barWidth, barHeight, progress)
        batch.setColor(Color.WHITE)
    }

    /** Бронзовый обод, тёмное ложе и золотое заполнение — цвета панелей самой игры. */
    private fun drawBar(batch: Batch, x: Float, y: Float, width: Float, height: Float, progress: Float) {
        val white = this.white ?: return
        val edge = (height * EDGE).coerceAtLeast(1f)

        batch.setColor(Palette.BRONZE)
        batch.draw(white, x, y, width, height)
        batch.setColor(Palette.STONE_DARK)
        batch.draw(white, x + edge, y + edge, width - edge * 2f, height - edge * 2f)

        val inner = height - edge * 2f
        val fill = (width - edge * 2f) * MathUtils.clamp(progress, 0f, 1f)
        if (fill <= 0f) return
        batch.setColor(Palette.GOLD)
        batch.draw(white, x + edge, y + edge, fill, inner)
        // Тонкий светлый кант поверху: без него золото читается плоской заливкой.
        val rim = inner * RIM
        batch.setColor(Palette.GOLD_LIGHT)
        batch.draw(white, x + edge, y + height - edge - rim, fill, rim)
    }

    override fun dispose() {
        background?.dispose()
        emblem?.dispose()
        white?.dispose()
    }

    companion object {
        /** Файлы, из которых собран сам экран: в вебе их грузят раньше остальных. */
        const val BACKGROUND = "bg/bg_loading.jpg"
        const val EMBLEM = "loading/emblem.png"

        /** Полоса: доли экрана по ширине, высоте и положению снизу. */
        private const val BAR_WIDTH = 0.52f
        private const val BAR_HEIGHT = 0.032f
        private const val BAR_Y = 0.28f

        /** Толщина обода полосы в долях её высоты и светлого канта — в долях заливки. */
        private const val EDGE = 0.3f
        private const val RIM = 0.3f

        /** Щит: доли высоты и ширины экрана и зазор до полосы. */
        private const val EMBLEM_HEIGHT = 0.4f
        private const val EMBLEM_WIDTH = 0.55f
        private const val EMBLEM_GAP = 0.06f
    }
}
