package dev.sonle.androidbasearchitechture.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.sonle.androidbasearchitechture.domain.navigation.Navigator

/**
 * Provider for Navigator that can be used in ViewModels
 * This creates a bridge between Compose and ViewModels
 */
@Composable
fun rememberNavigatorProvider(navController: NavController = rememberNavController()): Navigator {
    return remember(navController) {
        NavigationComposeNavigator(navController)
    }
}
