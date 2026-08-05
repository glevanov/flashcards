# Releasing: GitHub Actions → Obtanium

The app is distributed by attaching a signed APK to GitHub releases.
[Obtanium](https://obtainium.imranr.dev/) on the phone watches the repo's
releases and offers updates.

## One-time setup

### 1. Generate a signing keystore (local, never committed)

```bash
keytool -genkeypair -v \
  -keystore release.keystore \
  -storetype PKCS12 \
  -alias flashcards \
  -keyalg RSA -keysize 4096 -validity 10950   # 30 years
```

**The same key must sign every release.** If the key changes, Android refuses
to update the app in place (users would have to uninstall and lose SRS state).

### 2. Add GitHub secrets (repo → Settings → Secrets and variables → Actions)

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 release.keystore` |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | `flashcards` |
| `KEY_PASSWORD` | key password |

### 3. Obtanium on the phone

Add app → enter the GitHub repo URL → done. It will pick up every new release.

## CI workflow (`.github/workflows/release.yml`, added in Phase 5)

Trigger: push of a tag that is a plain integer (e.g. `1`, `2`, `3`).

Steps:

1. Check out repo.
2. Set up JDK 21 + Android SDK.
3. Decode `KEYSTORE_BASE64` to a temp keystore file.
4. `./gradlew assembleRelease` with signing config injected from secrets
   (via environment variables / gradle properties — see
   `app/build.gradle.kts` convention in [AGENTS.md](../AGENTS.md)).
5. Create a GitHub release for the tag and attach
   `app-release.apk` (renamed to include the version, e.g.
   `svenska-flashcards-1.apk`).

## Versioning conventions

- Tags are plain integers: `1`, `2`, `3`, … (no semantic versioning).
- `versionName` in `app/build.gradle.kts` = the tag, as a string.
- **`versionCode` must increase with every release** — bump it in the same
  commit that updates `versionName`, before tagging. Obtanium/Android use it
  to detect upgrades. Keep `versionCode` and the tag number in sync
  (release `N` → `versionCode = N`, `versionName = "N"`).
- Routine workflow: add vocab to `swedish-study` → run `scripts/sync-decks.sh`
  → bump version → commit → `git tag 1 && git push --tags` (next release: `2`, …).
