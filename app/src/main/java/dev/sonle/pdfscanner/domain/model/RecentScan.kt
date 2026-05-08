package dev.sonle.pdfscanner.domain.model

data class RecentScan(
    val id: Long,
    val fileName: String,
    val filePath: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val savedAt: Long
)
