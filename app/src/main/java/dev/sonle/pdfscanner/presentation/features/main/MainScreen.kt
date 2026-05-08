package dev.sonle.pdfscanner.presentation.features.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.presentation.features.main.home.HomeScreen
import dev.sonle.pdfscanner.presentation.features.main.settings.SettingsScreen

@Composable
fun MainScreen(
    onOpenScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.main_tab_home) to Iconsax.Linear.Home,
        stringResource(R.string.main_tab_settings) to Iconsax.Linear.Setting
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onOpenScanner = onOpenScanner,
                tabs = tabs
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(300)
                val fadeSpec = tween<Float>(300)
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(slideSpec) { width -> direction * width } + fadeIn(fadeSpec)).togetherWith(
                    slideOutHorizontally(slideSpec) { width -> -direction * width } + fadeOut(fadeSpec))
            },
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            label = "tab_animation"
        ) { targetTab ->
            when (targetTab) {
                0 -> HomeScreen(onOpenScanner = onOpenScanner)
                else -> SettingsScreen()
            }
        }
    }
}

@Composable
private fun CustomBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onOpenScanner: () -> Unit,
    tabs: List<Pair<String, ImageVector>>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bar Background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            CustomNavItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = tabs[0].second,
                label = tabs[0].first,
                modifier = Modifier.weight(1f)
            )

            // Space for Center FAB
            Spacer(modifier = Modifier.weight(1f))

            // Settings Tab
            CustomNavItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = tabs[1].second,
                label = tabs[1].first,
                modifier = Modifier.weight(1f)
            )
        }

        // Center FAB
        FloatingActionButton(
            onClick = onOpenScanner,
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(72.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Iconsax.Linear.Add,
                contentDescription = stringResource(R.string.main_scan_button),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun CustomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val contentColor = if (selected) selectedColor else unselectedColor

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}