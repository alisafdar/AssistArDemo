package com.teamviewer.assistar.demo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class SITypography(
    val normalTitle: TextStyle,
    val screenTitle: TextStyle,
    val sectionTitle: TextStyle,
    val small: TextStyle,
    val subTitle: TextStyle,
    val subText: TextStyle,
    val toolTip: TextStyle
)

val normalTitle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() =
        TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

val screenTitle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() =
        TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp
        )

val sectionTitle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() =
        TextStyle(
            fontSize = 18.sp
        )

val small: TextStyle
    @Composable
    @ReadOnlyComposable
    get() =
        TextStyle(
            fontSize = 10.sp
        )

val subTitle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() =
        TextStyle(
            fontSize = 16.sp
        )

val subText: TextStyle
    @Composable
    @ReadOnlyComposable
    get() =
        TextStyle(
            fontSize = 12.sp
        )

val toolTip: TextStyle
    @Composable
    @ReadOnlyComposable
    get() =
        TextStyle(
            lineHeight = 22.sp,
            fontSize = 13.sp
        )

@Composable
@ReadOnlyComposable
internal fun defaultTypography(): SITypography = SITypography(
    normalTitle = normalTitle,
    screenTitle = screenTitle,
    sectionTitle = sectionTitle,
    subTitle = subTitle,
    subText = subText,
    small = small,
    toolTip = toolTip
)
