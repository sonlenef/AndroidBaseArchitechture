package dev.sonle.androidbasearchitechture.presentation.features.login

import app.cash.turbine.test
import dev.sonle.androidbasearchitechture.TestData
import dev.sonle.androidbasearchitechture.TestNavigator
import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.domain.usecase.LoginUseCase
import dev.sonle.androidbasearchitechture.presentation.navigation.NavigatorManager
import dev.sonle.androidbasearchitechture.ui.features.login.LoginViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for LoginViewModel
 */
@ExperimentalCoroutinesApi
class LoginViewModelTest {
    
    @get:Rule
    val testDispatcherRule = dev.sonle.androidbasearchitechture.StandardTestDispatcherRule()
    
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel
    private lateinit var testNavigator: TestNavigator
    
    @Before
    fun setup() {
        loginUseCase = mockk()
        testNavigator = TestNavigator()
        NavigatorManager.setNavigator(testNavigator)
        viewModel = LoginViewModel(loginUseCase)
    }
    
    @After
    fun tearDown() {
        NavigatorManager.clear()
        testNavigator.clear()
    }
    
    @Test
    fun `initial state should have empty values`() {
        val initialState = viewModel.uiState.value
        
        assertEquals("", initialState.email)
        assertEquals("", initialState.password)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isLoginSuccessful)
        assertNull(initialState.emailError)
        assertNull(initialState.passwordError)
        assertNull(initialState.error)
    }
    
    @Test
    fun `onEmailChange should update email and clear error`() {
        val email = TestData.TEST_EMAIL
        
        viewModel.onEmailChange(email)
        
        val state = viewModel.uiState.value
        assertEquals(email, state.email)
        assertNull(state.emailError)
    }
    
    @Test
    fun `onPasswordChange should update password and clear error`() {
        val password = TestData.TEST_PASSWORD
        
        viewModel.onPasswordChange(password)
        
        val state = viewModel.uiState.value
        assertEquals(password, state.password)
        assertNull(state.passwordError)
    }
    
    @Test
    fun `onLoginClick should show validation errors for invalid input`() = runTest {
        // Given
        val invalidEmail = TestData.INVALID_EMAIL
        val shortPassword = TestData.INVALID_PASSWORD
        
        // When
        viewModel.onEmailChange(invalidEmail)
        viewModel.onPasswordChange(shortPassword)
        viewModel.onLoginClick()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.emailError?.contains("Invalid email format") == true)
            assertTrue(state.passwordError?.contains("Password must be at least") == true)
            assertFalse(state.isLoading)
        }
    }
    
    @Test
    fun `onLoginClick should start loading for valid input`() = runTest {
        // Given
        val email = TestData.TEST_EMAIL
        val password = TestData.TEST_PASSWORD
        coEvery { loginUseCase(email, password) } returns flowOf(
            NetworkResult.Loading,
            NetworkResult.Success(
                dev.sonle.androidbasearchitechture.domain.usecase.LoginResult(
                    userId = 1L,
                    email = email,
                    name = "Test User",
                    isSuccess = true
                )
            )
        )
        
        // When
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onLoginClick()
        
        // Then
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.emailError)
            assertNull(loadingState.passwordError)
            
            val successState = awaitItem()
            assertTrue(successState.isLoginSuccessful)
            assertFalse(successState.isLoading)
            assertNull(successState.error)
        }
        
        // Verify navigation was called
        assertTrue(testNavigator.navigationHistory.contains("navigateAndClearStack: user_list"))
    }
    
    @Test
    fun `onLoginClick should show error on login failure`() = runTest {
        // Given
        val email = TestData.TEST_EMAIL
        val password = TestData.TEST_PASSWORD
        val errorMessage = "Login failed"
        coEvery { loginUseCase(email, password) } returns flowOf(
            NetworkResult.Loading,
            NetworkResult.Error(errorMessage)
        )
        
        // When
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onLoginClick()
        
        // Then
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            
            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(errorMessage, errorState.error)
        }
    }
}
