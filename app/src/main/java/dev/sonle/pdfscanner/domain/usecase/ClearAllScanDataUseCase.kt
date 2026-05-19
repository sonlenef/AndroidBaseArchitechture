package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.repository.ClearAllScanDataResult
import dev.sonle.pdfscanner.domain.repository.ScanStorageRepository

class ClearAllScanDataUseCase(
    private val scanStorageRepository: ScanStorageRepository
) {
    suspend operator fun invoke(): ClearAllScanDataResult =
        scanStorageRepository.clearAllScanData()
}
