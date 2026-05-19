package dev.sonle.pdfscanner

import dev.sonle.pdfscanner.domain.model.ExportPage
import dev.sonle.pdfscanner.domain.model.RecentScan

object TestData {
    val sampleRecentScan = RecentScan(
        id = 1L,
        fileName = "Scan_1.pdf",
        filePath = "/tmp/Scan_1.pdf",
        pageCount = 2,
        fileSizeBytes = 1200,
        savedAt = 1000
    )

    fun solidExportPage(
        width: Int = 2,
        height: Int = 2,
        color: Int = 0xFFFFFFFF.toInt()
    ): ExportPage = ExportPage(
        width = width,
        height = height,
        pixels = IntArray(width * height) { color }
    )

    const val TEST_EMAIL = "test@example.com"
    const val TEST_PASSWORD = "password123"
    const val TEST_NAME = "Test User"
    const val TEST_PHONE = "+1234567890"
    const val TEST_WEBSITE = "https://test.com"
    const val INVALID_EMAIL = "invalid-email"
    const val INVALID_PASSWORD = "123"
    const val INVALID_NAME = ""
    const val INVALID_PHONE = "123"
    const val INVALID_WEBSITE = "not-a-website"
}
