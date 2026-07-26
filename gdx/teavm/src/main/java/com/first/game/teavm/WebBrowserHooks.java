package com.first.game.teavm;

import org.teavm.jso.JSBody;

/**
 * Браузерные обвязки, которые нельзя сделать из игрового цикла.
 *
 * Оба сценария ниже требуют выполнения <b>внутри самого обработчика события</b>:
 * к моменту, когда до кадра доберётся игровой цикл, жест пользователя уже
 * «протух», и браузер откажет.
 *
 * Написано на Java, а не на Kotlin: {@code @JSBody} требует статического
 * native-метода.
 */
public final class WebBrowserHooks {

    private WebBrowserHooks() {
    }

    /**
     * Возобновление звукового контекста.
     *
     * Мобильные браузеры держат AudioContext приостановленным, пока пользователь
     * не коснётся страницы, и снова усыпляют его при сворачивании вкладки.
     * Слушатели вешаются на документ и не снимаются: любое касание или возврат
     * на вкладку будят звук.
     */
    @JSBody(script =
        "var resume = function () {" +
        "  try {" +
        "    if (typeof Howler !== 'undefined' && Howler.ctx && Howler.ctx.state !== 'running') {" +
        "      Howler.ctx.resume();" +
        "    }" +
        "  } catch (e) {}" +
        "};" +
        "var events = ['touchstart', 'touchend', 'pointerup', 'mousedown', 'click', 'keydown'];" +
        "for (var i = 0; i < events.length; i++) {" +
        "  document.addEventListener(events[i], resume, { passive: true });" +
        "}" +
        "document.addEventListener('visibilitychange', function () {" +
        "  if (!document.hidden) { resume(); }" +
        "});"
    )
    public static native void installAudioResume();

    /**
     * Разворот в горизонтальную ориентацию на мобильных.
     *
     * Заблокировать ориентацию браузер разрешает только в полноэкранном режиме,
     * поэтому сначала запрашиваем его. Всё делается по первому касанию: без жеста
     * оба запроса отклоняются.
     *
     * На десктопе не срабатывает вовсе — там полный экран по клику был бы
     * навязчивым, а раскладка и так широкая.
     *
     * iOS Safari блокировку ориентации не поддерживает: запрос молча отклонится,
     * и игра останется в портретной раскладке — она у неё есть.
     */
    @JSBody(script =
        "var isMobile = ('ontouchstart' in window) || navigator.maxTouchPoints > 0;" +
        "if (isMobile) {" +
        "  var goLandscape = function () {" +
        "    try {" +
        "      var el = document.documentElement;" +
        "      var request = el.requestFullscreen || el.webkitRequestFullscreen;" +
        "      if (!document.fullscreenElement && request) {" +
        "        var result = request.call(el);" +
        "        if (result && result.then) { result.then(lock).catch(lock); } else { lock(); }" +
        "      } else {" +
        "        lock();" +
        "      }" +
        "    } catch (e) {}" +
        "  };" +
        "  var lock = function () {" +
        "    try {" +
        "      if (screen.orientation && screen.orientation.lock) {" +
        "        var locked = screen.orientation.lock('landscape');" +
        "        if (locked && locked.catch) { locked.catch(function () {}); }" +
        "      }" +
        "    } catch (e) {}" +
        "  };" +
        "  var once = function () {" +
        "    document.removeEventListener('touchend', once);" +
        "    document.removeEventListener('click', once);" +
        "    goLandscape();" +
        "  };" +
        "  document.addEventListener('touchend', once);" +
        "  document.addEventListener('click', once);" +
        "}"
    )
    public static native void installOrientationLock();
}
