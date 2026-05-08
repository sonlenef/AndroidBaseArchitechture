package dev.sonle.pdfscanner.presentation.features.scanner.model

/**
 * Page scanning mode.
 */
enum class PageMode {
    /** Scan a single page, proceed directly to save */
    SINGLE,
    /** Scan multiple pages into a single PDF */
    MULTI
}
