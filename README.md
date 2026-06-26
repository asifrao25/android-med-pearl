# Med Pearls — Android

Native Android client for **Med Pearls**, matching the iOS app design and sharing the same Supabase backend.

## Fix build error `What went wrong: 25.0.1`

**Cause:** macOS default Java is **25**, but Gradle/Kotlin need **JDK 17 or 21**.

### One-time fix

```bash
cd "/Users/m4-mac/Documents/Xcode-projects/Android studio/Med-Pearls-Android"
./scripts/setup-gradle-jdk.sh
./gradlew assembleDebug
```

If the script says no JDK 21 found, install one:

```bash
brew install --cask temurin@21
./scripts/setup-gradle-jdk.sh
```

### Android Studio settings (both required)

1. **Settings → Build Tools → Gradle → Gradle JDK** = **Embedded JDK (21)**
2. **File → Project Structure → SDK** = **Embedded JDK (21)**  
   *(fixes “Project JDK is invalid” / `#USE_PROJECT_JDK`)*

Then **File → Sync Project with Gradle Files**.

### SDK location not found

`local.properties` is missing (gitignored — not synced). Run:

```bash
cd "/Users/asif/.m4sync/mirror/Xcode-projects/Android studio/Med-Pearls-Android"
./scripts/setup-local-env.sh
```

**Or manual one-liner** (default SDK path after Android Studio install):

```bash
cd "/Users/asif/.m4sync/mirror/Xcode-projects/Android studio/Med-Pearls-Android"
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

If your SDK is on an external drive, use that path instead, e.g.:

```bash
echo 'sdk.dir=/Volumes/AI_SSD/Android/Sdk' > local.properties
```

Then **File → Sync Project with Gradle Files** and **Run ▶**.

---

## Fix “Invalid Gradle JDK configuration” (CLI)

Studio error about `#GRADLE_LOCAL_JAVA_HOME` means `gradle/config.properties` is missing.

Run once from the **project folder** (not `~`):

```bash
cd "/Users/m4-mac/Documents/Xcode-projects/Android studio/Med-Pearls-Android"
./scripts/setup-gradle-jdk.sh
```

Then **quit Android Studio completely** and reopen the project → **Sync Project with Gradle Files**.

Or click the banner: **Use Embedded JDK**.

**Manual fix** (if script fails):

```bash
cd "/Users/m4-mac/Documents/Xcode-projects/Android studio/Med-Pearls-Android"
mkdir -p gradle
echo 'java.home=/Applications/Android Studio.app/Contents/jbr/Contents/Home' > gradle/config.properties
```

## Open in Android Studio (MacBook)

1. Sync this folder to your MacBook (same path or any location).
2. Open **Android Studio** → *Open* → select this directory.
3. Let Gradle sync complete (downloads SDK components on first open).
4. Create `local.properties` with your SDK path (Android Studio usually creates this automatically):

```properties
sdk.dir=/Users/YOUR_USER/Library/Android/sdk
```

5. Run on emulator or device: **Run ▶ MedPearls**.

## Design approach

**Look and feel like iOS, respect Android:**

| iOS | Android equivalent |
|-----|-------------------|
| SwiftUI liquid glass | Compose `LiquidBackground` + frosted surfaces |
| SF Symbols | Material Icons Extended (mapped per screen) |
| `ultraThinMaterial` | Blur on API 31+; translucent surface fallback on API 26–30 |
| Sheet presentations | Modal bottom sheets + edge-to-edge |
| HapticFeedback | `HapticFeedbackConstants` on key actions |
| Apple Sign In | Not included — Google + Email only |

Design tokens live in `app/src/main/java/com/knowledgepearls/app/ui/theme/`.

## Project

| Item | Value |
|------|-------|
| App name | Med Pearls |
| Package | `com.knowledgepearls.app` |
| Min SDK | 26 |
| Backend | `https://pearls-api.asifrao.com` |

## Plan & progress

See [ANDROID_MIGRATION_PLAN.md](./ANDROID_MIGRATION_PLAN.md) for the full parity roadmap.

## Remote

```bash
git remote -v
# origin  git@github.com:asifrao25/android-med-pearl.git
```

## Assets (TODO on MacBook)

Replace placeholder launcher icon with exports from iOS:

`Knowledge Pearls/KnowledgePearls/Resources/Assets.xcassets/AppIcon.appiconset/`
`Knowledge Pearls/KnowledgePearls/Resources/Assets.xcassets/AppLogo.imageset/`
