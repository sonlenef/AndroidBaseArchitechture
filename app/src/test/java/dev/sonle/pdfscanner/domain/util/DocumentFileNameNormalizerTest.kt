package dev.sonle.pdfscanner.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DocumentFileNameNormalizerTest {

    @Test
    fun `normalize should append pdf extension`() {
        assertEquals("Invoice.pdf", DocumentFileNameNormalizer.normalize("Invoice"))
    }

    @Test
    fun `normalize should sanitize invalid characters`() {
        assertEquals("Tax_Report.pdf", DocumentFileNameNormalizer.normalize("Tax/Report"))
    }

    @Test
    fun `normalize should return null for blank input`() {
        assertNull(DocumentFileNameNormalizer.normalize("   "))
    }

    @Test
    fun `displayNameWithoutExtension should strip extension`() {
        assertEquals("Scan_001", DocumentFileNameNormalizer.displayNameWithoutExtension("Scan_001.pdf"))
    }
}
