package com.offcourse.picko.presentation.pickup.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.offcourse.picko.R
import com.offcourse.picko.core.common.extensions.noRippleClickable
import com.offcourse.picko.core.designsystem.component.indicator.PickoMicBadge
import com.offcourse.picko.core.designsystem.component.indicator.PickoMicLabelGap
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.presentation.voice.VoiceAnnouncement
import com.offcourse.picko.presentation.voice.VoiceListenState
import com.offcourse.picko.presentation.voice.voiceMessage

private val TextGap = 24.dp

/**
 * 장소를 못 찾았거나 다시 말하려 할 때 화면을 덮는 음성 재요청 안내.
 * 마이크를 누르면 여기서 다시 듣고, 새 요청을 읽으면 흐름이 새로 시작된다. 뒤로 가기나 바깥 터치로 닫는다.
 */
@Composable
internal fun VoiceRetryOverlay(
    voice: VoiceListenState,
    onMicClick: () -> Unit,
    onAnnounce: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PickoTheme.colors
    val density = LocalDensity.current
    // Figma shadow_level_2 는 두 겹(Y 12·Blur 40·30%, Y 3·Blur 4·16%)이다. 글자 그림자는 한 겹만 되어 큰 쪽만 쓴다.
    val textShadow = remember(density) {
        with(density) {
            Shadow(
                color = Color.Black.copy(alpha = 0.3f),
                offset = Offset(0f, 12.dp.toPx()),
                blurRadius = 40.dp.toPx(),
            )
        }
    }
    val examples = stringArrayResource(R.array.home_voice_examples)
    val example = remember(examples) { examples.random() }

    BackHandler(onBack = onDismiss)
    VoiceAnnouncement(voice = voice, onAnnounce = onAnnounce)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.overlayDim)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TextGap),
        ) {
            PickoMicBadge(
                onDim = true,
                // 마이크를 누르면 다시 듣는다. 바깥 터치(닫기)로 넘어가지 않도록 여기서 클릭을 받는다.
                modifier = Modifier
                    .padding(bottom = PickoMicLabelGap - TextGap)
                    .noRippleClickable(onClick = onMicClick),
            )

            Text(
                text = stringResource(R.string.pickup_voice_retry_overlay),
                style = PickoTheme.typography.headline.headlineLarge,
                color = colors.backgroundLight,
                textAlign = TextAlign.Center,
            )
            Text(
                text = voiceMessage(voice, example),
                style = PickoTheme.typography.body.bodyLargeVoice.copy(shadow = textShadow),
                color = colors.backgroundLight,
                textAlign = TextAlign.Center,
            )
        }
    }
}
