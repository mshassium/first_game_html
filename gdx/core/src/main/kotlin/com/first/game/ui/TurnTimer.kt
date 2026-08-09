package com.first.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.first.game.assets.Assets

/**
 * Обратный отсчёт хода в сетевой партии: песочные часы у портрета.
 *
 * Показывается только на последних секундах. Пока времени вагон, счётчик над
 * столом — это шум, который отвлекает от карт; к тому же при каждом ходе он
 * прыгал обратно на минуту, и этот скачок читался как поломка. Теперь его
 * просто нет, пока не начало поджимать.
 *
 * Часы стоят у портрета того, чьего хода ждут, — там же, где подсветка
 * активной стороны, так что взгляд не приходится переводить.
 */
class TurnTimer(private val assets: Assets, private val theme: Theme) {

    private val layout = GlyphLayout()

    /** С какой секунды таймер появляется. */
    var threshold = 10f

    /**
     * @param secondsLeft сколько осталось на ход
     * @param portrait рамка портрета того, чьего хода ждут
     */
    fun draw(batch: Batch, secondsLeft: Float, portrait: Rectangle) {
        if (secondsLeft > threshold || secondsLeft <= 0f) return

        // Насколько «горячо»: 0 в момент появления, 1 у самого нуля.
        val heat = 1f - (secondsLeft / threshold).coerceIn(0f, 1f)
        // Появление плавное: резкий скачок читался бы как сбой отрисовки.
        val alpha = Interpolation.smooth.apply(((threshold - secondsLeft) / 1.2f).coerceIn(0f, 1f))
        val color = urgency(heat)
        val whole = MathUtils.ceil(secondsLeft).coerceAtLeast(1)
        // Доля текущей секунды: на ней держатся качание часов и удар цифры.
        val tick = 1f - (secondsLeft - MathUtils.floor(secondsLeft))
        // Последние секунды бьют заметнее — иначе их легко прозевать.
        val punch = if (secondsLeft <= 3f) 0.3f else 0.16f

        val size = portrait.width * 0.42f
        val x = portrait.x + portrait.width * 1.05f
        val y = portrait.y + (portrait.height - size) / 2f

        // Подложка-свечение: без неё часы теряются на светлых участках стола.
        batch.setColor(Palette.rgba(color, alpha * 0.35f * (0.6f + heat * 0.4f)))
        batch.draw(assets.glow, x - size * 0.6f, y - size * 0.6f, size * 2.2f, size * 2.2f)

        // Часы покачиваются в начале каждой секунды, будто их перевернули.
        val swing = MathUtils.sin(tick * MathUtils.PI * 2f) * 8f * (1f - tick)
        assets.icon("hourglass")?.let { icon ->
            batch.setColor(Palette.rgba(color, alpha))
            batch.draw(icon, x, y, size / 2f, size / 2f, size, size, 1f, 1f, swing)
        }

        drawNumber(
            batch,
            text = whole.toString(),
            centerX = x + size * 1.5f,
            centerY = portrait.y + portrait.height / 2f,
            size = portrait.width * 0.4f * (1f + punch * (1f - tick)),
            color = color,
            alpha = alpha,
        )
        batch.setColor(Color.WHITE)
    }

    /**
     * Цифра по центру точки.
     *
     * BitmapFont рисует от верха строки вниз, поэтому «поставить по центру»
     * означает поднять на половину высоты — иначе счётчик уезжает под портрет.
     */
    private fun drawNumber(
        batch: Batch,
        text: String,
        centerX: Float,
        centerY: Float,
        size: Float,
        color: Color,
        alpha: Float,
    ) {
        val font = theme.title.font
        font.data.setScale(size / font.capHeight)
        font.color = Palette.rgba(color, alpha)
        layout.setText(font, text)
        font.draw(batch, layout, centerX - layout.width / 2f, centerY + layout.height / 2f)
        font.data.setScale(1f)
        font.color = Color.WHITE
    }

    /** Золото на десятой секунде, тревожный багрянец у нуля. */
    private fun urgency(heat: Float): Color = Color(
        MathUtils.lerp(Palette.GOLD_LIGHT.r, ALARM.r, heat),
        MathUtils.lerp(Palette.GOLD_LIGHT.g, ALARM.g, heat),
        MathUtils.lerp(Palette.GOLD_LIGHT.b, ALARM.b, heat),
        1f,
    )

    private companion object {
        val ALARM: Color = Color.valueOf("FF6B6B")
    }
}
