package com.example.plantpal


fun buildNextFireAt(
    year: Int,
    month: Int,     // 1–12
    day: Int,
    hour: Int,
    minute: Int
): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.YEAR, year)
    cal.set(java.util.Calendar.MONTH, month - 1)  //calendar is 0-based
    cal.set(java.util.Calendar.DAY_OF_MONTH, day)
    cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
    cal.set(java.util.Calendar.MINUTE, minute)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
