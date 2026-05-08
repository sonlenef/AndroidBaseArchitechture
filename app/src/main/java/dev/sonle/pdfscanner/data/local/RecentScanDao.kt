package dev.sonle.pdfscanner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.sonle.pdfscanner.data.model.RecentScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentScanDao {

    @Query("SELECT * FROM recent_scans ORDER BY savedAt DESC")
    fun observeRecentScans(): Flow<List<RecentScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentScan(recentScanEntity: RecentScanEntity)

    @Query("DELETE FROM recent_scans WHERE id = :id")
    suspend fun deleteRecentScanById(id: Long)

    @Query("DELETE FROM recent_scans WHERE filePath = :filePath")
    suspend fun deleteRecentScanByPath(filePath: String)
}
