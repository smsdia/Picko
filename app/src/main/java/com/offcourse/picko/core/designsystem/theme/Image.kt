package com.offcourse.picko.core.designsystem.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.offcourse.picko.R

/**
 * 모드에 따라 다른 이미지 에셋. `drawable-night/`는 시스템 설정만 따라서 `PickoTheme(darkTheme = ...)`으로 주입한 값과
 * 어긋나므로, 색상과 같은 방식으로 인스턴스가 고른다.
 */
@Immutable
data class PickoImages(
    @DrawableRes val mapPin3d: Int,
    @DrawableRes val gradientBackground: Int,
)

val lightPickoImages = PickoImages(
    mapPin3d = R.drawable.img_map_pin_3d_light,
    gradientBackground = R.drawable.img_gradient_background_light,
)

val darkPickoImages = PickoImages(
    mapPin3d = R.drawable.img_map_pin_3d_dark,
    gradientBackground = R.drawable.img_gradient_background_dark,
)
