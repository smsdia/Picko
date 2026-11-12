package com.offcourse.picko.core.designsystem.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme

private val ResultCardShape = RoundedCornerShape(28.dp)

/**
 * 번호, 내용, 선택 표시로 이루어진 결과 카드의 뼈대. 내용은 [content]에 채운다.
 * 연락처, 장소, 주변 장소 카드는 아래의 전용 함수를 쓴다.
 */
@Composable
fun PickoResultCard(
    order: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PickoTheme.colors
    val surface = if (isSelected) colors.surfaceRaised else colors.surfaceCard
    val border = if (isSelected) 3.75.dp to colors.borderSelected else 1.25.dp to colors.borderCard

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 145.dp)
            .clip(ResultCardShape)
            .background(surface)
            .border(border.first, border.second, ResultCardShape)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(start = 35.dp, end = 64.dp, top = 24.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = order.toString(),
                style = PickoTheme.typography.title.titleLarge,
                color = colors.accent,
            )
        }

        Spacer(modifier = Modifier.width(31.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )

        if (isSelected) {
            Icon(
                imageVector = PickoIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(46.dp),
                tint = colors.borderSelected,
            )
        }
    }
}

@Composable
fun PickoContactResultCard(
    order: Int,
    name: String,
    phone: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PickoResultCard(
        order = order,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = name,
            style = PickoTheme.typography.title.titleLarge,
            color = PickoTheme.colors.textPrimary,
        )
        Text(
            text = phone,
            style = PickoTheme.typography.label.labelMedium,
            color = PickoTheme.colors.textSecondary,
        )
    }
}

/** [tollFeeText]는 "3,000원"처럼 이미 만들어진 문자열이고, 통행료가 없으면 null이다. */
@Composable
fun PickoPlaceResultCard(
    order: Int,
    name: String,
    distanceText: String,
    durationText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tollFeeText: String? = null,
) {
    val colors = PickoTheme.colors
    val metaStyle = PickoTheme.typography.label.labelMedium

    PickoResultCard(
        order = order,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = name,
            style = PickoTheme.typography.title.titleLarge,
            color = colors.textPrimary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DistanceAndDuration(
                distanceText = distanceText,
                durationText = durationText,
            )
            if (tollFeeText != null) {
                InlineDivider(color = colors.separator)
                Text(
                    text = stringResource(R.string.result_card_toll_label),
                    style = metaStyle,
                    color = colors.textMuted,
                )
                Text(
                    text = tollFeeText,
                    style = metaStyle,
                    color = colors.accentText,
                )
            }
        }
    }
}

@Composable
fun PickoNearbyPlaceResultCard(
    order: Int,
    name: String,
    distanceText: String,
    durationText: String,
    recommendation: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors

    PickoResultCard(
        order = order,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = name,
            style = PickoTheme.typography.title.titleLarge,
            color = colors.textPrimary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DistanceAndDuration(
                distanceText = distanceText,
                durationText = durationText,
            )
            InlineDivider(color = colors.accentText)
            Text(
                text = recommendation,
                style = PickoTheme.typography.label.labelMedium,
                color = colors.accentText,
            )
        }
    }
}

@Composable
private fun DistanceAndDuration(
    distanceText: String,
    durationText: String,
) {
    val color = PickoTheme.colors.textMeta
    val style = PickoTheme.typography.label.labelMedium

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = distanceText,
            style = style,
            color = color,
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(color, CircleShape),
        )
        Text(
            text = durationText,
            style = style,
            color = color,
        )
    }
}

@Composable
private fun InlineDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(28.dp)
            .background(color, RoundedCornerShape(1.dp)),
    )
}

@PickoCarPreview
@Composable
private fun PickoResultCardPreview() {
    PickoPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PickoContactResultCard(
                order = 1,
                name = "사랑하는 우리 현대",
                phone = "010-1234-5678",
                isSelected = true,
                onClick = {},
            )
            PickoPlaceResultCard(
                order = 2,
                name = "대치동 학원가 사거리",
                distanceText = "18km",
                durationText = "20분",
                isSelected = false,
                onClick = {},
                tollFeeText = "3,000원",
            )
            PickoNearbyPlaceResultCard(
                order = 1,
                name = "대치역 2번 출구 앞 안전정차구역",
                distanceText = "18km",
                durationText = "20분",
                recommendation = "아이가 1분 만에 안전하게 걸어올 수 있어요.",
                isSelected = true,
                onClick = {},
            )
        }
    }
}
