package dev.sonle.pdfscanner.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dev.sonle.pdfscanner.domain.model.AppLanguage

/**
 * Applies per-app locale. Cold start syncs with the system; in-app changes only update [AppLocaleController]
 * to avoid Activity recreation and UI jank.
 */
class AppLocaleApplicator(
    private val appLocaleController: AppLocaleController
) {

    /** Call once from [android.app.Application.onCreate] before any Activity is shown. */
    fun applyForColdStart(language: AppLanguage) {
        appLocaleController.initialize(language)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.languageTag)
        )
    }

    /** Call when the user picks a language — smooth in-app switch (no Activity recreate). */
    fun applyInApp(language: AppLanguage) {
        appLocaleController.setLanguage(language)
    }
}
