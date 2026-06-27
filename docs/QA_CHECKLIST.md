# Med Pearls Android — QA checklist (Stage 14)

Run on a physical device with Android Studio. Test light and dark appearance where noted.

## Auth & profile

- [ ] Email OTP sign-in and sign-out
- [ ] Google sign-in (requires `google.web.client.id` in `local.properties`)
- [ ] Profile setup gate for new accounts
- [ ] Edit profile and avatar upload

## My Feed & capture

- [ ] Create pearl (text, link, clinical case, media)
- [ ] Edit and delete pearl
- [ ] Tag filter and content-type picker
- [ ] Pearl detail opens correctly

## Folders & favourites

- [ ] Create/rename/delete folder
- [ ] Move pearl to folder
- [ ] Favourite / unfavourite

## Public feed

- [ ] New / Seen tabs and badge count
- [ ] Open pearl detail; media opens **in-app** (photo, video, PDF, doc)
- [ ] Like, comment, share to public
- [ ] Submit pearl for review; pending list

## Inbox & messaging

- [ ] Message thread send/receive (iOS ↔ Android)
- [ ] Pearl share receive, accept, decline
- [ ] Unread badge on tab bar reminder chip

## Settings

- [ ] Appearance (system / light / dark)
- [ ] Backup now and restore
- [ ] Clear device cache (success alert)
- [ ] Scheduled backup worker scheduled on app start

## Connectivity overlays (Stage 11)

- [ ] Airplane mode → offline alert; continue offline
- [ ] Server unreachable → backend alert; retry
- [ ] Connection restored toast after recovery

## Push, deep links & share (Stage 13)

Requires `app/google-services.json` for push.

- [ ] Notification permission prompt (Android 13+)
- [ ] Push on new message → tap opens conversation
- [ ] Push on pearl share → tap opens shared pearl
- [ ] Deep link: `com.knowledgepearls.app://inbox`
- [ ] Deep link: `com.knowledgepearls.app://inbox/shared-pearls/{id}`
- [ ] Deep link: `com.knowledgepearls.app://inbox/messages/{conversationId}`
- [ ] Share sheet: URL/text → capture import flow
- [ ] Open `.pearl` / JSON file → import flow

## Accessibility

- [ ] TalkBack: tab bar announces tab names and selection
- [ ] TalkBack: primary actions on feed cards and settings rows
- [ ] Sufficient contrast in light and dark themes

## Release build

- [ ] `./gradlew :app:assembleRelease` with signing config
- [ ] Smoke test release APK (minify + ProGuard)
- [ ] No crashes on cold start after install

## Play Store (when ready)

- [ ] Store listing copy and screenshots (light + dark)
- [ ] Internal testing track upload
- [ ] Release SHA-1 added to Firebase
