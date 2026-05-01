package dev.sonle.androidbasearchitecture.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.sonle.androidbasearchitecture.domain.navigation.LoginScreenRoute
import dev.sonle.androidbasearchitecture.domain.navigation.ProfileScreenRoute
import dev.sonle.androidbasearchitecture.domain.navigation.UserListScreenRoute
import dev.sonle.androidbasearchitecture.presentation.features.login.LoginScreen
import dev.sonle.androidbasearchitecture.presentation.features.profile.ProfileScreen
import dev.sonle.androidbasearchitecture.presentation.features.userlist.UserListScreen

/**
 * Main navigation composable that defines the app's navigation graph
 * Upgraded to Type-Safe Navigation (Compose 2.8+)
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
            startDestination = LoginScreenRoute
        ) {
            composable<LoginScreenRoute> {
                LoginScreen()
            }
            
            composable<UserListScreenRoute> {
                UserListScreen()
            }
            
            composable<ProfileScreenRoute> { backStackEntry ->
                val profileArgs = backStackEntry.toRoute<ProfileScreenRoute>()
                ProfileScreen(userId = profileArgs.userId)
            }
        }
    }
}
