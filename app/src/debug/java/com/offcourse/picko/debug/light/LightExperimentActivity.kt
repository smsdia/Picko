package com.offcourse.picko.debug.light

import android.app.UiModeManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offcourse.picko.core.designsystem.theme.PickoTheme

/**
 * 차량 주야간·조도 신호가 일반 안드로이드 경로로 오는지 확인하는 디버그 전용 화면.
 * 에뮬레이터에서 야간 모드를 바꾸거나 조도 센서 값을 바꿔 가며 아래 값이 따라오는지 본다.
 * `uiMode` 변경은 재생성 없이 받아야 변화 순간을 볼 수 있어 매니페스트에서 `configChanges`를 선언했다.
 */
class LightExperimentActivity : ComponentActivity(), SensorEventListener {
    private var uiModeNight by mutableStateOf(0)
    private var uiModeLog by mutableStateOf(emptyList<String>())
    private var lux by mutableStateOf<Float?>(null)
    private var luxSamples by mutableStateOf(0)
    private var hasLightSensor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        uiModeNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val sensorManager = getSystemService(SensorManager::class.java)
        val light = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        hasLightSensor = light != null
        if (light != null) sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)

        setContent {
            // 앱 테마가 시스템 신호를 그대로 따르는지 배경색으로도 눈에 보이게 한다.
            PickoTheme {
                LightExperimentScreen(
                    isSystemInDarkTheme = isSystemInDarkTheme(),
                    uiModeNight = uiModeNightLabel(uiModeNight),
                    uiModeManagerNight = uiModeManagerLabel(),
                    hasLightSensor = hasLightSensor,
                    lux = lux,
                    luxSamples = luxSamples,
                    log = uiModeLog,
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        uiModeNight = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        uiModeLog = listOf("uiMode 변경 → ${uiModeNightLabel(uiModeNight)}") + uiModeLog
    }

    override fun onSensorChanged(event: SensorEvent) {
        lux = event.values.firstOrNull()
        luxSamples += 1
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        getSystemService(SensorManager::class.java)?.unregisterListener(this)
        super.onDestroy()
    }

    private fun uiModeManagerLabel(): String {
        val manager = getSystemService(UiModeManager::class.java) ?: return "UiModeManager 없음"
        val night = when (manager.nightMode) {
            UiModeManager.MODE_NIGHT_AUTO -> "AUTO"
            UiModeManager.MODE_NIGHT_YES -> "YES"
            UiModeManager.MODE_NIGHT_NO -> "NO"
            UiModeManager.MODE_NIGHT_CUSTOM -> "CUSTOM"
            else -> "?(${manager.nightMode})"
        }
        val type = when (manager.currentModeType) {
            Configuration.UI_MODE_TYPE_CAR -> "CAR"
            Configuration.UI_MODE_TYPE_NORMAL -> "NORMAL"
            else -> "기타(${manager.currentModeType})"
        }
        return "nightMode=$night, currentModeType=$type"
    }

    private fun uiModeNightLabel(value: Int): String = when (value) {
        Configuration.UI_MODE_NIGHT_YES -> "NIGHT_YES"
        Configuration.UI_MODE_NIGHT_NO -> "NIGHT_NO"
        else -> "NIGHT_UNDEFINED"
    }
}

@Composable
private fun LightExperimentScreen(
    isSystemInDarkTheme: Boolean,
    uiModeNight: String,
    uiModeManagerNight: String,
    hasLightSensor: Boolean,
    lux: Float?,
    luxSamples: Int,
    log: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("주야간·조도 실험", style = MaterialTheme.typography.titleLarge)
        Text("isSystemInDarkTheme: $isSystemInDarkTheme", style = MaterialTheme.typography.bodyMedium)
        Text("Configuration.uiMode: $uiModeNight", style = MaterialTheme.typography.bodyMedium)
        Text("UiModeManager: $uiModeManagerNight", style = MaterialTheme.typography.bodyMedium)
        Text(
            if (hasLightSensor) "TYPE_LIGHT: ${lux ?: "값 없음"} lux (수신 ${luxSamples}회)" else "TYPE_LIGHT 센서 없음",
            style = MaterialTheme.typography.bodyMedium,
        )
        log.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}
