package dev.sonle.pdfscanner

import dev.sonle.pdfscanner.domain.navigation.Navigator

/**
 * Test implementation of Navigator for unit tests
 */
class TestNavigator : Navigator {
    val navigationHistory = mutableListOf<String>()
    val backStackHistory = mutableListOf<String>()
    
    override fun navigateTo(route: Any) {
        navigationHistory.add("navigateTo: $route")
    }
    
    override fun navigateBack() {
        backStackHistory.add("navigateBack")
    }
    
    override fun navigateBackTo(route: Any) {
        backStackHistory.add("navigateBackTo: $route")
    }
    
    override fun navigateAndClearStack(route: Any) {
        navigationHistory.add("navigateAndClearStack: $route")
    }
    
    override fun navigateAndPopUpTo(route: Any, popUpToRoute: Any) {
        navigationHistory.add("navigateAndPopUpTo: $route, popUpTo: $popUpToRoute")
    }
    
    fun clear() {
        navigationHistory.clear()
        backStackHistory.clear()
    }
}
