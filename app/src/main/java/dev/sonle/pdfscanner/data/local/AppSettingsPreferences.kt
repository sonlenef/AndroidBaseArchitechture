package dev.sonle.pdfscanner.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dev.sonle.pdfscanner.core.util.Constants
import dev.sonle.pdfscanner.domain.model.AppLanguage
import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.model.DocumentFilterPreset
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality
import dev.sonle.pdfscanner.domain.model.ScanPageLayout
import dev.sonle.pdfscanner.domain.model.ScannerCaptureMode
import dev.sonle.pdfscanner.domain.model.ThemeMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AppSettingsPreferences(
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun observeSettings(): Flow<AppSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readSettings())
        }
        trySend(readSettings())
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun readSettings(): AppSettings = AppSettings(
        themeMode = prefs.getString(KEY_THEME, null)?.toThemeMode() ?: ThemeMode.SYSTEM,
        useDynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
        appLanguage = AppLanguage.fromStoredValue(prefs.getString(KEY_APP_LANGUAGE, null)),
        defaultCaptureMode = prefs.getString(KEY_CAPTURE_MODE, null)?.toCaptureMode()
            ?: ScannerCaptureMode.AUTO,
        defaultPageLayout = prefs.getString(KEY_PAGE_LAYOUT, null)?.toPageLayout()
            ?: ScanPageLayout.SINGLE,
        defaultFilter = prefs.getString(KEY_DEFAULT_FILTER, null)?.toFilterPreset()
            ?: DocumentFilterPreset.SHARPEN,
        pdfOutputQuality = prefs.getString(KEY_PDF_QUALITY, null)?.toPdfQuality()
            ?: PdfOutputQuality.STANDARD
    )

    fun writeSettings(settings: AppSettings) {
        prefs.edit {
            putString(KEY_THEME, settings.themeMode.name)
            putBoolean(KEY_DYNAMIC_COLOR, settings.useDynamicColor)
            putString(KEY_APP_LANGUAGE, settings.appLanguage.name)
            putString(KEY_CAPTURE_MODE, settings.defaultCaptureMode.name)
            putString(KEY_PAGE_LAYOUT, settings.defaultPageLayout.name)
            putString(KEY_DEFAULT_FILTER, settings.defaultFilter.name)
            putString(KEY_PDF_QUALITY, settings.pdfOutputQuality.name)
        }
    }

    private fun String.toThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(this) }.getOrDefault(ThemeMode.SYSTEM)

    private fun String.toCaptureMode(): ScannerCaptureMode =
        runCatching { ScannerCaptureMode.valueOf(this) }.getOrDefault(ScannerCaptureMode.AUTO)

    private fun String.toPageLayout(): ScanPageLayout =
        runCatching { ScanPageLayout.valueOf(this) }.getOrDefault(ScanPageLayout.SINGLE)

    private fun String.toFilterPreset(): DocumentFilterPreset =
        runCatching { DocumentFilterPreset.valueOf(this) }.getOrDefault(DocumentFilterPreset.SHARPEN)

    private fun String.toPdfQuality(): PdfOutputQuality =
        runCatching { PdfOutputQuality.valueOf(this) }.getOrDefault(PdfOutputQuality.STANDARD)

    companion object {
        private const val KEY_THEME = "settings_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "settings_dynamic_color"
        private const val KEY_APP_LANGUAGE = "settings_app_language"
        private const val KEY_CAPTURE_MODE = "settings_capture_mode"
        private const val KEY_PAGE_LAYOUT = "settings_page_layout"
        private const val KEY_DEFAULT_FILTER = "settings_default_filter"
        private const val KEY_PDF_QUALITY = "settings_pdf_quality"
    }
}
