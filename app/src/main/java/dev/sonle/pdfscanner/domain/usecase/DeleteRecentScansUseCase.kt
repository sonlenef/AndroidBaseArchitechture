package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.BatchDeleteRecentScansResult
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository

class DeleteRecentScansUseCase(
    private val recentScanRepository: RecentScanRepository
) {
    suspend operator fun invoke(ids: Collection<Long>): BatchDeleteRecentScansResult {
        if (ids.isEmpty()) return BatchDeleteRecentScansResult()

        var deletedCount = 0
        var notFoundCount = 0
        var partialFailureCount = 0

        ids.distinct().forEach { id ->
            when (recentScanRepository.deleteRecentScanById(id)) {
                RecentScanDeleteResult.Success -> deletedCount++
                RecentScanDeleteResult.NotFound -> notFoundCount++
                is RecentScanDeleteResult.PartialFailure -> partialFailureCount++
            }
        }

        return BatchDeleteRecentScansResult(
            deletedCount = deletedCount,
            notFoundCount = notFoundCount,
            partialFailureCount = partialFailureCount
        )
    }
}
