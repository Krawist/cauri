package com.cauri.feature.goals

import com.cauri.core.data.model.Goal
import com.cauri.core.data.model.GoalKind
import com.cauri.core.data.model.GoalProgress
import com.cauri.core.data.model.GoalsOverview
import com.cauri.core.data.model.NewGoal
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakeGoalRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class GoalsViewModelTest {
    private val goals = FakeGoalRepository()
    private lateinit var viewModel: GoalsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = GoalsViewModel(
            goals = goals,
            categories = FakeCategoryRepository(),
            clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnShown {
        @Test
        fun `should create the emergency fund`() {
            // when
            viewModel.onShown("Fonds d'urgence")

            // then
            goals.emergencyFunds shouldContainExactly listOf("Fonds d'urgence")
        }
    }

    @Nested
    inner class OnDraftSave {
        @Test
        fun `should add a tontine typed, with its day and payout`() {
            // given
            viewModel.onEdit(null)
            viewModel.onDraftChange(
                GoalDraft(
                    kind = GoalKind.TONTINE,
                    name = "Tontine du quartier",
                    amount = "25 000",
                    dayOfMonth = 10,
                    payoutDate = LocalDate.parse("2026-12-15"),
                ),
            )

            // when
            viewModel.onDraftSave()

            // then
            goals.added shouldContainExactly listOf(
                NewGoal(
                    name = "Tontine du quartier",
                    kind = GoalKind.TONTINE,
                    amount = 25_000,
                    payoutDate = LocalDate.parse("2026-12-15"),
                    dayOfMonth = 10,
                ),
            )
            viewModel.uiState.value.draft.shouldBeNull()
        }

        @Test
        fun `should point out an envelope without category`() {
            // given
            viewModel.onEdit(null)
            viewModel.onDraftChange(GoalDraft(kind = GoalKind.ENVELOPE, name = "Imprévus", amount = "30000"))

            // when
            viewModel.onDraftSave()

            // then
            viewModel.uiState.value.draft?.categoryMissing shouldBe true
            goals.added.shouldBeEmpty()
        }

        @Test
        fun `should replace the goal edited`() {
            // given
            viewModel.onEdit(progress(saved = 0))
            viewModel.onDraftChange(checkNotNull(viewModel.uiState.value.draft).copy(amount = "1500000"))

            // when
            viewModel.onDraftSave()

            // then
            goals.updated shouldContainExactly listOf(
                3L to NewGoal(name = "Moto", kind = GoalKind.SAVING, amount = 1_500_000, dayOfMonth = 1),
            )
        }
    }

    @Nested
    inner class OnMove {
        @ParameterizedTest
        @CsvSource("true, 50000, 50000", "false, 50000, -20000")
        fun `should put money aside, never taking back more than held`(putAside: Boolean, typed: Long, moved: Long) {
            // given
            viewModel.onEdit(progress(saved = 20_000))
            viewModel.onDraftChange(checkNotNull(viewModel.uiState.value.draft).copy(moving = typed.toString()))

            // when
            viewModel.onMove(putAside)

            // then
            goals.putAside shouldContainExactly listOf(3L to moved)
            viewModel.uiState.value.draft?.saved shouldBe 20_000 + moved
        }
    }

    @Nested
    inner class OnMoveFirst {
        @Test
        fun `should put the goal first`() {
            // given
            viewModel.onEdit(progress(saved = 0))

            // when
            viewModel.onMoveFirst()

            // then
            goals.movedFirst shouldContainExactly listOf(3L)
            viewModel.uiState.value.draft?.first shouldBe true
        }
    }

    @Nested
    inner class OnDraftDelete {
        @Test
        fun `should delete the goal edited`() {
            // given
            goals.overview.value = GoalsOverview(savedThisMonth = 0, goals = listOf(progress(saved = 0)))
            viewModel.onEdit(progress(saved = 0))

            // when
            viewModel.onDraftDelete()

            // then
            goals.deleted shouldContainExactly listOf(3L)
        }
    }

    private fun progress(saved: Long) = GoalProgress(
        goal = Goal(
            id = 3,
            name = "Moto",
            kind = GoalKind.SAVING,
            amount = 1_200_000,
            deadline = null,
            payoutDate = null,
            dayOfMonth = null,
            categoryId = null,
        ),
        saved = saved,
        estimate = null,
        receiving = true,
    )
}
