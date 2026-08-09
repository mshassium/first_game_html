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
    // Панели из COMPACT приходят из упаковщика уже обрезанными до канта с углами:
    // у них кант занимает ровно 45% стороны, и линия разреза должна совпадать,
    // иначе в растяжку попадёт угловая накладка.
    // Панель руки — единственная с заметной текстурой, поэтому она не сжимается,
    // а растягивается мягко: нерастяжимыми остаются только кант с углами, а середина
    // занимает большую часть спрайта и тянется примерно в полтора раза.
    val panel: Drawable = stretchable("panel_wood", 0.06f, 0.17f, assets.panel)
    val panelStone: Drawable = stretchable("panel_stone", COMPACT_SPLIT, COMPACT_SPLIT, assets.panelStone)
    val panelParchment: Drawable = stretchable("panel_parchment", 0.22f, 0.30f, assets.panelStone)
    val modalFrame: Drawable = stretchable("modal_frame", COMPACT_SPLIT, COMPACT_SPLIT, assets.panel)
    val slot: Drawable = stretchable("slot_card", 0.30f, 0.30f, assets.slot)

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

    /** Кнопка для узких мест: те же спрайты, но с ужатыми углами. */
    val buttonCompact = TextButton.TextButtonStyle(
        stretchable("btn_primary_up", 0.12f, 0.32f, assets.buttonUp, COMPACT_BUTTON_SCALE),
        stretchable("btn_primary_down", 0.12f, 0.32f, assets.buttonDown, COMPACT_BUTTON_SCALE),
        null,
        assets.titleFont,
    ).apply {
        fontColor = Color.WHITE
        downFontColor = Palette.GOLD_LIGHT
    }

    /**
     * 9-patch из атласа, если спрайт нарисован; иначе — обычная растяжка заглушки.
     * [horizontal] и [vertical] — доли ширины и высоты, которые остаются нерастяжимыми,
     * [scale] ужимает неподвижные углы для кнопок, которые ниже спрайта.
     */
    private fun stretchable(
        name: String,
        horizontal: Float,
        vertical: Float,
        fallback: TextureRegion,
        scale: Float = 1f,
    ): Drawable {
        val region: TextureRegion = assets.uiRegion(name) ?: return TextureRegionDrawable(fallback)
        val left = (region.regionWidth * horizontal).toInt().coerceAtLeast(1)
        val top = (region.regionHeight * vertical).toInt().coerceAtLeast(1)
        val patch = NinePatch(region, left, left, top, top)
        // Неподвижные углы рисуются в пикселях спрайта. У кнопки это 86 единиц по
        // высоте, и в кнопку ниже они попросту не влезают: верх и низ наезжают друг
        // на друга, и нижний кант уходит за границу. Масштаб ужимает сами углы.
        if (scale != 1f) patch.scale(scale, scale)
        // Обнуляем отступы: по умолчанию 9-patch отдаёт их равными нерастяжимым краям,
        // а те заданы в пикселях спрайта — на кнопке высотой 70 единиц мира это
        // 33 единицы сверху, и подпись выдавливает к нижнему ободу.
        patch.setPadding(0f, 0f, 0f, 0f)
        return NinePatchDrawable(patch)
    }

    /**
     * Насколько Label опускает прописные буквы ниже центра своего бокса.
     *
     * Вертикальную центровку Label делает по высоте разметки текста, а она не
     * совпадает с прописными: у шрифта заголовков capHeight 39 при боксе 71.
     * Величина измерена по экрану настроек и задана в долях capHeight, чтобы
     * не зависеть от размера кнопки — от него смещение не меняется.
     * Отступ ячейки сдвигает виджет на половину своей величины, поэтому
     * вызывающий удваивает результат.
     */
    fun capSink(label: Label): Float = label.style.font.capHeight * CAP_SINK

    fun dim(alpha: Float): Drawable = TextureRegionDrawable(assets.white).tint(Palette.rgba(Palette.SHADOW, alpha))

    fun tinted(color: Color): Drawable = TextureRegionDrawable(assets.white).tint(color)

    private companion object {
        /**
         * Линия разреза для панелей, которые упаковщик сжимает до канта с углами
         * (список COMPACT в AtlasPacker). Число задано там же при сборке спрайта.
         */
        const val COMPACT_SPLIT = 0.45f

        /**
         * Во сколько раз ужимаются углы кнопки в узких местах. При 86 неподвижных
         * единицах спрайта и кнопке высотой около 52 углы должны занимать не больше
         * двух третей высоты, иначе на растяжку ничего не остаётся.
         */
        const val COMPACT_BUTTON_SCALE = 0.42f

        /** Доля capHeight, на которую Label опускает прописные ниже центра бокса. */
        const val CAP_SINK = 0.29f
    }
}
