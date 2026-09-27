package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.geo.MalaysiaRoadNetwork
import com.example.data.local.RoadQuestDatabase
import com.example.data.model.Achievement
import com.example.data.model.DriveRecord
import com.example.data.model.Friend
import com.example.data.model.RoadSegment
import com.example.data.model.SocialPost
import com.example.data.model.StateProgress
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
import com.example.data.repository.RoadQuestRepository
import com.example.service.LiveDriveTelemetry
import com.example.service.LocationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoadQuestViewModel(application: Application) : AndroidViewModel(application) {
    private val database = RoadQuestDatabase.getDatabase(application)
    private val repository = RoadQuestRepository(
        roadSegmentDao = database.roadSegmentDao(),
        driveRecordDao = database.driveRecordDao(),
        vehicleDao = database.vehicleDao(),
        achievementDao = database.achievementDao()
    )
    val locationTracker = LocationTracker(application)

    val roadSegments: StateFlow<List<RoadSegment>> = repository.allRoadSegments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driveHistory: StateFlow<List<DriveRecord>> = repository.allDrives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeVehicle: StateFlow<Vehicle?> = repository.activeVehicle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val friends: StateFlow<List<Friend>> = repository.friends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialPosts: StateFlow<List<SocialPost>> = repository.socialPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveTelemetry: StateFlow<LiveDriveTelemetry> = locationTracker.telemetry

    private val _selectedState = MutableStateFlow<String?>(null)
    val selectedState = _selectedState.asStateFlow()

    private val _selectedRoadSegment = MutableStateFlow<RoadSegment?>(null)
    val selectedRoadSegment = _selectedRoadSegment.asStateFlow()

    private val _completedDriveSummary = MutableStateFlow<DriveRecord?>(null)
    val completedDriveSummary = _completedDriveSummary.asStateFlow()

    private val _replayDriveRecord = MutableStateFlow<DriveRecord?>(null)
    val replayDriveRecord = _replayDriveRecord.asStateFlow()

    private val _newRoadAlert = MutableStateFlow<RoadSegment?>(null)
    val newRoadAlert = _newRoadAlert.asStateFlow()

    private val newlyDiscoveredInThisDrive = mutableListOf<RoadSegment>()

    // Calculated state statistics
    val stateProgressList: StateFlow<List<StateProgress>> = roadSegments.combine(_selectedState) { segments, _ ->
        val grouped = segments.groupBy { it.state }
        MalaysiaRoadNetwork.MALAYSIAN_STATES.map { stateName ->
            val stateSegments = grouped[stateName] ?: emptyList()
            val totalCount = stateSegments.size
            val exploredCount = stateSegments.count { it.isExplored }
            val percent = if (totalCount > 0) (exploredCount.toDouble() / totalCount.toDouble()) * 100.0 else 0.0
            val totalDistance = stateSegments.sumOf { it.lengthKm }
            val center = MalaysiaRoadNetwork.STATE_CENTERS[stateName] ?: com.example.data.model.GeoPoint(3.139, 101.686)

            StateProgress(
                stateName = stateName,
                exploredPercent = percent,
                totalRoadsCount = totalCount,
                exploredRoadsCount = exploredCount,
                totalDistanceKm = totalDistance,
                centerLat = center.latitude,
                centerLng = center.longitude
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe live telemetry point to match against roads
        viewModelScope.launch {
            liveTelemetry.collect { telemetry ->
                if (telemetry.isDriving && !telemetry.isPaused) {
                    val discovered = repository.checkPointAgainstRoads(telemetry.currentPoint)
                    if (discovered != null) {
                        newlyDiscoveredInThisDrive.add(discovered)
                        locationTracker.registerDiscoveredRoad(discovered.name)
                        _newRoadAlert.value = discovered
                    }
                }
            }
        }
    }

    fun dismissNewRoadAlert() {
        _newRoadAlert.value = null
    }

    fun startDrive(useSimulation: Boolean = false, presetIndex: Int = 0) {
        newlyDiscoveredInThisDrive.clear()
        locationTracker.startDrive(useSimulation, presetIndex)
    }

    fun togglePauseDrive() {
        locationTracker.togglePause()
    }

    fun endDrive() {
        viewModelScope.launch {
            val telemetry = locationTracker.stopDrive()
            val activeCarName = activeVehicle.value?.let { "${it.make} ${it.model}" } ?: "Vehicle"
            val startName = if (telemetry.isSimulated) telemetry.routeName.substringBefore(" (") else "Klang Valley"
            val endName = if (telemetry.isSimulated) telemetry.routeName.substringAfter("-> ").replace(")", "") else "Destination"

            val record = repository.saveCompletedDrive(
                distanceKm = telemetry.distanceKm,
                durationSeconds = telemetry.durationSeconds,
                avgSpeedKmh = telemetry.averageSpeedKmh,
                topSpeedKmh = telemetry.topSpeedKmh,
                startName = startName,
                endName = endName,
                routePoints = telemetry.routePoints,
                newRoadsDiscovered = newlyDiscoveredInThisDrive.toList(),
                vehicleName = activeCarName
            )
            _completedDriveSummary.value = record
        }
    }

    fun clearDriveSummary() {
        _completedDriveSummary.value = null
    }

    fun selectRoadSegment(segment: RoadSegment?) {
        _selectedRoadSegment.value = segment
    }

    fun selectState(stateName: String?) {
        _selectedState.value = stateName
    }

    fun setReplayDrive(drive: DriveRecord?) {
        _replayDriveRecord.value = drive
    }

    fun setActiveVehicle(vehicleId: String) {
        viewModelScope.launch {
            repository.setActiveVehicle(vehicleId)
        }
    }

    fun addVehicle(make: String, model: String, year: Int, variant: String, color: String, nickname: String) {
        viewModelScope.launch {
            val v = Vehicle(
                id = "v-${UUID.randomUUID()}",
                make = make,
                model = model,
                year = year,
                variant = variant,
                color = color,
                nickname = nickname,
                isActive = false
            )
            repository.addVehicle(v)
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicleId)
        }
    }

    fun deleteDriveHistory() {
        viewModelScope.launch {
            repository.deleteDriveHistory()
        }
    }

    fun toggleLikePost(postId: String) {
        repository.togglePostLike(postId)
    }

    fun addFriend(name: String, car: String) {
        repository.addFriend(name, car)
    }

    fun removeFriend(friendId: String) {
        repository.removeFriend(friendId)
    }

    fun updatePrivacySettings(
        privateProfile: Boolean? = null,
        hideExactRoutes: Boolean? = null,
        friendsOnlyLocation: Boolean? = null,
        liveLocationSharing: Boolean? = null
    ) {
        repository.updatePrivacySettings(privateProfile, hideExactRoutes, friendsOnlyLocation, liveLocationSharing)
    }
}
