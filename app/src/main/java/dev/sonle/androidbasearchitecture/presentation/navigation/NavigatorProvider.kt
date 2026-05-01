package dev.sonle.androidbasearchitecture.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import dev.sonle.androidbasearchitecture.domain.navigation.Navigator

@Composable
fun rememberNavigator(navController: NavHostController): Navigator {
    return remember(navController) {
        NavigationComposeNavigator(navController)
    }
}
