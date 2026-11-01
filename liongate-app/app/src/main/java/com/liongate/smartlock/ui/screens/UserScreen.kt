package com.liongate.smartlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liongate.smartlock.ble.BioLockBleManager
import com.liongate.smartlock.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserScreen(bleManager: BioLockBleManager) {
    val users by bleManager.users.collectAsState()
    val isLoadingUsers by bleManager.isLoadingUsers.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val maxUsers = 90
    val adminCount = users.count { it.privilege == 14 }
    val normalCount = users.count { it.privilege != 14 }
    val tempCount = 0 // Not fully supported by protocol in this view, keeping 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Capacity Overview Header Card
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
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "BIOMETRIC SLOTS CAPACITY",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Slate400,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${users.size}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Text(
                                " / $maxUsers",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${(users.size.toFloat() / maxUsers * 100).toInt()}% ALLOCATED",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }
                    IconButton(
                        onClick = { coroutineScope.launch { bleManager.fetchUsers() } },
                        modifier = Modifier.size(32.dp).background(SurfaceTier2, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Slate400, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Multi-Color Capacity Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CanvasVoid)
                        .border(1.dp, SurfaceTier3, RoundedCornerShape(4.dp))
                ) {
                    val adminWeight = if (users.isNotEmpty()) adminCount.toFloat() / maxUsers else 0f
                    val normalWeight = if (users.isNotEmpty()) normalCount.toFloat() / maxUsers else 0f
                    val tempWeight = if (users.isNotEmpty()) tempCount.toFloat() / maxUsers else 0f
                    
                    if (adminWeight > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(adminWeight).background(ElectricCyan))
                        Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(CanvasVoid))
                    }
                    if (normalWeight > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(normalWeight).background(SecurityEmerald))
                        Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(CanvasVoid))
                    }
                    if (tempWeight > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(tempWeight).background(TelemetryAmber))
                    }
                    // Vacant space
                    val vacantWeight = 1f - adminWeight - normalWeight - tempWeight
                    if (vacantWeight > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(vacantWeight).background(SurfaceTier2))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingUsers && users.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        } else if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No users found", color = Slate400)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        user.name.ifEmpty { "User ${user.uid}" }, 
                                        fontWeight = FontWeight.SemiBold, 
                                        color = Color.White, 
                                        fontSize = 14.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                        Text(
                                            if (user.privilege == 14) "Admin" else "Normal", 
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp, 
                                            color = if (user.privilege == 14) ElectricCyan else SecurityEmerald,
                                            modifier = Modifier.background(
                                                (if (user.privilege == 14) ElectricCyan else SecurityEmerald).copy(alpha = 0.15f),
                                                RoundedCornerShape(4.dp)
                                            ).padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("PIN: ${user.pin}", fontSize = 12.sp, color = Slate400)
                                    }
                                }
                            }
                            IconButton(onClick = { coroutineScope.launch { bleManager.deleteUser(user.uid); bleManager.fetchUsers() } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberCrimson)
                            }
                        }
                    }
                }
            }
        }
    }
}
