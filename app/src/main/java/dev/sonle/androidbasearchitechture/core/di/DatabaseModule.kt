package dev.sonle.androidbasearchitechture.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sonle.androidbasearchitechture.core.database.AppDatabase
import dev.sonle.androidbasearchitechture.data.local.UserDao
import javax.inject.Singleton

/**
 * Module providing database-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "baseapp_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
