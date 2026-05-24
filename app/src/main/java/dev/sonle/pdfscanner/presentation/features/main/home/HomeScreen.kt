package dev.sonle.pdfscanner.presentation.features.main.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import dev.sonle.pdfscanner.R
import android.app.Activity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import dev.sonle.pdfscanner.BuildConfig
import dev.sonle.pdfscanner.core.review.InAppReviewManager
import dev.sonle.pdfscanner.core.review.InAppReviewOutcome
import dev.sonle.pdfscanner.core.review.ReviewPromptDebug
import dev.sonle.pdfscanner.core.util.findActivity
import timber.log.Timber
import dev.sonle.pdfscanner.core.util.PdfExportActions
import dev.sonle.pdfscanner.domain.repository.ReviewPromptRepository
import dev.sonle.pdfscanner.presentation.components.ReviewPromptDialog
import dev.sonle.pdfscanner.domain.model.AppLanguage
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.presentation.locale.labelResId
import dev.sonle.pdfscanner.domain.util.DocumentFileNameNormalizer
import dev.sonle.pdfscanner.domain.navigation.PdfViewerScreenRoute
import dev.sonle.pdfscanner.core.ads.AdPlacementPolicy
import dev.sonle.pdfscanner.presentation.ads.AdUiStateHolder
import dev.sonle.pdfscanner.presentation.navigation.LocalNavigator
import org.koin.compose.koinInject
import dev.sonle.pdfscanner.presentation.util.FormatUtils
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat

/** Space reserved above the main bottom navigation bar (incl. FAB offset). */
private val HomeBottomNavClearance = 96.dp

private val HomeTitleToSearchSpacing = 20.dp
private val HomeSearchToListSpacing = 32.dp

private val SelectionSheetTopRadius = 24.dp
private val SelectionSheetContentHeight = 88.dp

/** List padding while the selection bottom sheet is visible (nav bar hidden). */
private val SelectionSheetListClearance = SelectionSheetContentHeight + 48.dp

@Composable
fun HomeScreen(
    onOpenScanner: () -> Unit,
    onSelectionModeChanged: (Boolean) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dismissKeyboard: () -> Unit = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        Unit
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReviewPrompt by remember { mutableStateOf(false) }
    val reviewPromptRepository: ReviewPromptRepository = koinInject()
    val inAppReviewManager: InAppReviewManager = koinInject()
    val hostActivity = LocalActivityResultRegistryOwner.current as? Activity

    LaunchedEffect(Unit) {
        if (BuildConfig.ENABLE_DEBUG_LOGGING && ReviewPromptDebug.forceShowOnHome) {
            ReviewPromptDebug.forceShowOnHome = false
            reviewPromptRepository.prepareDebugReviewPromptOnHome()
            showReviewPrompt = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, reviewPromptRepository) {
        fun tryShowReviewPrompt() {
            reviewPromptRepository.refreshReviewPromptPendingFromCounts()
            if (reviewPromptRepository.consumePendingReviewPromptOnHome()) {
                showReviewPrompt = true
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                tryShowReviewPrompt()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            tryShowReviewPrompt()
        }
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                }

                is HomeUiEffect.DocumentsDeleted -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.main_selection_deleted, effect.count)
                    )
                }
            }
        }
    }

    val adUiStateHolder: AdUiStateHolder = koinInject()

    LaunchedEffect(uiState.isSelectionMode) {
        onSelectionModeChanged(uiState.isSelectionMode)
        adUiStateHolder.setSelectionMode(uiState.isSelectionMode)
    }

    if (uiState.isSelectionMode) {
        BackHandler { viewModel.exitSelectionMode() }
    } else if (uiState.isSearching) {
        BackHandler {
            dismissKeyboard()
            viewModel.clearSearch()
        }
    }

    val bannerExtraPadding = if (
        adUiStateHolder.adsEnabled && !uiState.isSelectionMode
    ) {
        AdPlacementPolicy.BANNER_SLOT_HEIGHT_DP.dp +
            AdPlacementPolicy.MIN_CLICKABLE_SEPARATION_DP.dp
    } else {
        0.dp
    }
    val listBottomPadding = if (uiState.isSelectionMode) {
        SelectionSheetListClearance
    } else {
        HomeBottomNavClearance + bannerExtraPadding
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissKeyboardOnTap()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                HomeTopBar(
                    isSelectionMode = uiState.isSelectionMode,
                    selectedCount = uiState.selectedCount,
                    isAllSelected = uiState.isAllSelected,
                    canSelectAll = uiState.canSelectAll,
                    isBulkActionInProgress = uiState.isBulkActionInProgress,
                    selectedLanguage = uiState.appLanguage,
                    onDismissKeyboard = dismissKeyboard,
                    onLanguageSelected = viewModel::onLanguageSelected,
                    onExitSelectionMode = viewModel::exitSelectionMode,
                    onToggleSelectAll = viewModel::toggleSelectAll
                )

                AnimatedVisibility(visible = !uiState.isSelectionMode) {
                    Column {
                        Spacer(modifier = Modifier.height(HomeTitleToSearchSpacing))
                        HomeSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::updateSearchQuery,
                            onClear = viewModel::clearSearch,
                            enabled = !uiState.isLoading
                        )
                        if (uiState.isSearching && uiState.displayedScans.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pluralStringResource(
                                    R.plurals.main_search_results_count,
                                    uiState.displayedScans.size,
                                    uiState.displayedScans.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(HomeSearchToListSpacing))

                uiState.errorMessageRes?.let { messageRes ->
                    Text(
                        text = stringResource(messageRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LaunchedEffect(messageRes) {
                        viewModel.clearError()
                    }
                }

                when {
                    uiState.isLoading -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .shimmerEffect()
                                )
                            }
                        }
                    }

                    uiState.isEmptyLibrary -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = listBottomPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyDocsState()
                        }
                    }

                    uiState.isEmptySearchResults -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = listBottomPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            SearchNoResultsState(onClearSearch = viewModel::clearSearch)
                        }
                    }

                    else -> {
                        val listState = rememberLazyListState()
                        LaunchedEffect(listState.isScrollInProgress) {
                            if (listState.isScrollInProgress) {
                                dismissKeyboard()
                            }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = listBottomPadding),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.displayedScans, key = { it.id }) { recentScan ->
                                RecentScanItem(
                                    recentScan = recentScan,
                                    isSelectionMode = uiState.isSelectionMode,
                                    isSelected = recentScan.id in uiState.selectedScanIds,
                                    onDismissKeyboard = dismissKeyboard,
                                    onOpen = {
                                        navigator.navigateTo(PdfViewerScreenRoute(recentScan.filePath))
                                    },
                                    onShare = {
                                        PdfExportActions.sharePdfFiles(
                                            context,
                                            listOf(recentScan.filePath),
                                            context.getString(R.string.main_selection_share_chooser)
                                        ).onFailure {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    context.getString(R.string.main_selection_share_failed)
                                                )
                                            }
                                        }
                                    },
                                    onToggleSelection = { viewModel.toggleScanSelection(recentScan.id) },
                                    onEnterSelection = { viewModel.enterSelectionWithScan(recentScan.id) },
                                    onRename = { newName ->
                                        viewModel.renameRecentScan(recentScan.id, newName)
                                    },
                                    onDelete = { viewModel.deleteRecentScan(recentScan.id) }
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.isSelectionMode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                HomeSelectionActionBar(
                    canShare = uiState.hasSelection && !uiState.isBulkActionInProgress,
                    canDelete = uiState.hasSelection && !uiState.isBulkActionInProgress,
                    canOpen = uiState.selectedCount == 1 && !uiState.isBulkActionInProgress,
                    isLoading = uiState.isBulkActionInProgress,
                    onShare = {
                        val paths = viewModel.selectedScans().map { it.filePath }
                        PdfExportActions.sharePdfFiles(
                            context,
                            paths,
                            context.getString(R.string.main_selection_share_chooser)
                        ).onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.main_selection_share_failed)
                                )
                            }
                        }
                    },
                    onOpen = {
                        viewModel.selectedScans().firstOrNull()?.let { scan ->
                            viewModel.exitSelectionMode()
                            navigator.navigateTo(PdfViewerScreenRoute(scan.filePath))
                        }
                    },
                    onDelete = { showDeleteDialog = true }
                )
            }
        }
    }

    if (showReviewPrompt) {
        ReviewPromptDialog(
            adsEnabled = adUiStateHolder.adsEnabled,
            onRate = {
                showReviewPrompt = false
                reviewPromptRepository.markReviewPromptHandled()
                val activity = hostActivity ?: context.findActivity()
                if (activity == null) {
                    Timber.w("Review prompt: no Activity for in-app review")
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.settings_play_store_unavailable)
                        )
                    }
                } else {
                    scope.launch {
                        when (inAppReviewManager.requestReview(activity)) {
                            InAppReviewOutcome.Unavailable -> {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.settings_play_store_unavailable)
                                )
                            }
                            InAppReviewOutcome.FlowFinished,
                            InAppReviewOutcome.StoreFallback -> Unit
                        }
                    }
                }
            },
            onLater = {
                showReviewPrompt = false
                reviewPromptRepository.markReviewPromptHandled()
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.main_selection_delete_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.main_selection_delete_message,
                        uiState.selectedCount
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSelectedScans()
                    }
                ) {
                    Text(
                        stringResource(R.string.main_selection_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun HomeTopBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    isAllSelected: Boolean,
    canSelectAll: Boolean,
    isBulkActionInProgress: Boolean,
    selectedLanguage: AppLanguage,
    onDismissKeyboard: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onExitSelectionMode: () -> Unit,
    onToggleSelectAll: () -> Unit
) {
    if (isSelectionMode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                onDismissKeyboard()
                onExitSelectionMode()
            }) {
                Text(
                    text = stringResource(R.string.main_selection_cancel),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = stringResource(R.string.main_selection_count, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isBulkActionInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                TextButton(
                    onClick = {
                        onDismissKeyboard()
                        onToggleSelectAll()
                    },
                    enabled = canSelectAll
                ) {
                    Text(
                        text = stringResource(
                            if (isAllSelected) {
                                R.string.main_selection_deselect_all
                            } else {
                                R.string.main_selection_select_all
                            }
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.main_documents_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            HomeLanguageSelector(
                selectedLanguage = selectedLanguage,
                onDismissKeyboard = onDismissKeyboard,
                onLanguageSelected = onLanguageSelected
            )
        }
    }
}

@Composable
private fun HomeLanguageSelector(
    selectedLanguage: AppLanguage,
    onDismissKeyboard: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = {
                onDismissKeyboard()
                menuExpanded = true
            }
        ) {
            Icon(
                imageVector = Iconsax.Linear.Global,
                contentDescription = stringResource(R.string.language_action),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(selectedLanguage.labelResId()),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            Text(
                text = stringResource(R.string.language_menu_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(language.labelResId()),
                            fontWeight = if (language == selectedLanguage) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onLanguageSelected(language)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeSelectionActionBar(
    canShare: Boolean,
    canDelete: Boolean,
    canOpen: Boolean,
    isLoading: Boolean,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetShape = RoundedCornerShape(
        topStart = SelectionSheetTopRadius,
        topEnd = SelectionSheetTopRadius
    )
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val sheetBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.surface
        )
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = sheetShape,
        color = Color.Transparent,
        shadowElevation = 16.dp,
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(sheetBackground)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SelectionSheetContentHeight),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SelectionSheetContentHeight)
                        .padding(horizontal = 8.dp)
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionBarAction(
                        icon = Iconsax.Bold.Export,
                        label = stringResource(R.string.main_selection_share),
                        enabled = canShare,
                        isDestructive = false,
                        onClick = onShare
                    )
                    SelectionBarAction(
                        icon = Iconsax.Bold.DocumentText,
                        label = stringResource(R.string.main_selection_open),
                        enabled = canOpen,
                        isDestructive = false,
                        onClick = onOpen
                    )
                    SelectionBarAction(
                        icon = Iconsax.Linear.Trash,
                        label = stringResource(R.string.main_selection_delete),
                        enabled = canDelete,
                        isDestructive = true,
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionBarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    isDestructive: Boolean,
    onClick: () -> Unit
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isDestructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Modifier.dismissKeyboardOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        })
    }
}

@Composable
private fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dismissKeyboard: () -> Unit = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        Unit
    }
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = hintColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { dismissKeyboard() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.main_search_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = hintColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onClear()
                        dismissKeyboard()
                    },
                    enabled = enabled,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.main_search_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchNoResultsState(onClearSearch: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.main_search_no_results_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.main_search_no_results_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = onClearSearch) {
            Text(stringResource(R.string.main_search_clear))
        }
    }
}

@Composable
fun EmptyDocsState() {
    val boxColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Iconsax.Bold.DirectboxDefault,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    val brush = Brush.verticalGradient(
                        colors = listOf(boxColor, boxColor.copy(alpha = 0f)),
                        startY = 0f,
                        endY = size.height
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush, blendMode = BlendMode.SrcIn)
                    }
                }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.main_empty_docs_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.main_empty_docs_message),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentScanItem(
    recentScan: RecentScan,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onDismissKeyboard: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onToggleSelection: () -> Unit,
    onEnterSelection: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showActionMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(16.dp)
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val selectionBorder = if (isSelectionMode && isSelected) {
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = cardShape
        )
    } else {
        Modifier
    }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .then(selectionBorder)
            .combinedClickable(
                onClick = {
                    onDismissKeyboard()
                    if (isSelectionMode) onToggleSelection() else onOpen()
                },
                onLongClick = {
                    onDismissKeyboard()
                    if (!isSelectionMode) onEnterSelection()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Iconsax.Bold.DocumentText,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recentScan.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.main_recent_scan_meta,
                        formatDate(recentScan.savedAt),
                        recentScan.pageCount,
                        FormatUtils.formatFileSize(recentScan.fileSizeBytes)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelectionMode) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(
                                R.string.main_selection_item_cd,
                                recentScan.fileName
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                    shape = CircleShape
                                )
                        )
                    }
                } else {
                    Box {
                        IconButton(
                            onClick = {
                                onDismissKeyboard()
                                showActionMenu = true
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Iconsax.Linear.More,
                                contentDescription = stringResource(R.string.main_recent_more_actions),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(90f)
                            )
                        }
                        DropdownMenu(
                            expanded = showActionMenu,
                            onDismissRequest = {
                                showActionMenu = false
                                onDismissKeyboard()
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.main_recent_share)) },
                                onClick = {
                                    showActionMenu = false
                                    onDismissKeyboard()
                                    onShare()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Iconsax.Bold.Export,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.main_recent_rename)) },
                                onClick = {
                                    showActionMenu = false
                                    onDismissKeyboard()
                                    showRenameDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Iconsax.Linear.Edit,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.main_recent_delete),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showActionMenu = false
                                    onDismissKeyboard()
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Iconsax.Linear.Trash,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameDocumentDialog(
            currentFileName = recentScan.fileName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                showRenameDialog = false
                onRename(newName)
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.main_recent_delete_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.main_recent_delete_message,
                        recentScan.fileName
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.main_selection_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RenameDocumentDialog(
    currentFileName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameInput by remember(currentFileName) {
        mutableStateOf(DocumentFileNameNormalizer.displayNameWithoutExtension(currentFileName))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.main_recent_rename_title)) },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(stringResource(R.string.main_recent_rename_label)) },
                singleLine = true,
                suffix = { Text(stringResource(R.string.main_recent_rename_suffix)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nameInput) },
                enabled = nameInput.trim().isNotEmpty()
            ) {
                Text(stringResource(R.string.main_recent_rename_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(timestamp)
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "shimmer_offset"
    )
    val colorScheme = MaterialTheme.colorScheme
    val shimmerColors = listOf(
        colorScheme.surfaceVariant.copy(alpha = 0.3f),
        colorScheme.surfaceVariant.copy(alpha = 0.6f),
        colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 400f, y = translateAnim - 400f),
        end = Offset(x = translateAnim, y = translateAnim)
    )
    background(brush)
}
