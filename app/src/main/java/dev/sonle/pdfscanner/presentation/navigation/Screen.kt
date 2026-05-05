package dev.sonle.pdfscanner.presentation.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Sealed class defining all navigation routes in the app
 */
sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Login : Screen("login")
    
    object UserList : Screen("user_list")

    object Scanner : Screen("scanner")
    
    object Profile : Screen(
        route = "profile/{userId}",
        arguments = listOf(
            navArgument("userId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(userId: String) = "profile/$userId"
    }
}
