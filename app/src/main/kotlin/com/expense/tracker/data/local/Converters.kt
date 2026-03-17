package com.expense.tracker.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDate(millis: Long?): LocalDate? {
        return millis?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }
}
