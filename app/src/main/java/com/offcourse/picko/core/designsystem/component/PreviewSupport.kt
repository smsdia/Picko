package com.offcourse.picko.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/**
 * 차량 앱 영역(Figma `window/apps`, 1700×1185)에 라이트와 다크를 각각 그리는 Preview.
 * 대상 기기가 160dpi라서 dp와 Figma px가 같다.
 */
@Preview(name = "차량 라이트", widthDp = 1700, heightDp = 1185)
@Preview(name = "차량 다크", widthDp = 1700, heightDp = 1185, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class PickoCarPreview

/** [PickoCarPreview]가 그리는 영역의 배경. 테마는 시스템 설정(`uiMode`)을 따른다. */
@Composable
fun PickoPreviewSurface(content: @Composable () -> Unit) {
    PickoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PickoTheme.colors.backgroundSub)
                .padding(24.dp),
        ) {
            content()
        }
    }
}
