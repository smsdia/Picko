package com.offcourse.picko.core.designsystem.component.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme

// Figma `window/apps` 영역(1700×1185)의 모서리와 머리글 치수
private val FrameCorner = 40.dp
private val HeaderTopPadding = 53.dp
private val HeaderGap = 40.dp
private val HeaderDividerSidePadding = 50.dp
private val LogoStartPadding = 90.dp
private val ContentMaxWidth = 1520.dp

/**
 * 픽업 흐름 화면의 공통 틀. 그라데이션 배경 위에 로고 머리글과 구분선을 두고, 그 아래에 내용을 놓는다.
 * 내용은 [ContentMaxWidth]까지만 넓어지고 가운데에 맞춘다.
 */
@Composable
fun PickoScreenFrame(
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = PickoTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(FrameCorner)),
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(PickoTheme.images.gradientBackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.fillMaxSize()) {
            if (showHeader) {
                Spacer(modifier = Modifier.height(HeaderTopPadding))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = LogoStartPadding),
                ) {
                    Icon(
                        imageVector = PickoIcons.Logo,
                        contentDescription = null,
                        tint = colors.brandLogo,
                    )
                }

                Spacer(modifier = Modifier.height(HeaderGap))

                Box(
                    modifier = Modifier
                        .padding(horizontal = HeaderDividerSidePadding)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.dividerStrong),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                ) {
                    content()
                }
            }
        }
    }
}
