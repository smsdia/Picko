package com.offcourse.picko.core.designsystem.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.PickoPreviewSurface
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.core.common.extensions.dropShadowLevel2

/**
 * 제목, 설명, 버튼 두 개로 이루어진 확인 대화상자.
 * 왼쪽 강조 버튼이 [primaryText]이고, 오른쪽 보조 버튼이 [secondaryText]이다.
 * [secondaryText]가 null 이면 강조 버튼 하나만 보여 준다. TODO(figma): 버튼 하나짜리 시안이 없어 같은 스타일로 폭만 넓혔다.
 */
@Composable
fun PickoDialog(
    title: String,
    description: String,
    primaryText: String,
    onPrimaryClick: () -> Unit,
    onDismissRequest: () -> Unit,
    secondaryText: String? = null,
    onSecondaryClick: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // 창 기본 dim 은 색과 농도를 정할 수 없어 끄고, 시안의 dim(#090E1B 65%)을 직접 깐다.
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PickoTheme.colors.overlayDim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // 대화상자 안쪽을 눌러도 dim 의 닫기로 새지 않게 막는다.
            PickoDialogContent(
                title = title,
                description = description,
                primaryText = primaryText,
                onPrimaryClick = onPrimaryClick,
                secondaryText = secondaryText,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            )
        }
    }
}

@Composable
internal fun PickoDialogContent(
    title: String,
    description: String,
    primaryText: String,
    onPrimaryClick: () -> Unit,
    secondaryText: String?,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors
    val shape = RoundedCornerShape(32.dp)

    Column(
        modifier = modifier
            .widthIn(max = 960.dp)
            .fillMaxWidth()
            .dropShadowLevel2(shape)
            .clip(shape)
            .background(colors.surfaceModal)
            .border(2.dp, colors.borderModal, shape)
            .padding(horizontal = 64.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(48.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Text(
                text = title,
                style = PickoTheme.typography.headline.headlineLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = description,
                style = PickoTheme.typography.body.bodyLargeDialog,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            DialogButton(
                text = primaryText,
                onClick = onPrimaryClick,
                background = colors.accent,
                contentColor = colors.onAccent,
                modifier = Modifier.weight(1f),
            )
            if (secondaryText != null) {
                DialogButton(
                    text = secondaryText,
                    onClick = onSecondaryClick,
                    background = colors.buttonNeutral,
                    contentColor = colors.onButtonNeutral,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = PickoTheme.typography.label.labelLargeButton,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@PickoCarPreview
@Composable
private fun PickoDialogPreview() {
    PickoPreviewSurface {
        PickoDialogContent(
            title = "픽업을 취소할까요?",
            description = "취소하면 탑승자에게 알림이 전송되고\n픽업 안내가 종료됩니다.",
            primaryText = "계속 진행",
            onPrimaryClick = {},
            secondaryText = "픽업 취소",
            onSecondaryClick = {},
        )
    }
}
