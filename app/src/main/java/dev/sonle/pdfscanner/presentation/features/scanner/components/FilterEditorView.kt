package dev.sonle.pdfscanner.presentation.features.scanner.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack

import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.scanner.processing.ImageFilterProcessor
import dev.sonle.pdfscanner.presentation.features.scanner.model.ImageFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FilterEditorView(
    croppedBitmap: Bitmap,
    previewBitmap: Bitmap,
    selectedFilter: ImageFilter,
    rotation: Int,
    onFilterSelected: (ImageFilter) -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate filter thumbnails lazily
    val filterThumbnails = remember(croppedBitmap) {
        mutableStateMapOf<ImageFilter, Bitmap>()
    }

    // Generate thumbnails in background
    LaunchedEffect(croppedBitmap) {
        ImageFilter.entries.forEach { filter ->
            val thumbnail = withContext(Dispatchers.Default) {
                ImageFilterProcessor.generateFilterThumbnail(croppedBitmap, filter, 100)
            }
            filterThumbnails[filter] = thumbnail
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ─── Top Bar ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = stringResource(R.string.scanner_filter_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.scanner_filter_confirm),
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ─── Preview Image ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            // Rotation label
            if (rotation != 0) {
                Text(
                    text = "${rotation}°",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // ─── Rotation Controls ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable(onClick = onRotateLeft),
                contentAlignment = Alignment.Center
            ) {
                Text("↺", fontSize = 28.sp, color = Color.White)
            }

            Spacer(Modifier.width(48.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable(onClick = onRotateRight),
                contentAlignment = Alignment.Center
            ) {
                Text("↻", fontSize = 28.sp, color = Color.White)
            }
        }

        // ─── Bottom Actions Panel ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Filter Selection
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ImageFilter.entries) { filter ->
                        FilterThumbnailItem(
                            filter = filter,
                            thumbnail = filterThumbnails[filter],
                            isSelected = filter == selectedFilter,
                            onClick = { onFilterSelected(filter) }
                        )
                    }
                }

                // Confirm Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.scanner_filter_confirm),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterThumbnailItem(
    filter: ImageFilter,
    thumbnail: Bitmap?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00E676) else Color.Transparent,
        animationSpec = tween(200),
        label = "filter_border"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) borderColor else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = stringResource(filter.labelResId),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Label
        Text(
            text = stringResource(filter.labelResId),
            color = if (isSelected) Color(0xFF00E676) else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
