package com.example.plantpal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun PlantAvatar(
    avatarConfig: AvatarConfig,
    health: String = "healthy",
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animated: Boolean = true,
    animationController: AvatarAnimationController? = null // Allow external controller
) {
    val baseColor = parseColorString(avatarConfig.color)
    val potColor = Color(0xFFD2691E)
    
    // Use the advanced animation controller (external or create new)
    val controller = animationController ?: if (animated) {
        rememberAvatarAnimationController(
            health = health,
            daysSinceWatering = 0 // Could pass actual value if available
        )
    } else null
    
    val animParams = controller?.calculateParameters() ?: AnimationParameters()
    var blinkProgress by remember { mutableStateOf(0f) }
    
    if (animated) {
        LaunchedEffect(Unit) {
            while (true) {
                blinkProgress = (blinkProgress + 0.05f) % (2f * Math.PI.toFloat())
                delay(50)
            }
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFFFF8DC)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val centerX = canvasSize.width / 2
            val centerY = canvasSize.height / 2
            val baseScale = canvasSize.minDimension / 120f
            val scale = baseScale * animParams.scale
            val totalVerticalOffset = animParams.verticalOffset + animParams.bounce

            drawOval(
                color = Color(0x40000000),
                topLeft = Offset(centerX - 40f * scale, centerY + 48f * scale),
                size = Size(80f * scale, 8f * scale)
            )

            drawPot(centerX, centerY, scale, potColor)
            drawFaceOnPot(centerX, centerY + 20f * scale, scale, health, blinkProgress)

            translate(animParams.horizontalOffset, totalVerticalOffset) {
                rotate(degrees = animParams.rotation, pivot = Offset(centerX, centerY)) {
                    scale(scale = animParams.scale, pivot = Offset(centerX, centerY)) {
                        val adjustedColor = baseColor.copy(alpha = animParams.alpha)
                            .let { color ->
                                Color(
                                    red = (color.red * animParams.colorMultiplier).coerceIn(0f, 1f),
                                    green = (color.green * animParams.colorMultiplier).coerceIn(0f, 1f),
                                    blue = (color.blue * animParams.colorMultiplier).coerceIn(0f, 1f),
                                    alpha = color.alpha
                                )
                            }
                        
                        when (avatarConfig.baseType.lowercase()) {
                            "succulent" -> drawSucculentInPot(centerX, centerY, baseScale, adjustedColor)
                            "cactus" -> drawCactusInPot(centerX, centerY, baseScale, adjustedColor)
                            "flower" -> drawFlowerInPot(centerX, centerY, baseScale, adjustedColor)
                            "fern" -> drawFernInPot(centerX, centerY, baseScale, adjustedColor)
                            "tree" -> drawTreeInPot(centerX, centerY, baseScale, adjustedColor)
                            "herb" -> drawHerbInPot(centerX, centerY, baseScale, adjustedColor)
                            else -> drawGenericPlantInPot(centerX, centerY, baseScale, adjustedColor)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPot(x: Float, y: Float, scale: Float, potColor: Color) {
    val potY = y + 5f * scale

    val rimHeight = 6f * scale
    drawRect(
        color = Color(0xFF8B4513),
        topLeft = Offset(x - 45f * scale, potY - rimHeight),
        size = Size(90f * scale, rimHeight)
    )

    val potPath = Path().apply {
        moveTo(x - 43f * scale, potY)
        lineTo(x - 32f * scale, potY + 40f * scale)
        lineTo(x + 32f * scale, potY + 40f * scale)
        lineTo(x + 43f * scale, potY)
        close()
    }
    
    drawPath(
        path = potPath,
        color = potColor
    )

    drawPath(
        path = potPath,
        color = Color(0xFF2C2C2C),
        style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
    )

    drawRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - 45f * scale, potY - rimHeight),
        size = Size(90f * scale, rimHeight),
        style = Stroke(width = 3.5f * scale)
    )

    drawOval(
        color = Color(0xFF6B4423),
        topLeft = Offset(x - 43f * scale, potY - 3f * scale),
        size = Size(86f * scale, 10f * scale)
    )

    drawOval(
        color = Color(0xFF8B6A47),
        topLeft = Offset(x - 38f * scale, potY - 2f * scale),
        size = Size(76f * scale, 6f * scale)
    )
}

private fun DrawScope.drawFaceOnPot(
    x: Float,
    y: Float,
    scale: Float,
    health: String,
    animationProgress: Float
) {
    val eyeY = y
    val eyeSize = 7f * scale
    val blinkProgress = sin(animationProgress * 2)
    val eyeHeight = if (blinkProgress > 0.9f) eyeSize * 0.3f else eyeSize

    drawOval(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - 16f * scale, eyeY - eyeHeight / 2),
        size = Size(eyeSize, eyeHeight)
    )

    drawOval(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x + 9f * scale, eyeY - eyeHeight / 2),
        size = Size(eyeSize, eyeHeight)
    )

    val mouthY = y + 14f * scale
    when (health) {
        "healthy" -> {
            val path = Path().apply {
                moveTo(x - 12f * scale, mouthY)
                quadraticTo(
                    x, mouthY + 10f * scale,
                    x + 12f * scale, mouthY
                )
            }
            drawPath(
                path = path,
                color = Color(0xFF2C2C2C),
                style = Stroke(width = 3f * scale, cap = StrokeCap.Round)
            )
        }
        "warning" -> {
            drawLine(
                color = Color(0xFF2C2C2C),
                start = Offset(x - 12f * scale, mouthY),
                end = Offset(x + 12f * scale, mouthY),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
        }
        else -> {
            val path = Path().apply {
                moveTo(x - 12f * scale, mouthY + 6f * scale)
                quadraticTo(
                    x, mouthY - 4f * scale,
                    x + 12f * scale, mouthY + 6f * scale
                )
            }
            drawPath(
                path = path,
                color = Color(0xFF2C2C2C),
                style = Stroke(width = 3f * scale, cap = StrokeCap.Round)
            )
        }
    }

    if (health == "healthy") {
        drawCircle(
            color = Color(0xFFFFB6C1).copy(alpha = 0.6f),
            radius = 6f * scale,
            center = Offset(x - 24f * scale, y + 7f * scale)
        )
        drawCircle(
            color = Color(0xFFFFB6C1).copy(alpha = 0.6f),
            radius = 6f * scale,
            center = Offset(x + 24f * scale, y + 7f * scale)
        )
    }
}

private fun DrawScope.drawSucculentInPot(x: Float, y: Float, scale: Float, color: Color) {
    val plantY = y - 5f * scale

    for (i in 0..7) {
        val angle = (i * 45f) * Math.PI.toFloat() / 180f
        val leafX = x + kotlin.math.cos(angle) * 15f * scale
        val leafY = plantY + kotlin.math.sin(angle) * 15f * scale
        
        drawCircle(
            color = color.copy(alpha = 0.85f),
            radius = 9f * scale,
            center = Offset(leafX, leafY)
        )

        drawCircle(
            color = Color(0xFF2C2C2C),
            radius = 9f * scale,
            center = Offset(leafX, leafY),
            style = Stroke(width = 2.5f * scale)
        )
    }

    drawCircle(
        color = color,
        radius = 7f * scale,
        center = Offset(x, plantY)
    )
    drawCircle(
        color = Color(0xFF2C2C2C),
        radius = 7f * scale,
        center = Offset(x, plantY),
        style = Stroke(width = 2.5f * scale)
    )
}

private fun DrawScope.drawCactusInPot(x: Float, y: Float, scale: Float, color: Color) {
    val plantY = y

    drawRoundRect(
        color = color,
        topLeft = Offset(x - 11f * scale, plantY - 35f * scale),
        size = Size(22f * scale, 40f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale)
    )

    drawRoundRect(
        color = color,
        topLeft = Offset(x - 24f * scale, plantY - 20f * scale),
        size = Size(14f * scale, 22f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
    )

    drawRoundRect(
        color = color,
        topLeft = Offset(x + 10f * scale, plantY - 15f * scale),
        size = Size(14f * scale, 18f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
    )

    drawRoundRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - 11f * scale, plantY - 35f * scale),
        size = Size(22f * scale, 40f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale),
        style = Stroke(width = 2.5f * scale)
    )
    
    drawRoundRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - 24f * scale, plantY - 20f * scale),
        size = Size(14f * scale, 22f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale),
        style = Stroke(width = 2.5f * scale)
    )
    
    drawRoundRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x + 10f * scale, plantY - 15f * scale),
        size = Size(14f * scale, 18f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale),
        style = Stroke(width = 2.5f * scale)
    )

    for (i in 0..6) {
        val spikeY = plantY - 30f * scale + (i * 5.5f * scale)
        drawLine(
            color = Color(0xFF2C2C2C),
            start = Offset(x - 7f * scale, spikeY),
            end = Offset(x - 13f * scale, spikeY),
            strokeWidth = 1.5f * scale,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF2C2C2C),
            start = Offset(x + 7f * scale, spikeY),
            end = Offset(x + 13f * scale, spikeY),
            strokeWidth = 1.5f * scale,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawFlowerInPot(x: Float, y: Float, scale: Float, color: Color) {
    val soilY = y

    drawLine(
        color = Color(0xFF4CAF50),
        start = Offset(x, soilY + 2f * scale),
        end = Offset(x, soilY - 30f * scale),
        strokeWidth = 3f * scale,
        cap = StrokeCap.Round
    )

    drawLine(
        color = Color(0xFF2C2C2C),
        start = Offset(x - 1.5f * scale, soilY),
        end = Offset(x - 1.5f * scale, soilY - 30f * scale),
        strokeWidth = 0.5f * scale
    )
    drawLine(
        color = Color(0xFF2C2C2C),
        start = Offset(x + 1.5f * scale, soilY),
        end = Offset(x + 1.5f * scale, soilY - 30f * scale),
        strokeWidth = 0.5f * scale
    )

    val flowerY = soilY - 30f * scale
    for (i in 0..4) {
        val angle = (i * 72f - 90f) * Math.PI.toFloat() / 180f
        val petalX = x + kotlin.math.cos(angle) * 11f * scale
        val petalY = flowerY + kotlin.math.sin(angle) * 11f * scale
        
        drawCircle(
            color = color,
            radius = 8f * scale,
            center = Offset(petalX, petalY)
        )
        drawCircle(
            color = Color(0xFF2C2C2C),
            radius = 8f * scale,
            center = Offset(petalX, petalY),
            style = Stroke(width = 2f * scale)
        )
    }

    drawCircle(
        color = Color(0xFFFFEB3B),
        radius = 7f * scale,
        center = Offset(x, flowerY)
    )
    drawCircle(
        color = Color(0xFF2C2C2C),
        radius = 7f * scale,
        center = Offset(x, flowerY),
        style = Stroke(width = 2f * scale)
    )
}

private fun DrawScope.drawFernInPot(x: Float, y: Float, scale: Float, color: Color) {
    val soilY = y

    for (frond in -1..1) {
        val frondX = x + frond * 10f * scale

        drawLine(
            color = color.copy(alpha = 0.8f),
            start = Offset(frondX, soilY + 2f * scale),
            end = Offset(frondX - frond * 4f * scale, soilY - 32f * scale),
            strokeWidth = 2f * scale
        )

        for (i in 0..4) {
            val leafY = soilY - 5f * scale - (i * 6f * scale)
            val leafSize = (5 - i) * 1.8f * scale

            drawCircle(
                color = color,
                radius = leafSize,
                center = Offset(frondX - 5f * scale, leafY)
            )

            drawCircle(
                color = color,
                radius = leafSize,
                center = Offset(frondX + 5f * scale, leafY)
            )
        }
    }
}

private fun DrawScope.drawTreeInPot(x: Float, y: Float, scale: Float, color: Color) {
    val soilY = y

    drawRect(
        color = Color(0xFF8B4513),
        topLeft = Offset(x - 4f * scale, soilY - 18f * scale),
        size = Size(8f * scale, 20f * scale)
    )

    drawRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - 4f * scale, soilY - 18f * scale),
        size = Size(8f * scale, 20f * scale),
        style = Stroke(width = 2f * scale)
    )

    val canopyY = soilY - 25f * scale
    drawCircle(
        color = color,
        radius = 14f * scale,
        center = Offset(x, canopyY)
    )
    drawCircle(
        color = color.copy(alpha = 0.9f),
        radius = 11f * scale,
        center = Offset(x - 11f * scale, canopyY + 5f * scale)
    )
    drawCircle(
        color = color.copy(alpha = 0.9f),
        radius = 11f * scale,
        center = Offset(x + 11f * scale, canopyY + 5f * scale)
    )

    drawCircle(
        color = Color(0xFF2C2C2C),
        radius = 14f * scale,
        center = Offset(x, canopyY),
        style = Stroke(width = 2f * scale)
    )
}

private fun DrawScope.drawHerbInPot(x: Float, y: Float, scale: Float, color: Color) {
    val soilY = y

    for (i in -1..1) {
        val stemX = x + i * 9f * scale

        drawLine(
            color = color.copy(alpha = 0.7f),
            start = Offset(stemX, soilY + 2f * scale),
            end = Offset(stemX, soilY - 28f * scale),
            strokeWidth = 2f * scale
        )

        for (j in 0..3) {
            val leafY = soilY - 5f * scale - j * 6.5f * scale
            
            drawCircle(
                color = color,
                radius = 4f * scale,
                center = Offset(stemX - 5f * scale, leafY)
            )
            drawCircle(
                color = color,
                radius = 4f * scale,
                center = Offset(stemX + 5f * scale, leafY)
            )
        }
    }
}

private fun DrawScope.drawGenericPlantInPot(x: Float, y: Float, scale: Float, color: Color) {
    val plantY = y - 8f * scale

    drawCircle(
        color = color,
        radius = 16f * scale,
        center = Offset(x, plantY - 8f * scale)
    )

    drawCircle(
        color = color.copy(alpha = 0.85f),
        radius = 13f * scale,
        center = Offset(x - 14f * scale, plantY)
    )
    drawCircle(
        color = color.copy(alpha = 0.85f),
        radius = 13f * scale,
        center = Offset(x + 14f * scale, plantY)
    )

    drawCircle(
        color = Color(0xFF2C2C2C),
        radius = 16f * scale,
        center = Offset(x, plantY - 8f * scale),
        style = Stroke(width = 2.5f * scale)
    )
    drawCircle(
        color = Color(0xFF2C2C2C),
        radius = 13f * scale,
        center = Offset(x - 14f * scale, plantY),
        style = Stroke(width = 2.5f * scale)
    )
    drawCircle(
        color = Color(0xFF2C2C2C),
        radius = 13f * scale,
        center = Offset(x + 14f * scale, plantY),
        style = Stroke(width = 2.5f * scale)
    )
}

private fun parseColorString(colorString: String): Color {
    return when (colorString.lowercase()) {
        "green" -> Color(0xFF4CAF50)
        "dark_green" -> Color(0xFF2E7D32)
        "light_green" -> Color(0xFF8BC34A)
        "blue" -> Color(0xFF64B5F6)
        "purple" -> Color(0xFF9C27B0)
        "pink" -> Color(0xFFE91E63)
        "red" -> Color(0xFFF44336)
        "orange" -> Color(0xFFFF9800)
        "yellow" -> Color(0xFFFFEB3B)
        "brown" -> Color(0xFF795548)
        else -> Color(0xFF4CAF50)
    }
}
