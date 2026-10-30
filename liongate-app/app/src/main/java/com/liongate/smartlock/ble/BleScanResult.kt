package com.liongate.smartlock.ble

import android.bluetooth.BluetoothDevice

data class BleDiscoveredDevice(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int
)
