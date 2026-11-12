package com.offcourse.picko.presentation.pickup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.component.indicator.PickoLoadingIndicator
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/** 로고와 로딩 점, 안내 문구가 가운데에 놓이는 대기 화면의 내용 */
@Composable
internal fun LoadingContent(
    message: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = PickoIcons.Logo,
                contentDescription = null,
                modifier = Modifier.size(width = 308.dp, height = 90.dp),
                tint = PickoTheme.colors.brandLogo,
            )

            PickoLoadingIndicator()

            Text(
                text = message,
                style = PickoTheme.typography.label.labelLarge,
                color = PickoTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** [template] 안의 [highlight]에 해당하는 부분만 [color]로 칠한다. 없으면 그대로 돌려준다. */
internal fun highlightedText(
    template: String,
    highlight: String,
    color: Color,
): AnnotatedString = buildAnnotatedString {
    val start = template.indexOf(highlight)

    if (start < 0 || highlight.isEmpty()) {
        append(template)
        return@buildAnnotatedString
    }

    append(template.substring(0, start))
    withStyle(SpanStyle(color = color)) { append(highlight) }
    append(template.substring(start + highlight.length))
}
