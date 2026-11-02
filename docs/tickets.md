# Cauri — MVP tickets

> English working copy of the reference document kept in Claude (November 2, 2026): https://claude.ai/artifact/XE2X5itqGYCmyDifwjKm4e. The reference is written in French; if the two differ, the Claude document prevails. Update this copy when it changes.

This document splits the Cauri MVP into 50 tickets over three phases. Phase 1 gives an app usable every day that follows the four places money lives: cash, MTN MoMo, Orange Money and the bank. Phase 2 makes the reminders smart (places, contextual bandit, predictions). Phase 3 adds budgets, goals, insights and the finishing touches.

Indicative sizes: S for a day at most, M for two or three days, L for about a week.

User-facing texts are quoted in French, the app's default language. Mockups are artboards of the design canvas (https://claude.ai/artifact/7cuvPsaveMQEG1VCZh2qbP), named here by their title; every screen has a light version and, below it on the canvas, a dark one. Amounts and names in the mockups are examples.

## Order of work

The skeleton and the design system come first, then the database, since every feature writes to it. Cash comes before digital capture: it is most of the spending, and its screens work without any permission. Digital capture can start in parallel as soon as real MoMo, Orange Money and bank messages have been collected, because their formats must never be invented. Places and the reminder engine (phase 2) need weeks of real data from phase 1, which is why the decision log starts in phase 1.

## Phase 1 · Technical foundations

### CAU-01 · Create the project

Set up the multi-module Gradle project (Kotlin DSL, version catalog, convention plugins) with Compose, Hilt, Room, DataStore and type-safe navigation. The skeleton exists in this repository; this ticket is done when it builds and runs.

- The app compiles, launches and goes from the placeholder onboarding to the placeholder home screen.
- CI runs ktlint, detekt, unit tests and the debug build on every pull request.
- Debug and release variants are signed separately; both can be installed side by side.

Size: M.

### CAU-02 · Cauri theme

Turn the mockups into a Compose theme: lagoon, gold, coral, green, pearl and night palettes in light and dark, embedded Inter font (OFL), type scale with tabular figures for amounts, radii and spacing.

- The theme follows the phone's dark mode; screens read colours only from `CauriTheme.palette`.
- Gold is used only for cash and savings, coral for spending, green for income.
- Text contrast reaches at least 4.5:1 in both modes (checked by a unit test).
- A catalogue of previews shows every token and component.

Size: M. Dependencies: CAU-01. Mockups: all screens.

### CAU-03 · Glass surface

A reusable surface with background blur and transparency (Haze), used for cards, the bottom bar and sheets. On devices without blur (before Android 12, or low-end ones), it becomes an opaque tinted surface with the same shape and border.

- Blur is on only where `RenderEffect` is available and the device is not flagged as low-end.
- Scrolling a list behind the bottom bar stays smooth on a Tecno or Infinix entry-level phone.
- The fallback is chosen once at startup, never per frame.

Size: M. Dependencies: CAU-02. Mockups: « Accueil » (bottom bar, cards).

### CAU-04 · Navigation

Floating bottom bar with Accueil, Activité, the central « + », Objectifs and Analyses, and navigation between the features. Features never navigate to each other directly: `:app` wires them through callbacks.

- The selected tab is highlighted in lagoon; « + » opens the quick entry sheet.
- Back from a tab returns to Accueil, then leaves the app.

Size: S. Dependencies: CAU-03. Mockups: « Accueil », « Activité », « Objectifs », « Analyses ».

### CAU-05 · Database

Room tables: accounts, categories, transactions, cash counts, places, visits, raw signals and decision log. Amounts are `Long` in minor units, always positive, with the account's ISO 4217 currency; the direction comes from the transaction type. Schemas are exported for migrations.

- A transaction has a type (expense, income, transfer out, transfer in), a status (detected, confirmed, dismissed), an origin (signal, manual, natural language, reconciliation) and an optional transfer group.
- A raw signal can create at most one transaction (unique index).
- The Cash account is created with the database and cannot be deleted.
- DAOs are tested on an in-memory database with the bundled SQLite driver.

Size: M. Dependencies: CAU-01.

## Phase 1 · Onboarding

### CAU-06 · Profile screen

First name, occupation (« Salarié·e », « Indépendant·e », « Commerçant·e », « Étudiant·e », « Autre ») and income type (« À date fixe » or « Variables »). Saved locally.

- « Continuer » stays enabled; a missing first name is pointed out after a first attempt.
- The line « Tout reste sur ton téléphone » is visible.

Size: S. Dependencies: CAU-05. Mockup: « Onboarding · profil ».

### CAU-07 · Payment habits screen

Main way of paying (« Surtout en espèces », « Un peu des deux », « Surtout par téléphone ») and cash in pocket right now.

- The amount creates the first cash count, which sets the starting balance of the Cash account.
- The main way of paying is stored: the reminder rules (CAU-37) use it.

Size: S. Dependencies: CAU-05, CAU-12. Mockup: « Onboarding · habitudes ».

### CAU-08 · Choice of categories

Cameroon pack of categories to keep or remove, with free additions: Alimentation, Transport, Crédit et forfaits, Famille, Tontine, Logement et factures, Santé, Loisirs, Éducation, Habillement, Imprévus famille. Two levels at most.

- Tontine is created as a saving category, not an expense.
- The button counts the kept categories (« Continuer · 6 catégories »).
- Categories can be renamed, merged or archived later; an archived category keeps its transactions.

Size: M. Dependencies: CAU-05. Mockup: « Onboarding · catégories ».

### CAU-09 · Accounts

The user ticks the accounts they use: MTN MoMo, Orange Money, their bank. The Cash account always exists.

- Each account has a name, a kind (bank, mobile money, cash), a provider and a currency (XAF by default).
- An account can be added or hidden later from the settings.

Size: S. Dependencies: CAU-05.

### CAU-10 · Permissions

Access to notifications, then location as an option. Each request comes after a short explanation screen; a refusal never blocks the onboarding.

- Notification access opens the system settings and the result is read on return.
- Location is asked only if the user chooses to enable place reminders, with background access explained separately.
- The settings screen shows the status of each permission.

Size: M. Dependencies: CAU-06.

### CAU-11 · Battery optimisation

Guide to exclude Cauri from battery saving, with steps per brand (Tecno HiOS, Infinix XOS, itel, Samsung, others), based on dontkillmyapp.com.

- The brand is detected; the matching steps are shown with a button to the right settings screen when possible.
- The guide can be reopened from the settings.
- Menu paths are verified on real phones before release.

Size: M. Dependencies: CAU-10.

## Phase 1 · Cash

### CAU-12 · Estimated cash balance

The Cash balance is the last cash count, plus withdrawals and cash income, minus cash expenses since that count.

- The calculation is pure Kotlin, covered by JVM tests.
- It is shown with « ≈ » on the home screen and in the widget, with the time of the last count.

Size: M. Dependencies: CAU-05. Mockup: « Accueil » (card « Cash dans ta poche »).

### CAU-13 · Withdrawal as a transfer to cash

A MoMo, Orange Money or bank withdrawal creates a transfer to the Cash account: two transactions with the same transfer group, never an expense.

- The cash balance goes up by the withdrawn amount.
- Withdrawal fees, when the message gives them, are a separate expense.

Size: S. Dependencies: CAU-12, CAU-21. Mockup: « Activité » (« Retrait au guichet »).

### CAU-14 · One-tap expenses

A row of habitual cash expenses on the home screen (« Moto 500 F », « Beignets 300 F »…). One tap records a confirmed cash expense, with a short undo.

- The first habits are suggested from the onboarding answers; the user can add, edit, reorder and remove them.
- After a few weeks, the most frequent manual cash entries are suggested as new habits.
- « + » at the end of the row opens the quick entry with Cash selected.

Size: M. Dependencies: CAU-12. Mockup: « Accueil ».

### CAU-15 · Count my cash

Screen with banknotes (10 000, 5 000, 2 000, 1 000) and coins (500, 100, 50, 25), each with « − » and « + » and its count, « Tout effacer », total counted, expected amount and gap.

- The total and the gap update on every tap.
- « Saisir le total directement » lets the user type the total instead.
- With no gap, « Valider le comptage » saves the count; with a gap, « Répartir l'écart » opens CAU-16; a surplus is recorded as found money.

Size: M. Dependencies: CAU-12. Mockup: « Compter mon cash » (interactive).

### CAU-16 · Split the gap

List of the moments Cauri noticed since the last count (visits, habits), each with an amount to type. Suggestions stay grey placeholders until typed; only typed amounts count. The rest is recorded as « Divers cash ».

- A bar shows how much of the gap is split.
- « + Autre dépense » adds a free line with a category.
- « Enregistrer » creates one confirmed expense per filled line, plus « Divers cash » for the rest.

Size: M. Dependencies: CAU-15. Mockup: « Répartir l'écart » (interactive).

### CAU-17 · Cash count reminder

A notification suggests counting the cash, for example in the evening after a day with many cash expenses or a withdrawal. Frequency adjustable in the settings.

- No more than one count reminder a day.
- The notification opens the count screen directly.

Size: S. Dependencies: CAU-15.

## Phase 1 · Digital capture

### CAU-18 · Listen to notifications

The notification listener stores, as raw signals, the notifications of the watched senders: MoMo, Orange Money, the banks and the SMS app. Nothing else is kept.

- The list of watched package names is data, checked on real phones, never guessed.
- A raw signal keeps the sender, the text, the time and the template version that parsed it (or none).
- The listener reconnects after a reboot.

Size: M. Dependencies: CAU-05, CAU-10.

### CAU-19 · Template engine

A template describes one message format of one sender and extracts amount, direction, counterparty, date and fees. Templates are versioned data (JSON), not code, so they can later be updated without a new release.

- Pure Kotlin in `:core:parsing`, covered by JVM tests on anonymised messages.
- A message no template recognises is kept and can be replayed when a template is added.

Size: L. Dependencies: CAU-01.

### CAU-20 · MoMo, Orange Money and bank templates

Templates for MTN MoMo, Orange Money and the main banks of Cameroon, for the common messages: payment, transfer, withdrawal, money received.

- Built from about twenty real, anonymised messages per operator and message type.
- Every collected message is parsed correctly in the tests.

Size: M. Dependencies: CAU-19, collected messages.

### CAU-21 · Detected transaction

A recognised signal creates a transaction « à confirmer ». A notification received twice creates only one transaction.

- Money received creates an income; a transfer between the user's accounts goes to CAU-23.
- The detected transaction appears in « À confirmer » and in « Activité ».

Size: S. Dependencies: CAU-18, CAU-20. Mockup: « Accueil » (card « Boulangerie Saker »).

### CAU-22 · Duplicates

A manual entry close to a detected transaction (same amount, close time, same account) is merged with it instead of creating a second expense.

- The user's category and note win; the detected data fill in the rest.
- The merge is logged and can be undone.

Size: M. Dependencies: CAU-21.

### CAU-23 · Internal transfers

A debit and a credit of the same amount between two accounts of the user, within a short window, are linked as one transfer and never counted as spending.

- Bank to MoMo, MoMo to Orange Money and withdrawals to Cash are covered.
- A transfer shows as one line in « Activité ».

Size: M. Dependencies: CAU-21.

### CAU-24 · Health check

If no banking notification has arrived for several days while they used to, Cauri tells the user that detection may have stopped and offers the battery guide.

- The threshold adapts to the user's usual rhythm.
- The message is shown at most once a week.

Size: S. Dependencies: CAU-18, CAU-11.

## Phase 1 · Entry and confirmation

### CAU-25 · Home screen

Home screen, cash first: « Cash dans ta poche » with « Compter mon cash », one-tap expenses, available today with the digital account balances, and the cards to confirm.

- Every number comes from the database; nothing is computed in the composables.
- The cards to confirm show cash visits before digital transactions.

Size: M. Dependencies: CAU-12, CAU-14. Mockups: « Accueil », « Accueil · sombre ».

### CAU-26 · Quick entry

Sheet with the type (« Dépense », « Revenu », « Transfert »), the amount on a numeric keypad, the account (Cash selected by default), suggested categories and « Enregistrer ».

- A cash expense can be recorded in less than three seconds.
- The suggested categories are the most likely ones for this account and time of day.

Size: M. Dependencies: CAU-05. Mockup: « Ajout rapide ».

### CAU-27 · Cards to confirm

A card arrives pre-filled with Cauri's proposal: a category for a detected transaction, an amount for a visit. ✓ or a swipe right confirms, a swipe left means « Rien dépensé », a tap on the amount edits it, a tap on the card opens the edit sheet.

- The two swipe directions never do the same thing.
- Every outcome is reported to the decision log (CAU-37).
- A confirmed card disappears with a short undo.

Size: M. Dependencies: CAU-25, CAU-29. Mockup: « Accueil » (section « À confirmer »).

### CAU-28 · Edit an expense

Full sheet: amount, category (with « Proposée par Cauri » when it is the proposal), account, date, place, note and split across several categories. « Rien dépensé » and « Valider » at the bottom.

- Splitting creates one transaction per part, linked together.
- Changing the account moves the amount from one balance to the other.

Size: M. Dependencies: CAU-26. Mockup: « Modifier une dépense ».

### CAU-29 · Keep the proposal

Each transaction keeps the proposed category and amount next to the confirmed ones.

- The share of proposals confirmed unchanged can be computed per source (place, merchant, template).
- These pairs feed the predictions (CAU-42).

Size: S. Dependencies: CAU-05.

### CAU-30 · Activity

History grouped by day, filters « Tout », « Cash », « Mobile » and « Banque », cash and digital totals for the month. Cash counts appear in the timeline with their gap.

- Transfers are grey and never counted as spending.
- A line opens the edit sheet.

Size: M. Dependencies: CAU-28. Mockup: « Activité ».

### CAU-31 · Actionable notifications

The notification of a detected transaction shows category buttons; the one of a visit lets the user type the amount without opening the app (`RemoteInput`).

- A button confirms the transaction with that category.
- The typed amount creates a confirmed cash expense at that place.

Size: M. Dependencies: CAU-21, CAU-36.

### CAU-32 · Widget and quick settings tile

A home screen widget (cash in pocket and an add button) and a quick settings tile that opens the quick entry.

- The widget updates after each cash change, without a periodic job.

Size: M. Dependencies: CAU-12, CAU-26.

## Phase 2 · Places and visits

### CAU-33 · Declared places

The user saves a shop: name, position, default category. Each place becomes a geofence.

- At most 100 active geofences (system limit); the most visited places win.
- Home and work are marked as such and never trigger a reminder.

Size: M. Dependencies: CAU-10.

### CAU-34 · Detect visits

Entering and leaving a geofence, with the visit duration. Activity Recognition avoids continuous GPS.

- Battery use stays under a few percent a day on the tested phones.
- Visits survive a reboot.

Size: L. Dependencies: CAU-33.

### CAU-35 · Discover frequent places

Places where the user often stops are clustered from stays; Cauri asks « Tu viens souvent ici, c'est un commerce ? ».

- The question is asked at most once per place.
- A « no » marks the place as ignored.

Size: L. Dependencies: CAU-34.

### CAU-36 · Visit as a card to confirm

A visit to a shop with no transaction creates a card with the place's category and its usual amounts as suggestions. A message arriving during the visit is attached to it instead.

- Suggested amounts come from the history of this place (median, last amount).
- The visit appears in « Répartir l'écart » (CAU-16) when it was paid in cash.

Size: M. Dependencies: CAU-34, CAU-27. Mockup: « Accueil » (card « Marché Mokolo »).

## Phase 2 · Reminder engine and predictions

### CAU-37 · Reminder rules and decision log

Simple rules decide to notify now, add to the evening summary or skip. Each decision is logged with the feature vector computed at that moment, the action, its probability and the model version.

- The decision log starts in phase 1, before the bandit, to collect training data.
- Rules are pure Kotlin in `:core:engine`, covered by JVM tests.

Size: M. Dependencies: CAU-05.

### CAU-38 · Guardrails

Quiet hours, a daily cap on notifications, no reminder at home or at work. Guardrails apply before any decision.

- The user can change the quiet hours and the cap.

Size: S. Dependencies: CAU-37.

### CAU-39 · Evening summary

One notification in the evening gathers what is left to confirm.

- Sent only if something is waiting; its time adapts to when the user usually reacts.

Size: S. Dependencies: CAU-37.

### CAU-40 · Rewards

Each decision gets an outcome: confirmed, nothing spent, dismissed, missed (manual entry or cash gap after a skip), timeout. Reward values live in a versioned configuration and are applied at training time.

- Accidental taps (dismissed within two seconds) and notifications never seen are excluded.
- A late confirmation from the evening summary corrects the outcome.

Size: M. Dependencies: CAU-37, CAU-27.

### CAU-41 · Bandit and nightly training

A linear Thompson sampling bandit replaces the rules once enough decisions are valid. A nightly WorkManager job (charging, idle) attributes the closed outcomes, then retrains on a 90-day window.

- The job is idempotent; a new model is published in one transaction with a version number.
- The model can be rebuilt from the log at any time.

Size: L. Dependencies: CAU-40.

### CAU-42 · Predict category and amount

Suggest the most likely category and amount per place and per merchant, from the history and past corrections.

- Measured by the share of proposals confirmed unchanged (CAU-29).

Size: M. Dependencies: CAU-29.

## Phase 3 · Budgets, goals and insights

### CAU-43 · Split an income

When an income arrives, Cauri offers to split it: savings first, then fixed costs, then budgets.

- Only money received is split, never money expected.

Size: M. Dependencies: CAU-21.

### CAU-44 · Savings goals

Create a goal (amount, deadline), follow its progress and its estimated completion date. Tontine and family emergencies included. This screen is still to be clarified with Krawist before starting.

- The estimated date uses a cautious income estimate (25th percentile of past income).

Size: M. Dependencies: CAU-43. Mockup: « Objectifs ».

### CAU-45 · Suggested budgets

After four to six weeks, Cauri suggests a budget per category from real spending.

- The user accepts, adjusts or ignores each suggestion.

Size: M. Dependencies: CAU-30.

### CAU-46 · Available today

Money received, minus fixed costs, planned savings and spending, divided by the days left. Shown on the home screen and the widget.

Size: S. Dependencies: CAU-43. Mockup: « Accueil ».

### CAU-47 · Insights screen

Spending per category against budget, share of cash (including the unidentified part), overrun projection and yearly cost of habits.

- Every figure comes from code; an LLM may only word it.

Size: L. Dependencies: CAU-45. Mockup: « Analyses ».

## Phase 3 · Finishing touches

### CAU-48 · Encrypted backup

Encrypted export to a file or Google Drive, and restore on a new phone.

- Required before any public release.

Size: L. Dependencies: CAU-05.

### CAU-49 · Floating onboarding

A help bubble appears the first time an element is shown (for example the swipes on a card). One at a time, never shown again once closed.

Size: M. Dependencies: CAU-27.

### CAU-50 · Natural language entry

On compatible phones, « taxi 1500, beignets 500 » creates two expenses. Without an available model, the field is hidden.

- The model only extracts; amounts are checked by code before saving.

Size: L. Dependencies: CAU-26.
