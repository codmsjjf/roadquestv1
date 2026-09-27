package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.geo.GeoUtils
import com.example.data.model.DriveRecord
import com.example.data.model.GeoPoint
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MintDiscovery
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun ReplayDriveDialog(
    record: DriveRecord,
    onDismiss: () -> Unit
) {
    val points = remember(record) { GeoUtils.parsePoints(record.routePointsJson) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(isPlaying, currentIndex, playbackSpeed) {
        if (isPlaying && points.isNotEmpty() && currentIndex < points.size - 1) {
            val stepDelay = (400L / playbackSpeed).toLong().coerceAtLeast(80L)
            delay(stepDelay)
            currentIndex += 1
        }
    }

    val currentPoint: GeoPoint? = if (points.isNotEmpty()) points[currentIndex.coerceIn(0, points.size - 1)] else null
    val traveledPoints = if (points.isNotEmpty()) points.subList(0, (currentIndex + 1).coerceAtMost(points.size)) else emptyList()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .testTag("replay_drive_dialog"),
            color = DarkBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "REPLAY DRIVE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = AmberGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${playbackSpeed.toInt()}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                        Text(
                            text = "${record.startLocationName} → ${record.endLocationName}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_replay_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Interactive Map View showing animated trace
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    MalaysiaInteractiveMap(
                        roadSegments = emptyList(),
                        activeRoutePoints = traveledPoints,
                        userLocation = currentPoint,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Player Controls Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Telemetry stats while replaying
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val progressRatio = if (points.size > 1) currentIndex.toFloat() / (points.size - 1) else 1f
                            val currentDist = record.distanceKm * progressRatio
                            val currentElapsedSec = (record.durationSeconds * progressRatio).toLong()

                            Column {
                                Text("DISTANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text(
                                    "${String.format("%.1f", currentDist)} / ${String.format("%.1f", record.distanceKm)} km",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("ELAPSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text(
                                    "${currentElapsedSec / 60}m ${currentElapsedSec % 60}s",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress Slider
                        Slider(
                            value = if (points.size > 1) currentIndex.toFloat() / (points.size - 1) else 0f,
                            onValueChange = { ratio ->
                                currentIndex = (ratio * (points.size - 1)).toInt().coerceIn(0, (points.size - 1).coerceAtLeast(0))
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent,
                                inactiveTrackColor = DarkBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Playback control buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { currentIndex = 0; isPlaying = true }
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = "Restart", tint = TextSecondary)
                            }

                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(CyanAccent, RoundedCornerShape(24.dp))
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = DarkBackground
                                )
                            }

                            IconButton(
                                onClick = {
                                    playbackSpeed = when (playbackSpeed) {
                                        1f -> 2f
                                        2f -> 4f
                                        else -> 1f
                                    }
                                }
                            ) {
                                Icon(Icons.Default.FastForward, contentDescription = "Speed", tint = AmberGold)
                            }
                        }
                    }
                }
            }
        }
    }
}
