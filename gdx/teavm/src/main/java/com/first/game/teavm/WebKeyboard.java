package com.first.game.teavm;

import org.teavm.jso.JSBody;

/**
 * Клавиатура браузера для полей ввода игры.
 *
 * Веб-бэкенд libGDX клавиатуру не показывает: {@code setOnscreenKeyboardVisible}
 * там пустой метод. На телефоне это значит, что поле с курсором нарисовано, а
 * набрать в него нечего — ни ника, ни кода комнаты.
 *
 * Поэтому на странице живёт невидимое поле ввода. Клавиатуру открывает браузер
 * — как для любого поля, — а игра каждый кадр забирает набранную строку и
 * рисует её сама. Строку целиком, а не отдельные нажатия: подсказки, автозамена
 * и диктовка правят уже набранное слово, и посимвольного потока от них нет.
 *
 * Написано на Java, а не на Kotlin: {@code @JSBody} требует статического
 * native-метода.
 */
public final class WebKeyboard {

    private WebKeyboard() {
    }

    /** На десктопе клавиатура и так физическая, подменять ввод там незачем. */
    @JSBody(script = "return ('ontouchstart' in window) || navigator.maxTouchPoints > 0;")
    public static native boolean isTouchDevice();

    /**
     * Заводит скрытое поле и слушателей.
     *
     * Нажатия до игры доходить не должны: их слушает документ, и каждый символ
     * попал бы в строку дважды — один раз от браузера, другой от libGDX.
     */
    @JSBody(script =
        "var setup = function () {" +
        "  if (window.__firstKeyboard) { return; }" +
        "  var input = document.createElement('input');" +
        "  input.type = 'text';" +
        "  input.setAttribute('autocomplete', 'off');" +
        "  input.setAttribute('autocorrect', 'off');" +
        "  input.setAttribute('autocapitalize', 'off');" +
        "  input.setAttribute('spellcheck', 'false');" +
        "  input.setAttribute('enterkeyhint', 'done');" +
        // Размер в пиксель и полная прозрачность: поле нужно только ради
        // клавиатуры, текст игрок видит в игровом. Шрифт мельче 16px iOS
        // Safari встречает наездом камеры на страницу, поэтому ровно 16.
        "  input.style.cssText = 'position:fixed;left:0;top:0;width:1px;height:1px;opacity:0;" +
        "border:0;padding:0;margin:0;font-size:16px;background:transparent;caret-color:transparent;';" +
        "  var state = { input: input, active: false, dirty: false, submit: false };" +
        "  var swallow = function (e) { e.stopPropagation(); };" +
        "  input.addEventListener('keydown', function (e) {" +
        "    e.stopPropagation();" +
        "    if (e.key === 'Enter') { state.submit = true; e.preventDefault(); }" +
        "  });" +
        "  input.addEventListener('keypress', swallow);" +
        "  input.addEventListener('keyup', swallow);" +
        "  input.addEventListener('input', function () { state.dirty = true; });" +
        // Второй заход за фокусом. Клавиатуру браузер открывает только по жесту
        // игрока: запрос из игрового цикла — форма открылась сама, поле ждёт —
        // пропадает молча. Ближайшее касание отдаёт фокус полю, если оно всё
        // ещё ждёт ввода.
        "  var refocus = function () {" +
        "    if (state.active && document.activeElement !== input) {" +
        "      try { input.focus({ preventScroll: true }); } catch (e) {}" +
        "    }" +
        "  };" +
        "  document.addEventListener('touchend', refocus, false);" +
        "  document.addEventListener('click', refocus, false);" +
        "  document.body.appendChild(input);" +
        "  window.__firstKeyboard = state;" +
        "};" +
        "if (document.body) { setup(); } else { document.addEventListener('DOMContentLoaded', setup); }"
    )
    public static native void install();

    /** Открыть клавиатуру. Только из обработчика касания — иначе браузер откажет. */
    @JSBody(params = {"value", "maxLength", "secret"}, script =
        "var s = window.__firstKeyboard;" +
        "if (!s) { return; }" +
        "s.input.type = secret ? 'password' : 'text';" +
        "s.input.maxLength = maxLength;" +
        "s.input.value = value;" +
        "s.active = true;" +
        "s.dirty = false;" +
        "s.submit = false;" +
        "try {" +
        "  s.input.focus({ preventScroll: true });" +
        "  s.input.setSelectionRange(value.length, value.length);" +
        "} catch (e) {}"
    )
    public static native void open(String value, int maxLength, boolean secret);

    /** Убрать клавиатуру и забыть набранное: в поле мог остаться пароль. */
    @JSBody(script =
        "var s = window.__firstKeyboard;" +
        "if (!s) { return; }" +
        "s.active = false;" +
        "s.dirty = false;" +
        "s.submit = false;" +
        "s.input.value = '';" +
        "try { s.input.blur(); } catch (e) {}"
    )
    public static native void close();

    /** Менялся ли текст с прошлого опроса. */
    @JSBody(script =
        "var s = window.__firstKeyboard;" +
        "if (!s || !s.dirty) { return false; }" +
        "s.dirty = false;" +
        "return true;"
    )
    public static native boolean consumeChanged();

    /** Набранное на данный момент. */
    @JSBody(script = "var s = window.__firstKeyboard; return s ? s.input.value : '';")
    public static native String value();

    /** Нажат ли «Готово» на клавиатуре. */
    @JSBody(script =
        "var s = window.__firstKeyboard;" +
        "if (!s || !s.submit) { return false; }" +
        "s.submit = false;" +
        "return true;"
    )
    public static native boolean consumeSubmit();

    /**
     * Доля экрана снизу под клавиатурой.
     *
     * Клавиатура не двигает страницу — она накрывает её снизу, и посчитать её
     * высоту можно только по видимой области. Мелкие расхождения (адресная
     * строка браузера то прячется, то нет) отбрасываются как ноль.
     */
    @JSBody(script =
        "var view = window.visualViewport;" +
        "if (!view || !window.innerHeight) { return 0; }" +
        "var covered = 1 - view.height / window.innerHeight;" +
        "if (covered < 0.05) { return 0; }" +
        "return covered > 0.8 ? 0.8 : covered;"
    )
    public static native double coveredFraction();
}
