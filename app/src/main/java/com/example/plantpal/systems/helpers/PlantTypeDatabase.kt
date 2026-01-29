package com.example.plantpal

import android.util.Log


object PlantTypeDatabase {

    data class PlantTypeEntry(
        val families: Set<String>,
        val genera: Set<String>,
        val keywords: Set<String>,
        val scientificNamePatterns: Set<String>,
        val avatarType: String,
        val defaultColor: String,
        val alternateColors: List<String>,
        val confidence: Float = 1.0f,
        val description: String = ""
    )

    data class MatchResult(
        val entry: PlantTypeEntry,
        val matchedBy: String,
        val confidence: Float
    )

    private val plantTypes = listOf(

        PlantTypeEntry(
            families = setOf("cactaceae", "cacti"),
            genera = setOf(
                "mammillaria", "opuntia", "echinocactus", "ferocactus",
                "gymnocalycium", "cereus", "astrophytum", "melocactus",
                "rebutia", "parodia", "echinopsis"
            ),
            keywords = setOf(
                "cactus", "prickly pear", "barrel", "pincushion",
                "saguaro", "hedgehog", "ball cactus", "old man cactus"
            ),
            scientificNamePatterns = setOf("cactus", "opuntia", "cereus"),
            avatarType = "cactus_round",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "blue", "yellow"),
            confidence = 1.0f,
            description = "Round/barrel cactus shape"
        ),

        PlantTypeEntry(
            families = setOf("cactaceae"),
            genera = setOf("schlumbergera", "rhipsalidopsis", "hatiora"),
            keywords = setOf("christmas cactus", "easter cactus", "thanksgiving cactus"),
            scientificNamePatterns = setOf("schlumbergera"),
            avatarType = "cactus_trailing",
            defaultColor = "green",
            alternateColors = listOf("light_green", "pink", "red"),
            confidence = 1.0f,
            description = "Trailing holiday cactus"
        ),

        PlantTypeEntry(
            families = setOf("crassulaceae"),
            genera = setOf("echeveria", "graptopetalum", "graptoveria", "pachyveria"),
            keywords = setOf("echeveria", "rosette", "hens and chicks"),
            scientificNamePatterns = setOf("echeveria"),
            avatarType = "succulent_rosette",
            defaultColor = "blue",
            alternateColors = listOf("green", "purple", "pink", "light_green"),
            confidence = 1.0f,
            description = "Rosette-forming succulent"
        ),

        PlantTypeEntry(
            families = setOf("crassulaceae"),
            genera = setOf("crassula"),
            keywords = setOf("jade plant", "jade", "money plant", "money tree"),
            scientificNamePatterns = setOf("crassula", "ovata"),
            avatarType = "succulent_jade",
            defaultColor = "green",
            alternateColors = listOf("dark_green"),
            confidence = 1.0f,
            description = "Jade plant with thick leaves"
        ),

        PlantTypeEntry(
            families = setOf("crassulaceae", "apocynaceae"),
            genera = setOf("senecio", "curio"),
            keywords = setOf("string of pearls", "string of bananas", "string of dolphins"),
            scientificNamePatterns = setOf("senecio", "curio"),
            avatarType = "succulent_string",
            defaultColor = "light_green",
            alternateColors = listOf("green", "blue"),
            confidence = 1.0f,
            description = "Trailing string succulent"
        ),

        PlantTypeEntry(
            families = setOf("xanthorrhoeaceae", "asphodelaceae"),
            genera = setOf("aloe", "gasteria", "haworthia", "haworthiopsis"),
            keywords = setOf("aloe", "aloe vera", "zebra plant", "haworthia"),
            scientificNamePatterns = setOf("aloe", "haworthia"),
            avatarType = "succulent_aloe",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "blue"),
            confidence = 1.0f,
            description = "Spiky aloe-type succulent"
        ),

        PlantTypeEntry(
            families = setOf("asparagaceae", "dracaenaceae"),
            genera = setOf("sansevieria", "dracaena"),
            keywords = setOf("snake plant", "mother-in-law's tongue", "sansevieria"),
            scientificNamePatterns = setOf("sansevieria", "trifasciata"),
            avatarType = "snake_plant",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "yellow"),
            confidence = 1.0f,
            description = "Upright snake plant"
        ),

        PlantTypeEntry(
            families = setOf("araceae"),
            genera = setOf("epipremnum"),
            keywords = setOf("pothos", "devil's ivy", "golden pothos"),
            scientificNamePatterns = setOf("epipremnum", "aureum"),
            avatarType = "pothos",
            defaultColor = "green",
            alternateColors = listOf("light_green", "yellow"),
            confidence = 1.0f,
            description = "Heart-shaped trailing leaves"
        ),

        PlantTypeEntry(
            families = setOf("araceae"),
            genera = setOf("philodendron"),
            keywords = setOf("philodendron", "heartleaf", "brasil"),
            scientificNamePatterns = setOf("philodendron", "hederaceum", "scandens"),
            avatarType = "philodendron_heart",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "light_green"),
            confidence = 1.0f,
            description = "Heart-leaf philodendron"
        ),

        PlantTypeEntry(
            families = setOf("araceae"),
            genera = setOf("monstera"),
            keywords = setOf("monstera", "swiss cheese", "split leaf"),
            scientificNamePatterns = setOf("monstera", "deliciosa"),
            avatarType = "monstera",
            defaultColor = "dark_green",
            alternateColors = listOf("green"),
            confidence = 1.0f,
            description = "Large split leaves"
        ),

        PlantTypeEntry(
            families = setOf("polypodiaceae", "pteridaceae"),
            genera = setOf("nephrolepis"),
            keywords = setOf("boston fern", "sword fern"),
            scientificNamePatterns = setOf("nephrolepis"),
            avatarType = "fern_boston",
            defaultColor = "light_green",
            alternateColors = listOf("green"),
            confidence = 1.0f,
            description = "Fluffy Boston fern"
        ),

        PlantTypeEntry(
            families = setOf("pteridaceae"),
            genera = setOf("adiantum"),
            keywords = setOf("maidenhair fern", "maidenhair"),
            scientificNamePatterns = setOf("adiantum"),
            avatarType = "fern_maidenhair",
            defaultColor = "light_green",
            alternateColors = listOf("green"),
            confidence = 1.0f,
            description = "Delicate maidenhair fern"
        ),

        PlantTypeEntry(
            families = setOf("aspleniaceae"),
            genera = setOf("asplenium"),
            keywords = setOf("bird's nest fern", "bird nest"),
            scientificNamePatterns = setOf("asplenium", "nidus"),
            avatarType = "fern_birds_nest",
            defaultColor = "green",
            alternateColors = listOf("light_green"),
            confidence = 1.0f,
            description = "Rosette bird's nest fern"
        ),

        PlantTypeEntry(
            families = setOf("marantaceae"),
            genera = setOf("maranta", "calathea", "stromanthe", "ctenanthe"),
            keywords = setOf("prayer plant", "calathea", "rattlesnake plant", "peacock plant"),
            scientificNamePatterns = setOf("maranta", "calathea"),
            avatarType = "prayer_plant",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "purple"),
            confidence = 1.0f,
            description = "Patterned prayer plant"
        ),

        PlantTypeEntry(
            families = setOf("asparagaceae", "liliaceae"),
            genera = setOf("chlorophytum"),
            keywords = setOf("spider plant", "airplane plant"),
            scientificNamePatterns = setOf("chlorophytum", "comosum"),
            avatarType = "spider_plant",
            defaultColor = "light_green",
            alternateColors = listOf("green"),
            confidence = 1.0f,
            description = "Arching spider plant"
        ),

        PlantTypeEntry(
            families = setOf("araceae"),
            genera = setOf("spathiphyllum"),
            keywords = setOf("peace lily", "spathe"),
            scientificNamePatterns = setOf("spathiphyllum"),
            avatarType = "peace_lily",
            defaultColor = "dark_green",
            alternateColors = listOf("green"),
            confidence = 1.0f,
            description = "Peace lily with white flower"
        ),

        PlantTypeEntry(
            families = setOf("araceae"),
            genera = setOf("zamioculcas"),
            keywords = setOf("zz plant", "zanzibar gem"),
            scientificNamePatterns = setOf("zamioculcas", "zamiifolia"),
            avatarType = "zz_plant",
            defaultColor = "dark_green",
            alternateColors = listOf("green"),
            confidence = 1.0f,
            description = "Glossy ZZ plant"
        ),

        PlantTypeEntry(
            families = setOf("moraceae"),
            genera = setOf("ficus"),
            keywords = setOf("rubber plant", "rubber tree", "rubber fig"),
            scientificNamePatterns = setOf("ficus", "elastica"),
            avatarType = "rubber_plant",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "brown"),
            confidence = 1.0f,
            description = "Large-leafed rubber plant"
        ),

        PlantTypeEntry(
            families = setOf("moraceae"),
            genera = setOf("ficus"),
            keywords = setOf("fiddle leaf fig", "fiddle leaf"),
            scientificNamePatterns = setOf("ficus", "lyrata"),
            avatarType = "fiddle_leaf",
            defaultColor = "green",
            alternateColors = listOf("dark_green"),
            confidence = 1.0f,
            description = "Fiddle-shaped leaves"
        ),

        PlantTypeEntry(
            families = setOf("asparagaceae", "dracaenaceae"),
            genera = setOf("dracaena"),
            keywords = setOf("dracaena", "dragon tree", "corn plant"),
            scientificNamePatterns = setOf("dracaena", "marginata", "fragrans"),
            avatarType = "dracaena",
            defaultColor = "green",
            alternateColors = listOf("dark_green"),
            confidence = 1.0f,
            description = "Spiky dracaena"
        ),

        PlantTypeEntry(
            families = setOf("arecaceae", "palmae"),
            genera = setOf("chamaedorea", "dypsis", "howea"),
            keywords = setOf("palm", "parlor palm", "areca palm", "majesty palm"),
            scientificNamePatterns = setOf("chamaedorea", "dypsis", "palm"),
            avatarType = "palm",
            defaultColor = "green",
            alternateColors = listOf("light_green", "dark_green"),
            confidence = 1.0f,
            description = "Small indoor palm"
        ),

        PlantTypeEntry(
            families = setOf("orchidaceae"),
            genera = setOf("phalaenopsis", "dendrobium", "oncidium"),
            keywords = setOf("orchid", "moth orchid", "phalaenopsis"),
            scientificNamePatterns = setOf("orchid", "phalaenopsis"),
            avatarType = "orchid",
            defaultColor = "pink",
            alternateColors = listOf("purple", "white", "yellow"),
            confidence = 0.9f,
            description = "Elegant orchid flower"
        ),

        PlantTypeEntry(
            families = setOf("gesneriaceae"),
            genera = setOf("saintpaulia", "streptocarpus"),
            keywords = setOf("african violet", "saintpaulia"),
            scientificNamePatterns = setOf("saintpaulia"),
            avatarType = "african_violet",
            defaultColor = "purple",
            alternateColors = listOf("pink", "blue"),
            confidence = 1.0f,
            description = "Fuzzy-leafed violet"
        ),

        PlantTypeEntry(
            families = setOf("piperaceae"),
            genera = setOf("peperomia"),
            keywords = setOf("peperomia", "radiator plant", "baby rubber plant"),
            scientificNamePatterns = setOf("peperomia"),
            avatarType = "peperomia",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "light_green"),
            confidence = 1.0f,
            description = "Round-leafed peperomia"
        ),

        PlantTypeEntry(
            families = setOf("urticaceae"),
            genera = setOf("pilea"),
            keywords = setOf("pilea", "chinese money plant", "pancake plant"),
            scientificNamePatterns = setOf("pilea", "peperomioides"),
            avatarType = "pilea",
            defaultColor = "light_green",
            alternateColors = listOf("green"),
            confidence = 1.0f,
            description = "Round coin-shaped leaves"
        ),

        PlantTypeEntry(
            families = setOf("lamiaceae"),
            genera = setOf("ocimum", "mentha", "rosmarinus", "thymus", "salvia", "origanum", "lavandula"),
            keywords = setOf("basil", "mint", "rosemary", "thyme", "sage", "oregano", "lavender"),
            scientificNamePatterns = setOf("ocimum", "mentha", "thymus"),
            avatarType = "herb",
            defaultColor = "green",
            alternateColors = listOf("light_green"),
            confidence = 1.0f,
            description = "Culinary herb"
        ),

        PlantTypeEntry(
            families = emptySet(),
            genera = emptySet(),
            keywords = emptySet(),
            scientificNamePatterns = emptySet(),
            avatarType = "generic",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "light_green"),
            confidence = 0.3f,
            description = "Generic houseplant"
        )
    )

    fun findMatch(
        family: String?,
        genus: String?,
        commonName: String,
        scientificName: String = ""
    ): MatchResult {
        val familyLower = family?.lowercase()?.trim() ?: ""
        val genusLower = genus?.lowercase()?.trim() ?: ""
        val commonLower = commonName.lowercase().trim()
        val scientificLower = scientificName.lowercase().trim()

        Log.d("PlantTypeDB", "Finding match for:")
        Log.d("PlantTypeDB", "  Family: '$familyLower'")
        Log.d("PlantTypeDB", "  Genus: '$genusLower'")
        Log.d("PlantTypeDB", "  Common: '$commonLower'")

        if (genusLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.genera.any { it == genusLower }) {
                    Log.d("PlantTypeDB", "✅ Exact genus match: ${entry.avatarType}")
                    return MatchResult(entry, "genus", 1.0f)
                }
            }
        }

        if (familyLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.families.any { it == familyLower }) {
                    Log.d("PlantTypeDB", "✅ Exact family match: ${entry.avatarType}")
                    return MatchResult(entry, "family", 0.95f)
                }
            }
        }

        if (scientificLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.scientificNamePatterns.any { scientificLower.contains(it) }) {
                    Log.d("PlantTypeDB", "âœ… Scientific name match: ${entry.avatarType}")
                    return MatchResult(entry, "scientific_name", 0.90f)
                }
            }
        }

        val keywordMatches = mutableListOf<Triple<PlantTypeEntry, Int, Float>>()

        plantTypes.forEach { entry ->
            var matchCount = 0
            var longestMatch = 0

            entry.keywords.forEach { keyword ->
                if (commonLower.contains(keyword)) {
                    matchCount++
                    if (keyword.length > longestMatch) {
                        longestMatch = keyword.length
                    }
                }
            }

            if (matchCount > 0) {
                val confidence = 0.7f * (matchCount.toFloat() / 3f + longestMatch.toFloat() / 20f).coerceAtMost(1f)
                keywordMatches.add(Triple(entry, matchCount, confidence))
            }
        }

        if (keywordMatches.isNotEmpty()) {
            val best = keywordMatches.maxByOrNull { it.third }!!
            Log.d("PlantTypeDB", "âœ… Keyword match (${best.second} keywords): ${best.first.avatarType}")
            return MatchResult(best.first, "keyword", best.third)
        }

        if (familyLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.families.any {
                        familyLower.contains(it) || it.contains(familyLower)
                    }) {
                    Log.d("PlantTypeDB", "âš ï¸ Partial family match: ${entry.avatarType}")
                    return MatchResult(entry, "partial_family", 0.6f)
                }
            }
        }

        Log.d("PlantTypeDB", "â„¹ï¸ No match found, using generic")
        return MatchResult(
            entry = plantTypes.last(), // The generic entry
            matchedBy = "default",
            confidence = 0.3f
        )
    }

    fun getAllAvatarTypes(): List<String> {
        return listOf(
            //Cacti
            "cactus_round", "cactus_trailing",
            //Succulents
            "succulent_rosette", "succulent_jade", "succulent_string", "succulent_aloe",
            //Common houseplants
            "snake_plant", "pothos", "philodendron_heart", "monstera",
            //Ferns
            "fern_boston", "fern_maidenhair", "fern_birds_nest",
            //Other
            "prayer_plant", "spider_plant", "peace_lily", "zz_plant",
            "rubber_plant", "fiddle_leaf", "dracaena", "palm",
            //Flowering
            "orchid", "african_violet",
            //Small plants
            "peperomia", "pilea",
            //Herbs
            "herb",
            //Fallback
            "generic"
        )
    }

    fun getAllColors(): List<String> {
        return listOf(
            "green", "dark_green", "light_green",
            "blue", "purple", "pink",
            "red", "orange", "yellow", "brown", "white"
        )
    }

    fun getAvatarDescription(avatarType: String): String {
        return plantTypes.find { it.avatarType == avatarType }?.description ?: "Generic plant"
    }
}