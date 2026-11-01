package com.liongate.smartlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun ScheduleScreen(bleManager: BioLockBleManager) {
    val schedules by bleManager.schedules.collectAsState()
    val isLoadingSchedules by bleManager.isLoadingSchedules.collectAsState()
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
                Icon(Icons.Default.Schedule, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Access Schedules", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
            IconButton(
                onClick = { coroutineScope.launch { bleManager.fetchTimeZones() } },
                modifier = Modifier.size(32.dp).background(SurfaceTier1, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = if (isLoadingSchedules) Slate400 else ElectricCyan, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingSchedules && schedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        } else if (schedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No schedules found", color = Slate400)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(schedules) { schedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { /* Action edit not in scope, just visual mapping */ },
                        colors = CardDefaults.cardColors(containerColor = SurfaceTier1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceTier3)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "TIMETABLE ${schedule.timezoneId}", 
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold, 
                                    color = ElectricCyan, 
                                    fontSize = 12.sp
                                )
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 7-Day Matrix Selectors
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                                days.forEachIndexed { i, d ->
                                    val isActive = schedule.activeDays.getOrNull(i) == true
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .background(
                                                if (isActive) ElectricCyan.copy(alpha = 0.15f) else SurfaceTier2,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(1.dp, if (isActive) ElectricCyan else SurfaceTier3, RoundedCornerShape(4.dp))
                                            .padding(vertical = 8.dp, horizontal = 10.dp)
                                    ) {
                                        Text(
                                            d,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) ElectricCyan else Slate400,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (isActive) ElectricCyan else Color.Transparent, CircleShape)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 24h Timeline Bar representation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(schedule.formattedStartTime, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White)
                                Text(schedule.formattedEndTime, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CanvasVoid)
                                    .border(1.dp, SurfaceTier3, RoundedCornerShape(4.dp))
                            ) {
                                // Simple active visual bar
                                val startFraction = (schedule.startHour * 60 + schedule.startMinute) / (24f * 60f)
                                val endFraction = (schedule.endHour * 60 + schedule.endMinute) / (24f * 60f)
                                val widthFraction = if (endFraction >= startFraction) endFraction - startFraction else 0f
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(widthFraction)
                                        .align(Alignment.CenterStart)
                                        .offset(x = 300.dp * startFraction) // Approximation
                                        .background(ElectricCyan)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
