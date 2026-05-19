package dev.sonle.pdfscanner.domain.model

/**
 * Result of a successful PDF export (paths only — no Android types).
 */
data class ExportedPdf(
    val fileName: String,
    val absolutePath: String,
    val fileSizeBytes: Long
)
