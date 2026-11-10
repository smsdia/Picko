package com.offcourse.picko.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.offcourse.picko.R
import com.offcourse.picko.core.designsystem.theme.PickoTheme

// 네비게이션 연결점. 첫 화면이 생기면 NavHost 를 여기에 구성한다.
@Composable
fun MainScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    PickoTheme {
        MainScreen()
    }
}
