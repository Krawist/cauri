package com.cauri.feature.onboarding.security

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.R
import com.cauri.feature.onboarding.permissions.PermissionPoint
import com.cauri.feature.onboarding.permissions.PermissionPoints

internal const val SECURITY_STEP = 5

/**
 * « Protège ton argent »: what the lock of Cauri does, then « Activer le verrouillage », which asks the phone's
 * security once to know it works; a phone without screen lock is sent to its security settings first. « Plus tard »
 * never blocks: Réglages turns it on at any time. [onDone] goes on.
 */
@Composable
internal fun SecurityScreen(onDone: () -> Unit, viewModel: SecurityViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    SecurityContent(
        state = state,
        onEnable = {
            when {
                state.enabled -> onDone()
                state.available -> activity?.let { viewModel.onEnable(it, onDone) }
                else -> context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            }
        },
        onLater = onDone,
    )
}

@Composable
private fun SecurityContent(state: SecurityUiState, onEnable: () -> Unit, onLater: () -> Unit) {
    OnboardingStep(
        step = SECURITY_STEP,
        title = stringResource(R.string.security_title),
        subtitle = stringResource(R.string.security_subtitle),
        onContinue = onEnable,
        complete = true,
        continueLabel = stringResource(
            when {
                state.enabled -> R.string.onboarding_continue
                state.available -> R.string.security_enable
                else -> R.string.security_set_screen_lock
            },
        ),
        onLater = if (state.enabled) null else onLater,
    ) {
        PermissionPoints {
            PermissionPoint(
                icon = DesignR.drawable.ic_lock,
                title = R.string.security_unlock,
                text = R.string.security_unlock_text,
                separated = false,
            )
            PermissionPoint(
                icon = DesignR.drawable.ic_clock,
                title = R.string.security_minute,
                text = R.string.security_minute_text,
            )
            PermissionPoint(
                icon = R.drawable.ic_info,
                title = R.string.security_hidden,
                text = R.string.security_hidden_text,
            )
        }
        Text(
            text = stringResource(
                if (state.available) R.string.security_settings_hint else R.string.security_no_screen_lock,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = CauriTheme.palette.muted,
            modifier = Modifier.padding(top = CauriSpacing.l),
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SecurityContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            SecurityContent(state = SecurityUiState(available = true), onEnable = {}, onLater = {})
        }
    }
}
