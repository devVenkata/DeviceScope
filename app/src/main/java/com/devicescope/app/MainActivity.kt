package com.devicescope.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.devicescope.app.presentation.navigation.AppNavigation
import com.devicescope.app.ui.theme.DeviceScopeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Allow the app content to respect system bar inserts.
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            DeviceScopeTheme {
                AppNavigation()
            }
        }
    }
}