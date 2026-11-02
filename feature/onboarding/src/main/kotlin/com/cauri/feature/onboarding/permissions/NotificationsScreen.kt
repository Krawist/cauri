package com.cauri.feature.onboarding.permissions

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.LocalOnlyNote
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.R

internal const val PERMISSIONS_STEP = 5

/**
 * Notification access, the first permission: Android grants it in its settings, so the status is read again when the
 * user comes back. Once given, « Continuer » also asks to show Cauri's own notifications (Android 13+). [onDone] goes
 * on, whatever the answers.
 */
@Composable
internal fun NotificationsScreen(onDone: () -> Unit, viewModel: PermissionsViewModel = hiltViewModel()) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onDone()
    }
    NotificationsContent(
        accessGiven = status.notificationAccess,
        onAllow = { context.startActivity(viewModel.notificationAccessSettings()) },
        onContinue = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !status.notificationsAllowed) {
                askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onDone()
            }
        },
        onLater = onDone,
    )
}

@Composable
private fun NotificationsContent(
    accessGiven: Boolean,
    onAllow: () -> Unit,
    onContinue: () -> Unit,
    onLater: () -> Unit,
) {
    OnboardingStep(
        step = PERMISSIONS_STEP,
        title = stringResource(R.string.notifications_title),
        subtitle = stringResource(R.string.notifications_subtitle),
        onContinue = if (accessGiven) onContinue else onAllow,
        complete = true,
        continueLabel = stringResource(if (accessGiven) R.string.onboarding_continue else R.string.notifications_allow),
        note = { LocalOnlyNote() },
        onLater = if (accessGiven) null else onLater,
    ) {
        PermissionPoints {
            PermissionPoint(
                icon = R.drawable.ic_habit_phone,
                title = R.string.notifications_mobile_money,
                text = R.string.notifications_mobile_money_text,
                separated = false,
            )
            PermissionPoint(
                icon = R.drawable.ic_bank,
                title = R.string.notifications_banks,
                text = R.string.notifications_banks_text,
            )
            PermissionPoint(
                icon = DesignR.drawable.ic_message,
                title = R.string.notifications_sms,
                text = R.string.notifications_sms_text,
            )
        }
        if (accessGiven) AccessGiven() else SettingsHint()
    }
}

@Composable
private fun AccessGiven() {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .padding(top = CauriSpacing.l)
            .background(palette.greenSoft, CircleShape)
            .padding(horizontal = CauriSpacing.m + 2.dp, vertical = CauriSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(DesignR.drawable.ic_check), contentDescription = null, tint = palette.greenText)
        Text(
            text = stringResource(R.string.notifications_given),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.greenText,
        )
    }
}

@Composable
private fun SettingsHint() {
    Text(
        text = stringResource(R.string.notifications_settings_hint),
        style = MaterialTheme.typography.bodySmall,
        color = CauriTheme.palette.muted,
        modifier = Modifier.padding(top = CauriSpacing.l),
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationsContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            NotificationsContent(accessGiven = false, onAllow = {}, onContinue = {}, onLater = {})
        }
    }
}
