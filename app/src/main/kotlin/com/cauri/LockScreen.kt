package com.cauri

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cauri.core.capture.security.DeviceSecurity
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import kotlinx.coroutines.launch

private val ButtonHeight = 56.dp

/**
 * Cauri locked: the launch screen, « Cauri est verrouillé » and « Déverrouiller », which shows Android's dialog of the
 * phone's security, asked at once as the screen shows. [onUnlocked] once the user is recognised.
 */
@Composable
internal fun LockScreen(security: DeviceSecurity, onUnlocked: () -> Unit) {
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val unlock: () -> Unit = {
        activity?.let { scope.launch { if (security.authenticate(it)) onUnlocked() } }
    }
    LaunchedEffect(Unit) { unlock() }
    LockContent(onUnlock = unlock)
}

@Composable
private fun LockContent(onUnlock: () -> Unit) {
    val palette = CauriTheme.palette
    LaunchScreen { 
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.l),
        ) {
            Text(
                text = stringResource(R.string.lock_text),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.muted,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onUnlock,
                modifier = Modifier.fillMaxWidth().height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.lock_unlock), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LockContentPreview() {
    CauriTheme { LockContent(onUnlock = {}) }
}
