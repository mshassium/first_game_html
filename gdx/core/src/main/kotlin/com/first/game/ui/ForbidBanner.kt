package com.first.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.first.game.assets.Assets
import com.first.game.domain.Letter

/**
 * Флаг запрета, висящий у карты F в SPACE наложившей стороны.
 *
 * Запрет держится, пока карта F лежит в SPACE, и снимается только срабатыванием
 * или кражей этой карты (01-rules-spec §5.1). Разовой вспышки в момент наложения
 * для этого мало: к своему ходу игрок уже не помнит ни того, что запрет есть, ни
 * какую букву назвал. Флаг висит на самой карте, поэтому по его положению читается
 * и чей это запрет, и то, что он ещё в силе.
 *
 * [letter] `null` — запрет наложен на игрока: ему по правилам виден только факт
 * запрета, но не названная буква. Тогда полотнище лазурное, цвета самой школы F,
 * а вместо буквы стоит вопросительный знак.
 */
class ForbidBanner(
    private val assets: Assets,
    private val letter: Letter?,
    private val time: () -> Float,
) : Actor() {

    private val glyphs = GlyphLayout()

    /** Цвет полотнища: запрещённая школа для своего запрета, лазурь F — для чужого. */
    private val accent: Color = Palette.school(letter ?: Letter.F)

    override fun draw(batch: Batch, parentAlpha: Float) {
        val banner = assets.uiRegion("forbid_banner") ?: return
        val alpha = color.a * parentAlpha

        drawHalo(batch, alpha)
        batch.setColor(accent.r, accent.g, accent.b, alpha)
        batch.draw(banner, x, y, originX, originY, width, height, scaleX, scaleY, rotation)
        drawMark(batch, alpha)
        batch.setColor(Color.WHITE)
    }

    /**
     * Свечение за полотнищем — аддитивно, как и прочие эффекты.
     *
     * Дыхание считается от общих часов сцены, а не от заведённого действия: доска
     * пересобирается после каждого события, и действие каждый раз сбрасывало бы фазу.
     */
    private fun drawHalo(batch: Batch, alpha: Float) {
        val breath = HALO_DIM + (HALO_BRIGHT - HALO_DIM) * (0.5f + 0.5f * MathUtils.sin(time() * 1.6f))
        val spill = width * HALO_SPILL
        val previousBlend = batch.blendSrcFunc to batch.blendDstFunc
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        batch.setColor(accent.r, accent.g, accent.b, alpha * breath)
        batch.draw(assets.glow, x - spill, y - spill, width + spill * 2f, height + spill * 2f)
        batch.setBlendFunction(previousBlend.first, previousBlend.second)
    }

    /** Названная буква — или вопрос, если она скрыта — на свободном низе полотнища. */
    private fun drawMark(batch: Batch, alpha: Float) {
        val font = assets.cardLetterFont
        font.data.setScale(width * MARK_SCALE / font.capHeight)
        // Своим цветом: в шрифт карт уже запечены золотая заливка и тёмная обводка,
        // а тонировка в цвет школы поверх крашеного полотнища съедала бы контраст.
        font.color = Color(1f, 1f, 1f, alpha)
        font.drawCardLetter(
            batch, glyphs, letter?.name ?: HIDDEN_MARK,
            centerX = x + width / 2f,
            centerY = y + height * MARK_CENTER_Y,
        )
        font.data.setScale(1f)
        font.color = Color.WHITE
    }

    private companion object {
        /** Что стоит на флаге чужого запрета вместо буквы. */
        const val HIDDEN_MARK = "?"

        /**
         * Высота буквы в долях ширины флага и высота её середины в долях высоты,
         * снизу вверх. Верхнюю треть полотнища занимает рунная строчка, низ оставлен
         * под букву — те же доли заданы в промпт-буке §4.17.
         */
        const val MARK_SCALE = 0.42f
        const val MARK_CENTER_Y = 0.36f

        /** Ореол за полотнищем: насколько выходит за габарит и в каких пределах дышит. */
        const val HALO_SPILL = 0.5f
        const val HALO_DIM = 0.18f
        const val HALO_BRIGHT = 0.34f
    }
}
