package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Achievement
import com.example.data.model.StateProgress
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
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
fun ProfileScreen(
    userProfile: UserProfile,
    vehicles: List<Vehicle>,
    activeVehicle: Vehicle?,
    achievements: List<Achievement>,
    stateProgressList: List<StateProgress>,
    onSetActiveVehicle: (String) -> Unit,
    onAddVehicle: (String, String, Int, String, String, String) -> Unit,
    onDeleteVehicle: (String) -> Unit,
    onUpdatePrivacy: (Boolean?, Boolean?, Boolean?, Boolean?) -> Unit,
    onDeleteDriveHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Overview, 1 = Garage, 2 = States, 3 = Achievements, 4 = Privacy
    var showAddVehicleDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("profile_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_roadquest_hero),
                    contentDescription = "Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.6f to DarkBackground.copy(alpha = 0.7f),
                                1.0f to DarkBackground
                            )
                        )
                )

                // Level Tag in top corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground.copy(alpha = 0.8f))
                        .border(1.dp, AmberGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "LEVEL ${userProfile.level}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberGold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // User Identity Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(2.dp, CyanAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.username.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = userProfile.username.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )

                Text(
                    text = userProfile.activeCar,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold
                )

                Text(
                    text = userProfile.callsign,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // XP Progress Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "DRIVER PROGRESSION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${userProfile.currentXp} / ${userProfile.nextLevelXp} XP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (userProfile.currentXp.toFloat() / userProfile.nextLevelXp.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyanAccent,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs (Overview, Garage, States, Achievements, Privacy)
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = DarkSurface,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = CyanAccent,
                        height = 3.dp
                    )
                }
            ) {
                Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }, text = { Text("Stats", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }, text = { Text("Garage", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }, text = { Text("States", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                Tab(selected = selectedSection == 3, onClick = { selectedSection = 3 }, text = { Text("Badges", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                Tab(selected = selectedSection == 4, onClick = { selectedSection = 4 }, text = { Text("Privacy", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedSection) {
                0 -> OverviewSection(userProfile)
                1 -> GarageSection(vehicles, activeVehicle, onSetActiveVehicle, { showAddVehicleDialog = true }, onDeleteVehicle)
                2 -> StatesProgressSection(stateProgressList)
                3 -> AchievementsSection(achievements)
                4 -> PrivacySection(userProfile, onUpdatePrivacy, onDeleteDriveHistory)
            }
        }

        if (showAddVehicleDialog) {
            AddVehicleDialog(
                onDismiss = { showAddVehicleDialog = false },
                onAdd = { make, model, yr, variant, color, nick ->
                    onAddVehicle(make, model, yr, variant, color, nick)
                    showAddVehicleDialog = false
                }
            )
        }
    }
}

@Composable
private fun OverviewSection(userProfile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                label = "EXPLORATION",
                value = "${String.format("%.2f", userProfile.totalExploredPercent)}%",
                sub = "Malaysia Unlocked",
                accent = CyanAccent,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "DISTANCE",
                value = "${String.format("%.0f", userProfile.totalDistanceKm)} km",
                sub = "Total Driven",
                accent = AmberGold,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                label = "TOTAL DRIVES",
                value = "${userProfile.totalDrives}",
                sub = "Completed Logbooks",
                accent = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "TOP SPEED",
                value = "${String.format("%.0f", userProfile.topSpeedKmh)} km/h",
                sub = "Personal Track Pace",
                accent = TextSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                label = "ROADS DISCOVERED",
                value = "${userProfile.roadsDiscovered}",
                sub = "Unique Segments",
                accent = MintDiscovery,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "STATES VISITED",
                value = "${userProfile.statesVisited} / 13",
                sub = "${userProfile.citiesVisited} Cities",
                accent = AmberGold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GarageSection(
    vehicles: List<Vehicle>,
    activeVehicle: Vehicle?,
    onSetActive: (String) -> Unit,
    onAddNew: () -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PERSONAL FLEET (${vehicles.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBackground),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Vehicle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        vehicles.forEach { vehicle ->
            val isActive = activeVehicle?.id == vehicle.id
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isActive) DarkSurfaceElevated else DarkSurface),
                border = androidx.compose.foundation.BorderStroke(if (isActive) 1.5.dp else 1.dp, if (isActive) CyanAccent else CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DarkBackground,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CyanAccent)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Nickname: \"${vehicle.nickname}\" • ${vehicle.color}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        if (!isActive && vehicles.size > 1) {
                            IconButton(onClick = { onDelete(vehicle.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format("%.0f", vehicle.totalDistanceKm)} km logged",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent
                        )
                        Text(
                            text = "${vehicle.totalDrives} drives",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Top: ${String.format("%.0f", vehicle.topSpeedKmh)} km/h",
                            fontSize = 12.sp,
                            color = AmberGold
                        )
                    }

                    if (!isActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onSetActive(vehicle.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent)
                        ) {
                            Text("Set as Active Driving Vehicle", color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatesProgressSection(stateProgressList: List<StateProgress>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "MALAYSIA STATE BREAKDOWN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        stateProgressList.forEach { state ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.stateName.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${String.format("%.1f", state.exploredPercent)}% explored",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (state.exploredPercent > 0) CyanAccent else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (state.exploredPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (state.exploredPercent > 50) MintDiscovery else CyanAccent,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${state.exploredRoadsCount} of ${state.totalRoadsCount} road segments mapped (${String.format("%.0f", state.totalDistanceKm)} km network)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementsSection(achievements: List<Achievement>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val unlockedCount = achievements.count { it.isUnlocked }
        Text(
            text = "ACHIEVEMENTS ($unlockedCount / ${achievements.size} UNLOCKED)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        achievements.forEach { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (ach.isUnlocked) AmberGold.copy(alpha = 0.5f) else CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (ach.isUnlocked) AmberGold.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(1.dp, if (ach.isUnlocked) AmberGold else CardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (ach.isUnlocked) AmberGold else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ach.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) TextPrimary else TextSecondary
                            )
                            Text(
                                text = "+${ach.xpReward} XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) MintDiscovery else TextMuted
                            )
                        }

                        Text(
                            text = ach.description,
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (!ach.isUnlocked) {
                            LinearProgressIndicator(
                                progress = { (ach.currentProgress / ach.requiredProgress).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = CyanAccent,
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacySection(
    userProfile: UserProfile,
    onUpdatePrivacy: (Boolean?, Boolean?, Boolean?, Boolean?) -> Unit,
    onDeleteDriveHistory: () -> Unit
) {
    var showDeleteHistoryModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PRIVACY & LOCATION SAFETY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                PrivacyToggle(
                    title = "Private Profile",
                    desc = "Only accepted friends can view your garage and exploration map.",
                    checked = userProfile.privateProfile,
                    onChecked = { onUpdatePrivacy(it, null, null, null) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyToggle(
                    title = "Hide Exact Drive Routes",
                    desc = "Obscures origin and destination points within 1 km of home/work.",
                    checked = userProfile.hideExactRoutes,
                    onChecked = { onUpdatePrivacy(null, it, null, null) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyToggle(
                    title = "Friends-Only Location Sharing",
                    desc = "Never broadcasts your location publicly.",
                    checked = userProfile.friendsOnlyLocation,
                    onChecked = { onUpdatePrivacy(null, null, it, null) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyToggle(
                    title = "Live Location Sharing",
                    desc = "Show real-time vehicle radar to mutual friends while driving.",
                    checked = userProfile.liveLocationSharing,
                    onChecked = { onUpdatePrivacy(null, null, null, it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data Management Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DATA MANAGEMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showDeleteHistoryModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f), contentColor = DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Drive History & Route Points", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteHistoryModal) {
        AlertDialog(
            onDismissRequest = { showDeleteHistoryModal = false },
            title = { Text("Delete Drive History?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently wipe all your past driving routes and reset personal distance tallies. Your discovered roads on the Malaysia map will remain unlocked.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDriveHistory()
                        showDeleteHistoryModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Permanently Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteHistoryModal = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun PrivacyToggle(
    title: String,
    desc: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(desc, fontSize = 11.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = CyanAccent
            )
        )
    }
}

@Composable
private fun ProfileStatCard(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = accent
            )
            Text(
                text = sub,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAdd: (make: String, model: String, year: Int, variant: String, color: String, nickname: String) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var yearStr by remember { mutableStateOf("2023") }
    var variant by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vehicle to Garage", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = make,
                    onValueChange = { make = it },
                    label = { Text("Manufacturer (e.g. BMW, Honda, Proton)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = CyanAccent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model (e.g. F30 330i, X50, Civic)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = CyanAccent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = yearStr,
                    onValueChange = { yearStr = it },
                    label = { Text("Model Year") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = CyanAccent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = variant,
                    onValueChange = { variant = it },
                    label = { Text("Variant (e.g. M Sport, Flagship, RS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = CyanAccent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Exterior Color") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = CyanAccent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Vehicle Nickname / Callsign") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = CyanAccent)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (make.isNotBlank() && model.isNotBlank()) {
                        val yr = yearStr.toIntOrNull() ?: 2023
                        onAdd(make, model, yr, variant, color, if (nickname.isBlank()) model else nickname)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBackground)
            ) {
                Text("Add to Garage", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = DarkSurface
    )
}
