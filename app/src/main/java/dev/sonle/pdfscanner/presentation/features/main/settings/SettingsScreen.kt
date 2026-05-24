package dev.sonle.pdfscanner.presentation.features.main.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sonle.pdfscanner.BuildConfig
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.domain.model.DocumentFilterPreset
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality
import dev.sonle.pdfscanner.domain.model.ScanPageLayout
import dev.sonle.pdfscanner.domain.model.ScannerCaptureMode
import dev.sonle.pdfscanner.domain.model.ThemeMode
import dev.sonle.pdfscanner.presentation.features.scanner.model.ImageFilter
import dev.sonle.pdfscanner.presentation.util.FormatUtils
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val versionLabel = rememberAppVersionLabel()
    val environmentLabel = rememberEnvironmentLabel()

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is SettingsUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                }
                SettingsUiEffect.OpenPlayStore -> {
                    val marketUri = Uri.parse("market://details?id=${context.packageName}")
                    val webUri = Uri.parse(
                        "https://play.google.com/store/apps/details?id=${context.packageName}"
                    )
                    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                    val launched = runCatching { context.startActivity(marketIntent) }.isSuccess
                    if (!launched) {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }.onFailure {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.settings_play_store_unavailable)
                            )
                        }
                    }
                }
                SettingsUiEffect.OpenPrivacyPolicy -> {
                    val privacyUri = Uri.parse(context.getString(R.string.settings_privacy_policy_url))
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, privacyUri))
                    }.onFailure {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.settings_privacy_policy_error)
                        )
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 24.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.main_settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.main_settings_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_scanning)) {
                    SettingNavigationRow(
                        icon = Iconsax.Linear.Scan,
                        title = stringResource(R.string.settings_default_capture_mode),
                        subtitle = uiState.settings.defaultCaptureMode.label(),
                        onClick = { viewModel.openSheet(SettingsSheet.CaptureMode) }
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Iconsax.Bold.DocumentCopy,
                        title = stringResource(R.string.settings_default_page_layout),
                        subtitle = uiState.settings.defaultPageLayout.label(),
                        onClick = { viewModel.openSheet(SettingsSheet.PageLayout) }
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Iconsax.Bold.Edit,
                        title = stringResource(R.string.settings_default_filter),
                        subtitle = uiState.settings.defaultFilter.label(),
                        onClick = { viewModel.openSheet(SettingsSheet.DefaultFilter) }
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Iconsax.Bold.Maximize,
                        title = stringResource(R.string.settings_pdf_quality),
                        subtitle = uiState.settings.pdfOutputQuality.label(),
                        onClick = { viewModel.openSheet(SettingsSheet.PdfQuality) }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                    SettingNavigationRow(
                        icon = Iconsax.Bold.Colorfilter,
                        title = stringResource(R.string.settings_theme),
                        subtitle = uiState.settings.themeMode.label(),
                        onClick = { viewModel.openSheet(SettingsSheet.Theme) }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingsDivider()
                        SettingSwitchRow(
                            icon = Iconsax.Bold.MagicStar,
                            title = stringResource(R.string.settings_dynamic_color),
                            subtitle = stringResource(R.string.settings_dynamic_color_desc),
                            checked = uiState.settings.useDynamicColor,
                            onCheckedChange = viewModel::onDynamicColorChanged
                        )
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_storage)) {
                    SettingInfoRow(
                        icon = Iconsax.Bold.Folder,
                        title = stringResource(R.string.settings_storage_usage),
                        subtitle = if (uiState.isLoadingStorage) {
                            stringResource(R.string.settings_storage_usage_loading)
                        } else {
                            stringResource(
                                R.string.settings_storage_summary,
                                uiState.scanFileCount,
                                FormatUtils.formatFileSize(uiState.storageBytes)
                            )
                        }
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Iconsax.Linear.Trash,
                        title = stringResource(R.string.settings_clear_all_data),
                        subtitle = stringResource(R.string.settings_clear_all_data_desc),
                        onClick = viewModel::showClearDataDialog,
                        destructive = true,
                        trailing = {
                            if (uiState.isClearingStorage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_about)) {
                    SettingNavigationRow(
                        icon = Iconsax.Bold.Star,
                        title = stringResource(R.string.settings_rate_app),
                        subtitle = stringResource(R.string.settings_rate_app_desc),
                        onClick = viewModel::onRateAppClick
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Iconsax.Bold.Shield,
                        title = stringResource(R.string.settings_privacy_policy),
                        subtitle = stringResource(R.string.settings_privacy_policy_desc),
                        onClick = viewModel::onPrivacyPolicyClick
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Iconsax.Bold.InfoCircle,
                        title = stringResource(R.string.settings_about),
                        subtitle = versionLabel,
                        onClick = viewModel::showAboutDialog
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
        )
    }

    uiState.activeSheet?.let { sheet ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissSheet,
            sheetState = sheetState
        ) {
            when (sheet) {
                SettingsSheet.CaptureMode -> SelectionSheetContent(
                    title = stringResource(R.string.settings_default_capture_mode),
                    options = ScannerCaptureMode.entries,
                    selected = uiState.settings.defaultCaptureMode,
                    label = { it.label() },
                    onSelected = viewModel::onCaptureModeSelected
                )
                SettingsSheet.PageLayout -> SelectionSheetContent(
                    title = stringResource(R.string.settings_default_page_layout),
                    options = ScanPageLayout.entries,
                    selected = uiState.settings.defaultPageLayout,
                    label = { it.label() },
                    onSelected = viewModel::onPageLayoutSelected
                )
                SettingsSheet.DefaultFilter -> SelectionSheetContent(
                    title = stringResource(R.string.settings_default_filter),
                    options = DocumentFilterPreset.entries,
                    selected = uiState.settings.defaultFilter,
                    label = { it.label() },
                    onSelected = viewModel::onFilterSelected
                )
                SettingsSheet.Theme -> SelectionSheetContent(
                    title = stringResource(R.string.settings_theme),
                    options = ThemeMode.entries,
                    selected = uiState.settings.themeMode,
                    label = { it.label() },
                    onSelected = viewModel::onThemeSelected
                )
                SettingsSheet.PdfQuality -> SelectionSheetContent(
                    title = stringResource(R.string.settings_pdf_quality),
                    options = PdfOutputQuality.entries,
                    selected = uiState.settings.pdfOutputQuality,
                    label = { it.label() },
                    onSelected = viewModel::onPdfQualitySelected
                )
            }
        }
    }

    if (uiState.showAboutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissAboutDialog,
            icon = {
                Icon(
                    imageVector = Iconsax.Bold.DocumentText,
                    contentDescription = null
                )
            },
            title = { Text(stringResource(R.string.app_name)) },
            text = {
                Text(
                    stringResource(
                        R.string.settings_about_message,
                        versionLabel,
                        environmentLabel
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissAboutDialog) {
                    Text(stringResource(R.string.settings_about_close))
                }
            }
        )
    }

    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearDataDialog,
            title = { Text(stringResource(R.string.settings_clear_data_title)) },
            text = { Text(stringResource(R.string.settings_clear_data_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmClearAllData) {
                    Text(
                        text = stringResource(R.string.settings_clear_data_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClearDataDialog) {
                    Text(stringResource(R.string.settings_clear_data_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun SettingNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon = icon, destructive = destructive)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (destructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.invoke() ?: Icon(
            imageVector = Iconsax.Linear.ArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon = icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon = icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingIcon(
    icon: ImageVector,
    destructive: Boolean = false
) {
    val container = if (destructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val tint = if (destructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun <T> SelectionSheetContent(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(option) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = option == selected,
                    onClick = { onSelected(option) }
                )
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberAppVersionLabel(): String {
    val suffix = when (BuildConfig.ENVIRONMENT) {
        "development" -> stringResource(R.string.settings_version_suffix_dev)
        "staging" -> stringResource(R.string.settings_version_suffix_staging)
        else -> ""
    }
    return stringResource(R.string.settings_version_label, BuildConfig.VERSION_NAME + suffix)
}

@Composable
private fun rememberEnvironmentLabel(): String = when (BuildConfig.ENVIRONMENT) {
    "development" -> stringResource(R.string.settings_environment_development)
    "staging" -> stringResource(R.string.settings_environment_staging)
    else -> stringResource(R.string.settings_environment_production)
}

@Composable
private fun ScannerCaptureMode.label(): String = when (this) {
    ScannerCaptureMode.AUTO -> stringResource(R.string.settings_capture_auto)
    ScannerCaptureMode.MANUAL -> stringResource(R.string.settings_capture_manual)
}

@Composable
private fun ScanPageLayout.label(): String = when (this) {
    ScanPageLayout.SINGLE -> stringResource(R.string.settings_page_single)
    ScanPageLayout.MULTI -> stringResource(R.string.settings_page_multi)
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun PdfOutputQuality.label(): String = when (this) {
    PdfOutputQuality.STANDARD -> stringResource(R.string.settings_pdf_standard)
    PdfOutputQuality.HIGH -> stringResource(R.string.settings_pdf_high)
}

@Composable
private fun DocumentFilterPreset.label(): String {
    val filter = when (this) {
        DocumentFilterPreset.ORIGINAL -> ImageFilter.ORIGINAL
        DocumentFilterPreset.BLACK_WHITE -> ImageFilter.BLACK_WHITE
        DocumentFilterPreset.GRAYSCALE -> ImageFilter.GRAYSCALE
        DocumentFilterPreset.MAGIC_COLOR -> ImageFilter.MAGIC_COLOR
        DocumentFilterPreset.SHARPEN -> ImageFilter.SHARPEN
    }
    return stringResource(filter.labelResId)
}
