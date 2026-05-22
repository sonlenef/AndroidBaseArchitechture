package dev.sonle.pdfscanner.presentation.features.main.home

import dev.sonle.pdfscanner.domain.model.RecentScan
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDocumentFilterTest {

    private val scans = listOf(
        RecentScan(1, "Invoice_March.pdf", "/a.pdf", 1, 100, 1),
        RecentScan(2, "Receipt_2024.pdf", "/b.pdf", 2, 200, 2),
        RecentScan(3, "scan-notes.PDF", "/c.pdf", 1, 50, 3)
    )

    @Test
    fun `filter should return all scans when query is blank`() {
        assertEquals(scans, HomeDocumentFilter.filter(scans, ""))
        assertEquals(scans, HomeDocumentFilter.filter(scans, "   "))
    }

    @Test
    fun `filter should match file name case insensitively`() {
        assertEquals(listOf(scans[0]), HomeDocumentFilter.filter(scans, "invoice"))
        assertEquals(listOf(scans[2]), HomeDocumentFilter.filter(scans, "notes"))
    }

    @Test
    fun `filter should return empty list when nothing matches`() {
        assertEquals(emptyList<RecentScan>(), HomeDocumentFilter.filter(scans, "contract"))
    }
}
