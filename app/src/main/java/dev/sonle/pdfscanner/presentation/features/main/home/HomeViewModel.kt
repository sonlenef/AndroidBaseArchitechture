package dev.sonle.pdfscanner.presentation.features.main.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.RecentScanDeleteResult
import dev.sonle.pdfscanner.domain.repository.RecentScanRenameResult
import dev.sonle.pdfscanner.domain.usecase.DeleteRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.DeleteRecentScansUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveRecentScansUseCase
import dev.sonle.pdfscanner.domain.usecase.RenameRecentScanUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class HomeUiState(
    val isLoading: Boolean = true,
    val allRecentScans: List<RecentScan> = emptyList(),
    val searchQuery: String = "",
    val isSelectionMode: Boolean = false,
    val selectedScanIds: Set<Long> = emptySet(),
    val isBulkActionInProgress: Boolean = false,
    @StringRes val errorMessageRes: Int? = null
) {
    val displayedScans: List<RecentScan> =
        HomeDocumentFilter.filter(allRecentScans, searchQuery)

    val isSearching: Boolean get() = searchQuery.isNotBlank()
    val isEmptyLibrary: Boolean get() = !isLoading && allRecentScans.isEmpty()
    val isEmptySearchResults: Boolean get() =
        !isLoading && allRecentScans.isNotEmpty() && displayedScans.isEmpty()

    val selectedCount: Int get() = selectedScanIds.size
    val canSelectAll: Boolean get() = displayedScans.isNotEmpty()
    val isAllSelected: Boolean get() =
        canSelectAll && displayedScans.all { it.id in selectedScanIds }
    val hasSelection: Boolean get() = selectedScanIds.isNotEmpty()
}

class HomeViewModel(
    private val observeRecentScansUseCase: ObserveRecentScansUseCase,
    private val deleteRecentScanUseCase: DeleteRecentScanUseCase,
    private val deleteRecentScansUseCase: DeleteRecentScansUseCase,
    private val renameRecentScanUseCase: RenameRecentScanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffects = _uiEffects.receiveAsFlow()

    init {
        observeRecentScans()
    }

    private fun observeRecentScans() {
        viewModelScope.launch {
            observeRecentScansUseCase()
                .collect { items ->
                    _uiState.update { state ->
                        val validIds = state.selectedScanIds.intersect(items.map { it.id }.toSet())
                        val shouldExitSelection =
                            state.isSelectionMode && items.isEmpty()
                        state.copy(
                            isLoading = false,
                            allRecentScans = items,
                            errorMessageRes = null,
                            selectedScanIds = validIds,
                            isSelectionMode = if (shouldExitSelection) false else state.isSelectionMode
                        )
                    }
                }
        }
    }

    fun toggleSelectionMode() {
        _uiState.update { state ->
            if (state.isSelectionMode) {
                state.copy(isSelectionMode = false, selectedScanIds = emptySet())
            } else {
                state.copy(isSelectionMode = true, selectedScanIds = emptySet())
            }
        }
    }

    fun exitSelectionMode() {
        _uiState.update {
            it.copy(isSelectionMode = false, selectedScanIds = emptySet())
        }
    }

    fun toggleScanSelection(scanId: Long) {
        _uiState.update { state ->
            if (!state.isSelectionMode) return@update state
            val updated = state.selectedScanIds.toMutableSet()
            if (!updated.add(scanId)) {
                updated.remove(scanId)
            }
            state.copy(selectedScanIds = updated)
        }
    }

    fun enterSelectionWithScan(scanId: Long) {
        _uiState.update { state ->
            state.copy(
                isSelectionMode = true,
                selectedScanIds = state.selectedScanIds + scanId
            )
        }
    }

    fun toggleSelectAll() {
        _uiState.update { state ->
            if (!state.isSelectionMode || state.displayedScans.isEmpty()) return@update state
            val visibleIds = state.displayedScans.map { it.id }.toSet()
            val updatedSelection = if (state.isAllSelected) {
                state.selectedScanIds - visibleIds
            } else {
                state.selectedScanIds + visibleIds
            }
            state.copy(selectedScanIds = updatedSelection)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val displayedIds = HomeDocumentFilter
                .filter(state.allRecentScans, query)
                .map { it.id }
                .toSet()
            val prunedSelection = if (state.isSelectionMode) {
                state.selectedScanIds.intersect(displayedIds)
            } else {
                state.selectedScanIds
            }
            state.copy(
                searchQuery = query,
                selectedScanIds = prunedSelection
            )
        }
    }

    fun clearSearch() {
        updateSearchQuery("")
    }

    fun renameRecentScan(id: Long, newFileName: String) {
        viewModelScope.launch {
            when (renameRecentScanUseCase(id, newFileName)) {
                is RecentScanRenameResult.Success -> {
                    _uiEffects.send(HomeUiEffect.ShowMessage(R.string.main_recent_rename_success))
                }
                RecentScanRenameResult.NotFound -> {
                    _uiState.update {
                        it.copy(errorMessageRes = R.string.main_recent_delete_not_found)
                    }
                }
                RecentScanRenameResult.InvalidName -> {
                    _uiEffects.send(HomeUiEffect.ShowMessage(R.string.main_recent_rename_invalid))
                }
                RecentScanRenameResult.NameAlreadyExists -> {
                    _uiEffects.send(HomeUiEffect.ShowMessage(R.string.main_recent_rename_exists))
                }
                RecentScanRenameResult.Failed -> {
                    _uiEffects.send(HomeUiEffect.ShowMessage(R.string.main_recent_rename_failed))
                }
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

    fun deleteSelectedScans() {
        val selectedIds = _uiState.value.selectedScanIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBulkActionInProgress = true) }
            val result = deleteRecentScansUseCase(selectedIds)
            _uiState.update {
                it.copy(
                    isBulkActionInProgress = false,
                    isSelectionMode = false,
                    selectedScanIds = emptySet()
                )
            }
            when {
                result.deletedCount > 0 && !result.hasAnyFailure -> {
                    _uiEffects.send(HomeUiEffect.DocumentsDeleted(result.deletedCount))
                }
                result.deletedCount > 0 && result.hasAnyFailure -> {
                    _uiEffects.send(HomeUiEffect.DocumentsDeleted(result.deletedCount))
                    _uiEffects.send(HomeUiEffect.ShowMessage(R.string.main_selection_delete_partial))
                }
                result.deletedCount == 0 && result.notFoundCount > 0 -> {
                    _uiEffects.send(HomeUiEffect.ShowMessage(R.string.main_recent_delete_not_found))
                }
                else -> {
                    _uiEffects.send(HomeUiEffect.ShowMessage(R.string.main_recent_action_failed))
                }
            }
        }
    }

    fun selectedScans(): List<RecentScan> {
        val state = _uiState.value
        return state.allRecentScans.filter { it.id in state.selectedScanIds }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessageRes = null) }
    }
}
