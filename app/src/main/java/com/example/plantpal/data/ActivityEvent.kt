package com.example.plantpal.data

data class ActivityEvent(
    val activityId: String = "",
    val actorUid: String = "",
    val actorName: String = "",
    val type: String = "",
    val text: String = "",
    val plantId: String? = null,
    val plantName: String? = null,
    val createdAt: Long = 0L
)
