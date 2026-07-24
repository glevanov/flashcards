# Plan: Svenska Flashcards

## Goals

- Personal Android app for reviewing Swedish vocabulary with spaced repetition.
- Decks reused from `../swedish-study` (CSV files), bundled into the APK.
- Fully offline. Distributed via GitHub releases + Obtanium. No Play Store.

## Tech stack

| Choice | Rationale |
|---|---|
| Kotlin + Jetpack Compose (Material 3) | Native, Android-only target; no cross-platform overhead |
| Gradle (KTS) + Android Gradle Plugin | Builds fully from CLI; no Android Studio needed (Zed is the editor) |
| Room (SQLite) | On-device SRS state persistence |
| Android TTS (Swedish voice) | Built-in pronunciation, no network or extra deps |
| GitHub Actions | Builds signed release APKs attached to GitHub releases for Obtanium |

## Decisions (locked in)

1. **Decks are bundled as assets** (`app/src/main/assets/vocab/...`), copied from
   `../swedish-study/vocab/`. New vocab → copy CSVs → tag a release → Obtanium
   updates the app. No file-import UI in v1.
2. **TTS pronunciation is in v1** (🔊 button on cards, Swedish locale).
3. **No migration of `cards/state.json`** from the Python tool — phone starts fresh.
4. **Repo is public** on GitHub. Signing key lives in GitHub secrets (never committed).

## Data model

### Decks (static, bundled)

CSV files under `assets/vocab/`, format identical to `swedish-study`:

```csv
swedish,english,example
en fritid,free time,"På min fritid spelar jag fotboll."
```

- Header row optional (skipped if `row[0] == "swedish"`).
- Blank lines and rows starting with `#` are ignored.
- Deck name = path under `vocab/` without extension, e.g. `core/adjectives`,
  `rivstart/kapitel-01`. Folder structure becomes deck grouping in the UI.
- Card key = `<deck>::<swedish>` (stable identifier for SRS state).

### SRS state (dynamic, Room)

One row per card key:

| Field | Type | Meaning |
|---|---|---|
| `key` | String (PK) | `<deck>::<swedish>` |
| `box` | Int 1–6 | Leitner box |
| `due` | LocalDate | Next review date |
| `seen` / `correct` | Int | Lifetime counters |
| `isNew` | Boolean | Never reviewed yet |

### SRS algorithm (ported 1:1 from `cards/flashcards.py`)

- Leitner boxes 1–6 with intervals in days: `{1: 0, 2: 1, 3: 3, 4: 7, 5: 16, 6: 35}`.
- Correct answer → box + 1 (max 6); wrong answer → back to box 1.
- `due = today + interval[box]` after each review.
- A session = all due cards (shuffled) + up to *N* new cards (shuffled, `N`
  configurable, default 10). Skip leaves state untouched.

## App design

Three screens, single-activity Compose navigation:

### 1. Home / deck list

Mirrors `flashcards.py list-decks`:

```
Svenska Flashcards
─────────────────────────────────
[ Study all due · 23 due + 10 new ]
─────────────────────────────────
CORE
  adjectives          26 cards · 4 due · 12 new  ▓▓▓░░
  common-nouns        26 cards · 0 due · 26 new  ░░░░░
  ...
RIVSTART
  kapitel-01          26 cards · 2 due · 0 new   ▓▓▓▓░
```

- Tap deck → study session for that deck.
- "Study all" mixes due cards across every deck.
- Progress bar per deck = fraction of cards in boxes 3+ (or similar; see stats).
- Overflow menu → Stats screen, settings (new-cards-per-day, TTS on/off).

### 2. Study session

- Front of card: Swedish word/phrase, deck name in the corner, session
  progress (`12 / 40`).
- **Tap card to flip** (flip animation) → English translation + example sentence.
- After reveal, grade:
  - **Swipe right = knew it**, **swipe left = didn't**;
  - ✓ / ✗ buttons always visible as fallback.
- **🔊 button** speaks the Swedish (and optionally the example) via TTS.
- Toggle for **reverse mode** (English front → Swedish back) per session,
  equivalent to the Python tool's `--reverse`.
- Session ends with a summary: correct/total, accuracy %.

### 3. Stats

Per deck + global, mirroring `flashcards.py stats`:
- Cards total / new / learning / mastered.
- Box distribution (1 = hard … 6 = mastered) as horizontal bars.
- Lifetime accuracy.

## Roadmap

| Phase | Deliverable | Status |
|---|---|---|
| 0 | Fedora dev env: JDK, Android SDK cmdline-tools, adb to phone | ✅ done |
| 1 | Gradle scaffold, Compose "hello" installed on phone via adb | ✅ done |
| 2 | Deck assets + CSV parser + deck list screen | ✅ done |
| 3 | Study session: flip, swipe/buttons, Leitner engine + Room | ✅ done |
| 4 | TTS, reverse mode, stats screen, settings | ✅ done |
| 5 | GitHub Actions release pipeline + Obtanium end-to-end | ⬜ |

## Non-goals (v1)

- No accounts, sync, backup, or network features.
- No card editing/creation in the app (decks are edited in `swedish-study`).
- No Anki/CSV import UI.
- No Play Store listing, no tablet-specific layouts, no widget.
