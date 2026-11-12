package com.offcourse.picko.core.designsystem.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme

private val SummaryCardShape = RoundedCornerShape(28.dp)

/** 추천된 픽업 장소, 차량 도착 시간, 탑승자 도보 시간과 추천 이유를 보여 주는 카드. */
@Composable
fun PickoPickupInfoCard(
    locationName: String,
    vehicleEta: String,
    pedestrianEta: String,
    reason: String,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SummaryCardShape)
            .background(colors.surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PickupSummary(
            label = stringResource(R.string.pickup_info_label),
            locationName = locationName,
            locationStyle = PickoTheme.typography.title.titleLarge,
            vehicleLabel = stringResource(R.string.pickup_info_vehicle_label),
            vehicleEta = vehicleEta,
            pedestrianLabel = stringResource(R.string.navigation_status_pedestrian_label),
            pedestrianEta = pedestrianEta,
            etaStyle = PickoTheme.typography.label.labelLargeBold,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.separator),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = PickoIcons.Info,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colors.accentText,
            )
            Text(
                text = reason,
                style = PickoTheme.typography.label.labelLarge,
                color = colors.textMuted,
            )
        }
    }
}

/** 내비게이션 안내 중 목적지와 도착 시간을 보여 주는 카드. */
@Composable
fun PickoNavigationStatusCard(
    locationName: String,
    vehicleEta: String,
    pedestrianEta: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(SummaryCardShape)
            .background(PickoTheme.colors.surface)
            .padding(horizontal = 32.dp, vertical = 22.dp),
    ) {
        PickupSummary(
            label = stringResource(R.string.navigation_status_pickup_label),
            locationName = locationName,
            locationStyle = PickoTheme.typography.headline.headlineLarge,
            vehicleLabel = stringResource(R.string.navigation_status_vehicle_label),
            vehicleEta = vehicleEta,
            pedestrianLabel = stringResource(R.string.navigation_status_pedestrian_label),
            pedestrianEta = pedestrianEta,
            etaStyle = PickoTheme.typography.label.labelLargeNavigation,
        )
    }
}

@Composable
private fun PickupSummary(
    label: String,
    locationName: String,
    locationStyle: TextStyle,
    vehicleLabel: String,
    vehicleEta: String,
    pedestrianLabel: String,
    pedestrianEta: String,
    etaStyle: TextStyle,
) {
    val colors = PickoTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = PickoIcons.MapPin,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = colors.accent,
                )
                Text(
                    text = label,
                    style = PickoTheme.typography.label.labelSmall,
                    color = colors.textSecondary,
                )
            }
            Text(
                text = locationName,
                style = locationStyle,
                color = colors.textPrimary,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EtaItem(
                label = vehicleLabel,
                value = vehicleEta,
                valueStyle = etaStyle,
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(64.dp)
                    .background(colors.separator),
            )
            EtaItem(
                label = pedestrianLabel,
                value = pedestrianEta,
                valueStyle = etaStyle,
            )
        }
    }
}

@Composable
private fun EtaItem(
    label: String,
    value: String,
    valueStyle: TextStyle,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = PickoTheme.typography.label.labelSmall,
            color = PickoTheme.colors.textSecondary,
        )
        Text(
            text = value,
            style = valueStyle,
            color = PickoTheme.colors.accentStrong,
        )
    }
}

@PickoCarPreview
@Composable
private fun PickoPickupSummaryCardPreview() {
    PickoPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PickoPickupInfoCard(
                locationName = "대치역 2번 출구 앞",
                vehicleEta = "약 3분",
                pedestrianEta = "약 2분",
                reason = "차량 접근 경로와 탑승자의 도보 거리를 고려한 후보 지점입니다.",
            )
            PickoNavigationStatusCard(
                locationName = "대치역 2번 출구 앞",
                vehicleEta = "약 3분",
                pedestrianEta = "약 2분",
            )
        }
    }
}
