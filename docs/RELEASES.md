# Android Releases

GitHub Releases contain signed Android APKs for this repository. `versionName` and `versionCode` in `app/build.gradle.kts` are the version source of truth.

## Current release

| Version | APK | Notes |
|---|---|---|
| 1.4.2 | [Download Android APK](https://github.com/smartzheng/smart-netease/releases/download/v1.4.2/smart-netease-android-1.4.2.apk) | First release from the independently maintained Android project. |

## Build a release locally

1. Set the version in `app/build.gradle.kts`.
2. Configure `keystore.properties` locally. The file is ignored by Git; never publish the keystore or its passwords.
3. Build with `./gradlew :app:assembleRelease` (or `gradlew.bat` on a Windows development host).
4. Verify the APK signature and install it on a test device before publishing.
5. Create a versioned GitHub Release and attach the signed APK. Keep the release notes focused on Android user-visible changes.

Typical APK output: `app/build/outputs/apk/release/app-release.apk`.
