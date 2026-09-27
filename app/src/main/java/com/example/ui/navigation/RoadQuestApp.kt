package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DriveSummaryDialog
import com.example.ui.components.ReplayDriveDialog
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.DriveHistoryScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LiveDriveScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RoadQuestViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    MAP("Map", Icons.Default.Map),
    DRIVE("Drive", Icons.Default.DirectionsCar),
    HISTORY("History", Icons.Default.History),
    ACTIVITY("Activity", Icons.Default.People),
    LEADERBOARD("Ranks", Icons.Default.EmojiEvents),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun RoadQuestApp(
    viewModel: RoadQuestViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavigationTab.MAP) }

    val roadSegments by viewModel.roadSegments.collectAsStateWithLifecycle()
    val driveHistory by viewModel.driveHistory.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val activeVehicle by viewModel.activeVehicle.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val socialPosts by viewModel.socialPosts.collectAsStateWithLifecycle()
    val liveTelemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()
    val selectedState by viewModel.selectedState.collectAsStateWithLifecycle()
    val selectedSegment by viewModel.selectedRoadSegment.collectAsStateWithLifecycle()
    val completedDriveSummary by viewModel.completedDriveSummary.collectAsStateWithLifecycle()
    val replayDriveRecord by viewModel.replayDriveRecord.collectAsStateWithLifecycle()
    val newRoadAlert by viewModel.newRoadAlert.collectAsStateWithLifecycle()
    val stateProgressList by viewModel.stateProgressList.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = CyanAccent,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.MAP -> {
                    MapScreen(
                        roadSegments = roadSegments,
                        userProfile = userProfile,
                        selectedSegment = selectedSegment,
                        selectedState = selectedState,
                        onSegmentClick = { viewModel.selectRoadSegment(it) },
                        onDismissSegment = { viewModel.selectRoadSegment(null) },
                        onSelectState = { viewModel.selectState(it) },
                        onStartDriveClicked = { currentTab = NavigationTab.DRIVE }
                    )
                }

                NavigationTab.DRIVE -> {
                    LiveDriveScreen(
                        telemetry = liveTelemetry,
                        activeVehicle = activeVehicle,
                        allRoadSegments = roadSegments,
                        newRoadAlert = newRoadAlert,
                        onDismissAlert = { viewModel.dismissNewRoadAlert() },
                        onStartDrive = { isSimulated, presetIndex ->
                            viewModel.startDrive(isSimulated, presetIndex)
                        },
                        onTogglePause = { viewModel.togglePauseDrive() },
                        onEndDrive = { viewModel.endDrive() },
                        simulationPresets = viewModel.locationTracker.simulationPresets
                    )
                }

                NavigationTab.HISTORY -> {
                    DriveHistoryScreen(
                        drives = driveHistory,
                        onSelectDrive = { viewModel.setReplayDrive(it) },
                        onReplayDrive = { viewModel.setReplayDrive(it) },
                        onDeleteAllDrives = { viewModel.deleteDriveHistory() }
                    )
                }

                NavigationTab.ACTIVITY -> {
                    ActivityScreen(
                        socialPosts = socialPosts,
                        friends = friends,
                        userProfile = userProfile,
                        onLikePost = { viewModel.toggleLikePost(it) },
                        onAddFriend = { name, car -> viewModel.addFriend(name, car) },
                        onRemoveFriend = { viewModel.removeFriend(it) }
                    )
                }

                NavigationTab.LEADERBOARD -> {
                    LeaderboardScreen(
                        userProfile = userProfile,
                        friends = friends
                    )
                }

                NavigationTab.PROFILE -> {
                    ProfileScreen(
                        userProfile = userProfile,
                        vehicles = vehicles,
                        activeVehicle = activeVehicle,
                        achievements = achievements,
                        stateProgressList = stateProgressList,
                        onSetActiveVehicle = { viewModel.setActiveVehicle(it) },
                        onAddVehicle = { make, model, yr, variant, color, nick ->
                            viewModel.addVehicle(make, model, yr, variant, color, nick)
                        },
                        onDeleteVehicle = { viewModel.deleteVehicle(it) },
                        onUpdatePrivacy = { priv, hide, fr, live ->
                            viewModel.updatePrivacySettings(priv, hide, fr, live)
                        },
                        onDeleteDriveHistory = { viewModel.deleteDriveHistory() }
                    )
                }
            }

            // Drive Summary Full Dialog
            completedDriveSummary?.let { summary ->
                DriveSummaryDialog(
                    record = summary,
                    onDismiss = { viewModel.clearDriveSummary() },
                    onReplay = { record ->
                        viewModel.clearDriveSummary()
                        viewModel.setReplayDrive(record)
                    }
                )
            }

            // Replay Drive Dialog
            replayDriveRecord?.let { drive ->
                ReplayDriveDialog(
                    record = drive,
                    onDismiss = { viewModel.setReplayDrive(null) }
                )
            }
        }
    }
}
