package com.offcourse.picko.data.navi.repository

import com.offcourse.picko.data.navi.model.NaviDestinationModel
import com.offcourse.picko.data.navi.model.NaviGuidanceResult
import com.offcourse.picko.data.navi.model.NaviTripModel

/** 차량 내비(Pleos NaviHelper)로 소요 시간을 묻고 안내를 맡긴다. 경로의 좌표는 받을 수 없다. */
interface NaviRepository {
    /**
     * 안내를 시작하지 않고 차량 현재 위치에서 [destination]까지의 소요 시간과 거리를 묻는다.
     * 내비에 연결하지 못했거나 시간 안에 응답이 없으면 null 을 돌려준다.
     */
    suspend fun previewTrip(destination: NaviDestinationModel): NaviTripModel?

    /**
     * 차량 내비에 [destination]으로 경로 탐색과 안내를 요청한다. 내비 앱이 앱 영역에 올라와 우리 앱이 가려질 수 있다.
     * `requestRoute` 성공은 탐색 완료가 아니고 오류는 뒤늦게 이벤트로 오므로, 요청 뒤 잠시 기다려 온 오류를 결과로 돌려준다.
     */
    suspend fun startGuidance(destination: NaviDestinationModel): NaviGuidanceResult

    /**
     * 목적지 없이 차량 내비 화면만 앞으로 띄운다(`requestNavigationScreen(Foreground)`). 내비 앱이 앱 영역에 올라와 우리 앱이 가려진다.
     * 내비에 연결하지 못했으면 [NaviGuidanceResult.NotConnected]를 돌려준다.
     */
    suspend fun showNavigationScreen(): NaviGuidanceResult
}
