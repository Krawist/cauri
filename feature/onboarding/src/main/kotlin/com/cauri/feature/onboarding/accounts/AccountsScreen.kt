package com.cauri.feature.onboarding.accounts

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.Bank
import com.cauri.core.data.model.MobileMoney
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.model.knownBanks
import com.cauri.core.data.model.knownMobileMoney
import com.cauri.core.design.component.dashedBorder
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.ChoiceSurface
import com.cauri.feature.onboarding.OnboardingStep
import com.cauri.feature.onboarding.QuestionLabel
import com.cauri.feature.onboarding.R

private const val ACCOUNTS_STEP = 4
private const val ACCOUNT_CURRENCY = "XAF"
private val IconBoxSize = 40.dp
private val AddRowHeight = 56.dp

/** Accounts step of the onboarding: [onAccountsChosen] receives the accounts to create next to the Cash account. */
@Composable
internal fun AccountsScreen(
    onAccountsChosen: (List<NewAccount>) -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.answered) {
        state.answered?.let { answers ->
            onAccountsChosen(
                answers.mobileMoney.map {
                    NewAccount(
                        name = it.name,
                        kind = AccountKind.MOBILE_MONEY,
                        provider = it.code,
                        currency = ACCOUNT_CURRENCY,
                    )
                } + answers.banks.map {
                    NewAccount(name = it.name, kind = AccountKind.BANK, provider = it.code, currency = ACCOUNT_CURRENCY)
                },
            )
            viewModel.onAnswersHandedOn()
        }
    }
    AccountsContent(state = state, actions = viewModel)
    if (state.pickingBank) BankPicker(state = state, actions = viewModel)
}

@Composable
private fun AccountsContent(state: AccountsUiState, actions: AccountsActions) {
    val palette = CauriTheme.palette
    OnboardingStep(
        step = ACCOUNTS_STEP,
        title = stringResource(R.string.accounts_title),
        subtitle = stringResource(R.string.accounts_subtitle),
        onContinue = actions::onContinue,
        complete = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp)) {
            CashRow()
            knownMobileMoney.forEach { service ->
                AccountRow(
                    icon = R.drawable.ic_habit_phone,
                    name = service.name,
                    hint = service.description.ifEmpty { stringResource(R.string.accounts_mobile_money_hint) },
                    ticked = service in state.mobileMoney,
                    onToggle = { actions.onMobileMoneyToggle(service) },
                )
            }
        }

        QuestionLabel(R.string.accounts_banks, modifier = Modifier.padding(top = CauriSpacing.xxl))
        Text(
            text = stringResource(R.string.accounts_banks_why),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            modifier = Modifier.padding(bottom = CauriSpacing.m),
        )
        Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp)) {
            state.banks.forEach { bank ->
                BankRow(bank = bank, chosen = true, onToggle = { actions.onBankToggle(bank) })
            }
            if (state.banks.size < MAX_BANKS) {
                AddBankRow(onClick = actions::onBankPickerOpen)
            } else {
                Text(
                    text = stringResource(R.string.accounts_banks_max),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }
    }
}

// Cash is always tracked: shown ticked, and not a choice.
@Composable
private fun CashRow() {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.tile)
    GlassSurface(shape = shape, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.matchParentSize().background(palette.goldSoft))
        AccountRowContent(
            icon = R.drawable.ic_habit_cash,
            name = stringResource(R.string.accounts_cash),
            hint = stringResource(R.string.accounts_cash_hint),
            highlighted = true,
            gold = true,
        )
    }
}

@Composable
private fun AccountRow(@DrawableRes icon: Int, name: String, hint: String, ticked: Boolean, onToggle: () -> Unit) {
    ChoiceSurface(
        selected = ticked,
        onSelect = onToggle,
        shape = RoundedCornerShape(CauriRadius.tile),
        modifier = Modifier.fillMaxWidth(),
        role = Role.Checkbox,
    ) {
        AccountRowContent(icon = icon, name = name, hint = hint, highlighted = ticked, gold = false)
    }
}

/** A bank card, the same on the screen and in the list of banks: lagoon once chosen. */
@Composable
internal fun BankRow(bank: Bank, chosen: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    ChoiceSurface(
        selected = chosen,
        onSelect = onToggle,
        shape = RoundedCornerShape(CauriRadius.tile),
        modifier = modifier.fillMaxWidth(),
        role = Role.Checkbox,
    ) {
        AccountRowContent(
            icon = R.drawable.ic_bank,
            name = bank.name,
            hint = bank.description.ifEmpty { stringResource(R.string.accounts_bank_hint) },
            highlighted = chosen,
            gold = false,
            tintedIcon = true,
        )
    }
}

// Gold is for cash: the Cash row uses it, the other accounts use lagoon. A tinted icon keeps colour on cards that are
// not chosen yet.
@Composable
private fun AccountRowContent(
    @DrawableRes icon: Int,
    name: String,
    hint: String,
    highlighted: Boolean,
    gold: Boolean,
    tintedIcon: Boolean = false,
) {
    val palette = CauriTheme.palette
    val accentText = if (gold) palette.goldText else palette.accentText
    Row(
        modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(IconBoxSize)
                .background(
                    when {
                        gold -> palette.goldSoft
                        highlighted || tintedIcon -> palette.accentSoft
                        else -> palette.chip
                    },
                    RoundedCornerShape(CauriRadius.icon),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (highlighted || tintedIcon) accentText else palette.muted,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (highlighted) accentText else palette.ink,
            )
            Text(text = hint, style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
    }
}

@Composable
private fun AddBankRow(onClick: () -> Unit) {
    val palette = CauriTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AddRowHeight)
            .clip(RoundedCornerShape(CauriRadius.tile))
            .dashedBorder(palette.handle, radius = CauriRadius.tile)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.accounts_add_bank),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = palette.muted,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AccountsContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            AccountsContent(
                state = AccountsUiState(mobileMoney = setOf(knownMobileMoney[0]), banks = knownBanks.take(1)),
                actions = PreviewAccountsActions,
            )
        }
    }
}

/** Actions that do nothing, for previews. */
internal object PreviewAccountsActions : AccountsActions {
    override fun onMobileMoneyToggle(service: MobileMoney) = Unit

    override fun onBankPickerOpen() = Unit

    override fun onBankPickerClose() = Unit

    override fun onBankQueryChange(query: String) = Unit

    override fun onBankToggle(bank: Bank) = Unit

    override fun onContinue() = Unit
}
