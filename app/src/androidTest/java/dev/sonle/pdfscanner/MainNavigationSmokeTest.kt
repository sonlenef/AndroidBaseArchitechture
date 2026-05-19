package dev.sonle.pdfscanner

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainNavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_opensScanner_andReturnsBack() {
        composeRule.apply {
            onNodeWithText("My Documents").assertIsDisplayed()
            onNodeWithContentDescription("Scan").performClick()
            waitForIdle()
            onNodeWithContentDescription("Đóng").performClick()
            waitForIdle()
            onNodeWithText("My Documents").assertIsDisplayed()
        }
    }
}
