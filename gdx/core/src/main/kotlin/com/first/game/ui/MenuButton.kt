package com.first.game.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.first.game.assets.Assets
import com.first.game.audio.SoundManager

/**
 * Кнопка в стиле главного меню: рамка, подпись по центру, иконка у левого края.
 *
 * Живёт отдельно от экранов, потому что таких кнопок теперь три экрана — меню,
 * список комнат и лобби, — и разъехавшийся отступ подписи виден сразу.
 *
 * Высота подписи задаётся явно. По умолчанию Label просит
 * `capHeight - descent * 2`, у заголовочного шрифта это 71 пиксель при высоте
 * кнопки 61: бокс не влезает, строка распирает таблицу, и содержимое выходит
 * за кнопку — иконка упирается в верхний кант, а текст садится ниже неё.
 */
fun menuButton(
    theme: Theme,
    assets: Assets,
    sound: SoundManager,
    text: String,
    icon: String? = null,
    /** Высота прописных в мире экрана: от неё считаются отступы. */
    capSize: Float,
    action: () -> Unit,
): TextButton {
    val button = TextButton(text, theme.button)
    val label = button.label
    val region = assets.icon(icon ?: "")

    button.clearChildren()
    label.setAlignment(Align.center)
    if (region != null) {
        // Иконка и подпись лежат в разных слоях: иконка прижата к левому краю,
        // подпись центрируется по всей ширине кнопки. В одной строке таблицы
        // подпись центровалась бы по остатку после иконки и уезжала вправо.
        button.pad(0f, capSize * 0.28f, 0f, capSize * 0.28f)
        val iconLayer = Table().apply {
            add(Image(region)).size(capSize).expandX().left().padLeft(capSize * ICON_INSET)
        }
        val textLayer = Table().apply {
            add(label).height(capSize).expand().center().padBottom(theme.capSink(label) * 2f)
        }
        button.stack(textLayer, iconLayer).grow()
    } else {
        button.pad(0f, capSize * 0.5f, 0f, capSize * 0.5f)
        button.add(label).height(capSize).expandX().padBottom(theme.capSink(label) * 2f)
    }

    button.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent?, x: Float, y: Float) {
            sound.play(SoundManager.Sfx.UI_CLICK)
            action()
        }
    })
    return button
}

/** Насколько иконка отступает от края, в долях своего размера. */
private const val ICON_INSET = 0.35f
