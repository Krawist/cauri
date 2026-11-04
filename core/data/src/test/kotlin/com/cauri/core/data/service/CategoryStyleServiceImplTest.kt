package com.cauri.core.data.service

import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CategoryStyleServiceImplTest {
    private val service = CategoryStyleServiceImpl()

    @Nested
    inner class Icon {
        @ParameterizedTest
        @CsvSource(
            "Alimentation, FOOD",
            "Transport, TRANSPORT",
            "Logement et factures, HOUSE",
            "Crédit et forfaits, PHONE",
            "Santé, HEALTH",
            "Éducation, EDUCATION",
        )
        fun `should know the categories of the pack by their name, accents ignored`(name: String, icon: CategoryIcon) {
            // when & then
            service.icon(name, CategoryNature.EXPENSE) shouldBe icon
        }

        @Test
        fun `should give a name it does not know the icon of its nature`() {
            // when & then
            service.icon("Cadeaux reçus", CategoryNature.INCOME) shouldBe CategoryIcon.INCOME
        }
    }

    @Nested
    inner class Color {
        @Test
        fun `should give transport the gold of the mockup`() {
            // when & then
            service.color(CategoryIcon.TRANSPORT) shouldBe CategoryColor.GOLD
        }
    }
}
