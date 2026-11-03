package com.cauri.feature.onboarding.habits

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.ChoiceSurface
import com.cauri.feature.onboarding.MissingAnswer
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.QuestionLabel
import com.cauri.feature.onboarding.R

private const val HABITS_STEP = 2
private val IconBoxSize = 40.dp
private val AmountFieldHeight = 60.dp

/** Payment habits step of the onboarding: [onHabitsAnswered] receives both answers once given. */
@Composable
internal fun HabitsScreen(onHabitsAnswered: (HabitsAnswers) -> Unit, viewModel: HabitsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.answered) {
        state.answered?.let { answers ->
            onHabitsAnswered(answers)
            viewModel.onAnswersHandedOn()
        }
    }
    HabitsContent(
        state = state,
        onPaymentHabitSelect = viewModel::onPaymentHabitSelect,
        onCashAmountChange = viewModel::onCashAmountChange,
        onContinue = viewModel::onContinue,
    )
}

@Composable
private fun HabitsContent(
    state: HabitsUiState,
    onPaymentHabitSelect: (PaymentHabit) -> Unit,
    onCashAmountChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val palette = CauriTheme.palette
    OnboardingStep(
        step = HABITS_STEP,
        title = stringResource(R.string.habits_title),
        onContinue = onContinue,
        complete = state.complete,
    ) {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
        ) {
            PaymentHabit.entries.forEach { habit ->
                HabitChoice(habit = habit, selected = habit == state.paymentHabit, onSelect = {
                    onPaymentHabitSelect(habit)
                })
            }
        }
        if (HabitsField.PAYMENT_HABIT in state.missingFields) MissingAnswer(R.string.onboarding_choice_missing)

        QuestionLabel(R.string.habits_cash_amount, modifier = Modifier.padding(top = CauriSpacing.xxl))
        CauriTextField(
            value = state.cashDigits,
            onValueChange = onCashAmountChange,
            missing = HabitsField.CASH_AMOUNT in state.missingFields,
            textStyle = MaterialTheme.typography.headlineMedium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            height = AmountFieldHeight,
            visualTransformation = FcfaVisualTransformation,
            trailing = {
                Text(
                    text = stringResource(R.string.habits_currency),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = palette.muted,
                )
            },
        )
        if (HabitsField.CASH_AMOUNT in state.missingFields) {
            MissingAnswer(R.string.habits_cash_amount_missing)
        } else {
            Text(
                text = stringResource(R.string.habits_cash_amount_hint),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
                modifier = Modifier.padding(top = CauriSpacing.s),
            )
        }
    }
}

@Composable
private fun HabitChoice(habit: PaymentHabit, selected: Boolean, onSelect: () -> Unit) {
    val palette = CauriTheme.palette
    ChoiceSurface(
        selected = selected,
        onSelect = onSelect,
        shape = RoundedCornerShape(CauriRadius.tile),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(IconBoxSize)
                    .background(
                        if (selected) palette.accentSoft else palette.chip,
                        RoundedCornerShape(CauriRadius.icon),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(habit.icon),
                    contentDescription = null,
                    tint = if (selected) palette.accentText else palette.muted,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(habit.label),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) palette.accentText else palette.ink,
                )
                Text(
                    text = stringResource(habit.hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }
    }
}

private val PaymentHabit.icon: Int
    @DrawableRes get() = when (this) {
        PaymentHabit.MOSTLY_CASH -> R.drawable.ic_habit_cash
        PaymentHabit.BOTH -> R.drawable.ic_habit_both
        PaymentHabit.MOSTLY_PHONE -> R.drawable.ic_habit_phone
    }

private val PaymentHabit.label: Int
    @StringRes get() = when (this) {
        PaymentHabit.MOSTLY_CASH -> R.string.habits_cash
        PaymentHabit.BOTH -> R.string.habits_both
        PaymentHabit.MOSTLY_PHONE -> R.string.habits_phone
    }

private val PaymentHabit.hint: Int
    @StringRes get() = when (this) {
        PaymentHabit.MOSTLY_CASH -> R.string.habits_cash_hint
        PaymentHabit.BOTH -> R.string.habits_both_hint
        PaymentHabit.MOSTLY_PHONE -> R.string.habits_phone_hint
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitsContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            HabitsContent(
                state = HabitsUiState(paymentHabit = PaymentHabit.MOSTLY_CASH, cashDigits = "7500"),
                onPaymentHabitSelect = {},
                onCashAmountChange = {},
                onContinue = {},
            )
        }
    }
}
