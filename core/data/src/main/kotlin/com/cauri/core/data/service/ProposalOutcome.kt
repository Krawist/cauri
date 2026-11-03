package com.cauri.core.data.service

/**
 * A confirmed transaction that came with a proposal of Cauri, next to what the user kept.
 *
 * @property placeId Place of the transaction; `null` when unknown.
 * @property counterparty Merchant or person the message names; `null` when unknown.
 * @property templateVersion Template that read its message, as `<template id>@<version>`; `null` when not read in one.
 * @property proposedCategoryId Category proposed; `null` when none was.
 * @property categoryId Category confirmed; `null` when none.
 * @property proposedAmount Amount proposed, in minor units; `null` when none was.
 * @property amount Amount confirmed, in minor units.
 */
internal data class ProposalOutcome(
    val placeId: Long?,
    val counterparty: String?,
    val templateVersion: String?,
    val proposedCategoryId: Long?,
    val categoryId: Long?,
    val proposedAmount: Long?,
    val amount: Long,
)
