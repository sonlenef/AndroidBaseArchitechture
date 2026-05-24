package dev.sonle.pdfscanner.presentation.locale

import androidx.annotation.StringRes
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.domain.model.AppLanguage

@StringRes
fun AppLanguage.labelResId(): Int = when (this) {
    AppLanguage.ENGLISH -> R.string.language_option_english
    AppLanguage.SPANISH -> R.string.language_option_spanish
    AppLanguage.PORTUGUESE -> R.string.language_option_portuguese
    AppLanguage.HINDI -> R.string.language_option_hindi
    AppLanguage.VIETNAMESE -> R.string.language_option_vietnamese
}
