package dev.sonle.pdfscanner.data.repository

import android.content.Context
import dev.sonle.pdfscanner.data.local.RecentScanDao
import dev.sonle.pdfscanner.data.model.RecentScanEntity
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File

class RecentScanRepositoryImpl(
    private val recentScanDao: RecentScanDao,
    private val context: Context
) : RecentScanRepository {

    override fun observeRecentScans(): Flow<List<RecentScan>> {
        return recentScanDao.observeRecentScans().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertRecentScan(recentScan: RecentScan) {
        recentScanDao.upsertRecentScan(recentScan.toEntity())
        val excess = recentScanDao.countRecentScans() - MAX_RECENT_SCANS
        if (excess > 0) {
            val oldest = recentScanDao.getOldestScans(excess)
            oldest.forEach { entity ->
                deletePdfFile(entity.filePath)
                recentScanDao.deleteRecentScanById(entity.id)
            }
        }
    }

    override suspend fun findRecentScanById(id: Long): RecentScan? =
        recentScanDao.getRecentScanById(id)?.toDomain()

    override suspend fun deleteRecentScanById(id: Long): RecentScanDeleteResult {
        val entity = recentScanDao.getRecentScanById(id)
            ?: return RecentScanDeleteResult.NotFound

        val fileDeleted = deletePdfFile(entity.filePath)
        recentScanDao.deleteRecentScanById(id)

        return if (fileDeleted) {
            RecentScanDeleteResult.Success
        } else {
            RecentScanDeleteResult.PartialFailure(
                removedFromDatabase = true,
                fileDeleted = false
            )
        }
    }

    private fun deletePdfFile(filePath: String): Boolean {
        return runCatching {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.w("PDF file missing during delete: %s", filePath)
                true
            } else {
                file.delete()
            }
        }.getOrElse { error ->
            Timber.e(error, "Failed to delete PDF file: %s", filePath)
            false
        }
    }

    private fun RecentScanEntity.toDomain() = RecentScan(
        id = id,
        fileName = fileName,
        filePath = filePath,
        pageCount = pageCount,
        fileSizeBytes = fileSizeBytes,
        savedAt = savedAt
    )

    private fun RecentScan.toEntity() = RecentScanEntity(
        id = id,
        fileName = fileName,
        filePath = filePath,
        pageCount = pageCount,
        fileSizeBytes = fileSizeBytes,
        savedAt = savedAt
    )

    companion object {
        const val MAX_RECENT_SCANS = 50
    }
}
