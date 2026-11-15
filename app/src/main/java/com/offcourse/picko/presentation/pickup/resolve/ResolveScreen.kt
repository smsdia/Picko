package com.offcourse.picko.presentation.pickup.resolve

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.dialog.PickoDialog
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.vehicle.model.VehiclePermission
import com.offcourse.picko.presentation.pickup.PickupFlowState
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.LoadingContent
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects
import com.offcourse.picko.presentation.pickup.component.PickupNoticeDialog

/** 흐름의 첫 화면. 연락처를 찾는 동안 보이고, 결과에 따라 다음 단계로 넘어간다. */
@Composable
fun ResolveRoute(
    viewModel: PickupFlowViewModel,
    onNavigate: (PickupStep) -> Unit,
    onFlowFinished: () -> Unit,
    onPickupRequested: (PickupRequestModel) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val requestContactPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) viewModel.onContactPermissionGranted()
    }

    // 시스템 권한 팝업(차량 위치, 연락처)은 "알 수 없는 작업" 같은 문구로 뜨기도 해서, 팝업 전에 필요한 이유를 먼저 알린다.
    // 확인을 누르면 시스템 팝업을 연다. 위치 안내가 끝나기 전에는 연락처 안내를 띄우지 않아 두 안내가 겹치지 않는다.
    var isLocationRationaleVisible by rememberSaveable { mutableStateOf(false) }
    var isContactRationaleVisible by rememberSaveable { mutableStateOf(false) }
    var isLocationRequestPending by rememberSaveable { mutableStateOf(false) }

    // 픽업 경로의 출발지에 차량 위치가 필요해서, 흐름이 시작될 때 한 번 위치 권한을 요청한다. 거부해도 연락처 확인은 계속된다.
    val context = LocalContext.current
    val requestLocationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isLocationRequestPending = false }

    LaunchedEffect(Unit) {
        val isLocationGranted = context.checkSelfPermission(VehiclePermission.LOCATION) ==
            PackageManager.PERMISSION_GRANTED

        if (!isLocationGranted) {
            isLocationRequestPending = true
            isLocationRationaleVisible = true
        }
    }

    val lookup = state.contactLookup
    val isContactPermissionNeeded = lookup is UiState.Error && lookup.throwable is SecurityException
    LaunchedEffect(isContactPermissionNeeded, isLocationRequestPending) {
        if (isContactPermissionNeeded && !isLocationRequestPending) isContactRationaleVisible = true
    }

    PickupFlowEffects(
        viewModel = viewModel,
        onNavigate = onNavigate,
        onFlowFinished = onFlowFinished,
        onPickupRequested = onPickupRequested,
    )

    ResolveScreen(
        state = state,
        onRequestContactPermission = { requestContactPermission.launch(Manifest.permission.READ_CONTACTS) },
    )

    // 장소를 검색하는 동안에도 이 화면이라, 위치를 받지 못했거나 카카오 호출 한도에 닿으면 여기서 알린다.
    PickupNoticeDialog(notice = state.notice, viewModel = viewModel)

    if (isLocationRationaleVisible) {
        PermissionRationaleDialog(
            title = stringResource(R.string.permission_location_title),
            description = stringResource(R.string.permission_location_description),
            onConfirm = {
                isLocationRationaleVisible = false
                requestLocationPermission.launch(VehiclePermission.LOCATION)
            },
        )
    }

    if (isContactRationaleVisible) {
        PermissionRationaleDialog(
            title = stringResource(R.string.permission_contact_title),
            description = stringResource(R.string.permission_contact_description),
            onConfirm = {
                isContactRationaleVisible = false
                requestContactPermission.launch(Manifest.permission.READ_CONTACTS)
            },
        )
    }
}

/** 시스템 권한 팝업 전에 권한이 필요한 이유를 알리는 대화상자. 바깥을 눌러 닫아도 확인과 같게 이어 간다. */
@Composable
private fun PermissionRationaleDialog(
    title: String,
    description: String,
    onConfirm: () -> Unit,
) {
    PickoDialog(
        title = title,
        description = description,
        primaryText = stringResource(R.string.notice_confirm),
        onPrimaryClick = onConfirm,
        onDismissRequest = onConfirm,
    )
}

@Composable
private fun ResolveScreen(
    state: PickupFlowState,
    onRequestContactPermission: () -> Unit,
) {
    val lookup = state.contactLookup

    PickoScreenFrame(showHeader = false) {
        if (lookup is UiState.Error) {
            ResolveError(
                isPermissionNeeded = lookup.throwable is SecurityException,
                onRequestContactPermission = onRequestContactPermission,
            )
        } else {
            LoadingContent(message = AnnotatedString(stringResource(R.string.main_contact_loading)))
        }
    }
}

@Composable
private fun ResolveError(
    isPermissionNeeded: Boolean,
    onRequestContactPermission: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Icon(
                imageVector = PickoIcons.Logo,
                contentDescription = null,
                modifier = Modifier.size(width = 308.dp, height = 90.dp),
                tint = PickoTheme.colors.brandLogo,
            )

            Text(
                text = stringResource(
                    if (isPermissionNeeded) R.string.main_contact_permission_needed else R.string.main_contact_error,
                ),
                style = PickoTheme.typography.label.labelLarge,
                color = PickoTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            if (isPermissionNeeded) {
                PickoButton(
                    text = stringResource(R.string.main_contact_permission_request),
                    onClick = onRequestContactPermission,
                )
            }
        }
    }
}

@PickoCarPreview
@Composable
private fun ResolveScreenPreview() {
    PickoTheme {
        ResolveScreen(
            state = PickupFlowState(request = PickupRequestModel(passenger = "현대")),
            onRequestContactPermission = {},
        )
    }
}
