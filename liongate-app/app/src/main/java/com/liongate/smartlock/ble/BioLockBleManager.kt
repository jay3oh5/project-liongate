package com.liongate.smartlock.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.liongate.smartlock.protocol.ZkProtocol
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

sealed class LockConnectionState {
    object Disconnected : LockConnectionState()
    object Scanning : LockConnectionState()
    data class Connecting(val deviceName: String) : LockConnectionState()
    data class Connected(val deviceName: String, val address: String) : LockConnectionState()
    object Unlocking : LockConnectionState()
    data class Unlocked(val message: String) : LockConnectionState()
    data class Error(val message: String) : LockConnectionState()
}

@SuppressLint("MissingPermission")
class BioLockBleManager(private val context: Context) {
    companion object {
        private const val TAG = "BioLockBleManager"
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectionState = MutableStateFlow<LockConnectionState>(LockConnectionState.Disconnected)
    val connectionState: StateFlow<LockConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _logMessage = MutableStateFlow<String>("")
    val logMessage: StateFlow<String> = _logMessage.asStateFlow()

    private val _isPassageMode = MutableStateFlow(false)
    val isPassageMode: StateFlow<Boolean> = _isPassageMode.asStateFlow()

    private val _lockOnSeconds = MutableStateFlow(5)
    val lockOnSeconds: StateFlow<Int> = _lockOnSeconds.asStateFlow()

    private val _doorSensorMode = MutableStateFlow(1)
    val doorSensorMode: StateFlow<Int> = _doorSensorMode.asStateFlow()

    private val _lastTimeSync = MutableStateFlow<Long?>(null)
    val lastTimeSync: StateFlow<Long?> = _lastTimeSync.asStateFlow()

    private var activeGatt: BluetoothGatt? = null
    private var pullChar: BluetoothGattCharacteristic? = null
    private var cmdChar: BluetoothGattCharacteristic? = null

    private var sessionId: Int? = null
    private var requestNr: Int = 0
    private var currentPin: String = "123456"
    private var userPin: Int = 2
    private val pullRxBuffer = java.io.ByteArrayOutputStream()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun buildSessionC3Message(command: Byte, data: ByteArray? = null): ByteArray {
        val sId = sessionId
        val rNr = if (sId != null) {
            requestNr++
            requestNr
        } else null
        return ZkProtocol.buildC3Message(
            command = command,
            sessionId = sId,
            requestNr = rNr,
            data = data
        )
    }

    private var pendingDescriptorWrite: CompletableDeferred<Boolean>? = null
    private var pendingCharWrite: CompletableDeferred<Boolean>? = null
    private var pendingPasskeyResponse: CompletableDeferred<Boolean>? = null
    private var pendingPullResponse: CompletableDeferred<ZkProtocol.C3Response>? = null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun startScan() {
        if (!isBluetoothEnabled()) {
            _connectionState.value = LockConnectionState.Error("Bluetooth is turned off")
            return
        }

        _discoveredDevices.value = emptyList()
        _connectionState.value = LockConnectionState.Scanning
        log("Scanning for nearby locks...")

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            _connectionState.value = LockConnectionState.Error("BLE scanner unavailable")
            return
        }

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(ZkProtocol.UUID_SERVICE))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        // Scan with filter and also broad scan for device names like ML, PL, AL, BioLock
        try {
            scanner.startScan(listOf(filter), settings, scanCallback)
        } catch (e: Exception) {
            log("Filtered scan error, trying open scan: ${e.message}")
            scanner.startScan(scanCallback)
        }

        mainHandler.postDelayed({
            stopScan()
            if (_connectionState.value is LockConnectionState.Scanning) {
                _connectionState.value = LockConnectionState.Disconnected
            }
        }, 12000)
    }

    fun stopScan() {
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan", e)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { dev ->
                val name = dev.name ?: result.scanRecord?.deviceName ?: "Smart Lock (${dev.address.takeLast(5)})"
                val item = BleDiscoveredDevice(dev, name, dev.address, result.rssi)
                val currentList = _discoveredDevices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.address == dev.address }
                if (existingIndex >= 0) {
                    currentList[existingIndex] = item
                } else {
                    currentList.add(item)
                    log("Found: $name [${dev.address}] (RSSI: ${result.rssi})")
                }
                _discoveredDevices.value = currentList
            }
        }

        override fun onScanFailed(errorCode: Int) {
            log("Scan failed: code $errorCode")
            _connectionState.value = LockConnectionState.Error("Scan failed: $errorCode")
        }
    }

    fun scanAndUnlock(targetAddress: String, pin: String) {
        log("Listening for lock $targetAddress (touch lock to wake if needed)...")
        _connectionState.value = LockConnectionState.Scanning
        currentPin = pin.ifEmpty { "123456" }

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            connectAndUnlock(targetAddress, pin)
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            private var connected = false
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (connected) return
                val dev = result?.device ?: return
                if (dev.address.equals(targetAddress, ignoreCase = true)) {
                    connected = true
                    try { scanner.stopScan(this) } catch (e: Exception) {}
                    log("Lock detected active! [${dev.address}] (RSSI: ${result.rssi}). Connecting now...")
                    connectAndUnlock(dev, currentPin)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                log("scanAndUnlock scan error: $errorCode, falling back to direct connect")
                connectAndUnlock(targetAddress, pin)
            }
        }

        try {
            scanner.startScan(null, settings, callback)
        } catch (e: Exception) {
            log("Scan error: ${e.message}, falling back to direct connect")
            connectAndUnlock(targetAddress, pin)
            return
        }

        mainHandler.postDelayed({
            try { scanner.stopScan(callback) } catch (e: Exception) {}
        }, 30000)
    }

    fun connectAndUnlock(address: String, pin: String) {
        val device = try {
            bluetoothAdapter?.getRemoteDevice(address)
        } catch (e: Exception) {
            null
        }

        if (device != null) {
            connectAndUnlock(device, pin)
        } else {
            _connectionState.value = LockConnectionState.Error("Invalid lock address: $address")
        }
    }

    fun connectAndUnlock(device: BluetoothDevice, pin: String) {
        stopScan()
        currentPin = pin.ifEmpty { "123456" }
        sessionId = null
        requestNr = 0

        try {
            activeGatt?.disconnect()
            activeGatt?.close()
            activeGatt = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing previous gatt", e)
        }

        val name = device.name ?: device.address
        _connectionState.value = LockConnectionState.Connecting(name)
        log("Connecting to $name...")

        activeGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
            activeGatt = null
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
        _connectionState.value = LockConnectionState.Disconnected
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            log("GATT state change: status=$status, newState=$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED && gatt != null) {
                log("GATT connected! Requesting MTU 247...")
                _connectionState.value = LockConnectionState.Connected(
                    gatt.device.name ?: "Lock",
                    gatt.device.address
                )
                pullRxBuffer.reset()
                gatt.requestMtu(247)
                mainHandler.postDelayed({
                    val res = gatt.discoverServices()
                    log("discoverServices returned: $res")
                }, 400)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                log("GATT disconnected (status $status)")
                _connectionState.value = LockConnectionState.Disconnected
                try {
                    gatt?.close()
                    activeGatt = null
                } catch (e: Exception) {}
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            log("MTU negotiation completed: mtu=$mtu, status=$status")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                val service = gatt.getService(ZkProtocol.UUID_SERVICE)
                if (service == null) {
                    log("Lock service not found! Inspecting available services...")
                    for (s in gatt.services) {
                        log("Service: ${s.uuid}")
                    }
                    _connectionState.value = LockConnectionState.Error("Lock service not found")
                    return
                }

                pullChar = service.getCharacteristic(ZkProtocol.UUID_CHAR_PULL)
                cmdChar = service.getCharacteristic(ZkProtocol.UUID_CHAR_CMD)

                log("Lock services discovered. Starting unlock sequence...")

                scope.launch {
                    executeUnlockSequence(gatt)
                }
            } else {
                log("Service discovery failed: $status")
                _connectionState.value = LockConnectionState.Error("Service discovery failed")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            log("Descriptor written: ${descriptor?.characteristic?.uuid} status: $status")
            pendingDescriptorWrite?.complete(status == BluetoothGatt.GATT_SUCCESS)
            pendingDescriptorWrite = null
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            log("Char written: ${characteristic?.uuid} status: $status")
            pendingCharWrite?.complete(status == BluetoothGatt.GATT_SUCCESS)
            pendingCharWrite = null
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { handleCharacteristicData(characteristic.uuid, it) }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleCharacteristicData(characteristic.uuid, value)
        }
    }

    private fun handleCharacteristicData(uuid: UUID, value: ByteArray) {
        val hex = value.joinToString("") { "%02X ".format(it) }

        if (uuid == ZkProtocol.UUID_CHAR_CMD) {
            val responseStr = String(value, Charsets.US_ASCII).trim()
            log("CMD received: '$responseStr' ($hex)")

            if (responseStr.contains("PASSKEY TRUE") || responseStr.contains("-PASSKEY TRUE") || responseStr.contains("OK")) {
                pendingPasskeyResponse?.complete(true)
            } else if (responseStr.contains("PASSKEY FALSE")) {
                pendingPasskeyResponse?.complete(false)
            }
        } else if (uuid == ZkProtocol.UUID_CHAR_PULL) {
            log("PULL received: $hex")
            if (value.isNotEmpty() && value[0] == ZkProtocol.C3_MESSAGE_START) {
                pullRxBuffer.reset()
            }
            pullRxBuffer.write(value)
            val accumulated = pullRxBuffer.toByteArray()
            val resp = ZkProtocol.parseC3Response(accumulated)
            if (resp != null) {
                pullRxBuffer.reset()
                log("C3 parsed -> cmd=0x${"%02X".format(resp.command)} success=${resp.isSuccess} code=${resp.returnCode} len=${resp.data.size}")

                if (resp.isSuccess && resp.data.size >= 4) {
                    val sId = (resp.data[0].toInt() and 0xFF) or ((resp.data[1].toInt() and 0xFF) shl 8)
                    sessionId = sId
                    val rNr = (resp.data[2].toInt() and 0xFF) or ((resp.data[3].toInt() and 0xFF) shl 8)
                    requestNr = rNr
                    log("Session ID established: 0x${"%04X".format(sId)}, requestNr=$rNr")
                }

                pendingPullResponse?.complete(resp)
            } else {
                log("PULL fragment/raw (${accumulated.size}b accumulated): $hex")
            }
        }
    }

    private suspend fun sendC3Packet(
        gatt: BluetoothGatt,
        packet: ByteArray,
        timeoutMs: Long = 2500L
    ): ZkProtocol.C3Response? {
        val deferred = CompletableDeferred<ZkProtocol.C3Response>()
        pendingPullResponse = deferred
        val hex = packet.joinToString("") { "%02X ".format(it) }
        log("C3 TX (${packet.size}b): $hex")
        val ok = pullChar?.let { writeCharacteristicSequentially(gatt, it, packet) } ?: false
        if (!ok) {
            log("C3 TX write failed")
            return null
        }
        val resp = withTimeoutOrNull(timeoutMs) { deferred.await() }
        if (resp == null) {
            log("C3 RX timed out after ${timeoutMs}ms")
        }
        return resp
    }

    private suspend fun executeUnlockSequence(gatt: BluetoothGatt) {
        // Step 1: Enable notifications on CMD
        log("Enabling notifications on CMD characteristic...")
        enableNotification(gatt, cmdChar)
        delay(150)

        // Step 2: Enable notifications on PULL
        log("Enabling notifications on PULL characteristic...")
        enableNotification(gatt, pullChar)
        delay(200)

        // Step 3: Send Passkey AT command
        _connectionState.value = LockConnectionState.Unlocking
        val pin = currentPin.ifEmpty { "123456" }
        log("Verifying Passkey ($pin)...")
        val passkeyDeferred = CompletableDeferred<Boolean>()
        pendingPasskeyResponse = passkeyDeferred

        val passkeyCmd = ZkProtocol.buildPasskeyCommand(pin)
        cmdChar?.let { writeCharacteristicSequentially(gatt, it, passkeyCmd) }

        val passkeyOk = withTimeoutOrNull(4000) { passkeyDeferred.await() } ?: false
        if (!passkeyOk) {
            log("PIN verification rejected by lock (or timed out)")
            _connectionState.value = LockConnectionState.Error("PIN rejected or lock timeout ($pin)")
            disconnect()
            return
        }
        log("Passkey accepted! Authenticating C3 Pull session...")
        delay(150)

        // Step 4: C3 Handshake / Authentication & Privilege Discovery
        val phoneMac = bluetoothAdapter?.address ?: "02:00:00:00:00:00"
        val candidates = listOf(
            "$pin\n02:00:00:00:00:00-1\n", // Administrator session
            "$pin\n$phoneMac-1\n",
            "$pin\n1-1\n",
            "$pin\n",
            "$pin\n\n",
            "12312124234\n",
            "\n"
        )

        var authenticated = false
        var currentUserType = -1
        var lastReturnCode = -999

        for (cand in candidates) {
            val displayPwd = cand.replace("\n", "\\n")
            log("Trying C3 Connect (pwd='$displayPwd')...")
            val payload = cand.toByteArray(Charsets.US_ASCII)
            val msg = ZkProtocol.buildC3Message(
                command = ZkProtocol.CMD_CONNECT,
                sessionId = null,
                requestNr = null,
                data = payload
            )
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2000)
            if (resp != null && resp.isSuccess) {
                lastReturnCode = resp.returnCode
                log("C3 Connect accepted by lock with '$displayPwd'! Querying UserType...")
                delay(100)

                // Query device parameter context
                val getParamMsg = ZkProtocol.buildC3Message(
                    command = 0x04.toByte(), // CMD_GET_DEVICE_PARAM
                    sessionId = null,
                    requestNr = null,
                    data = "UserType,UserName,UserPin".toByteArray(Charsets.US_ASCII)
                )
                val paramResp = sendC3Packet(gatt, getParamMsg, timeoutMs = 2000)
                var uType = -1
                if (paramResp != null) {
                    val pStr = String(paramResp.data, Charsets.US_ASCII).trim()
                    log("Device context: $pStr")
                    val match = Regex("UserType=(\\d+)").find(pStr)
                    uType = match?.groupValues?.get(1)?.toIntOrNull() ?: -1

                    val pinMatch = Regex("UserPin=(\\d+)").find(pStr)
                    userPin = pinMatch?.groupValues?.get(1)?.toIntOrNull() ?: 2
                }

                currentUserType = uType
                authenticated = true

                if (uType > 0) {
                    log("SUCCESS! Privileged user session established (UserType=$uType > 0)! Authorized to operate door.")
                    break
                } else if (uType == 0) {
                    log("Candidate returned UserType=0 (AUTH_NO_ACCESS). Checking next candidate for privileged access...")
                } else {
                    log("C3 Connect accepted, UserType unknown ($uType). Proceeding.")
                    break
                }
            } else if (resp != null) {
                log("C3 Connect rejected: code=${resp.returnCode}")
            } else {
                log("C3 Connect timed out")
            }
            delay(100)
        }

        if (!authenticated) {
            log("C3 Authentication failed for all candidates. Last code: $lastReturnCode")
            _connectionState.value = LockConnectionState.Error("Lock auth failed (code $lastReturnCode)")
            disconnect()
            return
        }

        // If UserType is still 0 (AUTH_NO_ACCESS), try granting Admin privilege via CMD_SET_DATA (CMD 0x07)
        if (currentUserType == 0) {
            log("UserType is 0 (AUTH_NO_ACCESS). Attempting to grant Admin privilege via CMD_SET_DATA...")
            try {
                val setUserData = "user\r\nPin=12\tPrivilege=2\tName=12\tMark=$phoneMac-1001\r\n"
                val setUserMsg = ZkProtocol.buildC3Message(
                    command = 0x07.toByte(), // CMD_SET_DATA_NEW
                    sessionId = null,
                    requestNr = null,
                    data = setUserData.toByteArray(Charsets.US_ASCII)
                )
                val setResp = sendC3Packet(gatt, setUserMsg, timeoutMs = 2000)
                log("SetDeviceData resp: code=${setResp?.returnCode}")
            } catch (e: Exception) {
                log("SetDeviceData failed: ${e.message}")
            }
        }

        delay(300)

        // Step 5: Synchronize Lock RTC (mandated by BioLock firmware for countdown timers)
        try {
            val epoch = System.currentTimeMillis() / 1000
            val tz = java.util.TimeZone.getDefault()
            val tzOffset = tz.getOffset(System.currentTimeMillis()) / 3600000
            val tzMin = Math.abs(tz.getOffset(System.currentTimeMillis()) / 60000) % 60
            val syncPayload = "SysTime=$epoch,TimeZone=$tzOffset,TimeZoneMin=$tzMin".toByteArray(Charsets.US_ASCII)

            log("Syncing lock RTC with session header: SysTime=$epoch, Tz=$tzOffset...")
            val syncMsg = buildSessionC3Message(
                command = 0x03.toByte(), // CMD_SET_DEVICE_PARAM
                data = syncPayload
            )
            var syncResp = sendC3Packet(gatt, syncMsg, timeoutMs = 2500)
            if (syncResp == null || !syncResp.isSuccess) {
                log("Session RTC sync returned code ${syncResp?.returnCode}. Retrying session-less...")
                val fallbackSyncMsg = ZkProtocol.buildC3Message(
                    command = 0x03.toByte(),
                    data = syncPayload
                )
                syncResp = sendC3Packet(gatt, fallbackSyncMsg, timeoutMs = 2000)
            }
            log("Lock RTC sync status: ${if (syncResp?.isSuccess == true) "SUCCESS" else "FAILED"} (code ${syncResp?.returnCode})")
        } catch (e: Exception) {
            log("Lock RTC sync notice: ${e.message}")
        }

        delay(150)

        // Step 5b: Push TableStruct (svn.931) as the official app does on every pullConnect
        try {
            val tableStruct = ("user=1,UID=i1,CardNo=i2,Pin=i3,Password=s4,Group=i5,StartTime=i6,EndTime=i7,Name=s8,Privilege=i9,SuperAuthorize=i10,Mark=s11\n" +
                "userauthorize=2,Pin=i1,AuthorizeTimezoneId=i2,AuthorizeDoorId=i3\n" +
                "holiday=3,Holiday=i1,HolidayType=i2,Loop=i3\n" +
                "timezone=4,TimezoneId=i1,SunTime1=i2,SunTime2=i3,SunTime3=i4,MonTime1=i5,MonTime2=i6,MonTime3=i7,TueTime1=i8,TueTime2=i9,TueTime3=i10,WedTime1=i11,WedTime2=i12,WedTime3=i13,ThuTime1=i14,ThuTime2=i15,ThuTime3=i16,FriTime1=i17,FriTime2=i18,FriTime3=i19,SatTime1=i20,SatTime2=i21,SatTime3=i22,Hol1Time1=i23,Hol1Time2=i24,Hol1Time3=i25,Hol2Time1=i26,Hol2Time2=i27,Hol2Time3=i28,Hol3Time1=i29,Hol3Time2=i30,Hol3Time3=i31\n" +
                "transaction=5,Cardno=i1,Pin=i2,Verified=i3,DoorID=i4,EventType=i5,InOutState=i6,Time_second=i7,Index=i8\n" +
                "templatev10=6,Size=i1,UID=i2,PIN=i3,FingerID=i4,Valid=i5,Template=B6,Resverd=i7,EndTag=i8").toByteArray(Charsets.US_ASCII)
            log("Pushing TableStruct (svn.931) to lock...")
            val setTableMsg = ZkProtocol.buildC3Message(
                command = 0x07.toByte(), // CMD_SET_DATA_NEW
                data = tableStruct
            )
            val setTableResp = sendC3Packet(gatt, setTableMsg, timeoutMs = 2500)
            log("SetTableStruct result: ${setTableResp?.returnCode}")
        } catch (e: Exception) {
            log("SetTableStruct notice: ${e.message}")
        }

        delay(150)

        // Step 5c: Read current lock state (DSM, LockOn, etc.) and enforce DSM=1 (Normal Close / Auto-Relock)
        try {
            val queryStateMsg = ZkProtocol.buildC3Message(
                command = 0x04.toByte(),
                data = "LockOn,DSM,MachineVSType,AlarmFunOff".toByteArray(Charsets.US_ASCII)
            )
            val stateResp = sendC3Packet(gatt, queryStateMsg, timeoutMs = 2000)
            if (stateResp != null) {
                val sStr = String(stateResp.data, Charsets.US_ASCII).trim()
                log("Current Lock State from hardware: $sStr")
            }

            log("Enforcing DSM=1 (Normal Close / Auto-Relock Mode) and LockOn=5...")
            val dsmPayload = "DSM=1,LockOn=5".toByteArray(Charsets.US_ASCII)
            val setDsmMsg = ZkProtocol.buildC3Message(
                command = 0x03.toByte(),
                data = dsmPayload
            )
            val setDsmResp = sendC3Packet(gatt, setDsmMsg, timeoutMs = 2000)
            log("Set DSM=1,LockOn=5 result: code=${setDsmResp?.returnCode}")

            val disablePassagePayload = byteArrayOf(0x04, 0x01, 0x00, 0x00, 0x00)
            val disablePassageMsg = ZkProtocol.buildC3Message(
                command = ZkProtocol.CMD_CONTROL,
                data = disablePassagePayload
            )
            val pmResp = sendC3Packet(gatt, disablePassageMsg, timeoutMs = 1500)
            log("Passage mode disable (Op=4) result: code=${pmResp?.returnCode}")
        } catch (e: Exception) {
            log("Lock state configuration notice: ${e.message}")
        }

        delay(150)

        // Step 6: Send C3 Timed Unlock Pulse
        val durationSeconds = 5
        log("Initiating C3 Unlock pulse for ${durationSeconds}s...")

        data class ControlCandidate(
            val name: String,
            val sessionId: Int?,
            val requestNr: Int?,
            val payload: ByteArray
        )

        val baseTimedPayload = byteArrayOf(0x01, 0x01, 0x00, durationSeconds.toByte(), 0x00)
        val pinTimedPayload = baseTimedPayload + "PIN=$userPin".toByteArray(Charsets.US_ASCII)

        val sId = sessionId
        val rNr = requestNr

        val controlAttempts = mutableListOf<ControlCandidate>()
        if (sId != null) {
            // Priority 1: Session authenticated pulse with PIN attribution (as sent by official BioLock app)
            controlAttempts.add(ControlCandidate("Session pulse with PIN ($userPin)", sId, rNr + 1, pinTimedPayload))
            // Priority 2: Session authenticated standard pulse
            controlAttempts.add(ControlCandidate("Session standard pulse", sId, rNr + 1, baseTimedPayload))
        }
        // Priority 3: Sessionless pulse with PIN
        controlAttempts.add(ControlCandidate("Sessionless pulse with PIN ($userPin)", null, null, pinTimedPayload))
        // Priority 4: Sessionless standard pulse
        controlAttempts.add(ControlCandidate("Sessionless standard pulse", null, null, baseTimedPayload))

        var unlockSuccess = false
        var lastErr = -999

        for (att in controlAttempts) {
            log("Attempting unlock: ${att.name}...")
            val msg = ZkProtocol.buildC3Message(
                command = ZkProtocol.CMD_CONTROL,
                sessionId = att.sessionId,
                requestNr = att.requestNr,
                data = att.payload
            )
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2500)
            if (resp != null) {
                lastErr = resp.returnCode
                if (resp.isSuccess) {
                    log("Unlock SUCCESS with '${att.name}'! (code ${resp.returnCode})")
                    unlockSuccess = true
                    if (att.requestNr != null) {
                        requestNr = att.requestNr
                    }
                    break
                } else {
                    log("Unlock candidate '${att.name}' rejected: code=${resp.returnCode}")
                }
            } else {
                log("Unlock candidate '${att.name}' timed out")
            }
            delay(150)
        }

        if (unlockSuccess) {
            log("Door unlocked! Starting ${durationSeconds}s countdown...")
            _connectionState.value = LockConnectionState.Unlocked("Unlocked (Auto-locking in ${durationSeconds}s)")

            scope.launch {
                try {
                    // Update user UI countdown
                    for (s in durationSeconds downTo 1) {
                        _connectionState.value = LockConnectionState.Unlocked("Unlocked ($s s)")
                        delay(1000)
                    }

                    // Release session cleanly (hardware lock disengages clutch autonomously after 5s)
                    log("Releasing C3 session and closing BLE connection...")
                    val discMsg = buildSessionC3Message(command = ZkProtocol.CMD_DISCONNECT)
                    sendC3Packet(gatt, discMsg, timeoutMs = 1000)
                } catch (e: Exception) {
                    log("Disconnect sequence notice: ${e.message}")
                } finally {
                    disconnect()
                    _connectionState.value = LockConnectionState.Disconnected
                    log("Door cycle finished and BLE link cleanly closed.")
                }
            }
        } else {
            log("All unlock candidates rejected. Last code: $lastErr")
            _connectionState.value = LockConnectionState.Error("Lock rejected unlock (code $lastErr)")
            disconnect()
        }
    }

    private suspend fun enableNotification(gatt: BluetoothGatt, char: BluetoothGattCharacteristic?) {
        if (char == null) return
        gatt.setCharacteristicNotification(char, true)
        val descriptor = char.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID) ?: return

        val latch = CompletableDeferred<Boolean>()
        pendingDescriptorWrite = latch

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }

        withTimeoutOrNull(2000) { latch.await() }
    }

    private suspend fun writeCharacteristicSequentially(
        gatt: BluetoothGatt,
        char: BluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        var offset = 0
        while (offset < data.size) {
            val chunkSize = minOf(20, data.size - offset)
            val chunk = data.copyOfRange(offset, offset + chunkSize)

            val latch = CompletableDeferred<Boolean>()
            pendingCharWrite = latch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(char, chunk, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                @Suppress("DEPRECATION")
                char.value = chunk
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(char)
            }

            val success = withTimeoutOrNull(2000) { latch.await() } ?: false
            if (!success) {
                log("Chunk write timed out at offset $offset")
                return false
            }
            offset += chunkSize
            delay(15)
        }
        return true
    }

    suspend fun setPassageMode(enable: Boolean): Result<Boolean> {
        val gatt = activeGatt ?: return Result.failure(IllegalStateException("Not connected"))
        return try {
            log("Sending Passage Mode (Op=4, enable=$enable)...")
            val payload = ZkProtocol.buildPassageModePayload(enable)
            val msg = buildSessionC3Message(command = ZkProtocol.CMD_CONTROL, data = payload)
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2500)
            if (resp != null && resp.isSuccess) {
                _isPassageMode.value = enable
                log("Passage Mode updated: enable=$enable (code=${resp.returnCode})")
                Result.success(true)
            } else {
                val err = resp?.returnCode ?: -1
                log("Passage Mode rejected: code=$err")
                Result.failure(RuntimeException("Rejected by lock: $err"))
            }
        } catch (e: Exception) {
            log("Error setting passage mode: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun lockNow(): Result<Boolean> {
        val gatt = activeGatt ?: return Result.failure(IllegalStateException("Not connected"))
        return try {
            log("Sending immediate Force Lock (Op=1, duration=0s)...")
            val payload = ZkProtocol.buildForceLockPayload()
            val msg = buildSessionC3Message(command = ZkProtocol.CMD_CONTROL, data = payload)
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2000)
            if (resp != null && resp.isSuccess) {
                _isPassageMode.value = false
                log("Force Lock accepted: motor clutch disengaged")
                Result.success(true)
            } else {
                val err = resp?.returnCode ?: -1
                log("Force Lock rejected: code=$err")
                Result.failure(RuntimeException("Rejected: $err"))
            }
        } catch (e: Exception) {
            log("Error sending force lock: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun setLockOnDuration(seconds: Int): Result<Boolean> {
        val gatt = activeGatt ?: return Result.failure(IllegalStateException("Not connected"))
        return try {
            val clamped = seconds.coerceIn(1, 10)
            log("Setting LockOn duration = ${clamped}s...")
            val payload = ZkProtocol.buildSetLockOnPayload(clamped)
            val msg = buildSessionC3Message(command = ZkProtocol.CMD_SET_PARAM, data = payload)
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2000)
            if (resp != null && resp.isSuccess) {
                _lockOnSeconds.value = clamped
                log("LockOn duration updated to ${clamped}s")
                Result.success(true)
            } else {
                val err = resp?.returnCode ?: -1
                log("Set LockOn rejected: code=$err")
                Result.failure(RuntimeException("Rejected: $err"))
            }
        } catch (e: Exception) {
            log("Error setting LockOn: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun setDoorSensorMode(mode: Int): Result<Boolean> {
        val gatt = activeGatt ?: return Result.failure(IllegalStateException("Not connected"))
        return try {
            log("Setting DSM mode = $mode...")
            val payload = ZkProtocol.buildSetDsmPayload(mode)
            val msg = buildSessionC3Message(command = ZkProtocol.CMD_SET_PARAM, data = payload)
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2000)
            if (resp != null && resp.isSuccess) {
                _doorSensorMode.value = mode
                log("DSM mode updated to $mode")
                Result.success(true)
            } else {
                val err = resp?.returnCode ?: -1
                log("Set DSM rejected: code=$err")
                Result.failure(RuntimeException("Rejected: $err"))
            }
        } catch (e: Exception) {
            log("Error setting DSM: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun syncTime(): Result<Boolean> {
        val gatt = activeGatt ?: return Result.failure(IllegalStateException("Not connected"))
        return try {
            val epoch = System.currentTimeMillis() / 1000
            val tz = java.util.TimeZone.getDefault()
            val tzOffset = tz.getOffset(System.currentTimeMillis()) / 3600000
            val payload = ZkProtocol.buildSyncTimePayload(epoch, tzOffset)
            log("Syncing Lock RTC: SysTime=$epoch, TimeZone=$tzOffset...")
            val msg = buildSessionC3Message(command = ZkProtocol.CMD_SET_PARAM, data = payload)
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2500)
            if (resp != null && resp.isSuccess) {
                _lastTimeSync.value = System.currentTimeMillis()
                log("Lock RTC synchronized successfully!")
                Result.success(true)
            } else {
                val err = resp?.returnCode ?: -1
                log("RTC sync rejected: code=$err")
                Result.failure(RuntimeException("Rejected: $err"))
            }
        } catch (e: Exception) {
            log("Error syncing RTC: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun queryLockParams(): Result<Map<String, String>> {
        val gatt = activeGatt ?: return Result.failure(IllegalStateException("Not connected"))
        return try {
            log("Querying lock parameters (LockOn, DSM, etc.)...")
            val payload = ZkProtocol.buildGetDeviceParamPayload("LockOn,DSM,MachineVSType,AlarmFunOff")
            val msg = buildSessionC3Message(command = ZkProtocol.CMD_GET_PARAM, data = payload)
            val resp = sendC3Packet(gatt, msg, timeoutMs = 2500)
            if (resp != null) {
                val str = String(resp.data, Charsets.US_ASCII).trim()
                log("Lock params response: $str")
                val map = mutableMapOf<String, String>()
                str.split(",").forEach { pair ->
                    val parts = pair.split("=")
                    if (parts.size == 2) {
                        map[parts[0].trim()] = parts[1].trim()
                    }
                }
                map["LockOn"]?.toIntOrNull()?.let { _lockOnSeconds.value = it }
                map["DSM"]?.toIntOrNull()?.let { _doorSensorMode.value = it }
                Result.success(map)
            } else {
                Result.failure(RuntimeException("No response from lock"))
            }
        } catch (e: Exception) {
            log("Error querying lock parameters: ${e.message}")
            Result.failure(e)
        }
    }

    private fun log(msg: String) {
        Log.d(TAG, msg)
        _logMessage.value = msg
    }
}
