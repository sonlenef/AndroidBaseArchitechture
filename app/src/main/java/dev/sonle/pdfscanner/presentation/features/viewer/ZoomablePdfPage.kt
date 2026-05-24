package dev.sonle.pdfscanner.presentation.features.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import dev.sonle.pdfscanner.R

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f
private const val DOUBLE_TAP_ZOOM = 2.5f

/**
 * At 1x zoom there is no drag/pinch handler so [HorizontalPager] receives horizontal swipes.
 * When zoomed in, pinch/pan is enabled and the user should reset zoom (double-tap) to change pages.
 */
@Composable
fun ZoomablePdfPage(
    image: ImageBitmap,
    pageIndex: Int,
    modifier: Modifier = Modifier,
) {
    var scale by remember(pageIndex) { mutableFloatStateOf(1f) }
    var offset by remember(pageIndex) { mutableStateOf(Offset.Zero) }

    val imageModifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offset.x
            translationY = offset.y
        }
        .pointerInput(pageIndex) {
            detectTapGestures(
                onDoubleTap = {
                    if (scale > 1f) {
                        scale = 1f
                        offset = Offset.Zero
                    } else {
                        scale = DOUBLE_TAP_ZOOM
                    }
                }
            )
        }
        .then(
            if (scale > 1f) {
                Modifier.pointerInput(pageIndex, scale) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                        scale = newScale
                        offset = if (newScale <= 1f) {
                            Offset.Zero
                        } else {
                            offset + pan
                        }
                    }
                }
            } else {
                Modifier
            }
        )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = image,
            contentDescription = stringResource(R.string.viewer_page_content_desc, pageIndex + 1),
            modifier = imageModifier,
            contentScale = ContentScale.Fit
        )
    }
}
