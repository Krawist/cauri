package com.cauri.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 44.dp

/**
 * Warns, while it lasts, that Cauri received no message for a month and misses a permission; « Vérifier » opens
 * Réglages through [onCheck], and the card goes once the permissions are back.
 */
@Composable
internal fun CaptureHealth(onCheck: () -> Unit, viewModel: CaptureHealthViewModel = hiltViewModel()) {
    val paused by viewModel.paused.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    if (paused) CaptureHealthCard(onCheck = onCheck)
}

@Composable
private fun CaptureHealthCard(onCheck: () -> Unit) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.hero), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_bell),
                    contentDescription = null,
                    tint = palette.coralText,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.home_capture_label),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.coralText,
                )
            }
            Text(
                text = stringResource(R.string.home_capture_title),
                style = MaterialTheme.typography.titleLarge,
                color = palette.ink,
            )
            Text(
                text = stringResource(R.string.home_capture_text),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            Button(
                onClick = onCheck,
                modifier = Modifier.padding(top = CauriSpacing.s).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.home_capture_check), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview
@Composable
private fun CaptureHealthCardPreview() {
    CauriTheme { CaptureHealthCard(onCheck = {}) }
}
