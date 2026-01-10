package com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data

data class CustomReminder(
    val id: String = "",
    val userId: String = "",
    val plantId: String? = null,
    val plantName: String? = null,
    val title: String = "",
    val message: String = "",
    val nextFireAt: Long = 0L,          //UTC
    val repeatIntervalDays: Int? = null,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastFiredAt: Long? = null
)
