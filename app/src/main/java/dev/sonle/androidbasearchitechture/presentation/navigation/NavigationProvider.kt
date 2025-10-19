package dev.sonle.androidbasearchitechture.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.sonle.androidbasearchitechture.domain.navigation.Navigator

/**
 * Composable function to provide Navigator instance
 * This allows easy switching between different navigation implementations
 */
@Composable
fun rememberNavigator(navController: NavController = rememberNavController()): Navigator {
    return remember(navController) {
        NavigationComposeNavigator(navController)
    }
}
