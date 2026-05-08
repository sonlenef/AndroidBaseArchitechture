package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import kotlinx.coroutines.flow.Flow

class ObserveRecentScansUseCase(
    private val recentScanRepository: RecentScanRepository
) {
    operator fun invoke(): Flow<List<RecentScan>> = recentScanRepository.observeRecentScans()
}
