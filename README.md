# LionGate (ZKBioBT Smart Lock)

[![Build and Release LionGate APK](https://github.com/jay3oh5/project-liongate/actions/workflows/build-apk.yml/badge.svg)](https://github.com/jay3oh5/project-liongate/actions/workflows/build-apk.yml)
[![Latest Release](https://img.shields.io/github/v/release/jay3oh5/project-liongate?color=blue)](https://github.com/jay3oh5/project-liongate/releases/latest)
[![Android Target](https://img.shields.io/badge/Android-14%2B%20(API%2034)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**LionGate** is a modern, 64-bit Android application and macOS desktop controller designed for **ZKTeco PL10B** smart biometric locks. 

It completely replaces the obsolete 32-bit vendor companion app (`ZKBioBT 1.5.2`), allowing modern Android 14+ devices (such as Samsung Galaxy S24 Ultra) and macOS workstations to seamlessly communicate with and unlock the door via Bluetooth Low Energy (BLE).

---

## 🚀 Key Features

* **64-bit Native Android 14+ Support:** Built using Kotlin, Jetpack Compose, and Material 3 dark theme. Runs smoothly on modern 64-bit-only Android processors.
* **Hardware-Autonomous Protocol:** Reverse-engineered single-pulse C3 Pull protocol (`01 01 00 05 00`). The lock autonomously engages and disengages the mechanical clutch using its 5-second internal hardware timer.
* **macOS Direct Desktop Controller:** Native Python CLI controller powered by CoreBluetooth (`bleak` / `pyobjc`) for remote unlocking, RTC time syncing, status diagnostics, and passage mode configuration from Mac.
* **Decoupled Security Architecture:** Zero private credentials (PINs, MAC addresses, hardware UUIDs) are committed to Git. Local overrides (`liongate_secrets.json` and `local.properties`) keep your physical lock secure while enabling open-source development.
* **Automated Cloud Builds (CI/CD):** Every tagged release is compiled directly in GitHub Actions cloud runners and attached as an installable `.apk` asset in under 2 minutes.

---

## 📲 Download & Installation

1. Navigate to the **[Latest Release](https://github.com/jay3oh5/project-liongate/releases/latest)**.
2. Download **`LionGate-v0.1.0.apk`** directly to your Android device.
3. Tap the file to install (enable "Install unknown apps" if prompted).
4. Launch **LionGate**, set your lock's PIN and MAC address in the settings dialog, and tap **Unlock**.

---

## 🖥️ macOS Desktop CLI Usage

LionGate includes a standalone Python desktop client for macOS:

```bash
# Display help and available commands
python3 liongate_mac_control.py help

# Trigger a standard 5-second timed unlock
python3 liongate_mac_control.py unlock

# Force motor clutch to disengage immediately
python3 liongate_mac_control.py lock

# Enable Normally Open (Passage) mode (holds door unlocked)
python3 liongate_mac_control.py passage_on

# Disable Normally Open mode (forces auto-relocking)
python3 liongate_mac_control.py passage_off

# Query lock diagnostics, user database tables, and battery parameters
python3 liongate_mac_control.py status
```

---

## 🔒 Security & Local Configuration

To run locally without hardcoding private lock details in source code:

1. **For macOS Python Controller:**
   Copy the example secrets file:
   ```bash
   cp liongate_secrets.example.json liongate_secrets.json
   ```
   Edit `liongate_secrets.json` with your lock's default PIN, MAC address, and CoreBluetooth UUIDs. This file is gitignored.

2. **For Android Gradle Builds:**
   Create `liongate-app/local.properties`:
   ```properties
   LIONGATE_DEFAULT_PIN=123456
   LIONGATE_DEFAULT_MAC=AA:BB:CC:DD:EE:FF
   ```
   These values are automatically injected into `BuildConfig.DEFAULT_PIN` and `BuildConfig.DEFAULT_MAC` during compilation.

---

## 🛠️ Project Structure

```text
├── .github/workflows/          # GitHub Actions CI/CD (Cloud build & release)
├── liongate-app/               # Modern 64-bit Android application
│   ├── app/src/main/java/      # Jetpack Compose UI & BLE Manager
│   └── build.gradle.kts        # Kotlin DSL Gradle configuration
├── liongate_mac_control.py     # macOS CoreBluetooth BLE CLI tool
├── liongate_secrets.example.json # Safe template for local credentials
├── AGENTS.md                   # AI agent guide and handoff specifications
├── CHANGELOG.md                # Release version history
└── README.md                   # Project overview & documentation
```

---

## 📄 License

This project is licensed under the MIT License.
