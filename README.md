# Cauri

Cauri helps people in Cameroon track where their money goes and save more of it. It follows the four places money lives day to day: cash, MTN MoMo, Orange Money and the bank. Digital payments are detected from notifications; cash, which is most of the spending, is tracked through one-tap habits, visits to places and regular cash counts. Everything stays on the phone.

The context for Claude Code is in [`CLAUDE.md`](CLAUDE.md), which also links the product documents.

## Requirements

- A recent Android Studio (compatible with AGP 8.13), JDK 17
- An Android 10+ phone

## Getting started

Open the folder in Android Studio and let it sync, or from a terminal:

```bash
./gradlew assembleDebug        # debug APK: app/build/outputs/apk/debug/
./gradlew installDebug         # installs on the connected phone
```

Checks run by CI on every pull request:

```bash
./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug
```

Fix formatting with `./gradlew ktlintFormat`.

## First sync

The dependency versions in `gradle/libs.versions.toml` are the ones Eifi already builds with on this machine, but the skeleton was written without access to the Maven repositories and has not been compiled yet. If the first sync fails on a missing or incompatible version, adjust the catalog: Android Studio suggests the available versions.

## Signing

- **debug**: Android Studio's local debug key, `applicationId` suffixed with `.debug`, so both variants can be installed side by side.
- **release**: copy `keystore.properties.example` to `keystore.properties` and fill it in. This file and the `.jks` files are ignored by git. Without it, the release build is unsigned.

Create the key once:

```bash
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/cauri-release.jks -alias cauri -keyalg RSA -keysize 4096 -validity 10000
```

## Structure

```
app/                    application, navigation between features
feature/onboarding      profile, payment habits, categories, accounts, permissions
feature/home            home screen, cash first
feature/cash            cash count and split of the gap
feature/transactions    quick entry, edit sheet, activity
core/design             Compose theme (light and dark) and components
core/data               DataStore preferences, Room database
core/capture            notification listening (MoMo, Orange Money, banks, SMS)
core/parsing            message templates and parsing (pure Kotlin)
core/engine             reminder rules and contextual bandit (pure Kotlin)
build-logic/            Gradle convention plugins
config/                 CI workflow and detekt rules
```

The CI workflow lives in `config/ci/ci.yml`; copy it to `.github/workflows/` once the repository is on GitHub.
