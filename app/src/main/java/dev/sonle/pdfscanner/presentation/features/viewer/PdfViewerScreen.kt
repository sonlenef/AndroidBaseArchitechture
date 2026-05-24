package dev.sonle.pdfscanner.presentation.features.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.util.PdfExportActions
import dev.sonle.pdfscanner.presentation.components.ErrorView
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

const val PDF_VIEWER_PAGER_TEST_TAG = "pdfViewerPager"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    filePath: String,
    onBack: () -> Unit,
    viewModel: PdfViewerViewModel = koinViewModel { parametersOf(filePath) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val targetWidthPx = remember(screenWidthDp, density) {
        with(density) { screenWidthDp.dp.roundToPx() }
    }

    LaunchedEffect(targetWidthPx) {
        viewModel.setTargetWidthPx(targetWidthPx)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PdfViewerUiEffect.Share -> {
                    PdfExportActions.sharePdf(
                        context,
                        effect.exported,
                        context.getString(R.string.viewer_share_chooser_title)
                    ).onFailure {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.viewer_share_failed)
                        )
                    }
                }
                is PdfViewerUiEffect.OpenWith -> {
                    PdfExportActions.openWithPdf(
                        context,
                        effect.exported,
                        context.getString(R.string.viewer_open_with_chooser_title)
                    ).onFailure {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.viewer_open_with_failed)
                        )
                    }
                }
                is PdfViewerUiEffect.SaveToDownloads -> {
                    PdfExportActions.saveToDownloads(context, effect.exported)
                        .onSuccess {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.viewer_saved_to_downloads)
                            )
                        }
                        .onFailure {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.viewer_save_downloads_failed)
                            )
                        }
                }
                is PdfViewerUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        if (uiState.isReady) {
                            Text(
                                text = stringResource(
                                    R.string.viewer_page_indicator,
                                    uiState.currentPageIndex + 1,
                                    uiState.pageCount
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState.isReady) {
                        IconButton(onClick = viewModel::onShareClick) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.viewer_share)
                            )
                        }
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.viewer_more_actions)
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.viewer_open_with)) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.onOpenWithClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.viewer_save_to_downloads)) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.onSaveToDownloadsClick()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessageRes != null -> {
                    ErrorView(
                        message = stringResource(uiState.errorMessageRes!!),
                        onRetry = viewModel::onRetryOpen,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.isReady -> {
                    PdfViewerPager(
                        uiState = uiState,
                        onPageSettled = viewModel::onPageSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfViewerPager(
    uiState: PdfViewerUiState,
    onPageSettled: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    key(uiState.pageCount) {
        val pagerState = rememberPagerState(
            initialPage = uiState.currentPageIndex.coerceIn(0, uiState.pageCount - 1),
            pageCount = { uiState.pageCount }
        )

        LaunchedEffect(pagerState, uiState.pageCount) {
            snapshotFlow { pagerState.settledPage }
                .distinctUntilChanged()
                .collect(onPageSettled)
        }

        LaunchedEffect(uiState.currentPageIndex) {
            if (pagerState.currentPage != uiState.currentPageIndex) {
                scope.launch {
                    pagerState.animateScrollToPage(uiState.currentPageIndex)
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag(PDF_VIEWER_PAGER_TEST_TAG),
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
            key = { pageIndex -> pageIndex }
        ) { pageIndex ->
            PdfViewerPageContent(
                pageState = uiState.pageStates[pageIndex],
                pageIndex = pageIndex
            )
        }
    }
}

@Composable
private fun PdfViewerPageContent(
    pageState: PdfPageUiState?,
    pageIndex: Int
) {
    when (pageState) {
        is PdfPageUiState.Ready -> {
            ZoomablePdfPage(
                image = pageState.image,
                pageIndex = pageIndex,
                modifier = Modifier.fillMaxSize()
            )
        }
        is PdfPageUiState.Failed -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(pageState.messageRes),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        PdfPageUiState.Loading, null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.viewer_page_loading, pageIndex + 1),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
