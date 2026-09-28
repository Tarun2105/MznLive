package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// Indian Tricolor Palette (Tiranga) - Saffron, White, India Green, Navy
// ============================================================================

// 1. Saffron (Kesari) - Courage, Energy & Warmth
val TricolorSaffron = Color(0xFFFF6F00) // Rich, authentic saffron
val TricolorSaffronDark = Color(0xFFD9480F) // Deep terracotta saffron
val TricolorSaffronLight = Color(0xFFFFF3E0) // Soft warm saffron tint
val TricolorSaffronBorder = Color(0xFFFFE0B2) // Subtle saffron boundary

// 2. White & Ivory (Shwet) - Peace, Purity & Truth
val TricolorWhite = Color(0xFFFFFFFF) // Crisp white
val TricolorIvory = Color(0xFFF9FAF8) // Decent, serene light background
val TricolorLightSurface = Color(0xFFFFFFFF)
val TricolorLightSurfaceVariant = Color(0xFFF1F5F2) // Soothing light neutral
val TricolorLightCardBorder = Color(0xFFE2E8F0) // Subtle card border

// 3. India Green (Harit) - Prosperity, Life & Agriculture
val TricolorGreen = Color(0xFF138808) // Official India green
val TricolorGreenDark = Color(0xFF0D5C05) // Deep forest green
val TricolorGreenLight = Color(0xFFE8F5E9) // Soft mint green tint
val TricolorGreenAccent = Color(0xFF16A34A) // Vibrant action green
val TricolorGreenBorder = Color(0xFFC8E6C9) // Gentle green border

// 4. Ashoka Chakra Navy Blue - Law, Dharma & Stability
val TricolorNavy = Color(0xFF0F2B5C) // Deep Ashoka navy blue
val TricolorNavyLight = Color(0xFFE8EEF8) // Light navy wash
val TricolorNavyAccent = Color(0xFF1E3A8A) // Vibrant navy blue

// Decent Light Theme Neutrals & Typography
val MznLightBg = Color(0xFFF9FAF8) // Calming decent off-white background
val MznLightSurface = Color(0xFFFFFFFF)
val MznLightSurfaceVariant = Color(0xFFF1F5F2)
val MznLightCardBorder = Color(0xFFE2E8F0)
val MznLightTextPrimary = Color(0xFF1E293B) // High-contrast charcoal slate
val MznLightTextSecondary = Color(0xFF52606D) // Decent muted slate

// Mznlive Brand Mapping for Backward Compatibility (Aligned to Tricolor)
val MznCrimson = TricolorSaffron // Primary action maps to deep Saffron
val MznCrimsonDark = TricolorSaffronDark
val MznLiveRed = Color(0xFFE53935) // Urgent broadcast live pulse
val MznAmber = Color(0xFFFF9800) // Warm saffron amber
val MznOrange = TricolorSaffron
val MznTeal = TricolorNavy // Ashoka navy
val MznGreen = TricolorGreen // India green

// Dark Theme Surfaces (Retained as Fallback)
val MznDarkBg = Color(0xFF0F172A)
val MznDarkSurface = Color(0xFF1E293B)
val MznDarkSurfaceVariant = Color(0xFF334155)
val MznDarkCardBorder = Color(0xFF475569)
val MznDarkTextPrimary = Color(0xFFF8FAFC)
val MznDarkTextSecondary = Color(0xFF94A3B8)

// Social Brand Colors
val FacebookBlue = Color(0xFF1877F2)
val InstagramGradientStart = Color(0xFF833AB4)
val InstagramGradientMiddle = Color(0xFFFD1D1D)
val InstagramGradientEnd = Color(0xFFFCB045)
val WhatsAppGreen = Color(0xFF25D366)
val YouTubeRed = Color(0xFFFF0000)

// Branded Tricolor Gradients
val TricolorHorizontalBrush = Brush.horizontalGradient(
    colors = listOf(
        TricolorSaffron,
        Color.White,
        TricolorGreen
    )
)

val TricolorSubtleLightBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF8F0), // Subtle warm saffron mist at top
        Color(0xFFFFFFFF), // Pure white in middle
        Color(0xFFF0FDF4)  // Refreshing soft green mist at bottom
    )
)

/**
 * A decent, refined 3-stripe horizontal Indian Tricolor accent bar.
 * Saffron on left/top, White in center, India Green on right/bottom.
 */
@Composable
fun TricolorAccentBar(
    modifier: Modifier = Modifier,
    height: Dp = 3.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(TricolorSaffron)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(TricolorGreen)
        )
    }
}

