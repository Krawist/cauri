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
| `:feature:settings`     | Settings: permission and battery statuses                 | 10, 11         |
| `:core:design`          | Theme, palette light and dark, glass surface, components  | 02, 03         |
| `:core:data`            | DataStore preferences, Room database                      | 05, 12, 13     |
| `:core:capture`         | Permissions, notification listening, raw signals, health  | 10, 18, 21, 24 |
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
  on GitHub once the repository is pushed, release signing once `keystore.properties` exists.
- [ ] **CAU-02** — palette of every mockup token (light coral text darkened to `#BE4834` for 4.5:1), Inter variable
  font embedded, type scale with tabular figures, `CauriRadius`, `CauriSpacing`, contrast unit test, preview catalogue
  (`ThemeCatalog.kt`). Checked on a phone in light mode only.
- [ ] **CAU-51** — logo coin (SVG masters in `core/design/logo/`, vector drawables), silhouette for notifications and
  themed icons, adaptive launcher icon, system splash handing off to the Compose launch screen (`LaunchDuration`,
  2 s). Launch screen and splash checked on a phone in light and dark mode; launcher icon not checked on a device.
- [ ] **CAU-03** — `GlassScreen` (background, content and overlay layers) and `GlassSurface` with Haze 1.6.10 (2.x
  needs Compose 1.12 and Kotlin 2.4); blur from Android 12 on phones not flagged low-RAM, decided once per process in
  `CauriApplication`, opaque tinted fallback otherwise. Checked on a Galaxy S23; smooth scrolling on an entry-level
  Tecno or Infinix not verified.
- [ ] **CAU-04** — floating glass `CauriBottomBar` (Accueil, Activité, « + », Objectifs, Analyses), shown on the tabs
  only; « + » opens `QuickEntrySheet` from `:feature:transactions`; Objectifs and Analyses are placeholders in `:app`
  until their phase 3 modules. Back from a tab returns to Accueil, then leaves the app (checked on a phone). The
  mockups' blurred shapes (`backdrop`, `screenBackdropShapes`) are drawn once behind transparent screens; the bar and
  « + » are raised with a `shadow` palette colour.
- [ ] **CAU-05** — `CauriDatabase` (Room, schema v1 in `core/data/schemas/`): accounts, categories, raw signals, places,
  visits, transactions, cash counts, decision log; internal entities and DAOs, provided by Hilt. The Cash account
  (id 1, XAF) is created with the database and a trigger refuses its deletion; a raw signal has at most one
  transaction (unique index). DAOs tested on an in-memory database with the bundled SQLite driver.
- [ ] **CAU-06** — profile step (`ProfileScreen`, `ProfileViewModel`): first name, occupation and income type, all
  three required and pointed out after a first « Continuer »; saved in DataStore (`UserProfile`) through
  `UserPreferencesRepository`, now an interface with `UserPreferencesRepositoryImpl`. Until CAU-07, the onboarding
  completes after this step. Checked on a phone.
- [ ] **CAU-07** — payment habits step (`HabitsScreen`, `HabitsViewModel`): main way of paying and cash in pocket,
  both required; amounts typed with `FcfaVisualTransformation`. Checked on a phone.
- [ ] **CAU-08** — categories step (`CategoriesScreen`): Cameroon pack (`PackCategory`, the six of the mockup kept by
  default, tontine as savings), user additions, at least one category required, « Continuer · n catégories ». The
  onboarding steps save nothing: they hand their answers to the graph-scoped `OnboardingViewModel` (kept in its
  `SavedStateHandle`), which saves the complete `UserProfile`, the first cash count and the categories after the last
  step. Steps share `OnboardingStep`. Checked on a phone, back navigation included.
- [ ] **CAU-09** — accounts step (`AccountsScreen`), now the last one: Cash always tracked, MTN MoMo and Orange Money
  (`MobileMoney`), up to 3 banks from `knownBanks` (`Bank`: code, name, description, country; the account's provider
  is the bank's code), chosen in a sheet (`BankPicker`) with a search (case and accents ignored) and several at once.
  The sheet is a `GlassScreen` of its own, with its backdrop. The bank list comes from the Ministry of Finance list of
  September 2023: to check against COBAC before release. Cash only is allowed. Checked on a phone.
- [ ] **CAU-10** — permissions step, now the last one, each screen explaining before asking and « Plus tard » never
  blocking: notification access (system settings, read again on return), then Cauri's own notifications (Android
  13+) on « Continuer »; place reminders ask the location, then the background location on a screen of its own.
  `PermissionService` (`:core:capture`) reads the statuses and gives the system screens. The accounts are kept with
  the other answers and everything is saved after this step. `:feature:settings`: « Réglages » screen, opened from
  the profile button of Accueil, with the status of each permission. Checked on a phone with every permission put
  off; granting them not checked on a phone yet.
- [ ] **CAU-11** — no guide with steps per brand (dropped by decision): Réglages shows « Batterie de Cauri » with its
  status (`BatteryService` in `:core:capture`: unrestricted or not), and a tap opens Cauri's app details in Android,
  where « Batterie » lifts the restriction. Each row of Réglages says why Cauri needs it. The onboarding ends after the
  permissions. `FakePermissionService` and `FakeBatteryService` are test fixtures of `:core:capture`. Checked on a
  Galaxy S23.
- [ ] **CAU-12** — `CashBalanceService` (pure Kotlin in `:core:data`, JVM tests): last count, plus income and transfers
  in, minus expenses and transfers out on the Cash account after it; dismissed transactions are left out, detected
  ones count. The balance may go below zero until the next count. `CashCountRepository.balance` follows the latest
  count and the cash transactions (`CashBalance`: amount, count time, expenses since). Accueil shows the « Cash dans ta
  poche » card of the mockup (« ≈ », « Compté hier à 20 h · 3 dépenses depuis », « Compter mon cash »). The widget
  comes with CAU-32. The fakes of `:core:data` are its test fixtures. Checked on a phone. The layout of the card is
  to be reworked (button next to the amount when it fits).
- [ ] **CAU-13** — `TransactionRepository.recordWithdrawal(Withdrawal)`: a transfer from the account to the Cash
  account (two transactions, one transfer group) and the fee, when given, as an expense on the account; all or
  nothing. Parsed from a signal it is « à confirmer » and keeps the signal on the account's side; entered by the user it
  is confirmed. The cash balance goes up by the amount withdrawn (tested). Nothing creates withdrawals yet: parsing
  comes with CAU-18 to CAU-21, the « Retrait au guichet » row with « Activité » (CAU-30).
- [ ] **CAU-14** — habits (`HabitEntity`, `HabitRepository`, database v2 by auto-migration): row of glass tiles on
  Accueil, a tap records a confirmed cash expense (named after the habit, in its category) with « Annuler » for 4 s; a
  long-press opens the habit sheet (name, amount, category, delete), which leads to « Mes habitudes », reordered by
  dragging the handles. The first habits come from the kept pack categories (Moto, Beignets, Taxi, Crédit). « + »
  opens the quick entry; Cash preselected comes with CAU-26, and so do the suggestions from frequent manual entries.
  `CauriTextField` moved to `:core:design`. On a phone: the upgrade to v2 and the empty row (« Ajouter une
  habitude »); taps, undo, the sheets and dragging not checked yet.
- [ ] **CAU-15** — `CashCountScreen` (`:feature:cash`): banknotes and coins with « − » and « + » (gold once counted),
  « Tout effacer », « Saisir le total directement », « Total compté », « Attendu ≈ » and the gap, updated on every
  tap. No gap or a surplus: « Valider le comptage » saves the count, `CashCountRepository.addCount` recording a surplus
  as found money (income, at the time of the count). Money missing: « Répartir l'écart » opens `GapSplitRoute`, a
  placeholder until CAU-16. Checked on a phone.
- [ ] **CAU-16** — `GapSplitScreen` (`:feature:cash`): « Où sont partis 4 500 F ? » since the previous count, the habits
  as moments Cauri noticed (hint grey until typed), « + Autre dépense » in a category, the bar of what is split and
  « Reste non identifié ». « Enregistrer » records one confirmed cash expense per typed line and the rest, without
  category nor note (shown « Divers cash »), at the time of the count (`TransactionRepository.recordReconciliation`),
  then the count. Visits join the moments with the places tickets (CAU-33+). `formatMoment` and `dashedBorder` moved
  to `:core:design`. Checked on a phone.
- [ ] **CAU-17** — `CountReminderService` (`:core:data`, JVM tests): from the chosen hour until 23 h, at most once a
  day, never on a day already counted; « Quand c'est utile » after a withdrawal today, 3 cash expenses since the count
  or a count 3 days old, « Chaque soir » otherwise too, « Jamais ». `CountReminderWorker` (`:feature:cash`, WorkManager
  + Hilt, checked every hour, scheduled by `CauriApplication`) shows the notification, which opens the cash count
  (`CountReminders.ACTION_COUNT_CASH`). Réglages: « Rappels de comptage », frequency and hour (19 h to 22 h), saved in
  DataStore. Debug builds show a reminder on demand: `adb shell am broadcast -a com.cauri.debug.SHOW_COUNT_REMINDER
  -e reason WITHDRAWAL -p com.cauri.debug`. Checked on a phone: the four texts, the coin icon and lagoon colour, a tap
  opens the cash count and clears the notification; the evening check itself not seen firing yet.
- [ ] **CAU-18** — `CauriNotificationListener` (Hilt) keeps, as raw signals (`RawSignalRepository`, database v3 with the
  notification's `title`), the notifications of `watched_senders.json` (`:core:capture` assets): the operators' and
  banks' apps whole, the SMS and mail apps only for the listed senders (`SignalFilterService`, JVM tests). The same
  notification posted again within 10 minutes is kept once. Watched now, each with its status and where it was
  checked: apps Orange Cameroun and UBA (to confirm); message apps Samsung Messages, Google Messages, Gmail and Outlook
  (mails to confirm); senders OrangeMoney, UBACameroon, UBACM INFO and UBA, confirmed on a Galaxy S25 with an Orange
  SIM. MobileMoney (MTN MoMo) was seen there but sends no operation to that user: to confirm on a phone with an MTN
  SIM, as the banks' mail senders. Debug builds log the senders seen, never the text (`adb logcat -s CauriCapture`).
  Checked on two phones: listener bound, other notifications ignored; a kept signal and the rebind after a reboot not
  seen yet.
- [ ] **CAU-19** — `:core:parsing` (pure Kotlin, JVM tests on made-up messages): `MessageTemplate` (one format of one
  sender: identifier and version, senders by SMS or mail name or app package, kind and direction, currency, regular
  expression with the named groups `amount`, `fees`, `balance`, `counterparty`, `reference` and `date`, decimal
  separator, date format and language) in a versioned `TemplateCatalog` read from JSON by `TemplateCatalogReader`,
  which refuses unusable templates. `MessageParser` reads a message with the first template of its sender that
  matches, case and spaces ignored, amounts in minor units (`readAmount`), the date in the given zone or else the time
  of the notification. A message no template reads, or with a value that cannot be read, gives nothing and stays a raw
  signal to replay. The implementations are public for the app's Hilt modules; nothing uses them yet: the real
  templates come with CAU-20, the replay and the transactions with CAU-21.
- [ ] **CAU-20** — `message_templates.json` (`:core:parsing` resources, catalogue 2026.11.03): 19 Orange Money templates
  (transfers sent and received, money received, international transfers, from and to the bank, cash deposits and
  withdrawals, merchant, bundle, airtime, ENEO and other payments, virtual card payments and top-ups) and 2 UBA ones
  (card payment, transfer to Orange Money), written from the 1 610 SMS of the four watched senders on a Galaxy S25
  (Orange SIM): they read all 826 operations there, none of the other messages (promotions, failures, prompts, codes).
  `collected_messages.json` keeps 257 of them anonymised (names, numbers, references, amounts and dates replaced) with
  what must be read, tested by `MessageTemplatesTest`. The engine now rounds the cents Orange Money writes
  (« 94.07 FCFA » → 94 F) and reads a `currency` group (card payments in USD or EUR). Not covered: MTN MoMo (no MTN
  SIM), UBA credits and other UBA debits (only two operation SMS seen), bank mails, and the creation of the virtual
  card (one SMS, whose amount may be a fee or a first top-up). For CAU-21: the ENEO payment and
  the airtime top-up each come with a second SMS left unrecognised on purpose; the virtual card has its own balance,
  and its top-up has no Orange Money debit SMS.
- [ ] **CAU-21** — `SignalProcessor` (`:core:capture`) reads the unread raw signals with the bundled catalogue (each
  template now names its `provider`), when the listener keeps one and when Accueil opens, once at a time.
  `DetectionService` (JVM tests): withdrawal → transfer to Cash, deposit → transfer from Cash (agents and services
  alike, by decision), other debits → expense, other credits → income, a fee above 0 → separate expense, an operation in
  another currency → amount 0 with `foreignAmount`/`foreignCurrency` kept (database v4) until the user types it.
  `TransactionRepository.recordDetected` writes them « à confirmer » and marks the signal read in one transaction; a
  notification posted twice creates one transaction. A message about an account the user does not have creates nothing:
  Accueil offers to follow it (« Suivre ton compte … ? », `AccountProposalViewModel`), a refusal is kept
  (`ignoredAccounts` in DataStore) and Réglages lists the ignored accounts, a tap following them and reading their
  messages received so far. The Orange Money virtual card is an account of its own (`PREPAID_CARD`, « Orange Money
  Virtuo »). Every account has a provider, `CASH_PROVIDER` (« CASH ») for the Cash account (database v5, a manual
  migration that recreates the accounts table and its trigger). Debug builds keep a made-up message on demand: `adb
  shell am broadcast -a com.cauri.debug.RECORD_SIGNAL -e title OrangeMoney -e text "…" -p com.cauri.debug`. Checked on
  the Galaxy S23 with made-up Orange Money messages: database upgrades to v4 then v5 (Cash provider set, trigger kept),
  the offer, « Non merci », following from Réglages, the payment, the withdrawal raising the cash by 10 000 F with its
  fee, a repeated message kept once. « À confirmer » (CAU-27) and « Activité » (CAU-30) do not exist yet, so the
  detected transactions show only through the balances.
- [ ] **CAU-22** — `DuplicateService` (JVM tests): a manual entry and a detected transaction are the same operation
  when on the same account, of the same type (expense or income, transfers left to CAU-23) and amount, within two hours,
  the detected one read in a signal and not confirmed yet (never a fee); the closest in time wins. The detected one is
  kept, confirmed, with the user's category, note and place; the entry becomes `MERGED`, left out of every balance like
  `DISMISSED` (`TransactionStatus.NOT_COUNTED`). Both ways: `TransactionRepository.recordDetected` merges into an entry
  typed before, `recordEntry(ManualEntry)` into an operation detected before and returns the merge to offer to undo
  (`RecordedEntry`). Each merge is logged in `transaction_merges` (database v6) with the values the kept transaction
  had, and `undoMerge` puts both back; the writes are all or none in `TransactionMergeDao`. No screen uses it yet: the
  quick entry on any account (« Fusionné · Annuler ») comes with CAU-26, the merges in « Activité » with CAU-30.
  Checked on the Galaxy S23: upgrade to v6, a detected payment still recorded. Next: CAU-23.

Update this section at the end of each ticket.

## Way of working

- One ticket at a time, on a `cau-0xx-description` branch, ending in one commit named after the ticket
  (`CAU-03 · Glass surface`), committed and merged before the next ticket starts.
- Reread the ticket's acceptance criteria in `docs/tickets.md` before coding and check them at the end.
- If a product choice is not settled by the documents, ask rather than decide alone.
