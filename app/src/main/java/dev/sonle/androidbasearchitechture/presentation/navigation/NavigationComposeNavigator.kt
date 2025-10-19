package dev.sonle.androidbasearchitechture.presentation.navigation

import androidx.navigation.NavController
import dev.sonle.androidbasearchitechture.domain.navigation.Navigator
import dev.sonle.androidbasearchitechture.domain.navigation.NavigationRoutes

/**
 * Navigation Compose implementation of Navigator interface
 */
class NavigationComposeNavigator(
    private val navController: NavController
) : Navigator {
    
    override fun navigateTo(route: String) {
        navController.navigate(route)
    }
    
    override fun navigateTo(route: String, arguments: Map<String, Any>) {
        // Convert arguments to NavController format
        val navArguments = arguments.mapValues { (_, value) ->
            when (value) {
                is String -> value
                is Int -> value.toString()
                is Long -> value.toString()
                is Boolean -> value.toString()
                else -> value.toString()
            }
        }
        
        // For Navigation Compose, we need to handle arguments differently
        // This is a simplified implementation - in practice, you'd need to
        // handle different argument types more carefully
        navController.navigate(route)
    }
    
    override fun navigateBack() {
        navController.popBackStack()
    }
    
    override fun navigateBackTo(route: String) {
        navController.popBackStack(route, inclusive = false)
    }
    
    override fun navigateAndClearStack(route: String) {
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }
    
    override fun navigateAndPopUpTo(route: String, popUpToRoute: String) {
        navController.navigate(route) {
            popUpTo(popUpToRoute) { inclusive = true }
        }
    }
}
