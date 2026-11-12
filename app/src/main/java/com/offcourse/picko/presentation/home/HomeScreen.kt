package com.offcourse.picko.presentation.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.common.extensions.noRippleClickable
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.indicator.PickoMicBadge
import com.offcourse.picko.core.designsystem.component.indicator.PickoPickupIndicator
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.presentation.pickup.component.PickupHeading

/**
 * 앱의 최상위 화면. 처음 열면 시작 안내를, 그 뒤로는 Gleo 호출을 기다리는 음성 대기를 보여 준다.
 * 최상위 화면에서 뒤로 가면 앱을 종료하지 않고 숨긴다.
 */
@Composable
internal fun HomeRoute(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val hideApp = { activity?.moveTaskToBack(true); Unit }

    BackHandler(onBack = hideApp)

    HomeScreen(
        state = state,
        onStartClick = viewModel::onStartClicked,
        onCancelClick = hideApp,
    )
}

@Composable
private fun HomeScreen(
    state: HomeState,
    onStartClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    when (state.isFirstLaunch) {
        // 저장된 값을 읽는 동안에는 틀만 보여 시작 안내와 음성 대기가 번갈아 깜빡이지 않게 한다.
        null -> PickoScreenFrame {}

        true -> HomeIntro(onStartClick = onStartClick)

        false -> HomeVoiceWaiting(onCancelClick = onCancelClick)
    }
}

@Composable
private fun HomeIntro(onStartClick: () -> Unit) {
    val colors = PickoTheme.colors

    PickoScreenFrame {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_intro_title),
                    style = PickoTheme.typography.headline.headlineLarge,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.home_intro_subtitle),
                    style = PickoTheme.typography.label.labelLarge,
                    color = colors.textSecondary,
                )
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .noRippleClickable(onClick = onStartClick),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_intro_start),
                        style = PickoTheme.typography.label.labelLargeBold,
                        color = colors.accentText,
                    )
                    Icon(
                        imageVector = PickoIcons.ArrowStart,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = colors.accentText,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(PickoTheme.images.mapPin3d),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(520.dp),
                )
                PickoPickupIndicator(
                    text = stringResource(R.string.home_badge_smart_pickup),
                    modifier = Modifier.align(Alignment.TopEnd),
                )
                PickoPickupIndicator(
                    text = stringResource(R.string.home_badge_voice_control),
                    modifier = Modifier.align(Alignment.CenterStart),
                )
                PickoPickupIndicator(
                    text = stringResource(R.string.home_badge_safe_location),
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }
}

@Composable
private fun HomeVoiceWaiting(onCancelClick: () -> Unit) {
    val colors = PickoTheme.colors

    PickoScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 29.dp, bottom = 48.dp),
        ) {
            PickupHeading(
                title = stringResource(R.string.home_voice_title),
                subtitle = stringResource(R.string.home_voice_subtitle),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            ) {
                PickoMicBadge()

                Text(
                    text = stringResource(R.string.home_voice_label),
                    style = PickoTheme.typography.title.titleLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.home_voice_example),
                    style = PickoTheme.typography.label.labelMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            PickoButton(
                text = stringResource(R.string.home_voice_cancel),
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PickoCarPreview
@Composable
private fun HomeIntroPreview() {
    PickoTheme {
        HomeScreen(
            state = HomeState(isFirstLaunch = true),
            onStartClick = {},
            onCancelClick = {},
        )
    }
}

@PickoCarPreview
@Composable
private fun HomeVoiceWaitingPreview() {
    PickoTheme {
        HomeScreen(
            state = HomeState(isFirstLaunch = false),
            onStartClick = {},
            onCancelClick = {},
        )
    }
}
