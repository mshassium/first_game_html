package com.first.game.domain

/**
 * Правила игры целиком. Чистая функция вида (состояние, команда) -> (новое состояние, события).
 *
 * Движок ничего не знает ни о libGDX, ни об анимациях: когда [apply] вернул управление,
 * состояние уже финальное, а список событий описывает лишь то, что нужно показать.
 *
 * Ход оппонента разворачивается автоматически: после розыгрыша карты движок сам
 * передаёт ход, добирает карту и отрабатывает ловушку — и останавливается ровно там,
 * где нужен выбор стороны ([Phase.AWAITING_CHOICE]) или розыгрыш ([Phase.AWAITING_PLAY]).
 */
class GameEngine(private val rng: Rng) {

    // ---------------------------------------------------------------- запуск

    /** Новая партия: бросок кубиков, перемешивание колод, раздача. */
    fun newGame(): EngineResult {
        var youRoll = rng.rollDie()
        var aiRoll = rng.rollDie()
        while (youRoll == aiRoll) {
            youRoll = rng.rollDie()
            aiRoll = rng.rollDie()
        }
        val firstPlayer = if (youRoll > aiRoll) Side.YOU else Side.AI
        return startGame(
            youDeck = buildDeck().shuffled(rng),
            aiDeck = buildDeck().shuffled(rng),
            firstPlayer = firstPlayer,
            youRoll = youRoll,
            aiRoll = aiRoll,
        )
    }

    /**
     * Партия с заданными колодами — для тестов и для воспроизведения багов.
     * Карты берутся с конца списка (как из стопки).
     */
    fun startGame(
        youDeck: List<Letter>,
        aiDeck: List<Letter>,
        firstPlayer: Side,
        youRoll: Int = 0,
        aiRoll: Int = 0,
    ): EngineResult {
        val events = mutableListOf<GameEvent>()
        events += GameEvent.GameStarted(firstPlayer, youRoll, aiRoll)

        var state = GameState(
            you = SideState(deck = youDeck),
            ai = SideState(deck = aiDeck),
            turn = firstPlayer,
            firstPlayer = firstPlayer,
            firstTurnDone = false,
        )

        repeat(Rules.START_HAND) {
            for (side in listOf(Side.YOU, Side.AI)) {
                val deck = state.side(side).deck
                if (deck.isNotEmpty()) {
                    val letter = deck.last()
                    state = state.withSide(side) {
                        it.copy(deck = it.deck.dropLast(1), hand = it.hand + letter)
                    }
                    events += GameEvent.CardDealt(side, letter)
                }
            }
        }

        state = beginTurn(state, firstPlayer, events)
        return EngineResult(state, events)
    }

    private fun buildDeck(): List<Letter> =
        Letter.ALL.flatMap { letter -> List(Rules.CARDS_PER_LETTER) { letter } }

    // ------------------------------------------------------------ применение

    fun apply(state: GameState, command: Command): EngineResult {
        if (state.isOver) return EngineResult(state, emptyList())
        return when (command) {
            is Command.PlayCard -> playCard(state, command.handIndex)
            is Command.ChooseOption -> chooseOption(state, command.optionIndex)
        }
    }

    // -------------------------------------------------------------- розыгрыш

    private fun playCard(state: GameState, handIndex: Int): EngineResult {
        if (state.phase != Phase.AWAITING_PLAY) return EngineResult(state, emptyList())
        val side = state.turn
        val hand = state.side(side).hand
        if (handIndex !in hand.indices) return EngineResult(state, emptyList())

        val events = mutableListOf<GameEvent>()
        val letter = hand[handIndex]
        var next = state.withSide(side) { it.copy(hand = it.hand.removeAt(handIndex)) }

        // Запрет: карта уходит в свой сброс, эффект не срабатывает, запрет снимается.
        if (next.traps.forbidOn(side) == letter) {
            next = next
                .withSide(side) { it.copy(discard = it.discard + letter) }
                .copy(traps = next.traps.withForbidOn(side, null))
            events += GameEvent.CardForbidden(side, letter, handIndex)
            return EngineResult(finishTurn(next, side, events), events)
        }

        next = next.withSide(side) { it.copy(space = it.space + letter) }
        events += GameEvent.CardPlayed(side, letter, handIndex)

        // Победа проверяется до разрешения эффекта: иначе игрок, уже собравший набор,
        // получал бы бессмысленную модалку выбора. В старой HTML-версии проверка шла
        // после эффекта — расхождение осознанное.
        checkWin(next, side)?.let { reason ->
            return EngineResult(endGame(next, side, reason, events), events)
        }

        next = resolveEffect(next, side, letter, events)
        if (next.isOver || next.phase == Phase.AWAITING_CHOICE) {
            return EngineResult(next, events)
        }
        return EngineResult(finishTurn(next, side, events), events)
    }

    private fun resolveEffect(
        state: GameState,
        side: Side,
        letter: Letter,
        events: MutableList<GameEvent>,
    ): GameState = when (letter) {

        Letter.F -> requireChoice(
            state, side, ChoiceKind.FORBID_LETTER,
            Letter.ALL.mapIndexed { index, l -> ChoiceOption(index, l) },
            events,
        )

        Letter.I -> draw(state, side, events)

        Letter.R -> {
            val discard = state.side(side).discard
            if (discard.isEmpty()) {
                events += GameEvent.EffectFizzled(side, letter)
                state
            } else {
                // Один вариант на букву: возвращается последнее вхождение этой буквы.
                val options = Letter.ALL
                    .map { l -> l to discard.lastIndexOf(l) }
                    .filter { (_, index) -> index >= 0 }
                    .map { (l, index) -> ChoiceOption(index, l) }
                requireChoice(state, side, ChoiceKind.RECOVER_LETTER, options, events)
            }
        }

        Letter.S -> {
            val victimSpace = state.side(side.other).space
            if (victimSpace.isEmpty()) {
                events += GameEvent.EffectFizzled(side, letter)
                state
            } else {
                val options = victimSpace.mapIndexed { index, l -> ChoiceOption(index, l) }
                requireChoice(state, side, ChoiceKind.STEAL_TARGET, options, events)
            }
        }

        Letter.T -> {
            val count = state.traps.trapsOn(side.other) + 1
            events += GameEvent.TrapSet(side.other, count)
            state.copy(traps = state.traps.withTrapsOn(side.other, count))
        }
    }

    private fun requireChoice(
        state: GameState,
        side: Side,
        kind: ChoiceKind,
        options: List<ChoiceOption>,
        events: MutableList<GameEvent>,
    ): GameState {
        val choice = PendingChoice(side, kind, options)
        events += GameEvent.ChoiceRequired(choice)
        return state.copy(phase = Phase.AWAITING_CHOICE, pending = choice)
    }

    // ----------------------------------------------------------------- выбор

    private fun chooseOption(state: GameState, optionIndex: Int): EngineResult {
        val pending = state.pending ?: return EngineResult(state, emptyList())
        if (state.phase != Phase.AWAITING_CHOICE) return EngineResult(state, emptyList())
        if (optionIndex !in pending.options.indices) return EngineResult(state, emptyList())

        val events = mutableListOf<GameEvent>()
        val option = pending.options[optionIndex]
        val side = pending.side
        var next = state.copy(phase = Phase.AWAITING_PLAY, pending = null)

        when (pending.kind) {
            ChoiceKind.FORBID_LETTER -> {
                next = next.copy(traps = next.traps.withForbidOn(side.other, option.letter))
                events += GameEvent.ForbidSet(side, option.letter)
            }

            ChoiceKind.RECOVER_LETTER -> {
                next = next.withSide(side) {
                    it.copy(discard = it.discard.removeAt(option.index), hand = it.hand + option.letter)
                }
                events += GameEvent.CardRecovered(side, option.letter)
                next = enforceHandLimit(next, side, events)
            }

            ChoiceKind.STEAL_TARGET -> {
                val victim = side.other
                next = next.withSide(victim) {
                    it.copy(space = it.space.removeAt(option.index), discard = it.discard + option.letter)
                }
                events += GameEvent.CardStolen(victim, option.letter, option.index)
            }

            ChoiceKind.TRAP_DISCARD -> {
                next = next.withSide(side) {
                    it.copy(hand = it.hand.removeAt(option.index), discard = it.discard + option.letter)
                }
                val left = (next.traps.trapsOn(side) - 1).coerceAtLeast(0)
                next = next.copy(traps = next.traps.withTrapsOn(side, left))
                events += GameEvent.TrapTriggered(side, option.letter, option.index, left)
                // Ловушка не завершает ход: сторона всё равно разыгрывает карту.
                return EngineResult(continueAfterTrap(next, side, events), events)
            }
        }

        return EngineResult(finishTurn(next, side, events), events)
    }

    // ------------------------------------------------------------------- ход

    private fun beginTurn(state: GameState, side: Side, events: MutableList<GameEvent>): GameState {
        var next = state.copy(
            turn = side,
            turnNumber = state.turnNumber + 1,
            phase = Phase.AWAITING_PLAY,
            pending = null,
        )
        events += GameEvent.TurnBegan(side, next.turnNumber)

        val skipDraw = !next.firstTurnDone && side == next.firstPlayer
        if (!skipDraw) {
            next = draw(next, side, events)
            if (next.isOver) return next
        }
        next = next.copy(firstTurnDone = true)

        val traps = next.traps.trapsOn(side)
        if (traps > 0) {
            val hand = next.side(side).hand
            if (hand.isEmpty()) {
                val left = traps - 1
                next = next.copy(traps = next.traps.withTrapsOn(side, left))
                events += GameEvent.TrapFizzled(side, left)
            } else {
                val options = hand.mapIndexed { index, l -> ChoiceOption(index, l) }
                return requireChoice(next, side, ChoiceKind.TRAP_DISCARD, options, events)
            }
        }

        return continueAfterTrap(next, side, events)
    }

    /** Продолжение хода после фазы ловушки: либо играем, либо пропускаем ход из-за пустой руки. */
    private fun continueAfterTrap(state: GameState, side: Side, events: MutableList<GameEvent>): GameState {
        if (state.isOver) return state
        if (state.side(side).hand.isEmpty()) {
            events += GameEvent.TurnSkipped(side)
            events += GameEvent.TurnEnded(side)
            return beginTurn(state, side.other, events)
        }
        return state.copy(phase = Phase.AWAITING_PLAY, pending = null)
    }

    private fun finishTurn(state: GameState, side: Side, events: MutableList<GameEvent>): GameState {
        if (state.isOver) return state
        checkWin(state, side)?.let { reason ->
            return endGame(state, side, reason, events)
        }
        events += GameEvent.TurnEnded(side)
        return beginTurn(state, side.other, events)
    }

    // --------------------------------------------------------------- добор

    private fun draw(state: GameState, side: Side, events: MutableList<GameEvent>): GameState {
        val deck = state.side(side).deck
        if (deck.isEmpty()) {
            return endGame(state, side.other, EndReason.DECK_OUT, events)
        }
        val letter = deck.last()
        var next = state.withSide(side) {
            it.copy(deck = it.deck.dropLast(1), hand = it.hand + letter)
        }
        events += GameEvent.CardDrawn(side, letter, next.side(side).deck.size)
        next = enforceHandLimit(next, side, events)
        return next
    }

    private fun enforceHandLimit(
        state: GameState,
        side: Side,
        events: MutableList<GameEvent>,
    ): GameState {
        var next = state
        while (next.side(side).hand.size > Rules.HAND_LIMIT) {
            val overflowIndex = next.side(side).hand.lastIndex
            val overflow = next.side(side).hand[overflowIndex]
            next = next.withSide(side) {
                it.copy(hand = it.hand.dropLast(1), discard = it.discard + overflow)
            }
            events += GameEvent.HandOverflow(side, overflow, overflowIndex)
        }
        return next
    }

    // -------------------------------------------------------------- финал

    private fun checkWin(state: GameState, side: Side): EndReason? {
        val space = state.side(side)
        return when {
            space.distinctInSpace == Letter.ALL.size -> EndReason.FIRST_SET
            space.maxSameInSpace >= Rules.SAME_LETTERS_TO_WIN -> EndReason.FIVE_OF_A_KIND
            else -> null
        }
    }

    private fun endGame(
        state: GameState,
        winner: Side,
        reason: EndReason,
        events: MutableList<GameEvent>,
    ): GameState {
        val outcome = Outcome(winner, reason)
        events += GameEvent.GameEnded(outcome)
        return state.copy(phase = Phase.GAME_OVER, pending = null, outcome = outcome)
    }
}

internal fun <T> List<T>.removeAt(index: Int): List<T> =
    toMutableList().also { it.removeAt(index) }
