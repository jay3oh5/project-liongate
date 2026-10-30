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
    const val CMD_CONNECT_SESSION: Byte = 0x76.toByte()
    const val CMD_CONTROL: Byte = 0x05.toByte()
    const val CMD_DISCONNECT: Byte = 0x02.toByte()

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
}
