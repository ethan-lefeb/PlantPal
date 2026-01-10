package com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers

import java.util.Calendar


fun buildNextFireAt(
    year: Int,
    month: Int,     // 1–12
    day: Int,
    hour: Int,
    minute: Int
): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)  //calendar is 0-based
    cal.set(Calendar.DAY_OF_MONTH, day)
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
