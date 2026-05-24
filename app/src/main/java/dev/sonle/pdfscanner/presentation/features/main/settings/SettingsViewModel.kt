package dev.sonle.pdfscanner.presentation.features.main.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.model.DocumentFilterPreset
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality
import dev.sonle.pdfscanner.domain.model.ScanPageLayout
import dev.sonle.pdfscanner.domain.model.ScannerCaptureMode
import dev.sonle.pdfscanner.domain.model.ThemeMode
import dev.sonle.pdfscanner.domain.repository.ClearAllScanDataResult
import dev.sonle.pdfscanner.domain.usecase.ClearAllScanDataUseCase
import dev.sonle.pdfscanner.domain.usecase.GetScanStorageInfoUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveAppSettingsUseCase
import dev.sonle.pdfscanner.domain.usecase.UpdateAppSettingsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class SettingsUiState(
    val settings: AppSettings = AppSettings.Default,
    val scanFileCount: Int = 0,
    val storageBytes: Long = 0L,
    val isLoadingStorage: Boolean = true,
    val isClearingStorage: Boolean = false,
    val activeSheet: SettingsSheet? = null,
    val showAboutDialog: Boolean = false,
    val showClearDataDialog: Boolean = false
)

sealed interface SettingsSheet {
    data object CaptureMode : SettingsSheet
    data object PageLayout : SettingsSheet
    data object DefaultFilter : SettingsSheet
    data object Theme : SettingsSheet
    data object PdfQuality : SettingsSheet
}

sealed interface SettingsUiEffect {
    data class ShowMessage(@StringRes val messageResId: Int) : SettingsUiEffect
    data object OpenPlayStore : SettingsUiEffect
    data object OpenPrivacyPolicy : SettingsUiEffect
}

class SettingsViewModel(
    observeAppSettingsUseCase: ObserveAppSettingsUseCase,
    private val updateAppSettingsUseCase: UpdateAppSettingsUseCase,
    private val getScanStorageInfoUseCase: GetScanStorageInfoUseCase,
    private val clearAllScanDataUseCase: ClearAllScanDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val uiEffects = _uiEffects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeAppSettingsUseCase().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        refreshStorageInfo()
    }

    fun refreshStorageInfo() {
        viewModelScope.launch {
            runCatching { getScanStorageInfoUseCase() }
                .onSuccess { info ->
                    _uiState.update {
                        it.copy(
                            scanFileCount = info.scanFileCount,
                            storageBytes = info.totalBytes,
                            isLoadingStorage = false
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load storage info")
                }
        }
    }

    fun onDynamicColorChanged(enabled: Boolean) {
        update { it.copy(useDynamicColor = enabled) }
    }

    fun onCaptureModeSelected(mode: ScannerCaptureMode) {
        update { it.copy(defaultCaptureMode = mode) }
        dismissSheet()
    }

    fun onPageLayoutSelected(layout: ScanPageLayout) {
        update { it.copy(defaultPageLayout = layout) }
        dismissSheet()
    }

    fun onFilterSelected(filter: DocumentFilterPreset) {
        update { it.copy(defaultFilter = filter) }
        dismissSheet()
    }

    fun onThemeSelected(theme: ThemeMode) {
        update { it.copy(themeMode = theme) }
        dismissSheet()
    }

    fun onPdfQualitySelected(quality: PdfOutputQuality) {
        update { it.copy(pdfOutputQuality = quality) }
        dismissSheet()
    }

    fun openSheet(sheet: SettingsSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(activeSheet = null) }
    }

    fun showAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun dismissAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    fun showClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun dismissClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    fun confirmClearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingStorage = true, showClearDataDialog = false) }
            when (val result = clearAllScanDataUseCase()) {
                is ClearAllScanDataResult.Success -> {
                    _uiEffects.send(SettingsUiEffect.ShowMessage(R.string.settings_clear_data_success))
                    refreshStorageInfo()
                }
                is ClearAllScanDataResult.Failure -> {
                    _uiEffects.send(SettingsUiEffect.ShowMessage(R.string.settings_clear_data_failed))
                }
            }
            _uiState.update { it.copy(isClearingStorage = false) }
        }
    }

    fun onRateAppClick() {
        viewModelScope.launch {
            _uiEffects.send(SettingsUiEffect.OpenPlayStore)
        }
    }

    fun onPrivacyPolicyClick() {
        viewModelScope.launch {
            _uiEffects.send(SettingsUiEffect.OpenPrivacyPolicy)
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val current = _uiState.value.settings
        val updated = transform(current)
        if (updated == current) return
        viewModelScope.launch {
            runCatching { updateAppSettingsUseCase(updated) }
                .onFailure { error ->
                    Timber.e(error, "Failed to update settings")
                    _uiEffects.send(SettingsUiEffect.ShowMessage(R.string.settings_save_failed))
                }
        }
    }

}
