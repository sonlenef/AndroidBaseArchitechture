package dev.sonle.pdfscanner.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.model.ThemeMode
import dev.sonle.pdfscanner.domain.usecase.ObserveAppSettingsUseCase
import dev.sonle.pdfscanner.presentation.components.MaintenanceModeGate
import dev.sonle.pdfscanner.presentation.locale.LocalizedAppContent
import dev.sonle.pdfscanner.presentation.navigation.AppNavigation
import dev.sonle.pdfscanner.presentation.theme.AndroidBaseArchitechtureTheme
import org.koin.compose.koinInject

@Composable
fun PdfScannerApp(
    observeAppSettingsUseCase: ObserveAppSettingsUseCase = koinInject()
) {
    val settings by observeAppSettingsUseCase()
        .collectAsStateWithLifecycle(initialValue = AppSettings.Default)

    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    LocalizedAppContent {
        AndroidBaseArchitechtureTheme(
            darkTheme = darkTheme,
            dynamicColor = settings.useDynamicColor
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MaintenanceModeGate {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
