package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository

class AddRecentScanUseCase(
    private val recentScanRepository: RecentScanRepository
) {
    suspend operator fun invoke(recentScan: RecentScan) {
        recentScanRepository.upsertRecentScan(recentScan)
    }
}
