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
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.NewAccount
import com.cauri.core.design.R as DesignR
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 44.dp

/** Offers to follow the account Cauri recognised messages of; shows nothing when there is none. */
@Composable
internal fun AccountProposal(viewModel: AccountProposalViewModel = hiltViewModel()) {
    val proposal by viewModel.proposal.collectAsStateWithLifecycle()
    proposal?.let { account ->
        AccountProposalCard(account = account, onChoice = { follow -> viewModel.onChoice(account.provider, follow) })
    }
}

@Composable
private fun AccountProposalCard(account: NewAccount, onChoice: (follow: Boolean) -> Unit) {
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
                    painter = painterResource(DesignR.drawable.ic_message),
                    contentDescription = null,
                    tint = palette.accentText,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.home_account_label),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.accentText,
                )
            }
            Text(
                text = stringResource(R.string.home_account_title, account.name),
                style = MaterialTheme.typography.titleLarge,
                color = palette.ink,
            )
            Text(
                text = stringResource(R.string.home_account_text, account.name),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            ProposalButtons(onChoice = onChoice)
        }
    }
}

@Composable
private fun ProposalButtons(onChoice: (follow: Boolean) -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = { onChoice(true) },
            modifier = Modifier.height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(text = stringResource(R.string.home_account_follow), style = MaterialTheme.typography.labelLarge)
        }
        TextButton(
            onClick = { onChoice(false) },
            modifier = Modifier.height(ButtonHeight),
            colors = ButtonDefaults.textButtonColors(contentColor = palette.muted),
        ) {
            Text(text = stringResource(R.string.home_account_ignore), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview
@Composable
private fun AccountProposalCardPreview() {
    CauriTheme {
        AccountProposalCard(
            account = NewAccount(
                name = "Orange Money",
                kind = AccountKind.MOBILE_MONEY,
                provider = "ORANGE_MONEY",
                currency = "XAF",
            ),
            onChoice = {},
        )
    }
}
