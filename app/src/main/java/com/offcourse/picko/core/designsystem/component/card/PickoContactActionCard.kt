package com.offcourse.picko.core.designsystem.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offcourse.picko.R
import com.offcourse.picko.core.common.extensions.dropShadowLevel1
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/** 탑승자 이름과 번호를 보여 주고, 오른쪽 버튼으로 픽업 링크를 보내는 카드. */
@Composable
fun PickoContactActionCard(
    name: String,
    phone: String,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadowLevel1(shape)
            .clip(shape)
            .background(colors.surfaceRaised)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.avatarSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = PickoIcons.User,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = colors.avatarContent,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = name,
                style = PickoTheme.typography.label.labelLargeBold,
                color = colors.textPrimary,
            )
            Text(
                text = phone,
                // labelLarge의 줄 높이(24sp)가 글자(32)보다 작아 이름과 붙어 보이므로 늘려 쓴다.
                style = PickoTheme.typography.label.labelLarge.copy(lineHeight = 40.sp),
                color = colors.textSecondary,
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.accent)
                .clickable(
                    role = Role.Button,
                    onClick = onSendClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = PickoIcons.Send,
                contentDescription = stringResource(R.string.contact_action_send_a11y),
                modifier = Modifier.size(28.dp),
                tint = colors.onAccent,
            )
        }
    }
}

@PickoCarPreview
@Composable
private fun PickoContactActionCardPreview() {
    PickoPreviewSurface {
        PickoContactActionCard(
            name = "현대",
            phone = "010-1234-5678",
            onSendClick = {},
        )
    }
}
