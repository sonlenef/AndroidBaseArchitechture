package dev.sonle.pdfscanner.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sonle.pdfscanner.core.config.EnvironmentConfig

/**
 * Environment badge component for non-production builds
 */
@Composable
fun EnvironmentBadge(
    modifier: Modifier = Modifier
) {
    if (!EnvironmentConfig.isProduction) {
        Surface(
            color = if (EnvironmentConfig.isDevelopment) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.secondary,
            shape = MaterialTheme.shapes.small,
            modifier = modifier.padding(8.dp)
        ) {
            Text(
                text = EnvironmentConfig.environment.uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
