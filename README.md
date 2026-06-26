# Med Pearls — Android

Native Android client for **Med Pearls**, matching the iOS app design and sharing the same Supabase backend.

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
