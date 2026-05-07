package dev.sonle.pdfscanner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.sonle.pdfscanner.domain.navigation.LoginScreenRoute
import dev.sonle.pdfscanner.domain.navigation.ProfileScreenRoute
import dev.sonle.pdfscanner.domain.navigation.ScannerScreenRoute
import dev.sonle.pdfscanner.domain.navigation.UserListScreenRoute
import dev.sonle.pdfscanner.presentation.features.login.LoginScreen
import dev.sonle.pdfscanner.presentation.features.profile.ProfileScreen
import dev.sonle.pdfscanner.presentation.features.userlist.UserListScreen
import dev.sonle.pdfscanner.presentation.features.scanner.ScannerScreen

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
            startDestination = UserListScreenRoute
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

            composable<ScannerScreenRoute> {
                ScannerScreen(onNavigateBack = { navigator.navigateBack() })
            }
        }
    }
}
