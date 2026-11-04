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
| MVP tickets (CAU-01 to CAU-55, 3 phases)  | `docs/tickets.md` | https://claude.ai/artifact/XE2X5itqGYCmyDifwjKm4e |
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

- Kotlin, Jetpack Compose, Material 3 under the Cauri theme. minSdk 29 (Android 10), compile 37, target 36. JDK 17. AGP
  9.4, the latest Android Studio opens (Kotlin built in: no `kotlin.android` plugin), Gradle 9.6, Kotlin 2.4.21, Compose
  1.12, KSP 2.3.
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
| `:feature:settings`     | Settings: permission and battery statuses, reminders      | 10, 11, 38     |
| `:feature:places`       | « Mes lieux », the screen of a place                      | 33             |
| `:feature:goals`        | « Objectifs »: savings, tontines, envelopes               | 44             |
| `:feature:insights`     | « Analyses »: budgets suggested, insights                 | 45, 47         |
| `:core:design`          | Theme, palette light and dark, glass surface, components  | 02, 03         |
| `:core:data`            | DataStore preferences, Room database                      | 05, 12, 13     |
| `:core:capture`         | Permissions, notifications, signals, health, geofences    | 10–24, 33      |
| `:core:parsing`         | Message templates and parsing (pure Kotlin)               | 19, 20         |
| `:core:engine`          | Reminder rules, guardrails, bandit (pure Kotlin)          | 37–42          |
| `:core:ai`              | The phone's language model (Gemini Nano), never a number  | 50             |

Goals (phase 3) will get their own module when their ticket starts.

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
- [ ] **CAU-03** — `GlassScreen` (background, content and overlay layers) and `GlassSurface` with Haze 2.1.0 (`hazeBlur`
  from `haze-blur`, its `HazeBlurStyle` remembered per background and tint, the sources of the glass screen as input);
  blur from Android 12 on phones not flagged low-RAM, decided once per process in `CauriApplication`, opaque tinted
  fallback otherwise. Haze 2 brought the stack with it, at the user's request: Kotlin 2.4.21, Compose BOM 2026.09.00
  (Compose 1.12), AGP 9.4 (9.5 is past what Android Studio opens) with Gradle 9.6 and compileSdk 37 (target 36 kept),
  KSP 2.3.13, Hilt 2.60.1. Checked on a Galaxy S23, after the upgrade too; smooth scrolling on an entry-level Tecno or
  Infinix not verified.
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
- [ ] **CAU-14** — habits (`HabitEntity`, `HabitRepository`, database v2 by auto-migration): glass tiles on Accueil,
  three a line on two lines at most, more scrolling inside them, and « + Ajouter une habitude », dashed, under them; a
  tap records a confirmed cash expense (named after the habit, in its category) with « Annuler » for 4 s; a long-press
  opens the habit sheet (name, amount, category, delete), which leads to « Mes habitudes », reordered by dragging the
  handles. The first habits come from the kept pack categories (Moto, Beignets, Taxi, Crédit). « Ajouter une habitude »
  opens the sheet of a new habit (the quick entry is the « + » of the bottom bar). `CauriTextField` moved to
  `:core:design`. On a phone: the upgrade to v2 and the empty row, the habit sheet and « Mes habitudes », two drags in a
  row (each line keyed, the dragged one opaque); taps and undo not checked yet.
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
  Checked on the Galaxy S23: upgrade to v6, a detected payment still recorded.
- [ ] **CAU-23** — `TransferLinkService` (JVM tests): a detected debit and a detected credit are one transfer between
  the user's accounts when on two accounts, within 30 minutes, the credit equal to the debit or smaller by at most 3 %
  (an operator keeping its fee); the same amount, then the closest in time win, each operation in one transfer at most.
  Both become the two sides of the transfer (one transfer group), and when less arrived than left, the debit is the
  amount that arrived and the gap a fee on the debited account, so that each balance stays right. Orange Money writes
  the bank's amount and its fee apart (« transfert de 100000 FCFA … Frais : 1000 FCFA »): both sides are then equal and
  the fee is already an expense. `TransferRepository.linkTransfers` sweeps the detected expenses and incomes never
  linked, after each `SignalProcessor.process`; each link is logged in `transfer_links` (database v7) and
  `unlinkTransfer` puts both back, never linked again. Withdrawals to Cash were transfers already (CAU-13, CAU-21).
  One line per transfer in « Activité » and the undo come with CAU-30. Checked on the Galaxy S23 with made-up messages:
  the UBA debit « B2W … ORANGEMONEYCM » of 10 000 F and the Orange Money credit « depuis votre compte UBA » linked into
  one transfer, its 100 F fee kept as an expense.
- [ ] **CAU-24** — `CaptureHealthService` (`:core:capture`, JVM tests): the capture is silent a month after the last
  kept signal, never before the first one; a fixed month by decision, instead of a threshold following the user's
  rhythm: a month without a digital payment happens, but the banks write at least about their monthly fees, and every
  message of a watched sender counts, promotions included. `CaptureHealthWorker` (daily, scheduled by
  `CauriApplication`) shows the notification « Cauri ne reçoit plus tes messages », only when a permission the capture
  needs is missing (the access to the notifications taken back, or the battery restricted: with every permission given,
  a silence is the user's own rhythm), once a week at most (`lastCaptureWarning` in DataStore), which opens Réglages
  (`CaptureHealth.ACTION_OPEN_SETTINGS`, `NotificationTarget` in `:app`); Accueil shows the card « Détection en pause »
  with « Vérifier » on the same condition, read again each time Accueil comes back. Debug builds show the warning on
  demand (`adb shell am broadcast -a com.cauri.debug.SHOW_CAPTURE_WARNING -p com.cauri.debug`) and simulate a month of
  silence for the card (`-a com.cauri.debug.SIMULATE_CAPTURE_SILENCE`, `-ez on false` to end it;
  `DebugCaptureHealthService` bound in debug only). Checked on the Galaxy S23: the notification, its tap opening
  Réglages; the card with a simulated silence, « Vérifier » opening Réglages; the daily check not seen firing.
- [ ] **CAU-25** — Accueil, cash first: the day and « Bonjour, <prénom> », « Cash dans ta poche », the notices, the
  habits, « Tes comptes » and « À confirmer ». « Tes comptes » lists the balance of each digital account as its last
  message gave it (`account_balances`, database v8, written with the signal's transactions;
  `AccountRepository.balances`, mobile money first); by decision the latest balance wins, read or typed; the balance
  typed by the user, with the split of its gap as for the cash, is CAU-52. « Disponible aujourd'hui » waits for the
  budgets (CAU-46). « À confirmer » (`TransactionRepository.pending`) shows one card per operation read in a message,
  newest first: its fee rides with it, a withdrawal or a linked transfer is one card « UBA → Orange Money ». ✓, the
  swipes and the proposed category come with CAU-27, the visits (shown first) with the places. Balances of the messages
  read before v8 are not recovered. Checked on the Galaxy S23 in light mode: upgrade to v8, the greeting, the Orange
  Money balance from a made-up payment, five cards (payments, a withdrawal, a linked transfer), light and dark.
- [ ] **CAU-26** — `QuickEntrySheet` (`:feature:transactions`): « Dépense », « Revenu » or « Transfert », the amount on
  a keypad (« 000 », erase), the account (Cash first, in gold), up to three suggested categories, the first chosen, and
  « Autre… » for every category of the type; a transfer chooses « De » and « Vers » and has no category
  (`TransferRepository.recordTransfer`, two confirmed sides). `CategorySuggestionService` (JVM tests): the categories
  used most on the account within two hours of the time of day over 90 days, then at any hour, then by name
  (`CategoryRepository.suggested`). An entry merged into an operation detected before shows « Fusionné avec une
  opération reçue par message · Annuler » for 4 s above the bottom bar (`QuickEntryMergeBar`); `UndoBar` moved to
  `:core:design`. The sheet is a glass screen of its own over the screens' shapes, as tall as what it holds
  (`GlassScreen(fillMaxSize = false)`), the keys in strong glass; it scrolls on a small screen. An amount stops at 10
  000 000 F (`MAX_AMOUNT`), a digit over it refused with « Le montant ne peut pas dépasser 10 000 000 F ». The
  categories take three lines at most and scroll inside them. Checked on the Galaxy S23 in dark mode: a cash expense of
  5 000 F in four taps (cash 14 500 → 9 500), a payment typed on Orange Money merged into the detected one, a merge
  undone. In light mode: the glass sheet and its keys, a transfer Cash → Orange Money. Income not tried on the phone;
  the cap seen on it, the categories scrolling in three lines not (five categories only). Next: CAU-29.
- [ ] **CAU-29** — each transaction keeps Cauri's proposal next to the confirmed values (`proposedCategoryId`,
  `proposedAmount`, database v9): the amount read in a message for a detected operation, the first suggested category
  for a quick entry (whatever the user chose); a habit, typed by the user, has none, and a merge keeps the proposal of
  either side. `ProposalService` (JVM tests): the share of proposals confirmed unchanged, category and amount kept, per
  place, merchant or template (`ProposalRepository.acceptance`, `ProposalSource`), for the predictions of CAU-42. No
  screen confirms a detected operation yet (CAU-27): its proposal is measured once confirmed. Checked on the Galaxy
  S23: the upgrade to v9.
- [ ] **CAU-27** — « À confirmer » (`PendingSection`, `ConfirmationViewModel`, out of `HomeViewModel`): each card is
  pre-filled with Cauri's proposal, the categories suggested for its account and hour (the first chosen, kept as
  `proposedCategoryId`), none for a transfer. The card names what happened in bold (the merchant, or the two accounts of
  a transfer), the amount signed in the colour of its type, then the account behind its dot, the kind of operation in
  words (« Paiement », « Retrait », « Dépôt », « Transfert entre tes comptes ») and when. ✓ or a swipe right confirms, a
  swipe left sets aside (« Rien dépensé » for an expense, « Ignorer » otherwise), the answer given once the card has
  left the screen, a haptic tick as the finger crosses the point where letting go answers, the colour of the action
  coming in with the swipe and the list closing smoothly; a tap on the amount types another one (asked first for a
  payment in another currency); « Confirmé » or « Mis de côté » with « Annuler » for 4 s. `ConfirmationRepository`
  writes each answer all or none (`ConfirmationDao`): both sides of a transfer, and the open reminder decisions of the
  transaction closed as `CONFIRMED`, `NOTHING_SPENT` or `DISMISSED` (none exist before CAU-37); « Annuler » puts the
  operation back as read. A fee stays detected, counted, without card. A tap on the card opens the edit sheet with
  CAU-28. Checked on the Galaxy S23 in dark mode: ✓, « Annuler », both swipes, the amount field; the haptic tick not
  felt by me. Next: CAU-28.
- [ ] **CAU-28** — `EditTransactionSheet` (`:feature:transactions`, `EditViewModel`), opened by a tap on a card to
  confirm (`homeGraph(onEditTransaction)`, the sheet shown by `:app`): « Lu dans un message » over the merchant (or the
  two accounts of a transfer), the amount (a payment abroad shows its amount in its own currency until the user types
  what it cost), the suggested categories and « Toutes… » with « Proposée par Cauri » while the proposal is chosen, the
  account, the date (day then hour, never later than now) and the note, « Répartir sur plusieurs catégories », « Rien
  dépensé » (« Ignorer » for what is not an expense) and « Valider ». By decision every card opens it, a transfer
  without category nor split; the account of an operation read in a message, or of a transfer, is shown and never
  changed (it can be for an entry, once « Activité » opens the sheet, CAU-30); the parts each take a category and an
  amount and the rest stays in the category chosen above, the parts never taking all of it; the place row waits for the
  places (CAU-33+). `ConfirmationRepository.confirm(TransactionEdit)` writes it all or none: one confirmed transaction
  per part, without signal nor proposal, sharing a `splitGroup` with the edited one (database v10); a transfer is
  confirmed on both sides with the amount and time typed. The proposals count a split operation with its parts
  (`getProposalOutcomes`). `readableName` moved to `:core:design`. What was typed survives a change of theme or a
  rotation, and is forgotten once the sheet closes. Checked on the Galaxy S25 (with the user's go-ahead, the S23
  unplugged) with made-up Orange Money payments, in light mode: the sheet, a part of 1 000 F out of 2 500 F, « Valider
  », the date moved to yesterday, « Rien dépensé ». Dark mode, an income, a transfer and a payment abroad not tried on a
  phone. Next: CAU-30.
- [ ] **CAU-30** — « Activité » (`ActivityScreen`, `ActivityViewModel` in `:feature:transactions`;
  `ActivityRepository` and `ActivityService`, JVM tests, in `:core:data`): « Tout », « Cash », « Mobile » (prepaid cards
  included) and « Banque », a transfer showing with either of its accounts and a count with the cash; « Cash dépensé »
  and « Numérique dépensé » since the first of the month (set aside left out, waiting operations and fees counted); the
  history by day (« Aujourd'hui », « Hier », « Lundi 2 novembre »), one glass card per day. One line per operation: a
  transfer once and grey (« Retrait au guichet », « Dépôt », « Transfert », « MoMo → Cash »), never spending; by
  decision a split operation once with its total (« Réparti en 2 »), and the operations set aside shown struck through
  (« Mis de côté »); merged entries left out; a fee on a line of its own (« Frais »); « À confirmer » for a waiting one.
  A cash count shows what was counted and its gap (« Écart de 4 500 F réparti en 3 dépenses », « … de plus que prévu »,
  « Aucun écart »). A line opens the edit sheet (`activityGraph(onEditTransaction)`): a split operation opens with its
  parts, and saving replaces them; an entry shows its type icon and its note. The icons of expense, income, transfer and
  count moved to `:core:design`. The search button of the mockup is shown and does nothing yet: search, periods and
  sorting are CAU-55. Undoing a merge or a linked transfer from the sheet is CAU-54 (by decision). Checked on the Galaxy
  S23 in dark mode: upgrade to v10, the lines of the mockup (set aside, waiting, habits, deposits, a count with a
  surplus, fees), « Mobile », a line opening the sheet. A split line, light mode and a long history on an entry-level
  phone not checked. Next: CAU-31.
- [ ] **CAU-55** — « Activité » searches, narrows to a period and sorts (`ActivityQueryService`, public in
  `:core:data`, JVM tests): the search button opens a field in place of the title, by decision, keyboard up, the lines
  following as the user types (merchant, note, category or account, accents and case ignored; « 5000 » and « 5 000 »
  find an amount), a cross closing it. Under the filters, by decision, one chip « Tout l'historique · Par date » opens a
  sheet: « Tout l'historique », « Cette semaine » (from Monday), « Ce mois », « Le mois dernier », « Choisir les dates…
  » (a range up to today), and « Par date » or « Par montant » (one card, no days, the cash counts left out). The whole
  history stays the default, totalling the current month; a period narrows the lines and the totals, the filters and the
  search narrow the lines only. `MonthSpending` became `Spending`, computed by the view model. Checked on the Galaxy S23
  in light mode: a search, the sheet, the sort by amount. The range of dates not tried on a phone. Next: CAU-52.
- [ ] **CAU-52** — the balance of a digital account typed by the user: by decision, a tap on an account of « Tes
  comptes » opens `BalanceSheet` (`BalanceViewModel`, `:feature:home`): « Solde Orange Money », the balance typed, «
  Affiché : 5 000 F » (by decision the balance expected is the one shown) and the gap. « Enregistrer » keeps it
  (`AccountRepository.recordBalance`, `account_balances` with no signal, database v11), the gap left unsplit creating
  nothing; with money missing, « Répartir l'écart » opens, by decision, the split screen of the cash count
  (`GapSplitRoute(accountId)`, `navigateToBalanceGap`): no habits, « + Autre dépense » recording confirmed expenses on
  the account (`recordReconciliation(accountId, …)`), the rest recording nothing, then the balance. A balance typed
  above the one shown records no income (by decision). The latest balance, read or typed, wins (CAU-25). An account
  without balance yet now shows in « Tes comptes » with « — », its first balance without gap. Checked on the Galaxy S23
  in light mode: 11 000 F shown, 10 000 F typed, 600 F split to a category, the balance 10 000 F. With the updated
  mockup, « Tes comptes » became « Mes comptes », right under the cash and above the habits (by decision): one glass
  card per account scrolling sideways, edge to edge, its mark (MTN yellow, Orange orange, a bank's columns: `mtn` and
  `orange` in the palette), its name and its balance; the dashed « + » at the end opens « Ajouter un compte »
  (`AddAccountSheet`, `AddAccountViewModel`), as the bank list of the onboarding: a glass screen of its own, one search
  over the mobile money services not followed yet then the banks of `knownBanks`, glass rows ticked several at once, «
  Ajouter 2 comptes »; « Disponible aujourd'hui » waits for the budgets (CAU-46). The balance sheet is a glass screen of
  its own as the quick entry, the balance typed on Cauri's keypad, never the phone's (`Keypad` moved to `:core:design`).
  Next: CAU-54.
- [ ] **CAU-53** — one motion language (`CauriMotion` in `:core:design`, `CauriTheme.motion`): three durations (150,
  250 and 400 ms), three easings (standard, emphasized for what comes in, exit for what leaves) and the transitions
  built from them; with « Supprimer les animations » (animator scale 0, read once by `CauriTheme`) every spec snaps and
  every transition is none. Tabs fade into each other, the other screens slide in a tenth of their width and back
  (`NavHost` transitions in `:app`); the bottom bar goes down and comes back up; the undo bars rise and go down
  (`AnimatedPresence`); the cards « À confirmer » grow in and, once answered, fade while their place closes, the section
  leaving with its last card (`AnimatedItems`); the cash in pocket, the balances, the totals of « Activité » and the
  amounts of the keypads roll to their new value (`RollingNumber`); the keys, the chips, the filters and the habit tiles
  sink under the finger (`Modifier.pressScale`) and the selections glide (`animateColorAsState`); the days of « Activité
  » move into place (`animateItem`). Sheets rise as Material's own. One touch language too (`CauriHaptics`,
  `rememberCauriHaptics`, played only with the phone's touch vibrations on): a keyboard tap for the keys of the keypads
  and the « − » « + » of the count, a tick for a choice (chips, filters, tabs, the type of an entry, a row ticked, «
  Annuler »), a confirmation for what is saved (✓ and a swipe right, a habit tapped, « Enregistrer », « Valider », a
  count, a balance), a refusal for what is set aside or refused (a swipe left, « Rien dépensé », an amount over the
  limit), a long press for a habit's sheet and a line picked up to drag, a step each time it passes another, and the
  threshold of a swipe. Checked on the Galaxy S23 in light mode: a card confirmed shrinking while « Confirmé » rises,
  the tabs, the balances. « Supprimer les animations » (a system setting, left to the user) and the smoothness on an
  entry-level phone not checked. Next: CAU-54.
- [ ] **CAU-54** — « Séparer » in the edit sheet (`Separate`, `EditViewModel.onSeparate`), shown only for what
  Cauri joined: « Fusionné avec une opération que tu as saisie » for a merge, « Deux messages liés en un transfert entre
  tes comptes » for a linked transfer (`EditableTransaction.mergeId` and `transferLinkId`, the merge or link that still
  holds). A tap undoes it (`undoMerge`, `unlinkTransfer`) and closes the sheet. Both operations come back as before: an
  undone merge gives the detected one back the amount read in its message and drops the parts split since; an undone
  link puts the debit and the credit back « à confirmer » with the amounts of their messages, never linked again.
  Checked on the Galaxy S23 in light mode with made-up messages: a UBA → Orange Money transfer separated from « À
  confirmer », a payment merged with a quick entry separated from « Activité », each showing again on its own line. Dark
  mode not tried. Next: CAU-33.
- [ ] **CAU-33** — places (`Place`, `NewPlace`, `PlaceKind` now public, `PlaceRepository`, database v12 with
  `places.remindAfterVisit`, v13 with `places.area`, v14 with `places.address`): « Mes lieux » (`:feature:places`),
  opened from a row of Réglages (by decision), its header fixed, lists the shops (default category, visits) then home
  and work (their neighbourhood, « Sans rappel »), under where the map goes; a tap opens the screen of the place,
  filled, « + » a new one. By decision there is no real map, whose tiles would come from the network: `MapArea` draws
  the mockups' streets, the places as pins by their positions against each other, and the radius around the pin; a real
  map can take its place later. The screen is transparent over the screens' shapes, its form a glass sheet. A place is
  where the user stands (`LocationService`, asked as the screen opens, again with « Ma position ») or, by decision, at
  an address searched in « Adresse », above the name (`AddressService` on Android's `Geocoder`, Cameroon first, as the
  user stops typing; the addresses open above the field, the name the map knows then the address): the one other use of
  the network, what is typed and the positions going to the phone's geocoding service. The field always shows the
  address of the position, found at the phone's position too, kept with the place and its neighbourhood; the name the
  map knows, never a street, fills an empty name. The geocoder knows few names of shops. The radius goes from 50 to 300
  m (80 by default), the reminder and the category for a shop only; home and work never remind. « Enregistrer le lieu »
  and « Supprimer ce lieu » stay at the bottom of the screen. Each place is a geofence (Google Play services location
  21.3.0, by decision; `GeofenceService`), with the location « always » only: `PlaceService` keeps 100 at most, the most
  visited then the newest; `PlaceWatcher`, started by `CauriApplication`, registers them again after each change, after
  a restart (`PlaceBootReceiver`) and once the location is allowed; « Mes lieux » says when it is missing.
  The visits are CAU-34. Checked on the Galaxy S23 in light mode: a shop
  saved at ± 20 m, its geofence of 150 m registered, changed and deleted, the geofence gone; addresses found above the
  field, the address of the phone's position in the field. Dark mode and a phone without Google Play services not tried.
  Next: CAU-34.
- [ ] **CAU-34** — visits (`VisitRepository`, `VisitDao.enter`/`leave`): by decision, a visit counts once the user
  has stayed 5 minutes in a place (`VisitRecorder.STAY`), Android's « dwell » with that loitering delay, so that passing
  by on a moto or in a taxi never does; its departure closes it (geofences now `DWELL` and `EXIT`).
  `PlaceTransitionReceiver` (Hilt) has `VisitRecorder` record each transition: a stay opens a visit from 5 minutes
  before, unless one is open there already; a departure closes the open one. By decision, no Activity Recognition, which
  would need the « Activité physique » permission: the stay is enough for now. Visits are rows of the database, so they
  survive a restart; the geofences registered again then tell a stay with `INITIAL_TRIGGER_DWELL`, which goes on with
  the visit already open. « Mes lieux » counts them. Debug builds tell a stay or a departure on demand (`adb shell am
  broadcast -a com.cauri.debug.PLACE_VISIT -p com.cauri.debug -e place 3`, `--ez left true` to leave). Checked on the
  Galaxy S23 with a made-up place: two stays told twice kept as one visit each, a departure closing it, « 2 visites »,
  the geofences with a dwell of 5 minutes. A real visit, a restart and the battery over a day not checked: the phone
  does not move. Next: CAU-35.
- [ ] **CAU-35** — frequent places (`DiscoveryRepository`, `DiscoveryService` with JVM tests, database v15 with
  `stops` and `place_suggestions`): by decision, `PlaceDiscoveryWorker` (`:feature:places`, every 15 minutes, scheduled
  by `CauriApplication`) reads a coarse position (Wi-Fi and cell towers, `currentPosition(precise = false)`) with the
  location « always » only; two positions within 150 m and 40 minutes make a stop, a position joining the stop it fits
  among the last 40 minutes (positions may come late), and a position alone is forgotten 40 minutes after; and stops are
  kept 30 days on the phone, by decision. A place is frequent, by decision, with stops within 150 m on 3 different days
  of the last 30, none at night (22 h to 5 h: home, not a shop), far from the places declared and from the places asked
  about before. `PlaceDiscoverer` asks with a notification once per place, one at a time (« Tu viens souvent ici. C'est
  un commerce ? », « Près de Bastos · 6 arrêts ce mois », « Oui » and « Non »), and « Mes lieux » shows, by decision,
  every place not answered yet, one card each swiped sideways, edge to edge, the next one at the edge (« Oui, l'ajouter
  », « Non, ignorer »): « Oui » opens the screen of a new place at its position (`PlaceRoute(suggestionId)`, or the one
  the notification asked about last with `NotificationTarget.SUGGESTED_PLACE`), answered once the place is saved; « Non
  » ignores it for good (`PlaceSuggestionReceiver` from the notification). Debug builds make up a stop on demand (`adb
  shell am broadcast -a com.cauri.debug.PLACE_STOP -p com.cauri.debug -e lat 4.05 -e lon 9.77 -e days 5`). Checked on
  the Galaxy S23 with made-up stops in Douala on 3 days: the notification and its buttons, « Oui » opening the screen
  there, two cards swiped, « Non, ignorer ». `Modifier.bleed` moved to `:core:design`, for « Mes comptes » and these
  cards. The worker over a real day and its battery not checked. Next: CAU-36.
- [ ] **CAU-36** — visits to shops become cards to confirm (`VisitRepository`, `VisitService` with JVM tests,
  database v16 then v17 with `visits.transactionId`): by decision, leaving a shop that brings a reminder with nothing
  paid during the visit creates a cash expense « à confirmer » (`TransactionOrigin.VISIT`, at 0 so that nothing counts
  before the user says what, the place's category, its usual amount as `proposedAmount`: the median of the expenses
  there, none before the first), which is, by decision, the same card as the operations read in messages (its gold pin,
  « ● Cash · Visite de 25 min · … », the usual amount or « Montant ? », the categories with the place's first, ✓, the
  swipes) and opens the same edit sheet, the usual amount filled in. A payment read in a message or typed from the entry
  to 15 minutes after the exit, by decision, takes the place of the visit (`attachTransactions`, on leaving, after each
  message read and when Accueil opens): the card goes. By decision, a card waits until the next cash count: « Répartir
  l'écart » lists the visits since the count first (« Visite de 25 min », `GapLineSource`), the amount typed confirms
  the card, the others are set aside (`settle`). The cards waiting are left out of « Activité » and of the expenses
  since the count. The notification « Combien as-tu dépensé ? » and the evening recap come with the reminder engine
  (CAU-37+, by decision). Checked on the Galaxy S23 in dark mode with a made-up shop and simulated visits: the card at
  its usual amount, the sheet, « Rien dépensé ». The split of a visit in a count, a message come during a real visit not
  tried on a phone. Next: CAU-37.
- [ ] **CAU-37** — the reminder rules and the decision log: `:core:engine` (pure Kotlin, JVM tests) holds
  `ReminderRules` (« rules-1 », by decision: never a notification from 22 h to 7 h nor within 30 minutes of the last
  one, the evening summary instead; a visit notifies after a stay of 10 minutes at a place leading to an expense half
  the time or more, or with fewer than 5 visits before, is skipped below a fifth, goes to the summary otherwise; an
  expense read in a message notifies; a withdrawal, an income or a transfer goes to the summary) and `FeatureEncoder`
  (version 1, 12 numbers: the trigger, the hour as a sine and a cosine, the day, the stay by bands, the place's expense
  rate, the time since the last transaction and the last notification, the notifications of the day, the cash, the
  rules' score). `ReminderDecider` (`:core:capture`) decides once about each card of the last two hours, after each
  message read and each visit ended, and logs it (`DecisionLogRepository`: features, action, probability 1, model
  version; `ReminderAction` moved to `model`). By decision, « maintenant » sends the notification of the mockup
  (`ReminderNotifier`, channel « Dépenses à confirmer »): a visit « Marché Mokolo : combien as-tu dépensé ? », « Visite
  de 25 min · Alimentation · cash », its usual amount, « Rien » and « Autre montant » typed in the notification; an
  expense « 5 000 F payés à Boulangerie Saker », « Orange Money · c'est quoi ? » and its three likely categories. Its
  buttons answer the card without opening the app (`ReminderReplyReceiver`), which closes the decisions as before. The
  evening summary is CAU-39. Debug builds show a card's notification at any hour (`adb shell am broadcast -a
  com.cauri.debug.SHOW_REMINDER -p com.cauri.debug -e transaction 12`). Checked on the Galaxy S23 at night with made-up
  messages and a made-up shop: decisions logged « EVENING_SUMMARY » by the rules, the two notifications, a category and
  an amount typed confirming their card. A notification decided by day not seen yet. Next: CAU-38.

- [ ] **CAU-38** — the guardrails of the reminders (`ReminderGuardrails`, `:core:engine`, JVM tests), applied before the
  rules decide, which choose among what they allow (rules now « rules-2 », without the night and the spacing): never a
  notification in the quiet hours, past the daily cap, within 30 minutes of the last one, nor, by decision, while the
  user is at home or at work (an open visit there, `VisitRepository.atHomeOrWork`): the evening summary instead; with «
  Rappels après une visite » off, by decision, a visit is skipped, its card still waiting in « À confirmer ». « Rappels
  intelligents » (`RemindersScreen`, `RemindersViewModel`, `:feature:settings`), opened from a row of Réglages: the
  switch, « Heures de silence » (a sheet, start and end an hour a step, 22:00 – 07:00 by default), « Rappels par jour »
  from 1 to 10 (4 by default, as the mockup), « Lieux sans rappel » listing home, work and the shops without reminder, a
  tap opening « Mes lieux ». Saved in DataStore (`ReminderSettings`). The rows « Récapitulatif du soir » and « Ce que
  Cauri a appris » come with CAU-39 and CAU-41. Checked on the Galaxy S23 in dark mode: the screen, the sheet, the cap;
  a guardrail at work on a real decision not seen on a phone. Next: CAU-39.

- [ ] **CAU-39** — the evening summary: `EveningSummaryWorker` (`:core:capture`, every quarter of an hour, scheduled by
  `CauriApplication`) has `EveningSummarySender` (JVM tests) send, once a day and before 23 h, « Récapitulatif : 4
  éléments à confirmer » with « Ouvrir », only when cards wait, by decision every card of « À confirmer » and not only
  those the engine kept for the summary (`lastEveningSummary` in DataStore). Its time (`SummaryTime`, `:core:engine`,
  JVM tests) is the one chosen in « Rappels intelligents » (« Récapitulatif du soir », a sheet: « Automatique » or 19:00
  to 22:00 by half hours), else, by decision, the median time of the user's answers given in the evening (18 h to 23 h)
  over 30 days (`DecisionLogRepository.answerTimes`), to the half hour, between 18:00 and 22:00, once 10 answers show
  it; 20:30 before. A tap opens « Ce soir » (`EveningSummaryScreen`, `:feature:home`,
  `NotificationTarget.EVENING_SUMMARY`, outside the graph of the tab: no bottom bar): « Dépensé aujourd'hui » and « Dont
  cash », one line per card (« Visite de 25 min · cash », the amount in a pill or « Montant ? » dashed, ✓), a swipe to
  the left sets it aside, a tap on the amount types it, on the line opens the edit sheet; « Tout valider (n) » confirms
  every line with an amount (`ConfirmationViewModel.onConfirmAll`, no undo), « Plus tard » closes. The mockup's line «
  Moto · 2 trajets habituels » is left out: a habit is confirmed when tapped. Debug builds show the summary on demand
  (`adb shell am broadcast -a com.cauri.debug.SHOW_EVENING_SUMMARY -p com.cauri.debug`). Checked on the Galaxy S23 in
  dark mode with made-up messages: the notification, its tap opening « Ce soir », a line set aside and undone. The
  worker firing at its time and « Tout valider » not seen on a phone. Next: CAU-40.

- [ ] **CAU-40** — the outcomes of the decisions and their rewards. The answers close them as before (`CONFIRMED`,
  `NOTHING_SPENT`, `DISMISSED`), a late one, such as from the evening summary, now correcting a `TIMEOUT`; a cash count
  closes those of the visits it settles (`VisitDao.settle`), by decision: an amount found `MISSED` after a skip,
  `CONFIRMED` otherwise, the cards it sets aside `TIMEOUT`. `ReminderLearningWorker` (`:core:capture`, once a day,
  charging and idle, scheduled by `CauriApplication`) has `DecisionLogRepository.attributeOutcomes` give the others
  theirs (`OutcomeService`, JVM tests), again and again the same: a visit whose card went, paid during it, by decision,
  typed by hand `MISSED` after a skip and `CONFIRMED` otherwise, read in a message `NOTHING_SPENT` (no cash); a card
  waiting 3 days `TIMEOUT`. Left out of the training (`decision_log.excluded`, database v18, `DecisionExclusion`): a
  notification set aside within 2 seconds (`ACCIDENTAL`), and, by decision, a notification Android did not show, Cauri's
  notifications being off, as « never seen » (`UNSEEN`, at decision time). The rewards are `RewardTable`
  (`:core:engine`, « rewards-1 », JVM tests), values from -1 to 1 per action and outcome, applied at training time
  (CAU-41), by decision: a notification confirmed 1, set aside or timed out -0.5, the summary quieter (0.7 to -0.3), a
  skip right when nothing was spent (0.5) and -1 for any expense missed. Checked on the Galaxy S23: the upgrade to v18.
  Next: CAU-41.

- [ ] **CAU-41** — the contextual bandit: `Bandit` (`:core:engine`, JVM tests), linear Thompson sampling over the three
  actions, one Bayesian linear regression of the reward per action (λ 1, the weights drawn with a spread of 0.5),
  choosing among what the guardrails allow and logging how often it would choose so (200 draws, the first deciding).
  `ReminderLearningWorker` (nightly, charging and idle) now has `ReminderLearner` (`:core:capture`, JVM tests) attribute
  the outcomes, then train on the valid decisions (closed, not left out, same layout of the features) of the last 90
  days and since the last start from zero (`learningSince` in DataStore), the rewards of `RewardTable` applied then, and
  publish the model in one write as the next version (`bandit_models`, database v19, `BanditRepository`); the same log
  publishes nothing new, and a model can be rebuilt from the log at any time. By decision, the bandit takes over once
  150 decisions are valid, 15 of each action; the rules decide until then, and their score stays a feature.
  `ReminderDecider` logs « bandit-3 » and the probability when the bandit decides. « Rappels intelligents » shows « Ce
  que Cauri a appris » (`LessonsCard`, gold, as the mockup), by decision told from the log rather than from the weights:
  the two hours the user answers most in (10 answers at least), the shop where a visit leads to an expense 7 times in 10
  or more, the one 2 in 10 or less (5 visits at least), « Appris sur 214 réponses en 6 semaines », « Repartir de zéro »
  (a dialog) forgetting the models and the decisions before it; « Cauri apprend de tes réponses… » while nothing is
  learnt. Debug builds run the nightly learning on demand (`adb shell am broadcast -a com.cauri.debug.LEARN_REMINDERS -p
  com.cauri.debug`). Checked on the Galaxy S23 in dark mode: the upgrade to v19, the card while learning, the dialog,
  the learning on demand without error. A model trained on real answers not seen: the phone has 3. Next: CAU-42.

- [ ] **CAU-42** — the category of a card is now predicted per merchant and per place (`CategoryRepository.predicted`,
  `CategoryPredictionService` with JVM tests): the categories the user confirmed before with the same merchant (case and
  spaces around ignored), then at the same place, over 180 days, each choice weighing half as much every 30 days, so
  that a correction wins within weeks over an old habit; then the category the card has (a shop's own), then the ones
  used on the account at this hour (CAU-26). The cards of « À confirmer », of « Ce soir » and the categories of the
  reminder notifications use it, the first chosen and kept as the proposal (`PendingOperation.placeId`). The amount
  stays the one read in the message, and for a visit the median of what was confirmed at the place (CAU-36), corrections
  included, by decision. The share of proposals confirmed unchanged per place and per merchant is
  `ProposalRepository.acceptance` (CAU-29); no screen shows it yet. Checked on the Galaxy S23: the cards load; a
  prediction on real habits not seen on a phone. Next: phase 3, CAU-43.

- [ ] **CAU-49** — help bubbles (`HelpBubble` in `:core:design`): a lagoon bubble with its text and a cross, placed by a
  screen next to what it explains; of those on screen, the first asked for that the user never closed shows, one at a
  time, until closed, then never again (`HelpBubbles`, `LocalHelpBubbles`, provided by `HelpBubblesViewModel` in `:app`,
  JVM tests; `seenHelp` in DataStore). By decision, three bubbles for now: the swipes and the tap of the cards « À
  confirmer », the tap and the long press of the habits, the tap on a line of « Activité ». Checked on the Galaxy S23 in
  dark mode: the bubble of the cards, closed, gone after a restart; the two others not seen on a phone. Next: CAU-45.

- [ ] **CAU-45** — budgets suggested from real spending, in « Analyses », now `:feature:insights` (`InsightsScreen`,
  `InsightsViewModel`, the placeholder of the tab gone). `BudgetService` (JVM tests): nothing before four weeks of
  history; then, for each expense category spent on twice at least, what it cost over the last six weeks at most (the
  history when shorter), the waiting operations counted and what was set aside left out, brought to 30 days and rounded
  up to 500 F. `BudgetRepository` (`budgets`, database v20: amount a month, status, the amount suggested next to the one
  kept): one card per category without budget and never ignored, « Accepter », « Ajuster » (a field, the suggestion to
  start from) or « Ignorer » (by decision, never suggested again). Before four weeks, « Après quatre semaines de
  dépenses, Cauri te propose un budget par catégorie. ». The budgets are read by the insights (CAU-47) and the daily
  available amount (CAU-46). Checked on the Galaxy S23 in dark mode: the upgrade to v20, the tab before four weeks; a
  suggestion not seen on a phone (too little history). Next: CAU-47.

- [ ] **CAU-47** — « Analyses » as the mockup, for the current month (`InsightsRepository`, `InsightsService` with JVM
  tests, `InsightsDao`), every figure computed by code: « 61 % de tes dépenses en cash » and the total, one bar with the
  cash identified in gold, the rest of the counts no expense explained in pale gold (« Non identifié 4 100 F », the
  expenses of a count without category) and the digital in lagoon; « Dépenses par catégorie », each category spent on or
  budgeted, the costliest first, « 48 000 / 55 000 », one bar on a common scale with the budget marked, in coral once
  over it; the budget gone over the most, or else the one the month will go over the most at the pace of the days gone
  (« Transport déjà dépassé de 3 500 F », « Au rythme actuel, le mois finira autour de 52 500 F. »); the costliest habit
  over a year at the pace of its taps of the last 30 days, 4 at least (« Beignets : ≈ 122 000 F par an »); then the
  budgets suggested (CAU-45). Counted as in « Activité »: the waiting operations and the fees in, what was set aside or
  merged out. By decision, the month chip shows the current month only (no other month yet) and the button « Fixer une
  limite » of the mockup is left out. No language model words anything yet. Read again each time the tab comes back.
  Checked on the Galaxy S23 in dark mode with made-up data: the share of cash with its rest unidentified, the
  categories; a budget, an overrun and a habit not seen on a phone (none on it). Next: CAU-43, CAU-44, CAU-46, CAU-48
  and CAU-50, to clarify.

- [ ] **CAU-43** — the split of the incomes (database v21 with `fixed_costs` and `income_splits`): by decision, an
  income of 10 000 F or more (the user's threshold, `IncomeSplitSettings` in DataStore) arrived within a week, read in a
  message or typed, the money a count found left out, brings on Accueil the card « Revenu reçu · 150 000 F sur Orange
  Money » (`IncomeSplitViewModel`), « Répartir » or « Pas cette fois » (all of it to the budgets, never offered again).
  The sheet (`IncomeSplitSheet`, a glass screen of its own) puts the savings first, by decision a share of each income
  (10 % by default, moved by 5 % up to 50 % for this income only, rounded down to 100 F), then what the fixed costs of
  the month not paid yet need, then the rest for the budgets, over a bar of the three; « Valider la répartition » keeps
  it (`IncomeSplitRepository`, `IncomeSplitService` with JVM tests: what earlier incomes kept for the fixed costs pays
  those paid since first). By decision the fixed costs are found then confirmed (`FixedCostService`, JVM tests): an
  expense paid once a month in 2 months at least of the current one and the three before, the amounts within 10 %, by
  merchant, else by category and amount; Réglages › « Répartir mes revenus » (`IncomeScreen`) asks « ENEO revient chaque
  mois » (« Oui, c'est fixe », « Non », never asked again), lists the fixed costs (« Payée » once an expense of the
  month has the same merchant, its name as merchant, note or category, or its category for about its amount) and adds or
  edits them (name, amount a month, day). The savings rate and the threshold are set there too. `Stepper` moved to
  `:core:design`. The savings are only counted: goals (CAU-44) and « Disponible aujourd'hui » (CAU-46) use them. Checked
  on the Galaxy S23 in dark mode: the card of a made-up income, the rate moved to 15 %, the split kept, a fixed cost
  typed. A suggestion and « Payée » not seen on a phone (no history of months).

- [ ] **CAU-46** — « Disponible aujourd'hui » faces « Mes comptes » on Accueil (`AvailableRepository`,
  `AvailableService` with JVM tests): the money received this month (incomes confirmed or waiting, the money a count
  found and the transfers between the user's accounts left out), minus the savings the splits put aside (CAU-43), the
  fixed costs not paid yet and what was spent (fees and fixed costs paid included), divided by the days left in the
  month, today included, by decision never below 0 and rounded down to 100 F; by decision hidden before the first income
  of the month. The widget shows it with CAU-32. Checked on the Galaxy S23 in dark mode: 0 F with a rent of 50 000 F
  above the 20 000 F received.

- [ ] **CAU-44** — « Objectifs » (`:feature:goals`, `GoalsScreen`, `GoalsViewModel`; `GoalRepository`, `GoalService`
  with JVM tests; database v22 with `goals` and `goal_contributions`), as the mockup: « Mis de côté en novembre » (what
  was put aside this month), then the goals in their order. By decision the money is only counted, never moved: the
  savings of each income split (CAU-43) go to the first saving not reached, shown in gold, « Mettre en premier »
  choosing it; the sheet of a saving puts money aside or takes it back by hand (« Verser », « Reprendre »). A saving has
  an amount, a deadline or none, its share and bar, and « vers juin 2027 »: the savings to come estimated cautiously,
  the 25th percentile of the income of the last 6 full months times the savings rate, filling the savings one after the
  other from next month (« après l'échéance » past its deadline, « Date estimée après un mois de revenus » before). By
  decision a tontine is a contribution a month, made a fixed cost (deleted with it), and the day the user takes the pot
  (« 25 000 F par mois · levée le 15 déc. »): the pot, when it arrives, is an income like another. The emergency fund,
  by decision, is created first the first time « Objectifs » opens, without deadline, a month of spending (the last 30
  days, rounded up to 5 000 F; 50 000 F before any), never again once deleted. « Imprévus famille » of the mockup is an
  envelope: an amount a month for a category, « reste 18 000 F » and « 12 000 utilisés sur 30 000 F ce mois ». Checked
  on the Galaxy S23 in dark mode: the emergency fund created at 100 000 F, a tontine added. An estimate, a split going
  to a saving and an envelope not seen on a phone.

- [ ] **CAU-31** — the actions of the notifications, most of them there since CAU-37 (a category button confirms the
  payment with it; a visit's usual amount, « Rien » or the amount typed in the notification confirms a cash expense at
  the place). As the mockup, a payment now offers the two likely categories and « Autre », which opens the app on the
  edit sheet of its card (`EditFromNotification`, `NotificationTarget.EditTransaction`, now a sealed interface) and
  clears the notification. By decision the visit keeps one amount: Android shows three buttons at most, and the mockup's
  second amount would take the place of « Autre montant ». Checked on the Galaxy S23 with a made-up payment: the
  buttons, « Autre » opening the sheet, the notification gone.

- [ ] **CAU-32** — the home screen widget « Cash dans ta poche » (`CashWidgetProvider`, `CashWidgetUpdater` in `:app`,
  `RemoteViews` by decision: no Glance library): « ≈ 7 500 F », « Disponible aujourd'hui 12 500 F » once known (CAU-46),
  and a lagoon « + » that opens the quick entry; a tap elsewhere opens the app. No periodic job (`updatePeriodMillis`
  0): `CauriApplication` starts the updater, which redraws the widgets on each change of the cash or the available
  amount, and a widget just added asks once. Opaque, its colours mirroring the palette light and dark (a home screen
  cannot blur). The quick settings tile « Ajouter une dépense » (`QuickEntryTileService`) opens the app on the quick
  entry too (`QuickEntryLaunch.ACTION_QUICK_ENTRY`, `NotificationTarget.QuickEntry`). Checked on the Galaxy S23: the
  intent of the widget's « + » and of the tile opening the quick entry. The widget on a home screen and the tile in the
  quick settings not placed on a phone yet (the launcher's own gestures).

- [ ] **CAU-48** — the encrypted backup (`BackupRepository`, `BackupService` with JVM tests, `javax.crypto` only, by
  decision no Google Drive library): Réglages › « Sauvegarde chiffrée » (`BackupScreen`, `BackupViewModel`) writes the
  database and the preferences, zipped and encrypted with AES-256-GCM under a key derived from the user's passphrase
  (PBKDF2-HMAC-SHA256, 600 000 iterations, a random salt and nonce each time, the header authenticated), to a file the
  user chooses with Android's picker (`cauri-2026-11-04.cauri`: the phone, a memory card, Drive…). The passphrase has 8
  characters at least, typed twice; nobody, Cauri neither, opens the file without it. « Restaurer une sauvegarde » asks
  before replacing the data of the phone, refuses a wrong passphrase or a changed file, a file that is no backup and one
  from a newer Cauri (its `user_version`), then closes the database, replaces the files and starts the app again in a
  new process; an older backup is migrated as the app starts. On a new phone, « Déjà utilisé Cauri ? Restaurer une
  sauvegarde » under the profile step opens the restore alone (`navigateToRestore`). Checked on the Galaxy S23 with
  made-up data: a backup written to Téléchargements (10 Ko), restored, the app restarting on Accueil with its data. From
  the onboarding of a new phone not tried on a phone.

- [ ] **CAU-50** — « taxi 1500, beignets 500 » in the quick entry (`FreeTextEntry`, `FreeTextViewModel` in
  `:feature:transactions`): by decision on Gemini Nano (`:core:ai`, `LanguageModel`, `GeminiNanoModel` on ML Kit's
  Prompt API 1.0.0-beta4, through AICore), the field shown only for an expense on a phone whose model is available, its
  download started on a phone that can get it, hidden everywhere else. The model only extracts (`FreeTextReader` in
  `:core:parsing`, JVM tests): a JSON list of label, amount as written and one of the user's categories, at temperature
  0; code keeps an expense only when its amount is written in the text (« 1500 », « 1 500 », « 1.500 », up to 10 000 000
  F) and its category is one of the user's, so that an amount the model added or converted is left out. The expenses
  read wait for the user, each removable, then « Enregistrer 2 dépenses » records them confirmed on the account chosen,
  named as written, the category as Cauri's proposal (`ManualEntry.fromFreeText`, origin `NATURAL_LANGUAGE`). Checked on
  the Galaxy S23, which has no AICore: the field hidden. Reading a text not tried on a phone (the S25 has Gemini Nano:
  with the user's go-ahead).

- [ ] **CAU-50 (messages)** — at the user's request, the phone's model also reads, as a fallback, the messages of a
  known sender no template recognises (`ModelMessageReader` in `:core:parsing`, JVM tests): `SignalProcessor` asks it,
  when available, after the templates, about each message of the last 7 days it was never asked about, 10 at a time
  (`raw_signals.modelReadAt`, database v23; a message without answer is asked again). The model only names the kind and
  the direction and copies the amount, the fees and the balance; code keeps the operation only when each is written in
  the message, on the account of the sender's one provider, in its templates' currency. It is recorded « à confirmer »
  as any other (template `model.gemini-nano@1`), its card saying « Paiement · lu par l'IA »
  (`PendingOperation.readByModel`). By decision, an operation the model read stays once a template comes. Checked on the
  Galaxy S23: the upgrade to v23 (no AICore there, so no reading).

- [ ] **CAU-56** — « Catégories » (`CategoriesScreen`, `CategoriesViewModel`, `:feature:settings`), opened from Réglages
  › Argent, as the mockups: « Dépenses · 9 », « Revenus · 2 », « Épargne · 2 », each category with its badge (icon and
  colour; by decision Cauri's own from its name while the user chose none, `CategoryStyleService` with JVM tests), its
  sub-categories and what it counted this month (`CategoryRepository.monthTotals`, its sub-categories in it), the
  chevron opening them with their amounts and « + Sous-catégorie »; a long press moves a category (`ReorderableColumn`,
  new in `:core:design`; by decision the order is the one of the pickers), « Archivées · Santé » brings one back. Its
  sheet (`CategorySheet`): name, eleven icons, five colours, its sub-categories (added, removed by archiving, reordered
  by their handles), « Fusionner avec… » and « Archiver ». Database v24: `categories.icon`, `color`, `position`. By
  decision a sub-category is a category of its own in every picker, counted in its parent by the budgets and « Analyses
  »; a merge moves everything of the category (transactions and proposals, sub-categories, habits, places, fixed costs,
  envelopes; its budget dropped) into the other after a confirmation, then deletes it, never undone
  (`CategoryDao.merge`, all or none). Checked on the Galaxy S23 in dark mode: the upgrade to v24, the list, the sheet, a
  sub-category added and shown open. `ReorderableColumn` is the one drag to reorder of the app (`ReorderPickUp.TOUCH` by
  a handle, or `LONG_PRESS` on the whole row, and the gap between rows): « Mes habitudes » and the sub-categories use it
  too. A long press moving a category checked on the S23; a merge and an archive not tried on a phone.

- [ ] **CAU-20 (UBA mails)** — three templates for the mails of UBA (catalogue 2026.11.04), written from two real mails
  given by the user and kept anonymised in `collected_messages.json` (now with the `packageName` of a mail): « Type
  Operation: Débit » to Orange Money (`B2W/…/ORANGEMONEYCM`, a transfer), any other debit (a payment, the merchant after
  « POS PUR @ »), and « Crédit » (money received, the sender after « UBA CAMERO- »); the account's name and number never
  read, the balance read when the notification shows it. By decision the templates name the mail apps (Gmail, Outlook)
  as senders, the wording of the mail being UBA's own. Not working yet: the capture keeps a mail only from a sender
  listed in `watched_senders.json`, and the name Gmail shows for these mails is not known yet; nor whether Gmail's
  notification holds the whole mail.

- [ ] **CAU-25 (stale balances)** — at the user's request, a balance a bank forgot to update (UBA sometimes repeats the
  balance before the operation) is moved by the operation (`BalanceService`, JVM tests): by decision, when a message
  reports an operation that moved money but gives the very balance the account had just before (read or typed), the
  balance kept is that one plus or minus what moved, fees included, computed by code; the raw message stays as received.
  The next balance read, or one the user types, replaces it as before. An operation in another currency moves nothing
  known and keeps the balance read. The balance before is strictly older than the operation: on the owner's S25 the mail
  of a transfer, repeating the SMS's right balance at the same second, was taken for a stale one and the amount taken
  off twice (fixed; a repeated message records nothing since CAU-22).

- [ ] **CAU-18 (UBA mails)** — the mail senders of UBA given by the user, « UBA Transaction Notification Services (UBA
  ALERT) » and « UBACameroun.Alert », are watched in the mail apps (`watched_senders.json`); the debug broadcast
  `RECORD_SIGNAL` takes the app that shows a message (`app`, Gmail for a mail). Checked on the Galaxy S23 with a made-up
  mail of UBA through Gmail: read by `uba.mail.debit`, the expense « à confirmer », its stale balance of 500 000 F moved
  to 495 750 F (CAU-25). A real mail in Gmail's notification, whole up to « Solde », not seen yet.

- [ ] **CAU-57** — Cauri locked by the phone's own security (`DeviceSecurity` on Android's `BiometricPrompt`, no
  library: the fingerprint or the face when the phone knows them, else the screen lock's code; permission
  `USE_BIOMETRIC`, granted at install). The onboarding offers it in a step of its own, now the fifth before the
  permissions (« Protège ton argent », `SecurityScreen`): « Activer le verrouillage » asks the phone's security once to
  know it works, a phone without screen lock is sent to its security settings, « Plus tard » never blocks. Réglages ›
  Données › « Verrouillage de Cauri » turns it on or off, by decision always after the phone's security, from inside the
  app too (`AppLockService`, one implementation for both, JVM tests; `appLock` in DataStore). Once on
  (`AppLockViewModel`, JVM tests), the app is locked at start and, by decision, again after more than a minute away (to
  answer an SMS in peace), the lock screen being the launch screen with « Déverrouiller » (`LockScreen`; `LaunchScreen`
  takes a `bottom`), over the screens, which keep their state; by decision Cauri's preview is hidden in the recent apps
  and no screenshot nor recording can be taken (`FLAG_SECURE`). The notifications stay as they are: they hold nothing of
  the user's own (by decision). `ic_lock` moved to `:core:design`. Checked on the Galaxy S23: Android's dialog opening
  from Réglages and cancelled, the lock staying off. Turning it on, the lock screen and the relock after a minute not
  tried: they need the owner's fingerprint or code.

- [ ] **CAU-22 (repeated messages)** — a bank writes one operation in an SMS and a mail (UBA), both dated by it to the
  second: seen on the owner's Galaxy S25, a transfer UBA → Orange Money gave two debits « à confirmer ». By decision a
  message repeating an operation another signal already reported (same account, type and amount, the very same second)
  is read and records nothing (`DuplicateService.repeated`, JVM tests); messages without a date of their own never
  match, as they are dated when received. The duplicate already on that phone stays, to set aside by hand. The Orange
  Money credit of that transfer never reached the listener (no notification of OrangeMoney in Samsung Messages): to
  understand with its owner.

- [ ] **CAU-23** (transfers from one message) — a transfer between the user's accounts is recorded from its first
  message: the templates that name an own account (`ownAccount`, catalogue 2026.11.05: Orange Money from and to the
  bank, UBA to Orange Money by SMS and mail) give `TRANSFER_OUT` or `TRANSFER_IN` when the account named is followed
  (`DetectionService`, name or provider, accents and case ignored), one card « UBA → Orange Money » at once; the message
  of the other side, within 30 minutes and at most 3 % less, attaches to the waiting side
  (`TransferLinkService.waitingSide`, `TransactionMergeDao.attachSide`): no second card, the gap a fee, the balance it
  gives kept. Both ways. Not checked on a phone: the S23 was not plugged in.

- [ ] **CAU-25 (estimated balances)** — Android 15 hides from Cauri the notifications it takes for a one-time code: on
  the owner's S25 the SMS of OrangeMoney « Votre transfert de 1000 FCFA … depuis votre compte UBA »
  (`mSensitiveContent`), so the transfer was read from the UBA side only and the Orange Money balance stayed. At the
  user's request, a transfer between the user's accounts read from one message now estimates the balance of the other
  account, the one before moved by the amount (`account_balances.estimated`, database v25), shown with « ≈ » in « Mes
  comptes »; the balance its own message gives replaces it (`TransactionMergeDao.attachSide`), and an estimate is never
  taken for a balance the bank forgot to update. Not checked on a phone.

- [ ] **CAU-20 (UBA credits from Orange Money)** — on the owner's S25, a transfer Orange Money → UBA gave nothing: no
  template read the UBA credit « CREDIT … Desc: W2B/…/ORANGEMONEYCM » nor its mail, and Gmail's notification shows only
  part of a mail, « Type Operation » or « Devise » missing. Catalogue 2026.11.06: `uba.transfer-from-orange-money` (SMS)
  and `uba.mail.transfer-from-orange-money`, both own-account transfers (CAU-23); the mail templates no longer need «
  Devise », nor « Type Operation » for a transfer, its way given by `B2W` or `W2B`. Three samples anonymised in
  `collected_messages.json`. The Orange Money SMS of that transfer never reached Cauri (hidden or never sent): its
  balance is estimated (CAU-25).

- [ ] **CAU-20 (UBA card payments online)** — on the owner's S25, a Google Play payment by UBA card gave nothing: the
  SMS writes « MSC-PUR @ » where a shop writes « MSC-POS PUR @ », and the model's answer was refused, its amounts «
  15,376.00 » not found in the message. Catalogue 2026.11.07: `uba.card-payment` v2 reads both, and
  `uba.mail.card-payment` reads the mail of a purchase whose « Type Operation » Gmail hides, after `uba.mail.debit`.
  `ModelMessageReaderImpl` now finds the amounts whose thousands a comma or a dot groups (CAU-50). Two samples
  anonymised in `collected_messages.json`. Not checked on a phone: the waiting debit is read again when Accueil opens.

Update this section at the end of each ticket.

## Way of working

- One ticket at a time, on a `cau-0xx-description` branch, ending in one commit named after the ticket
  (`CAU-03 · Glass surface`), committed and merged before the next ticket starts.
- Reread the ticket's acceptance criteria in `docs/tickets.md` before coding and check them at the end.
- Debug-only actions (broadcasts of the `src/debug` source sets) are listed in `docs/actions.md`: add each new one
  there, with its command, what it does and its extras.
- If a product choice is not settled by the documents, ask rather than decide alone.
