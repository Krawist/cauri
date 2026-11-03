# Debug actions

Debug builds (`com.cauri.debug`) answer a few broadcasts, to check on a phone what otherwise needs a real SMS, an
evening or a month of silence. Release builds have none of them: the receivers live in the `src/debug` source sets.

Every command is sent with `adb`. When it is not in the `PATH`, use its full path, and name the phone with `-s` when
several are plugged in:

```bash
~/Library/Android/sdk/platform-tools/adb -s <serial> shell am broadcast -a <action> [extras] -p com.cauri.debug
```

`adb devices` lists the serials. Extras are passed with `-e <name> <text>` for text and `--ez <name> <true|false>` for
booleans.

| Action                                     | Module           | Effect                                                     |
|--------------------------------------------|------------------|------------------------------------------------------------|
| `com.cauri.debug.SHOW_COUNT_REMINDER`      | `:feature:cash`  | Shows a cash count reminder at once                        |
| `com.cauri.debug.RECORD_SIGNAL`            | `:core:capture`  | Keeps and reads a made-up operator message                 |
| `com.cauri.debug.SHOW_CAPTURE_WARNING`     | `:core:capture`  | Shows the warning that the capture stopped                 |
| `com.cauri.debug.SIMULATE_CAPTURE_SILENCE` | `:core:capture`  | Simulates a month without message, for the Accueil card    |
| `com.cauri.debug.PLACE_VISIT`              | `:core:capture`  | Tells a stay at a place, or its departure                  |
| `com.cauri.debug.PLACE_STOP`               | `:feature:places` | Makes up a stop, then looks for frequent places           |

## Show a cash count reminder

```bash
adb shell am broadcast -a com.cauri.debug.SHOW_COUNT_REMINDER -e reason WITHDRAWAL -p com.cauri.debug
```

Shows the count reminder notification at once, through the same notifier as the evening check (CAU-17), without its
rules: the hour, the daily cap and the day already counted are ignored. A tap opens the cash count.

| Extra    | Type | Default   | Values                                                                          |
|----------|------|-----------|---------------------------------------------------------------------------------|
| `reason` | text | `EVENING` | `WITHDRAWAL`, `EXPENSES`, `DAYS_WITHOUT_COUNT`, `EVENING`: the text it shows    |

Needs Cauri's notifications allowed; nothing shows otherwise.

## Record a made-up operator message

```bash
adb shell am broadcast -a com.cauri.debug.RECORD_SIGNAL -e title OrangeMoney -e text "Paiement de BOUTIQUE ALPHA reussi par 690000000 ATANGANA MBALLA. ID transaction:MP261103.1210.A12345, Montant:2500 FCFA. Solde: 12500.00 FCFA." -p com.cauri.debug
```

Keeps the message as a raw signal, as if Samsung Messages had shown it (`com.samsung.android.messaging`, received now),
then reads the unread signals as the listener does (CAU-21): the templates, the transaction « à confirmer », the offer
to follow an account the user does not have, the merges (CAU-22) and the transfers (CAU-23). It does not need the
access to the notifications.

| Extra   | Type | Default | Values                                                                               |
|---------|------|---------|--------------------------------------------------------------------------------------|
| `title` | text | none    | SMS sender, as the notification titles it: `OrangeMoney`, `UBA`…                     |
| `text`  | text | empty   | Text of the message; anonymised examples in `core/parsing/src/test/resources/collected_messages.json` |

The same message sent again within 10 minutes is kept once. The signal and its transactions stay in the database: use
a test phone, as the cash estimate and the balances change.

A text with apostrophes or line breaks is easier to send from a script, quoting it for the phone's shell (Python
`shlex.quote`).

## Show the capture warning

```bash
adb shell am broadcast -a com.cauri.debug.SHOW_CAPTURE_WARNING -p com.cauri.debug
```

Shows at once the notification « Cauri ne reçoit plus tes messages » (CAU-24), without its rule: the month of silence,
the missing permission and the weekly cap are ignored. A tap opens Réglages. No extra.

Needs Cauri's notifications allowed; nothing shows otherwise.

## Simulate a month of silence

```bash
adb shell am broadcast -a com.cauri.debug.SIMULATE_CAPTURE_SILENCE -p com.cauri.debug
```

Makes the capture count as silent for a month, so that Accueil shows the card « Détection en pause » (CAU-24). The
card also needs a missing permission (the access to the notifications, or the battery restricted): take one back in
Android first. Accueil reads it each time it comes back to the screen. The daily warning keeps its real rule.

| Extra | Type    | Default | Values                                                   |
|-------|---------|---------|----------------------------------------------------------|
| `on`  | boolean | `true`  | `true` simulates the silence, `false` ends it            |

```bash
adb shell am broadcast -a com.cauri.debug.SIMULATE_CAPTURE_SILENCE -p com.cauri.debug --ez on false
```

The choice is kept until it is changed or Cauri's data is cleared.

## Tell a stay at a place

```bash
adb shell am broadcast -a com.cauri.debug.PLACE_VISIT -p com.cauri.debug -e place 3
```

Tells that the user has stayed at the place `3` for five minutes, as its geofence would (CAU-34): a visit opens from
five minutes ago, unless one is open there already. « Mes lieux » counts it at once.

| Extra   | Type    | Default | Values                                                         |
|---------|---------|---------|----------------------------------------------------------------|
| `place` | text    | —       | Id of the place (the `id` of the `places` table); required     |
| `left`  | boolean | `false` | `true` tells that the user left it: the open visit closes      |

```bash
adb shell am broadcast -a com.cauri.debug.PLACE_VISIT -p com.cauri.debug -e place 3 --ez left true
```

As everywhere, the boolean extra is written `--ez`, after the package.

## Make up a stop

```bash
adb shell am broadcast -a com.cauri.debug.PLACE_STOP -p com.cauri.debug -e lat 4.0511 -e lon 9.7679 -e days 5
```

Makes up a stop of half an hour (three positions a quarter of an hour apart) at the position given, `days` days ago,
then looks for the places to suggest as the quarter-hourly worker does (CAU-35), and asks about the first one with a
notification and the card of « Mes lieux ». Three stops on different days, the oldest first, make a place frequent:

```bash
for days in 5 3 1; do adb shell am broadcast -a com.cauri.debug.PLACE_STOP -p com.cauri.debug -e lat 4.0511 -e lon 9.7679 -e days $days; done
```

| Extra  | Type | Default | Values                                                                      |
|--------|------|---------|-----------------------------------------------------------------------------|
| `lat`  | text | —       | Latitude in degrees; required                                               |
| `lon`  | text | —       | Longitude in degrees; required                                              |
| `days` | text | `0`     | How many days ago the stop was                                              |
| `hour` | text | `10`    | Hour it started; from 22 to 4 it is a night, and the place is never asked   |

A place near a declared one, or asked about before, is never asked again. Needs Cauri's notifications allowed for
the notification; the card shows anyway.

## Read what the listener sees

Not a broadcast: debug builds log each notification the listener sees, with its app and title, never its text.

```bash
adb logcat -s CauriCapture
```

`kept · com.samsung.android.messaging · OrangeMoney` for a watched sender, `ignored · …` for the others.
