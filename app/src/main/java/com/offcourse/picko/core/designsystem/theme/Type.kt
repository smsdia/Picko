package com.offcourse.picko.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.offcourse.picko.R

object PickoFont {
    val regular = FontFamily(Font(R.font.pretendard_regular))
    val medium = FontFamily(Font(R.font.pretendard_medium))
    val semiBold = FontFamily(Font(R.font.pretendard_semibold))
    val bold = FontFamily(Font(R.font.pretendard_bold))
}

private val PlatformStyle = PlatformTextStyle(includeFontPadding = false)

// 크기·줄 높이는 Figma px 값을 그대로 sp로 쓴다. 대상 기기가 160dpi라서 1px = 1dp = 1sp이다.
// 줄 높이가 Figma에서 `Auto`이면 lineHeight를 지정하지 않는다. 자간은 모두 0이므로 지정하지 않는다.
private fun pickoTextStyle(
    fontFamily: FontFamily,
    fontSize: Int,
    lineHeight: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = fontFamily,
    fontSize = fontSize.sp,
    lineHeight = lineHeight,
    platformStyle = PlatformStyle,
)

// Figma에서 줄 높이가 % 단위인 항목은 글자 크기에 비율을 곱해 sp로 바꾼다.
private fun percent(fontSize: Int, percent: Int): TextUnit = (fontSize * percent / 100f).sp

sealed interface TypographyTokens {
    @Immutable
    data class Display(
        val displayHeroLarge: TextStyle,
        val displayLargeBrand: TextStyle,
    )

    @Immutable
    data class Headline(
        val headlineLarge: TextStyle,
        val headlineMedium: TextStyle,
    )

    @Immutable
    data class Title(
        val titleLarge: TextStyle,
        val titleMedium: TextStyle,
    )

    @Immutable
    data class Body(
        val bodyHeroLarge: TextStyle,
        val bodyLarge: TextStyle,
        val bodyLargeDialog: TextStyle,
        val bodyLargeVoice: TextStyle,
        val bodyMedium: TextStyle,
    )

    @Immutable
    data class Label(
        val labelLargeNavigation: TextStyle,
        val labelLargeButton: TextStyle,
        val labelMediumButton: TextStyle,
        val labelLargeBold: TextStyle,
        val labelLarge: TextStyle,
        val labelMedium: TextStyle,
        val labelSmall: TextStyle,
    )

    @Immutable
    data class Tag(
        val tagLabel: TextStyle,
    )

    @Immutable
    data class Marker(
        val markerTitle: TextStyle,
        val markerDescription: TextStyle,
    )
}

@Immutable
data class PickoTypography(
    val display: TypographyTokens.Display,
    val headline: TypographyTokens.Headline,
    val title: TypographyTokens.Title,
    val body: TypographyTokens.Body,
    val label: TypographyTokens.Label,
    val tag: TypographyTokens.Tag,
    val marker: TypographyTokens.Marker,
)

val defaultPickoTypography = PickoTypography(
    display = TypographyTokens.Display(
        displayHeroLarge = pickoTextStyle(PickoFont.bold, 68, percent(68, 125)),
        displayLargeBrand = pickoTextStyle(PickoFont.bold, 54, 52.sp),
    ),
    headline = TypographyTokens.Headline(
        headlineLarge = pickoTextStyle(PickoFont.bold, 48, 42.sp),
        headlineMedium = pickoTextStyle(PickoFont.bold, 39, 38.sp),
    ),
    title = TypographyTokens.Title(
        titleLarge = pickoTextStyle(PickoFont.bold, 40, 34.sp),
        titleMedium = pickoTextStyle(PickoFont.medium, 36, 26.sp),
    ),
    body = TypographyTokens.Body(
        bodyHeroLarge = pickoTextStyle(PickoFont.medium, 44, percent(44, 125)),
        bodyLarge = pickoTextStyle(PickoFont.medium, 39, 32.sp),
        bodyLargeDialog = pickoTextStyle(PickoFont.medium, 39, 56.sp),
        bodyLargeVoice = pickoTextStyle(PickoFont.medium, 36, 46.sp),
        bodyMedium = pickoTextStyle(PickoFont.medium, 34, 40.sp),
    ),
    label = TypographyTokens.Label(
        labelLargeNavigation = pickoTextStyle(PickoFont.bold, 40),
        labelLargeButton = pickoTextStyle(PickoFont.bold, 38),
        labelMediumButton = pickoTextStyle(PickoFont.bold, 36, 44.sp),
        labelLargeBold = pickoTextStyle(PickoFont.bold, 32, 40.sp),
        labelLarge = pickoTextStyle(PickoFont.medium, 32, 24.sp),
        labelMedium = pickoTextStyle(PickoFont.medium, 30, 26.sp),
        labelSmall = pickoTextStyle(PickoFont.medium, 28, 36.sp),
    ),
    tag = TypographyTokens.Tag(
        tagLabel = pickoTextStyle(PickoFont.semiBold, 26, percent(26, 120)),
    ),
    marker = TypographyTokens.Marker(
        markerTitle = pickoTextStyle(PickoFont.bold, 30),
        markerDescription = pickoTextStyle(PickoFont.regular, 26),
    ),
)
