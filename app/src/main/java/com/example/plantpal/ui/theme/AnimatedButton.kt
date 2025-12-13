package com.example.plantpal.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantpal.ui.theme.ForestPrimary
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.background



@Composable
fun EntryButton(
    text: String = "Login",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "borderAnimation"
    )

    Box(
        modifier = Modifier
            .width(220.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {

        // 🌫 Subtle shaded background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = ForestPrimary.copy(
                        alpha = if (isPressed) 0.10f else 0.06f
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val inset = strokeWidth / 2f
                val radius = size.height / 2f

                val borderPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                inset,
                                inset,
                                size.width - inset,
                                size.height - inset
                            ),
                            cornerRadius = CornerRadius(radius, radius)
                        )
                    )
                }

                val pm = PathMeasure()
                pm.setPath(borderPath, false)

                val halfLength = pm.length / 2f
                val drawLength = halfLength * progress

                // Path from top-left → forward
                val forwardPath = Path()
                pm.getSegment(
                    startDistance = 0f,
                    stopDistance = drawLength,
                    destination = forwardPath,
                    startWithMoveTo = true
                )

                // Path from bottom-right → backward
                val backwardPath = Path()
                pm.getSegment(
                    startDistance = halfLength,
                    stopDistance = halfLength + drawLength,
                    destination = backwardPath,
                    startWithMoveTo = true
                )

                drawPath(
                    path = forwardPath,
                    color = ForestPrimary,
                    style = Stroke(width = strokeWidth)
                )

                drawPath(
                    path = backwardPath,
                    color = ForestPrimary,
                    style = Stroke(width = strokeWidth)
                )
            }



            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center),
                color = ForestPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

        }
    }
    }
