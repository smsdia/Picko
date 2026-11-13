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
import com.offcourse.picko.data.navi.model.NaviDestinationModel
import com.offcourse.picko.data.navi.repository.NaviRepository
import com.offcourse.picko.data.place.model.PlaceModel
import com.offcourse.picko.data.place.repository.PlaceRepository
import com.offcourse.picko.core.common.model.distanceMetersTo
import com.offcourse.picko.data.route.repository.RouteRepository
import com.offcourse.picko.data.speech.repository.SpeechSynthesisRepository
import com.offcourse.picko.data.vehicle.repository.VehicleRepository
import com.offcourse.picko.domain.pickup.usecase.FindPickupRoutesUseCase
import com.offcourse.picko.domain.pickup.usecase.ListenForPickupUseCase
import com.offcourse.picko.presentation.pickup.navigation.PickupFlowDestination
import com.offcourse.picko.presentation.voice.VoiceListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
    private val naviRepository: NaviRepository,
    private val vehicleRepository: VehicleRepository,
    private val findPickupRoutes: FindPickupRoutesUseCase,
    listenForPickup: ListenForPickupUseCase,
    speechSynthesis: SpeechSynthesisRepository,
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
    private var etaJob: Job? = null
    private var locationJob: Job? = null

    // Gleo 는 호출당 콜백 하나만 기다린다. 권한을 허용해 다시 조회해도 두 번 보내지 않는다.
    private var hasRespondedToGleo = false

    // 재요청 오버레이에서 마이크를 눌러 새 요청을 듣는다. 읽으면 지금 흐름을 버리고 새 흐름을 시작한다.
    private val voiceListener = VoiceListener(
        scope = viewModelScope,
        listenForPickup = listenForPickup,
        speechSynthesis = speechSynthesis,
        onRequested = { request -> _sideEffect.send(PickupFlowSideEffect.StartPickup(request)) },
    )

    init {
        viewModelScope.launch {
            voiceListener.state.collect { voice -> _state.update { it.copy(voice = voice) } }
        }

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

                // 다음 화면으로 넘어갈 때까지 로딩을 유지한다. 여기서 Idle 로 돌리면 확인 화면이 잠깐 다시 보인다.
                _state.update { it.copy(passenger = passenger, linkStatus = LinkStatus.Completed) }
                if (!proceedAfterPassengerLocation(passenger)) {
                    _state.update { it.copy(linkStatus = LinkStatus.Idle) }
                }
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

    /** 다음 화면에서 뒤로 돌아와 링크 화면이 다시 보일 때, 끝난 로딩 상태를 확인 화면으로 되돌린다. */
    fun onLinkScreenResumed() {
        _state.update { state ->
            if (state.linkStatus == LinkStatus.Completed) state.copy(linkStatus = LinkStatus.Idle) else state
        }
    }

    fun onNearbySelected(place: PlaceModel) {
        _state.update { it.copy(selectedPlace = place) }

        viewModelScope.launch { prepareSpot() }
    }

    fun onStartNavigationClicked() {
        vehicleRepository.onNavigationStarted()
        navigateTo(PickupStep.Driving)
        trackVehicleEta()
    }

    fun onPickupCompleted() {
        etaJob?.cancel()
        locationJob?.cancel()
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

    /** 차량 내비에 목적지 안내를 맡기고 흐름을 끝낸다. 픽업을 마치면 이 앱이 할 일은 없다. */
    fun onGuideClicked() {
        val from = _state.value.selectedPlace?.coordinate ?: return

        viewModelScope.launch {
            val route = routeRepository.findDestinationRoute(from)
            val destination = NaviDestinationModel(
                name = route.destinationName,
                coordinate = route.route.points.last(),
            )

            if (!naviRepository.startGuidance(destination)) {
                Timber.w("차량 내비에 연결하지 못해 안내를 시작하지 못했다")
            }

            finishFlow()
        }
    }

    // TODO: 음악 재생은 Pleos 에서 호출할 방법이 확인되면 구현한다.
    fun onMusicClicked() = Unit

    fun onVoiceRetryRequested() {
        _state.update { it.copy(isVoiceRetryVisible = true) }
    }

    fun onVoiceRetryDismissed() {
        voiceListener.stop()
        _state.update { it.copy(isVoiceRetryVisible = false) }
    }

    /** 재요청 오버레이의 마이크를 눌렀다. 다시 듣는다. */
    fun onVoiceRetryMicClicked() {
        voiceListener.start()
    }

    /** 오버레이에서 다시 말해야 하는 상태가 됐다. 안내를 소리로 읽고 다시 듣는다. */
    fun onVoiceAnnounce(text: String) {
        voiceListener.announceAndRetry(text)
    }

    /**
     * 픽업 지점으로 이동하는 동안 차량 내비에 소요 시간을 주기적으로 다시 묻고 안내 화면의 차량 도착 시간을 갱신한다.
     * 안내를 시작하지 않는 `requestTrip`이라 내비 화면이 올라오지 않는다. 내비가 답하지 못한 주기는 건너뛴다.
     */
    private fun trackVehicleEta() {
        val spot = _state.value.selectedPlace ?: return
        val destination = NaviDestinationModel(name = spot.name, coordinate = spot.coordinate)

        etaJob?.cancel()
        etaJob = viewModelScope.launch {
            while (true) {
                delay(VEHICLE_ETA_REFRESH_MILLIS)

                val trip = naviRepository.previewTrip(destination) ?: continue
                Timber.d("차량 도착 시간 갱신: %d분, %dm", trip.durationMinutes, trip.distanceMeters)

                _state.update { state ->
                    val routes = state.routes ?: return@update state

                    state.copy(
                        routes = routes.copy(vehicle = routes.vehicle.copy(durationMinutes = trip.durationMinutes)),
                    )
                }
            }
        }
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

                    // 못 찾은 이름으로는 이어 갈 화면이 없다. 처음 화면으로 돌아가고, 안내는 Gleo 콜백(FAIL)이 맡는다.
                    ContactLookupResult.NotFound -> finishFlow()
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

    /**
     * 탑승자의 위치를 받은 뒤: 장소를 이미 골랐으면 추천 지점으로, 아니면 주변 장소를 추천한다.
     * 다음 화면으로 넘어가지 못했으면 false.
     */
    private suspend fun proceedAfterPassengerLocation(passenger: Coordinate): Boolean {
        if (_state.value.selectedPlace != null) return prepareSpot()

        val nearby = placeRepository.recommendNearby(passenger)

        _state.update { it.copy(nearbyCandidates = nearby.toImmutableList()) }
        navigateTo(PickupStep.Nearby)
        return true
    }

    /** 픽업 경로를 만들고 추천 지점 화면으로 간다. 필요한 값이 없어 가지 못했으면 false. */
    private suspend fun prepareSpot(): Boolean {
        val state = _state.value
        val spot = state.selectedPlace ?: return false
        val passenger = state.passenger ?: return false
        val vehicle = vehicleCoordinate()
        if (vehicle == null) {
            // TODO: 차량 위치를 받지 못했을 때 화면 상태로 알린다. 지금은 현재 화면에 머문다.
            Timber.w("차량 위치를 받지 못해 픽업 경로를 만들지 못했다")
            return false
        }
        val routes = findPickupRoutes(
            vehicle = vehicle,
            passenger = passenger,
            pickup = spot.coordinate,
            pickupName = spot.name,
        )

        _state.update { it.copy(vehicle = vehicle, routes = routes) }
        navigateTo(PickupStep.Spot)
        trackVehicleLocation()
        return true
    }

    /**
     * 차량 위치가 바뀔 때마다 지도의 차량 마커를 옮긴다. 지도는 이 값으로 지나간 경로를 지우고 카메라를 다시 맞춘다.
     * 몇 미터 안의 흔들림은 건너뛴다.
     */
    private fun trackVehicleLocation() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            vehicleRepository.observeLocation().collect { location ->
                val position = Coordinate(latitude = location.latitude, longitude = location.longitude)

                _state.update { state ->
                    val last = state.vehicle
                    if (last != null && last.distanceMetersTo(position) < VEHICLE_MOVE_THRESHOLD_METERS) return@update state

                    state.copy(vehicle = position)
                }
            }
        }
    }

    /** 차량의 현재 위치. 시간 안에 받지 못하면 null. */
    private suspend fun vehicleCoordinate(): Coordinate? = withTimeoutOrNull(VEHICLE_LOCATION_TIMEOUT_MILLIS) {
        vehicleRepository.observeLocation().firstOrNull()
    }?.let { location -> Coordinate(latitude = location.latitude, longitude = location.longitude) }

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
        // 앱이 직접 들은 요청에는 기다리는 Gleo 호출이 없다.
        if (!destination.isGleoRequest || hasRespondedToGleo) return

        hasRespondedToGleo = true
        gleoRepository.sendPickupResponse(GleoActionResponseModel(status = status, message = message))
    }

    private fun navigateTo(step: PickupStep) {
        _sideEffect.trySend(PickupFlowSideEffect.NavigateTo(step))
    }

    private fun finishFlow() {
        etaJob?.cancel()
        locationJob?.cancel()
        _sideEffect.trySend(PickupFlowSideEffect.FinishFlow)
    }
}

// 차량 도착 시간을 내비에 다시 묻는 간격. 호출 하나가 약 0.3초 걸린다. NaviHelper 의 onDrivingInfo 는 약 5분 간격이라 쓰지 않는다.
private const val VEHICLE_ETA_REFRESH_MILLIS = 10_000L

// 차량 위치를 기다리는 상한. 위치가 1초마다 오므로 이 안에 오지 않으면 받을 수 없는 것으로 본다.
private const val VEHICLE_LOCATION_TIMEOUT_MILLIS = 5_000L

// 이보다 적게 움직인 위치 갱신은 지도에 반영하지 않는다.
private const val VEHICLE_MOVE_THRESHOLD_METERS = 10.0
