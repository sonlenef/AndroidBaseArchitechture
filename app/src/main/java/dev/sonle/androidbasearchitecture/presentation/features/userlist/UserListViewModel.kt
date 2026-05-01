package dev.sonle.androidbasearchitecture.presentation.features.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.androidbasearchitecture.core.network.NetworkResult
import dev.sonle.androidbasearchitecture.domain.model.User
import dev.sonle.androidbasearchitecture.domain.navigation.ProfileScreenRoute
import dev.sonle.androidbasearchitecture.domain.usecase.GetUsersUseCase
import dev.sonle.androidbasearchitecture.domain.usecase.RefreshUsersUseCase
import dev.sonle.androidbasearchitecture.domain.usecase.UpdateFavoriteStatusUseCase
import dev.sonle.androidbasearchitecture.presentation.navigation.NavigatorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for UserList screen using MVI architecture
 */
class UserListViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val refreshUsersUseCase: RefreshUsersUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    init {
        onAction(UserListAction.LoadUsers)
    }

    // The single entry point for all UI interactions
    fun onAction(action: UserListAction) {
        when (action) {
            is UserListAction.LoadUsers -> loadUsers()
            is UserListAction.RefreshUsers -> refreshUsers()
            is UserListAction.ToggleFavorite -> toggleFavorite(action.userId)
            is UserListAction.UserClicked -> onUserClick(action.userId)
            is UserListAction.ClearError -> clearError()
        }
    }
    
    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getUsersUseCase().collect { userList ->
                _users.value = userList
                _uiState.update { it.copy(isLoading = false, error = null) }
            }
        }
    }
    
    private fun refreshUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            
            when (val result = refreshUsersUseCase()) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isRefreshing = false, error = null) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isRefreshing = false, error = result.message) }
                }
                is NetworkResult.Loading -> {
                    // Keep refreshing state
                }
            }
        }
    }
    
    private fun toggleFavorite(userId: Long) {
        val user = _users.value.find { it.id == userId } ?: return
        val newFavoriteStatus = !user.isFavorite
        
        viewModelScope.launch {
            when (val result = updateFavoriteStatusUseCase(userId, newFavoriteStatus)) {
                is NetworkResult.Success -> {
                    // Update local state (Room flow usually handles this, but doing it proactively helps UI response time)
                    _users.update { currentUsers ->
                        currentUsers.map { u ->
                            if (u.id == userId) u.copy(isFavorite = newFavoriteStatus) else u
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = "Failed to update favorite status: ${result.message}") }
                }
                is NetworkResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }
    
    private fun onUserClick(userId: Long) {
        NavigatorManager.getNavigator().navigateTo(ProfileScreenRoute(userId = userId.toString()))
    }
    
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for UserList screen
 */
data class UserListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
