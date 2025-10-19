package dev.sonle.androidbasearchitechture.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sonle.androidbasearchitechture.util.ResponsiveUtils

/**
 * Error view component with retry functionality
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true
) {
    if (showBackground) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ErrorContent(
                message = message,
                onRetry = onRetry
            )
        }
    } else {
        ErrorContent(
            message = message,
            onRetry = onRetry,
            modifier = modifier
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(ResponsiveUtils.getResponsivePadding()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getResponsiveSpacing())
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            onRetry?.let {
                Button(
                    onClick = it,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Retry",
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}
