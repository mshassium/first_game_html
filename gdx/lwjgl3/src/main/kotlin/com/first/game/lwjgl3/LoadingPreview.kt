package com.first.game.lwjgl3

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.first.game.ui.LoadingArt
import com.first.game.ui.Palette

/**
 * Показ экрана загрузки на десктопе: `-Dfirst.boot=loading`.
 *
 * Настоящий экран загрузки бывает только в вебе и живёт полторы секунды на чужом
 * канале — увидеть его иначе нечем, а браузер без GPU (headless) эту страницу
 * вообще не рисует. Прогресс здесь поддельный и просто едет по кругу.
 */
class LoadingPreview : ApplicationAdapter() {

    private val art = LoadingArt()
    private lateinit var batch: SpriteBatch
    private var elapsed = 0f

    override fun create() {
        batch = SpriteBatch()
        art.load()
    }

    override fun render() {
        elapsed += Gdx.graphics.deltaTime
        ScreenUtils.clear(Palette.SHADOW.r, Palette.SHADOW.g, Palette.SHADOW.b, 1f, true)
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        batch.projectionMatrix = batch.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        batch.begin()
        art.draw(batch, width, height, (elapsed % CYCLE) / CYCLE)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        art.dispose()
    }

    private companion object {
        /** За столько секунд поддельная полоса проходит от края до края. */
        const val CYCLE = 4f
    }
}
