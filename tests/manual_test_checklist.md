# 🎮 F!RST Game - Manual Testing Checklist

## 📋 Overview
This comprehensive checklist covers all possible scenarios and edge cases for the F!RST card game. Use this document to manually verify game behavior.

---

## 🎲 **Game Initialization Tests**

### ✅ Initial Setup
- [ ] Game loads without errors
- [ ] Main menu displays with F-I-R-S-T cards animation
- [ ] Language switching works (EN/RU)
- [ ] New Game button starts game properly

### ✅ Game Start
- [ ] Dice roll animation plays
- [ ] Both dice show different values (re-rolls if same)
- [ ] Higher roll determines first player
- [ ] First player indicator is correct
- [ ] Each player starts with 5 cards in hand
- [ ] Deck counters show 45 remaining for each player
- [ ] Timer starts counting

---

## 🃏 **Card Effects Tests**

### 🚫 **F (FORBID) Card**
- [ ] **Player F Card:**
  - [ ] Modal opens to choose letter to forbid
  - [ ] Can select any letter (F, I, R, S, T)
  - [ ] AI's F card gets highlighted with chosen letter badge
  - [ ] When AI plays forbidden letter, it goes to AI's discard (not space)
  - [ ] Forbid effect is cleared after triggering
  - [ ] Log message appears: "You forbade a letter for AI"

- [ ] **AI F Card:**
  - [ ] AI chooses letter automatically (preferably most common in player space)
  - [ ] Player's F card gets highlighted (no letter shown to player)
  - [ ] When player tries to play forbidden letter, it goes to discard
  - [ ] Card shakes and animates to discard pile
  - [ ] Forbid effect is cleared after triggering
  - [ ] Log message appears: "AI forbade a letter for you"

### ➕ **I (INCREASE) Card**
- [ ] **Player I Card:**
  - [ ] Player draws +1 card from deck
  - [ ] Hand count increases by 1
  - [ ] Deck counter decreases by 1
  - [ ] If hand exceeds 7 cards, excess go to discard
  - [ ] Log message appears: "You drew a card"

- [ ] **AI I Card:**
  - [ ] AI draws +1 card from deck
  - [ ] AI hand count increases by 1
  - [ ] AI deck counter decreases by 1
  - [ ] Log message appears: "AI drew a card"

### 🔄 **R (RECOVER) Card**
- [ ] **Player R Card:**
  - [ ] Modal opens showing available letters in discard
  - [ ] Only shows unique letters (no duplicates in choices)
  - [ ] Selected card moves from discard to hand
  - [ ] Discard pile count decreases
  - [ ] Hand count increases (unless at limit)
  - [ ] Log message appears: "You recovered [card] to hand"
  - [ ] If discard is empty, no modal appears (effect does nothing)

- [ ] **AI R Card:**
  - [ ] AI automatically recovers last card from discard
  - [ ] Card moves from AI discard to AI hand
  - [ ] Log message appears: "AI recovered a card to hand"
  - [ ] If AI discard is empty, effect does nothing

### 🎯 **S (STEAL) Card**
- [ ] **Player S Card:**
  - [ ] Modal opens showing opponent's space cards
  - [ ] Can select any card from opponent's space
  - [ ] Selected card moves from opponent space to opponent discard
  - [ ] Opponent's space updates (stack counter decreases)
  - [ ] Opponent's discard pile updates
  - [ ] Log message appears: "You moved opponent [card] to DISCARD"
  - [ ] If opponent space is empty, no modal appears

- [ ] **AI S Card:**
  - [ ] AI automatically steals last card from player space
  - [ ] Card moves from player space to player discard
  - [ ] Player's space updates visually
  - [ ] Log message appears: "AI stole your [card] to your DISCARD"
  - [ ] If player space is empty, effect does nothing

### 🪤 **T (TRAP) Card**
- [ ] **Player T Card:**
  - [ ] AI trap counter increases
  - [ ] No immediate effect on AI
  - [ ] Log message appears: "You used T: AI will discard 1 card next turn"
  - [ ] **Next AI turn:** AI automatically discards 1 random card at turn start
  - [ ] AI trap counter resets to 0
  - [ ] AI continues normal turn after discarding

- [ ] **AI T Card:**
  - [ ] Player trap counter increases
  - [ ] No immediate effect on player
  - [ ] Log message appears: "AI used T: you must discard 1 card next turn"
  - [ ] **Next player turn:** Modal opens to choose card to discard
  - [ ] Selected card goes to player discard
  - [ ] Player trap counter resets to 0
  - [ ] Player continues normal turn after discarding

---

## 🏆 **Win Condition Tests**

### 🎯 **F-I-R-S-T Sequence Win**
- [ ] Having exactly one of each letter (F,I,R,S,T) in space triggers win
- [ ] Win detection works with additional cards (e.g., F,F,I,R,S,T still wins)
- [ ] Win modal appears with correct message
- [ ] Timer stops when game ends
- [ ] "New Game" and "Menu" buttons work in win modal

### 🃏 **Five of a Kind Win**
- [ ] Having 5 or more of any single letter triggers win
- [ ] Works for all letter types (5×F, 5×I, 5×R, 5×S, 5×T)
- [ ] Win detection works with mixed cards (e.g., F,F,F,F,F,I still wins)
- [ ] Correct win message displays

### 💔 **Loss Conditions**
- [ ] **Deck Empty:** Trying to draw from empty deck triggers loss
- [ ] **Opponent Wins:** Opponent achieving win condition triggers loss
- [ ] Loss modal shows correct defeat message

---

## 📏 **Hand Limit Tests**

### 🎯 **Seven Card Limit**
- [ ] Hand never exceeds 7 cards visually
- [ ] Excess cards automatically go to discard
- [ ] Overflow message appears in log: "Hand overflow: [card] → DISCARD"
- [ ] Works when drawing multiple cards (I effect + regular draw)
- [ ] Works when recovering from discard with full hand

### 🔄 **Hand Management**
- [ ] Cards in hand are clickable only during player turn
- [ ] Hand cards show hover effects
- [ ] Hand scrolls horizontally if needed on small screens
- [ ] Hand cards scale properly on different screen sizes

---

## 🔄 **Turn Flow Tests**

### 🎯 **First Turn Special Rules**
- [ ] First player does NOT draw a card on their first turn
- [ ] Second player DOES draw a card on their first turn
- [ ] "First turn done" flag prevents future first turn skips

### 🎯 **Regular Turn Flow**
- [ ] **Turn Start:**
  - [ ] Player draws 1 card (if not first turn)
  - [ ] Trap effects trigger before normal turn actions
  - [ ] Turn highlight appears on correct player's space panel
  
- [ ] **Turn Actions:**
  - [ ] Player can click any card in hand to play
  - [ ] Card animates from hand to space
  - [ ] Card effect resolves immediately
  - [ ] Turn automatically ends after effect resolution
  
- [ ] **Turn End:**
  - [ ] Turn passes to opponent
  - [ ] Turn highlight switches
  - [ ] AI turn begins automatically (with delay)

### 🤖 **AI Turn Behavior**
- [ ] AI waits 800-1400ms before playing (random delay)
- [ ] AI prioritizes: S > R > F > T > I (if conditions met)
- [ ] AI card appears as "?" during animation
- [ ] AI card reveals actual letter when placed
- [ ] AI effects execute automatically (no user input needed)

---

## 🚫 **Forbid Mechanics Tests**

### 🎯 **Forbid Triggering**
- [ ] Forbidden card shakes when played
- [ ] AI's F card flashes red when forbid triggers
- [ ] Card flies to discard pile (not space)
- [ ] Forbid effect clears after triggering
- [ ] Turn still ends normally after forbid

### 🎯 **Forbid Visual Feedback**
- [ ] Active forbid shows glowing border on opponent's F card
- [ ] Player's forbid shows letter badge on AI's F card
- [ ] AI's forbid shows glow but no letter (hidden from player)

---

## 🪤 **Trap Mechanics Tests**

### 🎯 **Trap Timing (CRITICAL)**
- [ ] T card sets trap counter (does NOT immediately discard)
- [ ] Trap triggers at START of opponent's next turn
- [ ] Opponent discards BEFORE drawing phase
- [ ] Opponent continues normal turn after trap discard
- [ ] Multiple traps queue properly (one per turn)

### 🎯 **Trap Edge Cases**
- [ ] Trap with empty hand: trap counter resets, no discard
- [ ] Multiple traps: only one triggers per turn start
- [ ] Trap during AI turn: AI discards random card automatically
- [ ] Trap during player turn: player chooses card to discard

---

## 🎨 **UI/UX Tests**

### 📱 **Responsive Design**
- [ ] Game scales properly on mobile devices
- [ ] Cards remain readable at all sizes
- [ ] Touch interactions work on mobile
- [ ] Landscape/portrait mode switching works
- [ ] Hamburger menu appears on mobile

### 🎭 **Visual Effects**
- [ ] Card animations are smooth
- [ ] GSAP animations work without errors
- [ ] Hover effects work on desktop
- [ ] Touch effects work on mobile
- [ ] Forbid flash effects are visible
- [ ] Dice rolling animation is smooth

### 🌐 **Internationalization**
- [ ] Language switching updates all text immediately
- [ ] Russian translations are correct
- [ ] Rules text updates in both languages
- [ ] All UI elements translate properly

---

## 🔧 **Edge Cases & Error Handling**

### 🎯 **Empty Collections**
- [ ] R card with empty discard does nothing
- [ ] S card with empty opponent space does nothing
- [ ] I card with empty deck triggers loss
- [ ] AI handles empty collections gracefully

### 🎯 **Simultaneous Effects**
- [ ] Hand limit enforcement after multiple draws
- [ ] Win condition checked after each card played
- [ ] Trap + regular turn flow works correctly
- [ ] Forbid + trap interactions work properly

### 🎯 **Game State Integrity**
- [ ] Total cards always equal 100 (50 per player)
- [ ] Cards never duplicate or disappear
- [ ] Game state remains consistent after all actions
- [ ] Browser refresh resets game properly

---

## 🎯 **Performance Tests**

### ⚡ **Animation Performance**
- [ ] Game runs smoothly on low-end devices
- [ ] Animations don't cause lag or stuttering
- [ ] Multiple rapid actions don't break animations
- [ ] Memory usage remains stable during long games

### 🖥️ **Browser Compatibility**
- [ ] Chrome: All features work
- [ ] Firefox: All features work
- [ ] Safari: All features work
- [ ] Mobile browsers: All features work

---

## 📝 **Test Completion Checklist**

### Before Release:
- [ ] All basic gameplay tests pass
- [ ] All card effects work correctly
- [ ] All win/loss conditions function
- [ ] Trap timing follows rules exactly
- [ ] Forbid mechanics work as intended
- [ ] UI is responsive and polished
- [ ] No console errors appear
- [ ] Game is stable and performant

### Test Notes:
```
Date: ___________
Tester: ___________
Browser: ___________
Device: ___________

Issues Found:
1. ___________
2. ___________
3. ___________

Overall Status: [ ] PASS [ ] FAIL
```

---

## 🏁 **Completion Summary**

**Total Test Categories:** 11  
**Total Test Items:** 100+  
**Critical Path Items:** 25  

**Must-Pass Categories:**
1. Card Effects (especially T timing)
2. Win Conditions
3. Turn Flow
4. Trap Mechanics
5. Forbid Mechanics

This comprehensive test suite ensures the F!RST game implementation fully matches the specified rules and provides a polished user experience.