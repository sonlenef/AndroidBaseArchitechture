package dev.sonle.pdfscanner.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.sonle.pdfscanner.util.ResponsiveUtils

/**
 * Loading indicator component
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    showBackground: Boolean = false
) {
    if (showBackground) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(ResponsiveUtils.getResponsiveIconSize()),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        CircularProgressIndicator(
            modifier = modifier.size(ResponsiveUtils.getResponsiveIconSize()),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
