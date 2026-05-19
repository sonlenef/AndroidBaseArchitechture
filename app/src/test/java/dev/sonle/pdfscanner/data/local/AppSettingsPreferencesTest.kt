package dev.sonle.pdfscanner.data.local

import dev.sonle.pdfscanner.domain.model.DocumentFilterPreset
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality
import dev.sonle.pdfscanner.domain.model.ScanPageLayout
import dev.sonle.pdfscanner.domain.model.ScannerCaptureMode
import dev.sonle.pdfscanner.domain.model.ThemeMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AppSettingsPreferencesTest {

    @Test
    fun `writeSettings should persist and read back values`() = runTest {
        val preferences = AppSettingsPreferences(RuntimeEnvironment.getApplication())
        val settings = dev.sonle.pdfscanner.domain.model.AppSettings(
            themeMode = ThemeMode.DARK,
            useDynamicColor = false,
            defaultCaptureMode = ScannerCaptureMode.MANUAL,
            defaultPageLayout = ScanPageLayout.MULTI,
            defaultFilter = DocumentFilterPreset.BLACK_WHITE,
            pdfOutputQuality = PdfOutputQuality.HIGH
        )

        preferences.writeSettings(settings)

        assertEquals(settings, preferences.readSettings())
    }
}
