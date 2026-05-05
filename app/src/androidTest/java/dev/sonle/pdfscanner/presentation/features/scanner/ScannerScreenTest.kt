package dev.sonle.pdfscanner.presentation.features.scanner

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun scannerScreen_displaysScanButton_whenIdle() {
        val mockViewModel = mockk<ScannerViewModel>(relaxed = true)
        
        composeTestRule.setContent {
            ScannerScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Kiểm tra xem nút SCAN có hiển thị không
        composeTestRule.onNodeWithText("SCAN").assertIsDisplayed()
    }
}
