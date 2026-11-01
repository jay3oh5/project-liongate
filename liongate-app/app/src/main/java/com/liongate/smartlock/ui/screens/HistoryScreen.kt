package com.liongate.smartlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liongate.smartlock.ble.BioLockBleManager
import com.liongate.smartlock.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(bleManager: BioLockBleManager) {
    val auditLogs by bleManager.auditLogs.collectAsState()
    val isLoadingLogs by bleManager.isLoadingLogs.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = SecurityEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Access Audit Logs", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
            IconButton(
                onClick = { coroutineScope.launch { bleManager.fetchAuditLogs() } },
                modifier = Modifier.size(32.dp).background(SurfaceTier1, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = if (isLoadingLogs) Slate400 else SecurityEmerald, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingLogs && auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SecurityEmerald)
            }
        } else if (auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No records found", color = Slate400)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(auditLogs) { log ->
                    Column {
                        // Log Timeline Item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Timeline dot
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp, end = 12.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(SecurityEmerald, CircleShape))
                                Box(modifier = Modifier.width(1.dp).height(50.dp).background(SurfaceTier3))
                            }
                            
                            // Log details
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).background(SecurityEmerald.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = SecurityEmerald, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            log.verifyTypeDescription,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "User/Card: ${if (log.cardNo.isNotEmpty() && log.cardNo != "0") log.cardNo else log.pin}",
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "GRANTED",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SecurityEmerald,
                                            modifier = Modifier
                                                .background(SecurityEmerald.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                                .border(1.dp, SecurityEmerald.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            log.formattedTime,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
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
