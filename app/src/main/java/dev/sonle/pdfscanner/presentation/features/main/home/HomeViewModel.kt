package dev.sonle.pdfscanner.presentation.features.main.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.usecase.DeleteRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveRecentScansUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentScans: List<RecentScan> = emptyList(),
    @StringRes val errorMessageRes: Int? = null
)

class HomeViewModel(
    private val observeRecentScansUseCase: ObserveRecentScansUseCase,
    private val deleteRecentScanUseCase: DeleteRecentScanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeRecentScans()
    }

    private fun observeRecentScans() {
        viewModelScope.launch {
            observeRecentScansUseCase()
                .collect { items ->
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        recentScans = items,
                        errorMessageRes = null
                    )
                }
        }
    }

    fun deleteRecentScan(id: Long) {
        viewModelScope.launch {
            when (val result = deleteRecentScanUseCase(id)) {
                RecentScanDeleteResult.Success -> Unit
                RecentScanDeleteResult.NotFound -> {
                    _uiState.update {
                        it.copy(errorMessageRes = R.string.main_recent_delete_not_found)
                    }
                }
                is RecentScanDeleteResult.PartialFailure -> {
                    Timber.w(
                        "Delete partial: db=%s file=%s",
                        result.removedFromDatabase,
                        result.fileDeleted
                    )
                    _uiState.update {
                        it.copy(errorMessageRes = R.string.main_recent_delete_partial)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessageRes = null) }
    }
}
