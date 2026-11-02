package com.cauri.feature.onboarding.profile

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.R

// The onboarding has five steps: profile, payment habits, categories, accounts and permissions.
private const val PROFILE_STEP = 1
private const val ONBOARDING_STEPS = 5

private val StepWidth = 22.dp
private val StepHeight = 6.dp
private val FieldHeight = 56.dp
private val ButtonHeight = 56.dp
private val SelectedBorderWidth = 1.5.dp

/** Profile step of the onboarding: [onProfileSaved] is called once the three answers are saved. */
@Composable
internal fun ProfileScreen(onProfileSaved: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onProfileSaved()
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
    val palette = CauriTheme.palette
    // Transparent: the app draws the backdrop behind the onboarding.
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = CauriSpacing.xl)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = CauriSpacing.xl),
        ) {
            StepIndicator(current = PROFILE_STEP, total = ONBOARDING_STEPS)
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineLarge,
                color = palette.ink,
                modifier = Modifier.padding(top = CauriSpacing.xxl, bottom = CauriSpacing.xl),
            )
            QuestionLabel(R.string.profile_first_name)
            FirstNameField(
                value = state.firstName,
                missing = ProfileField.FIRST_NAME in state.missingFields,
                onValueChange = onFirstNameChange,
            )
            if (ProfileField.FIRST_NAME in state.missingFields) MissingAnswer(R.string.profile_first_name_missing)

            QuestionLabel(R.string.profile_occupation, modifier = Modifier.padding(top = CauriSpacing.xxl))
            OccupationChoices(selected = state.occupation, onSelect = onOccupationSelect)
            if (ProfileField.OCCUPATION in state.missingFields) MissingAnswer(R.string.profile_choice_missing)

            QuestionLabel(R.string.profile_income, modifier = Modifier.padding(top = CauriSpacing.xxl))
            IncomeTypeChoices(selected = state.incomeType, onSelect = onIncomeTypeSelect)
            if (ProfileField.INCOME_TYPE in state.missingFields) MissingAnswer(R.string.profile_choice_missing)
            Spacer(Modifier.height(CauriSpacing.xl))
        }
        LocalOnlyNote()
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(text = stringResource(R.string.profile_continue), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(CauriSpacing.l))
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    val palette = CauriTheme.palette
    val description = stringResource(R.string.profile_step, current, total)
    Row(
        modifier = Modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        repeat(total) { index ->
            Box(
                Modifier
                    .size(width = StepWidth, height = StepHeight)
                    .background(if (index < current) palette.accent else palette.trackStrong, CircleShape),
            )
        }
    }
}

@Composable
private fun QuestionLabel(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelMedium,
        color = CauriTheme.palette.muted,
        modifier = modifier.padding(bottom = CauriSpacing.s),
    )
}

@Composable
private fun MissingAnswer(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodySmall,
        color = CauriTheme.palette.coralText,
        modifier = Modifier.padding(top = CauriSpacing.s),
    )
}

@Composable
private fun FirstNameField(value: String, missing: Boolean, onValueChange: (String) -> Unit) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.control)
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        missing -> palette.coralText
        focused -> palette.accent
        else -> palette.glassBorder
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(color = palette.ink, fontSize = 18.sp),
        cursorBrush = SolidColor(palette.accent),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        modifier = Modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .onFocusChanged { focused = it.isFocused }
            .background(palette.field, shape)
            .border(SelectedBorderWidth, borderColor, shape),
        decorationBox = { field ->
            Box(
                modifier = Modifier.padding(horizontal = CauriSpacing.l + 2.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                field()
            }
        },
    )
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
                    firstName = "",
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
