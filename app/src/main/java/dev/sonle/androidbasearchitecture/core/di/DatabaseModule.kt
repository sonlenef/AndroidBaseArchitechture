package dev.sonle.androidbasearchitecture.core.di

import androidx.room.Room
import dev.sonle.androidbasearchitecture.core.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Module providing database-related dependencies
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "baseapp_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    single { get<AppDatabase>().userDao() }
}
