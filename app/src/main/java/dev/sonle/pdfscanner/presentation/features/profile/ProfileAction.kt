package dev.sonle.pdfscanner.presentation.features.profile

sealed interface ProfileAction {
    data class LoadUser(val userId: Long) : ProfileAction
    object ToggleEditMode : ProfileAction
    data class NameChanged(val name: String) : ProfileAction
    data class EmailChanged(val email: String) : ProfileAction
    data class PhoneChanged(val phone: String) : ProfileAction
    data class WebsiteChanged(val website: String) : ProfileAction
    object SaveUser : ProfileAction
    object CancelEdit : ProfileAction
    object NavigateBack : ProfileAction
    object SummarizeProfile : ProfileAction
}
