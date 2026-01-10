package com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data

object PlantCareDefaults {

    private val wateringSchedules = mapOf(
        //Succulents and Cacti
        "cactaceae" to 14,
        "crassulaceae" to 10,
        "aizoaceae" to 14,

        //Ferns
        "polypodiaceae" to 3,
        "pteridaceae" to 3,
        "aspleniaceae" to 3,

        //Tropical plants
        "araceae" to 5,
        "marantaceae" to 4,
        "bromeliaceae" to 7,
        "orchidaceae" to 7,

        //Common houseplants
        "moraceae" to 7,
        "araliaceae" to 7,
        "liliaceae" to 7,
        "asparagaceae" to 7,

        //Herbs
        "lamiaceae" to 3,

        //Trees
        "fagaceae" to 7,
        "pinaceae" to 10,

        //Default
        "default" to 7
    )

    private val sunlightRequirements = mapOf(
        //Full sun
        "cactaceae" to "Full sun (6+ hours)",
        "lamiaceae" to "Full sun (6+ hours)",

        //Partial
        "araceae" to "Partial shade (3-6 hours)",
        "marantaceae" to "Partial shade (3-6 hours)",
        "moraceae" to "Bright indirect light",

        //Low light
        "polypodiaceae" to "Low to medium light",
        "aspleniaceae" to "Low to medium light",

        //Default
        "default" to "Bright indirect light"
    )

    fun getWateringFrequencyDays(family: String?, genus: String?): Int {
        val searchFamily = family?.lowercase() ?: ""
        val searchGenus = genus?.lowercase() ?: ""

        return wateringSchedules[searchFamily]
            ?: wateringSchedules[searchGenus]
            ?: wateringSchedules["default"]!!
    }

    fun getSunlightRequirement(family: String?, genus: String?): String {
        val searchFamily = family?.lowercase() ?: ""
        val searchGenus = genus?.lowercase() ?: ""

        return sunlightRequirements[searchFamily]
            ?: sunlightRequirements[searchGenus]
            ?: sunlightRequirements["default"]!!
    }

    fun getFertilizerFrequency(family: String?): Int {
        return when (family?.lowercase()) {
            "cactaceae", "crassulaceae" -> 60
            "orchidaceae" -> 14
            else -> 30
        }
    }

    fun getCareTips(family: String?): String {
        return when (family?.lowercase()) {
            "cactaceae", "crassulaceae" -> "Allow soil to dry completely between waterings. Prefers well-draining soil."
            "polypodiaceae", "pteridaceae" -> "Keep soil consistently moist but not waterlogged. Prefers high humidity."
            "araceae" -> "Water when top inch of soil is dry. Wipe leaves occasionally to remove dust."
            "moraceae" -> "Water when top 2 inches of soil are dry. Prefers consistent moisture."
            "orchidaceae" -> "Water when potting medium is nearly dry. Prefers bright indirect light and high humidity."
            else -> "Water when top inch of soil is dry. Adjust based on your environment."
        }
    }

    fun getCycle(family: String?): String {
        return when (family?.lowercase()) {
            "lamiaceae", "solanaceae" -> "annual"
            "cactaceae", "crassulaceae" -> "perennial"
            "orchidaceae", "bromeliaceae" -> "perennial"
            else -> "perennial"
        }
    }

    fun getWateringFrequency(family: String?): String {
        return when (family?.lowercase()) {
            "cactaceae", "crassulaceae" -> "minimum"
            "polypodiaceae", "pteridaceae" -> "frequent"
            "araceae", "marantaceae" -> "average"
            else -> "average"
        }
    }

    fun isIndoorPlant(family: String?): Boolean {
        val indoorFamilies = setOf(
            "araceae", "marantaceae", "bromeliaceae", "orchidaceae",
            "crassulaceae", "cactaceae", "moraceae", "araliaceae"
        )
        return indoorFamilies.contains(family?.lowercase())
    }

    fun getCareLevel(family: String?): String {
        return when (family?.lowercase()) {
            "cactaceae", "crassulaceae" -> "easy"
            "orchidaceae", "bromeliaceae" -> "moderate"
            "polypodiaceae", "pteridaceae" -> "moderate"
            else -> "easy"
        }
    }

    fun isDroughtTolerant(family: String?): Boolean {
        val droughtTolerantFamilies = setOf("cactaceae", "crassulaceae", "agavaceae")
        return droughtTolerantFamilies.contains(family?.lowercase())
    }
}