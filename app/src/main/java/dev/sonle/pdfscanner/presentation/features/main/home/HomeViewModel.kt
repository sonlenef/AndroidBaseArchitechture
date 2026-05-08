package dev.sonle.pdfscanner.presentation.features.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.domain.model.RecentScan
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
    val errorMessage: String? = null
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
                        errorMessage = null
                    )
                }
        }
    }

    fun deleteRecentScan(id: Long) {
        viewModelScope.launch {
            runCatching { deleteRecentScanUseCase(id) }
                .onFailure { error ->
                    Timber.e(error, "Failed to delete recent scan")
                    _uiState.update {
                        it.copy(errorMessage = error.message)
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
