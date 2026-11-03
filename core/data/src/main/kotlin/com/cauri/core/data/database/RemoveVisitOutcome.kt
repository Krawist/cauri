package com.cauri.core.data.database

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/** Version 17: a visit to a shop asks with a transaction to confirm, which holds the answer, not with an outcome. */
@DeleteColumn(tableName = "visits", columnName = "outcome")
internal class RemoveVisitOutcome : AutoMigrationSpec
