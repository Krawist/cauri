# CLAUDE.md — Cauri

Context for Claude Code. Read this file in full before any task, then the reference document relevant to the
current ticket.

## The product in short

Cauri is a personal finance Android app for Cameroon, built to be deployed widely. It answers two questions: where
did my money go, and how do I keep more of it? It follows the four places money lives day to day: **cash**, **MTN
MoMo**, **Orange Money** and the **bank**.

Cash is most of the spending and the hardest part to track, so it comes first in every screen and every model. Digital
payments are detected from the operators' notifications and only need a confirmation. Cash is captured through
one-tap habits (moto, beignets…), reminders after visits to places, and regular cash counts that turn the gap
between the expected and the counted amount into expenses.

## Reference documents

The documents kept in Claude are the reference; they are written in French.

| Document                                  | Local copy        | Reference                                         |
|-------------------------------------------|-------------------|---------------------------------------------------|
| Product requirements (what and why)       | —                 | https://claude.ai/artifact/6ZMKPajYcTATWmgfoHCqin |
| MVP tickets (CAU-01 to CAU-51, 3 phases)  | `docs/tickets.md` | https://claude.ai/artifact/XE2X5itqGYCmyDifwjKm4e |
| Mockups, light and dark (canvas)          | —                 | https://claude.ai/artifact/7cuvPsaveMQEG1VCZh2qbP |

`docs/tickets.md` is the detailed English working copy: acceptance criteria, dependencies and the mockup of each
screen, named by its artboard title on the canvas.

Before implementing a screen, look at its mockup. Amounts, names and dates in the mockups are examples.

## Decisions taken (do not question them unless asked)

- **Local-first, no backend.** All data stays on the phone. The only network use planned is a downloadable catalogue
  of parsing templates and, later, an encrypted backup the user chooses.
- **Cash first.** The Cash account always exists. Its balance is estimated from the last cash count, withdrawals and
  cash expenses, and is always shown with "≈". A withdrawal is a transfer to the Cash account, never an expense.
- **Raw data is the source of truth.** Received notifications are stored as raw signals, and reminder decisions in a
  decision log. Parsed transactions and the trained model are projections that can be rebuilt.
- **Notifications, not SMS permissions.** `NotificationListenerService` only, never `READ_SMS` / `RECEIVE_SMS`
  (Play Store). Location is optional: the app must be useful without it.
- **Parsing templates are data, not code**, one per sender and message type. Operator message formats and sender
  package names are values to be checked on real phones, never invented.
- **Budget the money received, not the money expected.** Income is often variable; only money that has arrived is
  split between savings, fixed costs and budgets.
- **Reminders are decided by a contextual bandit** (linear Thompson sampling, three actions: notify now, add to the
  evening summary, skip), not by an LLM. Rules decide first; the bandit takes over once there is enough data. It is
  retrained at night on a 90-day window from the decision log. Guardrails (quiet hours, daily cap, no reminder at
  home or work) apply before it. Reward values live in a versioned configuration; the log stores the reward reason.
- **An LLM, when available on the device, never computes a number.** It may interpret free text or word a message;
  every amount comes from code.
- **Confirmation cards arrive pre-filled** with Cauri's proposal. ✓ or swipe right confirms, swipe left means
  "nothing spent", a tap opens the edit sheet. The proposed category and amount are stored next to the final ones.

## Stack and conventions

- Kotlin, Jetpack Compose, Material 3 under the Cauri theme. minSdk 29 (Android 10), compile/target 36. JDK 17.
- UDF architecture: `@HiltViewModel` + state in `StateFlow`, coroutines. Hilt (KSP), Room, DataStore and type-safe
  Navigation Compose (`@Serializable` routes).
- Navigation: each feature exposes its graph object (`HomeGraph`, `CashGraph`…) and a `NavGraphBuilder.xxxGraph(...)`
  extension; `:app` (`CauriApp.kt`) wires them. Features never navigate to each other directly: they take callbacks.
- Gradle Kotlin DSL, `gradle/libs.versions.toml` catalog, convention plugins in `build-logic/`:
  `cauri.android.application`, `cauri.android.library`, `cauri.android.compose`, `cauri.android.feature` (library +
  compose + Hilt + navigation + `:core:design`), `cauri.android.hilt`, `cauri.android.room` (schemas in `schemas/`),
  `cauri.jvm.library` (pure Kotlin).
- Kotlin sources in `src/main/kotlin` and `src/test/kotlin`. Type-safe module accessors:
  `implementation(projects.core.design)`.
- Pure business logic (parsing, matching, cash balance, reminder rules, bandit) in Kotlin without Android dependencies
  whenever possible, covered by JVM tests.
- Amounts: `Long` in minor units with the account's ISO 4217 currency (XAF has no decimals), always positive; the
  direction comes from the transaction type. Displayed with `formatFcfa()` (thin space U+202F as thousands separator).
- User-facing texts: `strings.xml` in French (default) **and** English (`values-en`). Never hard-coded text in a
  composable.
- Colours come from `CauriTheme.palette`, never raw values in screens. Gold is for cash and savings, coral for
  spending, green for income, lagoon for actions.
- Tests: JUnit 5 + Kotest assertions. Names start with `should`, one `@Nested inner class` per tested method,
  `// given`, `// when`, `// then`, `@ParameterizedTest` for variations. Fakes for repositories.
- Quality: `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` must pass before a ticket is done.
- Everything in English: code, identifiers, comments, KDoc, commit messages and documentation. Only the user-facing
  French strings stay in French.

### Modules

| Module                  | Content                                                   | Tickets        |
|-------------------------|-----------------------------------------------------------|----------------|
| `:app`                  | Application, navigation, launch screen, signing           | 01, 04, 51     |
| `:feature:onboarding`   | Profile, payment habits, categories, accounts, permissions | 06–11         |
| `:feature:home`         | Home screen, one-tap habits, cards to confirm             | 14, 25, 27     |
| `:feature:cash`         | Cash count, split of the gap                              | 15, 16         |
| `:feature:transactions` | Quick entry, edit sheet, activity                         | 26, 28, 30     |
| `:core:design`          | Theme, palette light and dark, glass surface, components  | 02, 03         |
| `:core:data`            | DataStore preferences, Room database                      | 05, 12, 13     |
| `:core:capture`         | Notification listening, raw signals, health check         | 18, 21, 24     |
| `:core:parsing`         | Message templates and parsing (pure Kotlin)               | 19, 20         |
| `:core:engine`          | Reminder rules, guardrails, bandit (pure Kotlin)          | 37–42          |

Places (CAU-33 to 36), budgets and goals (phase 3) will get their own modules when their tickets start.

### Design tokens

| Token     | Light     | Dark      | Usage                                |
|-----------|-----------|-----------|--------------------------------------|
| lagoon    | `#0F7B76` | `#2BB3A8` | Main actions, selection              |
| gold      | `#C9963F` | `#D9AE5F` | Cash, savings, goals                 |
| coral     | `#E2614B` | `#EE7A65` | Spending, overruns                   |
| green     | `#23774F` | `#7FD6A8` | Income (text)                        |
| pearl     | `#F6F2EA` | —         | Light background                     |
| night     | —         | `#0B1214` | Dark background                      |
| ink       | `#0E1A1C` | `#EEF3F2` | Main text                            |
| muted     | `#55666A` | `#93A3A5` | Secondary text                       |

iOS-inspired, modern look: translucent "glass" surfaces with a background blur (Haze) on capable phones, and an
opaque tinted fallback on the others (CAU-03). Inter font (to embed, CAU-02). Large amounts in bold with tabular
figures. Cards 20 to 28 radius, touch targets ≥ 44 dp.

## Progress

- [x] **Skeleton** — multi-module project, convention plugins, CI, debug/release signing, Hilt, type-safe navigation
  with placeholder screens, DataStore (`UserPreferencesRepository`: onboarding done or not), Room plugin wired in
  `:core:data` (the database class comes with its first entities in CAU-05), light and dark palettes, empty
  notification listener, French default and English locales.
- [ ] **CAU-01** — builds with the catalogue versions; ktlint, detekt, unit tests, debug and release builds pass
  locally; onboarding → home → back checked on a phone. CI workflow in `.github/workflows/ci.yml`. Left: CI green
  on GitHub once the repository is pushed, release signing once `keystore.properties` exists. Then CAU-02 and CAU-05.

Update this section at the end of each ticket.

## Way of working

- One ticket at a time, on a `cau-0xx-description` branch, with small, readable commits.
- Reread the ticket's acceptance criteria in `docs/tickets.md` before coding and check them at the end.
- If a product choice is not settled by the documents, ask rather than decide alone.
