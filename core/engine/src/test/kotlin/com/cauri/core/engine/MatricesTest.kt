package com.cauri.core.engine

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MatricesTest {
    @Nested
    inner class Inverse {
        @Test
        fun `should give back the identity once multiplied`() {
            // given
            val matrix = listOf(4.0, 1.0, 1.0, 3.0)

            // when
            val inverse = Matrices.inverse(matrix, size = 2)

            // then
            Matrices.times(matrix, listOf(inverse[0], inverse[2]), size = 2).map { it.plusOrMinus(1e-9) }
                .zip(listOf(1.0, 0.0)).forEach { (actual, expected) -> expected shouldBe actual }
        }
    }

    @Nested
    inner class Cholesky {
        @Test
        fun `should give the lower triangle of the matrix`() {
            // when
            val lower = Matrices.cholesky(listOf(4.0, 2.0, 2.0, 5.0), size = 2)

            // then
            lower shouldBe listOf(2.0, 0.0, 1.0, 2.0)
        }
    }
}
