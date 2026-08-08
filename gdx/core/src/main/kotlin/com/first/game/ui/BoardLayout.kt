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

    /** Резной разделитель между зонами SPACE. Только в портрете, иначе нулевой. */
    val divider = Rectangle()
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
        divider.set(0f, 0f, 0f, 0f)
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

        // Размер карты задаётся шириной: пять карт встают в строку без наложения,
        // шестая и седьмая ложатся внахлёст. Если загонять в строку все семь,
        // карта выходит вдвое мельче, чем позволяет высота ряда.
        val byWidth = boardWidth / (PORTRAIT_COLUMNS + (PORTRAIT_COLUMNS - 1) * SLOT_GAP)
        val budget = worldHeight - hudHeight - pad * 6
        val byHeight = budget / (3f * ROW_FILL + 2f * DISCARD_FILL)
        cardHeight = minOf(byWidth * CARD_ASPECT_INVERSE, byHeight)
        cardWidth = cardHeight * CARD_ASPECT

        val rowHeight = cardHeight * ROW_FILL
        val discardHeight = cardHeight * DISCARD_FILL

        // Свободная высота делится поровну между всеми пятью блоками. Прежде она
        // уходила в три больших зазора, и над рукой оставался заметный провал.
        val used = hudHeight + rowHeight * 3f + discardHeight * 2f + pad
        val gap = ((worldHeight - used) / 5f).coerceAtLeast(pad)

        val portraitSize = minOf(rowHeight * 0.5f, boardWidth * 0.13f)
        val zoneX = pad + portraitSize + pad
        val zoneWidth = boardWidth - portraitSize - pad

        var y = worldHeight - hudHeight - gap - discardHeight
        aiDiscard.set(pad, y, boardWidth, discardHeight)

        y -= gap + rowHeight
        aiSpace.set(zoneX, y, zoneWidth, rowHeight)
        aiPortrait.set(pad, y + (rowHeight - portraitSize) / 2f, portraitSize, portraitSize)

        y -= gap + rowHeight
        youSpace.set(zoneX, y, zoneWidth, rowHeight)
        youPortrait.set(pad, y + (rowHeight - portraitSize) / 2f, portraitSize, portraitSize)
        // Разделитель по центру зазора между зонами: он обозначает границу сторон.
        val dividerHeight = gap * 0.8f
        divider.set(
            zoneX, aiSpace.y - gap / 2f - dividerHeight / 2f,
            zoneWidth, dividerHeight,
        )

        y -= gap + discardHeight
        youDiscard.set(pad, y, boardWidth, discardHeight)

        // Колода и кнопка правил — своей колонкой справа, панель руки на неё сужается.
        val buttonHeight = (rowHeight * 0.24f).coerceIn(34f, 50f)
        val deckHeight = minOf(cardHeight * 0.72f, rowHeight * 0.58f)
        val deckWidth = deckHeight * CARD_ASPECT
        y -= gap + rowHeight
        hand.set(pad, y, boardWidth - (deckWidth + pad * 3f), rowHeight)
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
        if (count <= 0) return emptyList()
        val inset = hand.height * HAND_INSET
        val inner = Rectangle(
            hand.x + inset, hand.y + inset,
            hand.width - inset * 2f, hand.height - inset * 2f,
        )
        val height = minOf(cardHeight, inner.height)
        val width = height * CARD_ASPECT
        val gap = width * SLOT_GAP
        // Если карты не помещаются, они наезжают друг на друга, а не мельчают:
        // мелкая карта в большой панели читается как ошибка раскладки.
        val step = if (count * width + (count - 1) * gap <= inner.width) {
            width + gap
        } else {
            (inner.width - width) / (count - 1)
        }
        var x = inner.x + (inner.width - (width + (count - 1) * step)) / 2f
        val y = inner.y + (inner.height - height) / 2f
        return List(count) {
            val rect = Rectangle(x, y, width, height)
            x += step
            rect
        }
    }

    /**
     * Пять постоянных гнёзд в зоне SPACE — по одному на букву.
     *
     * Позиция закреплена за буквой, поэтому пустое гнездо сразу показывает, чего
     * не хватает до набора F-I-R-S-T. Это понятнее, чем сдвигающийся ряд карт.
     */
    fun spaceSlots(zone: Rectangle): List<Rectangle> {
        val inner = spaceInner(zone)
        val height = minOf(cardHeight * 0.92f, inner.height)
        // Полоса под флаг запрета отдаётся ряду всегда, а не только пока флаг висит:
        // иначе его появление и снятие двигали бы все пять карт ряда. В портрете
        // гнёзда иначе встают вплотную к краям панели, и флагу пришлось бы лечь на
        // карту поверх медальона буквы.
        val lane = height * BANNER_HEIGHT * BANNER_ASPECT * (1f + BANNER_GAP)
        val row = Rectangle(inner.x + lane, inner.y, inner.width - lane, inner.height)
        return rowSlots(row, Letter.ALL.size, height * CARD_ASPECT, height)
    }

    /**
     * Флаг запрета: висит слева от гнезда буквы F, в отведённой ему полосе.
     *
     * Размер считается от настоящего гнезда, а не от расчётной высоты карты: в
     * портрете ряд ужимается, и флаг обязан ужаться вместе с ним.
     */
    fun forbidBannerSlot(zone: Rectangle): Rectangle {
        val slot = spaceSlots(zone).firstOrNull() ?: return Rectangle()
        val height = slot.height * BANNER_HEIGHT
        val width = height * BANNER_ASPECT
        return Rectangle(
            slot.x - width * (1f + BANNER_GAP),
            slot.y + slot.height - height,
            width, height,
        )
    }

    /**
     * Внутренняя область зоны SPACE: габарит за вычетом резного канта панели.
     *
     * Гнёзда кладутся сюда, а не в габарит: иначе крайние карты наезжают на угловые
     * накладки. Наружу отсюда не должно выходить ничего, что положено в зону.
     */
    fun spaceInner(zone: Rectangle): Rectangle {
        val inset = zone.height * ZONE_INSET
        return Rectangle(
            zone.x + inset, zone.y + inset,
            zone.width - inset * 2f, zone.height - inset * 2f,
        )
    }

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

        /** То же для зон SPACE: гнёзда не должны залезать на кант панели. */
        const val ZONE_INSET = 0.06f

        /** Сколько карт помещается в строку портрета без наложения. */
        const val PORTRAIT_COLUMNS = 5f

        /** Зазор между гнёздами в долях ширины карты. */
        const val SLOT_GAP = 0.12f

        /**
         * Флаг запрета: высота в долях высоты гнезда, пропорции спрайта
         * `forbid_banner` (промпт-бук §4.17) и зазор до карты в долях ширины флага.
         */
        const val BANNER_HEIGHT = 0.98f
        const val BANNER_ASPECT = 89f / 384f
        const val BANNER_GAP = 0.22f

        /** Во сколько раз ряд выше карты и во сколько раз полоса сброса ниже её. */
        const val ROW_FILL = 1.15f
        const val DISCARD_FILL = 0.62f
        const val CARD_ASPECT_INVERSE = 3f / 2f
    }
}
