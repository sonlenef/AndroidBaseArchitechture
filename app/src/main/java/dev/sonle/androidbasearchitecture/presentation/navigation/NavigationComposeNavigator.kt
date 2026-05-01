package dev.sonle.androidbasearchitecture.presentation.navigation

import androidx.navigation.NavHostController
import dev.sonle.androidbasearchitecture.domain.navigation.Navigator

class NavigationComposeNavigator(
    private val navController: NavHostController
) : Navigator {

    override fun navigateTo(route: Any) {
        navController.navigate(route)
    }

    override fun navigateBack() {
        navController.popBackStack()
    }

    override fun navigateBackTo(route: Any) {
        // Compose Navigation 2.8+ supports passing the object/class for popBackStack
        navController.popBackStack(route = route, inclusive = false)
    }

    override fun navigateAndClearStack(route: Any) {
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    override fun navigateAndPopUpTo(route: Any, popUpToRoute: Any) {
        navController.navigate(route) {
            popUpTo(popUpToRoute) { inclusive = true }
        }
    }
}
