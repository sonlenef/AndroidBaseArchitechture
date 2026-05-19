package dev.sonle.pdfscanner.core.di

import androidx.room.Room
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import dev.sonle.pdfscanner.core.database.AppDatabase
import dev.sonle.pdfscanner.core.database.AppDatabaseMigrations
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            EnvironmentConfig.databaseName
        )
            .addMigrations(AppDatabaseMigrations.MIGRATION_2_3)
            .build()
    }

    single { get<AppDatabase>().recentScanDao() }
}
