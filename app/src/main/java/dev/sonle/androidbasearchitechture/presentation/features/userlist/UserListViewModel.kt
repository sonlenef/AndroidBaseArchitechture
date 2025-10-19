package dev.sonle.androidbasearchitechture.ui.features.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.domain.model.User
import dev.sonle.androidbasearchitechture.domain.navigation.NavigationRoutes
import dev.sonle.androidbasearchitechture.domain.usecase.GetUsersUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.RefreshUsersUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.UpdateFavoriteStatusUseCase
import dev.sonle.androidbasearchitechture.presentation.navigation.NavigatorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for UserList screen
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val refreshUsersUseCase: RefreshUsersUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getUsersUseCase().collect { userList ->
                _users.value = userList
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            }
        }
    }
    
    fun refreshUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            when (val result = refreshUsersUseCase()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Keep refreshing state
                }
            }
        }
    }
    
    fun toggleFavorite(userId: Long) {
        val user = _users.value.find { it.id == userId } ?: return
        val newFavoriteStatus = !user.isFavorite
        
        viewModelScope.launch {
            when (val result = updateFavoriteStatusUseCase(userId, newFavoriteStatus)) {
                is NetworkResult.Success -> {
                    // Update local state
                    _users.value = _users.value.map { u ->
                        if (u.id == userId) u.copy(isFavorite = newFavoriteStatus) else u
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update favorite status: ${result.message}"
                    )
                }
                is NetworkResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }
    
    fun onUserClick(userId: Long) {
        NavigatorManager.getNavigator().navigateTo(NavigationRoutes.createProfileRoute(userId.toString()))
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
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
