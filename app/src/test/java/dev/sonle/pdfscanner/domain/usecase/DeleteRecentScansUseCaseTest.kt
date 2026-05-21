package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteRecentScansUseCaseTest {

    private val repository: RecentScanRepository = mockk()

    @Test
    fun `invoke should return empty result when ids are empty`() = runTest {
        val useCase = DeleteRecentScansUseCase(repository)

        val result = useCase(emptyList())

        assertEquals(0, result.deletedCount)
        coVerify(exactly = 0) { repository.deleteRecentScanById(any()) }
    }

    @Test
    fun `invoke should aggregate delete outcomes for distinct ids`() = runTest {
        coEvery { repository.deleteRecentScanById(1) } returns RecentScanDeleteResult.Success
        coEvery { repository.deleteRecentScanById(2) } returns RecentScanDeleteResult.NotFound
        coEvery { repository.deleteRecentScanById(3) } returns RecentScanDeleteResult.PartialFailure(
            removedFromDatabase = true,
            fileDeleted = false
        )
        val useCase = DeleteRecentScansUseCase(repository)

        val result = useCase(listOf(1, 2, 2, 3))

        assertEquals(1, result.deletedCount)
        assertEquals(1, result.notFoundCount)
        assertEquals(1, result.partialFailureCount)
        coVerify(exactly = 1) { repository.deleteRecentScanById(1) }
        coVerify(exactly = 1) { repository.deleteRecentScanById(2) }
        coVerify(exactly = 1) { repository.deleteRecentScanById(3) }
    }
}
