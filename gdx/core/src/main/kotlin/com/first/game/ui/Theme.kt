package com.first.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.first.game.assets.Assets

/**
 * Стили Scene2D, собранные в коде. Отдельный Skin с JSON не заводим: стилей мало,
 * а лишний формат — лишний источник рассинхрона с ассетами.
 */
class Theme(val assets: Assets) {

    val white: Drawable = TextureRegionDrawable(assets.white)
    val panel: Drawable = TextureRegionDrawable(assets.panel)
    val panelStone: Drawable = TextureRegionDrawable(assets.panelStone)
    val slot: Drawable = TextureRegionDrawable(assets.slot)

    val titleLarge = Label.LabelStyle(assets.titleLargeFont, Color.WHITE)
    val title = Label.LabelStyle(assets.titleFont, Color.WHITE)
    val body = Label.LabelStyle(assets.bodyFont, Palette.TEXT)
    val bodyMuted = Label.LabelStyle(assets.bodyFont, Palette.TEXT_MUTED)
    val bodyBold = Label.LabelStyle(assets.bodyBoldFont, Palette.TEXT)

    val button = TextButton.TextButtonStyle(
        TextureRegionDrawable(assets.buttonUp),
        TextureRegionDrawable(assets.buttonDown),
        null,
        assets.titleFont,
    ).apply {
        fontColor = Color.WHITE
        downFontColor = Palette.GOLD_LIGHT
        disabledFontColor = Palette.TEXT_MUTED
    }

    fun dim(alpha: Float): Drawable = TextureRegionDrawable(assets.white).tint(Palette.rgba(Palette.SHADOW, alpha))

    fun tinted(color: Color): Drawable = TextureRegionDrawable(assets.white).tint(color)
}
