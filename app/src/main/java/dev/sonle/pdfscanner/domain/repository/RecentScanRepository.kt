package dev.sonle.pdfscanner.domain.repository

import dev.sonle.pdfscanner.domain.model.RecentScan
import kotlinx.coroutines.flow.Flow

interface RecentScanRepository {
    fun observeRecentScans(): Flow<List<RecentScan>>
    suspend fun upsertRecentScan(recentScan: RecentScan)
    suspend fun deleteRecentScanById(id: Long)
}
