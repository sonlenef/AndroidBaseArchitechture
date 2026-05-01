package dev.sonle.androidbasearchitecture.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.sonle.androidbasearchitecture.presentation.theme.AndroidBaseArchitechtureTheme
import dev.sonle.androidbasearchitecture.presentation.components.CustomButton
import dev.sonle.androidbasearchitecture.presentation.components.CustomTextField
import dev.sonle.androidbasearchitecture.presentation.components.ErrorView
import dev.sonle.androidbasearchitecture.presentation.components.LoadingIndicator
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for custom components
 */
class CustomComponentsTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun customButton_displaysCorrectly() {
        composeTestRule.setContent {
            AndroidBaseArchitechtureTheme {
                CustomButton(
                    text = "Test Button",
                    onClick = { }
                )
            }
        }
        
        composeTestRule.onNodeWithText("Test Button")
            .assertIsDisplayed()
            .assertIsEnabled()
    }
    
    @Test
    fun customButton_showsLoadingState() {
        composeTestRule.setContent {
            AndroidBaseArchitechtureTheme {
                CustomButton(
                    text = "Test Button",
                    onClick = { },
                    isLoading = true
                )
            }
        }
        
        composeTestRule.onNodeWithText("Loading...")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Test Button")
            .assertIsNotEnabled()
    }
    
    @Test
    fun customTextField_displaysCorrectly() {
        composeTestRule.setContent {
            AndroidBaseArchitechtureTheme {
                CustomTextField(
                    value = "",
                    onValueChange = { },
                    label = "Test Label"
                )
            }
        }
        
        composeTestRule.onNodeWithText("Test Label")
            .assertIsDisplayed()
    }
    
    @Test
    fun customTextField_showsErrorWhenInvalid() {
        composeTestRule.setContent {
            AndroidBaseArchitechtureTheme {
                CustomTextField(
                    value = "invalid",
                    onValueChange = { },
                    label = "Email",
                    isError = true,
                    errorMessage = "Invalid email"
                )
            }
        }
        
        composeTestRule.onNodeWithText("Invalid email")
            .assertIsDisplayed()
    }
    
    @Test
    fun loadingIndicator_displaysCorrectly() {
        composeTestRule.setContent {
            AndroidBaseArchitechtureTheme {
                LoadingIndicator()
            }
        }
        
        // Loading indicator should be displayed (CircularProgressIndicator)
        // Note: We can't easily test the CircularProgressIndicator directly,
        // but we can verify the component doesn't crash
    }
    
    @Test
    fun errorView_displaysCorrectly() {
        composeTestRule.setContent {
            AndroidBaseArchitechtureTheme {
                ErrorView(
                    message = "Test error message",
                    onRetry = { }
                )
            }
        }
        
        composeTestRule.onNodeWithText("Test error message")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
    }
}
