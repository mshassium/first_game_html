package com.first.game.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import com.first.game.domain.Letter

/**
 * Единая точка доступа к графике и шрифтам.
 *
 * Настоящие атласы (cards.atlas, ui.atlas, vfx.atlas) появятся после генерации
 * ассетов по docs/gdx/05-prompt-book.md. Пока их нет, используется [PlaceholderArt] —
 * экраны и анимации пишутся сразу против финального интерфейса.
 */
class Assets : Disposable {

    private val placeholders = PlaceholderArt()
    private var cardsAtlas: TextureAtlas? = null
    private var uiAtlas: TextureAtlas? = null

    lateinit var cardLetterFont: BitmapFont private set
    lateinit var titleLargeFont: BitmapFont private set
    lateinit var titleFont: BitmapFont private set
    lateinit var bodyFont: BitmapFont private set
    lateinit var bodyBoldFont: BitmapFont private set

    /** Настоящие ассеты найдены — заглушки не используются. */
    var usesGeneratedArt: Boolean = false
        private set

    fun load() {
        cardLetterFont = font("fonts/card_letter.fnt")
        titleLargeFont = font("fonts/title_large.fnt")
        titleFont = font("fonts/title.fnt")
        bodyFont = font("fonts/body.fnt")
        bodyBoldFont = font("fonts/body_bold.fnt")

        if (Gdx.files.internal(CARDS_ATLAS).exists()) {
            cardsAtlas = TextureAtlas(Gdx.files.internal(CARDS_ATLAS))
        }
        if (Gdx.files.internal(UI_ATLAS).exists()) {
            uiAtlas = TextureAtlas(Gdx.files.internal(UI_ATLAS))
        }
        usesGeneratedArt = cardsAtlas != null
    }

    fun cardFace(letter: Letter): TextureRegion =
        cardsAtlas?.findRegion("card_${letter.name}") ?: placeholders.cardFaces.getValue(letter)

    val cardBack: TextureRegion
        get() = cardsAtlas?.findRegion("card_back") ?: placeholders.cardBack

    val white: TextureRegion get() = placeholders.white
    val glow: TextureRegion get() = uiAtlas?.findRegion("fx_glow_soft") ?: placeholders.glow
    val panel: TextureRegion get() = uiAtlas?.findRegion("panel_wood") ?: placeholders.panel
    val panelStone: TextureRegion get() = uiAtlas?.findRegion("panel_stone") ?: placeholders.panelStone
    val slot: TextureRegion get() = uiAtlas?.findRegion("slot_card") ?: placeholders.slot
    val buttonUp: TextureRegion get() = uiAtlas?.findRegion("btn_primary_up") ?: placeholders.buttonUp
    val buttonDown: TextureRegion get() = uiAtlas?.findRegion("btn_primary_down") ?: placeholders.buttonDown

    fun dieFace(value: Int): TextureRegion =
        uiAtlas?.findRegion("die_$value") ?: placeholders.dieFaces[(value - 1).coerceIn(0, 5)]

    private fun font(path: String): BitmapFont {
        val font = BitmapFont(Gdx.files.internal(path), false)
        font.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        font.setUseIntegerPositions(false)
        return font
    }

    override fun dispose() {
        placeholders.dispose()
        cardsAtlas?.dispose()
        uiAtlas?.dispose()
        if (::cardLetterFont.isInitialized) {
            cardLetterFont.dispose()
            titleLargeFont.dispose()
            titleFont.dispose()
            bodyFont.dispose()
            bodyBoldFont.dispose()
        }
    }

    private companion object {
        const val CARDS_ATLAS = "atlas/cards.atlas"
        const val UI_ATLAS = "atlas/ui.atlas"
    }
}
