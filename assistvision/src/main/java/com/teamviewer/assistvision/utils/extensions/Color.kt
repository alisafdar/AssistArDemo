package com.teamviewer.assistvision.utils.extensions

import androidx.compose.ui.graphics.Color

fun Color.withAlpha(newAlpha: Float): Color =
    Color(
        alpha = newAlpha,
        red = red,
        green = green,
        blue = blue
    )