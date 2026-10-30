package com.liongate.smartlock.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liongate.smartlock.ble.BioLockBleManager
import com.liongate.smartlock.ble.BleDiscoveredDevice
import com.liongate.smartlock.ble.LockConnectionState
import com.liongate.smartlock.ui.theme.Amber500
import com.liongate.smartlock.ui.theme.Cyan500
import com.liongate.smartlock.ui.theme.Emerald500
import com.liongate.smartlock.ui.theme.Rose500
import com.liongate.smartlock.ui.theme.Slate400
import com.liongate.smartlock.ui.theme.Slate700
import com.liongate.smartlock.ui.theme.Slate800
import com.liongate.smartlock.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    bleManager: BioLockBleManager,
    savedPin: String,
    onSavePin: (String) -> Unit,
    savedDeviceMac: String,
    savedDeviceName: String,
    onSaveDevice: (String, String) -> Unit
) {
    val context = LocalContext.current
    val connectionState by bleManager.connectionState.collectAsState()
    val discoveredDevices by bleManager.discoveredDevices.collectAsState()
    val logMessage by bleManager.logMessage.collectAsState()

    var pinText by remember(savedPin) { mutableStateOf(savedPin) }
    var showPin by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }

    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

    // Haptic feedback on unlock
    LaunchedEffect(connectionState) {
        if (connectionState is LockConnectionState.Unlocked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 150), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "LionGate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ZKBioBT",
                            fontSize = 12.sp,
                            color = Cyan500,
                            modifier = Modifier
                                .background(Cyan500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeviceSheet = true; bleManager.startScan() }) {
                        Icon(
                            Icons.Default.BluetoothSearching,
                            contentDescription = "Scan Devices",
                            tint = Cyan500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Selected Device Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeviceSheet = true; bleManager.startScan() },
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Cyan500.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Cyan500)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (savedDeviceName.isNotEmpty()) savedDeviceName else "No Lock Paired",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                if (savedDeviceMac.isNotEmpty()) savedDeviceMac else "Tap to scan & connect",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }
                    }
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Slate400)
                }
            }

            // Big Unlock Action Button
            val isUnlocking = connectionState is LockConnectionState.Unlocking ||
                    connectionState is LockConnectionState.Connecting

            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isUnlocking) 1.08f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            val buttonColor by animateColorAsState(
                targetValue = when (connectionState) {
                    is LockConnectionState.Unlocked -> Emerald500
                    is LockConnectionState.Unlocking -> Amber500
                    is LockConnectionState.Error -> Rose500
                    else -> Cyan500
                },
                label = "color"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(buttonColor.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        .border(3.dp, buttonColor, CircleShape)
                        .clickable(enabled = !isUnlocking && savedDeviceMac.isNotEmpty()) {
                            bleManager.connectAndUnlock(savedDeviceMac, pinText)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isUnlocking) {
                            CircularProgressIndicator(
                                color = buttonColor,
                                modifier = Modifier.size(54.dp),
                                strokeWidth = 4.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (connectionState is LockConnectionState.Unlocked)
                                    Icons.Default.LockOpen
                                else
                                    Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = buttonColor,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            when (connectionState) {
                                is LockConnectionState.Connecting -> "CONNECTING"
                                is LockConnectionState.Unlocking -> "UNLOCKING..."
                                is LockConnectionState.Unlocked -> "OPENED"
                                is LockConnectionState.Error -> "FAILED"
                                else -> "TAP TO UNLOCK"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = buttonColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status message
                Text(
                    when (val state = connectionState) {
                        is LockConnectionState.Connecting -> "Connecting to ${state.deviceName}..."
                        is LockConnectionState.Connected -> "Connected! Handshaking..."
                        is LockConnectionState.Unlocking -> "Sending unlock pulse to lock..."
                        is LockConnectionState.Unlocked -> state.message
                        is LockConnectionState.Error -> state.message
                        is LockConnectionState.Scanning -> "Searching for your lock..."
                        else -> if (savedDeviceMac.isEmpty()) "Scan and select your lock to begin" else "Ready"
                    },
                    fontSize = 14.sp,
                    color = if (connectionState is LockConnectionState.Error) Rose500 else Slate400,
                    fontWeight = FontWeight.Medium
                )
            }

            // PIN Code & Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Cyan500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Passkey / PIN", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = {
                            pinText = it
                            onSavePin(it)
                        },
                        label = { Text("Enter 4 or 6-digit PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    imageVector = if (showPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle PIN",
                                    tint = Slate400
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan500,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Diagnostic Log Viewer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (logMessage.isNotEmpty()) "> $logMessage" else "> Idle",
                    color = Emerald500,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp),
                    maxLines = 2
                )
            }
        }
    }

    // Bluetooth Devices Bottom Sheet
    if (showDeviceSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                showDeviceSheet = false
                bleManager.stopScan()
            },
            sheetState = sheetState,
            containerColor = Slate800
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select ZKTeco Lock", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Button(
                        onClick = { bleManager.startScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                    ) {
                        Text("Rescan")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (discoveredDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Cyan500, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Scanning for locks nearby...", color = Slate400, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(260.dp)) {
                        items(discoveredDevices) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSaveDevice(item.address, item.name)
                                        showDeviceSheet = false
                                        bleManager.stopScan()
                                        bleManager.connectAndUnlock(item.device, pinText)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (item.address == savedDeviceMac)
                                        Cyan500.copy(alpha = 0.2f)
                                    else
                                        Slate700
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(item.name, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        Text("${item.address}  •  ${item.rssi} dBm", fontSize = 12.sp, color = Slate400)
                                    }
                                    if (item.address == savedDeviceMac) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Cyan500)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
