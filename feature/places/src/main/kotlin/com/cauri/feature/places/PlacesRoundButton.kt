package com.cauri.feature.places

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

/** Round glass button at the top of the places screens: back, or a cross closing them. */
@Composable
internal fun PlacesRoundButton(@DrawableRes icon: Int, description: String, onClick: () -> Unit) {
    GlassSurface(
        shape = CircleShape,
        modifier = Modifier.size(CauriSpacing.touchTarget),
        interaction = Modifier.clickable(onClickLabel = description, role = Role.Button, onClick = onClick),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = CauriTheme.palette.ink,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
