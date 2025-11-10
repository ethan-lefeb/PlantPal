package com.example.plantpal

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
        PlantTypeEntry(
            families = setOf("cactaceae"),
            genera = setOf("mammillaria", "opuntia", "echinocactus", "ferocactus", "gymnocalycium"),
            keywords = setOf("cactus", "prickly pear", "barrel", "pincushion"),
            scientificNamePatterns = setOf("cactus"),
            avatarType = "cactus",
            defaultColor = "green",
            alternateColors = listOf("dark_green", "blue", "yellow"),
            confidence = 1.0f
        ),
        
        PlantTypeEntry(
            families = setOf("crassulaceae", "aizoaceae"),
            genera = setOf(
                "echeveria", "sedum", "crassula", "kalanchoe", "sempervivum",
                "haworthia", "aloe", "gasteria", "graptopetalum"
            ),
            keywords = setOf(
                "succulent", "jade plant", "hens and chicks", "stonecrop",
                "hen and chicken", "jade", "money plant", "string of pearls"
            ),
            scientificNamePatterns = setOf("echeveria", "sedum", "crassula"),
            avatarType = "succulent",
            defaultColor = "blue",
            alternateColors = listOf("green", "light_green", "purple", "pink"),
            confidence = 1.0f
        ),
        
        PlantTypeEntry(
            families = setOf(
                "polypodiaceae", "pteridaceae", "aspleniaceae",
                "dryopteridaceae", "blechnaceae"
            ),
            genera = setOf(
                "nephrolepis", "adiantum", "pteris", "asplenium",
                "polypodium", "dryopteris"
            ),
            keywords = setOf(
                "fern", "boston fern", "maidenhair", "sword fern",
                "bird's nest", "staghorn"
            ),
            scientificNamePatterns = setOf("fern", "nephrolepis", "adiantum"),
            avatarType = "fern",
            defaultColor = "light_green",
            alternateColors = listOf("green", "dark_green"),
            confidence = 1.0f
        ),
        
        PlantTypeEntry(
            families = setOf("orchidaceae", "rosaceae", "liliaceae", "iridaceae"),
            genera = setOf(
                "phalaenopsis", "rosa", "lilium", "tulipa",
                "iris", "orchid", "hibiscus"
            ),
            keywords = setOf(
                "orchid", "rose", "lily", "tulip", "daisy", "sunflower",
                "hibiscus", "violet", "petunia", "marigold", "zinnia"
            ),
            scientificNamePatterns = setOf("orchid", "rosa", "flower"),
            avatarType = "flower",
            defaultColor = "pink",
            alternateColors = listOf("purple", "red", "yellow", "orange", "green"),
            confidence = 0.9f
        ),
        
        PlantTypeEntry(
            families = setOf("lamiaceae", "apiaceae"),
            genera = setOf(
                "ocimum", "mentha", "rosmarinus", "thymus", "salvia",
                "origanum", "lavandula", "petroselinum"
            ),
            keywords = setOf(
                "basil", "mint", "rosemary", "thyme", "sage", "oregano",
                "parsley", "cilantro", "coriander", "dill", "lavender",
                "chamomile", "lemon balm"
            ),
            scientificNamePatterns = setOf("ocimum", "mentha", "thymus"),
            avatarType = "herb",
            defaultColor = "green",
            alternateColors = listOf("light_green", "yellow"),
            confidence = 1.0f
        ),
        
        PlantTypeEntry(
            families = setOf(
                "fagaceae", "pinaceae", "oleaceae", "moraceae",
                "arecaceae", "palmae"
            ),
            genera = setOf(
                "quercus", "pinus", "ficus", "dracaena", "yucca",
                "schefflera", "araucaria", "phoenix"
            ),
            keywords = setOf(
                "tree", "oak", "pine", "ficus", "rubber plant", "fig",
                "dracaena", "dragon tree", "yucca", "palm", "norfolk pine",
                "money tree", "umbrella tree"
            ),
            scientificNamePatterns = setOf("ficus", "dracaena", "tree"),
            avatarType = "tree",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "brown"),
            confidence = 0.9f
        ),
        
        PlantTypeEntry(
            families = setOf(
                "araceae", "marantaceae", "commelinaceae",
                "bromeliaceae", "gesneriaceae"
            ),
            genera = setOf(
                "monstera", "philodendron", "pothos", "calathea", "maranta",
                "syngonium", "dieffenbachia", "aglaonema", "anthurium",
                "spathiphyllum", "tradescantia"
            ),
            keywords = setOf(
                "monstera", "philodendron", "pothos", "calathea", "prayer plant",
                "swiss cheese", "split leaf", "peace lily", "chinese evergreen",
                "arrowhead", "wandering jew", "spider plant", "snake plant"
            ),
            scientificNamePatterns = setOf("philodendron", "monstera", "epipremnum"),
            avatarType = "generic",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "light_green"),
            confidence = 0.8f
        ),
        
        PlantTypeEntry(
            families = setOf("asparagaceae"),
            genera = setOf("sansevieria", "dracaena"),
            keywords = setOf(
                "snake plant", "mother-in-law's tongue", "sansevieria",
                "snake", "viper's bowstring"
            ),
            scientificNamePatterns = setOf("sansevieria", "trifasciata"),
            avatarType = "generic",
            defaultColor = "dark_green",
            alternateColors = listOf("green", "yellow"),
            confidence = 1.0f
        ),
        
        PlantTypeEntry(
            families = setOf("solanaceae", "cucurbitaceae", "brassicaceae"),
            genera = setOf("solanum", "capsicum", "cucumis", "brassica"),
            keywords = setOf(
                "tomato", "pepper", "cucumber", "squash", "lettuce",
                "cabbage", "kale", "eggplant", "zucchini"
            ),
            scientificNamePatterns = setOf("solanum", "capsicum"),
            avatarType = "herb",
            defaultColor = "green",
            alternateColors = listOf("light_green", "red", "orange"),
            confidence = 0.8f
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
        
        if (familyLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.families.any { it == familyLower }) {
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
            return MatchResult(
                entry = best.first,
                matchedBy = "keyword",
                confidence = 0.7f * (best.second.toFloat() / 3f).coerceAtMost(1f)
            )
        }
        
        if (familyLower.isNotEmpty()) {
            plantTypes.forEach { entry ->
                if (entry.families.any { familyLower.contains(it) || it.contains(familyLower) }) {
                    return MatchResult(
                        entry = entry,
                        matchedBy = "partial_family",
                        confidence = 0.6f
                    )
                }
            }
        }
        
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
