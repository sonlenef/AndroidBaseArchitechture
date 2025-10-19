package dev.sonle.androidbasearchitechture.domain.navigation

/**
 * Navigation interface for abstracting navigation logic
 * This allows easy switching between different navigation implementations
 * (Navigation Compose, Voyager, etc.) without changing feature code
 */
interface Navigator {
    
    /**
     * Navigate to a specific route
     * @param route The destination route
     */
    fun navigateTo(route: String)
    
    /**
     * Navigate to a specific route with arguments
     * @param route The destination route
     * @param arguments Map of arguments to pass to the destination
     */
    fun navigateTo(route: String, arguments: Map<String, Any> = emptyMap())
    
    /**
     * Navigate back to the previous screen
     */
    fun navigateBack()
    
    /**
     * Navigate back to a specific route, clearing the back stack
     * @param route The destination route
     */
    fun navigateBackTo(route: String)
    
    /**
     * Navigate to a route and clear the entire back stack
     * @param route The destination route
     */
    fun navigateAndClearStack(route: String)
    
    /**
     * Navigate to a route and clear the back stack up to a specific route
     * @param route The destination route
     * @param popUpToRoute The route to pop back to (inclusive)
     */
    fun navigateAndPopUpTo(route: String, popUpToRoute: String)
}

/**
 * Navigation routes constants
 */
object NavigationRoutes {
    const val LOGIN = "login"
    const val USER_LIST = "user_list"
    const val PROFILE = "profile/{userId}"
    
    /**
     * Create profile route with user ID
     */
    fun createProfileRoute(userId: String): String = "profile/$userId"
}
