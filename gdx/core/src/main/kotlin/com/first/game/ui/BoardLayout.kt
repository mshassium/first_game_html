package com.first.game.ui

import com.badlogic.gdx.math.Rectangle
import com.first.game.domain.Letter
import com.first.game.domain.Rules

/**
 * Раскладка стола. Считается от размера мира, а не от разрешения экрана,
 * поэтому одинаково работает и на десктопе, и на телефоне.
 *
 * Landscape: три ряда (SPACE противника, свой SPACE, рука), а сброс каждой стороны
 * стоит вплотную справа от её собственного ряда. Portrait: сброс прижат к своей зоне
 * сверху для противника и снизу для игрока.
 *
 * Принадлежность сброса читается по положению, а не по подписи: он всегда на одной
 * линии со своим SPACE и своим портретом.
 */
class BoardLayout(val worldWidth: Float, val worldHeight: Float) {

    val portrait: Boolean = worldHeight > worldWidth

    val hud = Rectangle()
    val aiSpace = Rectangle()
    val youSpace = Rectangle()
    val hand = Rectangle()
    val aiDiscard = Rectangle()
    val youDiscard = Rectangle()
    /**
     * Колода общая на обе стороны и стоит справа от руки.
     *
     * Две одинаковые стопки у каждого игрового поля не несли информации: колода в
     * правилах одна, и раздача идёт из неё же.
     */
    val deck = Rectangle()

    /** Кнопка правил — под колодой, в той же колонке. */
    val rulesButton = Rectangle()
    val aiPortrait = Rectangle()
    val youPortrait = Rectangle()

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

        // Колонка сброса заметно уже прежней: журнала в ней больше нет, а стопке
        // урны с миниатюрами карт хватает узкой полосы. Освободившееся уходит столу.
        val sideWidth = (worldWidth * 0.13f).coerceIn(140f, 230f)
        val boardWidth = worldWidth - sideWidth - pad * 3f
        val boardHeight = worldHeight - hudHeight - pad * 4f
        val rowHeight = boardHeight / 3f

        // Карта: по высоте ряда и по условию «семь карт руки помещаются в строку».
        val byHeight = rowHeight * 0.92f
        val byWidth = (boardWidth - pad * (Rules.HAND_LIMIT - 1)) / Rules.HAND_LIMIT * CARD_ASPECT_INVERSE
        cardHeight = minOf(byHeight, byWidth)
        cardWidth = cardHeight * CARD_ASPECT

        // Слева от каждой зоны SPACE — круглый портрет стороны.
        val portraitSize = minOf(rowHeight * 0.86f, boardWidth * 0.11f)
        val zoneX = pad + portraitSize + pad
        val zoneWidth = boardWidth - portraitSize - pad

        var y = worldHeight - hudHeight - pad - rowHeight
        aiSpace.set(zoneX, y, zoneWidth, rowHeight)
        aiPortrait.set(pad, y + (rowHeight - portraitSize) / 2f, portraitSize, portraitSize)
        y -= rowHeight + pad
        youSpace.set(zoneX, y, zoneWidth, rowHeight)
        youPortrait.set(pad, y + (rowHeight - portraitSize) / 2f, portraitSize, portraitSize)
        y -= rowHeight + pad
        hand.set(pad, y, boardWidth, rowHeight)

        // Сброс на одной линии со своим рядом: слева портрет стороны, в центре её
        // SPACE, справа её же сброс. Читать подпись, чтобы понять чей он, не нужно.
        val sideX = pad * 2 + boardWidth
        aiDiscard.set(sideX, aiSpace.y, sideWidth, rowHeight)
        youDiscard.set(sideX, youSpace.y, sideWidth, rowHeight)

        // Колода и кнопка правил — один блок в свободной колонке справа от руки,
        // отцентрированный по её ряду: иначе кнопка свисает ниже панели.
        val buttonHeight = (rowHeight * 0.26f).coerceIn(38f, 56f)
        val gap = pad * 0.6f
        val deckHeight = minOf(cardHeight * 0.85f, rowHeight * 0.62f)
        val deckWidth = deckHeight * CARD_ASPECT
        val blockY = hand.y + (rowHeight - (deckHeight + gap + buttonHeight)) / 2f
        rulesButton.set(sideX + sideWidth * 0.06f, blockY, sideWidth * 0.88f, buttonHeight)
        deck.set(
            sideX + (sideWidth - deckWidth) / 2f,
            blockY + buttonHeight + gap,
            deckWidth, deckHeight,
        )
    }

    private fun layoutPortrait() {
        val pad = worldHeight * 0.008f
        val hudHeight = worldHeight * 0.055f
        hud.set(0f, worldHeight - hudHeight, worldWidth, hudHeight)

        val boardWidth = worldWidth - pad * 2
        // Две полосы сброса: по одной на сторону, каждая вплотную к своей зоне.
        val discardHeight = worldHeight * 0.07f
        val available = worldHeight - hudHeight - discardHeight * 2f - pad * 7
        val rowHeight = available / 3f

        val byHeight = rowHeight * 0.94f
        val byWidth = (boardWidth - pad * (Rules.HAND_LIMIT - 1)) / Rules.HAND_LIMIT * CARD_ASPECT_INVERSE
        cardHeight = minOf(byHeight, byWidth)
        cardWidth = cardHeight * CARD_ASPECT

        val portraitSize = minOf(rowHeight * 0.5f, boardWidth * 0.13f)
        val zoneX = pad + portraitSize + pad
        val zoneWidth = boardWidth - portraitSize - pad

        // Сверху вниз: сброс противника, его зона, своя зона, свой сброс, рука.
        // Каждая полоса сброса касается своей зоны, а не чужой.
        var y = worldHeight - hudHeight - pad - discardHeight
        aiDiscard.set(pad, y, boardWidth, discardHeight)
        y -= rowHeight + pad * 0.5f
        aiSpace.set(zoneX, y, zoneWidth, rowHeight)
        aiPortrait.set(pad, y + (rowHeight - portraitSize) / 2f, portraitSize, portraitSize)
        y -= rowHeight + pad
        youSpace.set(zoneX, y, zoneWidth, rowHeight)
        youPortrait.set(pad, y + (rowHeight - portraitSize) / 2f, portraitSize, portraitSize)
        y -= discardHeight + pad * 0.5f
        youDiscard.set(pad, y, boardWidth, discardHeight)
        // В портрете колода тоже одна и тоже справа: панель руки сужается на её ширину.
        y -= rowHeight + pad
        val buttonHeight = (rowHeight * 0.24f).coerceIn(34f, 50f)
        val deckHeight = minOf(cardHeight * 0.72f, rowHeight * 0.58f)
        val deckWidth = deckHeight * CARD_ASPECT
        val deckColumn = deckWidth + pad * 3f
        hand.set(pad, y, boardWidth - deckColumn, rowHeight)
        val blockY = hand.y + (rowHeight - (deckHeight + pad + buttonHeight)) / 2f
        val columnX = hand.x + hand.width + pad * 1.5f
        rulesButton.set(columnX, blockY, deckWidth, buttonHeight)
        deck.set(columnX, blockY + buttonHeight + pad, deckWidth, deckHeight)
    }

    /**
     * Позиции карт руки: до семи штук по центру своей зоны.
     *
     * Карты кладутся внутрь резной рамки панели, а не в её габарит: иначе верхний
     * и нижний кант оказываются под картами и панель выглядит продавленной.
     */
    fun handSlots(count: Int): List<Rectangle> {
        val inset = hand.height * HAND_INSET
        val inner = Rectangle(
            hand.x + inset, hand.y + inset,
            hand.width - inset * 2f, hand.height - inset * 2f,
        )
        val height = minOf(cardHeight, inner.height)
        return rowSlots(inner, count, height * CARD_ASPECT, height)
    }

    /**
     * Пять постоянных гнёзд в зоне SPACE — по одному на букву.
     *
     * Позиция закреплена за буквой, поэтому пустое гнездо сразу показывает, чего
     * не хватает до набора F-I-R-S-T. Это понятнее, чем сдвигающийся ряд карт.
     */
    fun spaceSlots(zone: Rectangle): List<Rectangle> =
        rowSlots(zone, Letter.ALL.size, cardWidth * 0.92f, cardHeight * 0.92f)

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

        /** Отступ карт руки от края панели, в долях её высоты — под резной кант. */
        const val HAND_INSET = 0.08f
        const val CARD_ASPECT_INVERSE = 3f / 2f
    }
}
