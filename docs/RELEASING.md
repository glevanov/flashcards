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

Trigger: manual (`workflow_dispatch`) from the Actions tab — no tag push
needed. The workflow asks for the version number (plain integer), validates
it against the latest release, builds, and creates the release + tag itself.

Steps:

1. Check out repo.
2. Set up JDK 21 + Android SDK.
3. Decode `KEYSTORE_BASE64` to a temp keystore file.
4. `./gradlew assembleRelease` with signing config injected from secrets
   (via environment variables / gradle properties — see
   `app/build.gradle.kts` convention in [AGENTS.md](../AGENTS.md)).
5. Create a GitHub release for the tag and attach
   `app-release.apk` (renamed to include the version, e.g.
   `svenska-flashcards-1.apk`). The tag is created at the commit that
   was built.

## Versioning conventions

- Versions are plain integers: `1`, `2`, `3`, … (no semantic versioning).
- The version is decided at release time: run the **Release** workflow
  manually (Actions tab) and enter the version number. It must be greater
  than the latest release — the workflow enforces this.
- The workflow builds with `-PversionCode=N -PversionName=N`, so the APK's
  `versionCode` and `versionName` both equal the release number. Obtanium and
  Android use `versionCode` to detect upgrades.
- Also bump the committed `versionCode`/`versionName` in
  `app/build.gradle.kts` in the same commit, so the repo state matches the
  released version.
- Routine workflow: add vocab to `swedish-study` → run
  `scripts/sync-decks.sh` → bump version → commit → push → run the
  **Release** workflow manually with the new version number.
