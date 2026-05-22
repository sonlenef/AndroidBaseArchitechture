package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.repository.RecentScanRenameResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository

class RenameRecentScanUseCase(
    private val recentScanRepository: RecentScanRepository
) {
    suspend operator fun invoke(id: Long, newFileName: String): RecentScanRenameResult =
        recentScanRepository.renameRecentScanById(id, newFileName)
}
