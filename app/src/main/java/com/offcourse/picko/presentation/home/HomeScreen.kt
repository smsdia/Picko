package com.offcourse.picko.presentation.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.common.extensions.noRippleClickable
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.indicator.PickoMicBadge
import com.offcourse.picko.core.designsystem.component.indicator.PickoMicLabelGap
import com.offcourse.picko.core.designsystem.component.indicator.PickoPickupIndicator
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.speech.model.SpeechRecognitionError
import com.offcourse.picko.presentation.pickup.component.PickupHeading
import com.offcourse.picko.presentation.voice.VoiceAnnouncement
import com.offcourse.picko.presentation.voice.VoiceListenState
import com.offcourse.picko.presentation.voice.VoiceListenStatus
import com.offcourse.picko.presentation.voice.voiceMessage
import kotlin.math.roundToInt

/**
 * 앱의 최상위 화면. 처음 열면 시작 안내를, 그 뒤로는 말로 하는 픽업 요청을 듣는 음성 대기를 보여 준다.
 * 최상위 화면에서 뒤로 가면 앱을 종료하지 않고 숨긴다.
 */
@Composable
internal fun HomeRoute(
    viewModel: HomeViewModel,
    onPickupRequested: (PickupRequestModel) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val hideApp = { activity?.moveTaskToBack(true); Unit }
    val isVoiceScreen = state.isFirstLaunch == false

    BackHandler(onBack = hideApp)

    // 음성 대기 화면이 보이는 동안만 마이크를 켠다. 다른 화면이 덮거나 앱이 가려지면 끈다.
    LifecycleResumeEffect(isVoiceScreen) {
        if (isVoiceScreen) viewModel.onVoiceVisible()

        onPauseOrDispose { viewModel.onVoiceHidden() }
    }

    HomeEffects(viewModel = viewModel, onPickupRequested = onPickupRequested)

    HomeScreen(
        state = state,
        onStartClick = viewModel::onStartClicked,
        onCancelClick = hideApp,
        onMicClick = viewModel::onMicClicked,
        onVoiceAnnounce = viewModel::onVoiceAnnounce,
    )
}

/** 말로 읽은 픽업 요청을 호출 쪽 람다로 넘긴다. 화면이 `RESUMED`일 때만 수집한다. */
@Composable
private fun HomeEffects(
    viewModel: HomeViewModel,
    onPickupRequested: (PickupRequestModel) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPickupRequested = rememberUpdatedState(onPickupRequested)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    is HomeSideEffect.StartPickup -> currentOnPickupRequested.value(effect.request)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    state: HomeState,
    onStartClick: () -> Unit,
    onCancelClick: () -> Unit,
    onMicClick: () -> Unit,
    onVoiceAnnounce: (String) -> Unit,
) {
    when (state.isFirstLaunch) {
        // 저장된 값을 읽는 동안에는 틀만 보여 시작 안내와 음성 대기가 번갈아 깜빡이지 않게 한다.
        null -> PickoScreenFrame {}

        true -> HomeIntro(onStartClick = onStartClick)

        false -> HomeVoiceWaiting(
            voice = state.voice,
            onCancelClick = onCancelClick,
            onMicClick = onMicClick,
            onVoiceAnnounce = onVoiceAnnounce,
        )
    }
}

// Figma 메인화면 `application` 창(1700×1185) 기준 좌표. 기기 창 크기가 시안과 달라도 같은 비율로 놓는다.
// TODO(figma): 비율 배치가 어색하면 시안 절대값(dp)으로 되돌린다. 에뮬레이터 창은 1700×1210 정도로 시안보다 25dp 크다.
private const val DesignWidth = 1700f
private const val DesignHeight = 1185f
private val DesignHeaderHeight = 153.dp

private const val PinSize = 1058f
private const val PinX = 756f
private const val PinY = 99f
private const val SmartPickupX = 1371f
private const val SmartPickupY = 309f
private const val VoiceControlX = 884f
private const val VoiceControlY = 645f
private const val SafeLocationX = 1284f
private const val SafeLocationY = 973f

@Composable
private fun HomeIntro(onStartClick: () -> Unit) {
    val colors = PickoTheme.colors

    PickoScreenFrame(
        decoration = { HomeIntroArt() },
    ) {
        // 제목 묶음을 머리글을 뺀 영역이 아니라 창 전체의 세로 가운데에 둔다(시안 y=470).
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = DesignHeaderHeight),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
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
    }
}

/** 3D 핀과 기능 배지 3개. 창 크기에 비례해 놓는다. */
@Composable
private fun BoxScope.HomeIntroArt() {
    BoxWithConstraints(modifier = Modifier.matchParentSize()) {
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight
        val density = LocalDensity.current

        fun Modifier.at(x: Float, y: Float) = offset {
            IntOffset((x / DesignWidth * widthPx).roundToInt(), (y / DesignHeight * heightPx).roundToInt())
        }

        val pinSize = with(density) { (PinSize / DesignWidth * widthPx).toDp() }

        Image(
            bitmap = ImageBitmap.imageResource(PickoTheme.images.mapPin3d),
            contentDescription = null,
            modifier = Modifier.at(PinX, PinY).size(pinSize),
        )
        PickoPickupIndicator(
            text = stringResource(R.string.home_badge_smart_pickup),
            modifier = Modifier.at(SmartPickupX, SmartPickupY),
        )
        PickoPickupIndicator(
            text = stringResource(R.string.home_badge_voice_control),
            modifier = Modifier.at(VoiceControlX, VoiceControlY),
        )
        PickoPickupIndicator(
            text = stringResource(R.string.home_badge_safe_location),
            modifier = Modifier.at(SafeLocationX, SafeLocationY),
        )
    }
}

private val VoiceTextGap = 24.dp

@Composable
private fun HomeVoiceWaiting(
    voice: VoiceListenState,
    onCancelClick: () -> Unit,
    onMicClick: () -> Unit,
    onVoiceAnnounce: (String) -> Unit,
) {
    val colors = PickoTheme.colors
    // 화면이 보일 때마다 예시 문구 하나를 무작위로 고른다. 다시 구성돼도 바뀌지 않는다.
    val examples = stringArrayResource(R.array.home_voice_examples)
    val example = remember(examples) { examples.random() }

    VoiceAnnouncement(voice = voice, onAnnounce = onVoiceAnnounce)

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
                verticalArrangement = Arrangement.spacedBy(VoiceTextGap, Alignment.CenterVertically),
            ) {
                // 시안: 마이크 원 아래끝에서 제목 글자까지 약 100dp. halo가 퍼지는 자리를 비운다.
                PickoMicBadge(
                    modifier = Modifier
                        .padding(bottom = PickoMicLabelGap - VoiceTextGap)
                        .noRippleClickable(onClick = onMicClick),
                )

                Text(
                    text = stringResource(R.string.home_voice_label),
                    style = PickoTheme.typography.title.titleLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = voiceMessage(voice, example),
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
            onMicClick = {},
            onVoiceAnnounce = {},
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
            onMicClick = {},
            onVoiceAnnounce = {},
        )
    }
}

@PickoCarPreview
@Composable
private fun HomeVoiceListeningPreview() {
    PickoTheme {
        HomeScreen(
            state = HomeState(
                isFirstLaunch = false,
                voice = VoiceListenState(VoiceListenStatus.Listening, "엄마 강남역에서"),
            ),
            onStartClick = {},
            onCancelClick = {},
            onMicClick = {},
            onVoiceAnnounce = {},
        )
    }
}

@PickoCarPreview
@Composable
private fun HomeVoiceFailedPreview() {
    PickoTheme {
        HomeScreen(
            state = HomeState(
                isFirstLaunch = false,
                voice = VoiceListenState(VoiceListenStatus.Failed(SpeechRecognitionError.Network)),
            ),
            onStartClick = {},
            onCancelClick = {},
            onMicClick = {},
            onVoiceAnnounce = {},
        )
    }
}
