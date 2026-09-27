package com.example.data.repository

import com.example.data.geo.GeoUtils
import com.example.data.geo.MalaysiaRoadNetwork
import com.example.data.local.AchievementDao
import com.example.data.local.DriveRecordDao
import com.example.data.local.RoadSegmentDao
import com.example.data.local.VehicleDao
import com.example.data.model.Achievement
import com.example.data.model.DriveRecord
import com.example.data.model.Friend
import com.example.data.model.GeoPoint
import com.example.data.model.RoadSegment
import com.example.data.model.SocialPost
import com.example.data.model.StateProgress
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class RoadQuestRepository(
    private val roadSegmentDao: RoadSegmentDao,
    private val driveRecordDao: DriveRecordDao,
    private val vehicleDao: VehicleDao,
    private val achievementDao: AchievementDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val allRoadSegments: Flow<List<RoadSegment>> = roadSegmentDao.getAllRoadSegments()
    val allDrives: Flow<List<DriveRecord>> = driveRecordDao.getAllDriveRecords()
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    val activeVehicle: Flow<Vehicle?> = vehicleDao.getActiveVehicle()
    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile = _userProfile.asStateFlow()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _socialPosts = MutableStateFlow<List<SocialPost>>(emptyList())
    val socialPosts = _socialPosts.asStateFlow()

    init {
        scope.launch {
            seedDatabaseIfEmpty()
            seedSocialAndFriends()
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val totalCount = roadSegmentDao.getTotalRoadCount()
        if (totalCount == 0) {
            roadSegmentDao.insertRoadSegments(MalaysiaRoadNetwork.INITIAL_ROAD_SEGMENTS)
        }

        // Vehicles
        val vehicles = vehicleDao.getAllVehicles().first()
        if (vehicles.isEmpty()) {
            vehicleDao.insertVehicles(
                listOf(
                    Vehicle(
                        id = "v-bmw-f30",
                        make = "BMW",
                        model = "F30 330i M Sport",
                        year = 2016,
                        variant = "2.0L Turbo / 252hp",
                        color = "Estoril Blue",
                        nickname = "Bimmer 330",
                        totalDistanceKm = 1845.2,
                        totalDrives = 62,
                        topSpeedKmh = 97.0,
                        isActive = true
                    ),
                    Vehicle(
                        id = "v-civic-fl5",
                        make = "Honda",
                        model = "Civic Type R (FL5)",
                        year = 2023,
                        variant = "6MT / Championship Pack",
                        color = "Championship White",
                        nickname = "Type R",
                        totalDistanceKm = 636.3,
                        totalDrives = 24,
                        topSpeedKmh = 94.0,
                        isActive = false
                    ),
                    Vehicle(
                        id = "v-myvi-king",
                        make = "Perodua",
                        model = "Myvi 1.5 AV",
                        year = 2022,
                        variant = "D-CVT Facelift",
                        color = "Cranberry Red",
                        nickname = "King of the Highway",
                        totalDistanceKm = 0.0,
                        totalDrives = 0,
                        topSpeedKmh = 0.0,
                        isActive = false
                    )
                )
            )
        }

        // Initial sample drives
        val drives = driveRecordDao.getAllDriveRecords().first()
        if (drives.isEmpty()) {
            driveRecordDao.insertDriveRecord(
                DriveRecord(
                    id = "drive-001",
                    startTime = System.currentTimeMillis() - 86400000L * 2,
                    endTime = System.currentTimeMillis() - 86400000L * 2 + 3600000L,
                    distanceKm = 42.7,
                    durationSeconds = 3480,
                    avgSpeedKmh = 44.0,
                    topSpeedKmh = 97.0,
                    startLocationName = "Subang Jaya",
                    endLocationName = "KL Sentral",
                    newRoadsDiscoveredCount = 3,
                    newExplorationPercent = 0.21,
                    vehicleName = "BMW F30 330i M Sport",
                    routePointsJson = "3.076,101.588;3.090,101.615;3.105,101.640;3.118,101.662;3.134,101.686",
                    discoveredSegmentIdsJson = "MY-FED-01,MY-FED-03",
                    xpEarned = 180
                )
            )
            driveRecordDao.insertDriveRecord(
                DriveRecord(
                    id = "drive-002",
                    startTime = System.currentTimeMillis() - 86400000L * 4,
                    endTime = System.currentTimeMillis() - 86400000L * 4 + 4800000L,
                    distanceKm = 78.5,
                    durationSeconds = 4920,
                    avgSpeedKmh = 57.4,
                    topSpeedKmh = 95.0,
                    startLocationName = "Damansara",
                    endLocationName = "Putrajaya Boulevard",
                    newRoadsDiscoveredCount = 5,
                    newExplorationPercent = 0.38,
                    vehicleName = "BMW F30 330i M Sport",
                    routePointsJson = "3.158,101.615;3.105,101.660;3.060,101.692;2.980,101.680;2.930,101.682",
                    discoveredSegmentIdsJson = "MY-E20-01,MY-E20-02,MY-LDP-01",
                    xpEarned = 260
                )
            )
        }

        // Achievements
        val achievements = achievementDao.getAllAchievements().first()
        if (achievements.isEmpty()) {
            achievementDao.insertAchievements(
                listOf(
                    Achievement(
                        id = "ach-first-drive",
                        title = "First Drive",
                        description = "Complete your first recorded drive in Malaysia.",
                        category = "Pioneer",
                        requiredProgress = 1.0,
                        currentProgress = 1.0,
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis() - 86400000L * 30,
                        xpReward = 50
                    ),
                    Achievement(
                        id = "ach-road-explorer",
                        title = "Road Explorer",
                        description = "Discover 100 unique Malaysian road segments.",
                        category = "Explorer",
                        requiredProgress = 100.0,
                        currentProgress = 24.0,
                        isUnlocked = false,
                        xpReward = 200
                    ),
                    Achievement(
                        id = "ach-road-hunter",
                        title = "Road Hunter",
                        description = "Discover 500 unique Malaysian road segments.",
                        category = "Explorer",
                        requiredProgress = 500.0,
                        currentProgress = 24.0,
                        isUnlocked = false,
                        xpReward = 1000
                    ),
                    Achievement(
                        id = "ach-my-explorer",
                        title = "Malaysia Explorer",
                        description = "Explore 1% of all road networks in Malaysia.",
                        category = "Milestone",
                        requiredProgress = 1.0,
                        currentProgress = 1.0,
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis() - 86400000L * 15,
                        xpReward = 300
                    ),
                    Achievement(
                        id = "ach-state-hopper",
                        title = "State Hopper",
                        description = "Visit 5 different Malaysian states.",
                        category = "Traveler",
                        requiredProgress = 5.0,
                        currentProgress = 7.0,
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis() - 86400000L * 10,
                        xpReward = 400
                    ),
                    Achievement(
                        id = "ach-peninsular",
                        title = "Peninsular Explorer",
                        description = "Visit all states in Peninsular Malaysia.",
                        category = "Legendary",
                        requiredProgress = 11.0,
                        currentProgress = 7.0,
                        isUnlocked = false,
                        xpReward = 1500
                    ),
                    Achievement(
                        id = "ach-night-drive",
                        title = "Night Drive",
                        description = "Complete a drive past midnight under highway lights.",
                        category = "Atmosphere",
                        requiredProgress = 1.0,
                        currentProgress = 1.0,
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis() - 86400000L * 5,
                        xpReward = 100
                    ),
                    Achievement(
                        id = "ach-long-haul",
                        title = "Long Haul",
                        description = "Complete a single drive longer than 200 km.",
                        category = "Endurance",
                        requiredProgress = 200.0,
                        currentProgress = 78.5,
                        isUnlocked = false,
                        xpReward = 500
                    )
                )
            )
        }
    }

    private fun seedSocialAndFriends() {
        _friends.value = listOf(
            Friend(
                id = "f-1",
                name = "Faiz Hakimi",
                car = "Honda Civic FE RS",
                explorationPercent = 21.34,
                totalDistanceKm = 3410.0,
                roadsDiscovered = 485,
                statesVisited = 9,
                topSpeedKmh = 98.0,
                avatarInitial = "F",
                isOnline = true,
                status = "friend"
            ),
            Friend(
                id = "f-2",
                name = "Amirul Haziq",
                car = "Volkswagen Golf GTI Mk7.5",
                explorationPercent = 18.72,
                totalDistanceKm = 2950.0,
                roadsDiscovered = 398,
                statesVisited = 8,
                topSpeedKmh = 99.0,
                avatarInitial = "A",
                isOnline = false,
                status = "friend"
            ),
            Friend(
                id = "f-3",
                name = "Danial Farhan",
                car = "Mazda 3 Hatchback",
                explorationPercent = 15.60,
                totalDistanceKm = 2120.0,
                roadsDiscovered = 280,
                statesVisited = 6,
                topSpeedKmh = 95.0,
                avatarInitial = "D",
                isOnline = true,
                status = "friend"
            ),
            Friend(
                id = "f-4",
                name = "Syazwan Radzi",
                car = "Toyota GR Yaris",
                explorationPercent = 24.81,
                totalDistanceKm = 4150.0,
                roadsDiscovered = 560,
                statesVisited = 10,
                topSpeedKmh = 98.0,
                avatarInitial = "S",
                isOnline = false,
                status = "friend"
            )
        )

        _socialPosts.value = listOf(
            SocialPost(
                id = "post-1",
                authorName = "Faiz Hakimi",
                authorCar = "Honda Civic FE RS",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45, // 45m ago
                type = "exploration",
                title = "Karak Sprint & Bentong Coffee",
                description = "Unlocked the Genting Sempah pass this morning. Mist cleared up right after the tunnel! Discovered 4 new mountain segments.",
                statsSubtitle = "+2.4% Pahang Explored • 4 New Roads • 62 km",
                likesCount = 28,
                isLiked = true
            ),
            SocialPost(
                id = "post-2",
                authorName = "Syazwan Radzi",
                authorCar = "Toyota GR Yaris",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 180, // 3h ago
                type = "achievement",
                title = "Achievement Unlocked: Peninsular Legend",
                description = "Just stamped Johor Bahru! Visited 10 states across Peninsular Malaysia in the GR Yaris.",
                statsSubtitle = "Level 16 Vanguard • 560 Roads Discovered",
                likesCount = 45,
                isLiked = false
            ),
            SocialPost(
                id = "post-3",
                authorName = "Iqbal Arif (You)",
                authorCar = "BMW F30 330i M Sport",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 18,
                type = "drive",
                title = "Subang to Mid Valley Evening Cruise",
                description = "Cruising down Federal Highway after rain. Road surface was pristine.",
                statsSubtitle = "42.7 km • 58 min • 3 New Roads Discovered",
                likesCount = 14,
                isLiked = false
            )
        )
    }

    suspend fun checkPointAgainstRoads(currentLocation: GeoPoint): RoadSegment? {
        val allSegments = roadSegmentDao.getAllRoadSegments().first()
        for (segment in allSegments) {
            val polyline = GeoUtils.parsePoints(segment.pointsJson)
            if (GeoUtils.isPointNearPolyline(currentLocation, polyline, thresholdMeters = 150.0)) {
                if (!segment.isExplored) {
                    roadSegmentDao.markSegmentExplored(segment.id, System.currentTimeMillis())
                    addXp(10) // 10 XP for new road
                    return segment.copy(isExplored = true)
                }
            }
        }
        return null
    }

    suspend fun saveCompletedDrive(
        distanceKm: Double,
        durationSeconds: Long,
        avgSpeedKmh: Double,
        topSpeedKmh: Double,
        startName: String,
        endName: String,
        routePoints: List<GeoPoint>,
        newRoadsDiscovered: List<RoadSegment>,
        vehicleName: String
    ): DriveRecord {
        val xpFromDistance = (distanceKm * 2).toInt()
        val xpFromRoads = newRoadsDiscovered.size * 25
        val totalXp = xpFromDistance + xpFromRoads + 50 // +50 base for drive completion

        val explorationPercentGained = (newRoadsDiscovered.size.toDouble() / 25.0 * 0.15).coerceAtLeast(0.02)

        val record = DriveRecord(
            id = UUID.randomUUID().toString(),
            startTime = System.currentTimeMillis() - (durationSeconds * 1000),
            endTime = System.currentTimeMillis(),
            distanceKm = distanceKm,
            durationSeconds = durationSeconds,
            avgSpeedKmh = avgSpeedKmh,
            topSpeedKmh = topSpeedKmh,
            startLocationName = startName,
            endLocationName = endName,
            newRoadsDiscoveredCount = newRoadsDiscovered.size,
            newExplorationPercent = explorationPercentGained,
            vehicleName = vehicleName,
            routePointsJson = GeoUtils.pointsToString(routePoints),
            discoveredSegmentIdsJson = newRoadsDiscovered.joinToString(",") { it.id },
            xpEarned = totalXp
        )

        driveRecordDao.insertDriveRecord(record)
        addXp(totalXp)

        // Update user stats
        val current = _userProfile.value
        _userProfile.value = current.copy(
            totalDistanceKm = current.totalDistanceKm + distanceKm,
            totalDrives = current.totalDrives + 1,
            topSpeedKmh = maxOf(current.topSpeedKmh, topSpeedKmh),
            roadsDiscovered = current.roadsDiscovered + newRoadsDiscovered.size,
            totalExploredPercent = (current.totalExploredPercent + explorationPercentGained).coerceAtMost(100.0)
        )

        // Create social post
        val newPost = SocialPost(
            id = UUID.randomUUID().toString(),
            authorName = "Iqbal Arif (You)",
            authorCar = vehicleName,
            timestamp = System.currentTimeMillis(),
            type = "drive",
            title = "Completed Drive: $startName to $endName",
            description = "Logged ${(distanceKm * 10).toInt() / 10.0} km in ${(durationSeconds / 60)} mins. ${newRoadsDiscovered.size} new road segments unlocked!",
            statsSubtitle = "+${String.format("%.2f", explorationPercentGained)}% Malaysia • +$totalXp XP",
            likesCount = 1,
            isLiked = true
        )
        _socialPosts.value = listOf(newPost) + _socialPosts.value

        return record
    }

    private fun addXp(amount: Int) {
        val current = _userProfile.value
        var newXp = current.currentXp + amount
        var newLevel = current.level
        var nextLevelXp = current.nextLevelXp

        while (newXp >= nextLevelXp) {
            newXp -= nextLevelXp
            newLevel += 1
            nextLevelXp = (nextLevelXp * 1.25).toInt()
        }

        _userProfile.value = current.copy(
            currentXp = newXp,
            level = newLevel,
            nextLevelXp = nextLevelXp
        )
    }

    suspend fun setActiveVehicle(vehicleId: String) {
        vehicleDao.setActiveVehicle(vehicleId)
        val active = vehicleDao.getAllVehicles().first().find { it.id == vehicleId }
        if (active != null) {
            _userProfile.value = _userProfile.value.copy(
                activeCar = "${active.make} ${active.model}"
            )
        }
    }

    suspend fun addVehicle(vehicle: Vehicle) {
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicleId: String) {
        vehicleDao.deleteVehicle(vehicleId)
    }

    suspend fun deleteDriveHistory() {
        driveRecordDao.deleteAllDriveRecords()
        _userProfile.value = _userProfile.value.copy(
            totalDrives = 0,
            totalDistanceKm = 0.0
        )
    }

    fun togglePostLike(postId: String) {
        _socialPosts.value = _socialPosts.value.map { post ->
            if (post.id == postId) {
                if (post.isLiked) {
                    post.copy(isLiked = false, likesCount = post.likesCount - 1)
                } else {
                    post.copy(isLiked = true, likesCount = post.likesCount + 1)
                }
            } else post
        }
    }

    fun addFriend(name: String, car: String) {
        val newFriend = Friend(
            id = "f-${UUID.randomUUID()}",
            name = name,
            car = car,
            explorationPercent = 14.2,
            totalDistanceKm = 1200.0,
            roadsDiscovered = 180,
            statesVisited = 4,
            topSpeedKmh = 92.0,
            avatarInitial = name.firstOrNull()?.uppercase() ?: "F",
            isOnline = false,
            status = "friend"
        )
        _friends.value = _friends.value + newFriend
    }

    fun removeFriend(friendId: String) {
        _friends.value = _friends.value.filter { it.id != friendId }
    }

    fun updatePrivacySettings(
        privateProfile: Boolean? = null,
        hideExactRoutes: Boolean? = null,
        friendsOnlyLocation: Boolean? = null,
        liveLocationSharing: Boolean? = null
    ) {
        val current = _userProfile.value
        _userProfile.value = current.copy(
            privateProfile = privateProfile ?: current.privateProfile,
            hideExactRoutes = hideExactRoutes ?: current.hideExactRoutes,
            friendsOnlyLocation = friendsOnlyLocation ?: current.friendsOnlyLocation,
            liveLocationSharing = liveLocationSharing ?: current.liveLocationSharing
        )
    }
}
