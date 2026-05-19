package dev.sonle.pdfscanner.data.repository

import android.content.Context
import android.os.Environment
import dev.sonle.pdfscanner.data.local.RecentScanDao
import dev.sonle.pdfscanner.domain.model.StorageInfo
import dev.sonle.pdfscanner.domain.repository.ClearAllScanDataResult
import dev.sonle.pdfscanner.domain.repository.ScanStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class ScanStorageRepositoryImpl(
    private val context: Context,
    private val recentScanDao: RecentScanDao
) : ScanStorageRepository {

    override suspend fun getStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
        val scans = recentScanDao.getAllRecentScans()
        var totalBytes = 0L
        scans.forEach { entity ->
            val file = File(entity.filePath)
            if (file.exists()) {
                totalBytes += file.length()
            }
        }
        StorageInfo(
            scanFileCount = scans.size,
            totalBytes = totalBytes
        )
    }

    override suspend fun clearAllScanData(): ClearAllScanDataResult = withContext(Dispatchers.IO) {
        try {
            val scans = recentScanDao.getAllRecentScans()
            var deletedFiles = 0
            scans.forEach { entity ->
                val file = File(entity.filePath)
                if (file.exists() && file.delete()) {
                    deletedFiles++
                }
            }
            recentScanDao.deleteAllRecentScans()
            clearOrphanedPdfsInDocumentsDir()
            ClearAllScanDataResult.Success(deletedFiles)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear scan storage")
            ClearAllScanDataResult.Failure(e.message)
        }
    }

    private fun clearOrphanedPdfsInDocumentsDir() {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return
        directory.listFiles()
            ?.filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
            ?.forEach { file ->
                runCatching { file.delete() }
                    .onFailure { Timber.w(it, "Failed to delete orphan pdf: ${file.name}") }
            }
    }
}
