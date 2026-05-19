package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository

class DeleteRecentScanUseCase(
    private val recentScanRepository: RecentScanRepository
) {
    suspend operator fun invoke(id: Long): RecentScanDeleteResult {
        return recentScanRepository.deleteRecentScanById(id)
    }
}
