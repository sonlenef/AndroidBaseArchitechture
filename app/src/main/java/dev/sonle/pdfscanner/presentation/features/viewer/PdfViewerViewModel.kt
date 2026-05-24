package dev.sonle.pdfscanner.presentation.features.viewer

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.domain.model.PdfViewerDocument
import dev.sonle.pdfscanner.domain.model.PdfViewerError
import dev.sonle.pdfscanner.domain.repository.PdfViewerRepository
import dev.sonle.pdfscanner.domain.repository.PdfViewerException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

sealed interface PdfPageUiState {
    data object Loading : PdfPageUiState
    data class Ready(val image: ImageBitmap) : PdfPageUiState
    data class Failed(@StringRes val messageRes: Int) : PdfPageUiState
}

data class PdfViewerUiState(
    val isLoading: Boolean = true,
    val fileName: String = "",
    val pageCount: Int = 0,
    val currentPageIndex: Int = 0,
    val pageStates: Map<Int, PdfPageUiState> = emptyMap(),
    @StringRes val errorMessageRes: Int? = null
) {
    val isReady: Boolean get() = !isLoading && errorMessageRes == null && pageCount > 0
}

class PdfViewerViewModel(
    private val filePath: String,
    private val pdfViewerRepository: PdfViewerRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfViewerUiState())
    val uiState: StateFlow<PdfViewerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PdfViewerUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var document: PdfViewerDocument? = null
    private val pageLoadMutex = Mutex()
    private var targetWidthPx: Int = DEFAULT_TARGET_WIDTH_PX

    init {
        openDocument()
    }

    fun setTargetWidthPx(widthPx: Int) {
        if (widthPx > 0) {
            targetWidthPx = widthPx
        }
    }

    fun onPageSelected(pageIndex: Int) {
        goToPage(pageIndex, fromPager = true)
    }

    fun onGoToPage(pageIndex: Int) {
        goToPage(pageIndex, fromPager = false)
    }

    private fun goToPage(pageIndex: Int, fromPager: Boolean) {
        val state = _uiState.value
        if (pageIndex !in 0 until state.pageCount) return
        if (pageIndex == state.currentPageIndex && fromPager) return
        _uiState.update { it.copy(currentPageIndex = pageIndex) }
        trimCacheAround(pageIndex)
        requestPage(pageIndex)
        preloadAdjacent(pageIndex)
    }

    fun onShareClick() {
        emitExportAction { PdfViewerUiEffect.Share(it) }
    }

    fun onOpenWithClick() {
        emitExportAction { PdfViewerUiEffect.OpenWith(it) }
    }

    fun onSaveToDownloadsClick() {
        emitExportAction { PdfViewerUiEffect.SaveToDownloads(it) }
    }

    fun onRetryOpen() {
        openDocument()
    }

    override fun onCleared() {
        pdfViewerRepository.close()
        super.onCleared()
    }

    private fun openDocument() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = PdfViewerUiState(isLoading = true)
            pdfViewerRepository.openDocument(filePath)
                .onSuccess { opened ->
                    document = opened
                    _uiState.value = PdfViewerUiState(
                        isLoading = false,
                        fileName = opened.fileName,
                        pageCount = opened.pageCount,
                        currentPageIndex = 0
                    )
                    trimCacheAround(0)
                    requestPage(0)
                    preloadAdjacent(0)
                }
                .onFailure { error ->
                    val viewerError = (error as? PdfViewerException)?.error
                        ?: PdfViewerError.OpenFailed
                    Timber.w(error, "PDF viewer open failed for %s", filePath)
                    _uiState.value = PdfViewerUiState(
                        isLoading = false,
                        errorMessageRes = viewerError.toMessageRes()
                    )
                }
        }
    }

    private fun requestPage(pageIndex: Int) {
        val pageCount = _uiState.value.pageCount
        if (pageIndex !in 0 until pageCount) return

        val existing = _uiState.value.pageStates[pageIndex]
        if (existing is PdfPageUiState.Ready || existing is PdfPageUiState.Loading) return

        _uiState.update { state ->
            state.copy(
                pageStates = state.pageStates + (pageIndex to PdfPageUiState.Loading)
            )
        }

        viewModelScope.launch(ioDispatcher) {
            pageLoadMutex.withLock {
                pdfViewerRepository.renderPage(pageIndex, targetWidthPx)
                    .onSuccess { bitmap ->
                        val image = bitmap.asImageBitmap()
                        _uiState.update { state ->
                            state.copy(
                                pageStates = state.pageStates + (pageIndex to PdfPageUiState.Ready(image))
                            )
                        }
                    }
                    .onFailure { error ->
                        Timber.w(error, "Failed to render page %d", pageIndex)
                        _uiState.update { state ->
                            state.copy(
                                pageStates = state.pageStates + (
                                    pageIndex to PdfPageUiState.Failed(R.string.viewer_page_render_failed)
                                    )
                            )
                        }
                    }
            }
        }
    }

    private fun preloadAdjacent(currentIndex: Int) {
        val pageCount = _uiState.value.pageCount
        listOf(currentIndex - 1, currentIndex + 1)
            .filter { it in 0 until pageCount }
            .forEach(::requestPage)
    }

    private fun trimCacheAround(currentIndex: Int) {
        val keep = buildSet {
            add(currentIndex)
            add(currentIndex - 1)
            add(currentIndex + 1)
        }.filter { it >= 0 && it < _uiState.value.pageCount }.toSet()
        pdfViewerRepository.releasePagesExcept(keep)
        _uiState.update { state ->
            state.copy(
                pageStates = state.pageStates.filterKeys { it in keep }
            )
        }
    }

    private fun emitExportAction(factory: (dev.sonle.pdfscanner.domain.model.ExportedPdf) -> PdfViewerUiEffect) {
        val exported = document?.toExportedPdf() ?: return
        viewModelScope.launch {
            _effects.send(factory(exported))
        }
    }

    private fun PdfViewerError.toMessageRes(): Int = when (this) {
        PdfViewerError.FileNotFound -> R.string.viewer_file_not_found
        PdfViewerError.OpenFailed -> R.string.viewer_open_failed
        is PdfViewerError.RenderFailed -> R.string.viewer_open_failed
    }

    companion object {
        private const val DEFAULT_TARGET_WIDTH_PX = 1080
    }
}
