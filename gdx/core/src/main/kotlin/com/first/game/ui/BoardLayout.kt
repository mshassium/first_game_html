package com.first.game.ui

import com.badlogic.gdx.math.Rectangle
import com.first.game.domain.Rules

/**
 * Раскладка стола. Считается от размера мира, а не от разрешения экрана,
 * поэтому одинаково работает и на десктопе, и на телефоне.
 *
 * Landscape: слева три ряда (SPACE противника, свой SPACE, рука), справа колонка
 * со сбросами и журналом. Portrait: сбросы уезжают наверх одной строкой,
 * журнал скрывается — на узком экране он только съедает место.
 */
class BoardLayout(val worldWidth: Float, val worldHeight: Float) {

    val portrait: Boolean = worldHeight > worldWidth

    val hud = Rectangle()
    val aiSpace = Rectangle()
    val youSpace = Rectangle()
    val hand = Rectangle()
    val aiDiscard = Rectangle()
    val youDiscard = Rectangle()
    val log = Rectangle()
    val aiDeck = Rectangle()
    val youDeck = Rectangle()

    var cardWidth: Float = 0f
        private set
    var cardHeight: Float = 0f
        private set

    init {
        if (portrait) layoutPortrait() else layoutLandscape()
    }

    private fun layoutLandscape() {
        val pad = worldWidth * 0.012f
        val hudHeight = worldHeight * 0.075f
        hud.set(0f, worldHeight - hudHeight, worldWidth, hudHeight)

        val sideWidth = (worldWidth * 0.23f).coerceIn(220f, 340f)
        val boardWidth = worldWidth - sideWidth - pad * 3f
        val boardHeight = worldHeight - hudHeight - pad * 4f
        val rowHeight = boardHeight / 3f

        // Карта: по высоте ряда и по условию «семь карт руки помещаются в строку».
        val byHeight = rowHeight * 0.92f
        val byWidth = (boardWidth - pad * (Rules.HAND_LIMIT - 1)) / Rules.HAND_LIMIT * CARD_ASPECT_INVERSE
        cardHeight = minOf(byHeight, byWidth)
        cardWidth = cardHeight * CARD_ASPECT

        var y = worldHeight - hudHeight - pad - rowHeight
        aiSpace.set(pad, y, boardWidth, rowHeight)
        y -= rowHeight + pad
        youSpace.set(pad, y, boardWidth, rowHeight)
        y -= rowHeight + pad
        hand.set(pad, y, boardWidth, rowHeight)

        val sideX = pad * 2 + boardWidth
        val discardHeight = worldHeight * 0.16f
        aiDiscard.set(sideX, worldHeight - hudHeight - pad - discardHeight, sideWidth, discardHeight)
        youDiscard.set(sideX, aiDiscard.y - pad - discardHeight, sideWidth, discardHeight)
        log.set(sideX, pad, sideWidth, youDiscard.y - pad * 2)

        aiDeck.set(aiSpace.x + aiSpace.width - cardWidth * 0.7f, aiSpace.y, cardWidth * 0.7f, cardHeight * 0.7f)
        youDeck.set(youSpace.x + youSpace.width - cardWidth * 0.7f, youSpace.y, cardWidth * 0.7f, cardHeight * 0.7f)
    }

    private fun layoutPortrait() {
        val pad = worldHeight * 0.008f
        val hudHeight = worldHeight * 0.055f
        hud.set(0f, worldHeight - hudHeight, worldWidth, hudHeight)

        val boardWidth = worldWidth - pad * 2
        val discardHeight = worldHeight * 0.075f
        val available = worldHeight - hudHeight - discardHeight - pad * 6
        val rowHeight = available / 3f

        val byHeight = rowHeight * 0.94f
        val byWidth = (boardWidth - pad * (Rules.HAND_LIMIT - 1)) / Rules.HAND_LIMIT * CARD_ASPECT_INVERSE
        cardHeight = minOf(byHeight, byWidth)
        cardWidth = cardHeight * CARD_ASPECT

        val discardWidth = (boardWidth - pad) / 2f
        var y = worldHeight - hudHeight - pad - discardHeight
        aiDiscard.set(pad, y, discardWidth, discardHeight)
        youDiscard.set(pad * 2 + discardWidth, y, discardWidth, discardHeight)

        y -= rowHeight + pad
        aiSpace.set(pad, y, boardWidth, rowHeight)
        y -= rowHeight + pad
        youSpace.set(pad, y, boardWidth, rowHeight)
        y -= rowHeight + pad
        hand.set(pad, y, boardWidth, rowHeight)

        // В портрете журнал не показываем — под него нет места без ущерба столу.
        log.set(0f, 0f, 0f, 0f)

        aiDeck.set(aiSpace.x + aiSpace.width - cardWidth * 0.6f, aiSpace.y, cardWidth * 0.6f, cardHeight * 0.6f)
        youDeck.set(youSpace.x + youSpace.width - cardWidth * 0.6f, youSpace.y, cardWidth * 0.6f, cardHeight * 0.6f)
    }

    /** Позиции карт руки: до семи штук по центру своей зоны. */
    fun handSlots(count: Int): List<Rectangle> = rowSlots(hand, count, cardWidth, cardHeight)

    /** Позиции стопок в зоне SPACE. */
    fun spaceSlots(zone: Rectangle, count: Int): List<Rectangle> =
        rowSlots(zone, count, cardWidth * 0.92f, cardHeight * 0.92f)

    private fun rowSlots(
        zone: Rectangle,
        count: Int,
        slotWidth: Float,
        slotHeight: Float,
    ): List<Rectangle> {
        if (count <= 0) return emptyList()
        val gap = slotWidth * 0.12f
        val totalWidth = count * slotWidth + (count - 1) * gap
        val scale = if (totalWidth > zone.width) zone.width / totalWidth else 1f
        val width = slotWidth * scale
        val height = slotHeight * scale
        val realGap = gap * scale
        var x = zone.x + (zone.width - (count * width + (count - 1) * realGap)) / 2f
        val y = zone.y + (zone.height - height) / 2f
        return List(count) {
            val rect = Rectangle(x, y, width, height)
            x += width + realGap
            rect
        }
    }

    private companion object {
        /** Портретная карта 2:3 (решение D-9). */
        const val CARD_ASPECT = 2f / 3f
        const val CARD_ASPECT_INVERSE = 3f / 2f
    }
}
