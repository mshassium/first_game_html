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
    private var vfxAtlas: TextureAtlas? = null
    private val backgrounds = mutableMapOf<String, Texture>()

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
        if (Gdx.files.internal(VFX_ATLAS).exists()) {
            vfxAtlas = TextureAtlas(Gdx.files.internal(VFX_ATLAS))
        }
        for (name in listOf("bg_menu", "bg_table_landscape", "bg_table_portrait", "bg_loading")) {
            val handle = Gdx.files.internal("bg/$name.jpg")
            if (!handle.exists()) continue
            backgrounds[name] = Texture(handle, true).apply {
                setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
            }
        }

        usesGeneratedArt = cardsAtlas != null
    }

    /**
     * Полноэкранный фон или null, если он ещё не нарисован.
     * Фоны лежат отдельными JPEG вне атласов: они крупные, без прозрачности
     * и в атласе занимали бы целую страницу каждый.
     */
    fun background(name: String): Texture? = backgrounds[name]

    /** Регион из ui.atlas или null, если атласа ещё нет или спрайт не нарисован. */
    fun uiRegion(name: String): TextureRegion? = uiAtlas?.findRegion(name)

    /** Элемент эффекта из vfx.atlas или null, если он ещё не нарисован. */
    fun vfxRegion(name: String): TextureRegion? = vfxAtlas?.findRegion(name)

    /**
     * Иконка интерфейса по имени без префикса: `icon("settings")`.
     * Возвращает null, если иконка ещё не нарисована — вызывающий код решает,
     * рисовать ли текстовую замену.
     */
    fun icon(name: String): TextureRegion? = uiRegion("icon_$name")

    fun cardFace(letter: Letter): TextureRegion =
        cardsAtlas?.findRegion("card_${letter.name}") ?: placeholders.cardFaces.getValue(letter)

    val cardBack: TextureRegion
        get() = cardsAtlas?.findRegion("card_back") ?: placeholders.cardBack

    val white: TextureRegion get() = placeholders.white
    val glow: TextureRegion get() = vfxRegion("fx_glow_soft") ?: placeholders.glow
    val panel: TextureRegion get() = uiAtlas?.findRegion("panel_wood") ?: placeholders.panel
    val panelStone: TextureRegion get() = uiAtlas?.findRegion("panel_stone") ?: placeholders.panelStone
    val slot: TextureRegion get() = uiAtlas?.findRegion("slot_card") ?: placeholders.slot
    val buttonUp: TextureRegion get() = uiAtlas?.findRegion("btn_primary_up") ?: placeholders.buttonUp
    val buttonDown: TextureRegion get() = uiAtlas?.findRegion("btn_primary_down") ?: placeholders.buttonDown

    /** Круглая кнопка под иконку: у неё своя подложка, растянутая широкая смотрится дёшево. */
    val roundButtonUp: TextureRegion get() = uiRegion("btn_round_up") ?: buttonUp
    val roundButtonDown: TextureRegion get() = uiRegion("btn_round_down") ?: buttonDown

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
        vfxAtlas?.dispose()
        backgrounds.values.forEach { it.dispose() }
        backgrounds.clear()
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
        const val VFX_ATLAS = "atlas/vfx.atlas"
    }
}
