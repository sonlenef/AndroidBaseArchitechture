package dev.sonle.androidbasearchitecture.presentation.navigation

import dev.sonle.androidbasearchitecture.domain.navigation.Navigator

/**
 * Singleton manager for Navigator instance
 * This allows ViewModels to access Navigator without direct injection
 */
object NavigatorManager {
    private var navigator: Navigator? = null
    
    fun setNavigator(navigator: Navigator) {
        this.navigator = navigator
    }
    
    fun getNavigator(): Navigator {
        return navigator ?: throw IllegalStateException("Navigator not initialized")
    }
    
    fun clear() {
        navigator = null
    }
}
