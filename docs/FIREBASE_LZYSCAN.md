# Firebase project: `lzyscan`

Project console: https://console.firebase.google.com/project/lzyscan/overview

## Android apps (one Firebase project, three build flavors)

| Flavor   | Package name                    | Firebase App ID                                      | `google-services.json`              |
|----------|---------------------------------|------------------------------------------------------|-------------------------------------|
| dev      | `dev.sonle.pdfscanner.dev`      | `1:1081452583057:android:cb22548d6ec8fd4ea4c730`   | `app/src/dev/google-services.json`  |
| staging  | `dev.sonle.pdfscanner.staging`  | `1:1081452583057:android:2b6eccfbc8c89928a4c730`   | `app/src/staging/google-services.json` |
| prod     | `dev.sonle.pdfscanner`          | `1:1081452583057:android:90a690c71b5ee141a4c730`   | `app/src/prod/google-services.json` |

Project number: `1081452583057`

## Repo Firebase CLI config

- `.firebaserc` — default project `lzyscan`
- `firebase.json` — Remote Config template deploy
- `remoteconfig.template.json` — server-side defaults (deployed via `firebase deploy --only remoteconfig`)

## Refresh `google-services.json`

```bash
firebase apps:sdkconfig ANDROID <APP_ID> --project lzyscan -o /tmp/out.json
cp /tmp/out.json app/src/<flavor>/google-services.json
```

## Firebase MCP in Cursor

The Firebase MCP server (`plugin-firebase-firebase`) was not enabled in this workspace. Setup was done with **Firebase CLI** (`firebase` 15.x). To use MCP later: install/enable the Firebase extension in Cursor, then tools like `firebase_create_app` and `firebase_get_sdk_config` are available.

## Console checklist (manual)

1. **Analytics** — enabled automatically with the Android apps.
2. **Crashlytics** — open Crashlytics in console once; run a debug build on a device to register the app.
3. **Performance** — enabled via Gradle plugin `firebase-perf`; verify in Performance dashboard after first sessions.
4. **Remote Config** — template deployed from this repo; edit parameters in console or update `remoteconfig.template.json` and redeploy.

## CI secrets (optional)

Update GitHub Actions secrets if you use base64-encoded configs:

- `FIREBASE_CONFIG_DEV` / `FIREBASE_CONFIG_STAGING` / `FIREBASE_CONFIG_PROD`

Encode current files:

```bash
base64 -i app/src/dev/google-services.json | pbcopy
```
