package dev.sonle.pdfscanner.presentation.features.scanner

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun documentDetectionOverlay_displaysOverlay_whenDocumentCornersDetected() {
        composeTestRule.setContent {
            DocumentDetectionOverlay(
                quad = DocumentQuad(
                    tl = NormalizedPoint(0.2f, 0.2f),
                    tr = NormalizedPoint(0.8f, 0.2f),
                    br = NormalizedPoint(0.8f, 0.9f),
                    bl = NormalizedPoint(0.2f, 0.9f),
                    confidence = 0.9f
                )
            )
        }

        composeTestRule.onNodeWithTag("pdfDetectionOverlay").assertIsDisplayed()
    }
}
