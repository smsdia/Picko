package com.offcourse.picko.presentation.pickup.resolve

import android.Manifest
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.R
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.core.designsystem.component.PickoCarPreview
import com.offcourse.picko.core.designsystem.component.button.PickoButton
import com.offcourse.picko.core.designsystem.component.layout.PickoScreenFrame
import com.offcourse.picko.core.designsystem.icon.PickoIcons
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.pickup.PickupFlowState
import com.offcourse.picko.presentation.pickup.PickupFlowViewModel
import com.offcourse.picko.presentation.pickup.PickupStep
import com.offcourse.picko.presentation.pickup.component.LoadingContent
import com.offcourse.picko.presentation.pickup.component.PickupFlowEffects

/** 흐름의 첫 화면. 연락처를 찾는 동안 보이고, 결과에 따라 다음 단계로 넘어간다. */
@Composable
internal fun ResolveRoute(
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
