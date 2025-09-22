package com.teamviewer.assistvision.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.platform.LocalInspectionMode
import com.teamviewer.assistvision.ui.theme.ARTheme

// Use with previews ONLY
@Composable
fun FillSizeBackground(
    content:
    @Composable @UiComposable
    () -> Unit
) {
    FillBackground(
        modifier =
        Modifier
            .fillMaxSize()
            .background(
                color = ARTheme.colors.background
            ),
        content = {
            content()
        }
    )
}

@Composable
fun FillWidthBackground(
    content:
    @Composable @UiComposable
    () -> Unit
) {
    FillBackground(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(
                color = ARTheme.colors.background
            ),
        content = {
            content()
        }
    )
}

@Composable
fun FillBackground(
    modifier: Modifier,
    content:
    @Composable @UiComposable
    () -> Unit
) {
    Box(
        modifier = modifier,
        content = {
            content()
        }
    )
}

val isPreview: Boolean
    @Composable
    get() = LocalInspectionMode.current
