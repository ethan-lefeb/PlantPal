package com.example.plantpal.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class UIScale(val label: String, val scaleFactor: Float) {
    SMALL("Small", 0.85f),
    NORMAL("Normal", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.30f);

    companion object {
        fun fromScaleFactor(factor: Float): UIScale {
            return entries.minByOrNull { 
                kotlin.math.abs(it.scaleFactor - factor) 
            } ?: NORMAL
        }
    }
}

data class ScaledSizes(
    val scaleFactor: Float,

    val displayLarge: TextUnit,
    val headlineLarge: TextUnit,
    val headlineMedium: TextUnit,
    val headlineSmall: TextUnit,
    val titleLarge: TextUnit,
    val titleMedium: TextUnit,
    val titleSmall: TextUnit,
    val bodyLarge: TextUnit,
    val bodyMedium: TextUnit,
    val bodySmall: TextUnit,
    val labelLarge: TextUnit,
    val labelMedium: TextUnit,
    val labelSmall: TextUnit,

    val spacingXSmall: Dp,
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp,
    val spacingXLarge: Dp,

    val iconSizeSmall: Dp,
    val iconSizeMedium: Dp,
    val iconSizeLarge: Dp,
    val avatarSizeSmall: Dp,
    val avatarSizeMedium: Dp,
    val avatarSizeLarge: Dp,
    val buttonHeight: Dp,
    val minTouchTarget: Dp,

    val paddingXSmall: Dp,
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val paddingXLarge: Dp
) {
    companion object {
        fun fromScale(scale: Float): ScaledSizes {
            return ScaledSizes(
                scaleFactor = scale,

                displayLarge = (32.sp.value * scale).sp,
                headlineLarge = (28.sp.value * scale).sp,
                headlineMedium = (24.sp.value * scale).sp,
                headlineSmall = (20.sp.value * scale).sp,
                titleLarge = (18.sp.value * scale).sp,
                titleMedium = (16.sp.value * scale).sp,
                titleSmall = (14.sp.value * scale).sp,
                bodyLarge = (16.sp.value * scale).sp,
                bodyMedium = (14.sp.value * scale).sp,
                bodySmall = (12.sp.value * scale).sp,
                labelLarge = (14.sp.value * scale).sp,
                labelMedium = (12.sp.value * scale).sp,
                labelSmall = (11.sp.value * scale).sp,
                spacingXSmall = (4.dp.value * scale).dp,
                spacingSmall = (8.dp.value * scale).dp,
                spacingMedium = (16.dp.value * scale).dp,
                spacingLarge = (24.dp.value * scale).dp,
                spacingXLarge = (32.dp.value * scale).dp,
                iconSizeSmall = (16.dp.value * scale).dp,
                iconSizeMedium = (24.dp.value * scale).dp,
                iconSizeLarge = (32.dp.value * scale).dp,
                avatarSizeSmall = (40.dp.value * scale).dp,
                avatarSizeMedium = (60.dp.value * scale).dp,
                avatarSizeLarge = (80.dp.value * scale).dp,
                buttonHeight = (48.dp.value * scale).dp,
                minTouchTarget = maxOf((48.dp.value * scale).dp, 48.dp),
                paddingXSmall = (4.dp.value * scale).dp,
                paddingSmall = (8.dp.value * scale).dp,
                paddingMedium = (16.dp.value * scale).dp,
                paddingLarge = (24.dp.value * scale).dp,
                paddingXLarge = (32.dp.value * scale).dp
            )
        }
    }
}

val LocalUIScale = compositionLocalOf { ScaledSizes.fromScale(1.0f) }

@Composable
fun UIScaleProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { ScalePreferences(context) }
    
    var currentScale by remember { 
        mutableStateOf(preferences.getScale()) 
    }
    
    val scaledSizes = remember(currentScale) {
        ScaledSizes.fromScale(currentScale)
    }

    CompositionLocalProvider(
        LocalUIScale provides scaledSizes,
        LocalScaleUpdater provides { newScale ->
            currentScale = newScale
            preferences.setScale(newScale)
        }
    ) {
        content()
    }
}

val LocalScaleUpdater = compositionLocalOf<(Float) -> Unit> { {} }

object ScaledDimensions {
    val current: ScaledSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalUIScale.current
}
