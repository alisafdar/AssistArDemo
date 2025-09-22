package com.teamviewer.assistvision.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.teamviewer.assistvision.constants.AppConstants.SI_COLOR_MODE_DARK
import com.teamviewer.assistvision.constants.AppConstants.SI_COLOR_MODE_LIGHT

internal val LocalSiColors =
    staticCompositionLocalOf {
        lightColors
    }

object SITheme {
    val colors: SIColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSiColors.current

    val typography: SITypography
        @Composable
        @ReadOnlyComposable
        get() = defaultTypography()
}

@Composable
fun AssistArTheme(
    mode: Int = SI_COLOR_MODE_LIGHT,
    content: @Composable () -> Unit
) {
    val colors = when (mode) {
        SI_COLOR_MODE_DARK -> darkColors
        else -> lightColors
    }
    CompositionLocalProvider(
        LocalSiColors provides colors
    ) {
        content()
    }
}
