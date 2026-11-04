package com.cauri.core.data.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

/** Column types Room does not store natively. */
internal class DatabaseConverters {
    @TypeConverter
    fun instantToMillis(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun millisToInstant(millis: Long?): Instant? = millis?.let(Instant::ofEpochMilli)

    // Days, as the number of days since 1970-01-01.
    @TypeConverter
    fun dateToDays(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun daysToDate(days: Long?): LocalDate? = days?.let(LocalDate::ofEpochDay)

    // Feature vectors, as comma-separated numbers: readable when debugging the decision log.
    @TypeConverter
    fun doublesToText(values: List<Double>): String = values.joinToString(separator = ",")

    @TypeConverter
    fun textToDoubles(text: String): List<Double> =
        if (text.isEmpty()) emptyList() else text.split(",").map(String::toDouble)
}
