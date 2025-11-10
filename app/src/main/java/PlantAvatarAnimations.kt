package com.example.plantpal

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos

enum class AnimationType {
    IDLE,
    WATERING,
    FERTILIZING,
    HAPPY,
    THIRSTY,
    SICK,
    GROWING
}

data class AnimationState(
    val type: AnimationType = AnimationType.IDLE,
    val progress: Float = 0f,
    val intensity: Float = 1f,
    val isLooping: Boolean = true
)

data class AnimationParameters(
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val verticalOffset: Float = 0f,
    val horizontalOffset: Float = 0f,
    val colorMultiplier: Float = 1f,
    val alpha: Float = 1f,
    val bounce: Float = 0f
)
class AvatarAnimationController {
    private var _animationState = mutableStateOf(AnimationState())
    val animationState: State<AnimationState> = _animationState
    
    private var _currentTime = mutableStateOf(0f)
    private var _animationStartTime = 0f

    fun triggerAnimation(
        type: AnimationType,
        duration: Long = 2000L,
        intensity: Float = 1f,
        loop: Boolean = false
    ) {
        _animationStartTime = _currentTime.value
        _animationState.value = AnimationState(
            type = type,
            progress = 0f,
            intensity = intensity.coerceIn(0f, 1f),
            isLooping = loop || type == AnimationType.IDLE
        )
    }

    fun updateIdleAnimation(health: String, daysSinceWatering: Int) {
        if (_animationState.value.type != AnimationType.IDLE) {
            return
        }
        
        val intensity = when (health) {
            "healthy" -> 1.0f
            "warning" -> 0.6f
            "critical" -> 0.3f
            else -> 1.0f
        }

        val wateringFactor = when {
            daysSinceWatering < 2 -> 1.0f
            daysSinceWatering < 5 -> 0.7f
            daysSinceWatering < 10 -> 0.4f
            else -> 0.2f
        }
        
        _animationState.value = _animationState.value.copy(
            intensity = intensity * wateringFactor
        )
    }

    suspend fun updateAnimation() {
        _currentTime.value += 0.016f
        
        val state = _animationState.value
        val elapsed = _currentTime.value - _animationStartTime
        val duration = getAnimationDuration(state.type)
        val newProgress = (elapsed / duration).coerceIn(0f, 1f)
        
        _animationState.value = state.copy(progress = newProgress)

        if (newProgress >= 1f && !state.isLooping) {
            triggerAnimation(AnimationType.IDLE, loop = true)
        }
        
        delay(16)
    }

    fun calculateParameters(): AnimationParameters {
        val state = _animationState.value
        val time = _currentTime.value
        
        return when (state.type) {
            AnimationType.IDLE -> calculateIdleParameters(time, state.intensity)
            AnimationType.WATERING -> calculateWateringParameters(state.progress, state.intensity)
            AnimationType.FERTILIZING -> calculateFertilizingParameters(state.progress, state.intensity)
            AnimationType.HAPPY -> calculateHappyParameters(state.progress, state.intensity)
            AnimationType.THIRSTY -> calculateThirstyParameters(time, state.intensity)
            AnimationType.SICK -> calculateSickParameters(time, state.intensity)
            AnimationType.GROWING -> calculateGrowingParameters(state.progress, state.intensity)
        }
    }
    
    private fun getAnimationDuration(type: AnimationType): Float {
        return when (type) {
            AnimationType.IDLE -> Float.MAX_VALUE
            AnimationType.WATERING -> 2.0f
            AnimationType.FERTILIZING -> 2.5f
            AnimationType.HAPPY -> 1.5f
            AnimationType.THIRSTY -> Float.MAX_VALUE
            AnimationType.SICK -> Float.MAX_VALUE
            AnimationType.GROWING -> 3.0f
        }
    }
    
    private fun calculateIdleParameters(time: Float, intensity: Float): AnimationParameters {
        val breathCycle = sin(time * 1.5f) * 0.02f * intensity
        val scale = 1f + breathCycle
        val swayCycle = sin(time * 0.8f) * 3f * intensity
        val rotation = swayCycle
        val bobCycle = sin(time * 2f) * 1.5f * intensity
        val verticalOffset = bobCycle
        
        return AnimationParameters(
            scale = scale,
            rotation = rotation,
            verticalOffset = verticalOffset,
            bounce = bobCycle
        )
    }
    
    private fun calculateWateringParameters(progress: Float, intensity: Float): AnimationParameters {
        val scaleCurve = if (progress < 0.5f) {
            1f + (progress * 2f) * 0.15f * intensity
        } else {
            1f + ((1f - (progress - 0.5f) * 2f) * 0.15f * intensity)
        }

        val verticalOffset = -sin(progress * Math.PI.toFloat()) * 5f * intensity
        val colorMultiplier = 1f + (sin(progress * Math.PI.toFloat()) * 0.2f * intensity)
        
        return AnimationParameters(
            scale = scaleCurve,
            verticalOffset = verticalOffset,
            colorMultiplier = colorMultiplier,
            bounce = verticalOffset
        )
    }
    
    private fun calculateFertilizingParameters(progress: Float, intensity: Float): AnimationParameters {
        val glowCycle = sin(progress * Math.PI.toFloat() * 4f) * 0.3f * intensity
        val colorMultiplier = 1f + glowCycle
        val scale = 1f + (sin(progress * Math.PI.toFloat() * 4f) * 0.05f * intensity)
        val rotation = sin(progress * Math.PI.toFloat() * 2f) * 5f * intensity
        
        return AnimationParameters(
            scale = scale,
            rotation = rotation,
            colorMultiplier = colorMultiplier
        )
    }
    
    private fun calculateHappyParameters(progress: Float, intensity: Float): AnimationParameters {
        val bounceHeight = 15f * intensity
        val bounce = abs(sin(progress * Math.PI.toFloat() * 3f)) * bounceHeight
        val scale = 1f + (abs(sin(progress * Math.PI.toFloat() * 3f)) * 0.08f * intensity)
        val rotation = sin(progress * Math.PI.toFloat() * 6f) * 8f * intensity
        
        return AnimationParameters(
            scale = scale,
            rotation = rotation,
            verticalOffset = -bounce,
            bounce = bounce
        )
    }
    
    private fun calculateThirstyParameters(time: Float, intensity: Float): AnimationParameters {
        val droopAmount = 5f + (sin(time * 0.5f) * 2f)
        val rotation = -3f * intensity
        val scale = 1f - (0.05f * intensity)
        val colorMultiplier = 1f - (0.3f * intensity)
        
        return AnimationParameters(
            scale = scale,
            rotation = rotation,
            verticalOffset = droopAmount * intensity,
            colorMultiplier = colorMultiplier
        )
    }
    
    private fun calculateSickParameters(time: Float, intensity: Float): AnimationParameters {
        val droopAmount = 8f + (sin(time * 0.3f) * 3f)
        val rotation = -5f * intensity
        val scale = 1f - (0.1f * intensity)
        val colorMultiplier = 1f - (0.5f * intensity)
        val alpha = 1f - (0.1f * intensity)
        
        return AnimationParameters(
            scale = scale,
            rotation = rotation,
            verticalOffset = droopAmount * intensity,
            colorMultiplier = colorMultiplier,
            alpha = alpha
        )
    }
    
    private fun calculateGrowingParameters(progress: Float, intensity: Float): AnimationParameters {
        val verticalOffset = -progress * 10f * intensity
        val scale = 1f + (progress * 0.1f * intensity)
        val rotation = sin(progress * Math.PI.toFloat() * 2f) * 3f * intensity
        
        return AnimationParameters(
            scale = scale,
            rotation = rotation,
            verticalOffset = verticalOffset
        )
    }
    
    private fun abs(value: Float): Float = if (value < 0) -value else value
}

@Composable
fun rememberAvatarAnimationController(
    health: String = "healthy",
    daysSinceWatering: Int = 0
): AvatarAnimationController {
    val controller = remember { AvatarAnimationController() }
    
    LaunchedEffect(Unit) {
        controller.triggerAnimation(AnimationType.IDLE, loop = true)
        while (true) {
            controller.updateAnimation()
        }
    }
    
    LaunchedEffect(health, daysSinceWatering) {
        controller.updateIdleAnimation(health, daysSinceWatering)
    }
    
    return controller
}
