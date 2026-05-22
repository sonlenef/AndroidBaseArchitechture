package dev.sonle.pdfscanner.core.analytics

/**
 * Centralized Firebase Analytics event and parameter names.
 */
object FirebaseAnalyticsEvents {
    const val APP_STARTED = "app_started"
    const val PDF_SAVED = "pdf_saved"
    const val PDF_SAVE_FAILED = "pdf_save_failed"
    const val SCAN_STARTED = "scan_started"
    const val DOCUMENT_OPENED = "document_opened"
    const val DOCUMENT_DELETED = "document_deleted"
    const val DOCUMENT_RENAMED = "document_renamed"
    const val DOCUMENTS_BATCH_DELETED = "documents_batch_deleted"
    const val SEARCH_PERFORMED = "search_performed"
    const val SELECTION_MODE_ENTERED = "selection_mode_entered"

    const val PARAM_ENVIRONMENT = "environment"
    const val PARAM_VERSION = "version"
    const val PARAM_PAGE_COUNT = "page_count"
    const val PARAM_FILE_SIZE_BYTES = "file_size_bytes"
    const val PARAM_ERROR = "error"
    const val PARAM_DOCUMENT_COUNT = "document_count"
    const val PARAM_QUERY_LENGTH = "query_length"
    const val PARAM_SCREEN_NAME = "screen_name"
}
