package dev.sonle.pdfscanner.core.locale

import dev.sonle.pdfscanner.domain.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-app locale state for Compose. Updates here recompose the UI without recreating the Activity.
 */
class AppLocaleController {

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun initialize(language: AppLanguage) {
        _language.value = language
    }

    fun setLanguage(language: AppLanguage) {
        if (_language.value != language) {
            _language.value = language
        }
    }
}
