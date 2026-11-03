package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.data.model.AccountKind
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.RollingNumber
import com.cauri.core.design.component.dashedBorder
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val AccountCardWidth = 128.dp
private val MarkSize = 30.dp
private val MarkRadius = 10.dp
private val AddCardWidth = 64.dp
private val AddIconSize = 20.dp

// The margin of the home screen, which the cards scroll across up to the edges.
private val ScreenMargin = 20.dp

/**
 * « Mes comptes »: one card per digital account, side by side and scrolling sideways, its mark, its name and its
 * balance, the latest read in a message or typed, « — » before either. A tap on a card types its balance through
 * [onBalance]; the dashed « + » at the end adds one through [onAddAccount]. « Disponible aujourd'hui » joins it with
 * the budgets (CAU-46).
 */
@Composable
internal fun BalancesCard(
    balances: List<AccountBalance>,
    onBalance: (AccountBalance) -> Unit,
    onAddAccount: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp)) {
        Text(
            text = stringResource(R.string.home_balances_label),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = CauriTheme.palette.ink,
        )
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .bleed(ScreenMargin)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = ScreenMargin),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            balances.forEach { AccountCard(balance = it, onClick = { onBalance(it) }) }
            AddAccountCard(onClick = onAddAccount)
        }
    }
}

@Composable
private fun AccountCard(balance: AccountBalance, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    GlassSurface(
        shape = RoundedCornerShape(CauriRadius.tile),
        modifier = Modifier.width(AccountCardWidth),
        interaction = Modifier.clickable(
            onClickLabel = stringResource(R.string.home_balance_type),
            role = Role.Button,
            onClick = onClick,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = CauriSpacing.m + 2.dp, vertical = CauriSpacing.m),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
        ) {
            AccountMark(balance = balance)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = balance.account.name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = palette.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val amount = balance.amount
                if (amount == null) {
                    Text(
                        text = stringResource(R.string.home_balance_none),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.muted,
                    )
                } else {
                    // It rolls to the balance of a new message or of one typed.
                    RollingNumber(value = amount) { shown ->
                        Text(
                            text = stringResource(R.string.home_amount, formatFcfa(shown)),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.ink,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

// Dashed, as in the mockup, as tall as the cards beside it.
@Composable
private fun AddAccountCard(onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.tile)
    Box(
        modifier = Modifier
            .width(AddCardWidth)
            .fillMaxHeight()
            .clip(shape)
            .dashedBorder(palette.handle, radius = CauriRadius.tile)
            .clickable(
                onClickLabel = stringResource(R.string.home_add_account_title),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_nav_add),
            contentDescription = stringResource(R.string.home_add_account_title),
            tint = palette.muted,
            modifier = Modifier.size(AddIconSize),
        )
    }
}

// The operator's colours for MTN MoMo and Orange Money, a bank's columns, else the account's initial.
@Composable
private fun AccountMark(balance: AccountBalance) {
    val palette = CauriTheme.palette
    val account = balance.account
    val (background, content) = when {
        account.provider == MTN_MOMO -> palette.mtn to palette.onMtn
        account.provider in ORANGE_MONEY -> palette.orange to palette.onOrange
        else -> palette.accent to palette.onAccent
    }
    Box(
        modifier = Modifier.size(MarkSize).background(background, RoundedCornerShape(MarkRadius)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            account.provider == MTN_MOMO -> MarkText(text = stringResource(R.string.home_mark_mtn), color = content)
            account.provider in ORANGE_MONEY -> MarkText(
                text = stringResource(R.string.home_mark_orange),
                color = content,
            )
            account.kind == AccountKind.BANK -> Icon(
                painter = painterResource(R.drawable.ic_bank),
                contentDescription = null,
                tint = content,
            )
            else -> MarkText(text = account.name.take(1).uppercase(), color = content)
        }
    }
}

@Composable
private fun MarkText(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = color,
    )
}

// Widens the content by [margin] on both sides, out of the screen's margins: the cards scroll from edge to edge.
private fun Modifier.bleed(margin: Dp) = layout { measurable, constraints ->
    val extra = margin.roundToPx() * 2
    val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
}

// Providers of the accounts, as the onboarding and the messages name them.
private const val MTN_MOMO = "MTN_MOMO"
private val ORANGE_MONEY = setOf("ORANGE_MONEY", "ORANGE_MONEY_CARD")
