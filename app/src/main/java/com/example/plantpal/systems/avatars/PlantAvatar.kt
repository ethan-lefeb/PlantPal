package com.example.plantpal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.AvatarConfig
import com.example.plantpal.com.example.plantpal.systems.avatars.com.example.plantpal.systems.avatars.AnimationParameters
import com.example.plantpal.com.example.plantpal.systems.avatars.com.example.plantpal.systems.avatars.AvatarAnimationController
import com.example.plantpal.com.example.plantpal.systems.avatars.com.example.plantpal.systems.avatars.rememberAvatarAnimationController
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun PlantAvatar(
    avatarConfig: AvatarConfig,
    health: String = "healthy",
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animated: Boolean = true,
    animationController: AvatarAnimationController? = null
) {
    val baseColor = parseColorString(avatarConfig.color)
    val potColor = parsePotColorString(avatarConfig.potColor)
    
    val controller = animationController ?: if (animated) {
        rememberAvatarAnimationController(health = health, daysSinceWatering = 0)
    } else null
    
    val animParams = controller?.calculateParameters() ?: AnimationParameters()
    var blinkProgress by remember { mutableStateOf(0f) }
    
    if (animated) {
        LaunchedEffect(Unit) {
            while (true) {
                blinkProgress = (blinkProgress + 0.05f) % (2f * PI.toFloat())
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

            drawPot(centerX, centerY, scale, potColor, avatarConfig.potStyle)
            drawFaceOnPot(centerX, centerY + 20f * scale, scale, health, blinkProgress)

            translate(animParams.horizontalOffset, totalVerticalOffset) {
                rotate(degrees = animParams.rotation, pivot = Offset(centerX, centerY)) {
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
                        //Cacti
                        "cactus_round", "cactus" -> drawCactusRound(centerX, centerY, scale, adjustedColor)
                        "cactus_trailing" -> drawCactusTrailing(centerX, centerY, scale, adjustedColor)
                        
                        //Succulents
                        "succulent_rosette", "succulent" -> drawSucculentRosette(centerX, centerY, scale, adjustedColor)
                        "succulent_jade" -> drawSucculentJade(centerX, centerY, scale, adjustedColor)
                        "succulent_string" -> drawSucculentString(centerX, centerY, scale, adjustedColor)
                        "succulent_aloe" -> drawSucculentAloe(centerX, centerY, scale, adjustedColor)
                        
                        //Common
                        "snake_plant" -> drawSnakePlant(centerX, centerY, scale, adjustedColor)
                        "pothos" -> drawPothos(centerX, centerY, scale, adjustedColor)
                        "philodendron_heart" -> drawPhilodendronHeart(centerX, centerY, scale, adjustedColor)
                        "monstera" -> drawMonstera(centerX, centerY, scale, adjustedColor)
                        
                        //Ferns
                        "fern_boston", "fern" -> drawFernBoston(centerX, centerY, scale, adjustedColor)
                        "fern_maidenhair" -> drawFernMaidenhair(centerX, centerY, scale, adjustedColor)
                        "fern_birds_nest" -> drawFernBirdsNest(centerX, centerY, scale, adjustedColor)
                        //Other
                        "prayer_plant" -> drawPrayerPlant(centerX, centerY, scale, adjustedColor)
                        "spider_plant" -> drawSpiderPlant(centerX, centerY, scale, adjustedColor)
                        "peace_lily" -> drawPeaceLily(centerX, centerY, scale, adjustedColor)
                        "zz_plant" -> drawZZPlant(centerX, centerY, scale, adjustedColor)
                        "rubber_plant" -> drawRubberPlant(centerX, centerY, scale, adjustedColor)
                        "fiddle_leaf" -> drawFiddleLeaf(centerX, centerY, scale, adjustedColor)
                        "dracaena" -> drawDracaena(centerX, centerY, scale, adjustedColor)
                        "palm" -> drawPalm(centerX, centerY, scale, adjustedColor)
                        
                        //Flower
                        "orchid", "flower" -> drawOrchid(centerX, centerY, scale, adjustedColor)
                        "african_violet" -> drawAfricanViolet(centerX, centerY, scale, adjustedColor)
                        
                        //Small
                        "peperomia" -> drawPeperomia(centerX, centerY, scale, adjustedColor)
                        "pilea" -> drawPilea(centerX, centerY, scale, adjustedColor)
                        
                        //Herbs & Tree
                        "herb" -> drawHerb(centerX, centerY, scale, adjustedColor)
                        "tree" -> drawTree(centerX, centerY, scale, adjustedColor)
                        
                        //Generic
                        else -> drawGenericPlant(centerX, centerY, scale, adjustedColor)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPot(x: Float, y: Float, scale: Float, potColor: Color, potStyle: String = "classic") {
    val potY = y + 5f * scale
    
    when (potStyle.lowercase()) {
        "classic" -> drawClassicPot(x, potY, scale, potColor)
        "modern" -> drawModernPot(x, potY, scale, potColor)
        "hanging" -> drawHangingPot(x, potY, scale, potColor)
        else -> drawClassicPot(x, potY, scale, potColor)
    }
}

private fun DrawScope.drawClassicPot(x: Float, potY: Float, scale: Float, potColor: Color) {
    val rimHeight = 6f * scale
    
    drawRect(
        color = potColor.copy(red = potColor.red * 0.8f, green = potColor.green * 0.8f, blue = potColor.blue * 0.8f),
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
    
    drawPath(path = potPath, color = potColor)
    drawPath(path = potPath, color = Color(0xFF2C2C2C), style = Stroke(width = 3.5f * scale))
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
}

private fun DrawScope.drawModernPot(x: Float, potY: Float, scale: Float, potColor: Color) {
    val potHeight = 45f * scale
    val potWidth = 70f * scale
    
    drawRoundRect(
        color = potColor,
        topLeft = Offset(x - potWidth / 2, potY),
        size = Size(potWidth, potHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale)
    )
    
    drawRoundRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - potWidth / 2, potY),
        size = Size(potWidth, potHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale),
        style = Stroke(width = 3f * scale)
    )
    
    drawOval(
        color = Color(0xFF6B4423),
        topLeft = Offset(x - potWidth / 2 + 2f * scale, potY - 2f * scale),
        size = Size(potWidth - 4f * scale, 8f * scale)
    )
}

private fun DrawScope.drawDecorativePot(x: Float, potY: Float, scale: Float, potColor: Color) {
    val rimHeight = 8f * scale
    
    drawRect(
        color = potColor.copy(red = potColor.red * 0.9f, green = potColor.green * 0.9f, blue = potColor.blue * 0.9f),
        topLeft = Offset(x - 48f * scale, potY - rimHeight),
        size = Size(96f * scale, rimHeight)
    )

    val potPath = Path().apply {
        moveTo(x - 45f * scale, potY)
        cubicTo(
            x - 38f * scale, potY + 15f * scale,
            x - 35f * scale, potY + 30f * scale,
            x - 30f * scale, potY + 40f * scale
        )
        lineTo(x + 30f * scale, potY + 40f * scale)
        cubicTo(
            x + 35f * scale, potY + 30f * scale,
            x + 38f * scale, potY + 15f * scale,
            x + 45f * scale, potY
        )
        close()
    }
    
    drawPath(path = potPath, color = potColor)
    drawPath(path = potPath, color = Color(0xFF2C2C2C), style = Stroke(width = 3.5f * scale))
    
    for (i in 0..2) {
        val patternY = potY + 12f * scale + (i * 12f * scale)
        drawLine(
            color = Color(0xFF2C2C2C).copy(alpha = 0.3f),
            start = Offset(x - 35f * scale, patternY),
            end = Offset(x + 35f * scale, patternY),
            strokeWidth = 2f * scale
        )
    }
    
    drawRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - 48f * scale, potY - rimHeight),
        size = Size(96f * scale, rimHeight),
        style = Stroke(width = 3.5f * scale)
    )
    
    drawOval(
        color = Color(0xFF6B4423),
        topLeft = Offset(x - 45f * scale, potY - 3f * scale),
        size = Size(90f * scale, 10f * scale)
    )
}

private fun DrawScope.drawHangingPot(x: Float, potY: Float, scale: Float, potColor: Color) {
    drawLine(
        color = Color(0xFF8B4513),
        start = Offset(x, potY - 20f * scale),
        end = Offset(x, potY - 8f * scale),
        strokeWidth = 2f * scale
    )
    
    val potRadius = 35f * scale
    
    drawCircle(
        color = potColor,
        radius = potRadius,
        center = Offset(x, potY + 20f * scale)
    )
    
    drawCircle(
        color = Color(0xFF2C2C2C),
        radius = potRadius,
        center = Offset(x, potY + 20f * scale),
        style = Stroke(width = 3f * scale)
    )
    
    drawOval(
        color = Color(0xFF6B4423),
        topLeft = Offset(x - 30f * scale, potY - 5f * scale),
        size = Size(60f * scale, 12f * scale)
    )
}

private fun DrawScope.drawFaceOnPot(x: Float, y: Float, scale: Float, health: String, animProgress: Float) {
    val eyeY = y
    val eyeSize = 7f * scale
    val blinkProgress = sin(animProgress * 2)
    val eyeHeight = if (blinkProgress > 0.9f) eyeSize * 0.3f else eyeSize

    drawOval(color = Color(0xFF2C2C2C), topLeft = Offset(x - 16f * scale, eyeY - eyeHeight / 2), size = Size(eyeSize, eyeHeight))
    drawOval(color = Color(0xFF2C2C2C), topLeft = Offset(x + 9f * scale, eyeY - eyeHeight / 2), size = Size(eyeSize, eyeHeight))

    val mouthY = y + 14f * scale
    when (health) {
        "healthy" -> {
            val path = Path().apply {
                moveTo(x - 12f * scale, mouthY)
                quadraticTo(x, mouthY + 10f * scale, x + 12f * scale, mouthY)
            }
            drawPath(path, Color(0xFF2C2C2C), style = Stroke(3f * scale, cap = StrokeCap.Round))
        }
        "warning" -> drawLine(Color(0xFF2C2C2C), Offset(x - 12f * scale, mouthY), Offset(x + 12f * scale, mouthY), 3f * scale, cap = StrokeCap.Round)
        else -> {
            val path = Path().apply {
                moveTo(x - 12f * scale, mouthY + 6f * scale)
                quadraticTo(x, mouthY - 4f * scale, x + 12f * scale, mouthY + 6f * scale)
            }
            drawPath(path, Color(0xFF2C2C2C), style = Stroke(3f * scale, cap = StrokeCap.Round))
        }
    }

    if (health == "healthy") {
        drawCircle(Color(0xFFFFB6C1).copy(alpha = 0.6f), 6f * scale, Offset(x - 24f * scale, y + 7f * scale))
        drawCircle(Color(0xFFFFB6C1).copy(alpha = 0.6f), 6f * scale, Offset(x + 24f * scale, y + 7f * scale))
    }
}

private fun DrawScope.drawCactusRound(x: Float, y: Float, scale: Float, color: Color) {
    val plantY = y
    drawRoundRect(
        color = color,
        topLeft = Offset(x - 11f * scale, plantY - 35f * scale),
        size = Size(22f * scale, 40f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale)
    )
    drawRoundRect(
        color = Color(0xFF2C2C2C),
        topLeft = Offset(x - 11f * scale, plantY - 35f * scale),
        size = Size(22f * scale, 40f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale),
        style = Stroke(2.5f * scale)
    )

    for (i in 0..6) {
        val spikeY = plantY - 30f * scale + (i * 5.5f * scale)
        drawLine(Color(0xFF2C2C2C), Offset(x - 7f * scale, spikeY), Offset(x - 13f * scale, spikeY), 1.5f * scale)
        drawLine(Color(0xFF2C2C2C), Offset(x + 7f * scale, spikeY), Offset(x + 13f * scale, spikeY), 1.5f * scale)
    }
}

private fun DrawScope.drawCactusTrailing(x: Float, y: Float, scale: Float, color: Color) {
    val segments = listOf(
        Offset(x, y - 25f * scale),
        Offset(x - 8f * scale, y - 15f * scale),
        Offset(x - 15f * scale, y - 5f * scale),
        Offset(x + 8f * scale, y - 15f * scale),
        Offset(x + 15f * scale, y - 5f * scale)
    )
    
    segments.forEach { pos ->
        drawRoundRect(
            color = color,
            topLeft = Offset(pos.x - 4f * scale, pos.y - 6f * scale),
            size = Size(8f * scale, 12f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale)
        )
    }
}

private fun DrawScope.drawSucculentRosette(x: Float, y: Float, scale: Float, color: Color) {
    val plantY = y - 5f * scale
    for (i in 0..7) {
        val angle = (i * 45f) * PI.toFloat() / 180f
        val leafX = x + cos(angle) * 15f * scale
        val leafY = plantY + sin(angle) * 15f * scale
        drawCircle(color.copy(alpha = 0.85f), 9f * scale, Offset(leafX, leafY))
        drawCircle(Color(0xFF2C2C2C), 9f * scale, Offset(leafX, leafY), style = Stroke(2.5f * scale))
    }
    drawCircle(color, 7f * scale, Offset(x, plantY))
    drawCircle(Color(0xFF2C2C2C), 7f * scale, Offset(x, plantY), style = Stroke(2.5f * scale))
}

private fun DrawScope.drawSucculentJade(x: Float, y: Float, scale: Float, color: Color) {
    val trunkY = y
    drawRect(color = Color(0xFF8B4513), topLeft = Offset(x - 3f * scale, trunkY - 15f * scale), size = Size(6f * scale, 20f * scale))

    val leaves = listOf(
        Pair(Offset(x - 8f * scale, trunkY - 25f * scale), Offset(x + 8f * scale, trunkY - 25f * scale)),
        Pair(Offset(x - 10f * scale, trunkY - 15f * scale), Offset(x + 10f * scale, trunkY - 15f * scale))
    )
    
    leaves.forEach { pair ->
        drawCircle(color, 5f * scale, pair.first)
        drawCircle(color, 5f * scale, pair.second)
    }
}

private fun DrawScope.drawSucculentString(x: Float, y: Float, scale: Float, color: Color) {
    val pearls = listOf(
        Offset(x, y - 25f * scale),
        Offset(x - 5f * scale, y - 18f * scale),
        Offset(x, y - 11f * scale),
        Offset(x + 5f * scale, y - 4f * scale)
    )
    
    pearls.forEach { pos ->
        drawCircle(color, 4f * scale, pos)
    }

    drawLine(color.copy(alpha = 0.6f), Offset(x, y - 25f * scale), Offset(x + 5f * scale, y), 1.5f * scale)
}

private fun DrawScope.drawSucculentAloe(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(-12f, -6f, 0f, 6f, 12f)
    
    leaves.forEach { offsetX ->
        val path = Path().apply {
            moveTo(x + offsetX * scale, y)
            lineTo(x + offsetX * scale - 3f * scale, y - 30f * scale)
            lineTo(x + offsetX * scale, y - 35f * scale)
            lineTo(x + offsetX * scale + 3f * scale, y - 30f * scale)
            close()
        }
        drawPath(path, color)
        drawPath(path, Color(0xFF2C2C2C), style = Stroke(2f * scale))
    }
}

private fun DrawScope.drawSnakePlant(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(-10f, -3f, 4f, 11f)
    
    leaves.forEach { offsetX ->
        val height = (35f + (offsetX * 0.3f)) * scale
        drawRoundRect(
            color = color,
            topLeft = Offset(x + offsetX * scale - 2f * scale, y - height),
            size = Size(4f * scale, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale)
        )

        for (i in 1..4) {
            val stripeY = y - height + (i * height / 5)
            drawLine(
                color = Color(0xFF2C2C2C).copy(alpha = 0.3f),
                start = Offset(x + offsetX * scale - 2f * scale, stripeY),
                end = Offset(x + offsetX * scale + 2f * scale, stripeY),
                strokeWidth = 0.5f * scale
            )
        }
    }
}

private fun DrawScope.drawPothos(x: Float, y: Float, scale: Float, color: Color) {
    // Main trailing vine
    val vinePath = Path().apply {
        moveTo(x, y)
        cubicTo(
            x - 5f * scale, y - 8f * scale,
            x - 8f * scale, y - 15f * scale,
            x - 10f * scale, y - 22f * scale
        )
    }
    drawPath(vinePath, color.copy(alpha = 0.6f), style = Stroke(2f * scale))

    val leaves = listOf(
        Pair(Offset(x, y - 5f * scale), 7f),
        Pair(Offset(x - 6f * scale, y - 12f * scale), 6f),
        Pair(Offset(x - 10f * scale, y - 22f * scale), 5f),
        Pair(Offset(x + 5f * scale, y - 8f * scale), 6f),
        Pair(Offset(x + 8f * scale, y - 16f * scale), 5f)
    )

    leaves.forEach { (pos, size) ->
        val path = Path().apply {
            moveTo(pos.x, pos.y + size * scale)
            cubicTo(
                pos.x - size * scale, pos.y,
                pos.x - size * scale, pos.y - size * 0.8f * scale,
                pos.x, pos.y - size * 0.5f * scale
            )
            cubicTo(
                pos.x + size * scale, pos.y - size * 0.8f * scale,
                pos.x + size * scale, pos.y,
                pos.x, pos.y + size * scale
            )
        }
        drawPath(path, color)

        drawCircle(
            Color(0xFFFFEB3B).copy(alpha = 0.4f),
            size * 0.3f * scale,
            Offset(pos.x - size * 0.2f * scale, pos.y)
        )
        drawCircle(
            Color(0xFFFFEB3B).copy(alpha = 0.3f),
            size * 0.2f * scale,
            Offset(pos.x + size * 0.3f * scale, pos.y + size * 0.2f * scale)
        )

        drawPath(path, Color(0xFF2C2C2C), style = Stroke(1.5f * scale))
    }
}

private fun DrawScope.drawPhilodendronHeart(x: Float, y: Float, scale: Float, color: Color) {
    drawLine(
        Color(0xFF8B4513).copy(alpha = 0.7f),
        Offset(x, y),
        Offset(x, y - 25f * scale),
        3f * scale
    )

    val leaves = listOf(
        Offset(x - 12f * scale, y - 18f * scale),
        Offset(x + 12f * scale, y - 10f * scale),
        Offset(x, y - 25f * scale)
    )

    leaves.forEach { pos ->
        val path = Path().apply {
            moveTo(pos.x, pos.y + 10f * scale)
            cubicTo(
                pos.x - 8f * scale, pos.y + 6f * scale,
                pos.x - 8f * scale, pos.y - 2f * scale,
                pos.x - 4f * scale, pos.y - 6f * scale
            )
            cubicTo(
                pos.x - 2f * scale, pos.y - 8f * scale,
                pos.x, pos.y - 8f * scale,
                pos.x, pos.y - 6f * scale
            )
            cubicTo(
                pos.x, pos.y - 8f * scale,
                pos.x + 2f * scale, pos.y - 8f * scale,
                pos.x + 4f * scale, pos.y - 6f * scale
            )
            cubicTo(
                pos.x + 8f * scale, pos.y - 2f * scale,
                pos.x + 8f * scale, pos.y + 6f * scale,
                pos.x, pos.y + 10f * scale
            )
        }

        drawPath(path, color.copy(red = color.red * 0.9f, green = color.green * 0.95f))

        drawLine(
            Color(0xFF2C2C2C).copy(alpha = 0.4f),
            Offset(pos.x, pos.y - 6f * scale),
            Offset(pos.x, pos.y + 8f * scale),
            1.5f * scale
        )

        drawOval(
            Color.White.copy(alpha = 0.2f),
            topLeft = Offset(pos.x - 3f * scale, pos.y - 3f * scale),
            size = Size(4f * scale, 6f * scale)
        )

        drawPath(path, Color(0xFF2C2C2C), style = Stroke(1.5f * scale))
    }
}

private fun DrawScope.drawMonstera(x: Float, y: Float, scale: Float, color: Color) {
    val stem = y - 10f * scale
    drawLine(color.copy(alpha = 0.8f), Offset(x, y), Offset(x, stem), 2f * scale)

    val leaves = listOf(
        Offset(x, stem - 15f * scale),
        Offset(x - 12f * scale, stem - 5f * scale),
        Offset(x + 12f * scale, stem - 5f * scale)
    )

    leaves.forEach { pos ->
        drawCircle(color, 10f * scale, pos)

        for (i in -1..1) {
            drawLine(
                Color(0xFFFFF8DC),
                Offset(pos.x + i * 4f * scale, pos.y - 6f * scale),
                Offset(pos.x + i * 5f * scale, pos.y + 8f * scale),
                2f * scale
            )
        }

        drawCircle(Color(0xFF2C2C2C), 10f * scale, pos, style = Stroke(2f * scale))
    }
}

private fun DrawScope.drawFernBoston(x: Float, y: Float, scale: Float, color: Color) {
    for (frond in -2..2) {
        val frondX = x + frond * 8f * scale
        val curve = frond * 3f * scale
        
        drawLine(
            color.copy(alpha = 0.8f),
            Offset(frondX, y),
            Offset(frondX - curve, y - 30f * scale),
            2f * scale
        )

        for (i in 0..5) {
            val leafY = y - 5f * scale - (i * 5f * scale)
            val leafSize = (6 - i) * 1.5f * scale
            drawCircle(color, leafSize, Offset(frondX - 4f * scale - curve * i / 5, leafY))
            drawCircle(color, leafSize, Offset(frondX + 4f * scale - curve * i / 5, leafY))
        }
    }
}

private fun DrawScope.drawFernMaidenhair(x: Float, y: Float, scale: Float, color: Color) {
    val stems = listOf(-12f, -4f, 4f, 12f)
    
    stems.forEach { offsetX ->
        drawLine(
            color.copy(alpha = 0.4f),
            Offset(x + offsetX * scale, y),
            Offset(x + offsetX * scale, y - 25f * scale),
            1f * scale
        )

        for (i in 0..6) {
            val leafY = y - 5f * scale - (i * 3.5f * scale)
            drawCircle(color, 2f * scale, Offset(x + offsetX * scale - 3f * scale, leafY))
            drawCircle(color, 2f * scale, Offset(x + offsetX * scale + 3f * scale, leafY))
        }
    }
}

private fun DrawScope.drawFernBirdsNest(x: Float, y: Float, scale: Float, color: Color) {
    for (i in 0..7) {
        val angle = (i * 45f) * PI.toFloat() / 180f
        val endX = x + cos(angle) * 20f * scale
        val endY = y - 15f * scale + sin(angle) * 15f * scale
        
        val path = Path().apply {
            moveTo(x, y - 5f * scale)
            quadraticTo(
                x + cos(angle) * 10f * scale,
                y - 10f * scale + sin(angle) * 7f * scale,
                endX,
                endY
            )
        }
        
        drawPath(path, color, style = Stroke(3f * scale, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawPrayerPlant(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(
        Offset(x - 10f * scale, y - 20f * scale),
        Offset(x + 10f * scale, y - 20f * scale),
        Offset(x, y - 10f * scale)
    )
    
    leaves.forEach { pos ->
        drawOval(
            color = color,
            topLeft = Offset(pos.x - 7f * scale, pos.y - 5f * scale),
            size = Size(14f * scale, 10f * scale)
        )

        for (i in 1..3) {
            drawLine(
                Color(0xFF2C2C2C).copy(alpha = 0.4f),
                Offset(pos.x - 5f * scale, pos.y - 3f * scale + i * 2f * scale),
                Offset(pos.x + 5f * scale, pos.y - 3f * scale + i * 2f * scale),
                1f * scale
            )
        }
    }
}

private fun DrawScope.drawSpiderPlant(x: Float, y: Float, scale: Float, color: Color) {
    for (i in -3..3) {
        val leafX = x + i * 3.5f * scale
        val curve = i * 2f * scale
        
        val path = Path().apply {
            moveTo(leafX, y)
            quadraticTo(
                leafX - curve,
                y - 20f * scale,
                leafX - curve * 1.5f,
                y - 35f * scale
            )
        }
        
        drawPath(path, color, style = Stroke(2f * scale, cap = StrokeCap.Round))

        if (i % 2 == 0) {
            drawPath(path, Color.White.copy(alpha = 0.5f), style = Stroke(1f * scale))
        }
    }
}

private fun DrawScope.drawPeaceLily(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(
        Offset(x - 12f * scale, y - 15f * scale),
        Offset(x + 12f * scale, y - 15f * scale),
        Offset(x - 6f * scale, y - 8f * scale),
        Offset(x + 6f * scale, y - 8f * scale)
    )
    
    leaves.forEach { pos ->
        drawOval(
            color = color,
            topLeft = Offset(pos.x - 6f * scale, pos.y - 8f * scale),
            size = Size(12f * scale, 16f * scale)
        )
    }

    val path = Path().apply {
        moveTo(x, y - 30f * scale)
        quadraticTo(
            x - 8f * scale,
            y - 25f * scale,
            x - 5f * scale,
            y - 18f * scale
        )
        quadraticTo(
            x,
            y - 22f * scale,
            x + 5f * scale,
            y - 18f * scale
        )
        quadraticTo(
            x + 8f * scale,
            y - 25f * scale,
            x,
            y - 30f * scale
        )
    }
    drawPath(path, Color.White)
    drawPath(path, Color(0xFF2C2C2C), style = Stroke(1.5f * scale))
}

private fun DrawScope.drawZZPlant(x: Float, y: Float, scale: Float, color: Color) {
    val stems = listOf(-10f, 0f, 10f)
    
    stems.forEach { offsetX ->
        for (i in 0..4) {
            val leafY = y - 5f * scale - (i * 6f * scale)
            drawOval(
                color = color,
                topLeft = Offset(x + offsetX * scale - 4f * scale, leafY - 3f * scale),
                size = Size(8f * scale, 6f * scale)
            )

            drawOval(
                Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x + offsetX * scale - 3f * scale, leafY - 2f * scale),
                size = Size(3f * scale, 2f * scale)
            )
        }
    }
}

private fun DrawScope.drawRubberPlant(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(
        Offset(x, y - 25f * scale),
        Offset(x - 10f * scale, y - 15f * scale),
        Offset(x + 10f * scale, y - 15f * scale)
    )
    
    leaves.forEach { pos ->
        drawOval(
            color = color,
            topLeft = Offset(pos.x - 8f * scale, pos.y - 6f * scale),
            size = Size(16f * scale, 12f * scale)
        )

        drawLine(
            Color(0xFF2C2C2C).copy(alpha = 0.3f),
            Offset(pos.x, pos.y - 5f * scale),
            Offset(pos.x, pos.y + 5f * scale),
            1f * scale
        )
    }
}

private fun DrawScope.drawFiddleLeaf(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(
        Offset(x, y - 20f * scale),
        Offset(x - 12f * scale, y - 10f * scale),
        Offset(x + 12f * scale, y - 10f * scale)
    )
    
    leaves.forEach { pos ->
        val path = Path().apply {
            moveTo(pos.x, pos.y + 8f * scale)
            quadraticTo(pos.x - 5f * scale, pos.y + 3f * scale, pos.x - 7f * scale, pos.y - 2f * scale)
            quadraticTo(pos.x - 5f * scale, pos.y - 8f * scale, pos.x, pos.y - 10f * scale)
            quadraticTo(pos.x + 5f * scale, pos.y - 8f * scale, pos.x + 7f * scale, pos.y - 2f * scale)
            quadraticTo(pos.x + 5f * scale, pos.y + 3f * scale, pos.x, pos.y + 8f * scale)
        }
        drawPath(path, color)
        drawPath(path, Color(0xFF2C2C2C), style = Stroke(1.5f * scale))
    }
}

private fun DrawScope.drawDracaena(x: Float, y: Float, scale: Float, color: Color) {
    for (i in 0..11) {
        val angle = (i * 30f) * PI.toFloat() / 180f
        val length = (25f + (i % 3) * 5f) * scale
        val endX = x + cos(angle) * 10f * scale
        val endY = y - length + sin(angle) * 5f * scale
        
        drawLine(
            color,
            Offset(x, y),
            Offset(endX, endY),
            2f * scale,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPalm(x: Float, y: Float, scale: Float, color: Color) {
    drawLine(color = Color(0xFF8B4513), Offset(x, y), Offset(x, y - 20f * scale), 3f * scale)
    
    for (i in 0..4) {
        val angle = (-60f + i * 30f) * PI.toFloat() / 180f
        val endX = x + cos(angle) * 18f * scale
        val endY = y - 20f * scale + sin(angle) * 12f * scale

        drawLine(color.copy(alpha = 0.8f), Offset(x, y - 20f * scale), Offset(endX, endY), 2f * scale)

        for (j in 1..5) {
            val leafPos = j / 5f
            val leafX = x + cos(angle) * 18f * scale * leafPos
            val leafY = y - 20f * scale + sin(angle) * 12f * scale * leafPos
            
            drawLine(color, Offset(leafX, leafY), Offset(leafX + cos(angle + PI.toFloat() / 2) * 3f * scale, leafY), 1f * scale)
            drawLine(color, Offset(leafX, leafY), Offset(leafX + cos(angle - PI.toFloat() / 2) * 3f * scale, leafY), 1f * scale)
        }
    }
}

private fun DrawScope.drawOrchid(x: Float, y: Float, scale: Float, color: Color) {
    drawLine(Color(0xFF4CAF50), Offset(x, y), Offset(x, y - 30f * scale), 2f * scale)
    
    val flowers = listOf(
        Offset(x, y - 25f * scale),
        Offset(x - 5f * scale, y - 18f * scale),
        Offset(x + 5f * scale, y - 18f * scale)
    )
    
    flowers.forEach { pos ->
        for (i in 0..4) {
            val angle = (i * 72f) * PI.toFloat() / 180f
            val petalX = pos.x + cos(angle) * 6f * scale
            val petalY = pos.y + sin(angle) * 6f * scale
            
            drawCircle(color, 4f * scale, Offset(petalX, petalY))
        }

        drawCircle(Color(0xFFFFEB3B), 3f * scale, pos)
    }
}

private fun DrawScope.drawAfricanViolet(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(
        Offset(x - 10f * scale, y - 8f * scale),
        Offset(x + 10f * scale, y - 8f * scale),
        Offset(x - 5f * scale, y - 2f * scale),
        Offset(x + 5f * scale, y - 2f * scale)
    )
    
    leaves.forEach { pos ->
        drawCircle(Color(0xFF4CAF50), 6f * scale, pos)
    }

    drawCircle(color, 5f * scale, Offset(x, y - 20f * scale))
    drawCircle(Color(0xFFFFEB3B), 2f * scale, Offset(x, y - 20f * scale))
}

private fun DrawScope.drawPeperomia(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(
        Offset(x, y - 20f * scale),
        Offset(x - 8f * scale, y - 12f * scale),
        Offset(x + 8f * scale, y - 12f * scale),
        Offset(x - 4f * scale, y - 5f * scale),
        Offset(x + 4f * scale, y - 5f * scale)
    )
    
    leaves.forEach { pos ->
        drawCircle(color, 5f * scale, pos)
        drawCircle(Color.White.copy(alpha = 0.2f), 2f * scale, Offset(pos.x - 1f * scale, pos.y - 1f * scale))
    }
}

private fun DrawScope.drawPilea(x: Float, y: Float, scale: Float, color: Color) {
    val leaves = listOf(
        Offset(x, y - 25f * scale),
        Offset(x - 10f * scale, y - 15f * scale),
        Offset(x + 10f * scale, y - 15f * scale),
        Offset(x - 5f * scale, y - 8f * scale),
        Offset(x + 5f * scale, y - 8f * scale)
    )
    
    leaves.forEach { pos ->
        drawLine(color.copy(alpha = 0.6f), Offset(x, y), pos, 1f * scale)
        drawCircle(color, 6f * scale, pos)
        drawCircle(Color(0xFF2C2C2C), 6f * scale, pos, style = Stroke(1f * scale))
    }
}

private fun DrawScope.drawHerb(x: Float, y: Float, scale: Float, color: Color) {
    val stems = listOf(-8f, 0f, 8f)
    stems.forEach { offsetX ->
        drawLine(
            color.copy(alpha = 0.7f),
            Offset(x + offsetX * scale, y),
            Offset(x + offsetX * scale, y - 28f * scale),
            2f * scale
        )
        for (j in 0..3) {
            val leafY = y - 5f * scale - j * 6.5f * scale
            drawCircle(color, 4f * scale, Offset(x + offsetX * scale - 5f * scale, leafY))
            drawCircle(color, 4f * scale, Offset(x + offsetX * scale + 5f * scale, leafY))
        }
    }
}

private fun DrawScope.drawTree(x: Float, y: Float, scale: Float, color: Color) {
    drawRect(Color(0xFF8B4513), topLeft = Offset(x - 4f * scale, y - 18f * scale), size = Size(8f * scale, 20f * scale))
    val canopyY = y - 25f * scale
    drawCircle(color, 14f * scale, Offset(x, canopyY))
    drawCircle(color.copy(alpha = 0.9f), 11f * scale, Offset(x - 11f * scale, canopyY + 5f * scale))
    drawCircle(color.copy(alpha = 0.9f), 11f * scale, Offset(x + 11f * scale, canopyY + 5f * scale))
}

private fun DrawScope.drawGenericPlant(x: Float, y: Float, scale: Float, color: Color) {
    val plantY = y - 8f * scale
    drawCircle(color, 16f * scale, Offset(x, plantY - 8f * scale))
    drawCircle(color.copy(alpha = 0.85f), 13f * scale, Offset(x - 14f * scale, plantY))
    drawCircle(color.copy(alpha = 0.85f), 13f * scale, Offset(x + 14f * scale, plantY))
    drawCircle(Color(0xFF2C2C2C), 16f * scale, Offset(x, plantY - 8f * scale), style = Stroke(2.5f * scale))
}

private fun parsePotColorString(colorString: String): Color {
    return when (colorString.lowercase()) {
        "terracotta" -> Color(0xFFD2691E)
        "ceramic_white" -> Color(0xFFF5F5F5)
        "ceramic_blue" -> Color(0xFF87CEEB)
        "ceramic_green" -> Color(0xFF90EE90)
        "modern_gray" -> Color(0xFF808080)
        "modern_black" -> Color(0xFF2C2C2C)
        "rustic_brown" -> Color(0xFF8B4513)
        "pink" -> Color(0xFFFFB6C1)
        "yellow" -> Color(0xFFFFD700)
        "purple" -> Color(0xFFDA70D6)
        else -> Color(0xFFD2691E)
    }
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
        "white" -> Color(0xFFF5F5F5)
        else -> Color(0xFF4CAF50)
    }
}
