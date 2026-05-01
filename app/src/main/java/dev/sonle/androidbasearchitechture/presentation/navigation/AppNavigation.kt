package dev.sonle.androidbasearchitechture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sonle.androidbasearchitechture.domain.navigation.NavigationRoutes
import dev.sonle.androidbasearchitechture.presentation.navigation.LocalNavigator
import dev.sonle.androidbasearchitechture.presentation.navigation.NavigatorManager
import dev.sonle.androidbasearchitechture.presentation.navigation.rememberNavigator
import dev.sonle.androidbasearchitechture.ui.features.login.LoginScreen
import dev.sonle.androidbasearchitechture.ui.features.userlist.UserListScreen
import dev.sonle.androidbasearchitechture.ui.features.profile.ProfileScreen

/**
 * Main navigation composable that defines the app's navigation graph
 */
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    // Provide Navigator to the composition
    val navigator = rememberNavigator(navController)

    // Set navigator in manager for ViewModels to access
    NavigatorManager.setNavigator(navigator)
    
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.LOGIN
        ) {
            composable(NavigationRoutes.LOGIN) {
                LoginScreen()
            }
            
            composable(NavigationRoutes.USER_LIST) {
                UserListScreen()
            }
            
            composable(
                route = NavigationRoutes.PROFILE,
                arguments = Screen.Profile.arguments
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                ProfileScreen(userId = userId)
            }
        }
    }
}
