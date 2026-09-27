package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Achievement
import com.example.data.model.DriveRecord
import com.example.data.model.RoadSegment
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface RoadSegmentDao {
    @Query("SELECT * FROM road_segments")
    fun getAllRoadSegments(): Flow<List<RoadSegment>>

    @Query("SELECT * FROM road_segments WHERE state = :state")
    fun getRoadSegmentsByState(state: String): Flow<List<RoadSegment>>

    @Query("SELECT * FROM road_segments WHERE isExplored = 1")
    fun getExploredRoadSegments(): Flow<List<RoadSegment>>

    @Query("SELECT COUNT(*) FROM road_segments")
    suspend fun getTotalRoadCount(): Int

    @Query("SELECT COUNT(*) FROM road_segments WHERE isExplored = 1")
    suspend fun getExploredRoadCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadSegments(segments: List<RoadSegment>)

    @Update
    suspend fun updateRoadSegment(segment: RoadSegment)

    @Query("UPDATE road_segments SET isExplored = 1, exploredAt = :exploredAt, timesDriven = timesDriven + 1 WHERE id = :segmentId")
    suspend fun markSegmentExplored(segmentId: String, exploredAt: Long)
}

@Dao
interface DriveRecordDao {
    @Query("SELECT * FROM drive_records ORDER BY startTime DESC")
    fun getAllDriveRecords(): Flow<List<DriveRecord>>

    @Query("SELECT * FROM drive_records WHERE id = :id LIMIT 1")
    suspend fun getDriveRecordById(id: String): DriveRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriveRecord(driveRecord: DriveRecord)

    @Query("DELETE FROM drive_records WHERE id = :id")
    suspend fun deleteDriveRecord(id: String)

    @Query("DELETE FROM drive_records")
    suspend fun deleteAllDriveRecords()
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE isActive = 1 LIMIT 1")
    fun getActiveVehicle(): Flow<Vehicle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Query("UPDATE vehicles SET isActive = CASE WHEN id = :vehicleId THEN 1 ELSE 0 END")
    suspend fun setActiveVehicle(vehicleId: String)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicle(id: String)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)
}
