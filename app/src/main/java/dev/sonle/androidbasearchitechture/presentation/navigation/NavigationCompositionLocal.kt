package dev.sonle.androidbasearchitechture.presentation.navigation

import androidx.compose.runtime.compositionLocalOf
import dev.sonle.androidbasearchitechture.domain.navigation.Navigator

/**
 * CompositionLocal for providing Navigator to the composition tree
 * This allows ViewModels to access Navigator without direct injection
 */
val LocalNavigator = compositionLocalOf<Navigator> {
    error("No Navigator provided")
}
