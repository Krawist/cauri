package com.cauri.feature.onboarding.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.ChoiceSurface
import com.cauri.feature.onboarding.R

private val ChipHeight = 44.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OccupationChoices(selected: Occupation?, onSelect: (Occupation) -> Unit) {
    FlowRow(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
    ) {
        Occupation.entries.forEach { occupation ->
            ChoiceSurface(
                selected = occupation == selected,
                onSelect = { onSelect(occupation) },
                shape = CircleShape,
                modifier = Modifier.height(ChipHeight),
            ) {
                Text(
                    text = stringResource(occupation.label),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (occupation == selected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (occupation == selected) CauriTheme.palette.accentText else CauriTheme.palette.ink,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = CauriSpacing.l),
                )
            }
        }
    }
}

@Composable
internal fun IncomeTypeChoices(selected: IncomeType?, onSelect: (IncomeType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
    ) {
        IncomeType.entries.forEach { incomeType ->
            ChoiceSurface(
                selected = incomeType == selected,
                onSelect = { onSelect(incomeType) },
                shape = RoundedCornerShape(CauriRadius.tile),
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
                    verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs),
                ) {
                    Text(
                        text = stringResource(incomeType.label),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (incomeType == selected) CauriTheme.palette.accentText else CauriTheme.palette.ink,
                    )
                    Text(
                        text = stringResource(incomeType.hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = CauriTheme.palette.muted,
                    )
                }
            }
        }
    }
}

private val Occupation.label: Int
    @StringRes get() = when (this) {
        Occupation.EMPLOYEE -> R.string.profile_occupation_employee
        Occupation.SELF_EMPLOYED -> R.string.profile_occupation_self_employed
        Occupation.TRADER -> R.string.profile_occupation_trader
        Occupation.STUDENT -> R.string.profile_occupation_student
        Occupation.OTHER -> R.string.profile_occupation_other
    }

private val IncomeType.label: Int
    @StringRes get() = when (this) {
        IncomeType.FIXED_DATE -> R.string.profile_income_fixed
        IncomeType.VARIABLE -> R.string.profile_income_variable
    }

private val IncomeType.hint: Int
    @StringRes get() = when (this) {
        IncomeType.FIXED_DATE -> R.string.profile_income_fixed_hint
        IncomeType.VARIABLE -> R.string.profile_income_variable_hint
    }
