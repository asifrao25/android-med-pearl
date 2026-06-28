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

## Fix duplicate `androidx.activity.compose.R` (dex merge)

**Cause:** Stale `app/build/` folders synced via m4sync, or multiple Activity Compose versions on the classpath.

On your **MacBook** after syncing:

```bash
cd "/Users/asif/.m4sync/mirror/Xcode-projects/Android studio/Med-Pearls-Android"
./scripts/clean-build.sh
```

Then **Build → Rebuild Project** in Android Studio.

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

## Google Sign-In setup

**Yes — do this now.** Stage 4 app code is done; you only need Google Cloud + Supabase + `local.properties`.

Use the **same Google Cloud project** as iOS if you already have one (one Web client can serve Supabase for both platforms).

### 1. Google Cloud Console

1. Open [Google Cloud Console](https://console.cloud.google.com/) → select your project (or **Create project** → e.g. `Med Pearls`).
2. **APIs & Services → OAuth consent screen**
   - User type: **External** (unless Workspace-only)
   - App name: **Med Pearls**
   - User support email: your email
   - Developer contact: your email
   - Scopes: keep defaults (`email`, `profile`, `openid`) — **Save**
   - If External: add yourself as a **Test user** until the app is published
3. **APIs & Services → Credentials → Create credentials → OAuth client ID**

#### A. Web application (required)

| Field | Value |
|-------|--------|
| Name | `Med Pearls Web (Supabase)` |
| Authorized redirect URIs | `https://pearls-api.asifrao.com/auth/v1/callback` |

Copy the **Client ID** and **Client secret** — you need both for Supabase.

> This Web client ID is **not** the Android client ID. Android reads this same Web ID via `google.web.client.id` for Credential Manager.

#### B. Android (required for native account picker)

| Field | Value |
|-------|--------|
| Name | `Med Pearls Android` |
| Package name | `com.knowledgepearls.app` |
| SHA-1 certificate fingerprint | from debug keystore (below) |

**Get debug SHA-1** on your MacBook:

```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android -keypass android | rg "SHA1:"
```

Paste the `SHA1: AA:BB:…` value into the Android OAuth client. Add a **second** Android client (or extra fingerprint) when you have a release/upload keystore.

### 2. Supabase (pearls-api.asifrao.com)

1. **Authentication → Providers → Google**
   - Enable Google
   - **Client ID** = Web application client ID from step 1A
   - **Client secret** = Web application secret from step 1A
   - Save
2. **Authentication → URL configuration**
   - **Site URL**: your admin/site URL (can stay as-is)
   - **Redirect URLs** must include:
     ```
     com.knowledgepearls.app://login-callback
     ```
   - (iOS uses the same redirect — likely already listed)

### 3. Android app (`local.properties`)

On the MacBook, edit `local.properties` (gitignored):

```properties
sdk.dir=/Users/asif/Library/Android/sdk
google.web.client.id=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com
```

Use the **Web** client ID from step 1A — not the Android client ID.

Rebuild:

```bash
./gradlew assembleDebug
```

Or **Build → Rebuild Project** in Android Studio.

### 4. Test on device/emulator

1. Use an emulator **with Google Play** (not AOSP without Play Services).
2. Open app → **Settings** (gear) → **Sign in** → **Continue with Google**.
3. **With `google.web.client.id` set:** native Google account sheet (Credential Manager).
4. **Without it:** browser OAuth fallback (still works if Supabase Google provider is on).

**Same Supabase user as iOS:** sign in with the same Google account — one `auth.users` row for both apps.

### Troubleshooting

| Symptom | Fix |
|---------|-----|
| `DEVELOPER_ERROR` / `10:` on Google button | Android OAuth client missing, wrong package name, or wrong SHA-1 |
| Browser opens but redirect fails | Add `com.knowledgepearls.app://login-callback` in Supabase redirect URLs |
| `NoCredentialException` | Normal for first-time users; app retries with all accounts, or use browser fallback |
| Sign-in works on iOS, not Android | Add Android OAuth client + SHA-1; Web client alone is not enough for native picker |

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
