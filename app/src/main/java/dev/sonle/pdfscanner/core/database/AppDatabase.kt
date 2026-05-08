package dev.sonle.pdfscanner.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import dev.sonle.pdfscanner.data.local.RecentScanDao
import dev.sonle.pdfscanner.data.local.UserDao
import dev.sonle.pdfscanner.data.model.RecentScanEntity
import dev.sonle.pdfscanner.data.model.UserEntity

/**
 * Room database for the application
 */
@Database(
    entities = [UserEntity::class, RecentScanEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun recentScanDao(): RecentScanDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    EnvironmentConfig.databaseName
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
