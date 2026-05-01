package dev.sonle.androidbasearchitecture.presentation.features.login

sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    object LoginClicked : LoginAction
}
