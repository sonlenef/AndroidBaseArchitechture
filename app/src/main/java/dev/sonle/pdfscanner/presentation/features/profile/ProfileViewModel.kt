package dev.sonle.pdfscanner.presentation.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.core.network.NetworkResult
import dev.sonle.pdfscanner.core.util.ValidationUtils
import dev.sonle.pdfscanner.domain.model.User
import dev.sonle.pdfscanner.domain.usecase.GetUserByIdUseCase
import dev.sonle.pdfscanner.domain.usecase.UpdateUserUseCase
import dev.sonle.pdfscanner.domain.usecase.ai.SummarizeUserUseCase
import dev.sonle.pdfscanner.presentation.navigation.NavigatorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Profile screen using MVI architecture
 */
class ProfileViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val summarizeUserUseCase: SummarizeUserUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    // The single entry point for all UI interactions
    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.LoadUser -> loadUser(action.userId)
            is ProfileAction.ToggleEditMode -> toggleEditMode()
            is ProfileAction.NameChanged -> onNameChange(action.name)
            is ProfileAction.EmailChanged -> onEmailChange(action.email)
            is ProfileAction.PhoneChanged -> onPhoneChange(action.phone)
            is ProfileAction.WebsiteChanged -> onWebsiteChange(action.website)
            is ProfileAction.SaveUser -> saveUser()
            is ProfileAction.CancelEdit -> cancelEdit()
            is ProfileAction.NavigateBack -> navigateBack()
            is ProfileAction.SummarizeProfile -> summarizeProfile()
        }
    }
    
    private fun loadUser(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getUserByIdUseCase(userId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                user = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
    
    private fun toggleEditMode() {
        _uiState.update { 
            it.copy(
                isEditMode = !it.isEditMode,
                editedUser = it.user?.copy()
            )
        }
    }
    
    private fun onNameChange(name: String) {
        _uiState.update { 
            it.copy(
                editedUser = it.editedUser?.copy(name = name),
                nameError = null
            )
        }
    }
    
    private fun onEmailChange(email: String) {
        _uiState.update { 
            it.copy(
                editedUser = it.editedUser?.copy(email = email),
                emailError = null
            )
        }
    }
    
    private fun onPhoneChange(phone: String) {
        _uiState.update { 
            it.copy(
                editedUser = it.editedUser?.copy(phone = phone),
                phoneError = null
            )
        }
    }
    
    private fun onWebsiteChange(website: String) {
        _uiState.update { 
            it.copy(
                editedUser = it.editedUser?.copy(website = website),
                websiteError = null
            )
        }
    }
    
    private fun saveUser() {
        val editedUser = _uiState.value.editedUser ?: return
        
        // Validate inputs
        val nameError = validateName(editedUser.name)
        val emailError = validateEmail(editedUser.email)
        val phoneError = validatePhone(editedUser.phone)
        val websiteError = validateWebsite(editedUser.website)
        
        if (nameError != null || emailError != null || phoneError != null || websiteError != null) {
            _uiState.update { 
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    websiteError = websiteError
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            when (val result = updateUserUseCase(editedUser)) {
                is NetworkResult.Success -> {
                    _uiState.update { 
                        it.copy(
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
                }
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            error = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    _uiState.update { it.copy(isSaving = true) }
                }
            }
        }
    }
    
    private fun cancelEdit() {
        _uiState.update { 
            it.copy(
                isEditMode = false,
                editedUser = null,
                nameError = null,
                emailError = null,
                phoneError = null,
                websiteError = null
            )
        }
    }
    
    private fun navigateBack() {
        NavigatorManager.getNavigator().navigateBack()
    }

    private fun summarizeProfile() {
        val currentUser = _uiState.value.user ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, aiSummary = null) }
            
            summarizeUserUseCase(currentUser).collect { result ->
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isSummarizing = false, 
                            aiSummary = result.getOrNull()
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isSummarizing = false, 
                            error = "AI Summarization failed: ${result.exceptionOrNull()?.message}"
                        ) 
                    }
                }
            }
        }
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
    val websiteError: String? = null,
    val isSummarizing: Boolean = false,
    val aiSummary: String? = null
)
