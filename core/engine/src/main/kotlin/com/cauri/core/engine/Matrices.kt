package com.cauri.core.engine

import kotlin.math.sqrt

/** The few operations of linear algebra the bandit needs, on square matrices kept row by row in one list. */
internal object Matrices {
    /** The identity of [size] times [scale]. */
    fun identity(size: Int, scale: Double): List<Double> = List(size * size) {
        if (it / size ==
            it % size
        ) {
            scale
        } else {
            0.0
        }
    }

    /** The inverse of the symmetric positive definite [matrix] of [size], by Gauss-Jordan elimination. */
    fun inverse(matrix: List<Double>, size: Int): List<Double> {
        val left = Array(size) { row -> DoubleArray(size) { matrix[row * size + it] } }
        val right = Array(size) { row -> DoubleArray(size) { if (it == row) 1.0 else 0.0 } }
        for (pivot in 0 until size) {
            // The largest value of the column first, for precision.
            val best = (pivot until size).maxBy { kotlin.math.abs(left[it][pivot]) }
            left[pivot] = left[best].also { left[best] = left[pivot] }
            right[pivot] = right[best].also { right[best] = right[pivot] }
            val value = left[pivot][pivot]
            for (column in 0 until size) {
                left[pivot][column] /= value
                right[pivot][column] /= value
            }
            for (row in 0 until size) {
                if (row == pivot) continue
                val factor = left[row][pivot]
                for (column in 0 until size) {
                    left[row][column] -= factor * left[pivot][column]
                    right[row][column] -= factor * right[pivot][column]
                }
            }
        }
        return right.flatMap { it.toList() }
    }

    /** The lower triangle L of the symmetric positive definite [matrix] of [size], such that L Lᵀ is it. */
    fun cholesky(matrix: List<Double>, size: Int): List<Double> {
        val lower = DoubleArray(size * size)
        for (row in 0 until size) {
            for (column in 0..row) {
                val sum = (0 until column).sumOf { lower[row * size + it] * lower[column * size + it] }
                lower[row * size + column] = if (row == column) {
                    sqrt((matrix[row * size + row] - sum).coerceAtLeast(MIN_DIAGONAL))
                } else {
                    (matrix[row * size + column] - sum) / lower[column * size + column]
                }
            }
        }
        return lower.toList()
    }

    /** [matrix] of [size] times [vector]. */
    fun times(matrix: List<Double>, vector: List<Double>, size: Int): List<Double> =
        List(size) { row -> (0 until size).sumOf { matrix[row * size + it] * vector[it] } }

    /** The dot product of [left] and [right]. */
    fun dot(left: List<Double>, right: List<Double>): Double = left.indices.sumOf { left[it] * right[it] }

    // Keeps the root real when rounding makes a diagonal slightly negative.
    private const val MIN_DIAGONAL = 1e-12
}
