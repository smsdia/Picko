package com.offcourse.picko.presentation.pickup

import androidx.compose.runtime.Immutable
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.route.model.PickupRoutes
import com.offcourse.picko.presentation.voice.VoiceListenState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** 픽업 흐름의 단계. [PickupFlowSideEffect.NavigateTo]가 이동할 곳을 가리킨다. */
enum class PickupStep { Contact, Place, Link, Nearby, Spot, Driving, Complete }

/**
 * 링크를 보내고 탑승자의 위치가 오기를 기다리는 진행 상태.
 * [Completed]는 위치를 받고 다음 화면을 준비하는 중이다. 다음 화면으로 넘어가기 전까지 로딩을 유지해, 확인 화면이 잠깐 다시 보이지 않게 한다.
 */
enum class LinkStatus { Idle, Sending, WaitingLocation, Completed }

/** 흐름을 이어 가지 못하게 한 사유. 대화상자로 알린다. */
enum class PickupNotice {
    LocationUnsupported,
    LocationPermissionDenied,
    LocationUnavailable,
    NaviUnavailable,
    NaviLowBattery,
    NaviRouteTooFar,
    NaviRouteTooClose,
    NaviRouteFailed,
}

@Immutable
data class PickupFlowState(
    val request: PickupRequestModel,
    // 조회를 시작하기 전이면 null
    val contactLookup: UiState<ContactLookupResult>? = null,
    val selectedContact: ContactModel? = null,
    val placeCandidates: ImmutableList<PlaceModel> = persistentListOf(),
    val nearbyCandidates: ImmutableList<PlaceModel> = persistentListOf(),
    // 장소를 말한 경로에서는 장소 확인 뒤에, 추천 경로에서는 주변 장소 추천 뒤에 정해진다.
    val selectedPlace: PlaceModel? = null,
    val linkStatus: LinkStatus = LinkStatus.Idle,
    val vehicle: Coordinate? = null,
    val passenger: Coordinate? = null,
    val routes: PickupRoutes? = null,
    val isVoiceRetryVisible: Boolean = false,
    // 재요청 오버레이에서 마이크를 눌러 다시 듣는 상태
    val voice: VoiceListenState = VoiceListenState(),
    val isCancelDialogVisible: Boolean = false,
    val notice: PickupNotice? = null,
) {
    /** 연락처 후보가 여럿일 때 고를 목록 */
    val contactCandidates: ImmutableList<ContactModel>
        get() = ((contactLookup as? UiState.Success)?.data as? ContactLookupResult.Multiple)
            ?.candidates
            ?: persistentListOf()
}

sealed interface PickupFlowSideEffect {
    data class NavigateTo(val step: PickupStep) : PickupFlowSideEffect

    /** 흐름이 끝났다(취소 또는 안내 종료). 처음 화면으로 돌아간다. */
    data object FinishFlow : PickupFlowSideEffect

    /** 재요청 오버레이에서 새 픽업 요청을 읽었다. 지금 흐름을 버리고 새 흐름을 시작한다. */
    data class StartPickup(val request: PickupRequestModel) : PickupFlowSideEffect
}
