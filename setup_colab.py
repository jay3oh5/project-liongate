import os
import subprocess
import sys

def run_cmd(cmd):
    print(f"==> {cmd}", flush=True)
    res = subprocess.run(cmd, shell=True)
    if res.returncode != 0:
        print(f"Failed: {cmd}", flush=True)
        sys.exit(res.returncode)

if not os.path.exists("/content/gradle-8.4"):
    print("Setting up Gradle 8.4...", flush=True)
    run_cmd("wget -q https://services.gradle.org/distributions/gradle-8.4-bin.zip -O /content/gradle.zip")
    run_cmd("unzip -q -o /content/gradle.zip -d /content/")

sdk_root = "/content/android-sdk"
if not os.path.exists(sdk_root):
    print("Setting up Android SDK 34...", flush=True)
    os.makedirs(f"{sdk_root}/cmdline-tools", exist_ok=True)
    run_cmd("wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /content/cmdline-tools.zip")
    run_cmd(f"unzip -q -o /content/cmdline-tools.zip -d {sdk_root}/cmdline-tools/")
    if os.path.exists(f"{sdk_root}/cmdline-tools/cmdline-tools"):
        os.rename(f"{sdk_root}/cmdline-tools/cmdline-tools", f"{sdk_root}/cmdline-tools/latest")
    run_cmd(f'yes | {sdk_root}/cmdline-tools/latest/bin/sdkmanager --sdk_root={sdk_root} "platforms;android-34" "build-tools;34.0.0"')

print("SETUP COMPLETE!", flush=True)
