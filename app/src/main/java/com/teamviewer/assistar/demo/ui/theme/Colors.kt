package com.teamviewer.assistar.demo.ui.theme

import androidx.compose.ui.graphics.Color
import com.teamviewer.assistvision.utils.extensions.withAlpha

private val C24DarkBlue = Color(0xff022D94)
private val C24LightBlue = Color(0xff0563C1)

private val black100 = Color(0x14000000)

private val grey800 = Color(0xff333333)
private val grey600 = Color(0xff666666)
private val grey500 = Color(0xff999999)
private val gray450 = Color(0xffa6a5a7)
private val grey400 = Color(0xffdcdcdc)
private val gray350 = Color(0xffe0dfe0)
private val grey300 = Color(0xfff4f4f4)
private val grey200 = Color(0xfffafafa)

private val white = Color(0xffffffff)

private val blue800 = Color(0xff063773)
private val blue600 = Color(0xff005ea8)
private val blue400 = Color(0xff0271c2)
private val blue300 = Color(0xffc0ccdc)
private val blue200 = Color(0xffecf7fd)

private val teal800 = Color(0xff005c61)
private val teal400 = Color(0xff305D78)
private val teal500 = Color(0xff3ca7ac)
private val teal300 = Color(0xffc1e3e3)
private val teal200 = Color(0xffe2f2f3)

private val purple800 = Color(0xffae3775)

private val red800 = Color(0xffc82d2d)
private val red200 = Color(0xfff8e3e3)

private val orange800 = Color(0xffc05702)
private val orange500 = Color(0xfff07c00)
private val orange300 = Color(0xffffb74d)
private val orange200 = Color(0xfffdeede)

private val yellow800 = Color(0xffe29e0a)
private val yellow500 = Color(0xfff6b800)
private val yellow200 = Color(0xfffef6de)

private val green800 = Color(0xff008300)
private val green500 = Color(0xff7ab51D)
private val green200 = Color(0xffdeefde)

private val transparent = Color(0x00000000)

private val unset = Color(0xffff00ff)

data class SIColors(
    val unset: Color,
    val background: Color,
    val onBackground: Color,
    val container: Color,
    val containerRipple: List<Color>,
    val onContainer: Color,
    val onContainerLight: Color,
    val onContainerMediumLight: Color,
    val onContainerVeryLight: Color,
    val onContainerVeryVeryLight: Color,
    val containerShadow: Color,
    val mainTextFieldBorderFaded: Color,
    // buttons
    val mainLight: Color,
    // texts
    val main: Color,
    val mainTextFaded: Color,
    // header & footer
    val mainDark: Color,
    val mainRipple: List<Color>,
    val mainSpotlight: Color,
    val mainSpotlightDark: Color,
    val onMain: Color,
    val mainBackground: Color,
    val onMainBackground: Color,
    val success: Color,
    val successBackground: Color,
    val successDark: Color,
    val onSuccess: Color,
    val onSuccessDark: Color,
    val debug: Color,
    val warning: Color,
    val warningBackground: Color,
    val warningDark: Color,
    val warningLight: Color,
    val error: Color,
    val errorBackground: Color,
    val onError: Color,
    val transparent: Color
)

val lightColors =
    SIColors(
        unset = unset,
        background = grey200,
        onBackground = grey800,
        container = white,
        containerRipple =
        listOf(
            grey400
                .withAlpha(0.5f),
            grey300
                .withAlpha(0.5f)
        ),
        onContainer = grey800,
        onContainerLight = grey600,
        onContainerMediumLight = grey500,
        onContainerVeryLight = grey400,
        onContainerVeryVeryLight = grey300,
        containerShadow = black100,
        mainLight = C24LightBlue,
        main = blue600,
        mainDark = C24DarkBlue,
        mainRipple =
        listOf(
            blue200
                .withAlpha(0.5f),
            blue400
                .withAlpha(0.5f)
        ),
        mainSpotlight = teal500,
        mainSpotlightDark = teal800,
        onMain = white,
        mainBackground = blue200,
        onMainBackground = grey200,
        success = green500,
        successBackground = green200,
        successDark = green800,
        onSuccess = white,
        onSuccessDark = white,
        debug = teal400,
        warning = orange500,
        warningBackground = orange200,
        warningDark = orange800,
        warningLight = yellow800,
        error = red800,
        errorBackground = red200,
        onError = white,
        transparent = transparent,
        mainTextFaded = gray450,
        mainTextFieldBorderFaded = gray350
    )

val darkColors =
    SIColors(
        unset = unset,
        background = unset,
        onBackground = unset,
        container = unset,
        containerRipple = listOf(
            unset,
            unset
        ),
        onContainer = unset,
        onContainerLight = unset,
        onContainerMediumLight = unset,
        onContainerVeryLight = unset,
        onContainerVeryVeryLight = unset,
        containerShadow = unset,
        mainLight = unset,
        main = unset,
        mainDark = unset,
        mainRipple = listOf(
            unset,
            unset
        ),
        mainSpotlight = unset,
        mainSpotlightDark = unset,
        onMain = unset,
        mainBackground = unset,
        onMainBackground = unset,
        success = unset,
        successBackground = unset,
        successDark = unset,
        onSuccess = unset,
        onSuccessDark = unset,
        debug = unset,
        warning = unset,
        warningBackground = unset,
        warningDark = unset,
        warningLight = unset,
        error = unset,
        errorBackground = unset,
        onError = unset,
        transparent = transparent,
        mainTextFaded = unset,
        mainTextFieldBorderFaded = unset
    )
