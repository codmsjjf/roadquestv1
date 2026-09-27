package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoadSegment
import com.example.data.model.Vehicle
import com.example.service.LiveDriveTelemetry
import com.example.service.LocationTracker
import com.example.ui.components.MalaysiaInteractiveMap
import com.example.ui.components.SpeedometerGauge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintDiscovery
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveDriveScreen(
    telemetry: LiveDriveTelemetry,
    activeVehicle: Vehicle?,
    allRoadSegments: List<RoadSegment>,
    newRoadAlert: RoadSegment?,
    onDismissAlert: () -> Unit,
    onStartDrive: (useSimulation: Boolean, presetIndex: Int) -> Unit,
    onTogglePause: () -> Unit,
    onEndDrive: () -> Unit,
    simulationPresets: List<Pair<String, List<com.example.data.model.GeoPoint>>>,
    modifier: Modifier = Modifier
) {
    var useSimulation by remember { mutableStateOf(true) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var showPresetMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("live_drive_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar / Active Car Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE DRIVE TRACKER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = CyanAccent
                    )
                    Text(
                        text = activeVehicle?.let { "${it.make} ${it.model}" } ?: "RoadQuest Vehicle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                if (telemetry.isDriving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (telemetry.isPaused) AmberGold.copy(alpha = 0.2f)
                                else MintDiscovery.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (telemetry.isPaused) AmberGold else MintDiscovery)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (telemetry.isPaused) "PAUSED" else "RECORDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.isPaused) AmberGold else MintDiscovery
                        )
                    }
                }
            }

            // New Road Unlocked Popup Banner
            AnimatedVisibility(
                visible = newRoadAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                newRoadAlert?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_road_banner"),
                        colors = CardDefaults.cardColors(containerColor = MintDiscovery),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DarkBackground)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "NEW ROAD DISCOVERED!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DarkBackground
                                    )
                                    Text(
                                        text = "${alert.name} (+10 XP)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }
                            }
                            IconButton(onClick = onDismissAlert) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DarkBackground)
                            }
                        }
                    }
                }
            }

            // 1. Digital Speedometer & Arc Gauge
            SpeedometerGauge(
                currentSpeedKmh = telemetry.currentSpeedKmh,
                maxSpeedKmh = telemetry.topSpeedKmh,
                avgSpeedKmh = telemetry.averageSpeedKmh,
                modifier = Modifier.padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Primary Telemetry Metrics Row (Distance, Duration, New Roads, % Gained)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBox(
                    label = "DISTANCE",
                    value = "${String.format("%.1f", telemetry.distanceKm)}",
                    unit = "km",
                    color = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "DURATION",
                    value = formatTime(telemetry.durationSeconds),
                    unit = "",
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "NEW ROADS",
                    value = "${telemetry.newlyDiscoveredRoads.size}",
                    unit = "discovered",
                    color = MintDiscovery,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "EXPLORATION",
                    value = "+${String.format("%.2f", telemetry.explorationPercentGained)}",
                    unit = "% MY",
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Live Map View Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MalaysiaInteractiveMap(
                        roadSegments = allRoadSegments,
                        activeRoutePoints = telemetry.routePoints,
                        userLocation = telemetry.currentPoint,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Route overlay label
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (telemetry.isDriving) telemetry.routeName else "GPS Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Controls: Start / Stop / Pause Drive
            if (!telemetry.isDriving) {
                // Setup Section before starting
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DRIVE SIMULATOR / TEST ROUTE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = if (useSimulation) "Simulating iconic Malaysia road" else "Real Hardware GPS",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = useSimulation,
                                onCheckedChange = { useSimulation = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkBackground,
                                    checkedTrackColor = CyanAccent
                                )
                            )
                        }

                        if (useSimulation) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceVariant)
                                    .clickable { showPresetMenu = true }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "ROUTE PRESET",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = simulationPresets.getOrNull(selectedPresetIndex)?.first ?: "Default",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                    }
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = TextSecondary)
                                }

                                DropdownMenu(
                                    expanded = showPresetMenu,
                                    onDismissRequest = { showPresetMenu = false },
                                    modifier = Modifier.background(DarkSurfaceElevated)
                                ) {
                                    simulationPresets.forEachIndexed { index, preset ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    preset.first,
                                                    color = if (selectedPresetIndex == index) CyanAccent else TextPrimary,
                                                    fontSize = 13.sp
                                                )
                                            },
                                            onClick = {
                                                selectedPresetIndex = index
                                                showPresetMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Prominent START DRIVE button
                Button(
                    onClick = { onStartDrive(useSimulation, selectedPresetIndex) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("start_drive_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = DarkBackground
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START DRIVE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
            } else {
                // While driving controls: Pause/Resume and End Drive
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onTogglePause,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("pause_drive_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (telemetry.isPaused) MintDiscovery else AmberGold
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (telemetry.isPaused) MintDiscovery else AmberGold
                        )
                    ) {
                        Icon(
                            if (telemetry.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (telemetry.isPaused) "Resume" else "Pause",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onEndDrive,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("end_drive_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finish Drive", fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = color
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
