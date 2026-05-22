package dev.sonle.pdfscanner.data.repository

import android.content.Context
import dev.sonle.pdfscanner.data.local.RecentScanDao
import dev.sonle.pdfscanner.data.model.RecentScanEntity
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRenameResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import dev.sonle.pdfscanner.domain.util.DocumentFileNameNormalizer
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

    override suspend fun renameRecentScanById(id: Long, newFileName: String): RecentScanRenameResult {
        val normalizedName = DocumentFileNameNormalizer.normalize(newFileName)
            ?: return RecentScanRenameResult.InvalidName

        val entity = recentScanDao.getRecentScanById(id)
            ?: return RecentScanRenameResult.NotFound

        if (entity.fileName.equals(normalizedName, ignoreCase = true)) {
            return RecentScanRenameResult.Success(entity.toDomain())
        }

        val currentFile = File(entity.filePath)
        val parentDir = currentFile.parentFile
            ?: return RecentScanRenameResult.Failed

        val targetFile = File(parentDir, normalizedName)
        if (targetFile.exists() && targetFile.absolutePath != currentFile.absolutePath) {
            return RecentScanRenameResult.NameAlreadyExists
        }

        if (currentFile.exists()) {
            val renamed = currentFile.renameTo(targetFile)
            if (!renamed) {
                Timber.e("Failed to rename PDF file: %s -> %s", currentFile.absolutePath, targetFile.absolutePath)
                return RecentScanRenameResult.Failed
            }
        } else {
            Timber.w("PDF file missing during rename: %s", entity.filePath)
        }

        val updatedEntity = entity.copy(
            fileName = normalizedName,
            filePath = targetFile.absolutePath,
            fileSizeBytes = if (targetFile.exists()) targetFile.length() else entity.fileSizeBytes
        )
        recentScanDao.upsertRecentScan(updatedEntity)
        return RecentScanRenameResult.Success(updatedEntity.toDomain())
    }

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
