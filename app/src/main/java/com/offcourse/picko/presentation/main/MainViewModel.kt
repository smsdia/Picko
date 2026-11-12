package com.offcourse.picko.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offcourse.picko.core.common.util.UiState
import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.repository.ContactRepository
import com.offcourse.picko.data.gleo.model.GleoActionResponseModel
import com.offcourse.picko.data.gleo.model.GleoActionStatus
import com.offcourse.picko.data.gleo.repository.GleoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gleoRepository: GleoRepository,
    private val contactRepository: ContactRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private var lookupJob: Job? = null

    // Gleo 는 호출당 콜백 하나만 기다린다. 권한을 허용해 다시 조회해도 두 번 보내지 않는다.
    private var hasRespondedToGleo = false

    fun onPickupRequested(actionParam: String?) {
        val request = gleoRepository.parsePickupRequest(actionParam)

        hasRespondedToGleo = false
        _state.update {
            it.copy(
                isGleoActionReceived = true,
                pickupRequest = request,
                contactLookup = null,
            )
        }

        if (request != null) {
            lookupContact(request.passenger)
        } else {
            respondToGleo(GleoActionStatus.FAIL, "I could not understand the pickup request.")
        }
    }

    fun onMapError(error: Exception) {
        // 인증 실패(키, 키 해시)는 401 같은 코드가 메시지에 담긴다. 좌표는 로그에 남기지 않는다.
        Timber.w("Kakao map error: %s", error.message)
        _state.update { it.copy(isMapFailed = true) }
    }

    fun onContactPermissionGranted() {
        _state.value.pickupRequest?.let { lookupContact(it.passenger) }
    }

    // TODO: 결과가 Multiple 이면 후보 선택 화면으로 넘기고, NotFound 이면 수동 검색으로 넘긴다.
    // TODO: 네트워크 호출(장소 검색)을 붙일 때 withTimeout(15초 상한)을 걸고, 초과나 취소에서도 fail 콜백이 한 번은 나가게 한다.
    //  Gleo 는 20초 안에 콜백이 없으면 타임아웃 안내를 하고 늦은 콜백은 버린다.
    private fun lookupContact(name: String) {
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
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
        }
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
}
