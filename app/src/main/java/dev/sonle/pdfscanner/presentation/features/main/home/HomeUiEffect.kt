package dev.sonle.pdfscanner.presentation.features.main.home

import androidx.annotation.StringRes

sealed interface HomeUiEffect {
    data class ShowMessage(@StringRes val messageResId: Int) : HomeUiEffect
    data class DocumentsDeleted(val count: Int) : HomeUiEffect
}
