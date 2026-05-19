package dev.sonle.pdfscanner.presentation.features.main.home

import app.cash.turbine.test
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.usecase.DeleteRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveRecentScansUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var observeRecentScansUseCase: ObserveRecentScansUseCase
    private lateinit var deleteRecentScanUseCase: DeleteRecentScanUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        observeRecentScansUseCase = mockk()
        deleteRecentScanUseCase = mockk()
        coEvery { deleteRecentScanUseCase(any()) } returns RecentScanDeleteResult.Success
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should emit recent scans when repository emits data`() = runTest {
        val recentScans = listOf(
            RecentScan(
                id = 1,
                fileName = "Scan_1.pdf",
                filePath = "/tmp/Scan_1.pdf",
                pageCount = 2,
                fileSizeBytes = 1200,
                savedAt = 1000
            )
        )
        val source = MutableStateFlow(recentScans)
        every { observeRecentScansUseCase() } returns source

        val viewModel = HomeViewModel(observeRecentScansUseCase, deleteRecentScanUseCase)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertEquals(recentScans, state.recentScans)
        }
    }

    @Test
    fun `deleteRecentScan should call delete use case`() = runTest {
        every { observeRecentScansUseCase() } returns MutableStateFlow(emptyList())
        val viewModel = HomeViewModel(observeRecentScansUseCase, deleteRecentScanUseCase)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteRecentScan(42)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { deleteRecentScanUseCase(42) }
    }
}
