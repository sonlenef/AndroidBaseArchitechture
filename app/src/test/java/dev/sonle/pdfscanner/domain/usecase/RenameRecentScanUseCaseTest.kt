package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanRenameResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RenameRecentScanUseCaseTest {

    private val repository: RecentScanRepository = mockk()
    private val useCase = RenameRecentScanUseCase(repository)

    @Test
    fun `invoke should delegate to repository`() = runTest {
        val scan = RecentScan(1, "New.pdf", "/new.pdf", 1, 100, 1)
        coEvery { repository.renameRecentScanById(1, "New") } returns RecentScanRenameResult.Success(scan)

        val result = useCase(1, "New")

        assertTrue(result is RecentScanRenameResult.Success)
        coVerify(exactly = 1) { repository.renameRecentScanById(1, "New") }
    }
}
