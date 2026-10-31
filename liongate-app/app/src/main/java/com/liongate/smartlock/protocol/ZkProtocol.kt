package com.liongate.smartlock.protocol

import java.util.UUID

object ZkProtocol {
    val UUID_SERVICE: UUID = UUID.fromString("00004458-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_PULL: UUID = UUID.fromString("00004459-0000-1000-8000-00805f9b34fb")
    val UUID_CHAR_CMD: UUID = UUID.fromString("00004460-0000-1000-8000-00805f9b34fb")

    const val C3_MESSAGE_START: Byte = 0xAA.toByte()
    const val C3_MESSAGE_END: Byte = 0x55.toByte()
    const val C3_PROTOCOL_VERSION: Byte = 0x01.toByte()

    const val CMD_CONNECT: Byte = 0x01.toByte()
    const val CMD_DISCONNECT: Byte = 0x02.toByte()
    const val CMD_SET_PARAM: Byte = 0x03.toByte()
    const val CMD_GET_PARAM: Byte = 0x04.toByte()
    const val CMD_CONTROL: Byte = 0x05.toByte()
    const val CMD_GET_TABLE_STRUCT: Byte = 0x06.toByte()
    const val CMD_SET_TABLE_STRUCT: Byte = 0x07.toByte()
    const val CMD_TABLE_DATA: Byte = 0x08.toByte()
    const val CMD_DELETE_DATA: Byte = 0x0A.toByte()
    const val CMD_SET_DATA: Byte = 0x0B.toByte()
    const val CMD_CONNECT_SESSION: Byte = 0x76.toByte()

    const val CMD_ACK_OK: Int = 200        // 0xC8
    const val CMD_ACK_OK_NEW: Int = 201    // 0xC9
    const val CMD_ACK_ERROR_NEW: Int = 204 // 0xCC
    const val CMD_ACK_ERROR: Int = 205     // 0xCD

    const val DEFAULT_PULL_PASSWORD = "12312124234"

    data class C3Response(
        val command: Int,
        val isSuccess: Boolean,
        val returnCode: Int,
        val data: ByteArray,
        val raw: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as C3Response
            return command == other.command && isSuccess == other.isSuccess && returnCode == other.returnCode
        }

        override fun hashCode(): Int {
            var result = command
            result = 31 * result + isSuccess.hashCode()
            result = 31 * result + returnCode
            return result
        }
    }

    fun parseC3Response(packet: ByteArray): C3Response? {
        if (packet.size < 7 || packet[0] != C3_MESSAGE_START || packet.last() != C3_MESSAGE_END) {
            return null
        }
        val command = packet[2].toInt() and 0xFF
        val dataLen = (packet[3].toInt() and 0xFF) or ((packet[4].toInt() and 0xFF) shl 8)
        if (packet.size < 5 + dataLen + 3) {
            return null
        }
        val payload = packet.copyOfRange(5, 5 + dataLen)

        var isSuccess = false
        var returnCode = 0
        if (command == CMD_ACK_OK) {
            isSuccess = true
            returnCode = 0
        } else if (command == CMD_ACK_OK_NEW) {
            if (dataLen == 1) {
                returnCode = payload[0].toInt() // signed byte (-13, -14, 0, etc.)
                isSuccess = (returnCode >= 0)
            } else if (dataLen >= 4) {
                // Session response packet: [sessionId(2 bytes), requestNr(2 bytes), ...]
                returnCode = 0
                isSuccess = true
            } else {
                isSuccess = true
                returnCode = 0
            }
        } else {
            returnCode = if (dataLen >= 1) payload[0].toInt() else -1
            isSuccess = false
        }
        return C3Response(command, isSuccess, returnCode, payload, packet)
    }

    fun buildPasskeyCommand(pin: String): ByteArray {
        val cleanPin = pin.trim()
        return "AT+PASSKEY=$cleanPin\r\n".toByteArray(Charsets.US_ASCII)
    }

    private fun calcDivisor(byteIn: Int): Int {
        var byte = byteIn
        var poly = 0
        for (i in 0 until 8) {
            poly = if (((poly xor byte) and 1) == 1) {
                (poly ushr 1) xor 0xA001
            } else {
                poly ushr 1
            }
            byte = byte ushr 1
        }
        return poly
    }

    fun crc16(data: ByteArray): Int {
        var crc = 0x0000
        for (b in data) {
            val byteVal = b.toInt() and 0xFF
            val msb = crc ushr 8
            crc = msb xor calcDivisor((crc and 0xFF) xor byteVal)
        }
        return crc and 0xFFFF
    }

    fun buildC3Message(
        command: Byte,
        sessionId: Int? = null,
        requestNr: Int? = null,
        data: ByteArray? = null
    ): ByteArray {
        val payloadLen = (data?.size ?: 0) + (if (sessionId != null && requestNr != null) 4 else 0)
        val body = mutableListOf<Byte>()
        body.add(C3_PROTOCOL_VERSION)
        body.add(command)
        body.add((payloadLen and 0xFF).toByte())
        body.add(((payloadLen ushr 8) and 0xFF).toByte())

        if (sessionId != null && requestNr != null) {
            body.add((sessionId and 0xFF).toByte())
            body.add(((sessionId ushr 8) and 0xFF).toByte())
            body.add((requestNr and 0xFF).toByte())
            body.add(((requestNr ushr 8) and 0xFF).toByte())
        }

        if (data != null) {
            for (b in data) {
                body.add(b)
            }
        }

        val checksum = crc16(body.toByteArray())

        val packet = mutableListOf<Byte>()
        packet.add(C3_MESSAGE_START)
        packet.addAll(body)
        packet.add((checksum and 0xFF).toByte())
        packet.add(((checksum ushr 8) and 0xFF).toByte())
        packet.add(C3_MESSAGE_END)
        return packet.toByteArray()
    }

    fun buildConnectPayload(phoneMac: String, installMark: String): ByteArray {
        val str = "$DEFAULT_PULL_PASSWORD\n$phoneMac-$installMark\n"
        return str.toByteArray(Charsets.US_ASCII)
    }

    fun buildUnlockControlPayload(durationSeconds: Int = 5): ByteArray {
        // Output operation: [Operation=1 (Output), Param1=1 (Door 1), Param2=0 (Timed momentary pulse), Param3=durationSeconds, Param4=0]
        return byteArrayOf(
            0x01.toByte(), // Operation 1: Output Control
            0x01.toByte(), // Param 1: Door number 1
            0x00.toByte(), // Param 2: Door output type (0 = timed pulse, auto-relocks after Param3)
            durationSeconds.toByte(), // Param 3: Duration in seconds
            0x00.toByte()  // Param 4: Reserved
        )
    }

    fun buildPassageModePayload(enable: Boolean): ByteArray {
        // Operation 4: Normally Open (Passage Mode)
        // Param 1: Door 1, Param 2: 0, Param 3: 1 (enable) or 0 (disable), Param 4: 0
        return byteArrayOf(
            0x04.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            if (enable) 0x01.toByte() else 0x00.toByte(),
            0x00.toByte()
        )
    }

    fun buildForceLockPayload(): ByteArray {
        // Operation 1: Output Control with duration = 0s (immediately relock / disengage clutch)
        return byteArrayOf(
            0x01.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
    }

    fun buildSyncTimePayload(epochSeconds: Long, tzOffsetHours: Int): ByteArray {
        return "SysTime=$epochSeconds,TimeZone=$tzOffsetHours,TimeZoneMin=0".toByteArray(Charsets.US_ASCII)
    }

    fun buildSetLockOnPayload(seconds: Int): ByteArray {
        val clamped = seconds.coerceIn(1, 10)
        return "LockOn=$clamped".toByteArray(Charsets.US_ASCII)
    }

    fun buildSetDsmPayload(mode: Int): ByteArray {
        return "DSM=$mode".toByteArray(Charsets.US_ASCII)
    }

    fun buildGetDeviceParamPayload(paramNames: String): ByteArray {
        return paramNames.toByteArray(Charsets.US_ASCII)
    }

    data class LockUser(
        val uid: Long,
        val pin: Int,
        val name: String,
        val privilege: Int = 0, // 0 = Normal User, 14 = Admin
        val cardNo: String = "0",
        val password: String = "",
        val mark: String = "",
        val timezoneId: Int = 1
    )

    fun buildSetUserDataPayload(user: LockUser): ByteArray {
        return "user\tUID=${user.uid}\tPin=${user.pin}\tName=${user.name}\tPassword=${user.password}\tCardNo=${user.cardNo}\tPrivilege=${user.privilege}\tMark=${user.mark}\n".toByteArray(Charsets.US_ASCII)
    }

    fun buildDeleteUserPayload(uid: Long): ByteArray {
        return "user\tUID=$uid\n".toByteArray(Charsets.US_ASCII)
    }

    fun buildSetUserAuthorizePayload(pin: Int, timezoneId: Int): ByteArray {
        return "userauthorize\tPin=$pin\tAuthorizeTimezoneId=$timezoneId\n".toByteArray(Charsets.US_ASCII)
    }

    fun parseUsers(rawTableData: String): List<LockUser> {
        val lines = rawTableData.split("\r\n", "\n").map { it.trim() }.filter { it.isNotEmpty() }
        val users = mutableListOf<LockUser>()
        
        for (line in lines) {
            try {
                val parts = line.split("\t")
                var uid = 0L
                var pin = 0
                var name = ""
                var privilege = 0
                var cardNo = "0"
                var password = ""
                var mark = ""
                
                for (part in parts) {
                    val kv = part.split("=")
                    if (kv.size == 2) {
                        val key = kv[0].trim()
                        val value = kv[1].trim()
                        when (key) {
                            "UID" -> uid = value.toLongOrNull() ?: 0L
                            "Pin" -> pin = value.toIntOrNull() ?: 0
                            "Name" -> name = value
                            "Privilege" -> privilege = value.toIntOrNull() ?: 0
                            "CardNo" -> cardNo = value
                            "Password" -> password = value
                            "Mark" -> mark = value
                        }
                    }
                }
                
                if (uid > 0) {
                    users.add(LockUser(uid, pin, name, privilege, cardNo, password, mark))
                }
            } catch (e: Exception) {
                // Ignore parse errors for individual lines
            }
        }
        return users
    }

    data class LockAuditRecord(
        val id: Long,
        val timestampSeconds: Long,
        val pin: String,
        val cardNo: String,
        val verifyType: Int,
        val eventType: Int,
        val formattedTime: String,
        val verifyTypeDescription: String
    )

    fun buildGetDeviceDataPayload(tableName: String = "transaction", fields: String = "*", filter: String = ""): ByteArray {
        return "$tableName\t$fields\t$filter\n".toByteArray(Charsets.US_ASCII)
    }

    fun parseAuditRecords(rawTableData: String): List<LockAuditRecord> {
        val lines = rawTableData.split("\r\n", "\n").map { it.trim() }.filter { it.isNotEmpty() }
        val records = mutableListOf<LockAuditRecord>()
        
        for (line in lines) {
            try {
                val parts = line.split("\t")
                var id = 0L
                var cardNo = ""
                var pin = ""
                var verified = 0
                var eventType = 0
                var timeSecond = 0L
                
                for (part in parts) {
                    val kv = part.split("=")
                    if (kv.size == 2) {
                        val key = kv[0].trim()
                        val value = kv[1].trim()
                        when (key) {
                            "Index" -> id = value.toLongOrNull() ?: 0L
                            "Cardno" -> cardNo = value
                            "Pin" -> pin = value
                            "Verified" -> verified = value.toIntOrNull() ?: 0
                            "EventType" -> eventType = value.toIntOrNull() ?: 0
                            "Time_second" -> timeSecond = value.toLongOrNull() ?: 0L
                        }
                    }
                }
                
                if (timeSecond > 0) {
                    val formattedTime = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timeSecond * 1000))
                    val verifyDesc = when (verified) {
                        1 -> "Fingerprint"
                        3 -> "Password"
                        4 -> "Card"
                        10 -> "Bluetooth Admin"
                        11 -> "Bluetooth App"
                        200 -> "Hardware/Key"
                        else -> "Method $verified"
                    }
                    records.add(LockAuditRecord(id, timeSecond, pin, cardNo, verified, eventType, formattedTime, verifyDesc))
                }
            } catch (e: Exception) {
                // Ignore parse errors for individual lines
            }
        }
        
        // Sort descending by timestamp
        return records.sortedByDescending { it.timestampSeconds }
    }
}
