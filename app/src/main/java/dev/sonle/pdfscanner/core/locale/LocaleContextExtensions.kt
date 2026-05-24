package dev.sonle.pdfscanner.core.locale

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import dev.sonle.pdfscanner.domain.model.AppLanguage
import java.util.Locale

fun Context.withAppLanguage(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.languageTag)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLocales(LocaleList(locale))
    return createConfigurationContext(config)
}
