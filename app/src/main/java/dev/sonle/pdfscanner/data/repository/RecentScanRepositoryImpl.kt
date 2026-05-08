package dev.sonle.pdfscanner.data.repository

import dev.sonle.pdfscanner.data.local.RecentScanDao
import dev.sonle.pdfscanner.data.model.RecentScanEntity
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecentScanRepositoryImpl(
    private val recentScanDao: RecentScanDao
) : RecentScanRepository {

    override fun observeRecentScans(): Flow<List<RecentScan>> {
        return recentScanDao.observeRecentScans().map { entities ->
            entities.map { entity ->
                RecentScan(
                    id = entity.id,
                    fileName = entity.fileName,
                    filePath = entity.filePath,
                    pageCount = entity.pageCount,
                    fileSizeBytes = entity.fileSizeBytes,
                    savedAt = entity.savedAt
                )
            }
        }
    }

    override suspend fun upsertRecentScan(recentScan: RecentScan) {
        recentScanDao.upsertRecentScan(
            RecentScanEntity(
                id = recentScan.id,
                fileName = recentScan.fileName,
                filePath = recentScan.filePath,
                pageCount = recentScan.pageCount,
                fileSizeBytes = recentScan.fileSizeBytes,
                savedAt = recentScan.savedAt
            )
        )
    }

    override suspend fun deleteRecentScanById(id: Long) {
        recentScanDao.deleteRecentScanById(id)
    }
}
