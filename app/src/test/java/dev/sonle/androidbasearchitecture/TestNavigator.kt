package dev.sonle.androidbasearchitecture

import dev.sonle.androidbasearchitecture.domain.navigation.Navigator

/**
 * Test implementation of Navigator for unit tests
 */
class TestNavigator : Navigator {
    val navigationHistory = mutableListOf<String>()
    val backStackHistory = mutableListOf<String>()
    
    override fun navigateTo(route: String) {
        navigationHistory.add("navigateTo: $route")
    }
    
    override fun navigateTo(route: String, arguments: Map<String, Any>) {
        navigationHistory.add("navigateTo: $route with args: $arguments")
    }
    
    override fun navigateBack() {
        backStackHistory.add("navigateBack")
    }
    
    override fun navigateBackTo(route: String) {
        backStackHistory.add("navigateBackTo: $route")
    }
    
    override fun navigateAndClearStack(route: String) {
        navigationHistory.add("navigateAndClearStack: $route")
    }
    
    override fun navigateAndPopUpTo(route: String, popUpToRoute: String) {
        navigationHistory.add("navigateAndPopUpTo: $route, popUpTo: $popUpToRoute")
    }
    
    fun clear() {
        navigationHistory.clear()
        backStackHistory.clear()
    }
}
