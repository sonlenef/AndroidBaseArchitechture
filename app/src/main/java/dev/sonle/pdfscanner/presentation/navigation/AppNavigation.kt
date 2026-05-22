package dev.sonle.pdfscanner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.sonle.pdfscanner.domain.navigation.MainScreenRoute
import dev.sonle.pdfscanner.domain.navigation.PdfViewerScreenRoute
import dev.sonle.pdfscanner.domain.navigation.ScannerScreenRoute
import dev.sonle.pdfscanner.core.analytics.TrackFirebaseScreen
import dev.sonle.pdfscanner.presentation.features.main.MainScreen
import dev.sonle.pdfscanner.presentation.features.scanner.ScannerScreen
import dev.sonle.pdfscanner.presentation.features.viewer.PdfViewerScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val navigator = rememberNavigator(navController)

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavHost(
            navController = navController,
            startDestination = MainScreenRoute
        ) {
            composable<MainScreenRoute> {
                TrackFirebaseScreen(screenName = "main", screenClass = "MainScreen")
                MainScreen(
                    onOpenScanner = { navigator.navigateTo(ScannerScreenRoute) }
                )
            }
            composable<ScannerScreenRoute> {
                TrackFirebaseScreen(screenName = "scanner", screenClass = "ScannerScreen")
                ScannerScreen(onNavigateBack = { navigator.navigateBack() })
            }
            composable<PdfViewerScreenRoute> { backStackEntry ->
                TrackFirebaseScreen(screenName = "pdf_viewer", screenClass = "PdfViewerScreen")
                val route = backStackEntry.toRoute<PdfViewerScreenRoute>()
                PdfViewerScreen(
                    filePath = route.filePath,
                    onBack = { navigator.navigateBack() }
                )
            }
        }
    }
}
