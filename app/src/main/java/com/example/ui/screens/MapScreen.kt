package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.geo.MalaysiaRoadNetwork
import com.example.data.model.RoadSegment
import com.example.data.model.UserProfile
import com.example.ui.components.GoogleMapContainer
import com.example.ui.components.MalaysiaInteractiveMap
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintDiscovery
import com.example.ui.theme.RoadUnexplored
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    roadSegments: List<RoadSegment>,
    userProfile: UserProfile,
    selectedSegment: RoadSegment?,
    selectedState: String?,
    onSegmentClick: (RoadSegment) -> Unit,
    onDismissSegment: () -> Unit,
    onSelectState: (String?) -> Unit,
    onStartDriveClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val filteredSegments = remember(roadSegments, selectedState, searchQuery) {
        roadSegments.filter { seg ->
            val matchesState = selectedState == null || seg.state.equals(selectedState, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    seg.name.contains(searchQuery, ignoreCase = true) ||
                    seg.code.contains(searchQuery, ignoreCase = true) ||
                    seg.city.contains(searchQuery, ignoreCase = true)
            matchesState && matchesSearch
        }
    }

    val exploredCount = roadSegments.count { it.isExplored }
    val totalCount = roadSegments.size
    val completionPercent = if (totalCount > 0) (exploredCount.toDouble() / totalCount.toDouble()) * 100.0 else 0.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("map_screen")
    ) {
        // 1. The Interactive Google Map Container (with Fog of War and Tactical Toggle)
        val stateCenter = selectedState?.let { MalaysiaRoadNetwork.STATE_CENTERS[it] }
        GoogleMapContainer(
            roadSegments = filteredSegments,
            focusedStateCenter = stateCenter,
            onSegmentClick = onSegmentClick,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Exploration HUD Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Stats Header Glass Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exploration_hud_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.94f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CyanAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MALAYSIA EXPLORED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = CyanAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format("%.2f", userProfile.totalExploredPercent)}%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }

                        // Right mini stats
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            MiniStat(
                                label = "ROADS",
                                value = "$exploredCount / $totalCount",
                                accent = CyanAccent
                            )
                            MiniStat(
                                label = "DISTANCE",
                                value = "${String.format("%.0f", userProfile.totalDistanceKm)} km",
                                accent = AmberGold
                            )
                            MiniStat(
                                label = "DRIVES",
                                value = "${userProfile.totalDrives}",
                                accent = MintDiscovery
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (userProfile.totalExploredPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanAccent,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sub row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${userProfile.statesVisited} States Visited • ${userProfile.citiesVisited} Cities Discovered",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Text(
                            text = "Fog of War Active",
                            fontSize = 11.sp,
                            color = MintDiscovery,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // State Filters & Search Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All States" Chip
                FilterChip(
                    selected = selectedState == null,
                    onClick = { onSelectState(null) },
                    label = { Text("All Malaysia", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedState == null,
                        borderColor = CardBorder,
                        selectedBorderColor = CyanAccent
                    )
                )

                listOf("Selangor", "Kuala Lumpur", "Penang", "Perak", "Johor", "Pahang", "Melaka", "Negeri Sembilan").forEach { state ->
                    FilterChip(
                        selected = selectedState == state,
                        onClick = { onSelectState(if (selectedState == state) null else state) },
                        label = { Text(state, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = DarkBackground,
                            containerColor = DarkSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedState == state,
                            borderColor = CardBorder,
                            selectedBorderColor = CyanAccent
                        )
                    )
                }
            }
        }

        // 3. Quick Floating Start Drive CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 100.dp)
        ) {
            Button(
                onClick = onStartDriveClicked,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("map_start_drive_fab"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = DarkBackground
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Drive", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }

        // 4. Selected Road Segment Inspection Bottom Sheet
        AnimatedVisibility(
            visible = selectedSegment != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            selectedSegment?.let { segment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("road_inspection_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = segment.code,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (segment.isExplored) CyanAccent else AmberGold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkSurfaceElevated)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (segment.isExplored) "EXPLORED" else "UNEXPLORED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = if (segment.isExplored) MintDiscovery else TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = segment.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${segment.city}, ${segment.state}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            IconButton(onClick = onDismissSegment) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("SEGMENT LENGTH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("${segment.lengthKm} km", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Column {
                                Text("TIMES DRIVEN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("${segment.timesDriven}x", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                            }

                            Column {
                                Text("XP VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("+10 XP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MintDiscovery)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onStartDriveClicked,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (segment.isExplored) DarkSurfaceElevated else CyanAccent,
                                contentColor = if (segment.isExplored) CyanAccent else DarkBackground
                            )
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (segment.isExplored) "Drive Again" else "Drive & Unlock This Road",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    accent: Color
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = accent
        )
    }
}
