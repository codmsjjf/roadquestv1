package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "road_segments")
data class RoadSegment(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val state: String,
    val city: String,
    val lengthKm: Double,
    val pointsJson: String, // Comma-separated or JSON list of lat,lon
    val isExplored: Boolean = false,
    val exploredAt: Long? = null,
    val timesDriven: Int = 0
)

@Entity(tableName = "drive_records")
data class DriveRecord(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long,
    val distanceKm: Double,
    val durationSeconds: Long,
    val avgSpeedKmh: Double,
    val topSpeedKmh: Double,
    val startLocationName: String,
    val endLocationName: String,
    val newRoadsDiscoveredCount: Int,
    val newExplorationPercent: Double,
    val vehicleName: String,
    val routePointsJson: String,
    val discoveredSegmentIdsJson: String,
    val xpEarned: Int
)

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val variant: String,
    val color: String,
    val nickname: String,
    val totalDistanceKm: Double = 0.0,
    val totalDrives: Int = 0,
    val topSpeedKmh: Double = 0.0,
    val isActive: Boolean = false
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val requiredProgress: Double,
    val currentProgress: Double = 0.0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val xpReward: Int = 100
)

data class Friend(
    val id: String,
    val name: String,
    val car: String,
    val explorationPercent: Double,
    val totalDistanceKm: Double,
    val roadsDiscovered: Int,
    val statesVisited: Int,
    val topSpeedKmh: Double,
    val avatarInitial: String,
    val isOnline: Boolean,
    val status: String // "friend" or "request"
)

data class SocialPost(
    val id: String,
    val authorName: String,
    val authorCar: String,
    val timestamp: Long,
    val type: String, // "drive", "discovery", "achievement", "vehicle"
    val title: String,
    val description: String,
    val statsSubtitle: String,
    var likesCount: Int = 0,
    var isLiked: Boolean = false
)

data class UserProfile(
    val id: String = "user_default",
    val username: String = "Iqbal Arif",
    val callsign: String = "Klang Valley Explorer",
    val activeCar: String = "BMW F30 330i M Sport",
    val totalExploredPercent: Double = 12.84,
    val totalDistanceKm: Double = 2481.5,
    val totalDrives: Int = 86,
    val topSpeedKmh: Double = 97.0,
    val roadsDiscovered: Int = 342,
    val statesVisited: Int = 7,
    val citiesVisited: Int = 16,
    val level: Int = 12,
    val currentXp: Int = 2840,
    val nextLevelXp: Int = 4000,
    val privateProfile: Boolean = false,
    val hideExactRoutes: Boolean = false,
    val friendsOnlyLocation: Boolean = true,
    val liveLocationSharing: Boolean = false
)

data class StateProgress(
    val stateName: String,
    val exploredPercent: Double,
    val totalRoadsCount: Int,
    val exploredRoadsCount: Int,
    val totalDistanceKm: Double,
    val centerLat: Double,
    val centerLng: Double
)
