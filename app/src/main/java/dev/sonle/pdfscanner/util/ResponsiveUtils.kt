package dev.sonle.pdfscanner.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen size classifications for responsive design
 */
enum class WindowSizeClass {
    Compact,    // Phone portrait
    Medium,     // Tablet portrait / Phone landscape
    Expanded    // Tablet landscape
}

/**
 * Responsive utilities for handling different screen sizes
 */
object ResponsiveUtils {
    
    /**
     * Get window size class based on screen width
     */
    @Composable
    fun getWindowSizeClass(): WindowSizeClass {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < 600.dp -> WindowSizeClass.Compact
            screenWidth < 840.dp -> WindowSizeClass.Medium
            else -> WindowSizeClass.Expanded
        }
    }
    
    /**
     * Get responsive padding based on screen size
     */
    @Composable
    fun getResponsivePadding(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 16.dp
            WindowSizeClass.Medium -> 24.dp
            WindowSizeClass.Expanded -> 32.dp
        }
    }
    
    /**
     * Get responsive horizontal padding
     */
    @Composable
    fun getResponsiveHorizontalPadding(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 16.dp
            WindowSizeClass.Medium -> 24.dp
            WindowSizeClass.Expanded -> 32.dp
        }
    }
    
    /**
     * Get responsive vertical padding
     */
    @Composable
    fun getResponsiveVerticalPadding(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 8.dp
            WindowSizeClass.Medium -> 12.dp
            WindowSizeClass.Expanded -> 16.dp
        }
    }
    
    /**
     * Get responsive spacing between elements
     */
    @Composable
    fun getResponsiveSpacing(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 8.dp
            WindowSizeClass.Medium -> 12.dp
            WindowSizeClass.Expanded -> 16.dp
        }
    }
    
    /**
     * Get responsive corner radius
     */
    @Composable
    fun getResponsiveCornerRadius(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 8.dp
            WindowSizeClass.Medium -> 12.dp
            WindowSizeClass.Expanded -> 16.dp
        }
    }
    
    /**
     * Get responsive elevation
     */
    @Composable
    fun getResponsiveElevation(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 2.dp
            WindowSizeClass.Medium -> 4.dp
            WindowSizeClass.Expanded -> 6.dp
        }
    }
    
    /**
     * Get responsive icon size
     */
    @Composable
    fun getResponsiveIconSize(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 24.dp
            WindowSizeClass.Medium -> 28.dp
            WindowSizeClass.Expanded -> 32.dp
        }
    }
    
    /**
     * Get responsive button height
     */
    @Composable
    fun getResponsiveButtonHeight(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 48.dp
            WindowSizeClass.Medium -> 52.dp
            WindowSizeClass.Expanded -> 56.dp
        }
    }
    
    /**
     * Get responsive text field height
     */
    @Composable
    fun getResponsiveTextFieldHeight(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 56.dp
            WindowSizeClass.Medium -> 60.dp
            WindowSizeClass.Expanded -> 64.dp
        }
    }
    
    /**
     * Get responsive card padding
     */
    @Composable
    fun getResponsiveCardPadding(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 16.dp
            WindowSizeClass.Medium -> 20.dp
            WindowSizeClass.Expanded -> 24.dp
        }
    }
    
    /**
     * Get responsive list item height
     */
    @Composable
    fun getResponsiveListItemHeight(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 64.dp
            WindowSizeClass.Medium -> 72.dp
            WindowSizeClass.Expanded -> 80.dp
        }
    }
    
    /**
     * Get responsive app bar height
     */
    @Composable
    fun getResponsiveAppBarHeight(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 56.dp
            WindowSizeClass.Medium -> 64.dp
            WindowSizeClass.Expanded -> 72.dp
        }
    }
    
    /**
     * Get responsive bottom navigation height
     */
    @Composable
    fun getResponsiveBottomNavHeight(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 56.dp
            WindowSizeClass.Medium -> 64.dp
            WindowSizeClass.Expanded -> 72.dp
        }
    }
    
    /**
     * Get responsive fab size
     */
    @Composable
    fun getResponsiveFabSize(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 56.dp
            WindowSizeClass.Medium -> 64.dp
            WindowSizeClass.Expanded -> 72.dp
        }
    }
    
    /**
     * Get responsive divider thickness
     */
    @Composable
    fun getResponsiveDividerThickness(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 1.dp
            WindowSizeClass.Medium -> 1.5.dp
            WindowSizeClass.Expanded -> 2.dp
        }
    }
    
    /**
     * Get responsive grid columns for LazyColumn
     */
    @Composable
    fun getResponsiveGridColumns(): Int {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 1
            WindowSizeClass.Medium -> 2
            WindowSizeClass.Expanded -> 3
        }
    }
    
    /**
     * Get responsive max content width
     */
    @Composable
    fun getResponsiveMaxContentWidth(): Dp {
        return when (getWindowSizeClass()) {
            WindowSizeClass.Compact -> 600.dp
            WindowSizeClass.Medium -> 800.dp
            WindowSizeClass.Expanded -> 1200.dp
        }
    }
}
