package com.cauri.feature.goals

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.Goal
import com.cauri.core.data.model.GoalKind
import com.cauri.core.data.model.GoalProgress
import com.cauri.core.data.model.GoalsOverview
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.RollingNumber
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// Above the floating bottom bar of the app.
private val BottomSpace = 120.dp
private val AmountSize = 40.sp
private val CurrencySize = 22.sp

/**
 * « Objectifs »: what was put aside this month, then each goal: a saving with its bar and when it should be reached
 * (gold while the savings go to it), a tontine with its contribution and its payout, an envelope with what is left of
 * its month. « + » adds one, a tap opens its sheet. The first time, the emergency fund is created.
 */
@Composable
internal fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fund = stringResource(R.string.goals_emergency_fund)
    LaunchedEffect(Unit) { viewModel.onShown(fund) }
    GoalsContent(state = state, actions = viewModel)
    state.draft?.let { GoalSheet(draft = it, categories = state.categories, actions = viewModel) }
}

@Composable
private fun GoalsContent(state: GoalsUiState, actions: GoalActions) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(
            start = CauriSpacing.xl,
            top = CauriSpacing.xxl,
            end = CauriSpacing.xl,
            bottom = BottomSpace,
        ),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
    ) {
        item { Header(onAdd = { actions.onEdit(null) }) }
        state.overview?.let { overview ->
            item { PutAsideCard(month = state.month, amount = overview.savedThisMonth) }
            items(overview.goals, key = { it.goal.id }) { progress ->
                when (progress.goal.kind) {
                    GoalKind.SAVING -> SavingCard(progress = progress, onClick = { actions.onEdit(progress) })
                    GoalKind.TONTINE -> TontineCard(goal = progress.goal, onClick = { actions.onEdit(progress) })
                    GoalKind.ENVELOPE -> EnvelopeCard(progress = progress, onClick = { actions.onEdit(progress) })
                }
            }
        }
    }
}

// « Objectifs » and the round « + ».
@Composable
private fun Header(onAdd: () -> Unit) {
    val palette = CauriTheme.palette
    val description = stringResource(R.string.goals_new)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.goals_title),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        GlassSurface(
            shape = CircleShape,
            modifier = Modifier.size(CauriSpacing.touchTarget),
            interaction = Modifier.clickable(onClickLabel = description, role = Role.Button, onClick = onAdd),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_nav_add),
                contentDescription = description,
                tint = palette.ink,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

// « Mis de côté en novembre », the amount large, and when it is put aside.
@Composable
private fun PutAsideCard(month: YearMonth, amount: Long) {
    val palette = CauriTheme.palette
    val locale = LocalConfiguration.current.locales[0]
    GlassSurface(shape = RoundedCornerShape(CauriRadius.hero), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s - 2.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.goals_put_aside,
                    month.format(DateTimeFormatter.ofPattern("MMMM", locale)),
                ),
                style = MaterialTheme.typography.labelLarge,
                color = palette.goldText,
            )
            RollingNumber(value = amount) { shown ->
                Text(
                    text = buildAnnotatedString {
                        append(formatFcfa(shown))
                        withStyle(
                            SpanStyle(fontSize = CurrencySize, fontWeight = FontWeight.SemiBold, color = palette.muted),
                        ) { append(" " + stringResource(R.string.goals_currency)) }
                    },
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = AmountSize,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = palette.ink,
                )
            }
            Text(
                text = stringResource(R.string.goals_put_aside_text),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
    }
}

// « juin 2027 », in the user's language.
@Composable
internal fun monthName(month: YearMonth): String =
    month.format(DateTimeFormatter.ofPattern("MMMM yyyy", LocalConfiguration.current.locales[0]))

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GoalsContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            GoalsContent(
                state = GoalsUiState(
                    month = YearMonth.parse("2026-11"),
                    overview = GoalsOverview(
                        savedThisMonth = 40_000,
                        goals = listOf(
                            GoalProgress(
                                goal = previewGoal(id = 1, name = "Moto", kind = GoalKind.SAVING, amount = 1_200_000),
                                saved = 420_000,
                                estimate = YearMonth.parse("2027-06"),
                                receiving = true,
                            ),
                            GoalProgress(
                                goal = previewGoal(id = 2, name = "Tontine", kind = GoalKind.TONTINE, amount = 25_000),
                                saved = 0,
                                estimate = null,
                                receiving = false,
                            ),
                        ),
                    ),
                ),
                actions = PreviewGoalActions,
            )
        }
    }
}

private fun previewGoal(id: Long, name: String, kind: GoalKind, amount: Long) = Goal(
    id = id,
    name = name,
    kind = kind,
    amount = amount,
    deadline = null,
    payoutDate = null,
    dayOfMonth = 10,
    categoryId = null,
)

/** Actions that do nothing, for previews. */
private object PreviewGoalActions : GoalActions {
    override fun onEdit(progress: GoalProgress?) = Unit

    override fun onDraftChange(draft: GoalDraft) = Unit

    override fun onDraftSave() = Unit

    override fun onMove(putAside: Boolean) = Unit

    override fun onMoveFirst() = Unit

    override fun onDraftDelete() = Unit

    override fun onEditClose() = Unit
}
