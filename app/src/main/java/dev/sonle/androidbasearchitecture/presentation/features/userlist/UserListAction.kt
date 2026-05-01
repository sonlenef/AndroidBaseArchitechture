package dev.sonle.androidbasearchitecture.presentation.features.userlist

sealed interface UserListAction {
    object LoadUsers : UserListAction
    object RefreshUsers : UserListAction
    data class ToggleFavorite(val userId: Long) : UserListAction
    data class UserClicked(val userId: Long) : UserListAction
    object ClearError : UserListAction
}
