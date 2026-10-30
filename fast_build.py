import os
import subprocess
import sys
import shutil

sdk_root = "/content/android-sdk"
run_cmd = lambda c: subprocess.run(c, shell=True, check=True)

run_cmd("tar -xzf /content/liongate-app.tar.gz -C /content/")

with open("/content/liongate-app/local.properties", "w") as f:
    f.write("sdk.dir=" + sdk_root + "\n")

build_env = os.environ.copy()
build_env["ANDROID_HOME"] = sdk_root

proc = subprocess.Popen(
    ["/content/gradle-8.4/bin/gradle", "assembleDebug", "--no-daemon"],
    cwd="/content/liongate-app",
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True,
    env=build_env
)

for line in proc.stdout:
    sys.stdout.write(line)
    sys.stdout.flush()

proc.wait()
if proc.returncode == 0:
    apk = "/content/liongate-app/app/build/outputs/apk/debug/app-debug.apk"
    shutil.copy(apk, "/content/liongate-debug.apk")
    print("SUCCESS: /content/liongate-debug.apk is ready!", flush=True)
else:
    print(f"FAILED with returncode {proc.returncode}", flush=True)
    sys.exit(proc.returncode)
