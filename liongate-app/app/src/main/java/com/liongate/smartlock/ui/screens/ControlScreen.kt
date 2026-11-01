package com.liongate.smartlock.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liongate.smartlock.ble.BioLockBleManager
import com.liongate.smartlock.ble.LockConnectionState
import com.liongate.smartlock.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ControlScreen(
    bleManager: BioLockBleManager,
    savedPin: String,
    onSavePin: (String) -> Unit,
    savedDeviceMac: String,
    savedDeviceName: String,
    onSaveDevice: (String, String) -> Unit,
    onShowDeviceSheet: () -> Unit
) {
    val connectionState by bleManager.connectionState.collectAsState()
    val isPassageMode by bleManager.isPassageMode.collectAsState()
    val logMessage by bleManager.logMessage.collectAsState()
    var pinText by remember(savedPin) { mutableStateOf(savedPin) }
    var showPin by remember { mutableStateOf(false) }
    val coroutineScope = remember { CoroutineScope(Dispatchers.IO) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Selected Device Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowDeviceSheet() },
            colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
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
                            .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = ElectricCyan)
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
        val isUnlocked = connectionState is LockConnectionState.Unlocked

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
                is LockConnectionState.Unlocked -> SecurityEmerald
                is LockConnectionState.Unlocking -> TelemetryAmber
                is LockConnectionState.Error -> CyberCrimson
                else -> ElectricCyan
            },
            label = "color"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Massive Central Biometric Unlock Actuator
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(SurfaceTier2)
                    .border(2.dp, if (isUnlocked) SecurityEmerald else CyberCrimson, CircleShape)
                    .clickable(enabled = !isUnlocking && savedDeviceMac.isNotEmpty()) {
                        bleManager.connectAndUnlock(savedDeviceMac, pinText)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Inner concentric elements
                Box(modifier = Modifier.fillMaxSize().padding(16.dp).border(1.dp, SurfaceTier3, CircleShape))
                
                if (isUnlocked) {
                    // SVG Circular Countdown Progress Simulation
                    CircularProgressIndicator(
                        progress = { 1f },
                        color = SecurityEmerald,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        strokeWidth = 3.dp,
                        trackColor = SurfaceTier3,
                    )
                }

                // Inner Glow Diffusion
                Box(modifier = Modifier.fillMaxSize().background(buttonColor.copy(alpha = 0.15f), CircleShape))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isUnlocking) {
                        CircularProgressIndicator(
                            color = buttonColor,
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = buttonColor,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        when (connectionState) {
                            is LockConnectionState.Connecting -> "CONNECTING"
                            is LockConnectionState.Unlocking -> "UNLOCKING..."
                            is LockConnectionState.Unlocked -> "ACCESS GRANTED // OPEN"
                            is LockConnectionState.Error -> "SECURED // LOCKED"
                            else -> "SECURED // LOCKED"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = buttonColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "ID: 0x8F • CRYPT_AES",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Actuator Guidance
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(if (isUnlocked) SecurityEmerald else CyberCrimson, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (isUnlocked) "LOCK IS DISENGAGED" else "TAP OR HOLD TO UNLOCK",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isUnlocked) SecurityEmerald else ElectricCyan
                )
            }

            // Status message / Timer
            Text(
                when (val state = connectionState) {
                    is LockConnectionState.Connecting -> "Connecting to ${state.deviceName}..."
                    is LockConnectionState.Connected -> "Connected! Handshaking..."
                    is LockConnectionState.Unlocking -> "Sending unlock pulse to lock..."
                    is LockConnectionState.Unlocked -> "Auto-Relock Delay: active"
                    is LockConnectionState.Error -> state.message
                    is LockConnectionState.Scanning -> "Searching for your lock..."
                    else -> if (savedDeviceMac.isEmpty()) "Scan and select your lock to begin" else "Ready"
                },
                fontSize = 14.sp,
                color = if (connectionState is LockConnectionState.Error) CyberCrimson else Slate400,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
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
                onClick = { coroutineScope.launch { bleManager.setPassageMode(!isPassageMode) } },
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPassageMode) TelemetryAmber else SurfaceTier1,
                    contentColor = if (isPassageMode) CanvasVoid else Color.White,
                    disabledContainerColor = SurfaceTier1.copy(alpha = 0.5f),
                    disabledContentColor = Slate400.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(
                        1.dp,
                        if (isPassageMode) TelemetryAmber else SurfaceTier3,
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
                onClick = { coroutineScope.launch { bleManager.lockNow() } },
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceTier1,
                    contentColor = CyberCrimson,
                    disabledContainerColor = SurfaceTier1.copy(alpha = 0.5f),
                    disabledContentColor = Slate400.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, CyberCrimson.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
            colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = ElectricCyan)
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
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceTier3,
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
                color = SecurityEmerald,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(12.dp),
                maxLines = 2
            )
        }
    }
}
