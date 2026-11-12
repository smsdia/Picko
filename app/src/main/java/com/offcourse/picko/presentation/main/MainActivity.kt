package com.offcourse.picko.presentation.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offcourse.picko.core.designsystem.theme.PickoTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

// Gleo 는 schema.json 의 function.name 앞에 패키지명을 붙인 action 으로 호출한다.
// AndroidManifest 의 intent-filter action 과 같아야 한다.
private const val ACTION_REQUEST_PICKUP = "com.offcourse.picko.REQUEST_PICKUP"

// Gleo 가 function 파라미터를 JSON 문자열로 담아 보내는 extra 키
private const val EXTRA_ACTION_PARAM = "ACTION_PARAM"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestContactPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) viewModel.onContactPermissionGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 화면 회전 등으로 다시 만들어질 때 같은 호출을 두 번 처리하지 않는다.
        if (savedInstanceState == null) {
            handleGleoAction(intent)
        }

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()

            PickoTheme {
                MainScreen(
                    state = state,
                    onMapError = viewModel::onMapError,
                    onRequestContactPermission = {
                        requestContactPermission.launch(Manifest.permission.READ_CONTACTS)
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleGleoAction(intent)
    }

    private fun handleGleoAction(intent: Intent) {
        if (intent.action != ACTION_REQUEST_PICKUP) return

        // 파라미터에 이름, 위치 등 개인정보가 있어 값은 로그에 남기지 않는다.
        Timber.d("Gleo action received: %s", intent.action)
        viewModel.onPickupRequested(intent.getStringExtra(EXTRA_ACTION_PARAM))
    }
}
