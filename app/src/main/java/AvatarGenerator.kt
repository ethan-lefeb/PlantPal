package com.example.plantpal

import android.util.Log

object AvatarGenerator {
    private const val TAG = "AvatarGen"

    data class GenerationResult(
        val config: AvatarConfig,
        val confidence: Float,
        val matchedBy: String,
        val suggestions: List<String> = emptyList() // Alternate suggestions if confidence is low
    )

    fun generateAvatarForPlant(
        family: String?,
        genus: String?,
        commonName: String,
        scientificName: String = ""
    ): AvatarConfig {
        Log.d(TAG, "=== Generating Avatar ===")
        Log.d(TAG, "Family: '$family'")
        Log.d(TAG, "Genus: '$genus'")
        Log.d(TAG, "Common Name: '$commonName'")
        Log.d(TAG, "Scientific Name: '$scientificName'")

        val match = PlantTypeDatabase.findMatch(family, genus, commonName, scientificName)
        
        val config = AvatarConfig(
            baseType = match.entry.avatarType,
            color = match.entry.defaultColor
        )
        
        Log.d(TAG, "Generated Base Type: '${config.baseType}'")
        Log.d(TAG, "Generated Color: '${config.color}'")
        Log.d(TAG, "Match Confidence: ${match.confidence}")
        Log.d(TAG, "Matched By: ${match.matchedBy}")
        Log.d(TAG, "=== Complete ===")
        
        return config
    }

    fun generateAvatarWithDetails(
        family: String?,
        genus: String?,
        commonName: String,
        scientificName: String = ""
    ): GenerationResult {
        Log.d(TAG, "=== Generating Avatar (Detailed) ===")
        Log.d(TAG, "Family: '$family'")
        Log.d(TAG, "Genus: '$genus'")
        Log.d(TAG, "Common Name: '$commonName'")
        Log.d(TAG, "Scientific Name: '$scientificName'")

        val match = PlantTypeDatabase.findMatch(family, genus, commonName, scientificName)
        
        val config = AvatarConfig(
            baseType = match.entry.avatarType,
            color = match.entry.defaultColor
        )
        
        Log.d(TAG, "Generated Base Type: '${config.baseType}'")
        Log.d(TAG, "Generated Color: '${config.color}'")
        Log.d(TAG, "Match Confidence: ${match.confidence}")
        Log.d(TAG, "Matched By: ${match.matchedBy}")
        Log.d(TAG, "=== Complete ===")

        val suggestions = if (match.confidence < 0.7f) {
            match.entry.alternateColors.take(2)
        } else {
            emptyList()
        }
        
        return GenerationResult(
            config = config,
            confidence = match.confidence,
            matchedBy = match.matchedBy,
            suggestions = suggestions
        )
    }

    fun generateForPlantProfile(plant: PlantProfile): GenerationResult {
        return generateAvatarWithDetails(
            family = plant.careInfo.family,
            genus = plant.careInfo.genus,
            commonName = plant.commonName,
            scientificName = plant.scientificName
        )
    }

    fun generateRandomAvatar(): AvatarConfig {
        val types = PlantTypeDatabase.getAllAvatarTypes()
        val colors = PlantTypeDatabase.getAllColors()
        
        return AvatarConfig(
            baseType = types.random(),
            color = colors.random()
        )
    }

    fun updateAvatarForPlantState(
        currentConfig: AvatarConfig,
        health: String,
        lastWatered: Long,
        wateringFrequency: Int
    ): AvatarConfig {
        return currentConfig
    }

    fun suggestImprovedAvatar(
        plant: PlantProfile,
        currentConfig: AvatarConfig
    ): AvatarConfig? {
        val result = generateForPlantProfile(plant)
        return if (result.config != currentConfig && result.confidence > 0.8f) {
            result.config
        } else {
            null
        }
    }

    fun validateAvatar(plant: PlantProfile, config: AvatarConfig): ValidationResult {
        val expectedResult = generateForPlantProfile(plant)
        
        val typeMatches = config.baseType == expectedResult.config.baseType
        val colorReasonable = config.color in PlantTypeDatabase.getAllColors()
        
        val issues = mutableListOf<String>()
        
        if (!typeMatches && expectedResult.confidence > 0.7f) {
            issues.add("Avatar type '${config.baseType}' doesn't match plant family. Expected '${expectedResult.config.baseType}'")
        }
        
        if (!colorReasonable) {
            issues.add("Color '${config.color}' is not a valid option")
        }
        
        val isValid = issues.isEmpty()
        val suggestion = if (!isValid) expectedResult.config else null
        
        return ValidationResult(
            isValid = isValid,
            issues = issues,
            suggestion = suggestion
        )
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val issues: List<String>,
        val suggestion: AvatarConfig?
    )

    fun explainAvatarChoice(result: GenerationResult): String {
        return when (result.matchedBy) {
            "family" -> "Avatar chosen based on plant family. This is highly accurate."
            "genus" -> "Avatar chosen based on plant genus. This is very reliable."
            "scientific_name" -> "Avatar matched using scientific name patterns."
            "keyword" -> "Avatar matched using common name keywords. " +
                if (result.confidence > 0.7f) "Good match." else "Consider customizing if incorrect."
            "partial_family" -> "Avatar matched using partial family name. May need adjustment."
            "default" -> "Generic avatar used - plant type not in database. " +
                "You can customize this avatar to better match your plant."
            else -> "Avatar generated using available plant information."
        }
    }
}
