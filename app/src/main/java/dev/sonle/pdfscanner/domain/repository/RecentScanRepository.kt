package dev.sonle.pdfscanner.domain.repository

import dev.sonle.pdfscanner.domain.model.RecentScan
import kotlinx.coroutines.flow.Flow

interface RecentScanRepository {
    fun observeRecentScans(): Flow<List<RecentScan>>
    suspend fun upsertRecentScan(recentScan: RecentScan)
    suspend fun findRecentScanById(id: Long): RecentScan?
    suspend fun deleteRecentScanById(id: Long): RecentScanDeleteResult
    suspend fun renameRecentScanById(id: Long, newFileName: String): RecentScanRenameResult
}

sealed class RecentScanDeleteResult {
    data object Success : RecentScanDeleteResult()
    data object NotFound : RecentScanDeleteResult()
    data class PartialFailure(
        val removedFromDatabase: Boolean,
        val fileDeleted: Boolean
    ) : RecentScanDeleteResult()
}
