# Verborum — Word Input Filtering (Android)

**Target location:** `verborum_android/docs/word-input-filtering.md`
**Related:** Android Development (`docs/android-development.md` §4), Frontend–Backend
Integration (backend repo, `docs/integration/frontend-backend-integration.md` §1, §4, §10),
Webapp Keyboard Input (KMP repo, `docs/word-input-keyboard-webapp.md`)

---

## 1. Goal

Restrict what a user can enter into word-form fields so that a stored surface only ever
contains characters valid for its language — without breaking any of the 19 supported scripts.

On Android the app keeps the **system keyboard**. Restriction happens by **filtering the text
field**, not by altering or disabling keys on the keyboard.

## 2. Why filter the field, not the keyboard

On Android the on-screen keyboard belongs to the user's chosen IME (Gboard, Samsung keyboard, a
Japanese/Chinese/Korean IME, and so on). The app cannot remove, disable, or repaint individual
keys — that surface is not ours to control.

What the app *can* do is reject disallowed characters as they arrive in the field:

- **View system:** an `InputFilter` on the `EditText`.
- **Compose:** validate inside `onValueChange` — when a change would introduce a disallowed
  character, either drop the change (return the previous value) or strip the offending
  characters before accepting.

Net effect: the user still sees a full keyboard, but only permitted characters land in the
field. This is the intended model — a **field-level filter**, not a modified keyboard.

## 3. What is allowed — per language, letters only

For every word type **except `FREE_TEXT`**, the allowed set for a field is the **letters of
that side's language, in both cases** — and nothing else.

Word types this rule covers:

```
NOUN  VERB  ADJECTIVE  ADVERB  PREPOSITION  PRONOUN
NUMERAL  CONJUNCTION  INTERJECTION  ARTICLE
```

(`NUMERAL` means the *word* — `three`, `drei`, `trois` — not digits, so no `0–9` is needed.)

None of `.` `-` `+` `=` `}` `{` `<` `>` `\` `|`, digits, or other punctuation appear in any
surface in the data model, so none are allowed for these types.

**"Which letters" is per-language.** The allowed set is driven by the language of the side being
edited (the `lang` value on that side). `LanguageGrammar` is already the single source of truth
for per-language behaviour (Android doc §4.3); the per-language allowed-character set belongs
there too, keyed by language.

**It is not "A–Z plus accents."** Surfaces span:

| Script | Languages | Notes |
|---|---|---|
| Latin (+ precomposed diacritics) | en, de, fr, es, it, pt, nl, tr, az, lt, pl | ł ß ş ğ ı ė ç ñ ã ä ö ü … must be allowed as letters |
| Cyrillic | uk, ru | |
| Greek | el | |
| Arabic | ar | RTL |
| Farsi | fa | RTL |
| Japanese | ja | kanji + hiragana + katakana |
| Chinese (Simplified) | zh | hanzi |
| Korean | ko | hangul |

A global A–Z allowlist would reject most of these languages. Use a **per-language allowlist**,
or a permissive Unicode-letter class combined with a **targeted blocklist** of the punctuation
and symbols you never want (see §7). The blocklist shape is simpler to maintain across nine-plus
scripts and still keeps out `+ = } { < > \ |`, control characters, and line breaks.

## 4. Space and apostrophe come from composition, not typing

Every non-letter character that appears in a stored surface in the knowledge base traces back to
**article composition**, not to what the user types:

- Article + word is composed into the surface: `der Apfel`, `lo studente`, Greek `ο άνθρωπος`
  — the **space** comes from composition.
- French/Italian elision: `l'eau` — the **apostrophe** comes from composition.

The article itself is **chip-selected** (the user picks the gender; the article follows), and
`extractBaseWord` strips it back off when editing (Android doc §4.3).

Consequence for the filter: the field the user actually types into is the **base word**, which
is letters only. The space and apostrophe are added afterward by composition, which the app
controls — they do not need to survive input filtering.

**Open question (decide from your data, not the docs):** whether a typed base word can itself
legitimately contain a space or a within-word apostrophe — a multi-word expression, or French
`aujourd'hui`. No knowledge-base example shows this. If your dictionaries contain such forms,
add space and/or apostrophe to the allowed set for the affected languages. The webapp keyboard
doc takes the more permissive stance here (it adds an apostrophe key); keep the two in step
(§8).

## 5. FREE_TEXT — no filter

For `FREE_TEXT` (the "type absent" case, Android doc §4.2), apply **no filter at all**. Free text
is arbitrary content in any script with any punctuation, and must pass through unchanged. The
per-language letters-only rule does not apply here.

## 6. Composing IMEs (ja / zh / ko) — validate on commit, not per keystroke

A per-character filter works cleanly for Latin, Cyrillic, Greek, Arabic, and Farsi. It is
**dangerous mid-composition** for Japanese and Chinese: those IMEs hold intermediate candidate
text in the field before the user commits, and a naïve per-character filter applied during
composition can drop or corrupt that candidate text.

Since these are exactly the scripts added in roadmap A1, treat them specially:

- Prefer validating **on commit** (when composition finishes) rather than on every keystroke for
  composing-IME languages.
- Test the filter against a real composing IME (a Japanese and a Chinese keyboard) before
  trusting it — this is Android input mechanics, not a Verborum-specific concern, and it is the
  most likely thing to misbehave.

## 7. Meta sub-fields — not all are typed, and two carry extra characters

The filter should be **field-aware**, because the typed meta fields differ from the surface:

- **Chip fields — never typed, never filtered:** `aux` (`haben`/`sein`) and `class`
  (`group1`/`group2`/`irregular`, `i`/`na`) are choice chips (Android doc §4.3, §8.1). They emit
  stored ASCII codes and never touch the keyboard.
- **Typed text fields:** `reading`, `plural`, `feminine`, `comparative`, `superlative`,
  `present`, `past`, `past3`, `participle`, `root`, `stem`, `measure`, `polite`.

Two typed meta fields need characters a bare letters-only rule would wrongly reject:

- **`reading` for Chinese (pinyin)** carries **tone marks** — `shū`, `mǎi`. The allowed set for
  that field must include tone-marked vowels, not just plain letters.
- **`root` for Arabic** is stored **with spaces between the letters** — `ك ت ب`. That field must
  allow the space, even though the surface's space comes from composition.

If your filter is one rule per language regardless of field, it has to be permissive enough to
cover these; a per-field rule is cleaner.

## 8. This is a cross-client contract

The Android field filter and the webapp's custom keyboard (`KeyboardLayout`) are **two
independent implementations of the same rule**: "which characters are typeable per language."
Android is native and shares no code with the KMP clients — nothing is shared between clients
except the contracts (Integration §1). So this is a **mirrored contract, not shared code**, and
the failure mode is drift: a character typeable on one client but rejected on the other, yielding
surfaces one client accepts and the other cannot.

Mitigation: treat the **per-language allowed-character set** as a documented contract item, and
keep Android's `LanguageGrammar` allow-set equal to the webapp keyboard's letter keys. See the
webapp doc for the auxiliary characters (space, apostrophe, hyphen) that keyboard adds — the
Android filter must make the same call for the same languages, or the two diverge.

> **Docs update needed.** The Integration doc (§4, §10) specifies the grammar fields and the
> meta JSON shape, but **not** the set of typeable characters per language. If input restriction
> is meant to be consistent across Android, web, and iOS, add a "typeable characters per
> language" section to `docs/integration/frontend-backend-integration.md` so all three clients
> implement one list rather than three.

## 9. Implementation checklist

- [x] Per-language allowed-letter set — lives in `LanguageScript` (Unicode blocks per `lang`),
      not `LanguageGrammar`: the script tables predate this work and `LanguageGrammar` is about
      grammatical forms, so the two stayed separate. `LanguageScript.allowsLetter` is the letter
      half of the rule.
- [x] Field filter — `WordInputFilter.apply(lang, wordType, fieldKey, text)`, called from
      `ScriptTextField.onValueChange` in `LanguageInputCard`.
- [x] `FREE_TEXT` bypasses the filter entirely.
- [x] Composing-IME languages (ja, zh, ko) validated on commit — the field drives a
      `TextFieldValue` and skips filtering while `composition != null`.
      **Not yet tested against a real IME** (§6's warning stands; needs a device pass with a
      Japanese and a Chinese keyboard).
- [x] Field-aware exceptions: `root` allows space (ar); chip fields (`aux`, `class`) are not
      filtered because they render as chips, not text fields. `reading` tone marks (zh) need no
      rule — precomposed `ū` is a Latin letter and Latin is already zh's composition alphabet;
      decomposed input passes as letter + combining mark.
- [ ] Allowed set kept equal to the webapp `KeyboardLayout` letter keys for each language
      (§8) — ideally via a shared contract in the Integration doc. **Open** — cross-repo, and the
      Integration doc still has no "typeable characters per language" section.
