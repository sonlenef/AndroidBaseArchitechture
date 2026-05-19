package dev.sonle.pdfscanner.domain.repository

import dev.sonle.pdfscanner.domain.model.StorageInfo

interface ScanStorageRepository {
    suspend fun getStorageInfo(): StorageInfo
    suspend fun clearAllScanData(): ClearAllScanDataResult
}

sealed class ClearAllScanDataResult {
    data class Success(val deletedFileCount: Int) : ClearAllScanDataResult()
    data class Failure(val message: String?) : ClearAllScanDataResult()
}
