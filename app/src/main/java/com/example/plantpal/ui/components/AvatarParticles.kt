package com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val lifetime: Float,
    val age: Float = 0f,
    val rotation: Float = 0f,
    val rotationSpeed: Float = 0f
)

enum class ParticleType {
    WATER_DROPLET,
    SPARKLE,
    HEART,
    LEAF,
    BUBBLE,
    GLOW,
    NUTRIENT
}

class ParticleSystem {
    private val _particles = mutableStateListOf<Particle>()
    val particles: List<Particle> get() = _particles

    fun generateBurst(
        type: ParticleType,
        count: Int,
        centerX: Float,
        centerY: Float,
        spreadRadius: Float = 30f
    ) {
        repeat(count) {
            _particles.add(createParticle(type, centerX, centerY, spreadRadius))
        }
    }

    fun update(deltaTime: Float = 0.016f) {
        _particles.removeAll { it.age >= it.lifetime }

        for (i in _particles.indices) {
            val p = _particles[i]
            _particles[i] = p.copy(
                x = p.x + p.velocityX * deltaTime * 60f,
                y = p.y + p.velocityY * deltaTime * 60f,
                velocityY = p.velocityY + 0.3f, // Gravity
                rotation = p.rotation + p.rotationSpeed * deltaTime * 60f,
                age = p.age + deltaTime
            )
        }
    }

    fun clear() {
        _particles.clear()
    }
    
    private fun createParticle(
        type: ParticleType,
        centerX: Float,
        centerY: Float,
        spreadRadius: Float
    ): Particle {
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val distance = Random.nextFloat() * spreadRadius
        
        return when (type) {
            ParticleType.WATER_DROPLET -> Particle(
                x = centerX + cos(angle) * distance,
                y = centerY + sin(angle) * distance - 20f,
                velocityX = (Random.nextFloat() - 0.5f) * 2.5f,
                velocityY = Random.nextFloat() * 2f + 1f,
                color = Color(0xFF4FC3F7),
                size = Random.nextFloat() * 5f + 4f,
                lifetime = 1.8f + Random.nextFloat() * 0.7f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f
            )
            
            ParticleType.SPARKLE -> Particle(
                x = centerX + cos(angle) * distance,
                y = centerY + sin(angle) * distance,
                velocityX = cos(angle) * 3f,
                velocityY = sin(angle) * 3f - 1.5f,
                color = Color(0xFFFFD700),
                size = Random.nextFloat() * 6f + 4f,
                lifetime = 1.0f + Random.nextFloat() * 0.6f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 25f
            )
            
            ParticleType.HEART -> Particle(
                x = centerX + cos(angle) * distance,
                y = centerY + sin(angle) * distance,
                velocityX = cos(angle) * 2f,
                velocityY = sin(angle) * 2f - 2f,
                color = Color(0xFFE91E63),
                size = Random.nextFloat() * 5f + 4f,
                lifetime = 1.2f + Random.nextFloat() * 0.3f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 15f
            )
            
            ParticleType.LEAF -> Particle(
                x = centerX + cos(angle) * distance,
                y = centerY - 10f,
                velocityX = (Random.nextFloat() - 0.5f) * 3f,
                velocityY = Random.nextFloat() * 0.5f + 0.5f,
                color = Color(0xFF8BC34A).copy(alpha = 0.7f),
                size = Random.nextFloat() * 4f + 3f,
                lifetime = 2.0f + Random.nextFloat() * 1f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 8f
            )
            
            ParticleType.BUBBLE -> Particle(
                x = centerX + cos(angle) * distance,
                y = centerY + 20f,
                velocityX = (Random.nextFloat() - 0.5f) * 1f,
                velocityY = -Random.nextFloat() * 2f - 1f,
                color = Color(0xFF4FC3F7).copy(alpha = 0.4f),
                size = Random.nextFloat() * 6f + 4f,
                lifetime = 2.0f + Random.nextFloat() * 1f,
                rotation = 0f,
                rotationSpeed = 0f
            )
            
            ParticleType.GLOW -> Particle(
                x = centerX + cos(angle) * distance,
                y = centerY + sin(angle) * distance,
                velocityX = cos(angle) * 0.5f,
                velocityY = sin(angle) * 0.5f - 0.5f,
                color = Color(0xFFFFD700).copy(alpha = 0.7f),
                size = Random.nextFloat() * 12f + 8f,
                lifetime = 2.0f + Random.nextFloat() * 0.8f,
                rotation = 0f,
                rotationSpeed = 0f
            )
            
            ParticleType.NUTRIENT -> Particle(
                x = centerX + cos(angle) * distance,
                y = centerY + sin(angle) * distance,
                velocityX = cos(angle) * 2f,
                velocityY = sin(angle) * 2f - 0.5f,
                color = if (Random.nextBoolean()) Color(0xFF8BC34A) else Color(0xFF795548),
                size = Random.nextFloat() * 3f + 2f,
                lifetime = 1.0f + Random.nextFloat() * 0.5f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 12f
            )
        }
    }
}

@Composable
fun ParticleEffect(
    particleSystem: ParticleSystem,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        while (true) {
            particleSystem.update()
            delay(16)
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particleSystem.particles.forEach { particle ->
            drawParticle(particle)
        }
    }
}

private fun DrawScope.drawParticle(particle: Particle) {
    val alpha = (1f - (particle.age / particle.lifetime)).coerceIn(0f, 1f)
    val color = particle.color.copy(alpha = alpha * particle.color.alpha)
    
    val center = Offset(particle.x, particle.y)

    when {
        particle.color.blue > 0.8f && particle.size < 6f -> {
            drawCircle(
                color = color,
                radius = particle.size,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.3f),
                radius = particle.size * 0.4f,
                center = Offset(particle.x - particle.size * 0.3f, particle.y - particle.size * 0.3f)
            )
        }

        particle.color.red > 0.8f && particle.color.green > 0.8f -> {
            drawStar(center, particle.size, color, particle.rotation)
        }

        particle.color.red > 0.8f && particle.color.blue > 0.3f && particle.size > 4f -> {
            drawHeart(center, particle.size, color)
        }

        particle.color.green > 0.5f && particle.size < 8f -> {
            drawLeaf(center, particle.size, color, particle.rotation)
        }

        particle.color.alpha < 0.5f && particle.size > 5f -> {
            drawCircle(
                color = color,
                radius = particle.size,
                center = center,
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.4f),
                radius = particle.size * 0.3f,
                center = Offset(particle.x - particle.size * 0.4f, particle.y - particle.size * 0.4f)
            )
        }

        particle.color.alpha < 0.7f && particle.size > 6f -> {
            drawCircle(
                color = color.copy(alpha = alpha * 0.3f),
                radius = particle.size * 1.5f,
                center = center
            )
            drawCircle(
                color = color,
                radius = particle.size,
                center = center
            )
        }

        else -> {
            drawCircle(
                color = color,
                radius = particle.size,
                center = center
            )
        }
    }
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color, rotation: Float) {
    val path = Path().apply {
        val outerRadius = size
        val innerRadius = size * 0.4f
        val angleStep = (Math.PI / 4).toFloat()
        val startAngle = rotation * Math.PI.toFloat() / 180f
        
        for (i in 0..7) {
            val angle = startAngle + angleStep * i
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val x = center.x + cos(angle) * radius
            val y = center.y + sin(angle) * radius
            
            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }
    
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val width = size * 2f
        val height = size * 2f
        
        moveTo(center.x, center.y + height * 0.3f)

        cubicTo(
            center.x - width * 0.5f, center.y - height * 0.1f,
            center.x - width * 0.5f, center.y - height * 0.5f,
            center.x, center.y - height * 0.3f
        )

        cubicTo(
            center.x + width * 0.5f, center.y - height * 0.5f,
            center.x + width * 0.5f, center.y - height * 0.1f,
            center.x, center.y + height * 0.3f
        )
        
        close()
    }
    
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawLeaf(center: Offset, size: Float, color: Color, rotation: Float) {
    val path = Path().apply {
        val angle = rotation * Math.PI.toFloat() / 180f
        val length = size * 2f
        val width = size
        
        // Rotate the leaf
        val cos = cos(angle)
        val sin = sin(angle)
        
        fun rotatePoint(x: Float, y: Float): Offset {
            return Offset(
                center.x + x * cos - y * sin,
                center.y + x * sin + y * cos
            )
        }
        
        val p1 = rotatePoint(0f, -length * 0.5f)
        val p2 = rotatePoint(width * 0.5f, 0f)
        val p3 = rotatePoint(0f, length * 0.5f)
        val p4 = rotatePoint(-width * 0.5f, 0f)
        
        moveTo(p1.x, p1.y)
        quadraticTo(p2.x, p2.y, p3.x, p3.y)
        quadraticTo(p4.x, p4.y, p1.x, p1.y)
        close()
    }
    
    drawPath(path, color, style = Fill)

    drawLine(
        color = color.copy(alpha = color.alpha * 0.5f),
        start = center,
        end = Offset(
            center.x + cos(rotation * Math.PI.toFloat() / 180f) * size,
            center.y + sin(rotation * Math.PI.toFloat() / 180f) * size
        ),
        strokeWidth = 1f
    )
}
@Composable
fun rememberParticleSystem(): ParticleSystem {
    return remember { ParticleSystem() }
}

fun ParticleSystem.waterEffect(centerX: Float, centerY: Float) {
    generateBurst(ParticleType.WATER_DROPLET, 20, centerX, centerY - 30f, 35f)

    generateBurst(ParticleType.WATER_DROPLET, 15, centerX - 20f, centerY - 25f, 25f)
    generateBurst(ParticleType.WATER_DROPLET, 15, centerX + 20f, centerY - 25f, 25f)

    generateBurst(ParticleType.BUBBLE, 12, centerX, centerY + 40f, 30f)

    generateBurst(ParticleType.WATER_DROPLET, 10, centerX, centerY, 20f)
}

fun ParticleSystem.sunlightEffect(centerX: Float, centerY: Float) {
    generateBurst(ParticleType.SPARKLE, 20, centerX, centerY - 40f, 45f)
    generateBurst(ParticleType.GLOW, 12, centerX, centerY, 50f)
    generateBurst(ParticleType.SPARKLE, 15, centerX - 25f, centerY - 30f, 30f)
    generateBurst(ParticleType.SPARKLE, 15, centerX + 25f, centerY - 30f, 30f)
    generateBurst(ParticleType.NUTRIENT, 10, centerX, centerY + 10f, 35f)
}

fun ParticleSystem.fertilizeEffect(centerX: Float, centerY: Float) {
    sunlightEffect(centerX, centerY)
}

fun ParticleSystem.happyEffect(centerX: Float, centerY: Float) {
    generateBurst(ParticleType.HEART, 8, centerX, centerY - 20f, 20f)
    generateBurst(ParticleType.SPARKLE, 12, centerX, centerY - 10f, 25f)
}

fun ParticleSystem.sickEffect(centerX: Float, centerY: Float) {
    generateBurst(ParticleType.LEAF, 5, centerX, centerY - 10f, 15f)
}
