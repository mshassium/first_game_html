(function (factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', './kotlin-kotlin-stdlib.js'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('./kotlin-kotlin-stdlib.js'));
  else {
    if (typeof globalThis['kotlin-kotlin-stdlib'] === 'undefined') {
      throw new Error("Error loading module 'com.first.game:rules'. Its dependency 'kotlin-kotlin-stdlib' was not found. Please, check whether 'kotlin-kotlin-stdlib' is loaded prior to 'com.first.game:rules'.");
    }
    globalThis['com.first.game:rules'] = factory(typeof globalThis['com.first.game:rules'] === 'undefined' ? {} : globalThis['com.first.game:rules'], globalThis['kotlin-kotlin-stdlib']);
  }
}(function (_, kotlin_kotlin) {
  'use strict';
  //region block: imports
  var protoOf = kotlin_kotlin.$_$.p1;
  var initMetadataForClass = kotlin_kotlin.$_$.l1;
  var VOID = kotlin_kotlin.$_$.a;
  var toString = kotlin_kotlin.$_$.q1;
  var equals = kotlin_kotlin.$_$.k1;
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.c;
  var ArrayList_init_$Create$_0 = kotlin_kotlin.$_$.b;
  var Unit_instance = kotlin_kotlin.$_$.m;
  var addAll = kotlin_kotlin.$_$.o;
  var emptyList = kotlin_kotlin.$_$.t;
  var collectionSizeOrDefault = kotlin_kotlin.$_$.r;
  var checkIndexOverflow = kotlin_kotlin.$_$.q;
  var to = kotlin_kotlin.$_$.f2;
  var noWhenBranchMatchedException = kotlin_kotlin.$_$.d2;
  var coerceAtLeast = kotlin_kotlin.$_$.s1;
  var Collection = kotlin_kotlin.$_$.n;
  var isInterface = kotlin_kotlin.$_$.o1;
  var last = kotlin_kotlin.$_$.w;
  var get_lastIndex = kotlin_kotlin.$_$.v;
  var dropLast = kotlin_kotlin.$_$.s;
  var plus = kotlin_kotlin.$_$.y;
  var listOf = kotlin_kotlin.$_$.x;
  var toMutableList = kotlin_kotlin.$_$.a1;
  var toList = kotlin_kotlin.$_$.z;
  var initMetadataForCompanion = kotlin_kotlin.$_$.m1;
  var THROW_IAE = kotlin_kotlin.$_$.b2;
  var enumEntries = kotlin_kotlin.$_$.b1;
  var Enum = kotlin_kotlin.$_$.y1;
  var checkCountOverflow = kotlin_kotlin.$_$.p;
  var NoSuchElementException_init_$Create$ = kotlin_kotlin.$_$.f;
  var compareTo = kotlin_kotlin.$_$.i1;
  var toString_0 = kotlin_kotlin.$_$.e2;
  var Random = kotlin_kotlin.$_$.r1;
  var _Char___init__impl__6a9atx = kotlin_kotlin.$_$.g;
  var charArrayOf = kotlin_kotlin.$_$.f1;
  var split = kotlin_kotlin.$_$.t1;
  var IllegalArgumentException_init_$Create$ = kotlin_kotlin.$_$.e;
  var toIntOrNull = kotlin_kotlin.$_$.v1;
  var Companion_instance = kotlin_kotlin.$_$.l;
  var _Result___init__impl__xyqfz8 = kotlin_kotlin.$_$.i;
  var createFailure = kotlin_kotlin.$_$.c2;
  var _Result___get_value__impl__bjfvqg = kotlin_kotlin.$_$.k;
  var THROW_CCE = kotlin_kotlin.$_$.a2;
  var _Result___get_isFailure__impl__jpiriv = kotlin_kotlin.$_$.j;
  var initMetadataForObject = kotlin_kotlin.$_$.n1;
  var toInt = kotlin_kotlin.$_$.w1;
  var joinToString = kotlin_kotlin.$_$.u;
  var charSequenceLength = kotlin_kotlin.$_$.h1;
  var Long = kotlin_kotlin.$_$.z1;
  var toLongOrNull = kotlin_kotlin.$_$.x1;
  var charCodeAt = kotlin_kotlin.$_$.g1;
  var Char__toInt_impl_vasixd = kotlin_kotlin.$_$.h;
  var fromInt = kotlin_kotlin.$_$.d1;
  var bitwiseXor = kotlin_kotlin.$_$.c1;
  var multiply = kotlin_kotlin.$_$.e1;
  var StringBuilder_init_$Create$ = kotlin_kotlin.$_$.d;
  var toBooleanStrict = kotlin_kotlin.$_$.u1;
  var defineProp = kotlin_kotlin.$_$.j1;
  //endregion
  //region block: pre-declaration
  initMetadataForClass(PlayCard, 'PlayCard');
  initMetadataForClass(ChooseOption, 'ChooseOption');
  initMetadataForClass(GameStarted, 'GameStarted');
  initMetadataForClass(CardDealt, 'CardDealt');
  initMetadataForClass(TurnBegan, 'TurnBegan');
  initMetadataForClass(CardDrawn, 'CardDrawn');
  initMetadataForClass(HandOverflow, 'HandOverflow');
  initMetadataForClass(TrapTriggered, 'TrapTriggered');
  initMetadataForClass(TrapFizzled, 'TrapFizzled');
  initMetadataForClass(TurnSkipped, 'TurnSkipped');
  initMetadataForClass(CardPlayed, 'CardPlayed');
  initMetadataForClass(CardForbidden, 'CardForbidden');
  initMetadataForClass(ForbidSet, 'ForbidSet');
  initMetadataForClass(ForbidBroken, 'ForbidBroken');
  initMetadataForClass(CardRecovered, 'CardRecovered');
  initMetadataForClass(CardStolen, 'CardStolen');
  initMetadataForClass(TrapSet, 'TrapSet');
  initMetadataForClass(EffectFizzled, 'EffectFizzled');
  initMetadataForClass(ChoiceRequired, 'ChoiceRequired');
  initMetadataForClass(TurnEnded, 'TurnEnded');
  initMetadataForClass(GameEnded, 'GameEnded');
  initMetadataForClass(EngineResult, 'EngineResult');
  initMetadataForClass(GameEngine, 'GameEngine');
  initMetadataForCompanion(Companion);
  initMetadataForClass(Letter, 'Letter', VOID, Enum);
  initMetadataForClass(Side, 'Side', VOID, Enum);
  initMetadataForClass(SideState, 'SideState', SideState);
  initMetadataForClass(Traps, 'Traps', Traps);
  initMetadataForClass(Phase, 'Phase', VOID, Enum);
  initMetadataForClass(ChoiceKind, 'ChoiceKind', VOID, Enum);
  initMetadataForClass(ChoiceOption, 'ChoiceOption');
  initMetadataForClass(PendingChoice, 'PendingChoice');
  initMetadataForClass(EndReason, 'EndReason', VOID, Enum);
  initMetadataForClass(Outcome, 'Outcome');
  initMetadataForClass(GameState, 'GameState');
  initMetadataForClass(SeededRng, 'SeededRng');
  initMetadataForObject(CommandCodec, 'CommandCodec');
  initMetadataForObject(EventCodec, 'EventCodec');
  initMetadataForClass(MatchError, 'MatchError', VOID, Enum);
  initMetadataForCompanion(Companion_0);
  initMetadataForClass(MatchResult, 'MatchResult');
  initMetadataForObject(MatchService, 'MatchService');
  initMetadataForObject(Mirror, 'Mirror');
  initMetadataForObject(Redact, 'Redact');
  initMetadataForCompanion(Companion_1);
  initMetadataForClass(Seat, 'Seat', VOID, Enum);
  initMetadataForObject(StateCodec, 'StateCodec');
  initMetadataForObject(MatchFacade, 'MatchFacade');
  initMetadataForClass(JsMatchResult, 'JsMatchResult');
  //endregion
  function PlayCard(handIndex) {
    this.k4_1 = handIndex;
  }
  protoOf(PlayCard).toString = function () {
    return 'PlayCard(handIndex=' + this.k4_1 + ')';
  };
  protoOf(PlayCard).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof PlayCard))
      return false;
    if (!(this.k4_1 === other.k4_1))
      return false;
    return true;
  };
  function ChooseOption(optionIndex) {
    this.l4_1 = optionIndex;
  }
  protoOf(ChooseOption).toString = function () {
    return 'ChooseOption(optionIndex=' + this.l4_1 + ')';
  };
  protoOf(ChooseOption).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ChooseOption))
      return false;
    if (!(this.l4_1 === other.l4_1))
      return false;
    return true;
  };
  function GameStarted(firstPlayer, youRoll, aiRoll) {
    this.m4_1 = firstPlayer;
    this.n4_1 = youRoll;
    this.o4_1 = aiRoll;
  }
  protoOf(GameStarted).p4 = function (firstPlayer, youRoll, aiRoll) {
    return new GameStarted(firstPlayer, youRoll, aiRoll);
  };
  protoOf(GameStarted).toString = function () {
    return 'GameStarted(firstPlayer=' + this.m4_1.toString() + ', youRoll=' + this.n4_1 + ', aiRoll=' + this.o4_1 + ')';
  };
  protoOf(GameStarted).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof GameStarted))
      return false;
    if (!this.m4_1.equals(other.m4_1))
      return false;
    if (!(this.n4_1 === other.n4_1))
      return false;
    if (!(this.o4_1 === other.o4_1))
      return false;
    return true;
  };
  function CardDealt(side, letter) {
    this.q4_1 = side;
    this.r4_1 = letter;
  }
  protoOf(CardDealt).s4 = function (side, letter) {
    return new CardDealt(side, letter);
  };
  protoOf(CardDealt).t4 = function (side, letter, $super) {
    side = side === VOID ? this.q4_1 : side;
    letter = letter === VOID ? this.r4_1 : letter;
    return $super === VOID ? this.s4(side, letter) : $super.s4.call(this, side, letter);
  };
  protoOf(CardDealt).toString = function () {
    return 'CardDealt(side=' + this.q4_1.toString() + ', letter=' + this.r4_1.toString() + ')';
  };
  protoOf(CardDealt).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CardDealt))
      return false;
    if (!this.q4_1.equals(other.q4_1))
      return false;
    if (!this.r4_1.equals(other.r4_1))
      return false;
    return true;
  };
  function TurnBegan(side, turnNumber) {
    this.u4_1 = side;
    this.v4_1 = turnNumber;
  }
  protoOf(TurnBegan).w4 = function (side, turnNumber) {
    return new TurnBegan(side, turnNumber);
  };
  protoOf(TurnBegan).x4 = function (side, turnNumber, $super) {
    side = side === VOID ? this.u4_1 : side;
    turnNumber = turnNumber === VOID ? this.v4_1 : turnNumber;
    return $super === VOID ? this.w4(side, turnNumber) : $super.w4.call(this, side, turnNumber);
  };
  protoOf(TurnBegan).toString = function () {
    return 'TurnBegan(side=' + this.u4_1.toString() + ', turnNumber=' + this.v4_1 + ')';
  };
  protoOf(TurnBegan).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof TurnBegan))
      return false;
    if (!this.u4_1.equals(other.u4_1))
      return false;
    if (!(this.v4_1 === other.v4_1))
      return false;
    return true;
  };
  function CardDrawn(side, letter, deckLeft) {
    this.y4_1 = side;
    this.z4_1 = letter;
    this.a5_1 = deckLeft;
  }
  protoOf(CardDrawn).b5 = function (side, letter, deckLeft) {
    return new CardDrawn(side, letter, deckLeft);
  };
  protoOf(CardDrawn).c5 = function (side, letter, deckLeft, $super) {
    side = side === VOID ? this.y4_1 : side;
    letter = letter === VOID ? this.z4_1 : letter;
    deckLeft = deckLeft === VOID ? this.a5_1 : deckLeft;
    return $super === VOID ? this.b5(side, letter, deckLeft) : $super.b5.call(this, side, letter, deckLeft);
  };
  protoOf(CardDrawn).toString = function () {
    return 'CardDrawn(side=' + this.y4_1.toString() + ', letter=' + this.z4_1.toString() + ', deckLeft=' + this.a5_1 + ')';
  };
  protoOf(CardDrawn).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CardDrawn))
      return false;
    if (!this.y4_1.equals(other.y4_1))
      return false;
    if (!this.z4_1.equals(other.z4_1))
      return false;
    if (!(this.a5_1 === other.a5_1))
      return false;
    return true;
  };
  function HandOverflow(side, letter, handIndex) {
    this.d5_1 = side;
    this.e5_1 = letter;
    this.f5_1 = handIndex;
  }
  protoOf(HandOverflow).b5 = function (side, letter, handIndex) {
    return new HandOverflow(side, letter, handIndex);
  };
  protoOf(HandOverflow).g5 = function (side, letter, handIndex, $super) {
    side = side === VOID ? this.d5_1 : side;
    letter = letter === VOID ? this.e5_1 : letter;
    handIndex = handIndex === VOID ? this.f5_1 : handIndex;
    return $super === VOID ? this.b5(side, letter, handIndex) : $super.b5.call(this, side, letter, handIndex);
  };
  protoOf(HandOverflow).toString = function () {
    return 'HandOverflow(side=' + this.d5_1.toString() + ', letter=' + this.e5_1.toString() + ', handIndex=' + this.f5_1 + ')';
  };
  protoOf(HandOverflow).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof HandOverflow))
      return false;
    if (!this.d5_1.equals(other.d5_1))
      return false;
    if (!this.e5_1.equals(other.e5_1))
      return false;
    if (!(this.f5_1 === other.f5_1))
      return false;
    return true;
  };
  function TrapTriggered(side, letter, handIndex, trapsLeft) {
    this.h5_1 = side;
    this.i5_1 = letter;
    this.j5_1 = handIndex;
    this.k5_1 = trapsLeft;
  }
  protoOf(TrapTriggered).l5 = function (side, letter, handIndex, trapsLeft) {
    return new TrapTriggered(side, letter, handIndex, trapsLeft);
  };
  protoOf(TrapTriggered).m5 = function (side, letter, handIndex, trapsLeft, $super) {
    side = side === VOID ? this.h5_1 : side;
    letter = letter === VOID ? this.i5_1 : letter;
    handIndex = handIndex === VOID ? this.j5_1 : handIndex;
    trapsLeft = trapsLeft === VOID ? this.k5_1 : trapsLeft;
    return $super === VOID ? this.l5(side, letter, handIndex, trapsLeft) : $super.l5.call(this, side, letter, handIndex, trapsLeft);
  };
  protoOf(TrapTriggered).toString = function () {
    return 'TrapTriggered(side=' + this.h5_1.toString() + ', letter=' + this.i5_1.toString() + ', handIndex=' + this.j5_1 + ', trapsLeft=' + this.k5_1 + ')';
  };
  protoOf(TrapTriggered).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof TrapTriggered))
      return false;
    if (!this.h5_1.equals(other.h5_1))
      return false;
    if (!this.i5_1.equals(other.i5_1))
      return false;
    if (!(this.j5_1 === other.j5_1))
      return false;
    if (!(this.k5_1 === other.k5_1))
      return false;
    return true;
  };
  function TrapFizzled(side, trapsLeft) {
    this.n5_1 = side;
    this.o5_1 = trapsLeft;
  }
  protoOf(TrapFizzled).w4 = function (side, trapsLeft) {
    return new TrapFizzled(side, trapsLeft);
  };
  protoOf(TrapFizzled).p5 = function (side, trapsLeft, $super) {
    side = side === VOID ? this.n5_1 : side;
    trapsLeft = trapsLeft === VOID ? this.o5_1 : trapsLeft;
    return $super === VOID ? this.w4(side, trapsLeft) : $super.w4.call(this, side, trapsLeft);
  };
  protoOf(TrapFizzled).toString = function () {
    return 'TrapFizzled(side=' + this.n5_1.toString() + ', trapsLeft=' + this.o5_1 + ')';
  };
  protoOf(TrapFizzled).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof TrapFizzled))
      return false;
    if (!this.n5_1.equals(other.n5_1))
      return false;
    if (!(this.o5_1 === other.o5_1))
      return false;
    return true;
  };
  function TurnSkipped(side) {
    this.q5_1 = side;
  }
  protoOf(TurnSkipped).r5 = function (side) {
    return new TurnSkipped(side);
  };
  protoOf(TurnSkipped).toString = function () {
    return 'TurnSkipped(side=' + this.q5_1.toString() + ')';
  };
  protoOf(TurnSkipped).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof TurnSkipped))
      return false;
    if (!this.q5_1.equals(other.q5_1))
      return false;
    return true;
  };
  function CardPlayed(side, letter, handIndex) {
    this.s5_1 = side;
    this.t5_1 = letter;
    this.u5_1 = handIndex;
  }
  protoOf(CardPlayed).b5 = function (side, letter, handIndex) {
    return new CardPlayed(side, letter, handIndex);
  };
  protoOf(CardPlayed).v5 = function (side, letter, handIndex, $super) {
    side = side === VOID ? this.s5_1 : side;
    letter = letter === VOID ? this.t5_1 : letter;
    handIndex = handIndex === VOID ? this.u5_1 : handIndex;
    return $super === VOID ? this.b5(side, letter, handIndex) : $super.b5.call(this, side, letter, handIndex);
  };
  protoOf(CardPlayed).toString = function () {
    return 'CardPlayed(side=' + this.s5_1.toString() + ', letter=' + this.t5_1.toString() + ', handIndex=' + this.u5_1 + ')';
  };
  protoOf(CardPlayed).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CardPlayed))
      return false;
    if (!this.s5_1.equals(other.s5_1))
      return false;
    if (!this.t5_1.equals(other.t5_1))
      return false;
    if (!(this.u5_1 === other.u5_1))
      return false;
    return true;
  };
  function CardForbidden(side, letter, handIndex) {
    this.w5_1 = side;
    this.x5_1 = letter;
    this.y5_1 = handIndex;
  }
  protoOf(CardForbidden).b5 = function (side, letter, handIndex) {
    return new CardForbidden(side, letter, handIndex);
  };
  protoOf(CardForbidden).z5 = function (side, letter, handIndex, $super) {
    side = side === VOID ? this.w5_1 : side;
    letter = letter === VOID ? this.x5_1 : letter;
    handIndex = handIndex === VOID ? this.y5_1 : handIndex;
    return $super === VOID ? this.b5(side, letter, handIndex) : $super.b5.call(this, side, letter, handIndex);
  };
  protoOf(CardForbidden).toString = function () {
    return 'CardForbidden(side=' + this.w5_1.toString() + ', letter=' + this.x5_1.toString() + ', handIndex=' + this.y5_1 + ')';
  };
  protoOf(CardForbidden).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CardForbidden))
      return false;
    if (!this.w5_1.equals(other.w5_1))
      return false;
    if (!this.x5_1.equals(other.x5_1))
      return false;
    if (!(this.y5_1 === other.y5_1))
      return false;
    return true;
  };
  function ForbidSet(by, letter) {
    this.a6_1 = by;
    this.b6_1 = letter;
  }
  protoOf(ForbidSet).s4 = function (by, letter) {
    return new ForbidSet(by, letter);
  };
  protoOf(ForbidSet).c6 = function (by, letter, $super) {
    by = by === VOID ? this.a6_1 : by;
    letter = letter === VOID ? this.b6_1 : letter;
    return $super === VOID ? this.s4(by, letter) : $super.s4.call(this, by, letter);
  };
  protoOf(ForbidSet).toString = function () {
    return 'ForbidSet(by=' + this.a6_1.toString() + ', letter=' + this.b6_1.toString() + ')';
  };
  protoOf(ForbidSet).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ForbidSet))
      return false;
    if (!this.a6_1.equals(other.a6_1))
      return false;
    if (!this.b6_1.equals(other.b6_1))
      return false;
    return true;
  };
  function ForbidBroken(on, letter) {
    this.d6_1 = on;
    this.e6_1 = letter;
  }
  protoOf(ForbidBroken).s4 = function (on, letter) {
    return new ForbidBroken(on, letter);
  };
  protoOf(ForbidBroken).f6 = function (on, letter, $super) {
    on = on === VOID ? this.d6_1 : on;
    letter = letter === VOID ? this.e6_1 : letter;
    return $super === VOID ? this.s4(on, letter) : $super.s4.call(this, on, letter);
  };
  protoOf(ForbidBroken).toString = function () {
    return 'ForbidBroken(on=' + this.d6_1.toString() + ', letter=' + this.e6_1.toString() + ')';
  };
  protoOf(ForbidBroken).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ForbidBroken))
      return false;
    if (!this.d6_1.equals(other.d6_1))
      return false;
    if (!this.e6_1.equals(other.e6_1))
      return false;
    return true;
  };
  function CardRecovered(side, letter) {
    this.g6_1 = side;
    this.h6_1 = letter;
  }
  protoOf(CardRecovered).s4 = function (side, letter) {
    return new CardRecovered(side, letter);
  };
  protoOf(CardRecovered).i6 = function (side, letter, $super) {
    side = side === VOID ? this.g6_1 : side;
    letter = letter === VOID ? this.h6_1 : letter;
    return $super === VOID ? this.s4(side, letter) : $super.s4.call(this, side, letter);
  };
  protoOf(CardRecovered).toString = function () {
    return 'CardRecovered(side=' + this.g6_1.toString() + ', letter=' + this.h6_1.toString() + ')';
  };
  protoOf(CardRecovered).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CardRecovered))
      return false;
    if (!this.g6_1.equals(other.g6_1))
      return false;
    if (!this.h6_1.equals(other.h6_1))
      return false;
    return true;
  };
  function CardStolen(victim, letter, spaceIndex) {
    this.j6_1 = victim;
    this.k6_1 = letter;
    this.l6_1 = spaceIndex;
  }
  protoOf(CardStolen).b5 = function (victim, letter, spaceIndex) {
    return new CardStolen(victim, letter, spaceIndex);
  };
  protoOf(CardStolen).m6 = function (victim, letter, spaceIndex, $super) {
    victim = victim === VOID ? this.j6_1 : victim;
    letter = letter === VOID ? this.k6_1 : letter;
    spaceIndex = spaceIndex === VOID ? this.l6_1 : spaceIndex;
    return $super === VOID ? this.b5(victim, letter, spaceIndex) : $super.b5.call(this, victim, letter, spaceIndex);
  };
  protoOf(CardStolen).toString = function () {
    return 'CardStolen(victim=' + this.j6_1.toString() + ', letter=' + this.k6_1.toString() + ', spaceIndex=' + this.l6_1 + ')';
  };
  protoOf(CardStolen).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof CardStolen))
      return false;
    if (!this.j6_1.equals(other.j6_1))
      return false;
    if (!this.k6_1.equals(other.k6_1))
      return false;
    if (!(this.l6_1 === other.l6_1))
      return false;
    return true;
  };
  function TrapSet(on, trapCount) {
    this.n6_1 = on;
    this.o6_1 = trapCount;
  }
  protoOf(TrapSet).w4 = function (on, trapCount) {
    return new TrapSet(on, trapCount);
  };
  protoOf(TrapSet).p6 = function (on, trapCount, $super) {
    on = on === VOID ? this.n6_1 : on;
    trapCount = trapCount === VOID ? this.o6_1 : trapCount;
    return $super === VOID ? this.w4(on, trapCount) : $super.w4.call(this, on, trapCount);
  };
  protoOf(TrapSet).toString = function () {
    return 'TrapSet(on=' + this.n6_1.toString() + ', trapCount=' + this.o6_1 + ')';
  };
  protoOf(TrapSet).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof TrapSet))
      return false;
    if (!this.n6_1.equals(other.n6_1))
      return false;
    if (!(this.o6_1 === other.o6_1))
      return false;
    return true;
  };
  function EffectFizzled(side, letter) {
    this.q6_1 = side;
    this.r6_1 = letter;
  }
  protoOf(EffectFizzled).s4 = function (side, letter) {
    return new EffectFizzled(side, letter);
  };
  protoOf(EffectFizzled).s6 = function (side, letter, $super) {
    side = side === VOID ? this.q6_1 : side;
    letter = letter === VOID ? this.r6_1 : letter;
    return $super === VOID ? this.s4(side, letter) : $super.s4.call(this, side, letter);
  };
  protoOf(EffectFizzled).toString = function () {
    return 'EffectFizzled(side=' + this.q6_1.toString() + ', letter=' + this.r6_1.toString() + ')';
  };
  protoOf(EffectFizzled).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof EffectFizzled))
      return false;
    if (!this.q6_1.equals(other.q6_1))
      return false;
    if (!this.r6_1.equals(other.r6_1))
      return false;
    return true;
  };
  function ChoiceRequired(choice) {
    this.t6_1 = choice;
  }
  protoOf(ChoiceRequired).u6 = function (choice) {
    return new ChoiceRequired(choice);
  };
  protoOf(ChoiceRequired).toString = function () {
    return 'ChoiceRequired(choice=' + this.t6_1.toString() + ')';
  };
  protoOf(ChoiceRequired).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ChoiceRequired))
      return false;
    if (!this.t6_1.equals(other.t6_1))
      return false;
    return true;
  };
  function TurnEnded(side) {
    this.v6_1 = side;
  }
  protoOf(TurnEnded).r5 = function (side) {
    return new TurnEnded(side);
  };
  protoOf(TurnEnded).toString = function () {
    return 'TurnEnded(side=' + this.v6_1.toString() + ')';
  };
  protoOf(TurnEnded).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof TurnEnded))
      return false;
    if (!this.v6_1.equals(other.v6_1))
      return false;
    return true;
  };
  function GameEnded(outcome) {
    this.w6_1 = outcome;
  }
  protoOf(GameEnded).x6 = function (outcome) {
    return new GameEnded(outcome);
  };
  protoOf(GameEnded).toString = function () {
    return 'GameEnded(outcome=' + this.w6_1.toString() + ')';
  };
  protoOf(GameEnded).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof GameEnded))
      return false;
    if (!this.w6_1.equals(other.w6_1))
      return false;
    return true;
  };
  function EngineResult(state, events) {
    this.y6_1 = state;
    this.z6_1 = events;
  }
  protoOf(EngineResult).toString = function () {
    return 'EngineResult(state=' + this.y6_1.toString() + ', events=' + toString(this.z6_1) + ')';
  };
  protoOf(EngineResult).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof EngineResult))
      return false;
    if (!this.y6_1.equals(other.y6_1))
      return false;
    if (!equals(this.z6_1, other.z6_1))
      return false;
    return true;
  };
  function buildDeck($this) {
    // Inline function 'kotlin.collections.flatMap' call
    var tmp0 = Companion_getInstance().a7_1;
    // Inline function 'kotlin.collections.flatMapTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.e();
    while (_iterator__ex2g4s.f()) {
      var element = _iterator__ex2g4s.g();
      // Inline function 'kotlin.collections.List' call
      // Inline function 'kotlin.collections.MutableList' call
      var list = ArrayList_init_$Create$_0(10);
      // Inline function 'kotlin.repeat' call
      var inductionVariable = 0;
      if (inductionVariable < 10)
        do {
          var index = inductionVariable;
          inductionVariable = inductionVariable + 1 | 0;
          list.o(element);
        }
         while (inductionVariable < 10);
      var list_0 = list;
      addAll(destination, list_0);
    }
    return destination;
  }
  function playCard($this, state, handIndex) {
    if (!state.h7_1.equals(Phase_AWAITING_PLAY_getInstance()))
      return new EngineResult(state, emptyList());
    var side = state.d7_1;
    var hand = state.p7(side).m7_1;
    if (!(0 <= handIndex ? handIndex <= (hand.j() - 1 | 0) : false))
      return new EngineResult(state, emptyList());
    // Inline function 'kotlin.collections.mutableListOf' call
    var events = ArrayList_init_$Create$();
    var letter = hand.i(handIndex);
    var next = state.q7(side, GameEngine$playCard$lambda(handIndex));
    if (equals(next.g7_1.x7(side), letter)) {
      var tmp = next;
      next = tmp.q7(side, GameEngine$playCard$lambda_0(letter)).w7(VOID, VOID, VOID, VOID, VOID, next.g7_1.v7(side, null));
      // Inline function 'kotlin.collections.plusAssign' call
      var element = new CardForbidden(side, letter, handIndex);
      events.o(element);
      return new EngineResult(finishTurn($this, next, side, events), events);
    }
    var tmp_0 = next;
    next = tmp_0.q7(side, GameEngine$playCard$lambda_1(letter));
    // Inline function 'kotlin.collections.plusAssign' call
    var element_0 = new CardPlayed(side, letter, handIndex);
    events.o(element_0);
    var tmp0_safe_receiver = checkWin($this, next, side);
    if (tmp0_safe_receiver == null)
      null;
    else {
      // Inline function 'kotlin.let' call
      return new EngineResult(endGame($this, next, side, tmp0_safe_receiver, events), events);
    }
    next = resolveEffect($this, next, side, letter, events);
    if (next.y7() || next.h7_1.equals(Phase_AWAITING_CHOICE_getInstance())) {
      return new EngineResult(next, events);
    }
    return new EngineResult(finishTurn($this, next, side, events), events);
  }
  function resolveEffect($this, state, side, letter, events) {
    var tmp;
    switch (letter.v_1) {
      case 0:
        var tmp_0 = ChoiceKind_FORBID_LETTER_getInstance();
        // Inline function 'kotlin.collections.mapIndexed' call

        var this_0 = Companion_getInstance().a7_1;
        // Inline function 'kotlin.collections.mapIndexedTo' call

        var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
        var index = 0;
        var _iterator__ex2g4s = this_0.e();
        while (_iterator__ex2g4s.f()) {
          var item = _iterator__ex2g4s.g();
          var _unary__edvuaz = index;
          index = _unary__edvuaz + 1 | 0;
          var index_0 = checkIndexOverflow(_unary__edvuaz);
          var tmp$ret$0 = new ChoiceOption(index_0, item);
          destination.o(tmp$ret$0);
        }

        tmp = requireChoice($this, state, side, tmp_0, destination, events);
        break;
      case 1:
        tmp = draw($this, state, side, events);
        break;
      case 2:
        var discard = state.p7(side).o7_1;
        var tmp_1;
        if (discard.h()) {
          // Inline function 'kotlin.collections.plusAssign' call
          var element = new EffectFizzled(side, letter);
          events.o(element);
          tmp_1 = state;
        } else {
          // Inline function 'kotlin.collections.map' call
          var this_1 = Companion_getInstance().a7_1;
          // Inline function 'kotlin.collections.mapTo' call
          var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_1, 10));
          var _iterator__ex2g4s_0 = this_1.e();
          while (_iterator__ex2g4s_0.f()) {
            var item_0 = _iterator__ex2g4s_0.g();
            var tmp$ret$4 = to(item_0, discard.s(item_0));
            destination_0.o(tmp$ret$4);
          }
          // Inline function 'kotlin.collections.filter' call
          // Inline function 'kotlin.collections.filterTo' call
          var destination_1 = ArrayList_init_$Create$();
          var _iterator__ex2g4s_1 = destination_0.e();
          while (_iterator__ex2g4s_1.f()) {
            var element_0 = _iterator__ex2g4s_1.g();
            var index_1 = element_0.i4();
            if (index_1 >= 0) {
              destination_1.o(element_0);
            }
          }
          // Inline function 'kotlin.collections.map' call
          // Inline function 'kotlin.collections.mapTo' call
          var destination_2 = ArrayList_init_$Create$_0(collectionSizeOrDefault(destination_1, 10));
          var _iterator__ex2g4s_2 = destination_1.e();
          while (_iterator__ex2g4s_2.f()) {
            var item_1 = _iterator__ex2g4s_2.g();
            var l = item_1.h4();
            var index_2 = item_1.i4();
            var tmp$ret$10 = new ChoiceOption(index_2, l);
            destination_2.o(tmp$ret$10);
          }
          var options = destination_2;
          tmp_1 = requireChoice($this, state, side, ChoiceKind_RECOVER_LETTER_getInstance(), options, events);
        }

        tmp = tmp_1;
        break;
      case 3:
        var victimSpace = state.p7(side.b8()).n7_1;
        var tmp_2;
        if (victimSpace.h()) {
          // Inline function 'kotlin.collections.plusAssign' call
          var element_1 = new EffectFizzled(side, letter);
          events.o(element_1);
          tmp_2 = state;
        } else {
          // Inline function 'kotlin.collections.mapIndexed' call
          // Inline function 'kotlin.collections.mapIndexedTo' call
          var destination_3 = ArrayList_init_$Create$_0(collectionSizeOrDefault(victimSpace, 10));
          var index_3 = 0;
          var _iterator__ex2g4s_3 = victimSpace.e();
          while (_iterator__ex2g4s_3.f()) {
            var item_2 = _iterator__ex2g4s_3.g();
            var _unary__edvuaz_0 = index_3;
            index_3 = _unary__edvuaz_0 + 1 | 0;
            var index_4 = checkIndexOverflow(_unary__edvuaz_0);
            var tmp$ret$14 = new ChoiceOption(index_4, item_2);
            destination_3.o(tmp$ret$14);
          }
          var options_0 = destination_3;
          tmp_2 = requireChoice($this, state, side, ChoiceKind_STEAL_TARGET_getInstance(), options_0, events);
        }

        tmp = tmp_2;
        break;
      case 4:
        var count = state.g7_1.c8(side.b8()) + 1 | 0;
        // Inline function 'kotlin.collections.plusAssign' call

        var element_2 = new TrapSet(side.b8(), count);
        events.o(element_2);
        tmp = state.w7(VOID, VOID, VOID, VOID, VOID, state.g7_1.d8(side.b8(), count));
        break;
      default:
        noWhenBranchMatchedException();
        break;
    }
    return tmp;
  }
  function requireChoice($this, state, side, kind, options, events) {
    var choice = new PendingChoice(side, kind, options);
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new ChoiceRequired(choice);
    events.o(element);
    return state.w7(VOID, VOID, VOID, VOID, VOID, VOID, Phase_AWAITING_CHOICE_getInstance(), choice);
  }
  function chooseOption($this, state, optionIndex) {
    var tmp0_elvis_lhs = state.i7_1;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return new EngineResult(state, emptyList());
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var pending = tmp;
    if (!state.h7_1.equals(Phase_AWAITING_CHOICE_getInstance()))
      return new EngineResult(state, emptyList());
    if (!(0 <= optionIndex ? optionIndex <= (pending.g8_1.j() - 1 | 0) : false))
      return new EngineResult(state, emptyList());
    // Inline function 'kotlin.collections.mutableListOf' call
    var events = ArrayList_init_$Create$();
    var option = pending.g8_1.i(optionIndex);
    var side = pending.e8_1;
    var next = state.w7(VOID, VOID, VOID, VOID, VOID, VOID, Phase_AWAITING_PLAY_getInstance(), null);
    switch (pending.f8_1.v_1) {
      case 0:
        next = next.w7(VOID, VOID, VOID, VOID, VOID, next.g7_1.v7(side.b8(), option.i8_1));
        // Inline function 'kotlin.collections.plusAssign' call

        var element = new ForbidSet(side, option.i8_1);
        events.o(element);
        break;
      case 1:
        var tmp_0 = next;
        next = tmp_0.q7(side, GameEngine$chooseOption$lambda(option));
        // Inline function 'kotlin.collections.plusAssign' call

        var element_0 = new CardRecovered(side, option.i8_1);
        events.o(element_0);
        next = enforceHandLimit($this, next, side, events);
        break;
      case 2:
        var victim = side.b8();
        var tmp_1 = next;
        next = tmp_1.q7(victim, GameEngine$chooseOption$lambda_0(option));
        // Inline function 'kotlin.collections.plusAssign' call

        var element_1 = new CardStolen(victim, option.i8_1, option.h8_1);
        events.o(element_1);
        next = breakForbidWithoutCard($this, next, victim, events);
        break;
      case 3:
        var tmp_2 = next;
        next = tmp_2.q7(side, GameEngine$chooseOption$lambda_1(option));
        var left = coerceAtLeast(next.g7_1.c8(side) - 1 | 0, 0);
        next = next.w7(VOID, VOID, VOID, VOID, VOID, next.g7_1.d8(side, left));
        // Inline function 'kotlin.collections.plusAssign' call

        var element_2 = new TrapTriggered(side, option.i8_1, option.h8_1, left);
        events.o(element_2);
        return new EngineResult(continueAfterTrap($this, next, side, events), events);
      default:
        noWhenBranchMatchedException();
        break;
    }
    return new EngineResult(finishTurn($this, next, side, events), events);
  }
  function breakForbidWithoutCard($this, state, caster, events) {
    var tmp0 = state.p7(caster).n7_1;
    var tmp$ret$0;
    $l$block_0: {
      // Inline function 'kotlin.collections.any' call
      var tmp;
      if (isInterface(tmp0, Collection)) {
        tmp = tmp0.h();
      } else {
        tmp = false;
      }
      if (tmp) {
        tmp$ret$0 = false;
        break $l$block_0;
      }
      var _iterator__ex2g4s = tmp0.e();
      while (_iterator__ex2g4s.f()) {
        var element = _iterator__ex2g4s.g();
        if (element.equals(Letter_F_getInstance())) {
          tmp$ret$0 = true;
          break $l$block_0;
        }
      }
      tmp$ret$0 = false;
    }
    if (tmp$ret$0)
      return state;
    var victim = caster.b8();
    var tmp0_elvis_lhs = state.g7_1.x7(victim);
    var tmp_0;
    if (tmp0_elvis_lhs == null) {
      return state;
    } else {
      tmp_0 = tmp0_elvis_lhs;
    }
    var forbidden = tmp_0;
    // Inline function 'kotlin.collections.plusAssign' call
    var element_0 = new ForbidBroken(victim, forbidden);
    events.o(element_0);
    return state.w7(VOID, VOID, VOID, VOID, VOID, state.g7_1.v7(victim, null));
  }
  function beginTurn($this, state, side, events) {
    var tmp0_turnNumber = state.k7_1 + 1 | 0;
    var tmp1_phase = Phase_AWAITING_PLAY_getInstance();
    var next = state.w7(VOID, VOID, side, VOID, VOID, VOID, tmp1_phase, null, VOID, tmp0_turnNumber);
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new TurnBegan(side, next.k7_1);
    events.o(element);
    var skipDraw = !next.f7_1 && side.equals(next.e7_1);
    if (!skipDraw) {
      next = draw($this, next, side, events);
      if (next.y7())
        return next;
    }
    next = next.w7(VOID, VOID, VOID, VOID, true);
    var traps = next.g7_1.c8(side);
    if (traps > 0) {
      var hand = next.p7(side).m7_1;
      if (hand.h()) {
        var left = traps - 1 | 0;
        next = next.w7(VOID, VOID, VOID, VOID, VOID, next.g7_1.d8(side, left));
        // Inline function 'kotlin.collections.plusAssign' call
        var element_0 = new TrapFizzled(side, left);
        events.o(element_0);
      } else {
        // Inline function 'kotlin.collections.mapIndexed' call
        // Inline function 'kotlin.collections.mapIndexedTo' call
        var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(hand, 10));
        var index = 0;
        var _iterator__ex2g4s = hand.e();
        while (_iterator__ex2g4s.f()) {
          var item = _iterator__ex2g4s.g();
          var _unary__edvuaz = index;
          index = _unary__edvuaz + 1 | 0;
          var index_0 = checkIndexOverflow(_unary__edvuaz);
          var tmp$ret$2 = new ChoiceOption(index_0, item);
          destination.o(tmp$ret$2);
        }
        var options = destination;
        return requireChoice($this, next, side, ChoiceKind_TRAP_DISCARD_getInstance(), options, events);
      }
    }
    return continueAfterTrap($this, next, side, events);
  }
  function continueAfterTrap($this, state, side, events) {
    if (state.y7())
      return state;
    if (state.p7(side).m7_1.h()) {
      // Inline function 'kotlin.collections.plusAssign' call
      var element = new TurnSkipped(side);
      events.o(element);
      // Inline function 'kotlin.collections.plusAssign' call
      var element_0 = new TurnEnded(side);
      events.o(element_0);
      return beginTurn($this, state, side.b8(), events);
    }
    return state.w7(VOID, VOID, VOID, VOID, VOID, VOID, Phase_AWAITING_PLAY_getInstance(), null);
  }
  function finishTurn($this, state, side, events) {
    if (state.y7())
      return state;
    var tmp0_safe_receiver = checkWin($this, state, side);
    if (tmp0_safe_receiver == null)
      null;
    else {
      // Inline function 'kotlin.let' call
      return endGame($this, state, side, tmp0_safe_receiver, events);
    }
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new TurnEnded(side);
    events.o(element);
    return beginTurn($this, state, side.b8(), events);
  }
  function draw($this, state, side, events) {
    var deck = state.p7(side).l7_1;
    if (deck.h()) {
      return endGame($this, state, side.b8(), EndReason_DECK_OUT_getInstance(), events);
    }
    var letter = last(deck);
    var next = state.q7(side, GameEngine$draw$lambda(letter));
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new CardDrawn(side, letter, next.p7(side).l7_1.j());
    events.o(element);
    next = enforceHandLimit($this, next, side, events);
    return next;
  }
  function enforceHandLimit($this, state, side, events) {
    var next = state;
    while (next.p7(side).m7_1.j() > 7) {
      var overflowIndex = get_lastIndex(next.p7(side).m7_1);
      var overflow = next.p7(side).m7_1.i(overflowIndex);
      var tmp = next;
      next = tmp.q7(side, GameEngine$enforceHandLimit$lambda(overflow));
      // Inline function 'kotlin.collections.plusAssign' call
      var element = new HandOverflow(side, overflow, overflowIndex);
      events.o(element);
    }
    return next;
  }
  function checkWin($this, state, side) {
    var space = state.p7(side);
    return space.k8() === Companion_getInstance().a7_1.j() ? EndReason_FIRST_SET_getInstance() : space.j8() >= 5 ? EndReason_FIVE_OF_A_KIND_getInstance() : null;
  }
  function endGame($this, state, winner, reason, events) {
    var outcome = new Outcome(winner, reason);
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new GameEnded(outcome);
    events.o(element);
    return state.w7(VOID, VOID, VOID, VOID, VOID, VOID, Phase_GAME_OVER_getInstance(), null, outcome);
  }
  function GameEngine$startGame$lambda($letter) {
    return function (it) {
      return it.l8(dropLast(it.l7_1, 1), plus(it.m7_1, $letter));
    };
  }
  function GameEngine$playCard$lambda($handIndex) {
    return function (it) {
      return it.l8(VOID, removeAt(it.m7_1, $handIndex));
    };
  }
  function GameEngine$playCard$lambda_0($letter) {
    return function (it) {
      return it.l8(VOID, VOID, VOID, plus(it.o7_1, $letter));
    };
  }
  function GameEngine$playCard$lambda_1($letter) {
    return function (it) {
      return it.l8(VOID, VOID, plus(it.n7_1, $letter));
    };
  }
  function GameEngine$chooseOption$lambda($option) {
    return function (it) {
      var tmp0_discard = removeAt(it.o7_1, $option.h8_1);
      var tmp1_hand = plus(it.m7_1, $option.i8_1);
      return it.l8(VOID, tmp1_hand, VOID, tmp0_discard);
    };
  }
  function GameEngine$chooseOption$lambda_0($option) {
    return function (it) {
      return it.l8(VOID, VOID, removeAt(it.n7_1, $option.h8_1), plus(it.o7_1, $option.i8_1));
    };
  }
  function GameEngine$chooseOption$lambda_1($option) {
    return function (it) {
      return it.l8(VOID, removeAt(it.m7_1, $option.h8_1), VOID, plus(it.o7_1, $option.i8_1));
    };
  }
  function GameEngine$draw$lambda($letter) {
    return function (it) {
      return it.l8(dropLast(it.l7_1, 1), plus(it.m7_1, $letter));
    };
  }
  function GameEngine$enforceHandLimit$lambda($overflow) {
    return function (it) {
      return it.l8(VOID, dropLast(it.m7_1, 1), VOID, plus(it.o7_1, $overflow));
    };
  }
  function GameEngine(rng) {
    this.m8_1 = rng;
  }
  protoOf(GameEngine).n8 = function () {
    var youRoll = rollDie(this.m8_1);
    var aiRoll = rollDie(this.m8_1);
    while (youRoll === aiRoll) {
      youRoll = rollDie(this.m8_1);
      aiRoll = rollDie(this.m8_1);
    }
    var firstPlayer = youRoll > aiRoll ? Side_YOU_getInstance() : Side_AI_getInstance();
    return this.o8(shuffled(buildDeck(this), this.m8_1), shuffled(buildDeck(this), this.m8_1), firstPlayer, youRoll, aiRoll);
  };
  protoOf(GameEngine).o8 = function (youDeck, aiDeck, firstPlayer, youRoll, aiRoll) {
    // Inline function 'kotlin.collections.mutableListOf' call
    var events = ArrayList_init_$Create$();
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new GameStarted(firstPlayer, youRoll, aiRoll);
    events.o(element);
    var state = new GameState(new SideState(youDeck), new SideState(aiDeck), firstPlayer, firstPlayer, false);
    // Inline function 'kotlin.repeat' call
    var inductionVariable = 0;
    if (inductionVariable < 5)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var _iterator__ex2g4s = listOf([Side_YOU_getInstance(), Side_AI_getInstance()]).e();
        while (_iterator__ex2g4s.f()) {
          var side = _iterator__ex2g4s.g();
          var deck = state.p7(side).l7_1;
          // Inline function 'kotlin.collections.isNotEmpty' call
          if (!deck.h()) {
            var letter = last(deck);
            var tmp = state;
            state = tmp.q7(side, GameEngine$startGame$lambda(letter));
            // Inline function 'kotlin.collections.plusAssign' call
            var element_0 = new CardDealt(side, letter);
            events.o(element_0);
          }
        }
      }
       while (inductionVariable < 5);
    state = beginTurn(this, state, firstPlayer, events);
    return new EngineResult(state, events);
  };
  protoOf(GameEngine).p8 = function (state, command) {
    if (state.y7())
      return new EngineResult(state, emptyList());
    var tmp;
    if (command instanceof PlayCard) {
      tmp = playCard(this, state, command.k4_1);
    } else {
      if (command instanceof ChooseOption) {
        tmp = chooseOption(this, state, command.l4_1);
      } else {
        noWhenBranchMatchedException();
      }
    }
    return tmp;
  };
  function removeAt(_this__u8e3s4, index) {
    // Inline function 'kotlin.also' call
    var this_0 = toMutableList(_this__u8e3s4);
    this_0.w1(index);
    return this_0;
  }
  var Letter_F_instance;
  var Letter_I_instance;
  var Letter_R_instance;
  var Letter_S_instance;
  var Letter_T_instance;
  function Companion() {
    Companion_instance_0 = this;
    this.a7_1 = toList(get_entries());
  }
  var Companion_instance_0;
  function Companion_getInstance() {
    Letter_initEntries();
    if (Companion_instance_0 == null)
      new Companion();
    return Companion_instance_0;
  }
  function values() {
    return [Letter_F_getInstance(), Letter_I_getInstance(), Letter_R_getInstance(), Letter_S_getInstance(), Letter_T_getInstance()];
  }
  function valueOf(value) {
    switch (value) {
      case 'F':
        return Letter_F_getInstance();
      case 'I':
        return Letter_I_getInstance();
      case 'R':
        return Letter_R_getInstance();
      case 'S':
        return Letter_S_getInstance();
      case 'T':
        return Letter_T_getInstance();
      default:
        Letter_initEntries();
        THROW_IAE('No enum constant com.first.game.domain.Letter.' + value);
        break;
    }
  }
  function get_entries() {
    if ($ENTRIES == null)
      $ENTRIES = enumEntries(values());
    return $ENTRIES;
  }
  var Letter_entriesInitialized;
  function Letter_initEntries() {
    if (Letter_entriesInitialized)
      return Unit_instance;
    Letter_entriesInitialized = true;
    Letter_F_instance = new Letter('F', 0);
    Letter_I_instance = new Letter('I', 1);
    Letter_R_instance = new Letter('R', 2);
    Letter_S_instance = new Letter('S', 3);
    Letter_T_instance = new Letter('T', 4);
    Companion_getInstance();
  }
  var $ENTRIES;
  function Letter(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  var Side_YOU_instance;
  var Side_AI_instance;
  function valueOf_0(value) {
    switch (value) {
      case 'YOU':
        return Side_YOU_getInstance();
      case 'AI':
        return Side_AI_getInstance();
      default:
        Side_initEntries();
        THROW_IAE('No enum constant com.first.game.domain.Side.' + value);
        break;
    }
  }
  var Side_entriesInitialized;
  function Side_initEntries() {
    if (Side_entriesInitialized)
      return Unit_instance;
    Side_entriesInitialized = true;
    Side_YOU_instance = new Side('YOU', 0);
    Side_AI_instance = new Side('AI', 1);
  }
  function Side(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  protoOf(Side).b8 = function () {
    return this.equals(Side_YOU_getInstance()) ? Side_AI_getInstance() : Side_YOU_getInstance();
  };
  function SideState(deck, hand, space, discard) {
    deck = deck === VOID ? emptyList() : deck;
    hand = hand === VOID ? emptyList() : hand;
    space = space === VOID ? emptyList() : space;
    discard = discard === VOID ? emptyList() : discard;
    this.l7_1 = deck;
    this.m7_1 = hand;
    this.n7_1 = space;
    this.o7_1 = discard;
  }
  protoOf(SideState).q8 = function (letter) {
    var tmp0 = this.n7_1;
    var tmp$ret$0;
    $l$block: {
      // Inline function 'kotlin.collections.count' call
      var tmp;
      if (isInterface(tmp0, Collection)) {
        tmp = tmp0.h();
      } else {
        tmp = false;
      }
      if (tmp) {
        tmp$ret$0 = 0;
        break $l$block;
      }
      var count = 0;
      var _iterator__ex2g4s = tmp0.e();
      while (_iterator__ex2g4s.f()) {
        var element = _iterator__ex2g4s.g();
        if (element.equals(letter)) {
          count = count + 1 | 0;
          checkCountOverflow(count);
        }
      }
      tmp$ret$0 = count;
    }
    return tmp$ret$0;
  };
  protoOf(SideState).k8 = function () {
    var tmp0 = Companion_getInstance().a7_1;
    var tmp$ret$0;
    $l$block: {
      // Inline function 'kotlin.collections.count' call
      var tmp;
      if (isInterface(tmp0, Collection)) {
        tmp = tmp0.h();
      } else {
        tmp = false;
      }
      if (tmp) {
        tmp$ret$0 = 0;
        break $l$block;
      }
      var count = 0;
      var _iterator__ex2g4s = tmp0.e();
      while (_iterator__ex2g4s.f()) {
        var element = _iterator__ex2g4s.g();
        var tmp0_0 = this.n7_1;
        var tmp$ret$1;
        $l$block_1: {
          // Inline function 'kotlin.collections.any' call
          var tmp_0;
          if (isInterface(tmp0_0, Collection)) {
            tmp_0 = tmp0_0.h();
          } else {
            tmp_0 = false;
          }
          if (tmp_0) {
            tmp$ret$1 = false;
            break $l$block_1;
          }
          var _iterator__ex2g4s_0 = tmp0_0.e();
          while (_iterator__ex2g4s_0.f()) {
            var element_0 = _iterator__ex2g4s_0.g();
            if (element_0.equals(element)) {
              tmp$ret$1 = true;
              break $l$block_1;
            }
          }
          tmp$ret$1 = false;
        }
        if (tmp$ret$1) {
          count = count + 1 | 0;
          checkCountOverflow(count);
        }
      }
      tmp$ret$0 = count;
    }
    return tmp$ret$0;
  };
  protoOf(SideState).j8 = function () {
    // Inline function 'kotlin.collections.maxOf' call
    var iterator = Companion_getInstance().a7_1.e();
    if (!iterator.f())
      throw NoSuchElementException_init_$Create$();
    var l = iterator.g();
    var maxValue = this.q8(l);
    while (iterator.f()) {
      var l_0 = iterator.g();
      var v = this.q8(l_0);
      if (compareTo(maxValue, v) < 0) {
        maxValue = v;
      }
    }
    return maxValue;
  };
  protoOf(SideState).r8 = function (deck, hand, space, discard) {
    return new SideState(deck, hand, space, discard);
  };
  protoOf(SideState).l8 = function (deck, hand, space, discard, $super) {
    deck = deck === VOID ? this.l7_1 : deck;
    hand = hand === VOID ? this.m7_1 : hand;
    space = space === VOID ? this.n7_1 : space;
    discard = discard === VOID ? this.o7_1 : discard;
    return $super === VOID ? this.r8(deck, hand, space, discard) : $super.r8.call(this, deck, hand, space, discard);
  };
  protoOf(SideState).toString = function () {
    return 'SideState(deck=' + toString(this.l7_1) + ', hand=' + toString(this.m7_1) + ', space=' + toString(this.n7_1) + ', discard=' + toString(this.o7_1) + ')';
  };
  protoOf(SideState).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof SideState))
      return false;
    if (!equals(this.l7_1, other.l7_1))
      return false;
    if (!equals(this.m7_1, other.m7_1))
      return false;
    if (!equals(this.n7_1, other.n7_1))
      return false;
    if (!equals(this.o7_1, other.o7_1))
      return false;
    return true;
  };
  function Traps(forbidOnYou, forbidOnAi, trapsOnYou, trapsOnAi) {
    forbidOnYou = forbidOnYou === VOID ? null : forbidOnYou;
    forbidOnAi = forbidOnAi === VOID ? null : forbidOnAi;
    trapsOnYou = trapsOnYou === VOID ? 0 : trapsOnYou;
    trapsOnAi = trapsOnAi === VOID ? 0 : trapsOnAi;
    this.r7_1 = forbidOnYou;
    this.s7_1 = forbidOnAi;
    this.t7_1 = trapsOnYou;
    this.u7_1 = trapsOnAi;
  }
  protoOf(Traps).x7 = function (side) {
    return side.equals(Side_YOU_getInstance()) ? this.r7_1 : this.s7_1;
  };
  protoOf(Traps).v7 = function (side, letter) {
    return side.equals(Side_YOU_getInstance()) ? this.s8(letter) : this.s8(VOID, letter);
  };
  protoOf(Traps).c8 = function (side) {
    return side.equals(Side_YOU_getInstance()) ? this.t7_1 : this.u7_1;
  };
  protoOf(Traps).d8 = function (side, count) {
    return side.equals(Side_YOU_getInstance()) ? this.s8(VOID, VOID, count) : this.s8(VOID, VOID, VOID, count);
  };
  protoOf(Traps).t8 = function (forbidOnYou, forbidOnAi, trapsOnYou, trapsOnAi) {
    return new Traps(forbidOnYou, forbidOnAi, trapsOnYou, trapsOnAi);
  };
  protoOf(Traps).s8 = function (forbidOnYou, forbidOnAi, trapsOnYou, trapsOnAi, $super) {
    forbidOnYou = forbidOnYou === VOID ? this.r7_1 : forbidOnYou;
    forbidOnAi = forbidOnAi === VOID ? this.s7_1 : forbidOnAi;
    trapsOnYou = trapsOnYou === VOID ? this.t7_1 : trapsOnYou;
    trapsOnAi = trapsOnAi === VOID ? this.u7_1 : trapsOnAi;
    return $super === VOID ? this.t8(forbidOnYou, forbidOnAi, trapsOnYou, trapsOnAi) : $super.t8.call(this, forbidOnYou, forbidOnAi, trapsOnYou, trapsOnAi);
  };
  protoOf(Traps).toString = function () {
    return 'Traps(forbidOnYou=' + toString_0(this.r7_1) + ', forbidOnAi=' + toString_0(this.s7_1) + ', trapsOnYou=' + this.t7_1 + ', trapsOnAi=' + this.u7_1 + ')';
  };
  protoOf(Traps).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Traps))
      return false;
    if (!equals(this.r7_1, other.r7_1))
      return false;
    if (!equals(this.s7_1, other.s7_1))
      return false;
    if (!(this.t7_1 === other.t7_1))
      return false;
    if (!(this.u7_1 === other.u7_1))
      return false;
    return true;
  };
  var Phase_AWAITING_PLAY_instance;
  var Phase_AWAITING_CHOICE_instance;
  var Phase_GAME_OVER_instance;
  function valueOf_1(value) {
    switch (value) {
      case 'AWAITING_PLAY':
        return Phase_AWAITING_PLAY_getInstance();
      case 'AWAITING_CHOICE':
        return Phase_AWAITING_CHOICE_getInstance();
      case 'GAME_OVER':
        return Phase_GAME_OVER_getInstance();
      default:
        Phase_initEntries();
        THROW_IAE('No enum constant com.first.game.domain.Phase.' + value);
        break;
    }
  }
  var Phase_entriesInitialized;
  function Phase_initEntries() {
    if (Phase_entriesInitialized)
      return Unit_instance;
    Phase_entriesInitialized = true;
    Phase_AWAITING_PLAY_instance = new Phase('AWAITING_PLAY', 0);
    Phase_AWAITING_CHOICE_instance = new Phase('AWAITING_CHOICE', 1);
    Phase_GAME_OVER_instance = new Phase('GAME_OVER', 2);
  }
  function Phase(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  var ChoiceKind_FORBID_LETTER_instance;
  var ChoiceKind_RECOVER_LETTER_instance;
  var ChoiceKind_STEAL_TARGET_instance;
  var ChoiceKind_TRAP_DISCARD_instance;
  function valueOf_2(value) {
    switch (value) {
      case 'FORBID_LETTER':
        return ChoiceKind_FORBID_LETTER_getInstance();
      case 'RECOVER_LETTER':
        return ChoiceKind_RECOVER_LETTER_getInstance();
      case 'STEAL_TARGET':
        return ChoiceKind_STEAL_TARGET_getInstance();
      case 'TRAP_DISCARD':
        return ChoiceKind_TRAP_DISCARD_getInstance();
      default:
        ChoiceKind_initEntries();
        THROW_IAE('No enum constant com.first.game.domain.ChoiceKind.' + value);
        break;
    }
  }
  var ChoiceKind_entriesInitialized;
  function ChoiceKind_initEntries() {
    if (ChoiceKind_entriesInitialized)
      return Unit_instance;
    ChoiceKind_entriesInitialized = true;
    ChoiceKind_FORBID_LETTER_instance = new ChoiceKind('FORBID_LETTER', 0);
    ChoiceKind_RECOVER_LETTER_instance = new ChoiceKind('RECOVER_LETTER', 1);
    ChoiceKind_STEAL_TARGET_instance = new ChoiceKind('STEAL_TARGET', 2);
    ChoiceKind_TRAP_DISCARD_instance = new ChoiceKind('TRAP_DISCARD', 3);
  }
  function ChoiceKind(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function ChoiceOption(index, letter) {
    this.h8_1 = index;
    this.i8_1 = letter;
  }
  protoOf(ChoiceOption).toString = function () {
    return 'ChoiceOption(index=' + this.h8_1 + ', letter=' + this.i8_1.toString() + ')';
  };
  protoOf(ChoiceOption).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof ChoiceOption))
      return false;
    if (!(this.h8_1 === other.h8_1))
      return false;
    if (!this.i8_1.equals(other.i8_1))
      return false;
    return true;
  };
  function PendingChoice(side, kind, options) {
    this.e8_1 = side;
    this.f8_1 = kind;
    this.g8_1 = options;
  }
  protoOf(PendingChoice).u8 = function (side, kind, options) {
    return new PendingChoice(side, kind, options);
  };
  protoOf(PendingChoice).v8 = function (side, kind, options, $super) {
    side = side === VOID ? this.e8_1 : side;
    kind = kind === VOID ? this.f8_1 : kind;
    options = options === VOID ? this.g8_1 : options;
    return $super === VOID ? this.u8(side, kind, options) : $super.u8.call(this, side, kind, options);
  };
  protoOf(PendingChoice).toString = function () {
    return 'PendingChoice(side=' + this.e8_1.toString() + ', kind=' + this.f8_1.toString() + ', options=' + toString(this.g8_1) + ')';
  };
  protoOf(PendingChoice).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof PendingChoice))
      return false;
    if (!this.e8_1.equals(other.e8_1))
      return false;
    if (!this.f8_1.equals(other.f8_1))
      return false;
    if (!equals(this.g8_1, other.g8_1))
      return false;
    return true;
  };
  var EndReason_FIRST_SET_instance;
  var EndReason_FIVE_OF_A_KIND_instance;
  var EndReason_DECK_OUT_instance;
  function valueOf_3(value) {
    switch (value) {
      case 'FIRST_SET':
        return EndReason_FIRST_SET_getInstance();
      case 'FIVE_OF_A_KIND':
        return EndReason_FIVE_OF_A_KIND_getInstance();
      case 'DECK_OUT':
        return EndReason_DECK_OUT_getInstance();
      default:
        EndReason_initEntries();
        THROW_IAE('No enum constant com.first.game.domain.EndReason.' + value);
        break;
    }
  }
  var EndReason_entriesInitialized;
  function EndReason_initEntries() {
    if (EndReason_entriesInitialized)
      return Unit_instance;
    EndReason_entriesInitialized = true;
    EndReason_FIRST_SET_instance = new EndReason('FIRST_SET', 0);
    EndReason_FIVE_OF_A_KIND_instance = new EndReason('FIVE_OF_A_KIND', 1);
    EndReason_DECK_OUT_instance = new EndReason('DECK_OUT', 2);
  }
  function EndReason(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function Outcome(winner, reason) {
    this.w8_1 = winner;
    this.x8_1 = reason;
  }
  protoOf(Outcome).y8 = function (winner, reason) {
    return new Outcome(winner, reason);
  };
  protoOf(Outcome).z8 = function (winner, reason, $super) {
    winner = winner === VOID ? this.w8_1 : winner;
    reason = reason === VOID ? this.x8_1 : reason;
    return $super === VOID ? this.y8(winner, reason) : $super.y8.call(this, winner, reason);
  };
  protoOf(Outcome).toString = function () {
    return 'Outcome(winner=' + this.w8_1.toString() + ', reason=' + this.x8_1.toString() + ')';
  };
  protoOf(Outcome).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Outcome))
      return false;
    if (!this.w8_1.equals(other.w8_1))
      return false;
    if (!this.x8_1.equals(other.x8_1))
      return false;
    return true;
  };
  function GameState(you, ai, turn, firstPlayer, firstTurnDone, traps, phase, pending, outcome, turnNumber) {
    traps = traps === VOID ? new Traps() : traps;
    phase = phase === VOID ? Phase_AWAITING_PLAY_getInstance() : phase;
    pending = pending === VOID ? null : pending;
    outcome = outcome === VOID ? null : outcome;
    turnNumber = turnNumber === VOID ? 0 : turnNumber;
    this.b7_1 = you;
    this.c7_1 = ai;
    this.d7_1 = turn;
    this.e7_1 = firstPlayer;
    this.f7_1 = firstTurnDone;
    this.g7_1 = traps;
    this.h7_1 = phase;
    this.i7_1 = pending;
    this.j7_1 = outcome;
    this.k7_1 = turnNumber;
  }
  protoOf(GameState).p7 = function (s) {
    return s.equals(Side_YOU_getInstance()) ? this.b7_1 : this.c7_1;
  };
  protoOf(GameState).q7 = function (s, transform) {
    return s.equals(Side_YOU_getInstance()) ? this.w7(transform(this.b7_1)) : this.w7(VOID, transform(this.c7_1));
  };
  protoOf(GameState).y7 = function () {
    return this.h7_1.equals(Phase_GAME_OVER_getInstance());
  };
  protoOf(GameState).a9 = function () {
    var tmp0_safe_receiver = this.i7_1;
    var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.e8_1;
    return tmp1_elvis_lhs == null ? this.d7_1 : tmp1_elvis_lhs;
  };
  protoOf(GameState).b9 = function (you, ai, turn, firstPlayer, firstTurnDone, traps, phase, pending, outcome, turnNumber) {
    return new GameState(you, ai, turn, firstPlayer, firstTurnDone, traps, phase, pending, outcome, turnNumber);
  };
  protoOf(GameState).w7 = function (you, ai, turn, firstPlayer, firstTurnDone, traps, phase, pending, outcome, turnNumber, $super) {
    you = you === VOID ? this.b7_1 : you;
    ai = ai === VOID ? this.c7_1 : ai;
    turn = turn === VOID ? this.d7_1 : turn;
    firstPlayer = firstPlayer === VOID ? this.e7_1 : firstPlayer;
    firstTurnDone = firstTurnDone === VOID ? this.f7_1 : firstTurnDone;
    traps = traps === VOID ? this.g7_1 : traps;
    phase = phase === VOID ? this.h7_1 : phase;
    pending = pending === VOID ? this.i7_1 : pending;
    outcome = outcome === VOID ? this.j7_1 : outcome;
    turnNumber = turnNumber === VOID ? this.k7_1 : turnNumber;
    return $super === VOID ? this.b9(you, ai, turn, firstPlayer, firstTurnDone, traps, phase, pending, outcome, turnNumber) : $super.b9.call(this, you, ai, turn, firstPlayer, firstTurnDone, traps, phase, pending, outcome, turnNumber);
  };
  protoOf(GameState).toString = function () {
    return 'GameState(you=' + this.b7_1.toString() + ', ai=' + this.c7_1.toString() + ', turn=' + this.d7_1.toString() + ', firstPlayer=' + this.e7_1.toString() + ', firstTurnDone=' + this.f7_1 + ', traps=' + this.g7_1.toString() + ', phase=' + this.h7_1.toString() + ', pending=' + toString_0(this.i7_1) + ', outcome=' + toString_0(this.j7_1) + ', turnNumber=' + this.k7_1 + ')';
  };
  protoOf(GameState).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof GameState))
      return false;
    if (!this.b7_1.equals(other.b7_1))
      return false;
    if (!this.c7_1.equals(other.c7_1))
      return false;
    if (!this.d7_1.equals(other.d7_1))
      return false;
    if (!this.e7_1.equals(other.e7_1))
      return false;
    if (!(this.f7_1 === other.f7_1))
      return false;
    if (!this.g7_1.equals(other.g7_1))
      return false;
    if (!this.h7_1.equals(other.h7_1))
      return false;
    if (!equals(this.i7_1, other.i7_1))
      return false;
    if (!equals(this.j7_1, other.j7_1))
      return false;
    if (!(this.k7_1 === other.k7_1))
      return false;
    return true;
  };
  function Letter_F_getInstance() {
    Letter_initEntries();
    return Letter_F_instance;
  }
  function Letter_I_getInstance() {
    Letter_initEntries();
    return Letter_I_instance;
  }
  function Letter_R_getInstance() {
    Letter_initEntries();
    return Letter_R_instance;
  }
  function Letter_S_getInstance() {
    Letter_initEntries();
    return Letter_S_instance;
  }
  function Letter_T_getInstance() {
    Letter_initEntries();
    return Letter_T_instance;
  }
  function Side_YOU_getInstance() {
    Side_initEntries();
    return Side_YOU_instance;
  }
  function Side_AI_getInstance() {
    Side_initEntries();
    return Side_AI_instance;
  }
  function Phase_AWAITING_PLAY_getInstance() {
    Phase_initEntries();
    return Phase_AWAITING_PLAY_instance;
  }
  function Phase_AWAITING_CHOICE_getInstance() {
    Phase_initEntries();
    return Phase_AWAITING_CHOICE_instance;
  }
  function Phase_GAME_OVER_getInstance() {
    Phase_initEntries();
    return Phase_GAME_OVER_instance;
  }
  function ChoiceKind_FORBID_LETTER_getInstance() {
    ChoiceKind_initEntries();
    return ChoiceKind_FORBID_LETTER_instance;
  }
  function ChoiceKind_RECOVER_LETTER_getInstance() {
    ChoiceKind_initEntries();
    return ChoiceKind_RECOVER_LETTER_instance;
  }
  function ChoiceKind_STEAL_TARGET_getInstance() {
    ChoiceKind_initEntries();
    return ChoiceKind_STEAL_TARGET_instance;
  }
  function ChoiceKind_TRAP_DISCARD_getInstance() {
    ChoiceKind_initEntries();
    return ChoiceKind_TRAP_DISCARD_instance;
  }
  function EndReason_FIRST_SET_getInstance() {
    EndReason_initEntries();
    return EndReason_FIRST_SET_instance;
  }
  function EndReason_FIVE_OF_A_KIND_getInstance() {
    EndReason_initEntries();
    return EndReason_FIVE_OF_A_KIND_instance;
  }
  function EndReason_DECK_OUT_getInstance() {
    EndReason_initEntries();
    return EndReason_DECK_OUT_instance;
  }
  function SeededRng(seed) {
    this.c9_1 = Random(seed);
  }
  protoOf(SeededRng).z2 = function (bound) {
    return this.c9_1.z2(bound);
  };
  function rollDie(_this__u8e3s4) {
    return _this__u8e3s4.z2(6) + 1 | 0;
  }
  function shuffled(_this__u8e3s4, rng) {
    var result = toMutableList(_this__u8e3s4);
    var inductionVariable = get_lastIndex(result);
    if (1 <= inductionVariable)
      do {
        var i = inductionVariable;
        inductionVariable = inductionVariable + -1 | 0;
        var j = rng.z2(i + 1 | 0);
        var tmp = result.i(i);
        result.x1(i, result.i(j));
        result.x1(j, tmp);
      }
       while (1 <= inductionVariable);
    return result;
  }
  function CommandCodec() {
  }
  protoOf(CommandCodec).d9 = function (raw) {
    var parts = split(raw, charArrayOf([_Char___init__impl__6a9atx(59)]));
    // Inline function 'kotlin.require' call
    if (!(parts.j() >= 2)) {
      var message = '\u043A\u043E\u043C\u0430\u043D\u0434\u0430 \u0434\u043E\u043B\u0436\u043D\u0430 \u0431\u044B\u0442\u044C \u0432\u0438\u0434\u0430 kind;index';
      throw IllegalArgumentException_init_$Create$(toString(message));
    }
    var tmp0_elvis_lhs = toIntOrNull(parts.i(1));
    var tmp;
    if (tmp0_elvis_lhs == null) {
      throw IllegalArgumentException_init_$Create$('\u0438\u043D\u0434\u0435\u043A\u0441 \u043D\u0435 \u0447\u0438\u0441\u043B\u043E: ' + parts.i(1));
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var index = tmp;
    var tmp_0;
    switch (parts.i(0)) {
      case 'play':
        tmp_0 = new PlayCard(index);
        break;
      case 'choose':
        tmp_0 = new ChooseOption(index);
        break;
      default:
        throw IllegalArgumentException_init_$Create$('\u043D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043D\u0430\u044F \u043A\u043E\u043C\u0430\u043D\u0434\u0430: ' + parts.i(0));
    }
    return tmp_0;
  };
  protoOf(CommandCodec).e9 = function (raw) {
    // Inline function 'kotlin.runCatching' call
    var tmp;
    try {
      // Inline function 'kotlin.Companion.success' call
      var value = this.d9(raw);
      tmp = _Result___init__impl__xyqfz8(value);
    } catch ($p) {
      var tmp_0;
      if ($p instanceof Error) {
        var e = $p;
        // Inline function 'kotlin.Companion.failure' call
        tmp_0 = _Result___init__impl__xyqfz8(createFailure(e));
      } else {
        throw $p;
      }
      tmp = tmp_0;
    }
    // Inline function 'kotlin.Result.getOrNull' call
    var this_0 = tmp;
    var tmp_1;
    if (_Result___get_isFailure__impl__jpiriv(this_0)) {
      tmp_1 = null;
    } else {
      var tmp_2 = _Result___get_value__impl__bjfvqg(this_0);
      tmp_1 = (tmp_2 == null ? true : !(tmp_2 == null)) ? tmp_2 : THROW_CCE();
    }
    return tmp_1;
  };
  var CommandCodec_instance;
  function CommandCodec_getInstance() {
    return CommandCodec_instance;
  }
  function decodeOne$side(p, index) {
    return valueOf_0(p.i(index));
  }
  function decodeOne$letter(p, index) {
    return valueOf(p.i(index));
  }
  function decodeOne$int(p, index) {
    return toInt(p.i(index));
  }
  function EventCodec$encodeOne$ref(p0) {
    var l = function (_this__u8e3s4) {
      return p0.f9(_this__u8e3s4);
    };
    l.callableName = 'encodeOne';
    return l;
  }
  function EventCodec$encodeOne$lambda(it) {
    return '' + it.h8_1 + ':' + it.i8_1.toString();
  }
  function EventCodec() {
  }
  protoOf(EventCodec).g9 = function (events) {
    return joinToString(events, '\n', VOID, VOID, VOID, VOID, EventCodec$encodeOne$ref(this));
  };
  protoOf(EventCodec).d9 = function (raw) {
    var tmp;
    // Inline function 'kotlin.text.isEmpty' call
    if (charSequenceLength(raw) === 0) {
      tmp = emptyList();
    } else {
      // Inline function 'kotlin.collections.filter' call
      var tmp0 = split(raw, charArrayOf([_Char___init__impl__6a9atx(10)]));
      // Inline function 'kotlin.collections.filterTo' call
      var destination = ArrayList_init_$Create$();
      var _iterator__ex2g4s = tmp0.e();
      while (_iterator__ex2g4s.f()) {
        var element = _iterator__ex2g4s.g();
        // Inline function 'kotlin.text.isNotEmpty' call
        if (charSequenceLength(element) > 0) {
          destination.o(element);
        }
      }
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(destination, 10));
      var _iterator__ex2g4s_0 = destination.e();
      while (_iterator__ex2g4s_0.f()) {
        var item = _iterator__ex2g4s_0.g();
        var tmp$ret$5 = this.h9(item);
        destination_0.o(tmp$ret$5);
      }
      tmp = destination_0;
    }
    return tmp;
  };
  protoOf(EventCodec).e9 = function (raw) {
    // Inline function 'kotlin.runCatching' call
    var tmp;
    try {
      // Inline function 'kotlin.Companion.success' call
      var value = this.d9(raw);
      tmp = _Result___init__impl__xyqfz8(value);
    } catch ($p) {
      var tmp_0;
      if ($p instanceof Error) {
        var e = $p;
        // Inline function 'kotlin.Companion.failure' call
        tmp_0 = _Result___init__impl__xyqfz8(createFailure(e));
      } else {
        throw $p;
      }
      tmp = tmp_0;
    }
    // Inline function 'kotlin.Result.getOrNull' call
    var this_0 = tmp;
    var tmp_1;
    if (_Result___get_isFailure__impl__jpiriv(this_0)) {
      tmp_1 = null;
    } else {
      var tmp_2 = _Result___get_value__impl__bjfvqg(this_0);
      tmp_1 = (tmp_2 == null ? true : !(tmp_2 == null)) ? tmp_2 : THROW_CCE();
    }
    return tmp_1;
  };
  protoOf(EventCodec).f9 = function (event) {
    var tmp;
    if (event instanceof GameStarted) {
      tmp = 'START;' + event.m4_1.toString() + ';' + event.n4_1 + ';' + event.o4_1;
    } else {
      if (event instanceof CardDealt) {
        tmp = 'DEAL;' + event.q4_1.toString() + ';' + event.r4_1.toString();
      } else {
        if (event instanceof TurnBegan) {
          tmp = 'TURN;' + event.u4_1.toString() + ';' + event.v4_1;
        } else {
          if (event instanceof CardDrawn) {
            tmp = 'DRAW;' + event.y4_1.toString() + ';' + event.z4_1.toString() + ';' + event.a5_1;
          } else {
            if (event instanceof HandOverflow) {
              tmp = 'OVERFLOW;' + event.d5_1.toString() + ';' + event.e5_1.toString() + ';' + event.f5_1;
            } else {
              if (event instanceof TrapTriggered) {
                tmp = 'TRAPHIT;' + event.h5_1.toString() + ';' + event.i5_1.toString() + ';' + event.j5_1 + ';' + event.k5_1;
              } else {
                if (event instanceof TrapFizzled) {
                  tmp = 'TRAPMISS;' + event.n5_1.toString() + ';' + event.o5_1;
                } else {
                  if (event instanceof TurnSkipped) {
                    tmp = 'SKIP;' + event.q5_1.toString();
                  } else {
                    if (event instanceof CardPlayed) {
                      tmp = 'PLAY;' + event.s5_1.toString() + ';' + event.t5_1.toString() + ';' + event.u5_1;
                    } else {
                      if (event instanceof CardForbidden) {
                        tmp = 'BLOCKED;' + event.w5_1.toString() + ';' + event.x5_1.toString() + ';' + event.y5_1;
                      } else {
                        if (event instanceof ForbidSet) {
                          tmp = 'FORBID;' + event.a6_1.toString() + ';' + event.b6_1.toString();
                        } else {
                          if (event instanceof ForbidBroken) {
                            tmp = 'UNFORBID;' + event.d6_1.toString() + ';' + event.e6_1.toString();
                          } else {
                            if (event instanceof CardRecovered) {
                              tmp = 'RECOVER;' + event.g6_1.toString() + ';' + event.h6_1.toString();
                            } else {
                              if (event instanceof CardStolen) {
                                tmp = 'STEAL;' + event.j6_1.toString() + ';' + event.k6_1.toString() + ';' + event.l6_1;
                              } else {
                                if (event instanceof TrapSet) {
                                  tmp = 'TRAP;' + event.n6_1.toString() + ';' + event.o6_1;
                                } else {
                                  if (event instanceof EffectFizzled) {
                                    tmp = 'FIZZLE;' + event.q6_1.toString() + ';' + event.r6_1.toString();
                                  } else {
                                    if (event instanceof ChoiceRequired) {
                                      var options = joinToString(event.t6_1.g8_1, ',', VOID, VOID, VOID, VOID, EventCodec$encodeOne$lambda);
                                      tmp = 'CHOICE;' + event.t6_1.e8_1.toString() + ';' + event.t6_1.f8_1.toString() + ';' + options;
                                    } else {
                                      if (event instanceof TurnEnded) {
                                        tmp = 'ENDTURN;' + event.v6_1.toString();
                                      } else {
                                        if (event instanceof GameEnded) {
                                          tmp = 'END;' + event.w6_1.w8_1.toString() + ';' + event.w6_1.x8_1.toString();
                                        } else {
                                          noWhenBranchMatchedException();
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return tmp;
  };
  protoOf(EventCodec).h9 = function (line) {
    var p = split(line, charArrayOf([_Char___init__impl__6a9atx(59)]));
    var tmp;
    switch (p.i(0)) {
      case 'START':
        tmp = new GameStarted(decodeOne$side(p, 1), decodeOne$int(p, 2), decodeOne$int(p, 3));
        break;
      case 'DEAL':
        tmp = new CardDealt(decodeOne$side(p, 1), decodeOne$letter(p, 2));
        break;
      case 'TURN':
        tmp = new TurnBegan(decodeOne$side(p, 1), decodeOne$int(p, 2));
        break;
      case 'DRAW':
        tmp = new CardDrawn(decodeOne$side(p, 1), decodeOne$letter(p, 2), decodeOne$int(p, 3));
        break;
      case 'OVERFLOW':
        tmp = new HandOverflow(decodeOne$side(p, 1), decodeOne$letter(p, 2), decodeOne$int(p, 3));
        break;
      case 'TRAPHIT':
        tmp = new TrapTriggered(decodeOne$side(p, 1), decodeOne$letter(p, 2), decodeOne$int(p, 3), decodeOne$int(p, 4));
        break;
      case 'TRAPMISS':
        tmp = new TrapFizzled(decodeOne$side(p, 1), decodeOne$int(p, 2));
        break;
      case 'SKIP':
        tmp = new TurnSkipped(decodeOne$side(p, 1));
        break;
      case 'PLAY':
        tmp = new CardPlayed(decodeOne$side(p, 1), decodeOne$letter(p, 2), decodeOne$int(p, 3));
        break;
      case 'BLOCKED':
        tmp = new CardForbidden(decodeOne$side(p, 1), decodeOne$letter(p, 2), decodeOne$int(p, 3));
        break;
      case 'FORBID':
        tmp = new ForbidSet(decodeOne$side(p, 1), decodeOne$letter(p, 2));
        break;
      case 'UNFORBID':
        tmp = new ForbidBroken(decodeOne$side(p, 1), decodeOne$letter(p, 2));
        break;
      case 'RECOVER':
        tmp = new CardRecovered(decodeOne$side(p, 1), decodeOne$letter(p, 2));
        break;
      case 'STEAL':
        tmp = new CardStolen(decodeOne$side(p, 1), decodeOne$letter(p, 2), decodeOne$int(p, 3));
        break;
      case 'TRAP':
        tmp = new TrapSet(decodeOne$side(p, 1), decodeOne$int(p, 2));
        break;
      case 'FIZZLE':
        tmp = new EffectFizzled(decodeOne$side(p, 1), decodeOne$letter(p, 2));
        break;
      case 'CHOICE':
        var tmp_0 = decodeOne$side(p, 1);
        var tmp_1 = valueOf_2(p.i(2));
        // Inline function 'kotlin.collections.filter' call

        var tmp0 = split(p.i(3), charArrayOf([_Char___init__impl__6a9atx(44)]));
        // Inline function 'kotlin.collections.filterTo' call

        var destination = ArrayList_init_$Create$();
        var _iterator__ex2g4s = tmp0.e();
        while (_iterator__ex2g4s.f()) {
          var element = _iterator__ex2g4s.g();
          // Inline function 'kotlin.text.isNotEmpty' call
          if (charSequenceLength(element) > 0) {
            destination.o(element);
          }
        }

        // Inline function 'kotlin.collections.map' call

        // Inline function 'kotlin.collections.mapTo' call

        var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(destination, 10));
        var _iterator__ex2g4s_0 = destination.e();
        while (_iterator__ex2g4s_0.f()) {
          var item = _iterator__ex2g4s_0.g();
          var _destruct__k2r9zo = split(item, charArrayOf([_Char___init__impl__6a9atx(58)]));
          // Inline function 'kotlin.collections.component1' call
          var index = _destruct__k2r9zo.i(0);
          // Inline function 'kotlin.collections.component2' call
          var value = _destruct__k2r9zo.i(1);
          var tmp$ret$6 = new ChoiceOption(toInt(index), valueOf(value));
          destination_0.o(tmp$ret$6);
        }

        tmp = new ChoiceRequired(new PendingChoice(tmp_0, tmp_1, destination_0));
        break;
      case 'ENDTURN':
        tmp = new TurnEnded(decodeOne$side(p, 1));
        break;
      case 'END':
        tmp = new GameEnded(new Outcome(decodeOne$side(p, 1), valueOf_3(p.i(2))));
        break;
      default:
        throw IllegalArgumentException_init_$Create$('\u043D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043D\u043E\u0435 \u0441\u043E\u0431\u044B\u0442\u0438\u0435: ' + p.i(0));
    }
    return tmp;
  };
  var EventCodec_instance;
  function EventCodec_getInstance() {
    return EventCodec_instance;
  }
  var MatchError_BAD_STATE_instance;
  var MatchError_BAD_COMMAND_instance;
  var MatchError_MATCH_FINISHED_instance;
  var MatchError_NOT_YOUR_TURN_instance;
  var MatchError_ILLEGAL_COMMAND_instance;
  var MatchError_entriesInitialized;
  function MatchError_initEntries() {
    if (MatchError_entriesInitialized)
      return Unit_instance;
    MatchError_entriesInitialized = true;
    MatchError_BAD_STATE_instance = new MatchError('BAD_STATE', 0);
    MatchError_BAD_COMMAND_instance = new MatchError('BAD_COMMAND', 1);
    MatchError_MATCH_FINISHED_instance = new MatchError('MATCH_FINISHED', 2);
    MatchError_NOT_YOUR_TURN_instance = new MatchError('NOT_YOUR_TURN', 3);
    MatchError_ILLEGAL_COMMAND_instance = new MatchError('ILLEGAL_COMMAND', 4);
  }
  function MatchError(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  function Companion_0() {
  }
  protoOf(Companion_0).i9 = function (error, state) {
    return new MatchResult(false, error, state, '');
  };
  protoOf(Companion_0).j9 = function (error, state, $super) {
    state = state === VOID ? '' : state;
    return $super === VOID ? this.i9(error, state) : $super.i9.call(this, error, state);
  };
  var Companion_instance_1;
  function Companion_getInstance_0() {
    return Companion_instance_1;
  }
  function MatchResult(ok, error, state, events) {
    this.k9_1 = ok;
    this.l9_1 = error;
    this.m9_1 = state;
    this.n9_1 = events;
  }
  protoOf(MatchResult).toString = function () {
    return 'MatchResult(ok=' + this.k9_1 + ', error=' + toString_0(this.l9_1) + ', state=' + this.m9_1 + ', events=' + this.n9_1 + ')';
  };
  protoOf(MatchResult).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof MatchResult))
      return false;
    if (!(this.k9_1 === other.k9_1))
      return false;
    if (!equals(this.l9_1, other.l9_1))
      return false;
    if (!(this.m9_1 === other.m9_1))
      return false;
    if (!(this.n9_1 === other.n9_1))
      return false;
    return true;
  };
  function MatchService() {
    MatchService_instance = this;
    this.o9_1 = new Long(-2078137563, -873292572);
    this.p9_1 = new Long(435, 256);
  }
  protoOf(MatchService).q9 = function (seed) {
    var result = (new GameEngine(new SeededRng(this.r9(seed)))).n8();
    return new MatchResult(true, null, StateCodec_instance.u9(result.y6_1), EventCodec_instance.g9(result.z6_1));
  };
  protoOf(MatchService).v9 = function (stateRaw, seat, commandRaw, seed) {
    var tmp0_elvis_lhs = StateCodec_instance.e9(stateRaw);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return Companion_instance_1.j9(MatchError_BAD_STATE_getInstance());
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var state = tmp;
    if (state.y7())
      return Companion_instance_1.i9(MatchError_MATCH_FINISHED_getInstance(), stateRaw);
    var tmp1_elvis_lhs = CommandCodec_instance.e9(commandRaw);
    var tmp_0;
    if (tmp1_elvis_lhs == null) {
      return Companion_instance_1.i9(MatchError_BAD_COMMAND_getInstance(), stateRaw);
    } else {
      tmp_0 = tmp1_elvis_lhs;
    }
    var command = tmp_0;
    if (!state.a9().equals(seat.y9())) {
      return Companion_instance_1.i9(MatchError_NOT_YOUR_TURN_getInstance(), stateRaw);
    }
    var result = (new GameEngine(new SeededRng(this.r9(seed)))).p8(state, command);
    if (result.z6_1.h()) {
      return Companion_instance_1.i9(MatchError_ILLEGAL_COMMAND_getInstance(), stateRaw);
    }
    return new MatchResult(true, null, StateCodec_instance.u9(result.y6_1), EventCodec_instance.g9(result.z6_1));
  };
  protoOf(MatchService).z9 = function (stateRaw, seat) {
    var tmp0_safe_receiver = StateCodec_instance.e9(stateRaw);
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = Redact_getInstance().ca(Mirror_instance.aa(tmp0_safe_receiver, seat), Side_YOU_getInstance());
    }
    var tmp1_safe_receiver = tmp;
    var tmp_0;
    if (tmp1_safe_receiver == null) {
      tmp_0 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_0 = StateCodec_instance.u9(tmp1_safe_receiver);
    }
    return tmp_0;
  };
  protoOf(MatchService).da = function (eventsRaw, seat) {
    var tmp0_safe_receiver = EventCodec_instance.e9(eventsRaw);
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = Redact_getInstance().fa(Mirror_instance.ea(tmp0_safe_receiver, seat), Side_YOU_getInstance());
    }
    var tmp1_safe_receiver = tmp;
    var tmp_0;
    if (tmp1_safe_receiver == null) {
      tmp_0 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_0 = EventCodec_instance.g9(tmp1_safe_receiver);
    }
    return tmp_0;
  };
  protoOf(MatchService).ga = function (stateRaw) {
    var tmp0_safe_receiver = StateCodec_instance.e9(stateRaw);
    return (tmp0_safe_receiver == null ? null : tmp0_safe_receiver.y7()) === true;
  };
  protoOf(MatchService).ha = function (stateRaw) {
    var tmp0_safe_receiver = StateCodec_instance.e9(stateRaw);
    var tmp1_safe_receiver = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.j7_1;
    var tmp;
    if (tmp1_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = Companion_instance_2.ia(tmp1_safe_receiver.w8_1);
    }
    return tmp;
  };
  protoOf(MatchService).ja = function (stateRaw) {
    var tmp0_safe_receiver = StateCodec_instance.e9(stateRaw);
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.takeIf' call
      var tmp_0;
      if (!tmp0_safe_receiver.y7()) {
        tmp_0 = tmp0_safe_receiver;
      } else {
        tmp_0 = null;
      }
      tmp = tmp_0;
    }
    var tmp1_safe_receiver = tmp;
    var tmp_1;
    if (tmp1_safe_receiver == null) {
      tmp_1 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_1 = Companion_instance_2.ia(tmp1_safe_receiver.a9());
    }
    return tmp_1;
  };
  protoOf(MatchService).r9 = function (raw) {
    var tmp0_safe_receiver = toLongOrNull(raw);
    if (tmp0_safe_receiver == null)
      null;
    else {
      // Inline function 'kotlin.let' call
      return tmp0_safe_receiver;
    }
    var hash = new Long(-2078137563, -873292572);
    var inductionVariable = 0;
    var last = raw.length;
    while (inductionVariable < last) {
      var char = charCodeAt(raw, inductionVariable);
      inductionVariable = inductionVariable + 1 | 0;
      var tmp = hash;
      // Inline function 'kotlin.code' call
      var tmp$ret$1 = Char__toInt_impl_vasixd(char);
      hash = bitwiseXor(tmp, fromInt(tmp$ret$1));
      hash = multiply(hash, new Long(435, 256));
    }
    return hash;
  };
  var MatchService_instance;
  function MatchService_getInstance() {
    if (MatchService_instance == null)
      new MatchService();
    return MatchService_instance;
  }
  function MatchError_BAD_STATE_getInstance() {
    MatchError_initEntries();
    return MatchError_BAD_STATE_instance;
  }
  function MatchError_BAD_COMMAND_getInstance() {
    MatchError_initEntries();
    return MatchError_BAD_COMMAND_instance;
  }
  function MatchError_MATCH_FINISHED_getInstance() {
    MatchError_initEntries();
    return MatchError_MATCH_FINISHED_instance;
  }
  function MatchError_NOT_YOUR_TURN_getInstance() {
    MatchError_initEntries();
    return MatchError_NOT_YOUR_TURN_instance;
  }
  function MatchError_ILLEGAL_COMMAND_getInstance() {
    MatchError_initEntries();
    return MatchError_ILLEGAL_COMMAND_instance;
  }
  function Mirror() {
  }
  protoOf(Mirror).aa = function (state, seat) {
    return seat.equals(Seat_A_getInstance()) ? state : this.ka(state);
  };
  protoOf(Mirror).ea = function (events, seat) {
    var tmp;
    if (seat.equals(Seat_A_getInstance())) {
      tmp = events;
    } else {
      // Inline function 'kotlin.collections.map' call
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(events, 10));
      var _iterator__ex2g4s = events.e();
      while (_iterator__ex2g4s.f()) {
        var item = _iterator__ex2g4s.g();
        var tmp$ret$0 = this.la(item);
        destination.o(tmp$ret$0);
      }
      tmp = destination;
    }
    return tmp;
  };
  protoOf(Mirror).ka = function (state) {
    var tmp = state.d7_1.b8();
    var tmp_0 = state.e7_1.b8();
    var tmp_1 = new Traps(state.g7_1.s7_1, state.g7_1.r7_1, state.g7_1.u7_1, state.g7_1.t7_1);
    var tmp0_safe_receiver = state.i7_1;
    var tmp_2;
    if (tmp0_safe_receiver == null) {
      tmp_2 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_2 = tmp0_safe_receiver.v8(tmp0_safe_receiver.e8_1.b8());
    }
    var tmp_3 = tmp_2;
    var tmp1_safe_receiver = state.j7_1;
    var tmp_4;
    if (tmp1_safe_receiver == null) {
      tmp_4 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_4 = tmp1_safe_receiver.z8(tmp1_safe_receiver.w8_1.b8());
    }
    return new GameState(state.c7_1, state.b7_1, tmp, tmp_0, state.f7_1, tmp_1, state.h7_1, tmp_3, tmp_4, state.k7_1);
  };
  protoOf(Mirror).la = function (event) {
    var tmp;
    if (event instanceof GameStarted) {
      tmp = event.p4(event.m4_1.b8(), event.o4_1, event.n4_1);
    } else {
      if (event instanceof CardDealt) {
        tmp = event.t4(event.q4_1.b8());
      } else {
        if (event instanceof TurnBegan) {
          tmp = event.x4(event.u4_1.b8());
        } else {
          if (event instanceof CardDrawn) {
            tmp = event.c5(event.y4_1.b8());
          } else {
            if (event instanceof HandOverflow) {
              tmp = event.g5(event.d5_1.b8());
            } else {
              if (event instanceof TrapTriggered) {
                tmp = event.m5(event.h5_1.b8());
              } else {
                if (event instanceof TrapFizzled) {
                  tmp = event.p5(event.n5_1.b8());
                } else {
                  if (event instanceof TurnSkipped) {
                    tmp = event.r5(event.q5_1.b8());
                  } else {
                    if (event instanceof CardPlayed) {
                      tmp = event.v5(event.s5_1.b8());
                    } else {
                      if (event instanceof CardForbidden) {
                        tmp = event.z5(event.w5_1.b8());
                      } else {
                        if (event instanceof ForbidSet) {
                          tmp = event.c6(event.a6_1.b8());
                        } else {
                          if (event instanceof ForbidBroken) {
                            tmp = event.f6(event.d6_1.b8());
                          } else {
                            if (event instanceof CardRecovered) {
                              tmp = event.i6(event.g6_1.b8());
                            } else {
                              if (event instanceof CardStolen) {
                                tmp = event.m6(event.j6_1.b8());
                              } else {
                                if (event instanceof TrapSet) {
                                  tmp = event.p6(event.n6_1.b8());
                                } else {
                                  if (event instanceof EffectFizzled) {
                                    tmp = event.s6(event.q6_1.b8());
                                  } else {
                                    if (event instanceof ChoiceRequired) {
                                      tmp = event.u6(event.t6_1.v8(event.t6_1.e8_1.b8()));
                                    } else {
                                      if (event instanceof TurnEnded) {
                                        tmp = event.r5(event.v6_1.b8());
                                      } else {
                                        if (event instanceof GameEnded) {
                                          tmp = event.x6(event.w6_1.z8(event.w6_1.w8_1.b8()));
                                        } else {
                                          noWhenBranchMatchedException();
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return tmp;
  };
  var Mirror_instance;
  function Mirror_getInstance() {
    return Mirror_instance;
  }
  function redactEvent($this, event, viewer) {
    var tmp;
    var tmp_0;
    if (event instanceof CardDealt) {
      tmp_0 = !event.q4_1.equals(viewer);
    } else {
      tmp_0 = false;
    }
    if (tmp_0) {
      tmp = event.t4(VOID, $this.ba_1);
    } else {
      var tmp_1;
      if (event instanceof CardDrawn) {
        tmp_1 = !event.y4_1.equals(viewer);
      } else {
        tmp_1 = false;
      }
      if (tmp_1) {
        tmp = event.c5(VOID, $this.ba_1);
      } else {
        var tmp_2;
        if (event instanceof ChoiceRequired) {
          tmp_2 = !event.t6_1.e8_1.equals(viewer);
        } else {
          tmp_2 = false;
        }
        if (tmp_2) {
          tmp = event.u6(event.t6_1.v8(VOID, VOID, emptyList()));
        } else {
          tmp = event;
        }
      }
    }
    return tmp;
  }
  function hideDeck($this, _this__u8e3s4) {
    // Inline function 'kotlin.collections.List' call
    // Inline function 'kotlin.collections.MutableList' call
    var size = _this__u8e3s4.l7_1.j();
    var list = ArrayList_init_$Create$_0(size);
    // Inline function 'kotlin.repeat' call
    var inductionVariable = 0;
    if (inductionVariable < size)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var tmp$ret$0 = Redact_getInstance().ba_1;
        list.o(tmp$ret$0);
      }
       while (inductionVariable < size);
    return _this__u8e3s4.l8(list);
  }
  function hideHand($this, _this__u8e3s4) {
    // Inline function 'kotlin.collections.List' call
    // Inline function 'kotlin.collections.MutableList' call
    var size = _this__u8e3s4.m7_1.j();
    var list = ArrayList_init_$Create$_0(size);
    // Inline function 'kotlin.repeat' call
    var inductionVariable = 0;
    if (inductionVariable < size)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var tmp$ret$0 = Redact_getInstance().ba_1;
        list.o(tmp$ret$0);
      }
       while (inductionVariable < size);
    return _this__u8e3s4.l8(VOID, list);
  }
  function Redact$state$lambda($own) {
    return function (it) {
      return $own;
    };
  }
  function Redact$state$lambda_0($foe) {
    return function (it) {
      return $foe;
    };
  }
  function Redact() {
    Redact_instance = this;
    this.ba_1 = Letter_F_getInstance();
  }
  protoOf(Redact).ca = function (state, viewer) {
    var own = hideDeck(this, state.p7(viewer));
    var foe = hideHand(this, hideDeck(this, state.p7(viewer.b8())));
    var tmp0_safe_receiver = state.i7_1;
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = tmp0_safe_receiver.e8_1.equals(viewer) ? tmp0_safe_receiver : tmp0_safe_receiver.v8(VOID, VOID, emptyList());
    }
    var pending = tmp;
    var tmp_0 = state.q7(viewer, Redact$state$lambda(own));
    var tmp_1 = viewer.b8();
    return tmp_0.q7(tmp_1, Redact$state$lambda_0(foe)).w7(VOID, VOID, VOID, VOID, VOID, VOID, VOID, pending);
  };
  protoOf(Redact).fa = function (events, viewer) {
    // Inline function 'kotlin.collections.map' call
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(events, 10));
    var _iterator__ex2g4s = events.e();
    while (_iterator__ex2g4s.f()) {
      var item = _iterator__ex2g4s.g();
      var tmp$ret$0 = redactEvent(Redact_getInstance(), item, viewer);
      destination.o(tmp$ret$0);
    }
    return destination;
  };
  var Redact_instance;
  function Redact_getInstance() {
    if (Redact_instance == null)
      new Redact();
    return Redact_instance;
  }
  var Seat_A_instance;
  var Seat_B_instance;
  function Companion_1() {
  }
  protoOf(Companion_1).ia = function (side) {
    return side.equals(Side_YOU_getInstance()) ? Seat_A_getInstance() : Seat_B_getInstance();
  };
  protoOf(Companion_1).ma = function (raw) {
    var tmp0 = get_entries_0();
    var tmp$ret$1;
    $l$block: {
      // Inline function 'kotlin.collections.firstOrNull' call
      var _iterator__ex2g4s = tmp0.e();
      while (_iterator__ex2g4s.f()) {
        var element = _iterator__ex2g4s.g();
        if (element.u_1 === raw) {
          tmp$ret$1 = element;
          break $l$block;
        }
      }
      tmp$ret$1 = null;
    }
    return tmp$ret$1;
  };
  var Companion_instance_2;
  function Companion_getInstance_1() {
    return Companion_instance_2;
  }
  function values_0() {
    return [Seat_A_getInstance(), Seat_B_getInstance()];
  }
  function get_entries_0() {
    if ($ENTRIES_0 == null)
      $ENTRIES_0 = enumEntries(values_0());
    return $ENTRIES_0;
  }
  var Seat_entriesInitialized;
  function Seat_initEntries() {
    if (Seat_entriesInitialized)
      return Unit_instance;
    Seat_entriesInitialized = true;
    Seat_A_instance = new Seat('A', 0);
    Seat_B_instance = new Seat('B', 1);
  }
  var $ENTRIES_0;
  function Seat(name, ordinal) {
    Enum.call(this, name, ordinal);
  }
  protoOf(Seat).y9 = function () {
    return this.equals(Seat_A_getInstance()) ? Side_YOU_getInstance() : Side_AI_getInstance();
  };
  function Seat_A_getInstance() {
    Seat_initEntries();
    return Seat_A_instance;
  }
  function Seat_B_getInstance() {
    Seat_initEntries();
    return Seat_B_instance;
  }
  function encodeSide($this, side) {
    var tmp = listOf([side.l7_1, side.m7_1, side.n7_1, side.o7_1]);
    return joinToString(tmp, ';', VOID, VOID, VOID, VOID, StateCodec$encodeSide$lambda);
  }
  function decodeSide($this, line) {
    var piles = split(line, charArrayOf([_Char___init__impl__6a9atx(59)]));
    // Inline function 'kotlin.require' call
    if (!(piles.j() >= 4)) {
      var message = '\u0443 \u0441\u0442\u043E\u0440\u043E\u043D\u044B \u0434\u043E\u043B\u0436\u043D\u043E \u0431\u044B\u0442\u044C \u0447\u0435\u0442\u044B\u0440\u0435 \u0441\u0442\u043E\u043F\u043A\u0438';
      throw IllegalArgumentException_init_$Create$(toString(message));
    }
    // Inline function 'kotlin.collections.map' call
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(piles, 10));
    var _iterator__ex2g4s = piles.e();
    while (_iterator__ex2g4s.f()) {
      var item = _iterator__ex2g4s.g();
      var tmp$ret$2 = decodePile($this, item);
      destination.o(tmp$ret$2);
    }
    var decoded = destination;
    return new SideState(decoded.i(0), decoded.i(1), decoded.i(2), decoded.i(3));
  }
  function decodePile($this, pile) {
    var tmp;
    // Inline function 'kotlin.text.isEmpty' call
    if (charSequenceLength(pile) === 0) {
      tmp = emptyList();
    } else {
      // Inline function 'kotlin.collections.map' call
      var this_0 = split(pile, charArrayOf([_Char___init__impl__6a9atx(44)]));
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
      var _iterator__ex2g4s = this_0.e();
      while (_iterator__ex2g4s.f()) {
        var item = _iterator__ex2g4s.g();
        var tmp$ret$1 = valueOf(item);
        destination.o(tmp$ret$1);
      }
      tmp = destination;
    }
    return tmp;
  }
  function encodePending($this, pending) {
    if (pending == null)
      return '';
    var options = joinToString(pending.g8_1, ',', VOID, VOID, VOID, VOID, StateCodec$encodePending$lambda);
    return pending.e8_1.toString() + ';' + pending.f8_1.toString() + ';' + options;
  }
  function decodePending($this, line) {
    // Inline function 'kotlin.text.isEmpty' call
    if (charSequenceLength(line) === 0)
      return null;
    var parts = split(line, charArrayOf([_Char___init__impl__6a9atx(59)]));
    // Inline function 'kotlin.require' call
    if (!(parts.j() >= 3)) {
      var message = '\u0438\u0441\u043F\u043E\u0440\u0447\u0435\u043D\u043D\u0430\u044F \u0441\u0442\u0440\u043E\u043A\u0430 \u0432\u044B\u0431\u043E\u0440\u0430';
      throw IllegalArgumentException_init_$Create$(toString(message));
    }
    // Inline function 'kotlin.collections.filter' call
    var tmp0 = split(parts.i(2), charArrayOf([_Char___init__impl__6a9atx(44)]));
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s = tmp0.e();
    while (_iterator__ex2g4s.f()) {
      var element = _iterator__ex2g4s.g();
      // Inline function 'kotlin.text.isNotEmpty' call
      if (charSequenceLength(element) > 0) {
        destination.o(element);
      }
    }
    // Inline function 'kotlin.collections.map' call
    // Inline function 'kotlin.collections.mapTo' call
    var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(destination, 10));
    var _iterator__ex2g4s_0 = destination.e();
    while (_iterator__ex2g4s_0.f()) {
      var item = _iterator__ex2g4s_0.g();
      var _destruct__k2r9zo = split(item, charArrayOf([_Char___init__impl__6a9atx(58)]));
      // Inline function 'kotlin.collections.component1' call
      var index = _destruct__k2r9zo.i(0);
      // Inline function 'kotlin.collections.component2' call
      var letter = _destruct__k2r9zo.i(1);
      var tmp$ret$9 = new ChoiceOption(toInt(index), valueOf(letter));
      destination_0.o(tmp$ret$9);
    }
    var options = destination_0;
    return new PendingChoice(valueOf_0(parts.i(0)), valueOf_2(parts.i(1)), options);
  }
  function encodeOutcome($this, outcome) {
    return outcome == null ? '' : outcome.w8_1.toString() + ';' + outcome.x8_1.toString();
  }
  function decodeOutcome($this, line) {
    // Inline function 'kotlin.text.isEmpty' call
    if (charSequenceLength(line) === 0)
      return null;
    var parts = split(line, charArrayOf([_Char___init__impl__6a9atx(59)]));
    // Inline function 'kotlin.require' call
    if (!(parts.j() >= 2)) {
      var message = '\u0438\u0441\u043F\u043E\u0440\u0447\u0435\u043D\u043D\u0430\u044F \u0441\u0442\u0440\u043E\u043A\u0430 \u0438\u0441\u0445\u043E\u0434\u0430';
      throw IllegalArgumentException_init_$Create$(toString(message));
    }
    return new Outcome(valueOf_0(parts.i(0)), valueOf_3(parts.i(1)));
  }
  function StateCodec$encodeSide$lambda(pile) {
    return joinToString(pile, ',', VOID, VOID, VOID, VOID, StateCodec$encodeSide$lambda$lambda);
  }
  function StateCodec$encodeSide$lambda$lambda(it) {
    return it.u_1;
  }
  function StateCodec$encodePending$lambda(it) {
    return '' + it.h8_1 + ':' + it.i8_1.toString();
  }
  function StateCodec() {
    this.s9_1 = 2;
    this.t9_1 = 8;
  }
  protoOf(StateCodec).u9 = function (state) {
    // Inline function 'kotlin.text.buildString' call
    // Inline function 'kotlin.apply' call
    var this_0 = StringBuilder_init_$Create$();
    this_0.i2(2).f2(_Char___init__impl__6a9atx(10));
    this_0.j2(encodeSide(StateCodec_instance, state.b7_1)).f2(_Char___init__impl__6a9atx(10));
    this_0.j2(encodeSide(StateCodec_instance, state.c7_1)).f2(_Char___init__impl__6a9atx(10));
    this_0.g2(state.d7_1).f2(_Char___init__impl__6a9atx(59)).g2(state.e7_1).f2(_Char___init__impl__6a9atx(59)).h2(state.f7_1).f2(_Char___init__impl__6a9atx(59)).i2(state.k7_1).f2(_Char___init__impl__6a9atx(10));
    var tmp0_elvis_lhs = state.g7_1.r7_1;
    var tmp = this_0.g2(tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs).f2(_Char___init__impl__6a9atx(59));
    var tmp1_elvis_lhs = state.g7_1.s7_1;
    tmp.g2(tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs).f2(_Char___init__impl__6a9atx(59)).i2(state.g7_1.t7_1).f2(_Char___init__impl__6a9atx(59)).i2(state.g7_1.u7_1).f2(_Char___init__impl__6a9atx(10));
    this_0.g2(state.h7_1).f2(_Char___init__impl__6a9atx(10));
    this_0.j2(encodePending(StateCodec_instance, state.i7_1)).f2(_Char___init__impl__6a9atx(10));
    this_0.j2(encodeOutcome(StateCodec_instance, state.j7_1));
    return this_0.toString();
  };
  protoOf(StateCodec).d9 = function (raw) {
    return this.na(split(raw, charArrayOf([_Char___init__impl__6a9atx(10)])));
  };
  protoOf(StateCodec).e9 = function (raw) {
    // Inline function 'kotlin.runCatching' call
    var tmp;
    try {
      // Inline function 'kotlin.Companion.success' call
      var value = this.d9(raw);
      tmp = _Result___init__impl__xyqfz8(value);
    } catch ($p) {
      var tmp_0;
      if ($p instanceof Error) {
        var e = $p;
        // Inline function 'kotlin.Companion.failure' call
        tmp_0 = _Result___init__impl__xyqfz8(createFailure(e));
      } else {
        throw $p;
      }
      tmp = tmp_0;
    }
    // Inline function 'kotlin.Result.getOrNull' call
    var this_0 = tmp;
    var tmp_1;
    if (_Result___get_isFailure__impl__jpiriv(this_0)) {
      tmp_1 = null;
    } else {
      var tmp_2 = _Result___get_value__impl__bjfvqg(this_0);
      tmp_1 = (tmp_2 == null ? true : !(tmp_2 == null)) ? tmp_2 : THROW_CCE();
    }
    return tmp_1;
  };
  protoOf(StateCodec).na = function (lines) {
    // Inline function 'kotlin.require' call
    if (!(lines.j() >= 8)) {
      var message = '\u0441\u043E\u0441\u0442\u043E\u044F\u043D\u0438\u0435 \u0437\u0430\u043D\u0438\u043C\u0430\u0435\u0442 8 \u0441\u0442\u0440\u043E\u043A, \u0434\u0430\u043D\u043E ' + lines.j();
      throw IllegalArgumentException_init_$Create$(toString(message));
    }
    // Inline function 'kotlin.require' call
    if (!(toIntOrNull(lines.i(0)) === 2)) {
      var message_0 = '\u0447\u0443\u0436\u0430\u044F \u0432\u0435\u0440\u0441\u0438\u044F \u0441\u043E\u0441\u0442\u043E\u044F\u043D\u0438\u044F: ' + lines.i(0);
      throw IllegalArgumentException_init_$Create$(toString(message_0));
    }
    var head = split(lines.i(3), charArrayOf([_Char___init__impl__6a9atx(59)]));
    // Inline function 'kotlin.require' call
    if (!(head.j() >= 4)) {
      var message_1 = '\u0438\u0441\u043F\u043E\u0440\u0447\u0435\u043D\u043D\u0430\u044F \u0441\u0442\u0440\u043E\u043A\u0430 \u0445\u043E\u0434\u0430';
      throw IllegalArgumentException_init_$Create$(toString(message_1));
    }
    var traps = split(lines.i(4), charArrayOf([_Char___init__impl__6a9atx(59)]));
    // Inline function 'kotlin.require' call
    if (!(traps.j() >= 4)) {
      var message_2 = '\u0438\u0441\u043F\u043E\u0440\u0447\u0435\u043D\u043D\u0430\u044F \u0441\u0442\u0440\u043E\u043A\u0430 \u043B\u043E\u0432\u0443\u0448\u0435\u043A';
      throw IllegalArgumentException_init_$Create$(toString(message_2));
    }
    var tmp2_you = decodeSide(this, lines.i(1));
    var tmp3_ai = decodeSide(this, lines.i(2));
    var tmp4_turn = valueOf_0(head.i(0));
    var tmp5_firstPlayer = valueOf_0(head.i(1));
    var tmp6_firstTurnDone = toBooleanStrict(head.i(2));
    var tmp7_turnNumber = toInt(head.i(3));
    // Inline function 'kotlin.takeIf' call
    var this_0 = traps.i(0);
    var tmp;
    // Inline function 'kotlin.text.isNotEmpty' call
    if (charSequenceLength(this_0) > 0) {
      tmp = this_0;
    } else {
      tmp = null;
    }
    var tmp0_safe_receiver = tmp;
    var tmp_0;
    if (tmp0_safe_receiver == null) {
      tmp_0 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_0 = valueOf(tmp0_safe_receiver);
    }
    var tmp_1 = tmp_0;
    // Inline function 'kotlin.takeIf' call
    var this_1 = traps.i(1);
    var tmp_2;
    // Inline function 'kotlin.text.isNotEmpty' call
    if (charSequenceLength(this_1) > 0) {
      tmp_2 = this_1;
    } else {
      tmp_2 = null;
    }
    var tmp1_safe_receiver = tmp_2;
    var tmp_3;
    if (tmp1_safe_receiver == null) {
      tmp_3 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_3 = valueOf(tmp1_safe_receiver);
    }
    var tmp8_traps = new Traps(tmp_1, tmp_3, toInt(traps.i(2)), toInt(traps.i(3)));
    var tmp9_phase = valueOf_1(lines.i(5));
    var tmp10_pending = decodePending(this, lines.i(6));
    var tmp11_outcome = decodeOutcome(this, lines.i(7));
    return new GameState(tmp2_you, tmp3_ai, tmp4_turn, tmp5_firstPlayer, tmp6_firstTurnDone, tmp8_traps, tmp9_phase, tmp10_pending, tmp11_outcome, tmp7_turnNumber);
  };
  var StateCodec_instance;
  function StateCodec_getInstance() {
    return StateCodec_instance;
  }
  function MatchFacade() {
  }
  protoOf(MatchFacade).newMatch = function (seed) {
    return toJs(MatchService_getInstance().q9(seed));
  };
  protoOf(MatchFacade).apply = function (state, seat, command, seed) {
    var tmp0_elvis_lhs = Companion_instance_2.ma(seat);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return new JsMatchResult(false, 'BAD_SEAT', '', '');
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var parsed = tmp;
    return toJs(MatchService_getInstance().v9(state, parsed, command, seed));
  };
  protoOf(MatchFacade).viewFor = function (state, seat) {
    var tmp0_safe_receiver = Companion_instance_2.ma(seat);
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = MatchService_getInstance().z9(state, tmp0_safe_receiver);
    }
    return tmp;
  };
  protoOf(MatchFacade).eventsFor = function (events, seat) {
    var tmp0_safe_receiver = Companion_instance_2.ma(seat);
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = MatchService_getInstance().da(events, tmp0_safe_receiver);
    }
    return tmp;
  };
  protoOf(MatchFacade).isOver = function (state) {
    return MatchService_getInstance().ga(state);
  };
  protoOf(MatchFacade).winnerSeat = function (state) {
    var tmp0_safe_receiver = MatchService_getInstance().ha(state);
    return tmp0_safe_receiver == null ? null : tmp0_safe_receiver.u_1;
  };
  protoOf(MatchFacade).actingSeat = function (state) {
    var tmp0_safe_receiver = MatchService_getInstance().ja(state);
    return tmp0_safe_receiver == null ? null : tmp0_safe_receiver.u_1;
  };
  var MatchFacade_instance;
  function MatchFacade_getInstance() {
    return MatchFacade_instance;
  }
  function JsMatchResult(ok, error, state, events) {
    this.ok = ok;
    this.error = error;
    this.state = state;
    this.events = events;
  }
  protoOf(JsMatchResult).oa = function () {
    return this.ok;
  };
  protoOf(JsMatchResult).pa = function () {
    return this.error;
  };
  protoOf(JsMatchResult).qa = function () {
    return this.state;
  };
  protoOf(JsMatchResult).ra = function () {
    return this.events;
  };
  function toJs(_this__u8e3s4) {
    var tmp0_safe_receiver = _this__u8e3s4.l9_1;
    return new JsMatchResult(_this__u8e3s4.k9_1, tmp0_safe_receiver == null ? null : tmp0_safe_receiver.u_1, _this__u8e3s4.m9_1, _this__u8e3s4.n9_1);
  }
  //region block: init
  CommandCodec_instance = new CommandCodec();
  EventCodec_instance = new EventCodec();
  Companion_instance_1 = new Companion_0();
  Mirror_instance = new Mirror();
  Companion_instance_2 = new Companion_1();
  StateCodec_instance = new StateCodec();
  MatchFacade_instance = new MatchFacade();
  //endregion
  //region block: exports
  function $jsExportAll$(_) {
    var $com = _.com || (_.com = {});
    var $com$first = $com.first || ($com.first = {});
    var $com$first$game = $com$first.game || ($com$first.game = {});
    var $com$first$game$domain = $com$first$game.domain || ($com$first$game.domain = {});
    var $com$first$game$domain$js = $com$first$game$domain.js || ($com$first$game$domain.js = {});
    defineProp($com$first$game$domain$js, 'MatchFacade', MatchFacade_getInstance, VOID, true);
    $com$first$game$domain$js.JsMatchResult = JsMatchResult;
  }
  $jsExportAll$(_);
  //endregion
  return _;
}));

//# sourceMappingURL=first-game-rules.js.map
