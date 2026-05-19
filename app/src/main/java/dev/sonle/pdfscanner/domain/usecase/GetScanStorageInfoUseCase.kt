package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.StorageInfo
import dev.sonle.pdfscanner.domain.repository.ScanStorageRepository

class GetScanStorageInfoUseCase(
    private val scanStorageRepository: ScanStorageRepository
) {
    suspend operator fun invoke(): StorageInfo = scanStorageRepository.getStorageInfo()
}
