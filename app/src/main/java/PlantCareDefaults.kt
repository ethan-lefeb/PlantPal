package com.example.plantpal


object PlantCareDefaults {

    private val wateringSchedules = mapOf(
        // Succulents and Cacti
        "cactaceae" to 14,
        "crassulaceae" to 10,
        "aizoaceae" to 14,
        "agavaceae" to 12,

        // Ferns
        "polypodiaceae" to 3,
        "pteridaceae" to 3,
        "aspleniaceae" to 3,
        "dryopteridaceae" to 3,
        
        // Tropical plants
        "araceae" to 5,
        "marantaceae" to 4,
        "bromeliaceae" to 7,
        "orchidaceae" to 7,
        "zingiberaceae" to 4,
        
        // Common houseplants
        "moraceae" to 7,
        "araliaceae" to 7,
        "liliaceae" to 7,
        "asparagaceae" to 7,
        "piperaceae" to 6,
        
        // Herbs
        "lamiaceae" to 3,
        "apiaceae" to 4,
        
        // Flowering plants
        "gesneriaceae" to 5,
        "begoniaceae" to 5,
        "primulaceae" to 4,
        
        // Trees and large plants
        "fagaceae" to 7,
        "pinaceae" to 10,
        "palmae" to 5,
        
        // Vegetables
        "solanaceae" to 3,
        "cucurbitaceae" to 2,
        
        // Default
        "default" to 7
    )

    private val sunlightRequirements = mapOf(
        // Full sun
        "cactaceae" to "Full sun (6+ hours)",
        "lamiaceae" to "Full sun (6+ hours)",
        "solanaceae" to "Full sun (6+ hours)",
        "rosaceae" to "Full sun (6+ hours)",
        
        // Partial shade
        "araceae" to "Partial shade (3-6 hours)",
        "marantaceae" to "Partial shade (3-6 hours)",
        "begoniaceae" to "Partial shade (3-6 hours)",
        "gesneriaceae" to "Partial shade (3-6 hours)",
        
        // Bright indirect light
        "moraceae" to "Bright indirect light",
        "piperaceae" to "Bright indirect light",
        "orchidaceae" to "Bright indirect light",
        "bromeliaceae" to "Bright indirect light",
        
        // Low to medium light
        "polypodiaceae" to "Low to medium light",
        "aspleniaceae" to "Low to medium light",
        "dryopteridaceae" to "Low to medium light",
        "asparagaceae" to "Low to medium light",
        
        // Default
        "default" to "Bright indirect light"
    )

    private val detailedCareTips = mapOf(
        // Succulents & Cacti
        "cactaceae" to listOf(
            "Allow soil to dry completely between waterings",
            "Use well-draining cactus soil mix",
            "Provide maximum sunlight (6+ hours daily)",
            "Fertilize monthly during spring and summer only",
            "Reduce watering in winter months",
            "Avoid overwatering - root rot is the #1 killer"
        ),
        
        "crassulaceae" to listOf(
            "Water deeply but infrequently",
            "Let soil dry between waterings",
            "Prefers bright light with some direct sun",
            "Propagates easily from leaf cuttings",
            "Minimal fertilizer needed (monthly in growing season)"
        ),
        
        // Ferns
        "polypodiaceae" to listOf(
            "Keep soil consistently moist but not waterlogged",
            "Mist leaves regularly to maintain humidity",
            "Prefers indirect light - avoid direct sun",
            "Use peat-based potting mix",
            "Fertilize monthly during growing season",
            "Brown leaf tips indicate low humidity"
        ),
        
        "pteridaceae" to listOf(
            "Water when top of soil feels slightly dry",
            "Requires high humidity (50-70%)",
            "Keep away from heating vents",
            "Fertilize every 2-3 weeks in growing season",
            "Trim brown fronds to encourage new growth"
        ),
        
        // Aroids
        "araceae" to listOf(
            "Water when top 1-2 inches of soil are dry",
            "Wipe leaves weekly to remove dust",
            "Provide bright, indirect light",
            "Fertilize monthly during spring and summer",
            "Many varieties enjoy climbing - provide support",
            "Yellow leaves often indicate overwatering"
        ),
        
        // Prayer plants
        "marantaceae" to listOf(
            "Keep soil evenly moist but not soggy",
            "High humidity is essential (60%+)",
            "Avoid direct sunlight - prefers shade",
            "Use distilled or filtered water if possible",
            "Leaves fold up at night (hence 'prayer plant')",
            "Fertilize every 2 weeks during growing season"
        ),
        
        // Ficus
        "moraceae" to listOf(
            "Water when top 2 inches of soil are dry",
            "Dislikes being moved - choose location carefully",
            "Prefers consistent moisture and light",
            "Fertilize monthly during spring/summer",
            "May drop leaves when stressed",
            "Prune in spring to maintain shape"
        ),
        
        // Orchids
        "orchidaceae" to listOf(
            "Water when potting medium is nearly dry",
            "Use orchid-specific potting mix (bark-based)",
            "Prefers 50-70% humidity",
            "Bright indirect light is ideal",
            "Fertilize weekly with diluted orchid fertilizer",
            "Rebloom by providing cooler night temperatures"
        ),
        
        // Snake plants & Spider plants
        "asparagaceae" to listOf(
            "Very drought tolerant - water every 2-3 weeks",
            "Tolerates low light but grows faster in bright light",
            "Avoid overwatering - can handle neglect",
            "Fertilize monthly during growing season",
            "One of the easiest houseplants to care for",
            "Propagates easily from leaf cuttings or division"
        ),
        
        // Herbs
        "lamiaceae" to listOf(
            "Water frequently - herbs prefer moist soil",
            "Needs 6+ hours of sunlight daily",
            "Pinch off flower buds to encourage leaf growth",
            "Harvest regularly to promote bushier growth",
            "Fertilize every 2-3 weeks during growing season",
            "Most herbs are annuals and need replanting yearly"
        ),
        
        // Peperomia
        "piperaceae" to listOf(
            "Allow top inch of soil to dry between waterings",
            "Prefers bright indirect light",
            "Compact growth habit - perfect for small spaces",
            "Fertilize monthly during growing season",
            "Sensitive to overwatering - err on dry side",
            "Over 1000 varieties with different leaf patterns"
        ),
        
        // African Violets
        "gesneriaceae" to listOf(
            "Water from below to avoid leaf spotting",
            "Use room temperature water",
            "Prefers bright indirect light",
            "Blooms year-round with proper care",
            "Fertilize with African violet fertilizer monthly",
            "Deadhead spent flowers to encourage more blooms"
        ),
        
        // Palms
        "palmae" to listOf(
            "Keep soil consistently moist in growing season",
            "Provide bright indirect light",
            "High humidity is beneficial",
            "Fertilize monthly during growing season",
            "Brown leaf tips indicate low humidity or fluoride in water",
            "Remove only completely brown fronds"
        ),
        
        // Tomatoes & Peppers
        "solanaceae" to listOf(
            "Water deeply and consistently",
            "Requires 6-8 hours of direct sunlight",
            "Fertilize every 2 weeks with balanced fertilizer",
            "Stake or cage tomatoes for support",
            "Pinch off suckers on tomatoes for larger fruit",
            "Watch for common pests like aphids and hornworms"
        ),
        
        // Default tips
        "default" to listOf(
            "Water when top inch of soil feels dry",
            "Provide bright indirect light",
            "Fertilize monthly during spring and summer",
            "Check for pests regularly",
            "Adjust care based on your specific environment",
            "Observe your plant - it will tell you what it needs"
        )
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
            "lamiaceae" -> 14
            "solanaceae" -> 14
            else -> 30
        }
    }

    fun getCareTips(family: String?): String {
        val tips = getDetailedCareTips(family)
        return tips.firstOrNull() ?: "Water when top inch of soil is dry. Adjust based on your environment."
    }

    fun getDetailedCareTips(family: String?): List<String> {
        return detailedCareTips[family?.lowercase()] 
            ?: detailedCareTips["default"]!!
    }

    fun getRandomCareTip(family: String?): String {
        val tips = getDetailedCareTips(family)
        return tips.random()
    }

    fun getCycle(family: String?): String {
        return when (family?.lowercase()) {
            "lamiaceae", "solanaceae", "cucurbitaceae" -> "annual"
            "cactaceae", "crassulaceae" -> "perennial"
            "orchidaceae", "bromeliaceae" -> "perennial"
            "polypodiaceae", "pteridaceae" -> "perennial"
            else -> "perennial"
        }
    }

    fun getWateringFrequency(family: String?): String {
        return when (family?.lowercase()) {
            "cactaceae", "crassulaceae", "agavaceae" -> "minimum"
            "polypodiaceae", "pteridaceae", "dryopteridaceae" -> "frequent"
            "araceae", "marantaceae", "moraceae" -> "average"
            "lamiaceae", "solanaceae" -> "frequent"
            else -> "average"
        }
    }

    fun isIndoorPlant(family: String?): Boolean {
        val indoorFamilies = setOf(
            "araceae", "marantaceae", "bromeliaceae", "orchidaceae",
            "crassulaceae", "cactaceae", "moraceae", "araliaceae",
            "piperaceae", "gesneriaceae", "asparagaceae", "palmae",
            "polypodiaceae", "pteridaceae", "dryopteridaceae"
        )
        return indoorFamilies.contains(family?.lowercase())
    }

    fun getCareLevel(family: String?): String {
        return when (family?.lowercase()) {
            "cactaceae", "crassulaceae", "asparagaceae" -> "easy"
            "orchidaceae", "bromeliaceae", "marantaceae" -> "moderate"
            "polypodiaceae", "pteridaceae" -> "moderate"
            "gesneriaceae" -> "moderate"
            "solanaceae" -> "moderate"
            else -> "easy"
        }
    }

    fun isDroughtTolerant(family: String?): Boolean {
        val droughtTolerantFamilies = setOf(
            "cactaceae", "crassulaceae", "agavaceae", "asparagaceae"
        )
        return droughtTolerantFamilies.contains(family?.lowercase())
    }

    fun getMaintenanceLevel(family: String?): String {
        return when (getCareLevel(family)) {
            "easy" -> "Low maintenance"
            "moderate" -> "Moderate maintenance"
            "difficult" -> "High maintenance"
            else -> "Moderate maintenance"
        }
    }

    fun isPetSafe(family: String?): Boolean {
        val petUnsafeFamilies = setOf(
            "araceae",
            "liliaceae",
            "solanaceae",
            "euphorbiaceae"
        )
        return !petUnsafeFamilies.contains(family?.lowercase())
    }
}
