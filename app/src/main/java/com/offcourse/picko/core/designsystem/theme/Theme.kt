package com.offcourse.picko.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val localPickoColors = staticCompositionLocalOf { lightPickoColors }

val localPickoTypography = staticCompositionLocalOf { defaultPickoTypography }

val localPickoImages = staticCompositionLocalOf { lightPickoImages }

// Material3 컴포넌트(리플, 텍스트 선택 핸들 등)가 읽는 색만 역할 색상에서 매핑한다.
private fun PickoColors.toMaterialColorScheme(darkTheme: Boolean) =
    if (darkTheme) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
        )
    }

object PickoTheme {
    val colors: PickoColors
        @Composable
        @ReadOnlyComposable
        get() = localPickoColors.current

    val typography: PickoTypography
        @Composable
        @ReadOnlyComposable
        get() = localPickoTypography.current

    val images: PickoImages
        @Composable
        @ReadOnlyComposable
        get() = localPickoImages.current
}

@Composable
fun ProvidePickoColorsAndTypography(
    colors: PickoColors,
    typography: PickoTypography,
    images: PickoImages,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        localPickoColors provides colors,
        localPickoTypography provides typography,
        localPickoImages provides images,
        content = content,
    )
}

/**
 * 주야간은 [darkTheme] 값으로만 결정한다. 기본값은 시스템 설정이며, 차량 신호를 쓰게 되면 호출부에서 값을 주입한다.
 */
@Composable
fun PickoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkPickoColors else lightPickoColors

    ProvidePickoColorsAndTypography(
        colors = colors,
        typography = defaultPickoTypography,
        images = if (darkTheme) darkPickoImages else lightPickoImages,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(darkTheme),
            content = content,
        )
    }
}
