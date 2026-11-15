package com.offcourse.picko.debug.navi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/**
 * NaviHelper 가 차량 뷰에서 어떻게 보이는지 확인하는 디버그 전용 화면.
 * 앱의 픽업 흐름과 이어지지 않는다. 결과를 보고 정식 연동 방식을 정한다.
 */
class NaviExperimentActivity : ComponentActivity() {
    private lateinit var controller: NaviExperimentController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        controller = NaviExperimentController(applicationContext)
        controller.connect()

        setContent {
            PickoTheme {
                NaviExperimentScreen(controller)
            }
        }
    }

    override fun onDestroy() {
        controller.release()
        super.onDestroy()
    }
}

@Composable
private fun NaviExperimentScreen(controller: NaviExperimentController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("NaviHelper 실험", style = MaterialTheme.typography.titleLarge)
        Text("연결: ${controller.connectionState}", style = MaterialTheme.typography.bodyMedium)

        ExperimentButton("A-1 경로 미리보기 (requestTrip, 안내 시작 안 함)", controller::previewPickupTrip)
        ExperimentButton("A-2 픽업 장소로 경로 요청 (requestRoute)", controller::requestPickupRoute)
        ExperimentButton("B-1 내비 화면 앞으로 (Foreground)", controller::showNavigationForeground)
        ExperimentButton("B-2 경로 전체 보기 (OverviewRoute)", controller::showRouteOverview)
        ExperimentButton("C 목적지 안내 시작 (requestRoute, 선릉역)", controller::requestDestinationRoute)
        ExperimentButton("D 내비 앱을 LAUNCH_ADJACENT 로 띄우기", controller::launchNaviAdjacent)
        ExperimentButton("경로 취소 (cancelRoute)", controller::cancelRoute)
        ExperimentButton("상태 조회 (RouteState, Destination, TBT)", controller::queryStates)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(controller.logs) { line ->
                Text(line, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ExperimentButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}
