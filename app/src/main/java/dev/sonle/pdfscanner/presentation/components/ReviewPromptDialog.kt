package dev.sonle.pdfscanner.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.sonle.pdfscanner.R

@Composable
fun ReviewPromptDialog(
    adsEnabled: Boolean,
    onRate: () -> Unit,
    onLater: () -> Unit,
    onDismissRequest: () -> Unit = onLater
) {
    val messageResId = if (adsEnabled) {
        R.string.review_prompt_message_with_ads
    } else {
        R.string.review_prompt_message_no_ads
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(
                    R.string.review_prompt_title,
                    stringResource(R.string.app_name)
                ),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(messageResId),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onRate) {
                Text(stringResource(R.string.review_prompt_rate))
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text(stringResource(R.string.review_prompt_later))
            }
        }
    )
}
