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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    // Set when a notification opened the app, until its screen is shown.
    private var notificationTarget by mutableStateOf<NotificationTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a fresh start: a restored activity already showed the screen its intent asked for.
        if (savedInstanceState == null) notificationTarget = NotificationTarget.of(intent?.action)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CauriTheme(glassBlur = (application as CauriApplication).glassBlur) {
                CauriApp(
                    uiState = uiState,
                    onOnboardingCompleted = viewModel::onOnboardingCompleted,
                    notificationTarget = notificationTarget,
                    onNotificationTargetShown = { notificationTarget = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        NotificationTarget.of(intent.action)?.let { notificationTarget = it }
    }
}
