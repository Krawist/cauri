package com.cauri.feature.cash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

/** Round glass back button at the top of the cash screens. */
@Composable
internal fun CashBackButton(onClick: () -> Unit) {
    val description = stringResource(R.string.cash_back)
    GlassSurface(
        shape = CircleShape,
        modifier = Modifier.size(CauriSpacing.touchTarget),
        interaction = Modifier.clickable(onClickLabel = description, role = Role.Button, onClick = onClick),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_back),
            contentDescription = description,
            tint = CauriTheme.palette.ink,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
