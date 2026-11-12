package com.offcourse.picko.data.gleo.repositoryimpl

import android.content.Context
import android.content.Intent
import com.offcourse.picko.data.gleo.datasource.PickupRequestParser
import com.offcourse.picko.data.gleo.dto.toDto
import com.offcourse.picko.data.gleo.model.GleoActionResponseModel
import com.offcourse.picko.data.gleo.model.PickupRequestModel
import com.offcourse.picko.data.gleo.repository.GleoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

// Gleo 가 코드에서 동적으로 등록한 수신기의 action 과 extra 키.
// extra 키는 문서에 없어 실제 음성 호출로 검증이 필요하다. (docs/references/pleos-sdk-index.md)
private const val ACTION_APP_ACTION_RESPONSE = "ai.pleos.playground.intent.action.APP_ACTION_RESPONSE"
private const val EXTRA_PACKAGE_NAME = "packageName"
private const val EXTRA_RESPONSE = "response"

class GleoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pickupRequestParser: PickupRequestParser,
) : GleoRepository {
    override fun parsePickupRequest(actionParam: String?): PickupRequestModel? =
        pickupRequestParser.parse(actionParam)

    // TODO: 수신기 패키지(ai.umos.ambientai)를 확인하고 setPackage 로 좁힌다. 지금은 암시적 브로드캐스트라 다른 앱도 받을 수 있다.
    override fun sendPickupResponse(response: GleoActionResponseModel) {
        context.sendBroadcast(
            Intent(ACTION_APP_ACTION_RESPONSE)
                .putExtra(EXTRA_PACKAGE_NAME, context.packageName)
                .putExtra(EXTRA_RESPONSE, Json.encodeToString(response.toDto())),
        )

        // message 에 이름이 들어 있어 내용은 남기지 않는다.
        Timber.d("Gleo response sent: %s", response.status)
    }
}
