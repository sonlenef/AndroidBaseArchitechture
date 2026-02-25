package dev.sonle.androidbasearchitechture.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.core.util.ValidationUtils
import dev.sonle.androidbasearchitechture.domain.model.User
import dev.sonle.androidbasearchitechture.domain.usecase.GetUserByIdUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.UpdateUserUseCase
import dev.sonle.androidbasearchitechture.presentation.navigation.NavigatorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * ViewModel for Profile screen
 */
class ProfileViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getUserByIdUseCase(userId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            user = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }
    
    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
            editedUser = _uiState.value.user?.copy()
        )
    }
    
    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            editedUser = _uiState.value.editedUser?.copy(name = name),
            nameError = null
        )
    }
    
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            editedUser = _uiState.value.editedUser?.copy(email = email),
            emailError = null
        )
    }
    
    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(
            editedUser = _uiState.value.editedUser?.copy(phone = phone),
            phoneError = null
        )
    }
    
    fun onWebsiteChange(website: String) {
        _uiState.value = _uiState.value.copy(
            editedUser = _uiState.value.editedUser?.copy(website = website),
            websiteError = null
        )
    }
    
    fun saveUser() {
        val editedUser = _uiState.value.editedUser ?: return
        
        // Validate inputs
        val nameError = validateName(editedUser.name)
        val emailError = validateEmail(editedUser.email)
        val phoneError = validatePhone(editedUser.phone)
        val websiteError = validateWebsite(editedUser.website)
        
        if (nameError != null || emailError != null || phoneError != null || websiteError != null) {
            _uiState.value = _uiState.value.copy(
                nameError = nameError,
                emailError = emailError,
                phoneError = phoneError,
                websiteError = websiteError
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            when (val result = updateUserUseCase(editedUser)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        user = editedUser,
                        isEditMode = false,
                        isSaving = false,
                        error = null,
                        nameError = null,
                        emailError = null,
                        phoneError = null,
                        websiteError = null
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isSaving = true)
                }
            }
        }
    }
    
    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            editedUser = null,
            nameError = null,
            emailError = null,
            phoneError = null,
            websiteError = null
        )
    }
    
    fun navigateBack() {
        NavigatorManager.getNavigator().navigateBack()
    }
    
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            !ValidationUtils.isValidName(name) -> "Invalid name format"
            else -> null
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !ValidationUtils.isValidEmail(email) -> "Invalid email format"
            else -> null
        }
    }
    
    private fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "Phone is required"
            !ValidationUtils.isValidPhone(phone) -> "Invalid phone format"
            else -> null
        }
    }
    
    private fun validateWebsite(website: String): String? {
        return when {
            website.isBlank() -> "Website is required"
            !ValidationUtils.isValidWebsite(website) -> "Invalid website format"
            else -> null
        }
    }
}

/**
 * UI state for Profile screen
 */
data class ProfileUiState(
    val user: User? = null,
    val editedUser: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val websiteError: String? = null
)
