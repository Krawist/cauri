package com.cauri

import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class HelpBubblesViewModelTest {
    private val preferences = FakeUserPreferencesRepository()
    private lateinit var bubbles: HelpBubblesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        bubbles = HelpBubblesViewModel(preferences)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class IsShown {
        @Test
        fun `should show one bubble at a time, the first asked for`() {
            // when
            bubbles.request("cards")
            bubbles.request("habits")

            // then
            (bubbles.isShown("cards") to bubbles.isShown("habits")) shouldBe (true to false)
        }

        @Test
        fun `should never show a bubble closed before`() {
            // given
            preferences.seenHelp.value = setOf("cards")

            // when
            bubbles.request("cards")

            // then
            bubbles.isShown("cards").shouldBeFalse()
        }

        @Test
        fun `should show the next bubble once one leaves the screen`() {
            // given
            bubbles.request("cards")
            bubbles.request("habits")

            // when
            bubbles.release("cards")

            // then
            bubbles.isShown("habits").shouldBeTrue()
        }
    }

    @Nested
    inner class Close {
        @Test
        fun `should save the bubble closed and show the next one`() {
            // given
            bubbles.request("cards")
            bubbles.request("habits")

            // when
            bubbles.close("cards")

            // then
            preferences.seenHelp.value shouldBe setOf("cards")
            bubbles.isShown("habits").shouldBeTrue()
        }
    }
}
