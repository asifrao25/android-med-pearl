# Firebase Cloud Messaging setup (Stage 13)

Push notifications on Android use **Firebase Cloud Messaging (FCM)** with the same Supabase `push_tokens` table as iOS.

## 1. Create Firebase project

1. Open [Firebase Console](https://console.firebase.google.com/)
2. Create a project (or reuse your Med Pearls project)
3. Add an **Android app** with package name: `com.knowledgepearls.app`
4. Download **`google-services.json`**
5. Place it at: `app/google-services.json` (app module root)

The Gradle build applies the Google Services plugin **only when this file exists**.

## 2. Enable Cloud Messaging

1. In Firebase → Project settings → Cloud Messaging
2. Configure the same Supabase edge functions as iOS (`send-message-push`, `notify-pearl-share`)

## 3. Token registration

The app uploads FCM tokens to `push_tokens` with `platform: "android"` after sign-in and notification permission (Android 13+).

## 4. Test on device

1. Sync Gradle after adding `google-services.json`
2. Sign in to Med Pearls
3. Accept notification permission
4. Send a message or pearl share from another account
5. Tap the notification → inbox / thread opens

## 5. Release SHA-1

Add release keystore SHA-1 to Firebase when preparing Play Store builds.

## Deep links & share target

No Firebase required for these:

- **Share sheet:** Share text/URLs into capture flows
- **Deep links:** `com.knowledgepearls.app://inbox`, `.../shared-pearls/{id}`, `.../messages/{id}`
