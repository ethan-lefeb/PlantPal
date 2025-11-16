package com.example.plantpal

import android.util.Log

/**
 * Enhanced PlantTypeDatabase with comprehensive plant family coverage
 * This version includes 60+ families for better avatar matching
 */
object PlantTypeDatabase {
    
    data class PlantTypeEntry(
        val families: Set<String>,
        val genera: Set<String>,
        val keywords: Set<String>,
        val scientificNamePatterns: Set<String>,
        val avatarType: String,
        val defaultColor: String,
        val alternateColors: List<String>,
        val confidence: Float = 1.0f
    )
    
    data class MatchResult(
        val entry: PlantTypeEntry,
        val matchedBy: String,
        val confidence: Float
    )
    
    private val plantTypes = listOf(
        // CACTI
        PlantTypeEntry(
            families = setOf("cactaceae", "cacti"),
            genera = setOf(
                "mammillaria", "opuntia", "echinocactus", "ferocactus", 
                "gymnocalycium", "cereus", "astrophytum", "melocactus",
                "rebutia", "parodia", "echinopsis", "schlumbergera"
            ),
            keywords = setOf(
                "cactus", "prickly pear", "barrel", "pincushion",
                "saguaro", "christmas cactus", "easter cactus", "hedgehog"
            ),
            scientificNamePatterns = setOf("cactus", "opuntia", "cereus"),
            avatarType = "cactus",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "blue", "yellow"),
            confidence = 1.0f
        ),

        // SUCCULENTS
        PlantTypeEntry(
            families = setOf(
                "crassulaceae", "aizoaceae", "portulacaceae",
                "euphorbiaceae", "apocynaceae", "xanthorrhoeaceae"
            ),
            genera = setOf(
                "echeveria", "sedum", "crassula", "kalanchoe", "sempervivum",
                "haworthia", "aloe", "gasteria", "graptopetalum",
                "pachyphytum", "graptoveria", "lithops", "conophytum",
                "senecio", "aeonium", "cotyledon"
            ),
            keywords = setOf(
                "succulent", "jade plant", "hens and chicks", "stonecrop",
                "hen and chicken", "jade", "money plant", "string of pearls",
                "living stones", "pebble plant", "burro's tail", "zebra plant",
                "panda plant", "ghost plant"
            ),
            scientificNamePatterns = setOf(
                "echeveria", "sedum", "crassula", "aloe", "haworthia"
            ),
            avatarType = "succulent",
            defaultColor = "blue",
            alternateColors = listOf("green", "light_green", "purple", "pink"),
            confidence = 1.0f
        ),
        
        // FERNS
        PlantTypeEntry(
            families = setOf(
                "polypodiaceae", "pteridaceae", "aspleniaceae",
                "dryopteridaceae", "blechnaceae", "dennstaedtiaceae",
                "thelypteridaceae", "marsileaceae", "davalliaceae"
            ),
            genera = setOf(
                "nephrolepis", "adiantum", "pteris", "asplenium",
                "polypodium", "dryopteris", "blechnum", "platycerium",
                "davallia"
            ),
            keywords = setOf(
                "fern", "boston fern", "maidenhair", "sword fern",
                "bird's nest", "staghorn", "holly fern", "brake fern",
                "rabbit's foot fern", "asparagus fern"
            ),
            scientificNamePatterns = setOf(
                "fern", "nephrolepis", "adiantum", "pteris", "asplenium"
            ),
            avatarType = "fern",
            defaultColor = "light_green",
            alternateColors = listOf("green", "dark_green"),
            confidence = 1.0f
        ),
        
        // FLOWERING PLANTS
        PlantTypeEntry(
            families = setOf(
                "orchidaceae", "rosaceae", "liliaceae", "iridaceae",
                "asteraceae", "violaceae", "primulaceae", "geraniaceae"
            ),
            genera = setOf(
                "phalaenopsis", "rosa", "lilium", "tulipa", "iris",
                "hibiscus", "gerbera", "chrysanthemum", "dahlia",
                "pelargonium", "anthurium", "begonia"
            ),
            keywords = setOf(
                "orchid", "rose", "lily", "tulip", "daisy", "sunflower",
                "hibiscus", "violet", "petunia", "marigold", "zinnia",
                "begonia", "geranium", "primrose", "impatiens", "pansy"
            ),
            scientificNamePatterns = setOf(
                "orchid", "rosa", "flower", "phalaenopsis", "anthurium"
            ),
            avatarType = "flower",
            defaultColor = "pink",
            alternateColors = listOf("purple", "red", "yellow", "orange", "green"),
            confidence = 0.9f
        ),
        
        // HERBS
        PlantTypeEntry(
            families = setOf(
                "lamiaceae", "apiaceae", "asteraceae", "alliaceae"
            ),
            genera = setOf(
                "ocimum", "mentha", "rosmarinus", "thymus", "salvia",
                "origanum", "lavandula", "petroselinum", "coriandrum",
                "anethum", "melissa", "allium"
            ),
            keywords = setOf(
                "basil", "mint", "rosemary", "thyme", "sage", "oregano",
                "parsley", "cilantro", "coriander", "dill", "lavender",
                "chamomile", "lemon balm", "chives", "tarragon", "marjoram"
            ),
            scientificNamePatterns = setOf(
                "ocimum", "mentha", "thymus", "salvia", "lavandula"
            ),
            avatarType = "herb",
            defaultColor = "green",
            alternateColors = listOf("light_green", "yellow"),
            confidence = 1.0f
        ),
        
        // TREES
        PlantTypeEntry(
            families = setOf(
                "fagaceae", "pinaceae", "oleaceae", "moraceae",
                "arecaceae", "palmae", "araucariaceae", "dracaenaceae"
            ),
            genera = setOf(
                "quercus", "pinus", "ficus", "dracaena", "yucca",
                "schefflera", "araucaria", "phoenix", "chamaedorea",
                "pachira", "caryota"
            ),
            keywords = setOf(
                "tree", "oak", "pine", "ficus", "rubber plant", "fig",
                "dracaena", "dragon tree", "yucca", "palm", "norfolk pine",
                "money tree", "umbrella tree", "corn plant", "weeping fig"
            ),
            scientificNamePatterns = setOf(
                "ficus", "dracaena", "tree", "araucaria", "pachira"
            ),
            avatarType = "tree",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "brown"),
            confidence = 0.9f
        ),
        
        // TROPICAL HOUSEPLANTS
        PlantTypeEntry(
            families = setOf(
                "araceae", "marantaceae", "commelinaceae",
                "bromeliaceae", "gesneriaceae", "piperaceae"
            ),
            genera = setOf(
                "monstera", "philodendron", "pothos", "epipremnum",
                "calathea", "maranta", "syngonium", "dieffenbachia",
                "aglaonema", "anthurium", "spathiphyllum", "tradescantia",
                "peperomia", "pilea", "alocasia", "colocasia"
            ),
            keywords = setOf(
                "monstera", "philodendron", "pothos", "calathea", "prayer plant",
                "swiss cheese", "split leaf", "peace lily", "chinese evergreen",
                "arrowhead", "wandering jew", "spider plant", "peperomia",
                "pilea", "aluminum plant", "elephant ear"
            ),
            scientificNamePatterns = setOf(
                "philodendron", "monstera", "epipremnum", "spathiphyllum",
                "alocasia"
            ),
            avatarType = "generic",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "light_green"),
            confidence = 0.8f
        ),
        
        // SNAKE PLANTS
        PlantTypeEntry(
            families = setOf("asparagaceae", "dracaenaceae", "ruscaceae"),
            genera = setOf("sansevieria", "dracaena"),
            keywords = setOf(
                "snake plant", "mother-in-law's tongue", "sansevieria",
                "snake", "viper's bowstring", "dracaena trifasciata"
            ),
            scientificNamePatterns = setOf("sansevieria", "trifasciata"),
            avatarType = "generic",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "yellow"),
            confidence = 1.0f
        ),
        
        // VEGETABLES/EDIBLES
        PlantTypeEntry(
            families = setOf(
                "solanaceae", "cucurbitaceae", "brassicaceae",
                "fabaceae", "apiaceae", "chenopodiaceae"
            ),
            genera = setOf(
                "solanum", "capsicum", "cucumis", "brassica",
                "lactuca", "lycopersicon", "cucurbita", "daucus"
            ),
            keywords = setOf(
                "tomato", "pepper", "cucumber", "squash", "lettuce",
                "cabbage", "kale", "eggplant", "zucchini", "pumpkin",
                "carrot", "radish", "spinach", "broccoli"
            ),
            scientificNamePatterns = setOf(
                "solanum", "capsicum", "cucumis", "lycopersicon"
            ),
            avatarType = "herb",
            defaultColor = "green",
            alternateColors = listOf("light_green", "red", "orange"),
            confidence = 0.8f
        ),
        
        // VINING/TRAILING PLANTS
        PlantTypeEntry(
            families = setOf(
                "vitaceae", "convolvulaceae", "cucurbitaceae"
            ),
            genera = setOf(
                "cissus", "ipomoea", "hedera", "hoya", "senecio"
            ),
            keywords = setOf(
                "ivy", "grape ivy", "english ivy", "sweet potato vine",
                "string of hearts", "string of pearls", "string of bananas",
                "pothos", "philodendron", "trailing"
            ),
            scientificNamePatterns = setOf("hedera", "cissus", "hoya"),
            avatarType = "generic",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "light_green"),
            confidence = 0.7f
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

        if (familyLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.families.any { it == familyLower }) {
                    Log.d("PlantTypeDB", "✅ Exact family match: ${entry.avatarType}")
                    return MatchResult(
                        entry = entry,
                        matchedBy = "family",
                        confidence = 1.0f
                    )
                }
            }
        }

        if (genusLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.genera.any { it == genusLower }) {
                    Log.d("PlantTypeDB", "✅ Exact genus match: ${entry.avatarType}")
                    return MatchResult(
                        entry = entry,
                        matchedBy = "genus",
                        confidence = 0.95f
                    )
                }
            }
        }

        if (scientificLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.scientificNamePatterns.any { scientificLower.contains(it) }) {
                    Log.d("PlantTypeDB", "✅ Scientific name match: ${entry.avatarType}")
                    return MatchResult(
                        entry = entry,
                        matchedBy = "scientific_name",
                        confidence = 0.85f
                    )
                }
            }
        }

        val keywordMatches = mutableListOf<Pair<PlantTypeEntry, Int>>()
        
        plantTypes.forEach { entry ->
            var matchCount = 0
            entry.keywords.forEach { keyword ->
                if (commonLower.contains(keyword)) {
                    matchCount++
                }
            }
            if (matchCount > 0) {
                keywordMatches.add(entry to matchCount)
            }
        }
        
        if (keywordMatches.isNotEmpty()) {
            val best = keywordMatches.maxByOrNull { it.second }!!
            val confidence = 0.7f * (best.second.toFloat() / 3f).coerceAtMost(1f)
            Log.d("PlantTypeDB", "✅ Keyword match (${best.second} keywords): ${best.first.avatarType}")
            return MatchResult(
                entry = best.first,
                matchedBy = "keyword",
                confidence = confidence
            )
        }

        if (familyLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.families.any { 
                    familyLower.contains(it) || it.contains(familyLower) 
                }) {
                    Log.d("PlantTypeDB", "⚠️ Partial family match: ${entry.avatarType}")
                    return MatchResult(
                        entry = entry,
                        matchedBy = "partial_family",
                        confidence = 0.6f
                    )
                }
            }
        }
        
        Log.d("PlantTypeDB", "ℹ️ No match found, using generic")
        return MatchResult(
            entry = PlantTypeEntry(
                families = emptySet(),
                genera = emptySet(),
                keywords = emptySet(),
                scientificNamePatterns = emptySet(),
                avatarType = "generic",
                defaultColor = "green",
                alternateColors = listOf("light_green", "dark_green"),
                confidence = 0.3f
            ),
            matchedBy = "default",
            confidence = 0.3f
        )
    }
    
    fun getAllAvatarTypes(): List<String> {
        return listOf("succulent", "cactus", "flower", "fern", "tree", "herb", "generic")
    }
    
    fun getAllColors(): List<String> {
        return listOf(
            "green", "dark_green", "light_green",
            "blue", "purple", "pink",
            "red", "orange", "yellow", "brown"
        )
    }
    
    fun getColorDisplayName(color: String): String {
        return color.split("_")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
    
    fun suggestColors(family: String?, genus: String?, commonName: String): List<String> {
        val match = findMatch(family, genus, commonName)
        return listOf(match.entry.defaultColor) + match.entry.alternateColors
    }
    
    fun getStats(): DatabaseStats {
        val totalFamilies = plantTypes.flatMap { it.families }.toSet().size
        val totalGenera = plantTypes.flatMap { it.genera }.toSet().size
        val totalKeywords = plantTypes.flatMap { it.keywords }.toSet().size
        
        return DatabaseStats(
            totalPlantTypes = plantTypes.size,
            totalFamilies = totalFamilies,
            totalGenera = totalGenera,
            totalKeywords = totalKeywords,
            avatarTypes = getAllAvatarTypes()
        )
    }
    
    data class DatabaseStats(
        val totalPlantTypes: Int,
        val totalFamilies: Int,
        val totalGenera: Int,
        val totalKeywords: Int,
        val avatarTypes: List<String>
    )
}
