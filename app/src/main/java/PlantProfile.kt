package com.example.plantpal

data class PlantProfile(
    val plantId: String = "",
    val userId: String = "",
    val commonName: String = "",
    val scientificName: String = "",
    val confidence: Double = 0.0,
    val notes: String = "",
    val careInfo: PlantCareInfo = PlantCareInfo(),
    val avatarConfig: AvatarConfig = AvatarConfig(),
    val baseType: String = "",
    val color: String = "",
    val careProfile: CareProfile = CareProfile(),
    //not used yet, but i plan to use these to allow the user to determine water and fertilization frequencies independently of the defined times.
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val wateringFrequency: Int = 7,
    val sunlight: String = "",
    val fertilizerFrequency: Int = 30,
    val currentStatus: CurrentStatus = CurrentStatus(),
    val health: String = "healthy",
    val lastWatered: Long = 0L,
    val lastFertilized: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val photoUrl: String = ""
)

data class PlantCareInfo(
    val commonNames: List<String> = emptyList(),
    val wikiDescription: String = "",
    val wikiUrl: String = "",
    val wateringMinDays: Int? = null,
    val wateringMaxDays: Int? = null,
    val propagationMethods: List<String> = emptyList(),
    val family: String = "",
    val genus: String = "",
    val cycle: String = "",
    val watering: String = "",
    val sunlight: List<String> = emptyList(),
    val indoor: Boolean = false,
    val careLevel: String = "",
    val growthRate: String = "",
    val maintenance: String = "",
    val droughtTolerant: Boolean = false,
    val poisonousToHumans: Boolean = false,
    val poisonousToPets: Boolean = false,
    val flowers: Boolean = false,
    val flowerColor: String = "",
    val harvestSeason: String = "",
    val attracts: List<String> = emptyList()
)

data class AvatarConfig(
    val baseType: String = "",
    val color: String = "",
    val potColor: String = "terracotta",
    val potStyle: String = "classic"
)

data class CareProfile(
    val wateringFrequency: Int = 7,
    val sunlight: String = "",
    val fertilizerFrequency: Int = 30,
    val lastRotated: Long = 0L,
    val rotationFrequency: Int = 14,
    var reminderHour: Int? = null,
    var reminderMinute: Int? = null
)

data class CurrentStatus(
    val health: String = "healthy",
    val lastWatered: Long = 0L,
    val lastFertilized: Long = 0L,
    val lastRotated: Long = 0L
)

fun Suggestion.toCareInfo(): PlantCareInfo {
    val details = this.plant_details
    return PlantCareInfo(
        commonNames = details?.common_names ?: emptyList(),
        wikiDescription = details?.wiki_description?.value ?: "",
        wikiUrl = details?.url ?: "",
        wateringMinDays = details?.watering?.min,
        wateringMaxDays = details?.watering?.max,
        propagationMethods = details?.propagation_methods ?: emptyList(),
        family = details?.taxonomy?.family ?: "",
        genus = details?.taxonomy?.genus ?: ""
    )
}