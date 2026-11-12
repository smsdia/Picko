package com.offcourse.picko.presentation.main

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.presentation.main.model.PickupSpotUiModel

// 서울역. 픽업 지점 추천이 만들어지기 전까지 지도 검증에 쓰는 고정 좌표다.
private val DummyPickupSpot = PickupSpotUiModel(
    latitude = 37.5547,
    longitude = 126.9707,
)

@Immutable
data class MainState(
    val isGleoActionReceived: Boolean = false,
    // 호출은 받았지만 파라미터를 읽지 못했으면 null
    val pickupRequest: PickupRequestModel? = null,
    // 조회를 시작하기 전이면 null
    val contactLookup: UiState<ContactLookupResult>? = null,
    // TODO: 고정 좌표를 추천 알고리즘이 계산한 픽업 지점으로 바꾼다.
    val pickupSpot: PickupSpotUiModel = DummyPickupSpot,
    val isMapFailed: Boolean = false,
)
