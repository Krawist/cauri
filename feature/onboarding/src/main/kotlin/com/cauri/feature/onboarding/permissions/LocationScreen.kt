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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.LocalOnlyNote
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.R

/**
 * Place reminders, optional: the location is asked only if the user enables them. Once allowed while Cauri is open,
 * [onForegroundAllowed] leads to the background access, explained apart; otherwise [onDone] ends the onboarding.
 */
@Composable
internal fun LocationScreen(
    onForegroundAllowed: () -> Unit,
    onDone: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel(),
) {
    val askLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.refresh()
        if (viewModel.status.value.location == LocationAccess.DENIED) onDone() else onForegroundAllowed()
    }
    LocationContent(
        onEnable = {
            // Already answered, after a back navigation: no need to ask again.
            when (viewModel.status.value.location) {
                LocationAccess.DENIED -> askLocation.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                )
                LocationAccess.WHILE_IN_USE -> onForegroundAllowed()
                LocationAccess.ALWAYS -> onDone()
            }
        },
        onLater = onDone,
    )
}

@Composable
private fun LocationContent(onEnable: () -> Unit, onLater: () -> Unit) {
    OnboardingStep(
        step = PERMISSIONS_STEP,
        title = stringResource(R.string.location_title),
        subtitle = stringResource(R.string.location_subtitle),
        onContinue = onEnable,
        complete = true,
        continueLabel = stringResource(R.string.location_enable),
        note = { LocalOnlyNote() },
        onLater = onLater,
    ) {
        PermissionPoints {
            PermissionPoint(
                icon = DesignR.drawable.ic_place,
                title = R.string.location_places,
                text = R.string.location_places_text,
                separated = false,
            )
            PermissionPoint(
                icon = DesignR.drawable.ic_bell,
                title = R.string.location_timing,
                text = R.string.location_timing_text,
            )
            PermissionPoint(
                icon = R.drawable.ic_info,
                title = R.string.location_optional,
                text = R.string.location_optional_text,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LocationContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            LocationContent(onEnable = {}, onLater = {})
        }
    }
}
