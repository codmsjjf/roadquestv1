package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Friend
import com.example.data.model.UserProfile
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintDiscovery
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val car: String,
    val scoreDisplay: String,
    val scoreRaw: Double,
    val isCurrentUser: Boolean
)

@Composable
fun LeaderboardScreen(
    userProfile: UserProfile,
    friends: List<Friend>,
    modifier: Modifier = Modifier
) {
    // Categories: 0 = Exploration %, 1 = Distance, 2 = Roads, 3 = State Explorer
    var selectedCategory by remember { mutableIntStateOf(0) }
    // Scope: 0 = Friends, 1 = Local (Klang Valley), 2 = Malaysia
    var selectedScope by remember { mutableIntStateOf(2) }

    val categories = listOf("Exploration %", "Distance", "Roads", "States")
    val scopes = listOf("Friends", "Local", "Malaysia")

    // Generate ranked list based on category & scope
    val rankedEntries = remember(selectedCategory, selectedScope, userProfile, friends) {
        val userEntry = when (selectedCategory) {
            0 -> LeaderboardEntry(0, "${userProfile.username} (You)", userProfile.activeCar, "${String.format("%.2f", userProfile.totalExploredPercent)}%", userProfile.totalExploredPercent, true)
            1 -> LeaderboardEntry(0, "${userProfile.username} (You)", userProfile.activeCar, "${String.format("%.0f", userProfile.totalDistanceKm)} km", userProfile.totalDistanceKm, true)
            2 -> LeaderboardEntry(0, "${userProfile.username} (You)", userProfile.activeCar, "${userProfile.roadsDiscovered} roads", userProfile.roadsDiscovered.toDouble(), true)
            else -> LeaderboardEntry(0, "${userProfile.username} (You)", userProfile.activeCar, "${userProfile.statesVisited} states", userProfile.statesVisited.toDouble(), true)
        }

        val pool = mutableListOf(userEntry)

        friends.forEach { f ->
            val entry = when (selectedCategory) {
                0 -> LeaderboardEntry(0, f.name, f.car, "${String.format("%.2f", f.explorationPercent)}%", f.explorationPercent, false)
                1 -> LeaderboardEntry(0, f.name, f.car, "${String.format("%.0f", f.totalDistanceKm)} km", f.totalDistanceKm, false)
                2 -> LeaderboardEntry(0, f.name, f.car, "${f.roadsDiscovered} roads", f.roadsDiscovered.toDouble(), false)
                else -> LeaderboardEntry(0, f.name, f.car, "${f.statesVisited} states", f.statesVisited.toDouble(), false)
            }
            pool.add(entry)
        }

        if (selectedScope != 0) {
            // Add prominent regional Malaysian drivers for Local/National scale
            val extraDrivers = listOf(
                LeaderboardEntry(0, "Hafiz Motorsport", "Porsche 911 GT3 (992)", if (selectedCategory == 0) "38.45%" else if (selectedCategory == 1) "6,420 km" else if (selectedCategory == 2) "720 roads" else "12 states", if (selectedCategory == 0) 38.45 else if (selectedCategory == 1) 6420.0 else if (selectedCategory == 2) 720.0 else 12.0, false),
                LeaderboardEntry(0, "Aiman SVR", "Mercedes-AMG A45S", if (selectedCategory == 0) "29.10%" else if (selectedCategory == 1) "4,890 km" else if (selectedCategory == 2) "590 roads" else "11 states", if (selectedCategory == 0) 29.10 else if (selectedCategory == 1) 4890.0 else if (selectedCategory == 2) 590.0 else 11.0, false),
                LeaderboardEntry(0, "Sarah Tan", "Subaru WRX STI", if (selectedCategory == 0) "22.60%" else if (selectedCategory == 1) "3,750 km" else if (selectedCategory == 2) "410 roads" else "9 states", if (selectedCategory == 0) 22.60 else if (selectedCategory == 1) 3750.0 else if (selectedCategory == 2) 410.0 else 9.0, false),
                LeaderboardEntry(0, "Khairul Nizam", "Proton Saga R3", if (selectedCategory == 0) "16.80%" else if (selectedCategory == 1) "2,620 km" else if (selectedCategory == 2) "310 roads" else "8 states", if (selectedCategory == 0) 16.80 else if (selectedCategory == 1) 2620.0 else if (selectedCategory == 2) 310.0 else 8.0, false)
            )
            pool.addAll(extraDrivers)
        }

        pool.sortedByDescending { it.scoreRaw }.mapIndexed { index, item ->
            item.copy(rank = index + 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("leaderboard_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXPLORATION RANKINGS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = CyanAccent
                    )
                    Text(
                        text = "Leaderboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Scope Selector Filter Chips (Friends / Local / Malaysia)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scopes.forEachIndexed { index, scopeName ->
                    FilterChip(
                        selected = selectedScope == index,
                        onClick = { selectedScope = index },
                        label = { Text(scopeName, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = DarkBackground,
                            containerColor = DarkSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedScope == index,
                            borderColor = CardBorder,
                            selectedBorderColor = CyanAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Tabs
            TabRow(
                selectedTabIndex = selectedCategory,
                containerColor = DarkSurface,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCategory]),
                        color = CyanAccent,
                        height = 3.dp
                    )
                }
            ) {
                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = {
                            Text(
                                cat,
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Safety Reminder Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Rankings reward road discovery & distance. Safe driving is prioritized.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Top 3 Podium Cards
            if (rankedEntries.size >= 3) {
                PodiumSection(rankedEntries.take(3))
            }

            // Full List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(rankedEntries) { _, entry ->
                    LeaderboardRow(entry = entry)
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}

@Composable
private fun PodiumSection(topThree: List<LeaderboardEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Rank 2 (Left)
        PodiumCard(entry = topThree.getOrNull(1), rank = 2, color = Color(0xFFC0C0C0), modifier = Modifier.weight(1f))
        // Rank 1 (Center - elevated)
        PodiumCard(entry = topThree.getOrNull(0), rank = 1, color = AmberGold, modifier = Modifier.weight(1.1f))
        // Rank 3 (Right)
        PodiumCard(entry = topThree.getOrNull(2), rank = 3, color = Color(0xFFCD7F32), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PodiumCard(
    entry: LeaderboardEntry?,
    rank: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (entry == null) return
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (entry.isCurrentUser) DarkSurfaceElevated else DarkSurface),
        border = androidx.compose.foundation.BorderStroke(if (entry.isCurrentUser) 2.dp else 1.dp, if (entry.isCurrentUser) CyanAccent else CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = DarkBackground
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.name.substringBefore(" (").take(11),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (entry.isCurrentUser) CyanAccent else TextPrimary,
                maxLines = 1
            )
            Text(
                text = entry.scoreDisplay,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) DarkSurfaceElevated else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (entry.isCurrentUser) 1.5.dp else 1.dp,
            if (entry.isCurrentUser) CyanAccent else CardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${entry.rank}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (entry.rank <= 3) AmberGold else TextMuted,
                    modifier = Modifier.width(34.dp)
                )

                Column {
                    Text(
                        text = entry.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isCurrentUser) CyanAccent else TextPrimary
                    )
                    Text(
                        text = entry.car,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = entry.scoreDisplay,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (entry.isCurrentUser) CyanAccent else TextPrimary
            )
        }
    }
}
