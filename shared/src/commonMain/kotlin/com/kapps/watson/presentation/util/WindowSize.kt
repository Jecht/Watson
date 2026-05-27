package com.kapps.watson.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * Discrete bucket describing the current window width.
 * Used to switch between mobile-friendly and desktop-friendly layouts.
 */
enum class WindowWidthSize {
    /** Below ~600dp: phone portrait, tight space. */
    Compact,

    /** Between 600 and 840dp: phone landscape, tablet portrait, foldable. */
    Medium,

    /** Above 840dp: tablet landscape, desktop, large windows. */
    Expanded,
}

/**
 * Returns the [WindowWidthSize] for the current window.
 * Recomputes whenever the window is resized.
 */
@Composable
fun rememberWindowWidthSize(): WindowWidthSize {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    return remember(windowInfo.containerSize, density) {
        val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
        when {
            widthDp < 600.dp -> WindowWidthSize.Compact
            widthDp < 840.dp -> WindowWidthSize.Medium
            else -> WindowWidthSize.Expanded
        }
    }
}