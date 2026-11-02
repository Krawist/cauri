package com.cauri.feature.onboarding.profile

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.MissingAnswer
import com.cauri.feature.onboarding.OnboardingField
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.QuestionLabel
import com.cauri.feature.onboarding.R

private const val PROFILE_STEP = 1

/** Profile step of the onboarding: [onProfileAnswered] receives the three answers once given. */
@Composable
internal fun ProfileScreen(onProfileAnswered: (ProfileAnswers) -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.answered) {
        state.answered?.let { answers ->
            onProfileAnswered(answers)
            viewModel.onAnswersHandedOn()
        }
    }
    ProfileContent(
        state = state,
        onFirstNameChange = viewModel::onFirstNameChange,
        onOccupationSelect = viewModel::onOccupationSelect,
        onIncomeTypeSelect = viewModel::onIncomeTypeSelect,
        onContinue = viewModel::onContinue,
    )
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onFirstNameChange: (String) -> Unit,
    onOccupationSelect: (Occupation) -> Unit,
    onIncomeTypeSelect: (IncomeType) -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingStep(
        step = PROFILE_STEP,
        title = stringResource(R.string.profile_title),
        onContinue = onContinue,
        complete = state.complete,
        note = { LocalOnlyNote() },
    ) {
        QuestionLabel(R.string.profile_first_name)
        OnboardingField(
            value = state.firstName,
            onValueChange = onFirstNameChange,
            missing = ProfileField.FIRST_NAME in state.missingFields,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
        )
        if (ProfileField.FIRST_NAME in state.missingFields) MissingAnswer(R.string.profile_first_name_missing)

        QuestionLabel(R.string.profile_occupation, modifier = Modifier.padding(top = CauriSpacing.xxl))
        OccupationChoices(selected = state.occupation, onSelect = onOccupationSelect)
        if (ProfileField.OCCUPATION in state.missingFields) MissingAnswer(R.string.onboarding_choice_missing)

        QuestionLabel(R.string.profile_income, modifier = Modifier.padding(top = CauriSpacing.xxl))
        IncomeTypeChoices(selected = state.incomeType, onSelect = onIncomeTypeSelect)
        if (ProfileField.INCOME_TYPE in state.missingFields) MissingAnswer(R.string.onboarding_choice_missing)
    }
}

@Composable
private fun LocalOnlyNote() {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = CauriSpacing.m + 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(R.drawable.ic_lock), contentDescription = null, tint = palette.muted)
        Spacer(Modifier.width(CauriSpacing.xs + 2.dp))
        Text(
            text = stringResource(R.string.profile_local_only),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            ProfileContent(
                state = ProfileUiState(
                    occupation = Occupation.EMPLOYEE,
                    missingFields = setOf(ProfileField.FIRST_NAME, ProfileField.INCOME_TYPE),
                ),
                onFirstNameChange = {},
                onOccupationSelect = {},
                onIncomeTypeSelect = {},
                onContinue = {},
            )
        }
    }
}
