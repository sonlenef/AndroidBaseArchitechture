package dev.sonle.pdfscanner.presentation.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.core.network.NetworkResult
import dev.sonle.pdfscanner.core.util.ValidationUtils
import dev.sonle.pdfscanner.domain.navigation.UserListScreenRoute
import dev.sonle.pdfscanner.domain.usecase.LoginUseCase
import dev.sonle.pdfscanner.presentation.navigation.NavigatorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Login screen using MVI pattern
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    // The single entry point for all UI interactions
    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> {
                _uiState.update { it.copy(email = action.email, emailError = null) }
            }
            is LoginAction.PasswordChanged -> {
                _uiState.update { it.copy(password = action.password, passwordError = null) }
            }
            is LoginAction.LoginClicked -> handleLogin()
        }
    }
    
    private fun handleLogin() {
        val currentState = _uiState.value
        
        // Validate inputs
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        
        if (emailError != null || passwordError != null) {
            _uiState.update { 
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }
        
        // Start login process
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            loginUseCase(currentState.email, currentState.password)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is NetworkResult.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    isLoginSuccessful = true,
                                    error = null
                                )
                            }
                            // Navigate to UserList screen using Type-Safe route
                            NavigatorManager.getNavigator().navigateAndClearStack(UserListScreenRoute)
                        }
                        is NetworkResult.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                }
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !ValidationUtils.isValidEmail(email) -> "Invalid email format"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            !ValidationUtils.isValidPassword(password) -> "Password must be at least 6 characters"
            else -> null
        }
    }
}

/**
 * UI state for Login screen
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null
)
