package dev.sonle.pdfscanner.presentation.features.main.settings

import app.cash.turbine.test
import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.model.ScannerCaptureMode
import dev.sonle.pdfscanner.domain.model.StorageInfo
import dev.sonle.pdfscanner.domain.model.ThemeMode
import dev.sonle.pdfscanner.domain.repository.ClearAllScanDataResult
import dev.sonle.pdfscanner.domain.usecase.ClearAllScanDataUseCase
import dev.sonle.pdfscanner.domain.usecase.GetScanStorageInfoUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveAppSettingsUseCase
import dev.sonle.pdfscanner.domain.usecase.UpdateAppSettingsUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var observeAppSettingsUseCase: ObserveAppSettingsUseCase
    private lateinit var updateAppSettingsUseCase: UpdateAppSettingsUseCase
    private lateinit var getScanStorageInfoUseCase: GetScanStorageInfoUseCase
    private lateinit var clearAllScanDataUseCase: ClearAllScanDataUseCase
    private val settingsFlow = MutableStateFlow(AppSettings.Default)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        observeAppSettingsUseCase = mockk()
        updateAppSettingsUseCase = mockk(relaxed = true)
        getScanStorageInfoUseCase = mockk()
        clearAllScanDataUseCase = mockk()
        every { observeAppSettingsUseCase() } returns settingsFlow
        coEvery { getScanStorageInfoUseCase() } returns StorageInfo(scanFileCount = 2, totalBytes = 2048)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should reflect settings from repository`() = runTest {
        settingsFlow.value = AppSettings.Default.copy(themeMode = ThemeMode.DARK)
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(ThemeMode.DARK, state.settings.themeMode)
            assertEquals(2, state.scanFileCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onThemeSelected should persist updated settings`() = runTest {
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.LIGHT)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify {
            updateAppSettingsUseCase(
                AppSettings.Default.copy(themeMode = ThemeMode.LIGHT)
            )
        }
    }

    @Test
    fun `confirmClearAllData should refresh storage on success`() = runTest {
        coEvery { clearAllScanDataUseCase() } returns ClearAllScanDataResult.Success(3)
        coEvery { getScanStorageInfoUseCase() } returnsMany listOf(
            StorageInfo(scanFileCount = 3, totalBytes = 4096),
            StorageInfo(scanFileCount = 0, totalBytes = 0)
        )
        val viewModel = createViewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.confirmClearAllData()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.scanFileCount)
        assertTrue(viewModel.uiState.value.showClearDataDialog.not())
    }

    private fun createViewModel() = SettingsViewModel(
        observeAppSettingsUseCase,
        updateAppSettingsUseCase,
        getScanStorageInfoUseCase,
        clearAllScanDataUseCase
    )
}
