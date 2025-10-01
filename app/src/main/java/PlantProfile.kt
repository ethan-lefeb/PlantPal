package com.example.plantpal

data class PlantProfile(
    val plantId: String = "",
    val userId: String = "",
    val commonName: String = "",
    val scientificName: String = "",
    val confidence: Double = 0.0,
    val notes: String = "",
    val avatarConfig: AvatarConfig = AvatarConfig(),
    val baseType: String = "",
    val color: String = "",
    val accessories: List<String> = emptyList(),
    val careProfile: CareProfile = CareProfile(),
    val wateringFrequency: Int = 7, // days
    val sunlight: String = "",
    val fertilizerFrequency: Int = 30, // days
    val currentStatus: CurrentStatus = CurrentStatus(),
    val health: String = "healthy",
    val lastWatered: Long = 0L,
    val lastFertilized: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

data class AvatarConfig(
    val baseType: String = "",
    val color: String = "",
    val accessories: List<String> = emptyList()
)

data class CareProfile(
    val wateringFrequency: Int = 7,
    val sunlight: String = "",
    val fertilizerFrequency: Int = 30
)

data class CurrentStatus(
    val health: String = "healthy",
    val lastWatered: Long = 0L,
    val lastFertilized: Long = 0L
)