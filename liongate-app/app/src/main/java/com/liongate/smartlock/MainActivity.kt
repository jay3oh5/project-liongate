package com.liongate.smartlock

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.liongate.smartlock.ble.BioLockBleManager
import com.liongate.smartlock.ui.LockScreen
import com.liongate.smartlock.ui.theme.LionGateTheme

class MainActivity : ComponentActivity() {
    private lateinit var bleManager: BioLockBleManager
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "liongate_prefs"
        private const val KEY_PIN = "key_pin"
        private const val KEY_DEVICE_MAC = "key_device_mac"
        private const val KEY_DEVICE_NAME = "key_device_name"
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            bleManager.startScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bleManager = BioLockBleManager(applicationContext)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val unlockReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: android.content.Intent?) {
                val pin = intent?.getStringExtra("pin") ?: prefs.getString(KEY_PIN, BuildConfig.DEFAULT_PIN) ?: BuildConfig.DEFAULT_PIN
                val mac = intent?.getStringExtra("mac") ?: prefs.getString(KEY_DEVICE_MAC, BuildConfig.DEFAULT_MAC) ?: BuildConfig.DEFAULT_MAC
                android.util.Log.d("BioLockBleManager", "ADB UNLOCK TRIGGERED: mac=$mac pin=$pin")
                bleManager.scanAndUnlock(mac, pin)
            }
        }
        val filter = android.content.IntentFilter("com.liongate.smartlock.UNLOCK")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(unlockReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(unlockReceiver, filter)
        }

        checkAndRequestPermissions()

        setContent {
            var currentPin by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(prefs.getString(KEY_PIN, "123456") ?: "123456")
            }
            var currentMac by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(prefs.getString(KEY_DEVICE_MAC, "") ?: "")
            }
            var currentName by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(prefs.getString(KEY_DEVICE_NAME, "") ?: "")
            }

            LionGateTheme {
                LockScreen(
                    bleManager = bleManager,
                    savedPin = currentPin,
                    onSavePin = { pin ->
                        currentPin = pin
                        prefs.edit().putString(KEY_PIN, pin).apply()
                    },
                    savedDeviceMac = currentMac,
                    savedDeviceName = currentName,
                    onSaveDevice = { mac, name ->
                        currentMac = mac
                        currentName = name
                        prefs.edit()
                            .putString(KEY_DEVICE_MAC, mac)
                            .putString(KEY_DEVICE_NAME, name)
                            .apply()
                    }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect()
    }
}
