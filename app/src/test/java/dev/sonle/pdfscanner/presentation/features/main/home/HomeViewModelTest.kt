package dev.sonle.pdfscanner.presentation.features.main.home

import app.cash.turbine.test
import dev.sonle.pdfscanner.core.analytics.AnalyticsManager
import dev.sonle.pdfscanner.core.crashlytics.CrashlyticsManager
import dev.sonle.pdfscanner.core.locale.AppLocaleApplicator
import dev.sonle.pdfscanner.domain.model.AppLanguage
import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.model.BatchDeleteRecentScansResult
import dev.sonle.pdfscanner.domain.usecase.ObserveAppSettingsUseCase
import dev.sonle.pdfscanner.domain.usecase.UpdateAppSettingsUseCase
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.usecase.DeleteRecentScanUseCase
import dev.sonle.pdfscanner.domain.repository.RecentScanRenameResult
import dev.sonle.pdfscanner.domain.usecase.DeleteRecentScansUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveRecentScansUseCase
import dev.sonle.pdfscanner.domain.usecase.RenameRecentScanUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var observeRecentScansUseCase: ObserveRecentScansUseCase
    private lateinit var observeAppSettingsUseCase: ObserveAppSettingsUseCase
    private lateinit var updateAppSettingsUseCase: UpdateAppSettingsUseCase
    private lateinit var appLocaleApplicator: AppLocaleApplicator
    private val settingsFlow = MutableStateFlow(AppSettings.Default)
    private lateinit var deleteRecentScanUseCase: DeleteRecentScanUseCase
    private lateinit var deleteRecentScansUseCase: DeleteRecentScansUseCase
    private lateinit var renameRecentScanUseCase: RenameRecentScanUseCase
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var crashlyticsManager: CrashlyticsManager

    private val sampleScans = listOf(
        RecentScan(1, "A.pdf", "/a.pdf", 1, 100, 1),
        RecentScan(2, "B.pdf", "/b.pdf", 2, 200, 2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        observeRecentScansUseCase = mockk()
        observeAppSettingsUseCase = mockk()
        updateAppSettingsUseCase = mockk(relaxed = true)
        appLocaleApplicator = mockk(relaxed = true)
        every { observeAppSettingsUseCase() } returns settingsFlow
        deleteRecentScanUseCase = mockk()
        deleteRecentScansUseCase = mockk()
        renameRecentScanUseCase = mockk()
        analyticsManager = mockk(relaxed = true)
        crashlyticsManager = mockk(relaxed = true)
        every { observeRecentScansUseCase() } returns MutableStateFlow(sampleScans)
        coEvery { renameRecentScanUseCase(any(), any()) } returns RecentScanRenameResult.Success(sampleScans[0])
        coEvery { deleteRecentScanUseCase(any()) } returns RecentScanDeleteResult.Success
        coEvery { deleteRecentScansUseCase(any()) } returns BatchDeleteRecentScansResult(deletedCount = 2)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            observeRecentScansUseCase,
            observeAppSettingsUseCase,
            updateAppSettingsUseCase,
            deleteRecentScanUseCase,
            deleteRecentScansUseCase,
            renameRecentScanUseCase,
            appLocaleApplicator,
            analyticsManager,
            crashlyticsManager
        )

    @Test
    fun `toggleSelectionMode should enable and clear selection`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSelectionMode()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSelectionMode)
        assertEquals(0, viewModel.uiState.value.selectedCount)

        viewModel.toggleScanSelection(1)
        viewModel.toggleSelectionMode()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertEquals(0, viewModel.uiState.value.selectedCount)
    }

    @Test
    fun `toggleSelectAll should select and deselect every scan`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSelectionMode()
        viewModel.toggleSelectAll()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAllSelected)
        assertEquals(2, viewModel.uiState.value.selectedCount)

        viewModel.toggleSelectAll()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.selectedCount)
    }

    @Test
    fun `enterSelectionWithScan should enable selection mode with one item`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionWithScan(2)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSelectionMode)
        assertEquals(setOf(2L), viewModel.uiState.value.selectedScanIds)
    }

    @Test
    fun `deleteSelectedScans should call batch delete and emit effect`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSelectionMode()
        viewModel.toggleScanSelection(1)
        viewModel.toggleScanSelection(2)

        viewModel.uiEffects.test {
            viewModel.deleteSelectedScans()
            dispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.DocumentsDeleted)
            assertEquals(2, (effect as HomeUiEffect.DocumentsDeleted).count)
            coVerify(exactly = 1) { deleteRecentScansUseCase(setOf(1L, 2L)) }
        }
    }

    @Test
    fun `deleteRecentScan should call single delete use case`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteRecentScan(42)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { deleteRecentScanUseCase(42) }
    }

    @Test
    fun `updateSearchQuery should filter displayed scans`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSearchQuery("A")
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSearching)
        assertEquals(1, state.displayedScans.size)
        assertEquals("A.pdf", state.displayedScans.first().fileName)
    }

    @Test
    fun `clearSearch should restore full list`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSearchQuery("A")
        viewModel.clearSearch()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearching)
        assertEquals(2, state.displayedScans.size)
    }

    @Test
    fun `renameRecentScan should emit success message when rename succeeds`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffects.test {
            viewModel.renameRecentScan(1, "Renamed")
            dispatcher.scheduler.advanceUntilIdle()

            assertTrue(awaitItem() is HomeUiEffect.ShowMessage)
            coVerify(exactly = 1) { renameRecentScanUseCase(1, "Renamed") }
        }
    }

    @Test
    fun `onLanguageSelected should persist settings and apply locale`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onLanguageSelected(AppLanguage.VIETNAMESE)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            updateAppSettingsUseCase(AppSettings.Default.copy(appLanguage = AppLanguage.VIETNAMESE))
        }
        coVerify(exactly = 1) { appLocaleApplicator.applyInApp(AppLanguage.VIETNAMESE) }
        assertEquals(AppLanguage.VIETNAMESE, viewModel.uiState.value.appLanguage)
    }

    @Test
    fun `toggleSelectAll should only affect visible scans when searching`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSearchQuery("A")
        viewModel.toggleSelectionMode()
        viewModel.toggleSelectAll()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(setOf(1L), viewModel.uiState.value.selectedScanIds)
        assertTrue(viewModel.uiState.value.isAllSelected)
    }
}
