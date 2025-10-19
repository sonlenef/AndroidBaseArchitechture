package dev.sonle.androidbasearchitechture.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import dev.sonle.androidbasearchitechture.core.config.EnvironmentConfig
import dev.sonle.androidbasearchitechture.data.local.UserDao
import dev.sonle.androidbasearchitechture.data.model.UserEntity

/**
 * Room database for the application
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    
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
