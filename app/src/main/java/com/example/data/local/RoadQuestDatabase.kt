package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Achievement
import com.example.data.model.DriveRecord
import com.example.data.model.RoadSegment
import com.example.data.model.Vehicle

@Database(
    entities = [
        RoadSegment::class,
        DriveRecord::class,
        Vehicle::class,
        Achievement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RoadQuestDatabase : RoomDatabase() {
    abstract fun roadSegmentDao(): RoadSegmentDao
    abstract fun driveRecordDao(): DriveRecordDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: RoadQuestDatabase? = null

        fun getDatabase(context: Context): RoadQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoadQuestDatabase::class.java,
                    "roadquest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
