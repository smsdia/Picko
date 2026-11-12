package com.offcourse.picko.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.offcourse.picko.R
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.presentation.main.component.PickupMap

private const val PHONE_SUFFIX_LENGTH = 4

// 네비게이션 연결점. 첫 화면이 생기면 NavHost 를 여기에 구성한다.
@Composable
fun MainScreen(
    state: MainState,
    onMapError: (Exception) -> Unit,
    onRequestContactPermission: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.app_name))

            val request = state.pickupRequest

            Text(
                text = when {
                    request != null -> stringResource(
                        R.string.main_pickup_received,
                        request.passenger,
                        request.place ?: stringResource(R.string.main_pickup_place_current),
                    )

                    state.isGleoActionReceived -> stringResource(R.string.main_pickup_invalid)
                    else -> stringResource(R.string.main_gleo_waiting)
                },
            )

            ContactLookupContent(
                lookup = state.contactLookup,
                onRequestContactPermission = onRequestContactPermission,
            )

            if (state.isMapFailed) {
                Text(text = stringResource(R.string.main_map_error))
            } else if (!LocalInspectionMode.current) {
                // 카카오 지도는 Preview 에서 그릴 수 없다.
                PickupMap(
                    spot = state.pickupSpot,
                    onMapError = onMapError,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ContactLookupContent(
    lookup: UiState<ContactLookupResult>?,
    onRequestContactPermission: () -> Unit,
) {
    when (lookup) {
        null -> Unit

        UiState.Loading -> Text(text = stringResource(R.string.main_contact_loading))

        is UiState.Success -> Text(
            text = when (val result = lookup.data) {
                is ContactLookupResult.Found -> stringResource(
                    R.string.main_contact_found,
                    result.contact.phoneNumber.filter(Char::isDigit).takeLast(PHONE_SUFFIX_LENGTH),
                )

                ContactLookupResult.NotFound -> stringResource(R.string.main_contact_not_found)

                is ContactLookupResult.Multiple -> stringResource(
                    R.string.main_contact_multiple,
                    result.candidates.size,
                )
            },
        )

        is UiState.Error -> if (lookup.throwable is SecurityException) {
            Text(text = stringResource(R.string.main_contact_permission_needed))

            Button(onClick = onRequestContactPermission) {
                Text(text = stringResource(R.string.main_contact_permission_request))
            }
        } else {
            Text(text = stringResource(R.string.main_contact_error))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    PickoTheme {
        MainScreen(
            state = MainState(),
            onMapError = {},
            onRequestContactPermission = {},
        )
    }
}
