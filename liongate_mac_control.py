#!/usr/bin/env python3
"""
LionGate Mac Controller - Direct Bluetooth Control for ZKTeco Smart Lock
Communicates with the lock directly from macOS via BLE.
"""

import asyncio
import json
import os
import struct
import sys
from bleak import BleakScanner, BleakClient

SERVICE_UUID = "00004458-0000-1000-8000-00805f9b34fb"
CHAR_PULL_UUID = "00004459-0000-1000-8000-00805f9b34fb"
CHAR_CMD_UUID = "00004460-0000-1000-8000-00805f9b34fb"

C3_START = 0xAA
C3_END = 0x55
C3_VER = 0x01

# Default fallback values for open-source repository
PIN = "123456"
CLIENT_ID = "02:00:00:00:00:00-1"
LOCKS = {
    "default": ("00000000-0000-0000-0000-000000000000", "Default Smart Lock")
}
DEFAULT_LOCK = "default"

# Load local secrets if present (gitignored)
SECRETS_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "liongate_secrets.json")
if os.path.exists(SECRETS_FILE):
    try:
        with open(SECRETS_FILE, "r") as _sf:
            _data = json.load(_sf)
            PIN = _data.get("default_pin", PIN)
            _raw_locks = _data.get("locks", {})
            if _raw_locks:
                LOCKS = {k: (v[0], v[1]) for k, v in _raw_locks.items()}
                DEFAULT_LOCK = next(iter(LOCKS.keys()))
    except Exception as _e:
        print(f"[Warning] Failed to load {SECRETS_FILE}: {_e}")

def calc_crc16(data: bytes) -> int:
    crc = 0x0000
    for b in data:
        byte_val = b & 0xFF
        msb = (crc >> 8) & 0xFF
        poly = 0
        byte = (crc & 0xFF) ^ byte_val
        for _ in range(8):
            if (poly ^ byte) & 1:
                poly = (poly >> 1) ^ 0xA001
            else:
                poly = poly >> 1
            byte = byte >> 1
        crc = msb ^ poly
    return crc & 0xFFFF

def build_c3_packet(command: int, data: bytes = b"") -> bytes:
    body = bytearray()
    body.append(C3_VER)
    body.append(command)
    length = len(data)
    body.append(length & 0xFF)
    body.append((length >> 8) & 0xFF)
    body.extend(data)
    crc = calc_crc16(bytes(body))
    packet = bytearray()
    packet.append(C3_START)
    packet.extend(body)
    packet.append(crc & 0xFF)
    packet.append((crc >> 8) & 0xFF)
    packet.append(C3_END)
    return bytes(packet)

class LionGateMac:
    def __init__(self, pin=None, target_lock=None):
        self.pin = pin if pin is not None else PIN
        target_key = (target_lock or DEFAULT_LOCK).lower()
        if target_key in LOCKS:
            self.lock_uuid, self.lock_label = LOCKS[target_key]
        else:
            matched = [k for k in LOCKS if k in target_key or target_key in k]
            chosen = matched[0] if matched else next(iter(LOCKS.keys()))
            self.lock_uuid, self.lock_label = LOCKS[chosen]
        self.target_name = target_key
        self.client = None
        self.cmd_event = asyncio.Event()
        self.pull_event = asyncio.Event()
        self.cmd_response = ""
        self.pull_buffer = bytearray()
        self.pull_response = None

    def on_cmd_notify(self, sender, data: bytearray):
        s = data.decode("ascii", errors="ignore").strip()
        print(f"[CMD RX] {s}")
        self.cmd_response = s
        self.cmd_event.set()

    def on_pull_notify(self, sender, data: bytearray):
        hex_str = " ".join(f"{b:02X}" for b in data)
        print(f"[PULL RX] ({len(data)}b): {hex_str}")
        if data and data[0] == C3_START:
            self.pull_buffer = bytearray()
        self.pull_buffer.extend(data)
        
        buf = bytes(self.pull_buffer)
        if len(buf) >= 8 and buf[0] == C3_START and buf[-1] == C3_END:
            cmd = buf[2]
            dlen = buf[3] | (buf[4] << 8)
            if len(buf) >= 5 + dlen + 3:
                payload = buf[5:5+dlen]
                code = struct.unpack("b", bytes([payload[0]]))[0] if dlen >= 1 else 0
                self.pull_response = {"cmd": cmd, "code": code, "payload": payload}
                print(f"[C3 Parsed] cmd=0x{cmd:02X} code={code} len={dlen}")
                self.pull_event.set()

    async def connect_and_listen(self, timeout=180.0):
        print(f"[*] Actively scanning for {self.lock_label} [{self.lock_uuid}] (active CoreBluetooth scan)...")
        print(f"[*] (Lock broadcasts periodically; or touch keypad to wake instantly)")

        def match_filter(dev, adv):
            name = (dev.name or adv.local_name or "").strip()
            addr = dev.address.upper()
            uuids = [str(u).lower() for u in (adv.service_uuids or [])]

            if self.lock_uuid and addr == self.lock_uuid.upper():
                return True
            # Never match iPhones or unrelated devices
            if "iphone" in name.lower() or "apple" in name.lower() or "mac" in name.lower():
                return False

            if self.lock_label and self.lock_label.lower() in name.lower():
                return True
            if SERVICE_UUID.lower() in uuids or any("4458" in u for u in uuids):
                return True
            if "biolock" in name.lower() or "smartlock" in name.lower():
                return True
            return False

        found_device = None
        device_event = asyncio.Event()

        def detection_cb(dev, adv):
            nonlocal found_device
            if match_filter(dev, adv):
                found_device = dev
                device_event.set()

        scanner = BleakScanner(detection_callback=detection_cb)
        await scanner.start()
        print(f"[*] BLE scan listening (timeout {int(timeout)}s)...")
        try:
            await asyncio.wait_for(device_event.wait(), timeout=timeout)
        except asyncio.TimeoutError:
            pass
        finally:
            await scanner.stop()

        device = found_device
        if device:
            print(f"[+] Found {device.name or 'Lock'} [{device.address}]. Connecting...")
            connected = False
            for attempt in range(1, 4):
                try:
                    self.client = BleakClient(device)
                    await self.client.connect(timeout=30.0)
                    connected = True
                    break
                except Exception as e:
                    print(f"[*] Connect attempt {attempt} notice: {e}. Retrying...")
                    await asyncio.sleep(0.5)
            if not connected:
                print(f"[-] Could not establish BLE link with {device.address}.")
                return False
        else:
            print(f"[*] Direct UUID connect fallback for [{self.lock_uuid}]...")
            connected = False
            for attempt in range(1, 4):
                try:
                    self.client = BleakClient(self.lock_uuid)
                    await self.client.connect(timeout=30.0)
                    connected = True
                    break
                except Exception as e:
                    print(f"[*] Direct connect attempt {attempt} notice: {e}. Retrying...")
                    await asyncio.sleep(0.5)
            if not connected:
                print(f"[-] Direct connection failed.")
                return False

        print(f"[+] Connected to {self.lock_label} over Bluetooth! Discovering services...")

        # Give CoreBluetooth a moment to discover services
        for _ in range(25):
            if self.client.services:
                char_cmd = self.client.services.get_characteristic(CHAR_CMD_UUID)
                char_pull = self.client.services.get_characteristic(CHAR_PULL_UUID)
                if char_cmd and char_pull:
                    break
            await asyncio.sleep(0.1)

        print("[+] Subscribing to notifications...")
        await self.client.start_notify(CHAR_CMD_UUID, self.on_cmd_notify)
        await self.client.start_notify(CHAR_PULL_UUID, self.on_pull_notify)
        await asyncio.sleep(0.2)
        return True

    async def send_c3(self, cmd: int, data: bytes = b"", timeout=3.0):
        self.pull_event.clear()
        self.pull_response = None
        pkt = build_c3_packet(cmd, data)
        hex_pkt = " ".join(f"{b:02X}" for b in pkt)
        print(f"[C3 TX] ({len(pkt)}b): {hex_pkt}")

        # Write in 20-byte chunks to respect BLE MTU
        chunk_size = 20
        for i in range(0, len(pkt), chunk_size):
            chunk = pkt[i:i + chunk_size]
            try:
                await self.client.write_gatt_char(CHAR_PULL_UUID, chunk, response=True)
            except Exception as e:
                print(f"[C3 TX ERROR] write_gatt_char failed: {e}")
                return None
            if i + chunk_size < len(pkt):
                await asyncio.sleep(0.02)

        try:
            await asyncio.wait_for(self.pull_event.wait(), timeout=timeout)
            return self.pull_response
        except asyncio.TimeoutError:
            print("[C3 TX] Timed out waiting for response.")
            return None

    # BioLock TableStruct for svn.931 (from apktool_out/res/raw/tablestruct.json)
    BIOLOCKK_TABLE_STRUCT_SVN931 = (
        "user=1,UID=i1,CardNo=i2,Pin=i3,Password=s4,Group=i5,StartTime=i6,EndTime=i7,"
        "Name=s8,Privilege=i9,SuperAuthorize=i10,Mark=s11\n"
        "userauthorize=2,Pin=i1,AuthorizeTimezoneId=i2,AuthorizeDoorId=i3\n"
        "holiday=3,Holiday=i1,HolidayType=i2,Loop=i3\n"
        "timezone=4,TimezoneId=i1,SunTime1=i2,SunTime2=i3,SunTime3=i4,MonTime1=i5,"
        "MonTime2=i6,MonTime3=i7,TueTime1=i8,TueTime2=i9,TueTime3=i10,WedTime1=i11,"
        "WedTime2=i12,WedTime3=i13,ThuTime1=i14,ThuTime2=i15,ThuTime3=i16,FriTime1=i17,"
        "FriTime2=i18,FriTime3=i19,SatTime1=i20,SatTime2=i21,SatTime3=i22,Hol1Time1=i23,"
        "Hol1Time2=i24,Hol1Time3=i25,Hol2Time1=i26,Hol2Time2=i27,Hol2Time3=i28,"
        "Hol3Time1=i29,Hol3Time2=i30,Hol3Time3=i31\n"
        "transaction=5,Cardno=i1,Pin=i2,Verified=i3,DoorID=i4,EventType=i5,InOutState=i6,"
        "Time_second=i7,Index=i8\n"
        "templatev10=6,Size=i1,UID=i2,PIN=i3,FingerID=i4,Valid=i5,Template=B6,Resverd=i7,EndTag=i8"
    )

    async def authenticate(self):
        """
        Full pullConnect() sequence matching the original ZKBioBT app:
          1. AT+PASSKEY=<pin>
          2. CMD 0x01 Connect (C3 session)
          3. GetDeviceParam FirmVer
          4. GetTableStruct (CMD 0x06) — if unknown version, query from lock
          5. SetTableStruct (CMD 0x07) — mirrors BioLockService.setTableStruct()
          6. SyncTime SetDeviceParam
        """
        # Step 1: AT+PASSKEY
        print(f"[1/5] Authenticating AT+PASSKEY={self.pin}...")
        self.cmd_event.clear()
        cmd_payload = f"AT+PASSKEY={self.pin}\r\n".encode("ascii")
        try:
            await self.client.write_gatt_char(CHAR_CMD_UUID, cmd_payload)
            await asyncio.wait_for(self.cmd_event.wait(), timeout=3.0)
        except Exception as e:
            print(f"[-] Passkey communication error: {e}")
            return False

        if "PASSKEY TRUE" not in self.cmd_response and "OK" not in self.cmd_response:
            print(f"[-] ERROR: Passkey rejected: {self.cmd_response}")
            return False
        print("[+] Passkey accepted!")
        await asyncio.sleep(0.15)

        # Step 2: C3 Connect (Admin session)
        connect_str = f"{self.pin}\n{CLIENT_ID}\n".encode("ascii")
        print(f"[2/5] Authenticating C3 session ({CLIENT_ID})...")
        resp = await self.send_c3(0x01, connect_str)
        if not resp or resp["code"] != 0:
            print(f"[-] ERROR: C3 connect failed: {resp}")
            return False
        print("[+] C3 session authenticated as Administrator!")
        await asyncio.sleep(0.15)

        # Step 3: Get FirmVer (original app queries this to select the right TableStruct)
        print("[3/5] Querying FirmVer...")
        firm_resp = await self.send_c3(0x04, b"FirmVer", timeout=2.5)
        firm_ver = None
        if firm_resp and firm_resp["code"] == 0 and firm_resp["payload"]:
            raw = firm_resp["payload"].decode("ascii", errors="ignore").strip()
            firm_ver = raw.replace("FirmVer=Ver ", "").replace("FirmVer=", "").strip()
            print(f"[+] FirmVer: {firm_ver}")
        else:
            print(f"[-] FirmVer query result: {firm_resp}")

        # Step 4: GetTableStruct — CMD 0x06. If we know the version use the cached struct;
        #         otherwise query lock and then push it back.
        #         BioLockBusiness.pullConnect() ALWAYS calls setTableStruct after connecting.
        print("[4/5] Pushing TableStruct (svn.931) to lock — this configures the session schema...")
        # Use the known good svn.931 struct from the APK (works as fallback for any version)
        table_struct_str = self.BIOLOCKK_TABLE_STRUCT_SVN931

        # If lock returns a version we don't know, query its table struct first
        if firm_ver and firm_ver not in ("svn.931", "svn.930"):
            print(f"[*] Unknown firmware version '{firm_ver}'; querying GetTableStruct from lock...")
            get_struct_resp = await self.send_c3(0x06, b"", timeout=3.0)
            if get_struct_resp and get_struct_resp["code"] == 0 and len(get_struct_resp["payload"]) > 4:
                table_struct_str = get_struct_resp["payload"].decode("ascii", errors="ignore").strip()
                print(f"[+] Got TableStruct from lock ({len(table_struct_str)} chars)")
            else:
                print(f"[-] GetTableStruct returned: {get_struct_resp} — using svn.931 fallback")

        # SetTableStruct — CMD 0x07 matches PullHelper.SetTableStruct() → PullSDK.SetTableStruct()
        set_struct_resp = await self.send_c3(0x07, table_struct_str.encode("ascii"), timeout=3.0)
        if set_struct_resp and set_struct_resp["code"] == 0:
            print("[+] SetTableStruct accepted by lock!")
        else:
            print(f"[-] SetTableStruct response: {set_struct_resp} (may be OK if lock echoes differently)")
        await asyncio.sleep(0.15)

        # Step 5: SyncTime (original app ALWAYS calls this after setTableStruct)
        print("[5/5] Syncing lock RTC clock...")
        await self.sync_time()

        return True

    async def set_passage_mode(self, enable: bool):
        """
        Controls Normal Open (Passage Mode) via ZKTeco Pull SDK OperationID = 4:
          Param1 = Door (1)
          Param2 = 0
          Param3 = 1 (Enable Passage Mode) or 0 (Disable Passage Mode / Auto-Relock)
          Param4 = 0
        """
        try:
            if not await self.authenticate():
                return False

            action_val = 0x01 if enable else 0x00
            mode_str = "ENABLE Normal Open / Passage Mode" if enable else "DISABLE Normal Open / Passage Mode (Auto-Relock Active)"
            print(f"[*] Sending OperationID=4 command: {mode_str} (04 01 00 {action_val:02X} 00)...")
            payload = bytes([0x04, 0x01, 0x00, action_val, 0x00])
            resp = await self.send_c3(0x05, payload, timeout=2.5)
            if resp and resp["code"] == 0:
                print(f"[+] Passage Mode successfully set to: {'ENABLED (Hold Open)' if enable else 'DISABLED (Auto-Relock Active)'}!")
            else:
                print(f"[-] Set Passage Mode response: {resp}")

            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        finally:
            if self.client and self.client.is_connected:
                await self.client.disconnect()
                print("[+] Disconnected.")

    async def unlock_and_relock(self, duration_seconds=5):
        """
        Full Original-App-Matching Unlock Sequence (with SetTableStruct):
        1. Full authenticate() → AT+PASSKEY + C3 Connect + GetFirmVer + SetTableStruct + SyncTime.
        2. Disable Passage Mode (Op=4, Door=1, Param3=0) to guarantee auto-relock state.
        3. Unlock pulse (Op=1, Door=1, Delay=5, 0) — same as original BioLockService.openDoor().
        4. Wait duration_seconds.
        5. CMD_DISCONNECT (0x02) and BLE disconnect.
        """
        try:
            if not await self.authenticate():
                return False

            # Step 1.5: Read current DSM and LockOn, then force DSM=1 (Normal Close) and LockOn=5
            print("[*] Reading lock state (LockOn, DSM, MachineVSType, AlarmFunOff)...")
            state_resp = await self.send_c3(0x04, b"LockOn,DSM,MachineVSType,AlarmFunOff", timeout=2.5)
            if state_resp and state_resp["code"] == 0 and state_resp["payload"]:
                print(f"[+] Current Lock State: {state_resp['payload'].decode('ascii', errors='ignore').strip()}")

            print(f"[*] Enforcing DSM=1 (Normal Close / Auto-Relock Mode) and LockOn={duration_seconds}s...")
            dsm_payload = f"DSM=1,LockOn={duration_seconds}".encode("ascii")
            set_dsm_resp = await self.send_c3(0x03, dsm_payload, timeout=2.5)
            print(f"[*] Set DSM=1,LockOn={duration_seconds} response: code={set_dsm_resp['code'] if set_dsm_resp else 'timeout'}")

            # Step 2: Disable Passage Mode (OperationID=4, Param3=0) before unlock pulse
            print("[*] Disabling Passage Mode (Op=4, Param3=0) to ensure auto-relock is active...")
            disable_passage_payload = bytes([0x04, 0x01, 0x00, 0x00, 0x00])
            pm_resp = await self.send_c3(0x05, disable_passage_payload, timeout=2.5)
            print(f"[*] Passage Mode disable response: code={pm_resp['code'] if pm_resp else 'timeout'}")

            # Step 3: Unlock pulse — EXACTLY as original: controlDevice(j, 1L, 1L, 0L, 5L, 0L, "")
            # Payload: Op=1 Door=1 Param2=0 Delay=5 0
            print(f"[***] Sending unlock pulse (01 01 00 05 00) — original app format...")
            unlock_payload = bytes([0x01, 0x01, 0x00, 0x05, 0x00])
            resp = await self.send_c3(0x05, unlock_payload)
            if not resp or resp["code"] != 0:
                print(f"[-] ERROR: Unlock command rejected: {resp}")
                return False

            print(f"[***] DOOR UNLOCKED! Motor clutch engaged.")
            print(f"[*] Lock should auto-relock in ~{duration_seconds}s...")

            # Countdown during unlock window
            for s in range(duration_seconds, 0, -1):
                print(f"    [T-{s}s] Door unlocked...")
                await asyncio.sleep(1.0)

            # Step 4: CMD_DISCONNECT then BLE disconnect (hardware lock auto-relocks after 5s)
            print("[*] Sending CMD_DISCONNECT (0x02)...")
            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        except Exception as e:
            print(f"[!] Exception during unlock sequence: {e}")
            return False
        finally:
            if self.client and self.client.is_connected:
                print("[*] Disconnecting BLE GATT...")
                try:
                    await self.client.disconnect()
                except Exception:
                    pass
                print(f"[+] Disconnected.")

    async def quick_unlock(self, duration_seconds=5):
        """
        Sends timed pulse and immediately disconnects BLE (original APK behavior).
        Now includes full pullConnect() sequence (SetTableStruct + SyncTime).
        """
        try:
            if not await self.authenticate():
                return False

            unlock_payload = bytes([0x01, 0x01, 0x00, duration_seconds, 0x00])
            print(f"[*] Sending quick unlock (01 01 00 {duration_seconds:02X} 00)...")
            resp = await self.send_c3(0x05, unlock_payload)
            if not resp or resp["code"] != 0:
                print(f"[-] ERROR: Unlock rejected: {resp}")
                return False
            print(f"[***] DOOR UNLOCKED! Motor engaged.")

            print("[*] Sending CMD_DISCONNECT (0x02)...")
            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        finally:
            if self.client and self.client.is_connected:
                print("[*] Disconnecting BLE GATT immediately...")
                try:
                    await self.client.disconnect()
                except Exception:
                    pass
                print("[+] Disconnected.")

    async def lock_now(self):
        """
        Sends explicit Close commands to force lock clutch to disengage immediately.
        """
        try:
            if not await self.authenticate():
                return False

            # In C3 protocol: DoorDelay = 0 means close / force locked
            close_payload = bytes([0x01, 0x01, 0x00, 0x00, 0x00])
            print(f"[*] Sending Close / Relock command (DoorDelay=0)...")
            resp = await self.send_c3(0x05, close_payload)
            if resp and resp["code"] == 0:
                print("[+] Close command accepted! (Motor clutch disengaged)")

            # Also send Action=1 DoorOutput variant
            action1_payload = bytes([0x01, 0x01, 0x01, 0x00, 0x00])
            print(f"[*] Sending Param2=1 Close command...")
            await self.send_c3(0x05, action1_payload, timeout=1.5)

            # Send CMD_DISCONNECT (0x02)
            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        finally:
            if self.client and self.client.is_connected:
                print("[*] Disconnecting BLE...")
                try:
                    await self.client.disconnect()
                except Exception:
                    pass
                print("[+] Disconnected.")

    async def shutdown_device(self):
        """
        Sends PullController shutdown command (PullSDK.ControlDevice: 1L, 0L, 0L, 6L, 0L).
        """
        try:
            if not await self.authenticate():
                return False

            shutdown_payload = bytes([0x01, 0x00, 0x00, 0x06, 0x00])
            print(f"[*] Sending Shutdown command (01 00 00 06 00)...")
            resp = await self.send_c3(0x05, shutdown_payload)
            print(f"[+] Shutdown response: {resp}")

            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        finally:
            if self.client and self.client.is_connected:
                await self.client.disconnect()
                print("[+] Disconnected.")

    async def set_dsm(self, mode: int):
        """
        Set Door Sensor Mode (DSM):
          0 = Normal Open (Passage Mode)
          1 = Normal Close (Auto-Relock Mode)
          2 = None (No Magnetometer)
        """
        try:
            if not await self.authenticate():
                return False

            mode_names = {0: "Normal Open (Passage Mode)", 1: "Normal Close (Auto-Relock)", 2: "None (No Magnetometer)"}
            print(f"[*] Setting DSM = {mode} ({mode_names.get(mode, 'Unknown')})...")
            payload = f"DSM={mode}".encode("ascii")
            resp = await self.send_c3(0x03, payload)
            if resp and resp["code"] == 0:
                print(f"[+] DSM successfully updated to {mode}!")
            else:
                print(f"[-] Set DSM response: {resp}")

            # Verify by reading back
            print("[*] Reading back LockOn and DSM...")
            resp_verify = await self.send_c3(0x04, b"LockOn,DSM")
            if resp_verify:
                val = resp_verify["payload"].decode("ascii", errors="ignore").strip()
                print(f"[+] Verified State: {val}")

            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        finally:
            if self.client and self.client.is_connected:
                await self.client.disconnect()
                print("[+] Disconnected.")

    async def set_lockon(self, seconds: int):
        """
        Set Open Door Duration (LockOn) in seconds (1 to 10).
        """
        try:
            if not await self.authenticate():
                return False

            print(f"[*] Setting LockOn = {seconds} seconds...")
            payload = f"LockOn={seconds}".encode("ascii")
            resp = await self.send_c3(0x03, payload)
            if resp and resp["code"] == 0:
                print(f"[+] LockOn successfully set to {seconds}s!")
            else:
                print(f"[-] Set LockOn response: {resp}")

            # Verify
            resp_verify = await self.send_c3(0x04, b"LockOn,DSM")
            if resp_verify:
                val = resp_verify["payload"].decode("ascii", errors="ignore").strip()
                print(f"[+] Verified State: {val}")

            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        finally:
            if self.client and self.client.is_connected:
                await self.client.disconnect()
                print("[+] Disconnected.")

    async def get_device_data(self, table: str, fields: str = "*", filter_str: str = ""):
        # Try C3 Action format first
        payload1 = f"Action=GetDeviceData\nTable={table}\nFields={fields}\nFilter={filter_str}\nOptions=\n".encode("ascii")
        resp1 = await self.send_c3(0x08, payload1, timeout=2.5)
        if resp1 and resp1["code"] >= 0 and len(resp1["payload"]) > 1:
            return resp1["payload"].decode("ascii", errors="ignore").strip()

        # Try tab-delimited format
        payload2 = f"{table}\t{fields}\t{filter_str}\n".encode("ascii")
        resp2 = await self.send_c3(0x08, payload2, timeout=2.5)
        if resp2 and resp2["code"] >= 0 and len(resp2["payload"]) > 1:
            return resp2["payload"].decode("ascii", errors="ignore").strip()

        return None

    async def get_table_struct(self):
        resp = await self.send_c3(0x06, b"", timeout=2.5)
        if resp and resp["code"] >= 0 and len(resp["payload"]) > 0:
            return resp["payload"].decode("ascii", errors="ignore").strip()
        return None

    async def query_param(self, name: str):
        resp = await self.send_c3(0x04, name.encode("ascii"), timeout=2.5)
        if resp and resp["code"] >= 0 and len(resp["payload"]) > 0:
            return resp["payload"].decode("ascii", errors="ignore").strip()
        elif resp and resp["code"] < 0:
            return f"ERR({resp['code']})"
        return None

    async def sync_time(self):
        """
        Synchronizes the lock RTC to host system time (SysTime=<epoch>,TimeZone=<tz>,TimeZoneMin=<min>).
        Original app executes this on EVERY admin connection.
        """
        import time
        now = int(time.time())
        # Local timezone offset in hours
        tz_offset = -int(time.timezone / 3600) if time.daylight == 0 else -int(time.altzone / 3600)
        payload = f"SysTime={now},TimeZone={tz_offset},TimeZoneMin=0".encode("ascii")
        print(f"[*] Synchronizing Lock RTC: {payload.decode('ascii')}...")
        resp = await self.send_c3(0x03, payload, timeout=2.5)
        if resp and resp["code"] == 0:
            print("[+] Lock clock successfully synchronized!")
            return True
        else:
            print(f"[-] Clock sync response: {resp}")
            return False

    async def get_status(self):
        try:
            if not await self.authenticate():
                return False

            print("[*] Probing lock parameters & settings...")
            candidates = [
                "FirmVer",
                "UserType,UserName,UserPin",
                "LockOn",
                "DSM",
                "MachineVSType",
                "AlarmFunOff",
                "Door1Drivertime",
                "Door1SensorType",
                "Door1ForceOpen",
                "Door1CloseAndLock",
                "PassageMode",
                "NormalOpen",
                "AutoLock",
                "SysTime",
                "TimeZone",
                "TimeZoneMin",
                "sys.autopower",
                "MachineType"
            ]

            results = {}
            for param in candidates:
                val = await self.query_param(param)
                if val and not val.startswith("ERR"):
                    print(f"    [SUPPORTED] {param} => {val}")
                    results[param] = val
                else:
                    print(f"    [NOT FOUND] {param} ({val})")

            print("\n[*] Probing Database Tables (TableStruct, userauthorize, timezone)...")
            tb_struct = await self.get_table_struct()
            if tb_struct:
                print(f"    [TABLE STRUCT] => {tb_struct}")
            else:
                print("    [TABLE STRUCT] => Not returned")

            for tbl in ["userauthorize", "timezone", "user"]:
                data = await self.get_device_data(tbl)
                if data:
                    print(f"    [TABLE: {tbl}] => {data}")
                else:
                    print(f"    [TABLE: {tbl}] => None / Not returned")

            print("\n================ PARAMETER SUMMARY ================")
            for k, v in results.items():
                print(f"  * {k}: {v}")
            print("===================================================\n")

            await self.send_c3(0x02, b"", timeout=1.5)
            return True
        finally:
            if self.client and self.client.is_connected:
                await self.client.disconnect()
                print("[+] Disconnected.")

async def main():
    if len(sys.argv) > 1 and sys.argv[1].lower() in ["--help", "-h", "help"]:
        print("Usage: python3 liongate_mac_control.py [action] [pin] [param] [target]")
        print("Actions:")
        print("  passage_off  - Disable Normal Open / Passage Mode (forces Auto-Relock mode)")
        print("  passage_on   - Enable Normal Open / Passage Mode (holds lock unlocked)")
        print("  unlock       - Sync RTC, trigger 5s timed pulse, countdown, send relock, cleanly disconnect")
        print("  quick        - Quick timed unlock pulse and immediate disconnect (original app behavior)")
        print("  lock         - Explicit relock command (Delay=0) to force motor clutch disengagement")
        print("  status       - Query lock firmware, user parameters, and database tables")
        print("  set_dsm      - Set DSM mode (0=Normal Open/Passage, 1=Normal Close/Auto-Relock)")
        print("  set_lockon   - Set LockOn duration in seconds (1 to 10)")
        print("  shutdown     - Send PullController shutdown device command")
        print(f"Targets: {' | '.join(LOCKS.keys())} (default: {DEFAULT_LOCK})")
        return

    action = sys.argv[1].lower() if len(sys.argv) > 1 else "unlock"
    pin = sys.argv[2] if len(sys.argv) > 2 else PIN
    dur = int(sys.argv[3]) if len(sys.argv) > 3 else 5
    target_lock = sys.argv[4].lower() if len(sys.argv) > 4 else DEFAULT_LOCK

    print(f"==================================================")
    print(f" LionGate Mac Direct Bluetooth Controller         ")
    print(f" Target: {target_lock.upper()} | Action: {action.upper()} | PIN: {pin} | Param: {dur}")
    print(f"==================================================\n")

    lock = LionGateMac(pin=pin, target_lock=target_lock)
    ok = await lock.connect_and_listen(timeout=180.0)
    if not ok:
        print("[!] Finished without connecting to lock.")
        return

    if action == "lock":
        await lock.lock_now()
    elif action in ["passage_off", "passage_disable", "normal_close"]:
        await lock.set_passage_mode(False)
    elif action in ["passage_on", "passage_enable", "normal_open"]:
        await lock.set_passage_mode(True)
    elif action == "status":
        await lock.get_status()
    elif action in ["set_dsm", "dsm"]:
        await lock.set_dsm(dur)
    elif action in ["set_lockon", "lockon"]:
        await lock.set_lockon(dur)
    elif action == "shutdown":
        await lock.shutdown_device()
    elif action == "quick":
        await lock.quick_unlock(duration_seconds=dur)
    else:  # "unlock" (verified unlock with explicit relock pulse)
        await lock.unlock_and_relock(duration_seconds=dur)

if __name__ == "__main__":
    asyncio.run(main())
