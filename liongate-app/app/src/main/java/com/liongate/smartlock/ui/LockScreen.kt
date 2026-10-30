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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val isPassageMode by bleManager.isPassageMode.collectAsState()
    val lockOnSeconds by bleManager.lockOnSeconds.collectAsState()
    val doorSensorMode by bleManager.doorSensorMode.collectAsState()
    val lastTimeSync by bleManager.lastTimeSync.collectAsState()

    var pinText by remember(savedPin) { mutableStateOf(savedPin) }
    var showPin by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Lock Settings",
                            tint = Slate400
                        )
                    }
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

            // Quick Controls: Passage Mode & Force Lock
            val isConnected = connectionState is LockConnectionState.Connected ||
                    connectionState is LockConnectionState.Unlocked ||
                    connectionState is LockConnectionState.Unlocking

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Passage Mode Toggle
                Button(
                    onClick = { bleManager.setPassageMode(!isPassageMode) },
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPassageMode) Amber500 else Slate800,
                        contentColor = if (isPassageMode) Slate900 else Color.White,
                        disabledContainerColor = Slate800.copy(alpha = 0.5f),
                        disabledContentColor = Slate400.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            1.dp,
                            if (isPassageMode) Amber500 else Slate700,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isPassageMode) Icons.Default.LockOpen else Icons.Default.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isPassageMode) "Passage: ON" else "Passage Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Force Lock / Relock Now Button
                Button(
                    onClick = { bleManager.lockNow() },
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate800,
                        contentColor = Rose500,
                        disabledContainerColor = Slate800.copy(alpha = 0.5f),
                        disabledContentColor = Slate400.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, Rose500.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Lock Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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

    // Lock Hardware Settings Bottom Sheet
    if (showSettingsSheet) {
        val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = settingsSheetState,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Cyan500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Lock Hardware Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Auto-Relock Hold Duration (LockOn)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate700.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Cyan500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-Relock Delay (LockOn)", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Text(
                            "Hold-open duration before clutch automatically relocks",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$lockOnSeconds seconds",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cyan500
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (lockOnSeconds > 1) bleManager.setLockOnDuration(lockOnSeconds - 1)
                                    },
                                    enabled = lockOnSeconds > 1,
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                                }
                                Button(
                                    onClick = {
                                        if (lockOnSeconds < 10) bleManager.setLockOnDuration(lockOnSeconds + 1)
                                    },
                                    enabled = lockOnSeconds < 10,
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Door Sensor Mode (DSM)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate700.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = Cyan500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Door Sensor Mode (DSM)", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Text(
                            "Defines magnetic sensor trigger reaction",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                1 to "Auto-Relock (NC)",
                                0 to "Normal Open (NO)",
                                2 to "Disabled (None)"
                            )
                            modes.forEach { (modeValue, modeLabel) ->
                                val isSelected = doorSensorMode == modeValue
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { bleManager.setDoorSensorMode(modeValue) }
                                        .border(
                                            1.dp,
                                            if (isSelected) Cyan500 else Slate700,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Cyan500.copy(alpha = 0.2f) else Slate800
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            modeLabel,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Cyan500 else Slate400,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Lock RTC Clock Synchronization
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate700.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Cyan500, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sync Lock Clock (RTC)", fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Button(
                                onClick = { bleManager.syncTime() },
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sync Now", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedTime = lastTimeSync?.let {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it * 1000L))
                        } ?: "Not synchronized"
                        Text(
                            "Last Sync: $formattedTime",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: Query Lock Parameters
                Button(
                    onClick = { bleManager.queryLockParams() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Lock Parameters", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
