package dev.sonle.pdfscanner.presentation.features.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        stringResource(R.string.main_tab_home) to Icons.Rounded.Home,
        stringResource(R.string.main_tab_settings) to Icons.Rounded.Settings
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenScanner,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, contentDescription = "Scan")
                    Text(
                        text = stringResource(R.string.main_scan_button),
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(tabs[0].second, contentDescription = tabs[0].first) },
                        label = { Text(tabs[0].first, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.weight(1f))
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(tabs[1].second, contentDescription = tabs[1].first) },
                        label = { Text(tabs[1].first, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
                0 -> HomeScreen()
                else -> SettingsScreen()
            }
        }
    }
}