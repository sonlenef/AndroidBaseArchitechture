package dev.sonle.pdfscanner.domain.model

/**
 * Supported in-app languages. [ENGLISH] is the default when none is stored.
 */
enum class AppLanguage(val languageTag: String) {
    ENGLISH("en"),
    SPANISH("es"),
    PORTUGUESE("pt"),
    HINDI("hi"),
    VIETNAMESE("vi");

    companion object {
        fun fromStoredValue(value: String?): AppLanguage = when (value) {
            null -> ENGLISH
            LEGACY_SYSTEM_VALUE -> ENGLISH
            else -> entries.firstOrNull { it.name == value } ?: ENGLISH
        }

        /** Previous builds persisted [SYSTEM]; treat as English. */
        private const val LEGACY_SYSTEM_VALUE = "SYSTEM"
    }
}
