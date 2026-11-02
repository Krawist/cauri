package com.cauri.feature.onboarding.permissions

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.LocalOnlyNote
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.R

/**
 * Background location, explained apart from the location itself as Android requires. From Android 11, Android opens
 * its settings instead of a question. [onDone] ends the onboarding, whatever the answer.
 */
@Composable
internal fun BackgroundLocationScreen(onDone: () -> Unit) {
    val askBackground = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onDone() }
    BackgroundLocationContent(
        onAllow = { askBackground.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) },
        onLater = onDone,
    )
}

@Composable
private fun BackgroundLocationContent(onAllow: () -> Unit, onLater: () -> Unit) {
    OnboardingStep(
        step = PERMISSIONS_STEP,
        title = stringResource(R.string.background_location_title),
        subtitle = stringResource(R.string.background_location_subtitle),
        onContinue = onAllow,
        complete = true,
        continueLabel = stringResource(R.string.background_location_allow),
        note = { LocalOnlyNote() },
        onLater = onLater,
    ) {
        PermissionPoints {
            PermissionPoint(
                icon = DesignR.drawable.ic_place,
                title = R.string.background_location_choice,
                text = R.string.background_location_choice_text,
                separated = false,
            )
            PermissionPoint(
                icon = R.drawable.ic_info,
                title = R.string.background_location_change,
                text = R.string.background_location_change_text,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BackgroundLocationContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            BackgroundLocationContent(onAllow = {}, onLater = {})
        }
    }
}
