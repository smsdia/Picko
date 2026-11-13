package com.offcourse.picko.data.navi

import ai.pleos.playground.navi.constants.NaviErrorCode
import com.offcourse.picko.data.navi.model.NaviGuidanceResult
import com.offcourse.picko.data.navi.repositoryimpl.toGuidanceResult
import org.junit.Assert.assertEquals
import org.junit.Test

class NaviGuidanceResultTest {
    @Test
    fun `내비 오류 코드를 안내 결과로 바꾼다`() {
        assertEquals(NaviGuidanceResult.LowBattery, NaviErrorCode.CAUSE_LOW_BATTERY.toGuidanceResult())
        assertEquals(
            NaviGuidanceResult.RouteTooFar,
            NaviErrorCode.CAUSE_SEARCH_ROUTE_OVER_DISTANCE.toGuidanceResult(),
        )
        assertEquals(
            NaviGuidanceResult.RouteTooClose,
            NaviErrorCode.CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE.toGuidanceResult(),
        )
        assertEquals(NaviGuidanceResult.RouteFailed, NaviErrorCode.CAUSE_SEARCH_ROUTE.toGuidanceResult())
    }
}
