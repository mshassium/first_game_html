package com.first.game.ui

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch

/**
 * Рисует фон на весь экран без искажения пропорций.
 *
 * Прежде фон растягивался прямо в габариты мира. На телефоне в вертикальной
 * ориентации мир получается 1280x2276, и картинка 16:9 вытягивалась почти втрое —
 * арки и колонны заметно плющило.
 *
 * Масштаб берётся по большей из сторон, лишнее уходит за края поровну: лучше
 * потерять края, чем показать неправильные пропорции.
 */
fun Batch.drawCover(texture: Texture, worldWidth: Float, worldHeight: Float) {
    val scale = maxOf(worldWidth / texture.width, worldHeight / texture.height)
    val width = texture.width * scale
    val height = texture.height * scale
    draw(texture, (worldWidth - width) / 2f, (worldHeight - height) / 2f, width, height)
}
