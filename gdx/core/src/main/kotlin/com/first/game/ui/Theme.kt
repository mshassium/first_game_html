package com.first.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.first.game.assets.Assets

/**
 * Стили Scene2D, собранные в коде. Отдельный Skin с JSON не заводим: стилей мало,
 * а лишний формат — лишний источник рассинхрона с ассетами.
 */
class Theme(val assets: Assets) {

    val white: Drawable = TextureRegionDrawable(assets.white)

    // Резные углы и рамки нельзя растягивать целиком — они превращаются в кашу.
    // Растягивается только середина, поля задаются долей от размера спрайта.
    val panel: Drawable = stretchable("panel_wood", 0.22f, 0.30f, assets.panel)
    val panelStone: Drawable = stretchable("panel_stone", 0.22f, 0.30f, assets.panelStone)
    val panelParchment: Drawable = stretchable("panel_parchment", 0.22f, 0.30f, assets.panelStone)
    val modalFrame: Drawable = stretchable("modal_frame", 0.22f, 0.30f, assets.panel)
    val slot: Drawable = stretchable("slot_card", 0.30f, 0.30f, assets.slot)

    /** Журнал рисуется на пергаменте — значит, текст в нём должен быть тёмным. */
    val parchmentLog: Boolean = assets.uiRegion("panel_parchment") != null

    val titleLarge = Label.LabelStyle(assets.titleLargeFont, Color.WHITE)
    val title = Label.LabelStyle(assets.titleFont, Color.WHITE)
    val body = Label.LabelStyle(assets.bodyFont, Palette.TEXT)
    val bodyMuted = Label.LabelStyle(assets.bodyFont, Palette.TEXT_MUTED)
    val bodyBold = Label.LabelStyle(assets.bodyBoldFont, Palette.TEXT)

    val button = TextButton.TextButtonStyle(
        stretchable("btn_primary_up", 0.12f, 0.32f, assets.buttonUp),
        stretchable("btn_primary_down", 0.12f, 0.32f, assets.buttonDown),
        null,
        assets.titleFont,
    ).apply {
        disabled = stretchable("btn_primary_disabled", 0.12f, 0.32f, assets.buttonUp)
        fontColor = Color.WHITE
        downFontColor = Palette.GOLD_LIGHT
        disabledFontColor = Palette.TEXT_MUTED
    }

    /**
     * 9-patch из атласа, если спрайт нарисован; иначе — обычная растяжка заглушки.
     * [horizontal] и [vertical] — доли ширины и высоты, которые остаются нерастяжимыми.
     */
    private fun stretchable(
        name: String,
        horizontal: Float,
        vertical: Float,
        fallback: TextureRegion,
    ): Drawable {
        val region: TextureRegion = assets.uiRegion(name) ?: return TextureRegionDrawable(fallback)
        val left = (region.regionWidth * horizontal).toInt().coerceAtLeast(1)
        val top = (region.regionHeight * vertical).toInt().coerceAtLeast(1)
        val patch = NinePatch(region, left, left, top, top)
        // Обнуляем отступы: по умолчанию 9-patch отдаёт их равными нерастяжимым краям,
        // а те заданы в пикселях спрайта — на кнопке высотой 70 единиц мира это
        // 33 единицы сверху, и подпись выдавливает к нижнему ободу.
        patch.setPadding(0f, 0f, 0f, 0f)
        return NinePatchDrawable(patch)
    }

    fun dim(alpha: Float): Drawable = TextureRegionDrawable(assets.white).tint(Palette.rgba(Palette.SHADOW, alpha))

    fun tinted(color: Color): Drawable = TextureRegionDrawable(assets.white).tint(color)
}
