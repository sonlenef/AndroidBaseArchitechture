package dev.sonle.androidbasearchitechture.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.core.util.ValidationUtils
import dev.sonle.androidbasearchitechture.domain.navigation.NavigationRoutes
import dev.sonle.androidbasearchitechture.domain.usecase.LoginUseCase
import dev.sonle.androidbasearchitechture.presentation.navigation.NavigatorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Login screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null
        )
    }
    
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }
    
    fun onLoginClick() {
        val currentState = _uiState.value
        
        // Validate inputs
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        
        if (emailError != null || passwordError != null) {
            _uiState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }
        
        // Start login process
        _uiState.value = currentState.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            loginUseCase(currentState.email, currentState.password)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is NetworkResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoginSuccessful = true,
                                error = null
                            )
                            // Navigate to UserList screen using Navigator interface
                            NavigatorManager.getNavigator().navigateAndClearStack(NavigationRoutes.USER_LIST)
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
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
