package com.first.game.ui

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor

/**
 * Светящийся элемент эффекта, нарисованный аддитивно.
 *
 * Эффекты нарисованы белым по чёрному, и обычное альфа-смешивание делает их
 * молочными пятнами поверх стола. Аддитивный режим даёт настоящее свечение:
 * тёмное не затемняет, светлое складывается с фоном.
 */
class GlowActor(private val region: TextureRegion) : Actor() {

    override fun draw(batch: Batch, parentAlpha: Float) {
        val previousBlend = batch.blendSrcFunc to batch.blendDstFunc
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        batch.draw(
            region, x, y, originX, originY, width, height, scaleX, scaleY, rotation,
        )
        batch.setBlendFunction(previousBlend.first, previousBlend.second)
    }
}
