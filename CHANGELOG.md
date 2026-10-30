# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Planned
- Quick Settings tile / Home screen widget for instant 1-tap unlocking.
- Lock access logs & event history viewer.
- Battery level monitoring indicator.

## [0.1.0] - 2026-10-30

### Added
- **Modern 64-bit Android App:** Rebuilt from the ground up targeting Android 14+ (API 34) in Kotlin and Jetpack Compose Material 3, replacing the legacy 32-bit `ZKBioBT` APK.
- **Single-Pulse Timed Unlock:** Reverse-engineered the proprietary C3 Pull protocol (`01 01 00 05 00`) matching PL10B hardware specifications.
- **Autonomous Auto-Relocking:** Allows the PL10B lock's internal hardware timer to mechanically disengage the motor clutch automatically after 5 seconds.
- **macOS Direct BLE Controller:** Direct Python BLE CLI interface ([`liongate_mac_control.py`](./liongate_mac_control.py)) for lock control, status queries, and configuration directly from macOS.
- **Automated Cloud CI/CD:** GitHub Actions workflow (`.github/workflows/build-apk.yml`) that compiles the Android APK in the cloud and automatically publishes releases with binary attachments upon tagging.
- **Decoupled Security Model:** Support for `liongate_secrets.json` and `local.properties` to keep personal lock PINs, MAC addresses, and UUIDs protected from Git commits while allowing open-source distribution.
