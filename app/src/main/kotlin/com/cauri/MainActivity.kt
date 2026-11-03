package com.cauri

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.cash.reminder.CountReminders
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    // Set when a count reminder opened the app, until the cash count is shown.
    private var openCashCount by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a fresh start: a restored activity already showed the count its intent asked for.
        if (savedInstanceState == null) openCashCount = intent?.action == CountReminders.ACTION_COUNT_CASH
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CauriTheme(glassBlur = (application as CauriApplication).glassBlur) {
                CauriApp(
                    uiState = uiState,
                    onOnboardingCompleted = viewModel::onOnboardingCompleted,
                    openCashCount = openCashCount,
                    onCashCountOpened = { openCashCount = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == CountReminders.ACTION_COUNT_CASH) openCashCount = true
    }
}
