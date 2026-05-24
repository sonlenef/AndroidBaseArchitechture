package dev.sonle.pdfscanner.presentation.locale

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sonle.pdfscanner.core.locale.AppLocaleController
import dev.sonle.pdfscanner.core.locale.withAppLanguage
import dev.sonle.pdfscanner.core.util.findComponentActivity
import org.koin.compose.koinInject

/**
 * Provides a localized [LocalContext] so [androidx.compose.ui.res.stringResource]
 * updates when the app language changes, without restarting the Activity.
 */
@Composable
fun LocalizedAppContent(
    appLocaleController: AppLocaleController = koinInject(),
    content: @Composable () -> Unit
) {
    val appLanguage by appLocaleController.language.collectAsStateWithLifecycle()
    val baseContext = LocalContext.current
    val activity = remember(baseContext) { baseContext.findComponentActivity() }

    val localizedContext = remember(baseContext, appLanguage) {
        baseContext.withAppLanguage(appLanguage)
    }
    val localizedConfiguration = remember(localizedContext) {
        localizedContext.resources.configuration
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        LocalResources provides localizedContext.resources
    ) {
        if (activity != null) {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides activity
            ) {
                content()
            }
        } else {
            content()
        }
    }
}
