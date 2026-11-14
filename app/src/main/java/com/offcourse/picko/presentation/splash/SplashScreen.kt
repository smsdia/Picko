package com.offcourse.picko.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import kotlinx.coroutines.delay

// 시안에 표시 시간이 없어서 임의로 정했다. TODO(figma): 시간이 정해지면 맞춘다.
private const val SPLASH_DURATION_MILLIS = 1_500L

/** 앱을 열면 잠깐 보이는 첫 화면 */
@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val currentOnFinished = rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MILLIS)
        currentOnFinished.value()
    }

    SplashScreen()
}

@Composable
private fun SplashScreen() {
    val colors = PickoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.splashBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Icon(
                imageVector = PickoIcons.Logo,
                contentDescription = null,
                modifier = Modifier.size(width = 530.dp, height = 154.dp),
                tint = colors.onSplash,
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                style = PickoTheme.typography.display.displayLargeBrand,
                color = colors.onSplash,
            )
        }
    }
}

@PickoCarPreview
@Composable
private fun SplashScreenPreview() {
    PickoTheme {
        SplashScreen()
    }
}
