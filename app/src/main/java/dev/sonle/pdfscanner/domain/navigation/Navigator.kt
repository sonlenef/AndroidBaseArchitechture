package dev.sonle.pdfscanner.domain.navigation

import kotlinx.serialization.Serializable

interface Navigator {
    fun navigateTo(route: Any)
    fun navigateBack()
    fun navigateBackTo(route: Any)
    fun navigateAndClearStack(route: Any)
    fun navigateAndPopUpTo(route: Any, popUpToRoute: Any)
}

@Serializable
object MainScreenRoute

@Serializable
object ScannerScreenRoute

@Serializable
data class PdfViewerScreenRoute(val filePath: String)
