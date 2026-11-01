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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.liongate.smartlock.ui.screens.*
import com.liongate.smartlock.ui.theme.ElectricCyan
import com.liongate.smartlock.ui.theme.SecurityEmerald
import com.liongate.smartlock.ui.theme.TelemetryAmber
import com.liongate.smartlock.ui.theme.CanvasVoid
import com.liongate.smartlock.ui.theme.SurfaceTier1
import com.liongate.smartlock.ui.theme.SurfaceTier2
import com.liongate.smartlock.ui.theme.CyberCrimson
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    bleManager: BioLockBleManager,
    savedPin: String,
    onSavePin: (String) -> Unit,
    savedAdminPin: String,
    onSaveAdminPin: (String) -> Unit,
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
    var adminPinText by remember(savedAdminPin) { mutableStateOf(savedAdminPin) }
    var showAdminPin by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showUserSheet by remember { mutableStateOf(false) }
    var showSchedulesSheet by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) }

    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    val coroutineScope = remember { CoroutineScope(Dispatchers.IO) }

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
                    if (connectionState !is LockConnectionState.Connected && connectionState !is LockConnectionState.Unlocked) {
                        Button(
                            onClick = { bleManager.connect(savedDeviceMac, pinText, adminPinText) },
                            enabled = savedDeviceMac.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                        ) {
                            Text("Connect", fontSize = 12.sp, color = Color.Black)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Connected (Admin)", color = Emerald500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { bleManager.disconnect() },
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                            ) {
                                Text("Disconnect", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                    IconButton(onClick = { showHistorySheet = true; coroutineScope.launch { bleManager.fetchAuditLogs() } }) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Audit Logs",
                            tint = Emerald500
                        )
                    }
                    IconButton(onClick = { showSchedulesSheet = true; coroutineScope.launch { bleManager.fetchTimeZones() } }) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Access Schedules",
                            tint = Cyan500
                        )
                    }
                    IconButton(onClick = { showUserSheet = true; coroutineScope.launch { bleManager.fetchUsers() } }) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "User Management",
                            tint = Amber500
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CanvasVoid)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceTier1,
                contentColor = Slate400
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.LockOpen, contentDescription = "Control") },
                    label = { Text("Control") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = SurfaceTier2
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1; coroutineScope.launch { bleManager.fetchUsers() } },
                    icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                    label = { Text("Users") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = SurfaceTier2
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2; coroutineScope.launch { bleManager.fetchTimeZones() } },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedules") },
                    label = { Text("Schedules") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = SurfaceTier2
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3; coroutineScope.launch { bleManager.fetchAuditLogs() } },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = SurfaceTier2
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = SurfaceTier2
                    )
                )
            }
        },
        containerColor = CanvasVoid
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                0 -> ControlScreen(bleManager, savedPin, onSavePin, savedAdminPin, onSaveAdminPin, savedDeviceMac, savedDeviceName, onSaveDevice, onShowDeviceSheet = { showDeviceSheet = true })
                1 -> UserScreen(bleManager)
                2 -> ScheduleScreen(bleManager)
                3 -> HistoryScreen(bleManager)
                4 -> SettingsScreen(bleManager, savedPin, onSavePin, savedAdminPin, onSaveAdminPin)
            }
        }
    }

    // Bluetooth Devices Bottom Sheet
    if (showDeviceSheet) {
        val sheetState = rememberModalBottomSheetState()

        var userToDelete by remember { mutableStateOf<com.liongate.smartlock.protocol.ZkProtocol.LockUser?>(null) }
        
        if (userToDelete != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete User", color = Color.White) },
                text = { Text("Are you sure you want to delete ${userToDelete?.name}?", color = Slate400) },
                confirmButton = {
                    Button(
                        onClick = {
                            val uid = userToDelete?.uid
                            userToDelete = null
                            if (uid != null) {
                                coroutineScope.launch {
                                    if (bleManager.deleteUser(uid).isSuccess) {
                                        bleManager.fetchUsers()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { userToDelete = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Slate800
            )
        }

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

        var userToDelete by remember { mutableStateOf<com.liongate.smartlock.protocol.ZkProtocol.LockUser?>(null) }
        
        if (userToDelete != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete User", color = Color.White) },
                text = { Text("Are you sure you want to delete ${userToDelete?.name}?", color = Slate400) },
                confirmButton = {
                    Button(
                        onClick = {
                            val uid = userToDelete?.uid
                            userToDelete = null
                            if (uid != null) {
                                coroutineScope.launch {
                                    if (bleManager.deleteUser(uid).isSuccess) {
                                        bleManager.fetchUsers()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { userToDelete = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Slate800
            )
        }

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
                                        if (lockOnSeconds > 1) coroutineScope.launch { bleManager.setLockOnDuration(lockOnSeconds - 1) }
                                    },
                                    enabled = lockOnSeconds > 1,
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                                }
                                Button(
                                    onClick = {
                                        if (lockOnSeconds < 10) coroutineScope.launch { bleManager.setLockOnDuration(lockOnSeconds + 1) }
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
                                            .clickable { coroutineScope.launch { bleManager.setDoorSensorMode(modeValue) } }
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
                                onClick = { coroutineScope.launch { bleManager.syncTime() } },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: Credentials
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate700.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Cyan500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Passkey (Bluetooth Pair PIN)", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = pinText,
                                onValueChange = { pinText = it },
                                modifier = Modifier.weight(1f),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { 
                                    if (pinText.length == 6) {
                                        onSavePin(pinText)
                                        coroutineScope.launch { bleManager.changePairPin(pinText) }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                        if (pinText.length != 6) {
                            Text("Passkey must be exactly 6 digits", color = Rose500, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Administrator Password (SuperPwd)", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = adminPinText,
                                onValueChange = { adminPinText = it },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = if (showAdminPin) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showAdminPin = !showAdminPin }) {
                                        Icon(
                                            imageVector = if (showAdminPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle PIN",
                                            tint = Slate400
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Amber500,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { 
                                    if (adminPinText.length == 8) {
                                        onSaveAdminPin(adminPinText)
                                        coroutineScope.launch { bleManager.changeAdminPassword(adminPinText) }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                        if (adminPinText.length != 8) {
                            Text("Admin Password must be exactly 8 digits", color = Rose500, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 5: Query Lock Parameters
                Button(
                    onClick = { coroutineScope.launch { bleManager.queryLockParams() } },
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


    if (showUserSheet) {
        val userSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val users by bleManager.users.collectAsState()
        val isLoadingUsers by bleManager.isLoadingUsers.collectAsState()
        var showAddUserForm by remember { mutableStateOf(false) }

        var newUserName by remember { mutableStateOf("") }
        var newUserPin by remember { mutableStateOf("") }
        var newUserRole by remember { mutableStateOf(0) } // 0=Normal, 14=Admin


        var userToDelete by remember { mutableStateOf<com.liongate.smartlock.protocol.ZkProtocol.LockUser?>(null) }
        
        if (userToDelete != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete User", color = Color.White) },
                text = { Text("Are you sure you want to delete ${userToDelete?.name}?", color = Slate400) },
                confirmButton = {
                    Button(
                        onClick = {
                            val uid = userToDelete?.uid
                            userToDelete = null
                            if (uid != null) {
                                coroutineScope.launch {
                                    if (bleManager.deleteUser(uid).isSuccess) {
                                        bleManager.fetchUsers()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { userToDelete = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Slate800
            )
        }

        ModalBottomSheet(

            onDismissRequest = { showUserSheet = false },
            sheetState = userSheetState,
            containerColor = Slate800
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (showAddUserForm) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showAddUserForm = false }) {
                            Icon(Icons.Default.Remove, contentDescription = "Back", tint = Slate400)
                        }
                        Text("Add New User", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        label = { Text("Name", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = Slate700
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newUserPin,
                        onValueChange = { newUserPin = it },
                        label = { Text("PIN (4-6 digits)", color = Slate400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = Slate700
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Role", color = Color.White, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { newUserRole = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = if (newUserRole == 0) Amber500 else Slate700),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Normal")
                        }
                        Button(
                            onClick = { newUserRole = 14 },
                            colors = ButtonDefaults.buttonColors(containerColor = if (newUserRole == 14) Amber500 else Slate700),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Admin")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (newUserName.isNotBlank() && newUserPin.length in 4..6) {
                                val user = com.liongate.smartlock.protocol.ZkProtocol.LockUser(
                                    uid = System.currentTimeMillis() / 1000 % 100000,
                                    pin = newUserPin.toIntOrNull() ?: 1234,
                                    name = newUserName,
                                    privilege = newUserRole
                                )
                                coroutineScope.launch {
                                    val result = bleManager.saveUser(user)
                                    if (result.isSuccess) {
                                        showAddUserForm = false
                                        newUserName = ""
                                        newUserPin = ""
                                        bleManager.fetchUsers()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                    ) {
                        Text("Save User")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Amber500)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "User Management",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                        Row {
                            IconButton(onClick = { showAddUserForm = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add User", tint = Amber500)
                            }
                            IconButton(onClick = { coroutineScope.launch { bleManager.fetchUsers() } }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = if (isLoadingUsers) Slate400 else Amber500)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoadingUsers && users.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Amber500)
                        }
                    } else if (users.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No users found", color = Slate400)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(users) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate700)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(40.dp).background(Amber500.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(user.name.ifEmpty { "User ${user.uid}" }, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(if (user.privilege == 14) "Admin" else "Normal", fontSize = 12.sp, color = Amber500)
                                                    Text(" • PIN: ${user.pin}", fontSize = 12.sp, color = Slate400)
                                                    if (user.cardNo.isNotEmpty() && user.cardNo != "0") {
                                                        Text(" • Card: ${user.cardNo}", fontSize = 12.sp, color = Slate400)
                                                    }
                                                }
                                            }
                                        }
                                        IconButton(onClick = { userToDelete = user }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Rose500)
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

    if (showSchedulesSheet) {
        val schedulesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val schedules by bleManager.schedules.collectAsState()
        val isLoadingSchedules by bleManager.isLoadingSchedules.collectAsState()
        var editSchedule by remember { mutableStateOf<com.liongate.smartlock.protocol.ZkProtocol.LockSchedule?>(null) }
        
        if (editSchedule != null) {
            val schedule = editSchedule!!
            var startHour by remember { mutableStateOf(schedule.startHour) }
            var startMin by remember { mutableStateOf(schedule.startMinute) }
            var endHour by remember { mutableStateOf(schedule.endHour) }
            var endMin by remember { mutableStateOf(schedule.endMinute) }
            var days by remember { mutableStateOf(schedule.activeDays.toMutableList()) }
            
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { editSchedule = null },
                title = { Text("Edit Schedule ${schedule.timezoneId}", color = Color.White) },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Start Time:", color = Slate400)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { startHour = (startHour - 1).mod(24) }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Cyan500) }
                                Text("%02d".format(startHour), color = Color.White)
                                IconButton(onClick = { startHour = (startHour + 1).mod(24) }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Cyan500) }
                                Text(":", color = Color.White)
                                IconButton(onClick = { startMin = (startMin - 1).mod(60) }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Cyan500) }
                                Text("%02d".format(startMin), color = Color.White)
                                IconButton(onClick = { startMin = (startMin + 1).mod(60) }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Cyan500) }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("End Time:", color = Slate400)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { endHour = (endHour - 1).mod(24) }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Cyan500) }
                                Text("%02d".format(endHour), color = Color.White)
                                IconButton(onClick = { endHour = (endHour + 1).mod(24) }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Cyan500) }
                                Text(":", color = Color.White)
                                IconButton(onClick = { endMin = (endMin - 1).mod(60) }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Cyan500) }
                                Text("%02d".format(endMin), color = Color.White)
                                IconButton(onClick = { endMin = (endMin + 1).mod(60) }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Cyan500) }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Active Days:", color = Slate400)
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { i, d ->
                                val active = days[i]
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (active) Cyan500 else Slate700, CircleShape)
                                        .clickable { days = days.toMutableList().apply { set(i, !active) } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(d, color = if (active) Color.Black else Slate400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newSchedule = schedule.copy(
                                startHour = startHour, startMinute = startMin,
                                endHour = endHour, endMinute = endMin,
                                activeDays = days
                            )
                            editSchedule = null
                            coroutineScope.launch {
                                if (bleManager.saveTimeZone(newSchedule).isSuccess) {
                                    bleManager.fetchTimeZones()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                    ) {
                        Text("Save", color = Color.Black)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { editSchedule = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Slate800
            )
        }

        ModalBottomSheet(
            onDismissRequest = { showSchedulesSheet = false },
            sheetState = schedulesSheetState,
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
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Cyan500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Access Schedules", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    }
                    IconButton(onClick = { coroutineScope.launch { bleManager.fetchTimeZones() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = if (isLoadingSchedules) Slate400 else Cyan500)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingSchedules && schedules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Cyan500)
                    }
                } else if (schedules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No schedules found", color = Slate400)
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(schedules) { schedule ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { editSchedule = schedule },
                                colors = CardDefaults.cardColors(containerColor = Slate700)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Schedule ID ${schedule.timezoneId}", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                                        Text("${schedule.formattedStartTime} - ${schedule.formattedEndTime}", fontSize = 12.sp, color = Cyan500)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEachIndexed { i, d ->
                                                if (schedule.activeDays.getOrNull(i) == true) {
                                                    Text(d, fontSize = 10.sp, color = Slate900, modifier = Modifier.background(Cyan500, CircleShape).padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                    }
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate400)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val auditLogs by bleManager.auditLogs.collectAsState()
        val isLoadingLogs by bleManager.isLoadingLogs.collectAsState()


        var userToDelete by remember { mutableStateOf<com.liongate.smartlock.protocol.ZkProtocol.LockUser?>(null) }
        
        if (userToDelete != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete User", color = Color.White) },
                text = { Text("Are you sure you want to delete ${userToDelete?.name}?", color = Slate400) },
                confirmButton = {
                    Button(
                        onClick = {
                            val uid = userToDelete?.uid
                            userToDelete = null
                            if (uid != null) {
                                coroutineScope.launch {
                                    if (bleManager.deleteUser(uid).isSuccess) {
                                        bleManager.fetchUsers()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { userToDelete = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Slate800
            )
        }

        ModalBottomSheet(

            onDismissRequest = { showHistorySheet = false },
            sheetState = historySheetState,
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
                        Icon(Icons.Default.History, contentDescription = null, tint = Emerald500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Event History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { coroutineScope.launch { bleManager.fetchAuditLogs() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = if (isLoadingLogs) Slate400 else Emerald500)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingLogs && auditLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Emerald500)
                    }
                } else if (auditLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No records found", color = Slate400)
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(auditLogs) { log ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate700)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Emerald500.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            log.formattedTime,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                log.verifyTypeDescription,
                                                fontSize = 12.sp,
                                                color = Emerald500
                                            )
                                            Text(
                                                " • User/Card: ${if (log.cardNo.isNotEmpty() && log.cardNo != "0") log.cardNo else log.pin}",
                                                fontSize = 12.sp,
                                                color = Slate400
                                            )
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
}
