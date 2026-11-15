package com.offcourse.picko.presentation.pickup.component

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.component.dialog.PickoDialog
import com.offcourse.picko.data.vehicle.model.VehiclePermission
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupNotice

/**
 * 흐름을 이어 가지 못한 사유(위치를 받지 못함, 내비 안내 실패)를 알리는 대화상자.
 * 위치 사유에는 차량의 개인정보 보호 설정을 언급하고, 설정 확인 버튼으로 시스템 안내 팝업을 연다.
 */
@Composable
fun PickupNoticeDialog(
    notice: PickupNotice?,
    viewModel: PickupFlowViewModel,
) {
    if (notice == null) return

    val context = LocalContext.current
    val activity = LocalActivity.current
    val requestLocationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.onNoticeRetry() }

    val content = notice.content()
    val dismiss = viewModel::onNoticeDismissed

    // 첫 버튼과 둘째 버튼이 하는 일. 둘째가 없으면 확인 버튼 하나만 둔다.
    val onPrimary: () -> Unit
    val onSecondary: () -> Unit

    when (notice) {
        PickupNotice.LocationPermissionDenied -> {
            onPrimary = { requestLocationPermission.launch(VehiclePermission.LOCATION) }
            onSecondary = dismiss
        }

        PickupNotice.LocationUnavailable -> {
            onPrimary = viewModel::onNoticeRetry
            onSecondary = { viewModel.onLocationPrivacyCheckClicked(context) }
        }

        // 내비에 연결하지 못하면 이 앱으로 할 수 있는 일이 없어 앱을 종료한다.
        PickupNotice.NaviUnavailable -> {
            onPrimary = { activity?.finishAffinity() }
            onSecondary = dismiss
        }

        PickupNotice.NaviRouteFailed, PickupNotice.LinkSendFailed -> {
            onPrimary = viewModel::onNoticeRetry
            onSecondary = dismiss
        }

        PickupNotice.LocationUnsupported,
        PickupNotice.NaviLowBattery,
        PickupNotice.NaviRouteTooFar,
        PickupNotice.NaviRouteTooClose,
        PickupNotice.KakaoQuotaExceeded,
        PickupNotice.SessionEnded,
        -> {
            onPrimary = dismiss
            onSecondary = dismiss
        }
    }

    PickoDialog(
        title = stringResource(content.title),
        description = stringResource(content.description),
        primaryText = stringResource(content.primary),
        onPrimaryClick = onPrimary,
        onDismissRequest = dismiss,
        secondaryText = content.secondary?.let { stringResource(it) },
        onSecondaryClick = onSecondary,
    )
}

private class NoticeContent(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val primary: Int,
    @StringRes val secondary: Int? = null,
)

private fun PickupNotice.content(): NoticeContent = when (this) {
    PickupNotice.LocationUnsupported -> NoticeContent(
        title = R.string.notice_location_unsupported_title,
        description = R.string.notice_location_unsupported_description,
        primary = R.string.notice_confirm,
    )

    PickupNotice.LocationPermissionDenied -> NoticeContent(
        title = R.string.notice_location_permission_title,
        description = R.string.notice_location_permission_description,
        primary = R.string.notice_location_permission_allow,
        secondary = R.string.notice_close,
    )

    PickupNotice.LocationUnavailable -> NoticeContent(
        title = R.string.notice_location_unavailable_title,
        description = R.string.notice_location_unavailable_description,
        primary = R.string.notice_retry,
        secondary = R.string.notice_location_privacy_check,
    )

    PickupNotice.NaviUnavailable -> NoticeContent(
        title = R.string.notice_navi_unavailable_title,
        description = R.string.notice_navi_unavailable_description,
        primary = R.string.notice_exit_app,
    )

    PickupNotice.NaviLowBattery -> NoticeContent(
        title = R.string.notice_navi_low_battery_title,
        description = R.string.notice_navi_low_battery_description,
        primary = R.string.notice_confirm,
    )

    PickupNotice.NaviRouteTooFar -> NoticeContent(
        title = R.string.notice_navi_too_far_title,
        description = R.string.notice_navi_too_far_description,
        primary = R.string.notice_confirm,
    )

    PickupNotice.NaviRouteTooClose -> NoticeContent(
        title = R.string.notice_navi_too_close_title,
        description = R.string.notice_navi_too_close_description,
        primary = R.string.notice_confirm,
    )

    PickupNotice.NaviRouteFailed -> NoticeContent(
        title = R.string.notice_navi_route_failed_title,
        description = R.string.notice_navi_route_failed_description,
        primary = R.string.notice_retry,
        secondary = R.string.notice_close,
    )

    PickupNotice.KakaoQuotaExceeded -> NoticeContent(
        title = R.string.notice_kakao_quota_title,
        description = R.string.notice_kakao_quota_description,
        primary = R.string.notice_confirm,
    )

    PickupNotice.LinkSendFailed -> NoticeContent(
        title = R.string.notice_link_send_failed_title,
        description = R.string.notice_link_send_failed_description,
        primary = R.string.notice_retry,
        secondary = R.string.notice_close,
    )

    PickupNotice.SessionEnded -> NoticeContent(
        title = R.string.notice_session_ended_title,
        description = R.string.notice_session_ended_description,
        primary = R.string.notice_confirm,
    )
}
