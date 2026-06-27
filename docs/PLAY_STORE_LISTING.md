# Play Store listing — Med Pearls (Android)

Use this copy when creating the Google Play Console listing. Adjust after final screenshots are captured.

## App name

**Med Pearls**

## Short description (80 chars max)

Capture, organize, and share medical knowledge pearls with your community.

## Full description

Med Pearls helps clinicians and learners capture clinical knowledge quickly — then organize, favourite, and share it with colleagues.

**Capture anything**
- Quick text pearls
- Web links with previews
- Clinical cases with structured fields
- Photos, camera, PDFs, and documents

**Stay organized**
- My Feed with tags and filters
- Folders and favourites
- Local backup and restore

**Community & collaboration**
- Public Feed — browse community pearls (New / Seen)
- Submit pearls for review
- Inbox for messages and friend shares (iOS ↔ Android)
- Push notifications for new messages and shares

**Built for daily use**
- Dark-first design with light mode option
- Offline-aware with clear connection alerts
- Share links and files into Med Pearls from other apps

Same account and backend as Med Pearls on iOS. Sign in with email or Google.

## Category

Medical

## Tags / keywords (internal)

medical education, clinical pearls, knowledge management, flashcards, case studies, physician, healthcare

## Content rating

Complete the Play Console questionnaire. App contains user-generated medical/educational content and messaging.

## Privacy policy URL

Add your hosted privacy policy URL (match iOS App Store listing if shared).

## Contact email

Add support email for Play Console.

## Screenshots checklist

Capture on phone (1080×1920 or device-native) in **light** and **dark** mode:

1. Splash / My Feed
2. Pearl detail with media
3. Capture options sheet
4. Public Feed (New tab with badge)
5. Public pearl detail + in-app media viewer
6. Inbox (messages + shared pearls)
7. Settings (appearance, backup)
8. Folders or favourites

Optional 7-inch tablet screenshots if supporting tablets.

## Feature graphic

1024×500 PNG — Med Pearls logo on gradient background matching app tab themes.

## Release notes (v1.0.0)

Initial Android release:
- My Feed, capture, folders, favourites
- Public Feed with engagement
- Inbox, messaging, and cross-platform pearl shares
- Push notifications, deep links, and share target
- Backup, cache management, and offline alerts

## Internal testing upload

1. Create release keystore and add to `local.properties` / CI
2. `./gradlew :app:bundleRelease`
3. Play Console → Testing → Internal testing → Create release
4. Upload `app/build/outputs/bundle/release/app-release.aab`
5. Add testers by email list

See `docs/QA_CHECKLIST.md` before promoting to production.
