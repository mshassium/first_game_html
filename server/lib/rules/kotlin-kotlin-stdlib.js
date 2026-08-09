//region block: polyfills
(function () {
  if (typeof globalThis === 'object')
    return;
  Object.defineProperty(Object.prototype, '__magic__', {get: function () {
    return this;
  }, configurable: true});
  __magic__.globalThis = __magic__;
  delete Object.prototype.__magic__;
}());
if (typeof Math.imul === 'undefined') {
  Math.imul = function imul(a, b) {
    return (a & 4.29490176E9) * (b & 65535) + (a & 65535) * (b | 0) | 0;
  };
}
if (typeof ArrayBuffer.isView === 'undefined') {
  ArrayBuffer.isView = function (a) {
    return a != null && a.__proto__ != null && a.__proto__.__proto__ === Int8Array.prototype.__proto__;
  };
}
if (typeof Math.clz32 === 'undefined') {
  Math.clz32 = function (log, LN2) {
    return function (x) {
      var asUint = x >>> 0;
      if (asUint === 0) {
        return 32;
      }
      return 31 - (log(asUint) / LN2 | 0) | 0; // the "| 0" acts like math.floor
    };
  }(Math.log, Math.LN2);
}
//endregion
(function (factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    globalThis['kotlin-kotlin-stdlib'] = factory(typeof globalThis['kotlin-kotlin-stdlib'] === 'undefined' ? {} : globalThis['kotlin-kotlin-stdlib']);
}(function (_) {
  'use strict';
  //region block: imports
  var imul = Math.imul;
  var isView = ArrayBuffer.isView;
  var clz32 = Math.clz32;
  //endregion
  //region block: pre-declaration
  initMetadataForInterface(CharSequence, 'CharSequence');
  initMetadataForClass(Number_0, 'Number');
  initMetadataForClass(asIterable$$inlined$Iterable$1);
  initMetadataForClass(Char, 'Char');
  initMetadataForInterface(Collection, 'Collection');
  initMetadataForInterface(KtList, 'List', VOID, VOID, [Collection]);
  initMetadataForCompanion(Companion);
  initMetadataForClass(Enum, 'Enum');
  initMetadataForCompanion(Companion_0);
  initMetadataForClass(Long, 'Long', VOID, Number_0);
  initMetadataForInterface(FunctionAdapter, 'FunctionAdapter');
  initMetadataForObject(Digit, 'Digit');
  initMetadataForInterface(Comparator, 'Comparator');
  initMetadataForObject(Unit, 'Unit');
  initMetadataForClass(AbstractCollection, 'AbstractCollection', VOID, VOID, [Collection]);
  initMetadataForClass(AbstractMutableCollection, 'AbstractMutableCollection', VOID, AbstractCollection, [AbstractCollection, Collection]);
  initMetadataForClass(IteratorImpl, 'IteratorImpl');
  initMetadataForClass(ListIteratorImpl, 'ListIteratorImpl', VOID, IteratorImpl);
  initMetadataForClass(AbstractMutableList, 'AbstractMutableList', VOID, AbstractMutableCollection, [AbstractMutableCollection, KtList, Collection]);
  initMetadataForCompanion(Companion_1);
  initMetadataForClass(ArrayList, 'ArrayList', ArrayList_init_$Create$, AbstractMutableList, [AbstractMutableList, KtList, Collection]);
  initMetadataForClass(Exception, 'Exception', Exception_init_$Create$, Error);
  initMetadataForClass(RuntimeException, 'RuntimeException', RuntimeException_init_$Create$, Exception);
  initMetadataForClass(IllegalArgumentException, 'IllegalArgumentException', IllegalArgumentException_init_$Create$, RuntimeException);
  initMetadataForClass(IllegalStateException, 'IllegalStateException', IllegalStateException_init_$Create$, RuntimeException);
  initMetadataForClass(UnsupportedOperationException, 'UnsupportedOperationException', UnsupportedOperationException_init_$Create$, RuntimeException);
  initMetadataForClass(NoSuchElementException, 'NoSuchElementException', NoSuchElementException_init_$Create$, RuntimeException);
  initMetadataForClass(IndexOutOfBoundsException, 'IndexOutOfBoundsException', IndexOutOfBoundsException_init_$Create$, RuntimeException);
  initMetadataForClass(ArithmeticException, 'ArithmeticException', ArithmeticException_init_$Create$, RuntimeException);
  initMetadataForClass(NumberFormatException, 'NumberFormatException', NumberFormatException_init_$Create$, IllegalArgumentException);
  initMetadataForClass(NoWhenBranchMatchedException, 'NoWhenBranchMatchedException', NoWhenBranchMatchedException_init_$Create$, RuntimeException);
  initMetadataForClass(ClassCastException, 'ClassCastException', ClassCastException_init_$Create$, RuntimeException);
  initMetadataForClass(StringBuilder, 'StringBuilder', StringBuilder_init_$Create$, VOID, [CharSequence]);
  initMetadataForClass(sam$kotlin_Comparator$0, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
  initMetadataForClass(IteratorImpl_0, 'IteratorImpl');
  initMetadataForClass(ListIteratorImpl_0, 'ListIteratorImpl', VOID, IteratorImpl_0);
  initMetadataForCompanion(Companion_2);
  initMetadataForClass(AbstractList, 'AbstractList', VOID, AbstractCollection, [AbstractCollection, KtList]);
  initMetadataForObject(EmptyList, 'EmptyList', VOID, VOID, [KtList]);
  initMetadataForObject(EmptyIterator, 'EmptyIterator');
  initMetadataForClass(IntIterator, 'IntIterator');
  initMetadataForClass(EnumEntriesList, 'EnumEntriesList', VOID, AbstractList, [KtList, AbstractList]);
  initMetadataForClass(Random, 'Random');
  initMetadataForObject(Default, 'Default', VOID, Random);
  initMetadataForCompanion(Companion_3);
  initMetadataForClass(XorWowRandom, 'XorWowRandom', VOID, Random);
  initMetadataForCompanion(Companion_4);
  initMetadataForClass(IntProgression, 'IntProgression');
  initMetadataForClass(IntRange, 'IntRange', VOID, IntProgression);
  initMetadataForClass(IntProgressionIterator, 'IntProgressionIterator', VOID, IntIterator);
  initMetadataForCompanion(Companion_5);
  initMetadataForClass(DelimitedRangesSequence$iterator$1);
  initMetadataForClass(DelimitedRangesSequence, 'DelimitedRangesSequence');
  initMetadataForCompanion(Companion_6);
  initMetadataForClass(Failure, 'Failure');
  initMetadataForClass(Pair, 'Pair');
  //endregion
  function CharSequence() {
  }
  function Number_0() {
  }
  function single(_this__u8e3s4) {
    var tmp;
    switch (_this__u8e3s4.length) {
      case 0:
        throw NoSuchElementException_init_$Create$_0('Array is empty.');
      case 1:
        tmp = _this__u8e3s4[0];
        break;
      default:
        throw IllegalArgumentException_init_$Create$_0('Array has more than one element.');
    }
    return tmp;
  }
  function lastIndexOf(_this__u8e3s4, element) {
    if (element == null) {
      var inductionVariable = _this__u8e3s4.length - 1 | 0;
      if (0 <= inductionVariable)
        do {
          var index = inductionVariable;
          inductionVariable = inductionVariable + -1 | 0;
          if (_this__u8e3s4[index] == null) {
            return index;
          }
        }
         while (0 <= inductionVariable);
    } else {
      var inductionVariable_0 = _this__u8e3s4.length - 1 | 0;
      if (0 <= inductionVariable_0)
        do {
          var index_0 = inductionVariable_0;
          inductionVariable_0 = inductionVariable_0 + -1 | 0;
          if (equals(element, _this__u8e3s4[index_0])) {
            return index_0;
          }
        }
         while (0 <= inductionVariable_0);
    }
    return -1;
  }
  function joinToString(_this__u8e3s4, separator, prefix, postfix, limit, truncated, transform) {
    separator = separator === VOID ? ', ' : separator;
    prefix = prefix === VOID ? '' : prefix;
    postfix = postfix === VOID ? '' : postfix;
    limit = limit === VOID ? -1 : limit;
    truncated = truncated === VOID ? '...' : truncated;
    transform = transform === VOID ? null : transform;
    return joinTo(_this__u8e3s4, StringBuilder_init_$Create$(), separator, prefix, postfix, limit, truncated, transform).toString();
  }
  function joinTo(_this__u8e3s4, buffer, separator, prefix, postfix, limit, truncated, transform) {
    separator = separator === VOID ? ', ' : separator;
    prefix = prefix === VOID ? '' : prefix;
    postfix = postfix === VOID ? '' : postfix;
    limit = limit === VOID ? -1 : limit;
    truncated = truncated === VOID ? '...' : truncated;
    transform = transform === VOID ? null : transform;
    buffer.d(prefix);
    var count = 0;
    var inductionVariable = 0;
    var last = _this__u8e3s4.length;
    $l$loop: while (inductionVariable < last) {
      var element = _this__u8e3s4[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      count = count + 1 | 0;
      if (count > 1) {
        buffer.d(separator);
      }
      if (limit < 0 || count <= limit) {
        appendElement(buffer, element, transform);
      } else
        break $l$loop;
    }
    if (limit >= 0 && count > limit) {
      buffer.d(truncated);
    }
    buffer.d(postfix);
    return buffer;
  }
  function getOrNull(_this__u8e3s4, index) {
    return (0 <= index ? index <= (_this__u8e3s4.length - 1 | 0) : false) ? _this__u8e3s4[index] : null;
  }
  function joinToString_0(_this__u8e3s4, separator, prefix, postfix, limit, truncated, transform) {
    separator = separator === VOID ? ', ' : separator;
    prefix = prefix === VOID ? '' : prefix;
    postfix = postfix === VOID ? '' : postfix;
    limit = limit === VOID ? -1 : limit;
    truncated = truncated === VOID ? '...' : truncated;
    transform = transform === VOID ? null : transform;
    return joinTo_0(_this__u8e3s4, StringBuilder_init_$Create$(), separator, prefix, postfix, limit, truncated, transform).toString();
  }
  function joinTo_0(_this__u8e3s4, buffer, separator, prefix, postfix, limit, truncated, transform) {
    separator = separator === VOID ? ', ' : separator;
    prefix = prefix === VOID ? '' : prefix;
    postfix = postfix === VOID ? '' : postfix;
    limit = limit === VOID ? -1 : limit;
    truncated = truncated === VOID ? '...' : truncated;
    transform = transform === VOID ? null : transform;
    buffer.d(prefix);
    var count = 0;
    var _iterator__ex2g4s = _this__u8e3s4.e();
    $l$loop: while (_iterator__ex2g4s.f()) {
      var element = _iterator__ex2g4s.g();
      count = count + 1 | 0;
      if (count > 1) {
        buffer.d(separator);
      }
      if (limit < 0 || count <= limit) {
        appendElement(buffer, element, transform);
      } else
        break $l$loop;
    }
    if (limit >= 0 && count > limit) {
      buffer.d(truncated);
    }
    buffer.d(postfix);
    return buffer;
  }
  function last(_this__u8e3s4) {
    if (_this__u8e3s4.h())
      throw NoSuchElementException_init_$Create$_0('List is empty.');
    return _this__u8e3s4.i(get_lastIndex(_this__u8e3s4));
  }
  function dropLast(_this__u8e3s4, n) {
    // Inline function 'kotlin.require' call
    if (!(n >= 0)) {
      var message = 'Requested element count ' + n + ' is less than zero.';
      throw IllegalArgumentException_init_$Create$_0(toString_1(message));
    }
    return take(_this__u8e3s4, coerceAtLeast(_this__u8e3s4.j() - n | 0, 0));
  }
  function plus(_this__u8e3s4, element) {
    var result = ArrayList_init_$Create$_0(_this__u8e3s4.j() + 1 | 0);
    result.n(_this__u8e3s4);
    result.o(element);
    return result;
  }
  function toMutableList(_this__u8e3s4) {
    return ArrayList_init_$Create$_1(_this__u8e3s4);
  }
  function toList(_this__u8e3s4) {
    if (isInterface(_this__u8e3s4, Collection)) {
      var tmp;
      switch (_this__u8e3s4.j()) {
        case 0:
          tmp = emptyList();
          break;
        case 1:
          var tmp_0;
          if (isInterface(_this__u8e3s4, KtList)) {
            tmp_0 = _this__u8e3s4.i(0);
          } else {
            tmp_0 = _this__u8e3s4.e().g();
          }

          tmp = listOf(tmp_0);
          break;
        default:
          tmp = toMutableList(_this__u8e3s4);
          break;
      }
      return tmp;
    }
    return optimizeReadOnlyList(toMutableList_0(_this__u8e3s4));
  }
  function first(_this__u8e3s4) {
    if (_this__u8e3s4.h())
      throw NoSuchElementException_init_$Create$_0('List is empty.');
    return _this__u8e3s4.i(0);
  }
  function take(_this__u8e3s4, n) {
    // Inline function 'kotlin.require' call
    if (!(n >= 0)) {
      var message = 'Requested element count ' + n + ' is less than zero.';
      throw IllegalArgumentException_init_$Create$_0(toString_1(message));
    }
    if (n === 0)
      return emptyList();
    if (isInterface(_this__u8e3s4, Collection)) {
      if (n >= _this__u8e3s4.j())
        return toList(_this__u8e3s4);
      if (n === 1)
        return listOf(first_0(_this__u8e3s4));
    }
    var count = 0;
    var list = ArrayList_init_$Create$_0(n);
    var _iterator__ex2g4s = _this__u8e3s4.e();
    $l$loop: while (_iterator__ex2g4s.f()) {
      var item = _iterator__ex2g4s.g();
      list.o(item);
      count = count + 1 | 0;
      if (count === n)
        break $l$loop;
    }
    return optimizeReadOnlyList(list);
  }
  function toMutableList_0(_this__u8e3s4) {
    if (isInterface(_this__u8e3s4, Collection))
      return toMutableList(_this__u8e3s4);
    return toCollection(_this__u8e3s4, ArrayList_init_$Create$());
  }
  function first_0(_this__u8e3s4) {
    if (isInterface(_this__u8e3s4, KtList))
      return first(_this__u8e3s4);
    else {
      var iterator = _this__u8e3s4.e();
      if (!iterator.f())
        throw NoSuchElementException_init_$Create$_0('Collection is empty.');
      return iterator.g();
    }
  }
  function toCollection(_this__u8e3s4, destination) {
    var _iterator__ex2g4s = _this__u8e3s4.e();
    while (_iterator__ex2g4s.f()) {
      var item = _iterator__ex2g4s.g();
      destination.o(item);
    }
    return destination;
  }
  function until(_this__u8e3s4, to) {
    if (to <= -2147483648)
      return Companion_getInstance_4().p_1;
    return numberRangeToNumber(_this__u8e3s4, to - 1 | 0);
  }
  function coerceAtLeast(_this__u8e3s4, minimumValue) {
    return _this__u8e3s4 < minimumValue ? minimumValue : _this__u8e3s4;
  }
  function downTo(_this__u8e3s4, to) {
    return Companion_instance_5.q(_this__u8e3s4, to, -1);
  }
  function coerceAtMost(_this__u8e3s4, maximumValue) {
    return _this__u8e3s4 > maximumValue ? maximumValue : _this__u8e3s4;
  }
  function coerceIn(_this__u8e3s4, minimumValue, maximumValue) {
    if (minimumValue > maximumValue)
      throw IllegalArgumentException_init_$Create$_0('Cannot coerce value to an empty range: maximum ' + maximumValue + ' is less than minimum ' + minimumValue + '.');
    if (_this__u8e3s4 < minimumValue)
      return minimumValue;
    if (_this__u8e3s4 > maximumValue)
      return maximumValue;
    return _this__u8e3s4;
  }
  function asIterable(_this__u8e3s4) {
    // Inline function 'kotlin.collections.Iterable' call
    return new asIterable$$inlined$Iterable$1(_this__u8e3s4);
  }
  function asIterable$$inlined$Iterable$1($this_asIterable) {
    this.r_1 = $this_asIterable;
  }
  protoOf(asIterable$$inlined$Iterable$1).e = function () {
    return this.r_1.e();
  };
  function _Char___init__impl__6a9atx(value) {
    return value;
  }
  function _get_value__a43j40($this) {
    return $this;
  }
  function Char__compareTo_impl_ypi4mb($this, other) {
    return _get_value__a43j40($this) - _get_value__a43j40(other) | 0;
  }
  function Char__minus_impl_a2frrh($this, other) {
    return _get_value__a43j40($this) - _get_value__a43j40(other) | 0;
  }
  function Char__toInt_impl_vasixd($this) {
    return _get_value__a43j40($this);
  }
  function toString($this) {
    // Inline function 'kotlin.js.unsafeCast' call
    return String.fromCharCode(_get_value__a43j40($this));
  }
  function Char() {
  }
  function KtList() {
  }
  function Collection() {
  }
  function Companion() {
  }
  var Companion_instance;
  function Companion_getInstance() {
    return Companion_instance;
  }
  function Enum(name, ordinal) {
    this.u_1 = name;
    this.v_1 = ordinal;
  }
  protoOf(Enum).w = function (other) {
    return compareTo(this.v_1, other.v_1);
  };
  protoOf(Enum).x = function (other) {
    return this.w(other instanceof Enum ? other : THROW_CCE());
  };
  protoOf(Enum).equals = function (other) {
    return this === other;
  };
  protoOf(Enum).toString = function () {
    return this.u_1;
  };
  function toString_0(_this__u8e3s4) {
    var tmp1_elvis_lhs = _this__u8e3s4 == null ? null : toString_1(_this__u8e3s4);
    return tmp1_elvis_lhs == null ? 'null' : tmp1_elvis_lhs;
  }
  function Companion_0() {
    Companion_instance_0 = this;
    this.y_1 = new Long(0, -2147483648);
    this.z_1 = new Long(-1, 2147483647);
    this.a1_1 = 8;
    this.b1_1 = 64;
  }
  var Companion_instance_0;
  function Companion_getInstance_0() {
    if (Companion_instance_0 == null)
      new Companion_0();
    return Companion_instance_0;
  }
  function Long(low, high) {
    Companion_getInstance_0();
    Number_0.call(this);
    this.c1_1 = low;
    this.d1_1 = high;
  }
  protoOf(Long).e1 = function (other) {
    return compare(this, other);
  };
  protoOf(Long).x = function (other) {
    return this.e1(other instanceof Long ? other : THROW_CCE());
  };
  protoOf(Long).toString = function () {
    return toStringImpl(this, 10);
  };
  protoOf(Long).equals = function (other) {
    var tmp;
    if (other instanceof Long) {
      tmp = equalsLong(this, other);
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(Long).valueOf = function () {
    return toNumber(this);
  };
  function implement(interfaces) {
    var maxSize = 1;
    var masks = [];
    var inductionVariable = 0;
    var last = interfaces.length;
    while (inductionVariable < last) {
      var i = interfaces[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      var currentSize = maxSize;
      var tmp0_elvis_lhs = i.prototype.$imask$;
      var imask = tmp0_elvis_lhs == null ? i.$imask$ : tmp0_elvis_lhs;
      if (!(imask == null)) {
        masks.push(imask);
        currentSize = imask.length;
      }
      var iid = i.$metadata$.iid;
      var tmp;
      if (iid == null) {
        tmp = null;
      } else {
        // Inline function 'kotlin.let' call
        tmp = bitMaskWith(iid);
      }
      var iidImask = tmp;
      if (!(iidImask == null)) {
        masks.push(iidImask);
        currentSize = Math.max(currentSize, iidImask.length);
      }
      if (currentSize > maxSize) {
        maxSize = currentSize;
      }
    }
    return compositeBitMask(maxSize, masks);
  }
  function bitMaskWith(activeBit) {
    var numberIndex = activeBit >> 5;
    var intArray = new Int32Array(numberIndex + 1 | 0);
    var positionInNumber = activeBit & 31;
    var numberWithSettledBit = 1 << positionInNumber;
    intArray[numberIndex] = intArray[numberIndex] | numberWithSettledBit;
    return intArray;
  }
  function compositeBitMask(capacity, masks) {
    var tmp = 0;
    var tmp_0 = new Int32Array(capacity);
    while (tmp < capacity) {
      var tmp_1 = tmp;
      var result = 0;
      var inductionVariable = 0;
      var last = masks.length;
      while (inductionVariable < last) {
        var mask = masks[inductionVariable];
        inductionVariable = inductionVariable + 1 | 0;
        if (tmp_1 < mask.length) {
          result = result | mask[tmp_1];
        }
      }
      tmp_0[tmp_1] = result;
      tmp = tmp + 1 | 0;
    }
    return tmp_0;
  }
  function isBitSet(_this__u8e3s4, possibleActiveBit) {
    var numberIndex = possibleActiveBit >> 5;
    if (numberIndex > _this__u8e3s4.length)
      return false;
    var positionInNumber = possibleActiveBit & 31;
    var numberWithSettledBit = 1 << positionInNumber;
    return !((_this__u8e3s4[numberIndex] & numberWithSettledBit) === 0);
  }
  function FunctionAdapter() {
  }
  function charArrayOf(arr) {
    var tmp0 = 'CharArray';
    // Inline function 'withType' call
    var array = new Uint16Array(arr);
    array.$type$ = tmp0;
    // Inline function 'kotlin.js.unsafeCast' call
    return array;
  }
  function get_ZERO() {
    _init_properties_boxedLong_kt__v24qrw();
    return ZERO;
  }
  var ZERO;
  function get_ONE() {
    _init_properties_boxedLong_kt__v24qrw();
    return ONE;
  }
  var ONE;
  function get_NEG_ONE() {
    _init_properties_boxedLong_kt__v24qrw();
    return NEG_ONE;
  }
  var NEG_ONE;
  function get_MAX_VALUE() {
    _init_properties_boxedLong_kt__v24qrw();
    return MAX_VALUE;
  }
  var MAX_VALUE;
  function get_MIN_VALUE() {
    _init_properties_boxedLong_kt__v24qrw();
    return MIN_VALUE;
  }
  var MIN_VALUE;
  function get_TWO_PWR_24_() {
    _init_properties_boxedLong_kt__v24qrw();
    return TWO_PWR_24_;
  }
  var TWO_PWR_24_;
  function compare(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    if (equalsLong(_this__u8e3s4, other)) {
      return 0;
    }
    var thisNeg = isNegative(_this__u8e3s4);
    var otherNeg = isNegative(other);
    return thisNeg && !otherNeg ? -1 : !thisNeg && otherNeg ? 1 : isNegative(subtract(_this__u8e3s4, other)) ? -1 : 1;
  }
  function add(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    var a48 = _this__u8e3s4.d1_1 >>> 16 | 0;
    var a32 = _this__u8e3s4.d1_1 & 65535;
    var a16 = _this__u8e3s4.c1_1 >>> 16 | 0;
    var a00 = _this__u8e3s4.c1_1 & 65535;
    var b48 = other.d1_1 >>> 16 | 0;
    var b32 = other.d1_1 & 65535;
    var b16 = other.c1_1 >>> 16 | 0;
    var b00 = other.c1_1 & 65535;
    var c48 = 0;
    var c32 = 0;
    var c16 = 0;
    var c00 = 0;
    c00 = c00 + (a00 + b00 | 0) | 0;
    c16 = c16 + (c00 >>> 16 | 0) | 0;
    c00 = c00 & 65535;
    c16 = c16 + (a16 + b16 | 0) | 0;
    c32 = c32 + (c16 >>> 16 | 0) | 0;
    c16 = c16 & 65535;
    c32 = c32 + (a32 + b32 | 0) | 0;
    c48 = c48 + (c32 >>> 16 | 0) | 0;
    c32 = c32 & 65535;
    c48 = c48 + (a48 + b48 | 0) | 0;
    c48 = c48 & 65535;
    return new Long(c16 << 16 | c00, c48 << 16 | c32);
  }
  function subtract(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    return add(_this__u8e3s4, negate(other));
  }
  function multiply(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    if (isZero(_this__u8e3s4)) {
      return get_ZERO();
    } else if (isZero(other)) {
      return get_ZERO();
    }
    if (equalsLong(_this__u8e3s4, get_MIN_VALUE())) {
      return isOdd(other) ? get_MIN_VALUE() : get_ZERO();
    } else if (equalsLong(other, get_MIN_VALUE())) {
      return isOdd(_this__u8e3s4) ? get_MIN_VALUE() : get_ZERO();
    }
    if (isNegative(_this__u8e3s4)) {
      var tmp;
      if (isNegative(other)) {
        tmp = multiply(negate(_this__u8e3s4), negate(other));
      } else {
        tmp = negate(multiply(negate(_this__u8e3s4), other));
      }
      return tmp;
    } else if (isNegative(other)) {
      return negate(multiply(_this__u8e3s4, negate(other)));
    }
    if (lessThan(_this__u8e3s4, get_TWO_PWR_24_()) && lessThan(other, get_TWO_PWR_24_())) {
      return fromNumber(toNumber(_this__u8e3s4) * toNumber(other));
    }
    var a48 = _this__u8e3s4.d1_1 >>> 16 | 0;
    var a32 = _this__u8e3s4.d1_1 & 65535;
    var a16 = _this__u8e3s4.c1_1 >>> 16 | 0;
    var a00 = _this__u8e3s4.c1_1 & 65535;
    var b48 = other.d1_1 >>> 16 | 0;
    var b32 = other.d1_1 & 65535;
    var b16 = other.c1_1 >>> 16 | 0;
    var b00 = other.c1_1 & 65535;
    var c48 = 0;
    var c32 = 0;
    var c16 = 0;
    var c00 = 0;
    c00 = c00 + imul(a00, b00) | 0;
    c16 = c16 + (c00 >>> 16 | 0) | 0;
    c00 = c00 & 65535;
    c16 = c16 + imul(a16, b00) | 0;
    c32 = c32 + (c16 >>> 16 | 0) | 0;
    c16 = c16 & 65535;
    c16 = c16 + imul(a00, b16) | 0;
    c32 = c32 + (c16 >>> 16 | 0) | 0;
    c16 = c16 & 65535;
    c32 = c32 + imul(a32, b00) | 0;
    c48 = c48 + (c32 >>> 16 | 0) | 0;
    c32 = c32 & 65535;
    c32 = c32 + imul(a16, b16) | 0;
    c48 = c48 + (c32 >>> 16 | 0) | 0;
    c32 = c32 & 65535;
    c32 = c32 + imul(a00, b32) | 0;
    c48 = c48 + (c32 >>> 16 | 0) | 0;
    c32 = c32 & 65535;
    c48 = c48 + (((imul(a48, b00) + imul(a32, b16) | 0) + imul(a16, b32) | 0) + imul(a00, b48) | 0) | 0;
    c48 = c48 & 65535;
    return new Long(c16 << 16 | c00, c48 << 16 | c32);
  }
  function divide(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    if (isZero(other)) {
      throw Exception_init_$Create$_0('division by zero');
    } else if (isZero(_this__u8e3s4)) {
      return get_ZERO();
    }
    if (equalsLong(_this__u8e3s4, get_MIN_VALUE())) {
      if (equalsLong(other, get_ONE()) || equalsLong(other, get_NEG_ONE())) {
        return get_MIN_VALUE();
      } else if (equalsLong(other, get_MIN_VALUE())) {
        return get_ONE();
      } else {
        var halfThis = shiftRight(_this__u8e3s4, 1);
        var approx = shiftLeft(divide(halfThis, other), 1);
        if (equalsLong(approx, get_ZERO())) {
          return isNegative(other) ? get_ONE() : get_NEG_ONE();
        } else {
          var rem = subtract(_this__u8e3s4, multiply(other, approx));
          return add(approx, divide(rem, other));
        }
      }
    } else if (equalsLong(other, get_MIN_VALUE())) {
      return get_ZERO();
    }
    if (isNegative(_this__u8e3s4)) {
      var tmp;
      if (isNegative(other)) {
        tmp = divide(negate(_this__u8e3s4), negate(other));
      } else {
        tmp = negate(divide(negate(_this__u8e3s4), other));
      }
      return tmp;
    } else if (isNegative(other)) {
      return negate(divide(_this__u8e3s4, negate(other)));
    }
    var res = get_ZERO();
    var rem_0 = _this__u8e3s4;
    while (greaterThanOrEqual(rem_0, other)) {
      var approxDouble = toNumber(rem_0) / toNumber(other);
      var approx2 = Math.max(1.0, Math.floor(approxDouble));
      var log2 = Math.ceil(Math.log(approx2) / Math.LN2);
      var delta = log2 <= 48 ? 1.0 : Math.pow(2.0, log2 - 48);
      var approxRes = fromNumber(approx2);
      var approxRem = multiply(approxRes, other);
      while (isNegative(approxRem) || greaterThan(approxRem, rem_0)) {
        approx2 = approx2 - delta;
        approxRes = fromNumber(approx2);
        approxRem = multiply(approxRes, other);
      }
      if (isZero(approxRes)) {
        approxRes = get_ONE();
      }
      res = add(res, approxRes);
      rem_0 = subtract(rem_0, approxRem);
    }
    return res;
  }
  function shiftLeft(_this__u8e3s4, numBits) {
    _init_properties_boxedLong_kt__v24qrw();
    var numBits_0 = numBits & 63;
    if (numBits_0 === 0) {
      return _this__u8e3s4;
    } else {
      if (numBits_0 < 32) {
        return new Long(_this__u8e3s4.c1_1 << numBits_0, _this__u8e3s4.d1_1 << numBits_0 | (_this__u8e3s4.c1_1 >>> (32 - numBits_0 | 0) | 0));
      } else {
        return new Long(0, _this__u8e3s4.c1_1 << (numBits_0 - 32 | 0));
      }
    }
  }
  function shiftRight(_this__u8e3s4, numBits) {
    _init_properties_boxedLong_kt__v24qrw();
    var numBits_0 = numBits & 63;
    if (numBits_0 === 0) {
      return _this__u8e3s4;
    } else {
      if (numBits_0 < 32) {
        return new Long(_this__u8e3s4.c1_1 >>> numBits_0 | 0 | _this__u8e3s4.d1_1 << (32 - numBits_0 | 0), _this__u8e3s4.d1_1 >> numBits_0);
      } else {
        return new Long(_this__u8e3s4.d1_1 >> (numBits_0 - 32 | 0), _this__u8e3s4.d1_1 >= 0 ? 0 : -1);
      }
    }
  }
  function bitwiseXor(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    return new Long(_this__u8e3s4.c1_1 ^ other.c1_1, _this__u8e3s4.d1_1 ^ other.d1_1);
  }
  function invert(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return new Long(~_this__u8e3s4.c1_1, ~_this__u8e3s4.d1_1);
  }
  function convertToInt(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return _this__u8e3s4.c1_1;
  }
  function toNumber(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return _this__u8e3s4.d1_1 * 4.294967296E9 + getLowBitsUnsigned(_this__u8e3s4);
  }
  function toStringImpl(_this__u8e3s4, radix) {
    _init_properties_boxedLong_kt__v24qrw();
    if (isZero(_this__u8e3s4)) {
      return '0';
    }
    if (isNegative(_this__u8e3s4)) {
      if (equalsLong(_this__u8e3s4, get_MIN_VALUE())) {
        var radixLong = fromInt(radix);
        var div = divide(_this__u8e3s4, radixLong);
        var rem = convertToInt(subtract(multiply(div, radixLong), _this__u8e3s4));
        var tmp = toStringImpl(div, radix);
        // Inline function 'kotlin.js.asDynamic' call
        // Inline function 'kotlin.js.unsafeCast' call
        return tmp + rem.toString(radix);
      } else {
        return '-' + toStringImpl(negate(_this__u8e3s4), radix);
      }
    }
    var digitsPerTime = radix === 2 ? 31 : radix <= 10 ? 9 : radix <= 21 ? 7 : radix <= 35 ? 6 : 5;
    var radixToPower = fromNumber(Math.pow(radix, digitsPerTime));
    var rem_0 = _this__u8e3s4;
    var result = '';
    while (true) {
      var remDiv = divide(rem_0, radixToPower);
      var intval = convertToInt(subtract(rem_0, multiply(remDiv, radixToPower)));
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      var digits = intval.toString(radix);
      rem_0 = remDiv;
      if (isZero(rem_0)) {
        return digits + result;
      } else {
        while (digits.length < digitsPerTime) {
          digits = '0' + digits;
        }
        result = digits + result;
      }
    }
  }
  function equalsLong(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    return _this__u8e3s4.d1_1 === other.d1_1 && _this__u8e3s4.c1_1 === other.c1_1;
  }
  function fromInt(value) {
    _init_properties_boxedLong_kt__v24qrw();
    return new Long(value, value < 0 ? -1 : 0);
  }
  function isNegative(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return _this__u8e3s4.d1_1 < 0;
  }
  function isZero(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return _this__u8e3s4.d1_1 === 0 && _this__u8e3s4.c1_1 === 0;
  }
  function isOdd(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return (_this__u8e3s4.c1_1 & 1) === 1;
  }
  function negate(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return add(invert(_this__u8e3s4), new Long(1, 0));
  }
  function lessThan(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    return compare(_this__u8e3s4, other) < 0;
  }
  function fromNumber(value) {
    _init_properties_boxedLong_kt__v24qrw();
    if (isNaN_0(value)) {
      return get_ZERO();
    } else if (value <= -9.223372036854776E18) {
      return get_MIN_VALUE();
    } else if (value + 1 >= 9.223372036854776E18) {
      return get_MAX_VALUE();
    } else if (value < 0) {
      return negate(fromNumber(-value));
    } else {
      var twoPwr32 = 4.294967296E9;
      // Inline function 'kotlin.js.jsBitwiseOr' call
      var tmp = value % twoPwr32 | 0;
      // Inline function 'kotlin.js.jsBitwiseOr' call
      var tmp$ret$1 = value / twoPwr32 | 0;
      return new Long(tmp, tmp$ret$1);
    }
  }
  function greaterThan(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    return compare(_this__u8e3s4, other) > 0;
  }
  function greaterThanOrEqual(_this__u8e3s4, other) {
    _init_properties_boxedLong_kt__v24qrw();
    return compare(_this__u8e3s4, other) >= 0;
  }
  function getLowBitsUnsigned(_this__u8e3s4) {
    _init_properties_boxedLong_kt__v24qrw();
    return _this__u8e3s4.c1_1 >= 0 ? _this__u8e3s4.c1_1 : 4.294967296E9 + _this__u8e3s4.c1_1;
  }
  var properties_initialized_boxedLong_kt_lfwt2;
  function _init_properties_boxedLong_kt__v24qrw() {
    if (!properties_initialized_boxedLong_kt_lfwt2) {
      properties_initialized_boxedLong_kt_lfwt2 = true;
      ZERO = fromInt(0);
      ONE = fromInt(1);
      NEG_ONE = fromInt(-1);
      MAX_VALUE = new Long(-1, 2147483647);
      MIN_VALUE = new Long(0, -2147483648);
      TWO_PWR_24_ = fromInt(16777216);
    }
  }
  function charSequenceGet(a, index) {
    var tmp;
    if (isString(a)) {
      tmp = charCodeAt(a, index);
    } else {
      tmp = a.b(index);
    }
    return tmp;
  }
  function isString(a) {
    return typeof a === 'string';
  }
  function charCodeAt(_this__u8e3s4, index) {
    // Inline function 'kotlin.js.asDynamic' call
    return _this__u8e3s4.charCodeAt(index);
  }
  function charSequenceLength(a) {
    var tmp;
    if (isString(a)) {
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      tmp = a.length;
    } else {
      tmp = a.a();
    }
    return tmp;
  }
  function charSequenceSubSequence(a, startIndex, endIndex) {
    var tmp;
    if (isString(a)) {
      tmp = substring(a, startIndex, endIndex);
    } else {
      tmp = a.c(startIndex, endIndex);
    }
    return tmp;
  }
  function arrayToString(array) {
    return joinToString(array, ', ', '[', ']', VOID, VOID, arrayToString$lambda);
  }
  function arrayToString$lambda(it) {
    return toString_1(it);
  }
  function compareTo(a, b) {
    var tmp;
    switch (typeof a) {
      case 'number':
        var tmp_0;
        if (typeof b === 'number') {
          tmp_0 = doubleCompareTo(a, b);
        } else {
          if (b instanceof Long) {
            tmp_0 = doubleCompareTo(a, toNumber(b));
          } else {
            tmp_0 = primitiveCompareTo(a, b);
          }
        }

        tmp = tmp_0;
        break;
      case 'string':
      case 'boolean':
      case 'bigint':
        tmp = primitiveCompareTo(a, b);
        break;
      default:
        tmp = compareToDoNotIntrinsicify(a, b);
        break;
    }
    return tmp;
  }
  function doubleCompareTo(a, b) {
    var tmp;
    if (a < b) {
      tmp = -1;
    } else if (a > b) {
      tmp = 1;
    } else if (a === b) {
      var tmp_0;
      if (a !== 0) {
        tmp_0 = 0;
      } else {
        // Inline function 'kotlin.js.asDynamic' call
        var ia = 1 / a;
        var tmp_1;
        // Inline function 'kotlin.js.asDynamic' call
        if (ia === 1 / b) {
          tmp_1 = 0;
        } else {
          if (ia < 0) {
            tmp_1 = -1;
          } else {
            tmp_1 = 1;
          }
        }
        tmp_0 = tmp_1;
      }
      tmp = tmp_0;
    } else if (a !== a) {
      tmp = b !== b ? 0 : 1;
    } else {
      tmp = -1;
    }
    return tmp;
  }
  function primitiveCompareTo(a, b) {
    return a < b ? -1 : a > b ? 1 : 0;
  }
  function compareToDoNotIntrinsicify(a, b) {
    return a.x(b);
  }
  function objectCreate(proto) {
    proto = proto === VOID ? null : proto;
    return Object.create(proto);
  }
  function defineProp(obj, name, getter, setter, enumerable) {
    return Object.defineProperty(obj, name, {configurable: true, get: getter, set: setter, enumerable: enumerable});
  }
  function toString_1(o) {
    var tmp;
    if (o == null) {
      tmp = 'null';
    } else if (isArrayish(o)) {
      tmp = '[...]';
    } else if (!(typeof o.toString === 'function')) {
      tmp = anyToString(o);
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      tmp = o.toString();
    }
    return tmp;
  }
  function anyToString(o) {
    return Object.prototype.toString.call(o);
  }
  function equals(obj1, obj2) {
    if (obj1 == null) {
      return obj2 == null;
    }
    if (obj2 == null) {
      return false;
    }
    if (typeof obj1 === 'object' && typeof obj1.equals === 'function') {
      return obj1.equals(obj2);
    }
    if (obj1 !== obj1) {
      return obj2 !== obj2;
    }
    if (typeof obj1 === 'number' && typeof obj2 === 'number') {
      var tmp;
      if (obj1 === obj2) {
        var tmp_0;
        if (obj1 !== 0) {
          tmp_0 = true;
        } else {
          // Inline function 'kotlin.js.asDynamic' call
          var tmp_1 = 1 / obj1;
          // Inline function 'kotlin.js.asDynamic' call
          tmp_0 = tmp_1 === 1 / obj2;
        }
        tmp = tmp_0;
      } else {
        tmp = false;
      }
      return tmp;
    }
    return obj1 === obj2;
  }
  function unboxIntrinsic(x) {
    var message = 'Should be lowered';
    throw IllegalStateException_init_$Create$_0(toString_1(message));
  }
  function captureStack(instance, constructorFunction) {
    if (Error.captureStackTrace != null) {
      Error.captureStackTrace(instance, constructorFunction);
    } else {
      // Inline function 'kotlin.js.asDynamic' call
      instance.stack = (new Error()).stack;
    }
  }
  function protoOf(constructor) {
    return constructor.prototype;
  }
  function defineMessage(message, cause) {
    var tmp;
    if (isUndefined(message)) {
      var tmp_0;
      if (isUndefined(cause)) {
        tmp_0 = message;
      } else {
        var tmp1_elvis_lhs = cause == null ? null : cause.toString();
        tmp_0 = tmp1_elvis_lhs == null ? VOID : tmp1_elvis_lhs;
      }
      tmp = tmp_0;
    } else {
      tmp = message == null ? VOID : message;
    }
    return tmp;
  }
  function isUndefined(value) {
    return value === VOID;
  }
  function extendThrowable(this_, message, cause) {
    defineFieldOnInstance(this_, 'message', defineMessage(message, cause));
    defineFieldOnInstance(this_, 'cause', cause);
    defineFieldOnInstance(this_, 'name', Object.getPrototypeOf(this_).constructor.name);
  }
  function defineFieldOnInstance(this_, name, value) {
    Object.defineProperty(this_, name, {configurable: true, writable: true, value: value});
  }
  function noWhenBranchMatchedException() {
    throw NoWhenBranchMatchedException_init_$Create$();
  }
  function THROW_CCE() {
    throw ClassCastException_init_$Create$();
  }
  function THROW_IAE(msg) {
    throw IllegalArgumentException_init_$Create$_0(msg);
  }
  function createMetadata(kind, name, defaultConstructor, associatedObjectKey, associatedObjects, suspendArity) {
    var undef = VOID;
    var iid = kind === 'interface' ? generateInterfaceId() : VOID;
    return {kind: kind, simpleName: name, associatedObjectKey: associatedObjectKey, associatedObjects: associatedObjects, suspendArity: suspendArity, $kClass$: undef, defaultConstructor: defaultConstructor, iid: iid};
  }
  function generateInterfaceId() {
    if (globalInterfaceId === VOID) {
      globalInterfaceId = 0;
    }
    // Inline function 'kotlin.js.unsafeCast' call
    globalInterfaceId = globalInterfaceId + 1 | 0;
    // Inline function 'kotlin.js.unsafeCast' call
    return globalInterfaceId;
  }
  var globalInterfaceId;
  function initMetadataForClass(ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
    var kind = 'class';
    initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects);
  }
  function initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
    if (!(parent == null)) {
      ctor.prototype = Object.create(parent.prototype);
      ctor.prototype.constructor = ctor;
    }
    var metadata = createMetadata(kind, name, defaultConstructor, associatedObjectKey, associatedObjects, suspendArity);
    ctor.$metadata$ = metadata;
    if (!(interfaces == null)) {
      var receiver = !equals(metadata.iid, VOID) ? ctor : ctor.prototype;
      receiver.$imask$ = implement(interfaces);
    }
  }
  function initMetadataForObject(ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
    var kind = 'object';
    initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects);
  }
  function initMetadataForInterface(ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
    var kind = 'interface';
    initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects);
  }
  function initMetadataForLambda(ctor, parent, interfaces, suspendArity) {
    initMetadataForClass(ctor, 'Lambda', VOID, parent, interfaces, suspendArity, VOID, VOID);
  }
  function initMetadataForCoroutine(ctor, parent, interfaces, suspendArity) {
    initMetadataForClass(ctor, 'Coroutine', VOID, parent, interfaces, suspendArity, VOID, VOID);
  }
  function initMetadataForFunctionReference(ctor, parent, interfaces, suspendArity) {
    initMetadataForClass(ctor, 'FunctionReference', VOID, parent, interfaces, suspendArity, VOID, VOID);
  }
  function initMetadataForCompanion(ctor, parent, interfaces, suspendArity) {
    initMetadataForObject(ctor, 'Companion', VOID, parent, interfaces, suspendArity, VOID, VOID);
  }
  function numberRangeToNumber(start, endInclusive) {
    return new IntRange(start, endInclusive);
  }
  function isArrayish(o) {
    return isJsArray(o) || isView(o);
  }
  function isJsArray(obj) {
    // Inline function 'kotlin.js.unsafeCast' call
    return Array.isArray(obj);
  }
  function isInterface(obj, iface) {
    return isInterfaceImpl(obj, iface.$metadata$.iid);
  }
  function isInterfaceImpl(obj, iface) {
    // Inline function 'kotlin.js.unsafeCast' call
    var tmp0_elvis_lhs = obj.$imask$;
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return false;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var mask = tmp;
    return isBitSet(mask, iface);
  }
  function isCharSequence(value) {
    return typeof value === 'string' || isInterface(value, CharSequence);
  }
  function get_VOID() {
    _init_properties_void_kt__3zg9as();
    return VOID;
  }
  var VOID;
  var properties_initialized_void_kt_e4ret2;
  function _init_properties_void_kt__3zg9as() {
    if (!properties_initialized_void_kt_e4ret2) {
      properties_initialized_void_kt_e4ret2 = true;
      VOID = void 0;
    }
  }
  function asList(_this__u8e3s4) {
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    return new ArrayList(_this__u8e3s4);
  }
  function digitToIntImpl(_this__u8e3s4) {
    // Inline function 'kotlin.code' call
    var ch = Char__toInt_impl_vasixd(_this__u8e3s4);
    var index = binarySearchRange(Digit_getInstance().g1_1, ch);
    var diff = ch - Digit_getInstance().g1_1[index] | 0;
    return diff < 10 ? diff : -1;
  }
  function binarySearchRange(array, needle) {
    var bottom = 0;
    var top = array.length - 1 | 0;
    var middle = -1;
    var value = 0;
    while (bottom <= top) {
      middle = (bottom + top | 0) / 2 | 0;
      value = array[middle];
      if (needle > value)
        bottom = middle + 1 | 0;
      else if (needle === value)
        return middle;
      else
        top = middle - 1 | 0;
    }
    return middle - (needle < value ? 1 : 0) | 0;
  }
  function Digit() {
    Digit_instance = this;
    var tmp = this;
    // Inline function 'kotlin.intArrayOf' call
    tmp.g1_1 = new Int32Array([48, 1632, 1776, 1984, 2406, 2534, 2662, 2790, 2918, 3046, 3174, 3302, 3430, 3558, 3664, 3792, 3872, 4160, 4240, 6112, 6160, 6470, 6608, 6784, 6800, 6992, 7088, 7232, 7248, 42528, 43216, 43264, 43472, 43504, 43600, 44016, 65296]);
  }
  var Digit_instance;
  function Digit_getInstance() {
    if (Digit_instance == null)
      new Digit();
    return Digit_instance;
  }
  function Comparator() {
  }
  function isNaN_0(_this__u8e3s4) {
    return !(_this__u8e3s4 === _this__u8e3s4);
  }
  function Unit() {
  }
  protoOf(Unit).toString = function () {
    return 'kotlin.Unit';
  };
  var Unit_instance;
  function Unit_getInstance() {
    return Unit_instance;
  }
  function collectionToArray(collection) {
    return collectionToArrayCommonImpl(collection);
  }
  function listOf(element) {
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp$ret$2 = [element];
    return new ArrayList(tmp$ret$2);
  }
  function checkCountOverflow(count) {
    if (count < 0) {
      throwCountOverflow();
    }
    return count;
  }
  function checkIndexOverflow(index) {
    if (index < 0) {
      throwIndexOverflow();
    }
    return index;
  }
  function copyToArray(collection) {
    var tmp;
    // Inline function 'kotlin.js.asDynamic' call
    if (collection.toArray !== undefined) {
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      tmp = collection.toArray();
    } else {
      // Inline function 'kotlin.js.unsafeCast' call
      // Inline function 'kotlin.js.asDynamic' call
      tmp = collectionToArray(collection);
    }
    return tmp;
  }
  function AbstractMutableCollection() {
    AbstractCollection.call(this);
  }
  protoOf(AbstractMutableCollection).n = function (elements) {
    this.h1();
    var modified = false;
    var _iterator__ex2g4s = elements.e();
    while (_iterator__ex2g4s.f()) {
      var element = _iterator__ex2g4s.g();
      if (this.o(element))
        modified = true;
    }
    return modified;
  };
  protoOf(AbstractMutableCollection).toJSON = function () {
    return this.toArray();
  };
  protoOf(AbstractMutableCollection).h1 = function () {
  };
  function IteratorImpl($outer) {
    this.k1_1 = $outer;
    this.i1_1 = 0;
    this.j1_1 = -1;
  }
  protoOf(IteratorImpl).f = function () {
    return this.i1_1 < this.k1_1.j();
  };
  protoOf(IteratorImpl).g = function () {
    if (!this.f())
      throw NoSuchElementException_init_$Create$();
    var tmp = this;
    var _unary__edvuaz = this.i1_1;
    this.i1_1 = _unary__edvuaz + 1 | 0;
    tmp.j1_1 = _unary__edvuaz;
    return this.k1_1.i(this.j1_1);
  };
  function ListIteratorImpl($outer, index) {
    this.o1_1 = $outer;
    IteratorImpl.call(this, $outer);
    Companion_instance_2.q1(index, this.o1_1.j());
    this.i1_1 = index;
  }
  protoOf(ListIteratorImpl).r1 = function () {
    return this.i1_1 > 0;
  };
  protoOf(ListIteratorImpl).s1 = function () {
    return this.i1_1;
  };
  protoOf(ListIteratorImpl).t1 = function () {
    if (!this.r1())
      throw NoSuchElementException_init_$Create$();
    var tmp = this;
    this.i1_1 = this.i1_1 - 1 | 0;
    tmp.j1_1 = this.i1_1;
    return this.o1_1.i(this.j1_1);
  };
  function AbstractMutableList() {
    AbstractMutableCollection.call(this);
    this.u1_1 = 0;
  }
  protoOf(AbstractMutableList).o = function (element) {
    this.h1();
    this.v1(this.j(), element);
    return true;
  };
  protoOf(AbstractMutableList).e = function () {
    return new IteratorImpl(this);
  };
  protoOf(AbstractMutableList).s = function (element) {
    var tmp$ret$1;
    $l$block: {
      // Inline function 'kotlin.collections.indexOfLast' call
      var iterator = this.t(this.j());
      while (iterator.r1()) {
        var it = iterator.t1();
        if (equals(it, element)) {
          tmp$ret$1 = iterator.s1();
          break $l$block;
        }
      }
      tmp$ret$1 = -1;
    }
    return tmp$ret$1;
  };
  protoOf(AbstractMutableList).t = function (index) {
    return new ListIteratorImpl(this, index);
  };
  protoOf(AbstractMutableList).equals = function (other) {
    if (other === this)
      return true;
    if (!(!(other == null) ? isInterface(other, KtList) : false))
      return false;
    return Companion_instance_2.y1(this, other);
  };
  function Companion_1() {
    Companion_instance_1 = this;
    var tmp = this;
    // Inline function 'kotlin.also' call
    var this_0 = ArrayList_init_$Create$_0(0);
    this_0.m_1 = true;
    tmp.z1_1 = this_0;
  }
  var Companion_instance_1;
  function Companion_getInstance_1() {
    if (Companion_instance_1 == null)
      new Companion_1();
    return Companion_instance_1;
  }
  function ArrayList_init_$Init$($this) {
    // Inline function 'kotlin.emptyArray' call
    var tmp$ret$0 = [];
    ArrayList.call($this, tmp$ret$0);
    return $this;
  }
  function ArrayList_init_$Create$() {
    return ArrayList_init_$Init$(objectCreate(protoOf(ArrayList)));
  }
  function ArrayList_init_$Init$_0(initialCapacity, $this) {
    // Inline function 'kotlin.emptyArray' call
    var tmp$ret$0 = [];
    ArrayList.call($this, tmp$ret$0);
    // Inline function 'kotlin.require' call
    if (!(initialCapacity >= 0)) {
      var message = 'Negative initial capacity: ' + initialCapacity;
      throw IllegalArgumentException_init_$Create$_0(toString_1(message));
    }
    return $this;
  }
  function ArrayList_init_$Create$_0(initialCapacity) {
    return ArrayList_init_$Init$_0(initialCapacity, objectCreate(protoOf(ArrayList)));
  }
  function ArrayList_init_$Init$_1(elements, $this) {
    // Inline function 'kotlin.collections.toTypedArray' call
    var tmp$ret$0 = copyToArray(elements);
    ArrayList.call($this, tmp$ret$0);
    return $this;
  }
  function ArrayList_init_$Create$_1(elements) {
    return ArrayList_init_$Init$_1(elements, objectCreate(protoOf(ArrayList)));
  }
  function increaseLength($this, amount) {
    var previous = $this.j();
    // Inline function 'kotlin.js.asDynamic' call
    $this.l_1.length = $this.j() + amount | 0;
    return previous;
  }
  function rangeCheck($this, index) {
    // Inline function 'kotlin.apply' call
    Companion_instance_2.a2(index, $this.j());
    return index;
  }
  function insertionRangeCheck($this, index) {
    // Inline function 'kotlin.apply' call
    Companion_instance_2.q1(index, $this.j());
    return index;
  }
  function ArrayList(array) {
    Companion_getInstance_1();
    AbstractMutableList.call(this);
    this.l_1 = array;
    this.m_1 = false;
  }
  protoOf(ArrayList).j = function () {
    return this.l_1.length;
  };
  protoOf(ArrayList).i = function (index) {
    var tmp = this.l_1[rangeCheck(this, index)];
    return (tmp == null ? true : !(tmp == null)) ? tmp : THROW_CCE();
  };
  protoOf(ArrayList).x1 = function (index, element) {
    this.h1();
    rangeCheck(this, index);
    // Inline function 'kotlin.apply' call
    var this_0 = this.l_1[index];
    this.l_1[index] = element;
    var tmp = this_0;
    return (tmp == null ? true : !(tmp == null)) ? tmp : THROW_CCE();
  };
  protoOf(ArrayList).o = function (element) {
    this.h1();
    // Inline function 'kotlin.js.asDynamic' call
    this.l_1.push(element);
    this.u1_1 = this.u1_1 + 1 | 0;
    return true;
  };
  protoOf(ArrayList).v1 = function (index, element) {
    this.h1();
    // Inline function 'kotlin.js.asDynamic' call
    this.l_1.splice(insertionRangeCheck(this, index), 0, element);
    this.u1_1 = this.u1_1 + 1 | 0;
  };
  protoOf(ArrayList).n = function (elements) {
    this.h1();
    if (elements.h())
      return false;
    var offset = increaseLength(this, elements.j());
    // Inline function 'kotlin.collections.forEachIndexed' call
    var index = 0;
    var _iterator__ex2g4s = elements.e();
    while (_iterator__ex2g4s.f()) {
      var item = _iterator__ex2g4s.g();
      var _unary__edvuaz = index;
      index = _unary__edvuaz + 1 | 0;
      var index_0 = checkIndexOverflow(_unary__edvuaz);
      this.l_1[offset + index_0 | 0] = item;
    }
    this.u1_1 = this.u1_1 + 1 | 0;
    return true;
  };
  protoOf(ArrayList).w1 = function (index) {
    this.h1();
    rangeCheck(this, index);
    this.u1_1 = this.u1_1 + 1 | 0;
    var tmp;
    if (index === get_lastIndex(this)) {
      // Inline function 'kotlin.js.asDynamic' call
      tmp = this.l_1.pop();
    } else {
      // Inline function 'kotlin.js.asDynamic' call
      tmp = this.l_1.splice(index, 1)[0];
    }
    return tmp;
  };
  protoOf(ArrayList).s = function (element) {
    return lastIndexOf(this.l_1, element);
  };
  protoOf(ArrayList).toString = function () {
    return arrayToString(this.l_1);
  };
  protoOf(ArrayList).b2 = function () {
    return [].slice.call(this.l_1);
  };
  protoOf(ArrayList).toArray = function () {
    return this.b2();
  };
  protoOf(ArrayList).h1 = function () {
    if (this.m_1)
      throw UnsupportedOperationException_init_$Create$();
  };
  function Exception_init_$Init$($this) {
    extendThrowable($this);
    Exception.call($this);
    return $this;
  }
  function Exception_init_$Create$() {
    var tmp = Exception_init_$Init$(objectCreate(protoOf(Exception)));
    captureStack(tmp, Exception_init_$Create$);
    return tmp;
  }
  function Exception_init_$Init$_0(message, $this) {
    extendThrowable($this, message);
    Exception.call($this);
    return $this;
  }
  function Exception_init_$Create$_0(message) {
    var tmp = Exception_init_$Init$_0(message, objectCreate(protoOf(Exception)));
    captureStack(tmp, Exception_init_$Create$_0);
    return tmp;
  }
  function Exception() {
    captureStack(this, Exception);
  }
  function IllegalArgumentException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    IllegalArgumentException.call($this);
    return $this;
  }
  function IllegalArgumentException_init_$Create$() {
    var tmp = IllegalArgumentException_init_$Init$(objectCreate(protoOf(IllegalArgumentException)));
    captureStack(tmp, IllegalArgumentException_init_$Create$);
    return tmp;
  }
  function IllegalArgumentException_init_$Init$_0(message, $this) {
    RuntimeException_init_$Init$_0(message, $this);
    IllegalArgumentException.call($this);
    return $this;
  }
  function IllegalArgumentException_init_$Create$_0(message) {
    var tmp = IllegalArgumentException_init_$Init$_0(message, objectCreate(protoOf(IllegalArgumentException)));
    captureStack(tmp, IllegalArgumentException_init_$Create$_0);
    return tmp;
  }
  function IllegalArgumentException() {
    captureStack(this, IllegalArgumentException);
  }
  function IllegalStateException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    IllegalStateException.call($this);
    return $this;
  }
  function IllegalStateException_init_$Create$() {
    var tmp = IllegalStateException_init_$Init$(objectCreate(protoOf(IllegalStateException)));
    captureStack(tmp, IllegalStateException_init_$Create$);
    return tmp;
  }
  function IllegalStateException_init_$Init$_0(message, $this) {
    RuntimeException_init_$Init$_0(message, $this);
    IllegalStateException.call($this);
    return $this;
  }
  function IllegalStateException_init_$Create$_0(message) {
    var tmp = IllegalStateException_init_$Init$_0(message, objectCreate(protoOf(IllegalStateException)));
    captureStack(tmp, IllegalStateException_init_$Create$_0);
    return tmp;
  }
  function IllegalStateException() {
    captureStack(this, IllegalStateException);
  }
  function UnsupportedOperationException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    UnsupportedOperationException.call($this);
    return $this;
  }
  function UnsupportedOperationException_init_$Create$() {
    var tmp = UnsupportedOperationException_init_$Init$(objectCreate(protoOf(UnsupportedOperationException)));
    captureStack(tmp, UnsupportedOperationException_init_$Create$);
    return tmp;
  }
  function UnsupportedOperationException() {
    captureStack(this, UnsupportedOperationException);
  }
  function RuntimeException_init_$Init$($this) {
    Exception_init_$Init$($this);
    RuntimeException.call($this);
    return $this;
  }
  function RuntimeException_init_$Create$() {
    var tmp = RuntimeException_init_$Init$(objectCreate(protoOf(RuntimeException)));
    captureStack(tmp, RuntimeException_init_$Create$);
    return tmp;
  }
  function RuntimeException_init_$Init$_0(message, $this) {
    Exception_init_$Init$_0(message, $this);
    RuntimeException.call($this);
    return $this;
  }
  function RuntimeException() {
    captureStack(this, RuntimeException);
  }
  function NoSuchElementException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    NoSuchElementException.call($this);
    return $this;
  }
  function NoSuchElementException_init_$Create$() {
    var tmp = NoSuchElementException_init_$Init$(objectCreate(protoOf(NoSuchElementException)));
    captureStack(tmp, NoSuchElementException_init_$Create$);
    return tmp;
  }
  function NoSuchElementException_init_$Init$_0(message, $this) {
    RuntimeException_init_$Init$_0(message, $this);
    NoSuchElementException.call($this);
    return $this;
  }
  function NoSuchElementException_init_$Create$_0(message) {
    var tmp = NoSuchElementException_init_$Init$_0(message, objectCreate(protoOf(NoSuchElementException)));
    captureStack(tmp, NoSuchElementException_init_$Create$_0);
    return tmp;
  }
  function NoSuchElementException() {
    captureStack(this, NoSuchElementException);
  }
  function IndexOutOfBoundsException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    IndexOutOfBoundsException.call($this);
    return $this;
  }
  function IndexOutOfBoundsException_init_$Create$() {
    var tmp = IndexOutOfBoundsException_init_$Init$(objectCreate(protoOf(IndexOutOfBoundsException)));
    captureStack(tmp, IndexOutOfBoundsException_init_$Create$);
    return tmp;
  }
  function IndexOutOfBoundsException_init_$Init$_0(message, $this) {
    RuntimeException_init_$Init$_0(message, $this);
    IndexOutOfBoundsException.call($this);
    return $this;
  }
  function IndexOutOfBoundsException_init_$Create$_0(message) {
    var tmp = IndexOutOfBoundsException_init_$Init$_0(message, objectCreate(protoOf(IndexOutOfBoundsException)));
    captureStack(tmp, IndexOutOfBoundsException_init_$Create$_0);
    return tmp;
  }
  function IndexOutOfBoundsException() {
    captureStack(this, IndexOutOfBoundsException);
  }
  function ArithmeticException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    ArithmeticException.call($this);
    return $this;
  }
  function ArithmeticException_init_$Create$() {
    var tmp = ArithmeticException_init_$Init$(objectCreate(protoOf(ArithmeticException)));
    captureStack(tmp, ArithmeticException_init_$Create$);
    return tmp;
  }
  function ArithmeticException_init_$Init$_0(message, $this) {
    RuntimeException_init_$Init$_0(message, $this);
    ArithmeticException.call($this);
    return $this;
  }
  function ArithmeticException_init_$Create$_0(message) {
    var tmp = ArithmeticException_init_$Init$_0(message, objectCreate(protoOf(ArithmeticException)));
    captureStack(tmp, ArithmeticException_init_$Create$_0);
    return tmp;
  }
  function ArithmeticException() {
    captureStack(this, ArithmeticException);
  }
  function NumberFormatException_init_$Init$($this) {
    IllegalArgumentException_init_$Init$($this);
    NumberFormatException.call($this);
    return $this;
  }
  function NumberFormatException_init_$Create$() {
    var tmp = NumberFormatException_init_$Init$(objectCreate(protoOf(NumberFormatException)));
    captureStack(tmp, NumberFormatException_init_$Create$);
    return tmp;
  }
  function NumberFormatException_init_$Init$_0(message, $this) {
    IllegalArgumentException_init_$Init$_0(message, $this);
    NumberFormatException.call($this);
    return $this;
  }
  function NumberFormatException_init_$Create$_0(message) {
    var tmp = NumberFormatException_init_$Init$_0(message, objectCreate(protoOf(NumberFormatException)));
    captureStack(tmp, NumberFormatException_init_$Create$_0);
    return tmp;
  }
  function NumberFormatException() {
    captureStack(this, NumberFormatException);
  }
  function NoWhenBranchMatchedException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    NoWhenBranchMatchedException.call($this);
    return $this;
  }
  function NoWhenBranchMatchedException_init_$Create$() {
    var tmp = NoWhenBranchMatchedException_init_$Init$(objectCreate(protoOf(NoWhenBranchMatchedException)));
    captureStack(tmp, NoWhenBranchMatchedException_init_$Create$);
    return tmp;
  }
  function NoWhenBranchMatchedException() {
    captureStack(this, NoWhenBranchMatchedException);
  }
  function ClassCastException_init_$Init$($this) {
    RuntimeException_init_$Init$($this);
    ClassCastException.call($this);
    return $this;
  }
  function ClassCastException_init_$Create$() {
    var tmp = ClassCastException_init_$Init$(objectCreate(protoOf(ClassCastException)));
    captureStack(tmp, ClassCastException_init_$Create$);
    return tmp;
  }
  function ClassCastException() {
    captureStack(this, ClassCastException);
  }
  var INV_2_26;
  var INV_2_53;
  function defaultPlatformRandom() {
    _init_properties_PlatformRandom_kt__6kjv62();
    // Inline function 'kotlin.js.unsafeCast' call
    var tmp$ret$0 = Math.random() * Math.pow(2, 32) | 0;
    return Random_1(tmp$ret$0);
  }
  var properties_initialized_PlatformRandom_kt_uibhw8;
  function _init_properties_PlatformRandom_kt__6kjv62() {
    if (!properties_initialized_PlatformRandom_kt_uibhw8) {
      properties_initialized_PlatformRandom_kt_uibhw8 = true;
      // Inline function 'kotlin.math.pow' call
      INV_2_26 = Math.pow(2.0, -26);
      // Inline function 'kotlin.math.pow' call
      INV_2_53 = Math.pow(2.0, -53);
    }
  }
  function StringBuilder_init_$Init$($this) {
    StringBuilder.call($this, '');
    return $this;
  }
  function StringBuilder_init_$Create$() {
    return StringBuilder_init_$Init$(objectCreate(protoOf(StringBuilder)));
  }
  function StringBuilder(content) {
    this.e2_1 = content;
  }
  protoOf(StringBuilder).a = function () {
    // Inline function 'kotlin.js.asDynamic' call
    return this.e2_1.length;
  };
  protoOf(StringBuilder).b = function (index) {
    // Inline function 'kotlin.text.getOrElse' call
    var this_0 = this.e2_1;
    var tmp;
    if (0 <= index ? index <= (charSequenceLength(this_0) - 1 | 0) : false) {
      tmp = charSequenceGet(this_0, index);
    } else {
      throw IndexOutOfBoundsException_init_$Create$_0('index: ' + index + ', length: ' + this.a() + '}');
    }
    return tmp;
  };
  protoOf(StringBuilder).c = function (startIndex, endIndex) {
    return substring(this.e2_1, startIndex, endIndex);
  };
  protoOf(StringBuilder).f2 = function (value) {
    this.e2_1 = this.e2_1 + toString(value);
    return this;
  };
  protoOf(StringBuilder).d = function (value) {
    this.e2_1 = this.e2_1 + toString_0(value);
    return this;
  };
  protoOf(StringBuilder).g2 = function (value) {
    this.e2_1 = this.e2_1 + toString_0(value);
    return this;
  };
  protoOf(StringBuilder).h2 = function (value) {
    this.e2_1 = this.e2_1 + value;
    return this;
  };
  protoOf(StringBuilder).i2 = function (value) {
    return this.j2(value.toString());
  };
  protoOf(StringBuilder).j2 = function (value) {
    var tmp = this;
    var tmp_0 = this.e2_1;
    tmp.e2_1 = tmp_0 + (value == null ? 'null' : value);
    return this;
  };
  protoOf(StringBuilder).toString = function () {
    return this.e2_1;
  };
  function uppercaseChar(_this__u8e3s4) {
    // Inline function 'kotlin.text.uppercase' call
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    var uppercase = toString(_this__u8e3s4).toUpperCase();
    return uppercase.length > 1 ? _this__u8e3s4 : charCodeAt(uppercase, 0);
  }
  function checkRadix(radix) {
    if (!(2 <= radix ? radix <= 36 : false)) {
      throw IllegalArgumentException_init_$Create$_0('radix ' + radix + ' was not in valid range 2..36');
    }
    return radix;
  }
  function toInt(_this__u8e3s4) {
    var tmp0_elvis_lhs = toIntOrNull(_this__u8e3s4);
    var tmp;
    if (tmp0_elvis_lhs == null) {
      numberFormatError(_this__u8e3s4);
    } else {
      tmp = tmp0_elvis_lhs;
    }
    return tmp;
  }
  function digitOf(char, radix) {
    // Inline function 'kotlin.let' call
    var it = Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(48)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(57)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(48)) : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(90)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(65)) + 10 | 0 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(97)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(122)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(97)) + 10 | 0 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(128)) < 0 ? -1 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65313)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65338)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(65313)) + 10 | 0 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65345)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65370)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(65345)) + 10 | 0 : digitToIntImpl(char);
    return it >= radix ? -1 : it;
  }
  var STRING_CASE_INSENSITIVE_ORDER;
  function substring(_this__u8e3s4, startIndex, endIndex) {
    _init_properties_stringJs_kt__bg7zye();
    // Inline function 'kotlin.js.asDynamic' call
    return _this__u8e3s4.substring(startIndex, endIndex);
  }
  function compareTo_0(_this__u8e3s4, other, ignoreCase) {
    ignoreCase = ignoreCase === VOID ? false : ignoreCase;
    _init_properties_stringJs_kt__bg7zye();
    if (ignoreCase) {
      var n1 = _this__u8e3s4.length;
      var n2 = other.length;
      // Inline function 'kotlin.comparisons.minOf' call
      var min = Math.min(n1, n2);
      if (min === 0)
        return n1 - n2 | 0;
      var inductionVariable = 0;
      if (inductionVariable < min)
        do {
          var index = inductionVariable;
          inductionVariable = inductionVariable + 1 | 0;
          var thisChar = charCodeAt(_this__u8e3s4, index);
          var otherChar = charCodeAt(other, index);
          if (!(thisChar === otherChar)) {
            thisChar = uppercaseChar(thisChar);
            otherChar = uppercaseChar(otherChar);
            if (!(thisChar === otherChar)) {
              // Inline function 'kotlin.text.lowercaseChar' call
              // Inline function 'kotlin.text.lowercase' call
              var this_0 = thisChar;
              // Inline function 'kotlin.js.asDynamic' call
              // Inline function 'kotlin.js.unsafeCast' call
              var tmp$ret$3 = toString(this_0).toLowerCase();
              thisChar = charCodeAt(tmp$ret$3, 0);
              // Inline function 'kotlin.text.lowercaseChar' call
              // Inline function 'kotlin.text.lowercase' call
              var this_1 = otherChar;
              // Inline function 'kotlin.js.asDynamic' call
              // Inline function 'kotlin.js.unsafeCast' call
              var tmp$ret$7 = toString(this_1).toLowerCase();
              otherChar = charCodeAt(tmp$ret$7, 0);
              if (!(thisChar === otherChar)) {
                return Char__compareTo_impl_ypi4mb(thisChar, otherChar);
              }
            }
          }
        }
         while (inductionVariable < min);
      return n1 - n2 | 0;
    } else {
      return compareTo(_this__u8e3s4, other);
    }
  }
  function sam$kotlin_Comparator$0(function_0) {
    this.k2_1 = function_0;
  }
  protoOf(sam$kotlin_Comparator$0).l2 = function (a, b) {
    return this.k2_1(a, b);
  };
  protoOf(sam$kotlin_Comparator$0).compare = function (a, b) {
    return this.l2(a, b);
  };
  protoOf(sam$kotlin_Comparator$0).f1 = function () {
    return this.k2_1;
  };
  protoOf(sam$kotlin_Comparator$0).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, Comparator) : false) {
      var tmp_0;
      if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
        tmp_0 = equals(this.f1(), other.f1());
      } else {
        tmp_0 = false;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  };
  function STRING_CASE_INSENSITIVE_ORDER$lambda(a, b) {
    _init_properties_stringJs_kt__bg7zye();
    return compareTo_0(a, b, true);
  }
  var properties_initialized_stringJs_kt_nta8o4;
  function _init_properties_stringJs_kt__bg7zye() {
    if (!properties_initialized_stringJs_kt_nta8o4) {
      properties_initialized_stringJs_kt_nta8o4 = true;
      var tmp = STRING_CASE_INSENSITIVE_ORDER$lambda;
      STRING_CASE_INSENSITIVE_ORDER = new sam$kotlin_Comparator$0(tmp);
    }
  }
  function regionMatches(_this__u8e3s4, thisOffset, other, otherOffset, length, ignoreCase) {
    ignoreCase = ignoreCase === VOID ? false : ignoreCase;
    return regionMatchesImpl(_this__u8e3s4, thisOffset, other, otherOffset, length, ignoreCase);
  }
  function AbstractCollection$toString$lambda(this$0) {
    return function (it) {
      return it === this$0 ? '(this Collection)' : toString_0(it);
    };
  }
  function AbstractCollection() {
  }
  protoOf(AbstractCollection).h = function () {
    return this.j() === 0;
  };
  protoOf(AbstractCollection).toString = function () {
    return joinToString_0(this, ', ', '[', ']', VOID, VOID, AbstractCollection$toString$lambda(this));
  };
  protoOf(AbstractCollection).toArray = function () {
    return collectionToArray(this);
  };
  function IteratorImpl_0($outer) {
    this.n2_1 = $outer;
    this.m2_1 = 0;
  }
  protoOf(IteratorImpl_0).f = function () {
    return this.m2_1 < this.n2_1.j();
  };
  protoOf(IteratorImpl_0).g = function () {
    if (!this.f())
      throw NoSuchElementException_init_$Create$();
    var _unary__edvuaz = this.m2_1;
    this.m2_1 = _unary__edvuaz + 1 | 0;
    return this.n2_1.i(_unary__edvuaz);
  };
  function ListIteratorImpl_0($outer, index) {
    this.q2_1 = $outer;
    IteratorImpl_0.call(this, $outer);
    Companion_instance_2.q1(index, this.q2_1.j());
    this.m2_1 = index;
  }
  protoOf(ListIteratorImpl_0).r1 = function () {
    return this.m2_1 > 0;
  };
  protoOf(ListIteratorImpl_0).s1 = function () {
    return this.m2_1;
  };
  protoOf(ListIteratorImpl_0).t1 = function () {
    if (!this.r1())
      throw NoSuchElementException_init_$Create$();
    this.m2_1 = this.m2_1 - 1 | 0;
    return this.q2_1.i(this.m2_1);
  };
  function Companion_2() {
    this.p1_1 = 2147483639;
  }
  protoOf(Companion_2).a2 = function (index, size) {
    if (index < 0 || index >= size) {
      throw IndexOutOfBoundsException_init_$Create$_0('index: ' + index + ', size: ' + size);
    }
  };
  protoOf(Companion_2).q1 = function (index, size) {
    if (index < 0 || index > size) {
      throw IndexOutOfBoundsException_init_$Create$_0('index: ' + index + ', size: ' + size);
    }
  };
  protoOf(Companion_2).y1 = function (c, other) {
    if (!(c.j() === other.j()))
      return false;
    var otherIterator = other.e();
    var _iterator__ex2g4s = c.e();
    while (_iterator__ex2g4s.f()) {
      var elem = _iterator__ex2g4s.g();
      var elemOther = otherIterator.g();
      if (!equals(elem, elemOther)) {
        return false;
      }
    }
    return true;
  };
  var Companion_instance_2;
  function Companion_getInstance_2() {
    return Companion_instance_2;
  }
  function AbstractList() {
    AbstractCollection.call(this);
  }
  protoOf(AbstractList).e = function () {
    return new IteratorImpl_0(this);
  };
  protoOf(AbstractList).s = function (element) {
    var tmp$ret$1;
    $l$block: {
      // Inline function 'kotlin.collections.indexOfLast' call
      var iterator = this.t(this.j());
      while (iterator.r1()) {
        var it = iterator.t1();
        if (equals(it, element)) {
          tmp$ret$1 = iterator.s1();
          break $l$block;
        }
      }
      tmp$ret$1 = -1;
    }
    return tmp$ret$1;
  };
  protoOf(AbstractList).t = function (index) {
    return new ListIteratorImpl_0(this, index);
  };
  protoOf(AbstractList).equals = function (other) {
    if (other === this)
      return true;
    if (!(!(other == null) ? isInterface(other, KtList) : false))
      return false;
    return Companion_instance_2.y1(this, other);
  };
  function collectionToArrayCommonImpl(collection) {
    if (collection.h()) {
      // Inline function 'kotlin.emptyArray' call
      return [];
    }
    // Inline function 'kotlin.arrayOfNulls' call
    var size = collection.j();
    var destination = Array(size);
    var iterator = collection.e();
    var index = 0;
    while (iterator.f()) {
      var _unary__edvuaz = index;
      index = _unary__edvuaz + 1 | 0;
      destination[_unary__edvuaz] = iterator.g();
    }
    return destination;
  }
  function listOf_0(elements) {
    return elements.length > 0 ? asList(elements) : emptyList();
  }
  function emptyList() {
    return EmptyList_getInstance();
  }
  function get_lastIndex(_this__u8e3s4) {
    return _this__u8e3s4.j() - 1 | 0;
  }
  function EmptyList() {
    EmptyList_instance = this;
    this.r2_1 = new Long(-1478467534, -1720727600);
  }
  protoOf(EmptyList).equals = function (other) {
    var tmp;
    if (!(other == null) ? isInterface(other, KtList) : false) {
      tmp = other.h();
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(EmptyList).toString = function () {
    return '[]';
  };
  protoOf(EmptyList).j = function () {
    return 0;
  };
  protoOf(EmptyList).h = function () {
    return true;
  };
  protoOf(EmptyList).i = function (index) {
    throw IndexOutOfBoundsException_init_$Create$_0("Empty list doesn't contain element at index " + index + '.');
  };
  protoOf(EmptyList).s2 = function (element) {
    return -1;
  };
  protoOf(EmptyList).s = function (element) {
    if (!false)
      return -1;
    var tmp;
    if (false) {
      tmp = element;
    } else {
      tmp = THROW_CCE();
    }
    return this.s2(tmp);
  };
  protoOf(EmptyList).e = function () {
    return EmptyIterator_instance;
  };
  protoOf(EmptyList).t = function (index) {
    if (!(index === 0))
      throw IndexOutOfBoundsException_init_$Create$_0('Index: ' + index);
    return EmptyIterator_instance;
  };
  var EmptyList_instance;
  function EmptyList_getInstance() {
    if (EmptyList_instance == null)
      new EmptyList();
    return EmptyList_instance;
  }
  function EmptyIterator() {
  }
  protoOf(EmptyIterator).f = function () {
    return false;
  };
  protoOf(EmptyIterator).r1 = function () {
    return false;
  };
  protoOf(EmptyIterator).s1 = function () {
    return 0;
  };
  protoOf(EmptyIterator).g = function () {
    throw NoSuchElementException_init_$Create$();
  };
  protoOf(EmptyIterator).t1 = function () {
    throw NoSuchElementException_init_$Create$();
  };
  var EmptyIterator_instance;
  function EmptyIterator_getInstance() {
    return EmptyIterator_instance;
  }
  function optimizeReadOnlyList(_this__u8e3s4) {
    switch (_this__u8e3s4.j()) {
      case 0:
        return emptyList();
      case 1:
        return listOf(_this__u8e3s4.i(0));
      default:
        return _this__u8e3s4;
    }
  }
  function throwCountOverflow() {
    throw ArithmeticException_init_$Create$_0('Count overflow has happened.');
  }
  function throwIndexOverflow() {
    throw ArithmeticException_init_$Create$_0('Index overflow has happened.');
  }
  function collectionSizeOrDefault(_this__u8e3s4, default_0) {
    var tmp;
    if (isInterface(_this__u8e3s4, Collection)) {
      tmp = _this__u8e3s4.j();
    } else {
      tmp = default_0;
    }
    return tmp;
  }
  function addAll(_this__u8e3s4, elements) {
    if (isInterface(elements, Collection))
      return _this__u8e3s4.n(elements);
    else {
      var result = false;
      var _iterator__ex2g4s = elements.e();
      while (_iterator__ex2g4s.f()) {
        var item = _iterator__ex2g4s.g();
        if (_this__u8e3s4.o(item))
          result = true;
      }
      return result;
    }
  }
  function IntIterator() {
  }
  protoOf(IntIterator).g = function () {
    return this.t2();
  };
  function enumEntries(entries) {
    return new EnumEntriesList(entries);
  }
  function EnumEntriesList(entries) {
    AbstractList.call(this);
    this.u2_1 = entries;
  }
  protoOf(EnumEntriesList).j = function () {
    return this.u2_1.length;
  };
  protoOf(EnumEntriesList).i = function (index) {
    Companion_instance_2.a2(index, this.u2_1.length);
    return this.u2_1[index];
  };
  protoOf(EnumEntriesList).v2 = function (element) {
    if (element === null)
      return -1;
    var ordinal = element.v_1;
    var target = getOrNull(this.u2_1, ordinal);
    return target === element ? ordinal : -1;
  };
  protoOf(EnumEntriesList).w2 = function (element) {
    return this.v2(element);
  };
  protoOf(EnumEntriesList).s = function (element) {
    if (!(element instanceof Enum))
      return -1;
    return this.w2(element instanceof Enum ? element : THROW_CCE());
  };
  function getProgressionLastElement(start, end, step) {
    var tmp;
    if (step > 0) {
      tmp = start >= end ? end : end - differenceModulo(end, start, step) | 0;
    } else if (step < 0) {
      tmp = start <= end ? end : end + differenceModulo(start, end, -step | 0) | 0;
    } else {
      throw IllegalArgumentException_init_$Create$_0('Step is zero.');
    }
    return tmp;
  }
  function differenceModulo(a, b, c) {
    return mod(mod(a, c) - mod(b, c) | 0, c);
  }
  function mod(a, b) {
    var mod = a % b | 0;
    return mod >= 0 ? mod : mod + b | 0;
  }
  function Default() {
    Default_instance = this;
    Random.call(this);
    this.x2_1 = defaultPlatformRandom();
  }
  protoOf(Default).y2 = function (bitCount) {
    return this.x2_1.y2(bitCount);
  };
  protoOf(Default).t2 = function () {
    return this.x2_1.t2();
  };
  protoOf(Default).z2 = function (until) {
    return this.x2_1.z2(until);
  };
  protoOf(Default).a3 = function (from, until) {
    return this.x2_1.a3(from, until);
  };
  var Default_instance;
  function Default_getInstance() {
    if (Default_instance == null)
      new Default();
    return Default_instance;
  }
  function Random() {
    Default_getInstance();
  }
  protoOf(Random).t2 = function () {
    return this.y2(32);
  };
  protoOf(Random).z2 = function (until) {
    return this.a3(0, until);
  };
  protoOf(Random).a3 = function (from, until) {
    checkRangeBounds(from, until);
    var n = until - from | 0;
    if (n > 0 || n === -2147483648) {
      var tmp;
      if ((n & (-n | 0)) === n) {
        var bitCount = fastLog2(n);
        tmp = this.y2(bitCount);
      } else {
        var v;
        do {
          var bits = this.t2() >>> 1 | 0;
          v = bits % n | 0;
        }
         while (((bits - v | 0) + (n - 1 | 0) | 0) < 0);
        tmp = v;
      }
      var rnd = tmp;
      return from + rnd | 0;
    } else {
      while (true) {
        var rnd_0 = this.t2();
        if (from <= rnd_0 ? rnd_0 < until : false)
          return rnd_0;
      }
    }
  };
  function Random_0(seed) {
    return XorWowRandom_init_$Create$(convertToInt(seed), convertToInt(shiftRight(seed, 32)));
  }
  function checkRangeBounds(from, until) {
    // Inline function 'kotlin.require' call
    if (!(until > from)) {
      var message = boundsErrorMessage(from, until);
      throw IllegalArgumentException_init_$Create$_0(toString_1(message));
    }
    return Unit_instance;
  }
  function fastLog2(value) {
    // Inline function 'kotlin.countLeadingZeroBits' call
    return 31 - clz32(value) | 0;
  }
  function boundsErrorMessage(from, until) {
    return 'Random range is empty: [' + toString_1(from) + ', ' + toString_1(until) + ').';
  }
  function Random_1(seed) {
    return XorWowRandom_init_$Create$(seed, seed >> 31);
  }
  function takeUpperBits(_this__u8e3s4, bitCount) {
    return (_this__u8e3s4 >>> (32 - bitCount | 0) | 0) & (-bitCount | 0) >> 31;
  }
  function XorWowRandom_init_$Init$(seed1, seed2, $this) {
    XorWowRandom.call($this, seed1, seed2, 0, 0, ~seed1, seed1 << 10 ^ (seed2 >>> 4 | 0));
    return $this;
  }
  function XorWowRandom_init_$Create$(seed1, seed2) {
    return XorWowRandom_init_$Init$(seed1, seed2, objectCreate(protoOf(XorWowRandom)));
  }
  function checkInvariants($this) {
    // Inline function 'kotlin.require' call
    if (!!(($this.b3_1 | $this.c3_1 | $this.d3_1 | $this.e3_1 | $this.f3_1) === 0)) {
      var message = 'Initial state must have at least one non-zero element.';
      throw IllegalArgumentException_init_$Create$_0(toString_1(message));
    }
  }
  function Companion_3() {
    Companion_instance_3 = this;
    this.h3_1 = new Long(0, 0);
  }
  var Companion_instance_3;
  function Companion_getInstance_3() {
    if (Companion_instance_3 == null)
      new Companion_3();
    return Companion_instance_3;
  }
  function XorWowRandom(x, y, z, w, v, addend) {
    Companion_getInstance_3();
    Random.call(this);
    this.b3_1 = x;
    this.c3_1 = y;
    this.d3_1 = z;
    this.e3_1 = w;
    this.f3_1 = v;
    this.g3_1 = addend;
    checkInvariants(this);
    // Inline function 'kotlin.repeat' call
    var inductionVariable = 0;
    if (inductionVariable < 64)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        this.t2();
      }
       while (inductionVariable < 64);
  }
  protoOf(XorWowRandom).t2 = function () {
    var t = this.b3_1;
    t = t ^ (t >>> 2 | 0);
    this.b3_1 = this.c3_1;
    this.c3_1 = this.d3_1;
    this.d3_1 = this.e3_1;
    var v0 = this.f3_1;
    this.e3_1 = v0;
    t = t ^ t << 1 ^ v0 ^ v0 << 4;
    this.f3_1 = t;
    this.g3_1 = this.g3_1 + 362437 | 0;
    return t + this.g3_1 | 0;
  };
  protoOf(XorWowRandom).y2 = function (bitCount) {
    return takeUpperBits(this.t2(), bitCount);
  };
  function Companion_4() {
    Companion_instance_4 = this;
    this.p_1 = new IntRange(1, 0);
  }
  var Companion_instance_4;
  function Companion_getInstance_4() {
    if (Companion_instance_4 == null)
      new Companion_4();
    return Companion_instance_4;
  }
  function IntRange(start, endInclusive) {
    Companion_getInstance_4();
    IntProgression.call(this, start, endInclusive, 1);
  }
  protoOf(IntRange).l3 = function () {
    return this.m3_1;
  };
  protoOf(IntRange).p3 = function () {
    return this.n3_1;
  };
  protoOf(IntRange).h = function () {
    return this.m3_1 > this.n3_1;
  };
  protoOf(IntRange).equals = function (other) {
    var tmp;
    if (other instanceof IntRange) {
      tmp = this.h() && other.h() || (this.m3_1 === other.m3_1 && this.n3_1 === other.n3_1);
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(IntRange).toString = function () {
    return '' + this.m3_1 + '..' + this.n3_1;
  };
  function IntProgressionIterator(first, last, step) {
    IntIterator.call(this);
    this.q3_1 = step;
    this.r3_1 = last;
    this.s3_1 = this.q3_1 > 0 ? first <= last : first >= last;
    this.t3_1 = this.s3_1 ? first : this.r3_1;
  }
  protoOf(IntProgressionIterator).f = function () {
    return this.s3_1;
  };
  protoOf(IntProgressionIterator).t2 = function () {
    var value = this.t3_1;
    if (value === this.r3_1) {
      if (!this.s3_1)
        throw NoSuchElementException_init_$Create$();
      this.s3_1 = false;
    } else {
      this.t3_1 = this.t3_1 + this.q3_1 | 0;
    }
    return value;
  };
  function Companion_5() {
  }
  protoOf(Companion_5).q = function (rangeStart, rangeEnd, step) {
    return new IntProgression(rangeStart, rangeEnd, step);
  };
  var Companion_instance_5;
  function Companion_getInstance_5() {
    return Companion_instance_5;
  }
  function IntProgression(start, endInclusive, step) {
    if (step === 0)
      throw IllegalArgumentException_init_$Create$_0('Step must be non-zero.');
    if (step === -2147483648)
      throw IllegalArgumentException_init_$Create$_0('Step must be greater than Int.MIN_VALUE to avoid overflow on negation.');
    this.m3_1 = start;
    this.n3_1 = getProgressionLastElement(start, endInclusive, step);
    this.o3_1 = step;
  }
  protoOf(IntProgression).e = function () {
    return new IntProgressionIterator(this.m3_1, this.n3_1, this.o3_1);
  };
  protoOf(IntProgression).h = function () {
    return this.o3_1 > 0 ? this.m3_1 > this.n3_1 : this.m3_1 < this.n3_1;
  };
  protoOf(IntProgression).equals = function (other) {
    var tmp;
    if (other instanceof IntProgression) {
      tmp = this.h() && other.h() || (this.m3_1 === other.m3_1 && this.n3_1 === other.n3_1 && this.o3_1 === other.o3_1);
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(IntProgression).toString = function () {
    return this.o3_1 > 0 ? '' + this.m3_1 + '..' + this.n3_1 + ' step ' + this.o3_1 : '' + this.m3_1 + ' downTo ' + this.n3_1 + ' step ' + (-this.o3_1 | 0);
  };
  function appendElement(_this__u8e3s4, element, transform) {
    if (!(transform == null))
      _this__u8e3s4.d(transform(element));
    else {
      if (element == null ? true : isCharSequence(element))
        _this__u8e3s4.d(element);
      else {
        if (element instanceof Char)
          _this__u8e3s4.f2(element.u3_1);
        else {
          _this__u8e3s4.d(toString_1(element));
        }
      }
    }
  }
  function equals_0(_this__u8e3s4, other, ignoreCase) {
    ignoreCase = ignoreCase === VOID ? false : ignoreCase;
    if (_this__u8e3s4 === other)
      return true;
    if (!ignoreCase)
      return false;
    var thisUpper = uppercaseChar(_this__u8e3s4);
    var otherUpper = uppercaseChar(other);
    var tmp;
    if (thisUpper === otherUpper) {
      tmp = true;
    } else {
      // Inline function 'kotlin.text.lowercaseChar' call
      // Inline function 'kotlin.text.lowercase' call
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      var tmp$ret$2 = toString(thisUpper).toLowerCase();
      var tmp_0 = charCodeAt(tmp$ret$2, 0);
      // Inline function 'kotlin.text.lowercaseChar' call
      // Inline function 'kotlin.text.lowercase' call
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      var tmp$ret$6 = toString(otherUpper).toLowerCase();
      tmp = tmp_0 === charCodeAt(tmp$ret$6, 0);
    }
    return tmp;
  }
  function toIntOrNull(_this__u8e3s4) {
    return toIntOrNull_0(_this__u8e3s4, 10);
  }
  function toLongOrNull(_this__u8e3s4) {
    return toLongOrNull_0(_this__u8e3s4, 10);
  }
  function toIntOrNull_0(_this__u8e3s4, radix) {
    checkRadix(radix);
    var length = _this__u8e3s4.length;
    if (length === 0)
      return null;
    var start;
    var isNegative;
    var limit;
    var firstChar = charCodeAt(_this__u8e3s4, 0);
    if (Char__compareTo_impl_ypi4mb(firstChar, _Char___init__impl__6a9atx(48)) < 0) {
      if (length === 1)
        return null;
      start = 1;
      if (firstChar === _Char___init__impl__6a9atx(45)) {
        isNegative = true;
        limit = -2147483648;
      } else if (firstChar === _Char___init__impl__6a9atx(43)) {
        isNegative = false;
        limit = -2147483647;
      } else
        return null;
    } else {
      start = 0;
      isNegative = false;
      limit = -2147483647;
    }
    var limitForMaxRadix = -59652323;
    var limitBeforeMul = limitForMaxRadix;
    var result = 0;
    var inductionVariable = start;
    if (inductionVariable < length)
      do {
        var i = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var digit = digitOf(charCodeAt(_this__u8e3s4, i), radix);
        if (digit < 0)
          return null;
        if (result < limitBeforeMul) {
          if (limitBeforeMul === limitForMaxRadix) {
            limitBeforeMul = limit / radix | 0;
            if (result < limitBeforeMul) {
              return null;
            }
          } else {
            return null;
          }
        }
        result = imul(result, radix);
        if (result < (limit + digit | 0))
          return null;
        result = result - digit | 0;
      }
       while (inductionVariable < length);
    return isNegative ? result : -result | 0;
  }
  function toLongOrNull_0(_this__u8e3s4, radix) {
    checkRadix(radix);
    var length = _this__u8e3s4.length;
    if (length === 0)
      return null;
    var start;
    var isNegative;
    var limit;
    var firstChar = charCodeAt(_this__u8e3s4, 0);
    if (Char__compareTo_impl_ypi4mb(firstChar, _Char___init__impl__6a9atx(48)) < 0) {
      if (length === 1)
        return null;
      start = 1;
      if (firstChar === _Char___init__impl__6a9atx(45)) {
        isNegative = true;
        limit = new Long(0, -2147483648);
      } else if (firstChar === _Char___init__impl__6a9atx(43)) {
        isNegative = false;
        limit = new Long(1, -2147483648);
      } else
        return null;
    } else {
      start = 0;
      isNegative = false;
      limit = new Long(1, -2147483648);
    }
    // Inline function 'kotlin.Long.div' call
    var this_0 = new Long(1, -2147483648);
    var limitForMaxRadix = divide(this_0, fromInt(36));
    var limitBeforeMul = limitForMaxRadix;
    var result = new Long(0, 0);
    var inductionVariable = start;
    if (inductionVariable < length)
      do {
        var i = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var digit = digitOf(charCodeAt(_this__u8e3s4, i), radix);
        if (digit < 0)
          return null;
        if (compare(result, limitBeforeMul) < 0) {
          if (equalsLong(limitBeforeMul, limitForMaxRadix)) {
            // Inline function 'kotlin.Long.div' call
            var this_1 = limit;
            limitBeforeMul = divide(this_1, fromInt(radix));
            if (compare(result, limitBeforeMul) < 0) {
              return null;
            }
          } else {
            return null;
          }
        }
        // Inline function 'kotlin.Long.times' call
        var this_2 = result;
        result = multiply(this_2, fromInt(radix));
        var tmp = result;
        // Inline function 'kotlin.Long.plus' call
        var this_3 = limit;
        var tmp$ret$3 = add(this_3, fromInt(digit));
        if (compare(tmp, tmp$ret$3) < 0)
          return null;
        // Inline function 'kotlin.Long.minus' call
        var this_4 = result;
        result = subtract(this_4, fromInt(digit));
      }
       while (inductionVariable < length);
    return isNegative ? result : negate(result);
  }
  function numberFormatError(input) {
    throw NumberFormatException_init_$Create$_0("Invalid number format: '" + input + "'");
  }
  function split(_this__u8e3s4, delimiters, ignoreCase, limit) {
    ignoreCase = ignoreCase === VOID ? false : ignoreCase;
    limit = limit === VOID ? 0 : limit;
    if (delimiters.length === 1) {
      return split_0(_this__u8e3s4, toString(delimiters[0]), ignoreCase, limit);
    }
    // Inline function 'kotlin.collections.map' call
    var this_0 = asIterable(rangesDelimitedBy(_this__u8e3s4, delimiters, VOID, ignoreCase, limit));
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
    var _iterator__ex2g4s = this_0.e();
    while (_iterator__ex2g4s.f()) {
      var item = _iterator__ex2g4s.g();
      var tmp$ret$0 = substring_0(_this__u8e3s4, item);
      destination.o(tmp$ret$0);
    }
    return destination;
  }
  function toBooleanStrict(_this__u8e3s4) {
    var tmp;
    switch (_this__u8e3s4) {
      case 'true':
        tmp = true;
        break;
      case 'false':
        tmp = false;
        break;
      default:
        throw IllegalArgumentException_init_$Create$_0("The string doesn't represent a boolean value: " + _this__u8e3s4);
    }
    return tmp;
  }
  function split_0(_this__u8e3s4, delimiter, ignoreCase, limit) {
    requireNonNegativeLimit(limit);
    var currentOffset = 0;
    var nextIndex = indexOf(_this__u8e3s4, delimiter, currentOffset, ignoreCase);
    if (nextIndex === -1 || limit === 1) {
      return listOf(toString_1(_this__u8e3s4));
    }
    var isLimited = limit > 0;
    var result = ArrayList_init_$Create$_0(isLimited ? coerceAtMost(limit, 10) : 10);
    $l$loop: do {
      var tmp2 = currentOffset;
      // Inline function 'kotlin.text.substring' call
      var endIndex = nextIndex;
      var tmp$ret$0 = toString_1(charSequenceSubSequence(_this__u8e3s4, tmp2, endIndex));
      result.o(tmp$ret$0);
      currentOffset = nextIndex + delimiter.length | 0;
      if (isLimited && result.j() === (limit - 1 | 0))
        break $l$loop;
      nextIndex = indexOf(_this__u8e3s4, delimiter, currentOffset, ignoreCase);
    }
     while (!(nextIndex === -1));
    var tmp2_0 = currentOffset;
    // Inline function 'kotlin.text.substring' call
    var endIndex_0 = charSequenceLength(_this__u8e3s4);
    var tmp$ret$1 = toString_1(charSequenceSubSequence(_this__u8e3s4, tmp2_0, endIndex_0));
    result.o(tmp$ret$1);
    return result;
  }
  function rangesDelimitedBy(_this__u8e3s4, delimiters, startIndex, ignoreCase, limit) {
    startIndex = startIndex === VOID ? 0 : startIndex;
    ignoreCase = ignoreCase === VOID ? false : ignoreCase;
    limit = limit === VOID ? 0 : limit;
    requireNonNegativeLimit(limit);
    return new DelimitedRangesSequence(_this__u8e3s4, startIndex, limit, rangesDelimitedBy$lambda(delimiters, ignoreCase));
  }
  function substring_0(_this__u8e3s4, range) {
    return toString_1(charSequenceSubSequence(_this__u8e3s4, range.l3(), range.p3() + 1 | 0));
  }
  function requireNonNegativeLimit(limit) {
    // Inline function 'kotlin.require' call
    if (!(limit >= 0)) {
      var message = 'Limit must be non-negative, but was ' + limit;
      throw IllegalArgumentException_init_$Create$_0(toString_1(message));
    }
    return Unit_instance;
  }
  function indexOf(_this__u8e3s4, string, startIndex, ignoreCase) {
    startIndex = startIndex === VOID ? 0 : startIndex;
    ignoreCase = ignoreCase === VOID ? false : ignoreCase;
    var tmp;
    var tmp_0;
    if (ignoreCase) {
      tmp_0 = true;
    } else {
      tmp_0 = !(typeof _this__u8e3s4 === 'string');
    }
    if (tmp_0) {
      tmp = indexOf_0(_this__u8e3s4, string, startIndex, charSequenceLength(_this__u8e3s4), ignoreCase);
    } else {
      // Inline function 'kotlin.text.nativeIndexOf' call
      // Inline function 'kotlin.js.asDynamic' call
      tmp = _this__u8e3s4.indexOf(string, startIndex);
    }
    return tmp;
  }
  function calcNext($this) {
    if ($this.x3_1 < 0) {
      $this.v3_1 = 0;
      $this.y3_1 = null;
    } else {
      var tmp;
      var tmp_0;
      if ($this.a4_1.d4_1 > 0) {
        $this.z3_1 = $this.z3_1 + 1 | 0;
        tmp_0 = $this.z3_1 >= $this.a4_1.d4_1;
      } else {
        tmp_0 = false;
      }
      if (tmp_0) {
        tmp = true;
      } else {
        tmp = $this.x3_1 > charSequenceLength($this.a4_1.b4_1);
      }
      if (tmp) {
        $this.y3_1 = numberRangeToNumber($this.w3_1, get_lastIndex_0($this.a4_1.b4_1));
        $this.x3_1 = -1;
      } else {
        var match = $this.a4_1.e4_1($this.a4_1.b4_1, $this.x3_1);
        if (match == null) {
          $this.y3_1 = numberRangeToNumber($this.w3_1, get_lastIndex_0($this.a4_1.b4_1));
          $this.x3_1 = -1;
        } else {
          var index = match.h4();
          var length = match.i4();
          $this.y3_1 = until($this.w3_1, index);
          $this.w3_1 = index + length | 0;
          $this.x3_1 = $this.w3_1 + (length === 0 ? 1 : 0) | 0;
        }
      }
      $this.v3_1 = 1;
    }
  }
  function DelimitedRangesSequence$iterator$1(this$0) {
    this.a4_1 = this$0;
    this.v3_1 = -1;
    this.w3_1 = coerceIn(this$0.c4_1, 0, charSequenceLength(this$0.b4_1));
    this.x3_1 = this.w3_1;
    this.y3_1 = null;
    this.z3_1 = 0;
  }
  protoOf(DelimitedRangesSequence$iterator$1).g = function () {
    if (this.v3_1 === -1) {
      calcNext(this);
    }
    if (this.v3_1 === 0)
      throw NoSuchElementException_init_$Create$();
    var tmp = this.y3_1;
    var result = tmp instanceof IntRange ? tmp : THROW_CCE();
    this.y3_1 = null;
    this.v3_1 = -1;
    return result;
  };
  protoOf(DelimitedRangesSequence$iterator$1).f = function () {
    if (this.v3_1 === -1) {
      calcNext(this);
    }
    return this.v3_1 === 1;
  };
  function DelimitedRangesSequence(input, startIndex, limit, getNextMatch) {
    this.b4_1 = input;
    this.c4_1 = startIndex;
    this.d4_1 = limit;
    this.e4_1 = getNextMatch;
  }
  protoOf(DelimitedRangesSequence).e = function () {
    return new DelimitedRangesSequence$iterator$1(this);
  };
  function indexOfAny(_this__u8e3s4, chars, startIndex, ignoreCase) {
    startIndex = startIndex === VOID ? 0 : startIndex;
    ignoreCase = ignoreCase === VOID ? false : ignoreCase;
    var tmp;
    if (!ignoreCase && chars.length === 1) {
      tmp = typeof _this__u8e3s4 === 'string';
    } else {
      tmp = false;
    }
    if (tmp) {
      var char = single(chars);
      // Inline function 'kotlin.text.nativeIndexOf' call
      // Inline function 'kotlin.text.nativeIndexOf' call
      var str = toString(char);
      // Inline function 'kotlin.js.asDynamic' call
      return _this__u8e3s4.indexOf(str, startIndex);
    }
    var inductionVariable = coerceAtLeast(startIndex, 0);
    var last = get_lastIndex_0(_this__u8e3s4);
    if (inductionVariable <= last)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var charAtIndex = charSequenceGet(_this__u8e3s4, index);
        var tmp$ret$4;
        $l$block: {
          // Inline function 'kotlin.collections.any' call
          var inductionVariable_0 = 0;
          var last_0 = chars.length;
          while (inductionVariable_0 < last_0) {
            var element = chars[inductionVariable_0];
            inductionVariable_0 = inductionVariable_0 + 1 | 0;
            if (equals_0(element, charAtIndex, ignoreCase)) {
              tmp$ret$4 = true;
              break $l$block;
            }
          }
          tmp$ret$4 = false;
        }
        if (tmp$ret$4)
          return index;
      }
       while (!(index === last));
    return -1;
  }
  function indexOf_0(_this__u8e3s4, other, startIndex, endIndex, ignoreCase, last) {
    last = last === VOID ? false : last;
    var indices = !last ? numberRangeToNumber(coerceAtLeast(startIndex, 0), coerceAtMost(endIndex, charSequenceLength(_this__u8e3s4))) : downTo(coerceAtMost(startIndex, get_lastIndex_0(_this__u8e3s4)), coerceAtLeast(endIndex, 0));
    var tmp;
    if (typeof _this__u8e3s4 === 'string') {
      tmp = typeof other === 'string';
    } else {
      tmp = false;
    }
    if (tmp) {
      var inductionVariable = indices.m3_1;
      var last_0 = indices.n3_1;
      var step = indices.o3_1;
      if (step > 0 && inductionVariable <= last_0 || (step < 0 && last_0 <= inductionVariable))
        do {
          var index = inductionVariable;
          inductionVariable = inductionVariable + step | 0;
          if (regionMatches(other, 0, _this__u8e3s4, index, other.length, ignoreCase))
            return index;
        }
         while (!(index === last_0));
    } else {
      var inductionVariable_0 = indices.m3_1;
      var last_1 = indices.n3_1;
      var step_0 = indices.o3_1;
      if (step_0 > 0 && inductionVariable_0 <= last_1 || (step_0 < 0 && last_1 <= inductionVariable_0))
        do {
          var index_0 = inductionVariable_0;
          inductionVariable_0 = inductionVariable_0 + step_0 | 0;
          if (regionMatchesImpl(other, 0, _this__u8e3s4, index_0, charSequenceLength(other), ignoreCase))
            return index_0;
        }
         while (!(index_0 === last_1));
    }
    return -1;
  }
  function get_lastIndex_0(_this__u8e3s4) {
    return charSequenceLength(_this__u8e3s4) - 1 | 0;
  }
  function regionMatchesImpl(_this__u8e3s4, thisOffset, other, otherOffset, length, ignoreCase) {
    if (otherOffset < 0 || thisOffset < 0 || thisOffset > (charSequenceLength(_this__u8e3s4) - length | 0) || otherOffset > (charSequenceLength(other) - length | 0)) {
      return false;
    }
    var inductionVariable = 0;
    if (inductionVariable < length)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        if (!equals_0(charSequenceGet(_this__u8e3s4, thisOffset + index | 0), charSequenceGet(other, otherOffset + index | 0), ignoreCase))
          return false;
      }
       while (inductionVariable < length);
    return true;
  }
  function rangesDelimitedBy$lambda($delimiters, $ignoreCase) {
    return function ($this$DelimitedRangesSequence, currentIndex) {
      // Inline function 'kotlin.let' call
      var it = indexOfAny($this$DelimitedRangesSequence, $delimiters, currentIndex, $ignoreCase);
      return it < 0 ? null : to(it, 1);
    };
  }
  function _Result___init__impl__xyqfz8(value) {
    return value;
  }
  function _Result___get_value__impl__bjfvqg($this) {
    return $this;
  }
  function _Result___get_isFailure__impl__jpiriv($this) {
    var tmp = _Result___get_value__impl__bjfvqg($this);
    return tmp instanceof Failure;
  }
  function Companion_6() {
  }
  var Companion_instance_6;
  function Companion_getInstance_6() {
    return Companion_instance_6;
  }
  function Failure(exception) {
    this.j4_1 = exception;
  }
  protoOf(Failure).equals = function (other) {
    var tmp;
    if (other instanceof Failure) {
      tmp = equals(this.j4_1, other.j4_1);
    } else {
      tmp = false;
    }
    return tmp;
  };
  protoOf(Failure).toString = function () {
    return 'Failure(' + this.j4_1.toString() + ')';
  };
  function createFailure(exception) {
    return new Failure(exception);
  }
  function Pair(first, second) {
    this.f4_1 = first;
    this.g4_1 = second;
  }
  protoOf(Pair).toString = function () {
    return '(' + toString_0(this.f4_1) + ', ' + toString_0(this.g4_1) + ')';
  };
  protoOf(Pair).h4 = function () {
    return this.f4_1;
  };
  protoOf(Pair).i4 = function () {
    return this.g4_1;
  };
  protoOf(Pair).equals = function (other) {
    if (this === other)
      return true;
    if (!(other instanceof Pair))
      return false;
    if (!equals(this.f4_1, other.f4_1))
      return false;
    if (!equals(this.g4_1, other.g4_1))
      return false;
    return true;
  };
  function to(_this__u8e3s4, that) {
    return new Pair(_this__u8e3s4, that);
  }
  //region block: init
  Companion_instance = new Companion();
  Unit_instance = new Unit();
  Companion_instance_2 = new Companion_2();
  EmptyIterator_instance = new EmptyIterator();
  Companion_instance_5 = new Companion_5();
  Companion_instance_6 = new Companion_6();
  //endregion
  //region block: exports
  _.$_$ = _.$_$ || {};
  _.$_$.a = VOID;
  _.$_$.b = ArrayList_init_$Create$_0;
  _.$_$.c = ArrayList_init_$Create$;
  _.$_$.d = StringBuilder_init_$Create$;
  _.$_$.e = IllegalArgumentException_init_$Create$_0;
  _.$_$.f = NoSuchElementException_init_$Create$;
  _.$_$.g = _Char___init__impl__6a9atx;
  _.$_$.h = Char__toInt_impl_vasixd;
  _.$_$.i = _Result___init__impl__xyqfz8;
  _.$_$.j = _Result___get_isFailure__impl__jpiriv;
  _.$_$.k = _Result___get_value__impl__bjfvqg;
  _.$_$.l = Companion_instance_6;
  _.$_$.m = Unit_instance;
  _.$_$.n = Collection;
  _.$_$.o = addAll;
  _.$_$.p = checkCountOverflow;
  _.$_$.q = checkIndexOverflow;
  _.$_$.r = collectionSizeOrDefault;
  _.$_$.s = dropLast;
  _.$_$.t = emptyList;
  _.$_$.u = joinToString_0;
  _.$_$.v = get_lastIndex;
  _.$_$.w = last;
  _.$_$.x = listOf;
  _.$_$.y = listOf_0;
  _.$_$.z = plus;
  _.$_$.a1 = toList;
  _.$_$.b1 = toMutableList;
  _.$_$.c1 = enumEntries;
  _.$_$.d1 = bitwiseXor;
  _.$_$.e1 = fromInt;
  _.$_$.f1 = multiply;
  _.$_$.g1 = charArrayOf;
  _.$_$.h1 = charCodeAt;
  _.$_$.i1 = charSequenceLength;
  _.$_$.j1 = compareTo;
  _.$_$.k1 = defineProp;
  _.$_$.l1 = equals;
  _.$_$.m1 = initMetadataForClass;
  _.$_$.n1 = initMetadataForCompanion;
  _.$_$.o1 = initMetadataForObject;
  _.$_$.p1 = isInterface;
  _.$_$.q1 = protoOf;
  _.$_$.r1 = toString_1;
  _.$_$.s1 = Random_0;
  _.$_$.t1 = coerceAtLeast;
  _.$_$.u1 = split;
  _.$_$.v1 = toBooleanStrict;
  _.$_$.w1 = toIntOrNull;
  _.$_$.x1 = toInt;
  _.$_$.y1 = toLongOrNull;
  _.$_$.z1 = Enum;
  _.$_$.a2 = Long;
  _.$_$.b2 = THROW_CCE;
  _.$_$.c2 = THROW_IAE;
  _.$_$.d2 = createFailure;
  _.$_$.e2 = noWhenBranchMatchedException;
  _.$_$.f2 = toString_0;
  _.$_$.g2 = to;
  //endregion
  return _;
}));

//# sourceMappingURL=kotlin-kotlin-stdlib.js.map
