package com.offcourse.picko.core.designsystem.component.marker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.core.common.extensions.dropShadowLevel1

enum class PickoMapMarkerType { Driver, Pedestrian, Pickup }

/** 배지를 기준으로 라벨이 놓이는 쪽. 가까이 놓인 마커의 라벨이 겹치지 않게 서로 다른 쪽에 둔다. */
enum class PickoMapMarkerLabelSide { Start, End }

private val BadgeSize = 88.dp
private val HaloRadius = 120.dp
private val MarkerHorizontalPadding = 12.dp

/**
 * 지도 위에 올리는 마커. 배지와 안내 라벨이 한 묶음이며, 차량 마커에는 뒤에 퍼지는 빛이 함께 그려진다.
 * 카카오 지도 라벨은 비트맵만 받으므로, 지도에 올릴 때는 이 컴포저블을 비트맵으로 바꾸는 방법이 따로 필요하다.
 */
@Composable
fun PickoMapMarker(
    type: PickoMapMarkerType,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    labelSide: PickoMapMarkerLabelSide = PickoMapMarkerLabelSide.End,
) {
    val haloColor = PickoTheme.colors.accentCornflower

    Row(
        modifier = modifier
            .drawBehind {
                if (type == PickoMapMarkerType.Driver) {
                    val badgeCenterX = if (labelSide == PickoMapMarkerLabelSide.End) {
                        MarkerHorizontalPadding.toPx() + BadgeSize.toPx() / 2
                    } else {
                        size.width - MarkerHorizontalPadding.toPx() - BadgeSize.toPx() / 2
                    }
                    val center = Offset(badgeCenterX, size.height / 2)

                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to haloColor.copy(alpha = 0.5f),
                            0.45f to haloColor.copy(alpha = 0.23f),
                            1f to haloColor.copy(alpha = 0f),
                            center = center,
                            radius = HaloRadius.toPx(),
                        ),
                        radius = HaloRadius.toPx(),
                        center = center,
                    )
                }
            }
            .padding(horizontal = MarkerHorizontalPadding, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (labelSide == PickoMapMarkerLabelSide.End) {
            PickoMapMarkerBadge(type = type)
        }
        PickoMapMarkerLabel(
            title = title,
            description = description,
        )
        if (labelSide == PickoMapMarkerLabelSide.Start) {
            PickoMapMarkerBadge(type = type)
        }
    }
}

@Composable
fun PickoMapMarkerBadge(
    type: PickoMapMarkerType,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors
    val (background, icon) = when (type) {
        PickoMapMarkerType.Driver -> colors.accent to PickoIcons.MarkerDriver
        PickoMapMarkerType.Pedestrian -> colors.mint to PickoIcons.MarkerPedestrian
        PickoMapMarkerType.Pickup -> colors.accentBright to PickoIcons.MarkerPickup
    }
    // 픽업 아이콘은 배지 크기에 맞춰 그려져 있고, 나머지는 48dp 아이콘이다.
    val iconSize = if (type == PickoMapMarkerType.Pickup) BadgeSize else 48.dp

    Box(
        modifier = modifier
            .size(BadgeSize)
            .dropShadowLevel1(CircleShape)
            .background(background, CircleShape)
            .border(4.dp, colors.white, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun PickoMapMarkerLabel(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .dropShadowLevel1(shape)
            .clip(shape)
            .background(colors.markerSurface)
            .border(2.dp, colors.glassBorder, shape)
            .padding(horizontal = 26.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text = title,
            style = PickoTheme.typography.marker.markerTitle,
            color = colors.markerTitle,
        )
        Text(
            text = description,
            style = PickoTheme.typography.marker.markerDescription,
            color = colors.markerBody,
        )
    }
}

@PickoCarPreview
@Composable
private fun PickoMapMarkerPreview() {
    PickoPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            PickoMapMarker(
                type = PickoMapMarkerType.Driver,
                title = "내 차량",
                description = "현재 위치",
            )
            PickoMapMarker(
                type = PickoMapMarkerType.Pedestrian,
                title = "탑승자",
                description = "픽업 지점으로 이동 중",
                labelSide = PickoMapMarkerLabelSide.Start,
            )
            PickoMapMarker(
                type = PickoMapMarkerType.Pickup,
                title = "추천 픽업 위치",
                description = "도보 약 3분",
            )
        }
    }
}
