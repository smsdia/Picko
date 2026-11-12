package com.offcourse.picko.presentation.pickup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.offcourse.picko.core.common.model.Coordinate
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.contact.repository.ContactRepository
import com.offcourse.picko.data.gleo.model.GleoActionResponseModel
import com.offcourse.picko.data.gleo.model.GleoActionStatus
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.gleo.repository.GleoRepository
import com.offcourse.picko.data.pickuplink.repository.PickupLinkRepository
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.place.repository.PlaceRepository
import com.offcourse.picko.data.route.repository.RouteRepository
import com.offcourse.picko.data.vehicle.repository.VehicleRepository
import com.offcourse.picko.presentation.pickup.navigation.PickupFlowDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 연락처 확인부터 목적지 안내까지 한 번의 픽업 흐름이 공유하는 ViewModel.
 * 흐름의 NavGraph 항목에 묶이므로 흐름이 끝나 그래프가 pop되면 함께 사라지고, 새 요청은 새 상태로 시작한다.
 *
 * 경로는 둘이다.
 * - 장소를 말함: 연락처 확인 → 장소 확인 → 링크 발송 → 위치 공유 대기 → 픽업 지점 추천
 * - 추천을 요청함(장소 없음): 연락처 확인 → 링크 발송 → 위치 공유 대기 → 주변 장소 추천 → 픽업 지점 추천
 */
@HiltViewModel
class PickupFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gleoRepository: GleoRepository,
    private val contactRepository: ContactRepository,
    private val placeRepository: PlaceRepository,
    private val pickupLinkRepository: PickupLinkRepository,
    private val routeRepository: RouteRepository,
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<PickupFlowDestination>()

    private val _state = MutableStateFlow(
        PickupFlowState(request = PickupRequestModel(passenger = destination.passenger, place = destination.place)),
    )
    val state: StateFlow<PickupFlowState> = _state.asStateFlow()

    private val _sideEffect = Channel<PickupFlowSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<PickupFlowSideEffect> = _sideEffect.receiveAsFlow()

    private var lookupJob: Job? = null
    private var linkJob: Job? = null

    // Gleo 는 호출당 콜백 하나만 기다린다. 권한을 허용해 다시 조회해도 두 번 보내지 않는다.
    private var hasRespondedToGleo = false

    init {
        lookupContact()
    }

    fun onContactPermissionGranted() {
        lookupContact()
    }

    fun onContactSelected(contact: ContactModel) {
        _state.update { it.copy(selectedContact = contact, isVoiceRetryVisible = false) }

        viewModelScope.launch { proceedAfterContact() }
    }

    fun onPlaceSelected(place: PlaceModel) {
        _state.update { it.copy(selectedPlace = place, isVoiceRetryVisible = false) }
        navigateTo(PickupStep.Link)
    }

    fun onSendLinkClicked() {
        val contact = _state.value.selectedContact ?: return

        linkJob?.cancel()
        linkJob = viewModelScope.launch {
            try {
                _state.update { it.copy(linkStatus = LinkStatus.Sending) }
                pickupLinkRepository.sendLink(contact)

                _state.update { it.copy(linkStatus = LinkStatus.WaitingLocation) }
                val passenger = pickupLinkRepository.awaitPassengerLocation()

                _state.update { it.copy(passenger = passenger, linkStatus = LinkStatus.Idle) }
                proceedAfterPassengerLocation(passenger)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // TODO: 링크 전송 실패와 위치 공유 시간 초과는 연동이 확정되면 화면 상태로 알린다.
                Timber.w(e, "링크 발송 또는 위치 공유 대기에 실패했다")
                _state.update { it.copy(linkStatus = LinkStatus.Idle) }
            }
        }
    }

    /** 위치 공유를 기다리는 중에 뒤로 가면 기다림을 멈추고 확인 화면으로 돌아간다. */
    fun onLinkWaitCancelled() {
        linkJob?.cancel()
        _state.update { it.copy(linkStatus = LinkStatus.Idle) }
    }

    fun onNearbySelected(place: PlaceModel) {
        _state.update { it.copy(selectedPlace = place) }

        viewModelScope.launch { prepareSpot() }
    }

    fun onStartNavigationClicked() {
        navigateTo(PickupStep.Driving)
    }

    fun onPickupCompleted() {
        navigateTo(PickupStep.Complete)
    }

    fun onCancelRequested() {
        _state.update { it.copy(isCancelDialogVisible = true) }
    }

    fun onCancelContinued() {
        _state.update { it.copy(isCancelDialogVisible = false) }
    }

    fun onCancelConfirmed() {
        _state.update { it.copy(isCancelDialogVisible = false) }

        viewModelScope.launch {
            _state.value.selectedContact?.let { contact ->
                try {
                    pickupLinkRepository.notifyCancellation(contact)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // 알림이 가지 못해도 운전자의 취소는 그대로 진행한다.
                    Timber.w(e, "취소 알림을 보내지 못했다")
                }
            }

            finishFlow()
        }
    }

    fun onGuideClicked() {
        val from = _state.value.selectedPlace?.coordinate ?: return

        viewModelScope.launch {
            val route = routeRepository.findDestinationRoute(from)

            _state.update { it.copy(destinationRoute = route) }
            navigateTo(PickupStep.Guide)
        }
    }

    // TODO: 음악 재생은 Pleos 에서 호출할 방법이 확인되면 구현한다.
    fun onMusicClicked() = Unit

    fun onGuideFinished() {
        finishFlow()
    }

    fun onVoiceRetryRequested() {
        _state.update { it.copy(isVoiceRetryVisible = true) }
    }

    fun onVoiceRetryDismissed() {
        _state.update { it.copy(isVoiceRetryVisible = false) }
    }

    // TODO: 네트워크 호출(장소 검색)을 붙일 때 withTimeout(15초 상한)을 걸고, 초과나 취소에서도 fail 콜백이 한 번은 나가게 한다.
    //  Gleo 는 20초 안에 콜백이 없으면 타임아웃 안내를 하고 늦은 콜백은 버린다.
    private fun lookupContact() {
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            val name = _state.value.request.passenger

            _state.update { it.copy(contactLookup = UiState.Loading) }

            val lookup = try {
                UiState.Success(contactRepository.findByName(name))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e)
            }

            _state.update { it.copy(contactLookup = lookup) }
            respondToGleo(lookup, name)

            if (lookup is UiState.Success) {
                when (val result = lookup.data) {
                    is ContactLookupResult.Found -> {
                        _state.update { it.copy(selectedContact = result.contact) }
                        proceedAfterContact()
                    }

                    is ContactLookupResult.Multiple -> navigateTo(PickupStep.Contact)

                    ContactLookupResult.NotFound -> {
                        _state.update { it.copy(isVoiceRetryVisible = true) }
                        navigateTo(PickupStep.Contact)
                    }
                }
            }
        }
    }

    /** 연락처가 정해진 뒤: 장소를 말했으면 장소를 확인하고, 아니면 바로 링크를 보낸다. */
    private suspend fun proceedAfterContact() {
        val place = _state.value.request.place

        if (place == null) {
            navigateTo(PickupStep.Link)
            return
        }

        val candidates = placeRepository.searchPlaces(place)

        _state.update {
            it.copy(
                placeCandidates = candidates.toImmutableList(),
                isVoiceRetryVisible = candidates.isEmpty(),
            )
        }
        navigateTo(PickupStep.Place)
    }

    /** 탑승자의 위치를 받은 뒤: 장소를 이미 골랐으면 추천 지점으로, 아니면 주변 장소를 추천한다. */
    private suspend fun proceedAfterPassengerLocation(passenger: Coordinate) {
        if (_state.value.selectedPlace != null) {
            prepareSpot()
        } else {
            val nearby = placeRepository.recommendNearby(passenger)

            _state.update { it.copy(nearbyCandidates = nearby.toImmutableList()) }
            navigateTo(PickupStep.Nearby)
        }
    }

    private suspend fun prepareSpot() {
        val state = _state.value
        val spot = state.selectedPlace ?: return
        val passenger = state.passenger ?: return
        val vehicle = vehicleCoordinate()
        val routes = routeRepository.findPickupRoutes(vehicle = vehicle, passenger = passenger, pickup = spot.coordinate)

        _state.update { it.copy(vehicle = vehicle, routes = routes) }
        navigateTo(PickupStep.Spot)
    }

    private suspend fun vehicleCoordinate(): Coordinate = vehicleRepository.observeLocation().first().let { location ->
        Coordinate(latitude = location.latitude, longitude = location.longitude)
    }

    private fun respondToGleo(
        lookup: UiState<ContactLookupResult>,
        name: String,
    ) {
        when (lookup) {
            UiState.Loading -> Unit

            is UiState.Success -> when (val result = lookup.data) {
                is ContactLookupResult.Found -> respondToGleo(
                    GleoActionStatus.SUCCESS,
                    "Found the contact for $name.",
                )

                // TODO: 후보를 음성으로 고르게 하려면 ASKING 과 data(구분 정보)를 보낸다. 마이크 재개, 답변 라우팅, 서버로 가는 개인정보 범위를 먼저 확인한다.
                is ContactLookupResult.Multiple -> respondToGleo(
                    GleoActionStatus.SUCCESS,
                    "There are ${result.candidates.size} contacts named $name. Please choose one on the screen.",
                )

                ContactLookupResult.NotFound -> respondToGleo(
                    GleoActionStatus.FAIL,
                    "No contact named $name was found.",
                )
            }

            is UiState.Error -> respondToGleo(
                GleoActionStatus.FAIL,
                if (lookup.throwable is SecurityException) {
                    "Contact permission is required to find $name. Please allow it on the screen."
                } else {
                    "I could not read the contacts."
                },
            )
        }
    }

    private fun respondToGleo(
        status: GleoActionStatus,
        message: String,
    ) {
        if (hasRespondedToGleo) return

        hasRespondedToGleo = true
        gleoRepository.sendPickupResponse(GleoActionResponseModel(status = status, message = message))
    }

    private fun navigateTo(step: PickupStep) {
        _sideEffect.trySend(PickupFlowSideEffect.NavigateTo(step))
    }

    private fun finishFlow() {
        _sideEffect.trySend(PickupFlowSideEffect.FinishFlow)
    }
}
