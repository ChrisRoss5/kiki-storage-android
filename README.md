# Kiki Storage (Android)

College project (App development for mobile devices S5): A cloud storage app with excellent file management, using storage and camera access for file uploads and attachments for chat with Gemini AI.

Kotlin sibling of [kiki-storage-web](https://github.com/ChrisRoss5/kiki-storage-web). Same Firebase backends: Auth, Firestore, Storage, Functions. Chat uses Vertex AI (`firebase-vertexai`).

## Run

Open the repo in Android Studio (Gradle project `Kiki Storage`, module `:app`).

- `applicationId` / namespace: `dev.k1k1.kikistorage`
- minSdk 26, targetSdk / compileSdk 34
- Needs `app/google-services.json` (already in the tree) and the secrets Gradle plugin for `BuildConfig` keys

## Layout

- `HomeFragment` - file explorer
- `GeminiFragment` - chat with attachments
- `AccountFragment` - account
- `worker/` - WorkManager upload/download

## Known issues

- Fix some icons in dark mode
- Handle edge cases when moving folders
