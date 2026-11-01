# Repository Guide

## Stack

- Kotlin, Jetpack Compose, Android SDK 34, Gradle 8.4, Python 3 (Bleak / PyObjC)

## Commands

- Setup: `python3 setup_colab.py` (Google Colab build environment)
- Build (GitHub Actions): Automated on push / workflow_dispatch in `.github/workflows/build-apk.yml`
- Build (Colab Cloud): `colab exec -s liongate-build -f fast_build.py`
- Mac Control: `python3 liongate_mac_control.py unlock`

## Architecture

- `liongate-app/`: Modern 64-bit Android application targeting Android 14+ (Samsung Galaxy S24 Ultra).
- `liongate_mac_control.py`: Direct macOS CoreBluetooth CLI interface for ZKTeco PL10B.
- `liongate_secrets.json`: Local private credentials file (gitignored). Contains default PIN, MAC address, and lock UUIDs.
- `liongate-app/local.properties`: Local developer Android configuration (gitignored). Injects `BuildConfig.DEFAULT_PIN` and `BuildConfig.DEFAULT_MAC`.

## Guardrails

- Private Credentials: Real lock PINs, MACs, and UUIDs are kept exclusively in `liongate_secrets.json` and `local.properties`. AI agents should read `liongate_secrets.json` to obtain target lock parameters without prompting the user.
- Cloud-First Heavy Computing: Local machine has limited CPU/RAM; never run heavy Gradle builds or APK decompilations locally. Always dispatch to GitHub Actions or Google Colab (`colab-cli`).
- Never commit secrets, binaries, or decompiled vendor code to Git.

<!-- agent-state:start -->
Task: Decouple Connection, Support Dual Credentials (Admin SuperPwd), and Fix Passage Mode
Base: c84dd8c
Status: idle
Next: Perform live hardware verification of Passage Mode and persistent connection with user
Blockers: None
Verified: Dispatched to Google Jules (session 7444395691256774146), applied patch, resolved TelemetryAmber in SettingsScreen, built successfully on GitHub Actions (run 41272184506), and installed to Samsung Galaxy S24 Ultra via ADB (Streamed Install Success).
<!-- agent-state:end -->
