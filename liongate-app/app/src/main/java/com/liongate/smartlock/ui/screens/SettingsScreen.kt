package com.liongate.smartlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liongate.smartlock.ble.BioLockBleManager
import com.liongate.smartlock.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun SettingsScreen(
    bleManager: BioLockBleManager,
    savedPin: String = "123456",
    onSavePin: (String) -> Unit = {},
    savedAdminPin: String = "12345678",
    onSaveAdminPin: (String) -> Unit = {}
) {
    val lockOnSeconds by bleManager.lockOnSeconds.collectAsState()
    val doorSensorMode by bleManager.doorSensorMode.collectAsState()
    val lastTimeSync by bleManager.lastTimeSync.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var pinText by remember(savedPin) { mutableStateOf(savedPin) }
    var showPin by remember { mutableStateOf(false) }
    var adminPinText by remember(savedAdminPin) { mutableStateOf(savedAdminPin) }
    var showAdminPin by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Hardware Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Credentials Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AUTHENTICATION CREDENTIALS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ElectricCyan)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("UNLOCK PASSKEY (BLUETOOTH PAIR PIN)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Slate400)
                    Spacer(modifier = Modifier.height(6.dp))
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
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = SurfaceTier3,
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
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Apply", color = CanvasVoid, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (pinText.length != 6) {
                        Text("Passkey must be exactly 6 digits", color = CyberCrimson, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("ADMINISTRATOR PASSWORD (SUPERPWD)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Slate400)
                    Spacer(modifier = Modifier.height(6.dp))
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
                                        contentDescription = "Toggle Admin PIN",
                                        tint = Slate400
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecurityAmber,
                                unfocusedBorderColor = SurfaceTier3,
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
                            colors = ButtonDefaults.buttonColors(containerColor = SecurityAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Apply", color = CanvasVoid, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (adminPinText.length != 8) {
                        Text("Admin Password must be exactly 8 digits", color = CyberCrimson, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("HARDWARE PROFILE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }
                        Text("UP TO DATE", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = SecurityEmerald, modifier = Modifier.background(SecurityEmerald.copy(alpha = 0.15f), RoundedCornerShape(2.dp)).border(1.dp, SecurityEmerald.copy(alpha = 0.3f), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DEVICE", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Slate400)
                            Text("ZKBioLock_PL10B", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = ElectricCyan)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("FIRMWARE", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Slate400)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SecurityEmerald, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("1.4.2-PRO", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = ElectricCyan)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-Relock Hold Duration (LockOn)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto-Relock Delay (LockOn)", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Text(
                        "Hold-open duration before clutch automatically relocks",
                        fontSize = 12.sp,
                        color = Slate400,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
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
                            color = ElectricCyan
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (lockOnSeconds > 1) coroutineScope.launch { bleManager.setLockOnDuration(lockOnSeconds - 1) }
                                },
                                enabled = lockOnSeconds > 1,
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceTier2),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                            }
                            Button(
                                onClick = {
                                    if (lockOnSeconds < 10) coroutineScope.launch { bleManager.setLockOnDuration(lockOnSeconds + 1) }
                                },
                                enabled = lockOnSeconds < 10,
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceTier2),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Door Sensor Mode (DSM)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Door Sensor Mode (DSM)", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Text(
                        "Defines magnetic sensor trigger reaction",
                        fontSize = 12.sp,
                        color = Slate400,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
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
                                        if (isSelected) ElectricCyan else SurfaceTier3,
                                        RoundedCornerShape(8.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ElectricCyan.copy(alpha = 0.15f) else SurfaceTier2
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
                                        color = if (isSelected) ElectricCyan else Slate400,
                                        maxLines = 2,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lock RTC Clock Synchronization
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Lock Clock (RTC)", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Button(
                            onClick = { coroutineScope.launch { bleManager.syncTime() } },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Sync Now", fontSize = 12.sp, color = CanvasVoid, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val formattedTime = lastTimeSync?.let {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it * 1000L))
                    } ?: "Not synchronized"
                    Text(
                        "Last Sync: $formattedTime",
                        fontSize = 12.sp,
                        color = Slate400,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Bypass Protocols
            Box {
                // Background glow
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(100.dp)
                        .blur(30.dp)
                        .background(CyberCrimson.copy(alpha = 0.15f), CircleShape)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCrimson.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "EMERGENCY BYPASS PROTOCOLS",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CyberCrimson,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Divider(color = CyberCrimson.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // 9V Battery Terminal Guide
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CanvasVoid),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier.background(SurfaceTier1, RoundedCornerShape(4.dp)).border(1.dp, SurfaceTier3, RoundedCornerShape(4.dp)).padding(8.dp)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("External 9V Battery Power Port", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        Text("PORT 9V DC", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = ElectricCyan, modifier = Modifier.border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "If the internal 4× AA cells are fully depleted: Touch standard 9V alkaline battery terminals firmly against the dual contact nubs beneath the front escutcheon handle. Maintain contact while scanning RFID or entering emergency key code.",
                                        fontSize = 11.sp,
                                        color = Slate400,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecurityEmerald, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("NO RESIDUAL DATA LOSS ON POWER DRAIN", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Slate400)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mechanical Key Override
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CanvasVoid),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier.background(SurfaceTier1, RoundedCornerShape(4.dp)).border(1.dp, SurfaceTier3, RoundedCornerShape(4.dp)).padding(8.dp)
                                ) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFFFB2B7), modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Mechanical Key Override", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        Text("C-CLASS CYLINDER", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFFFB2B7), modifier = Modifier.border(1.dp, Color(0xFFFFB2B7).copy(alpha = 0.3f), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Use the physical emergency key to mechanically bypass the clutch. Insert key into hidden cylinder under bottom cover and turn 90° clockwise. Do not force.",
                                        fontSize = 11.sp,
                                        color = Slate400,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Query Lock Parameters
            Button(
                onClick = { coroutineScope.launch { bleManager.queryLockParams() } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceTier2),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh Lock Parameters", fontSize = 13.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
