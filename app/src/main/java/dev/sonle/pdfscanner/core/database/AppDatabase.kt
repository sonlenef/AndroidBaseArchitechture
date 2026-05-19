package dev.sonle.pdfscanner.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.sonle.pdfscanner.data.local.RecentScanDao
import dev.sonle.pdfscanner.data.model.RecentScanEntity

@Database(
    entities = [RecentScanEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentScanDao(): RecentScanDao
}
