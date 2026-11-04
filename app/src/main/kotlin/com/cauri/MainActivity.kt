package com.cauri

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cauri.core.capture.reminder.EditFromNotification
import com.cauri.core.capture.security.DeviceSecurity
import com.cauri.core.design.theme.CauriTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val lock: AppLockViewModel by viewModels()

    @Inject
    lateinit var security: DeviceSecurity

    // Set when a notification opened the app, until its screen is shown.
    private var notificationTarget by mutableStateOf<NotificationTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a fresh start: a restored activity already showed the screen its intent asked for.
        if (savedInstanceState == null) opened(intent)
        hideWhileLocked()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val lockState by lock.uiState.collectAsStateWithLifecycle()
            CauriTheme(glassBlur = (application as CauriApplication).glassBlur) {
                CauriApp(
                    uiState = uiState,
                    onOnboardingCompleted = viewModel::onOnboardingCompleted,
                    notificationTarget = notificationTarget,
                    onNotificationTargetShown = { notificationTarget = null },
                    lock = lockState,
                    security = security,
                    onUnlocked = lock::onUnlocked,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lock.onBack(SystemClock.elapsedRealtime())
    }

    override fun onStop() {
        super.onStop()
        lock.onLeft(SystemClock.elapsedRealtime())
    }

    // While the lock is on, no preview in the recent apps and no screenshot nor screen recording of Cauri.
    private fun hideWhileLocked() {
        lifecycleScope.launch {
            lock.uiState.map { it.enabled == true }.distinctUntilChanged().collect { on ->
                if (on) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        opened(intent)
    }

    // The screen [intent] asks for, and the notification it came from cleared when it asks to.
    private fun opened(intent: Intent?) {
        NotificationTarget.of(intent)?.let { notificationTarget = it }
        intent?.getIntExtra(EditFromNotification.EXTRA_NOTIFICATION, 0)?.takeIf { it != 0 }?.let {
            NotificationManagerCompat.from(this).cancel(it)
        }
    }
}
