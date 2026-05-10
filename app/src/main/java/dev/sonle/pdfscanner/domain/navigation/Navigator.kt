package dev.sonle.pdfscanner.domain.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation interface for abstracting navigation logic
 */
interface Navigator {
    /**
     * Navigate to a specific route using a typed object
     */
    fun navigateTo(route: Any)
    
    /**
     * Navigate back to the previous screen
     */
    fun navigateBack()
    
    /**
     * Navigate back to a specific route, clearing the back stack
     */
    fun navigateBackTo(route: Any)
    
    /**
     * Navigate to a route and clear the entire back stack
     */
    fun navigateAndClearStack(route: Any)
    
    /**
     * Navigate to a route and clear the back stack up to a specific route
     */
    fun navigateAndPopUpTo(route: Any, popUpToRoute: Any)
}

/**
 * Type-Safe Navigation routes
 */
@Serializable
object LoginScreenRoute

@Serializable
object UserListScreenRoute

@Serializable
object MainScreenRoute

@Serializable
object ScannerScreenRoute

@Serializable
data class ProfileScreenRoute(val userId: String)

@Serializable
data class PdfViewerScreenRoute(val filePath: String)
