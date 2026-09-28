package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Horizontal 3D rotation animation (rotationY) for 4 seconds
    val rotationY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Horizontally rotating function for exactly 4 seconds (4000ms)
        rotationY.animateTo(
            targetValue = 720f,
            animationSpec = tween(
                durationMillis = 4000,
                easing = FastOutSlowInEasing
            )
        )
        // Screen redirects to the second screen after 4 seconds
        onTimeout()
    }

    // Pulse animation for LIVE badge
    val infiniteTransition = rememberInfiniteTransition(label = "SplashPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF7ED), // Subtle warm Saffron mist at top
                        Color(0xFFFFFFFF), // Pure crisp white in center
                        Color(0xFFF0FDF4)  // Refreshing soft Green mist at bottom
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow behind enlarged logo
        Box(
            modifier = Modifier
                .size(310.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TricolorSaffron.copy(alpha = 0.20f),
                            TricolorGreen.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Center Content: Rotating Logo & App Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Enhanced 3D Horizontally Rotating Logo (240dp)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        this.rotationY = rotationY.value
                        cameraDistance = 16f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.applogo3dtrns),
                    contentDescription = "Mznlive Logo",
                    modifier = Modifier.size(240.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name with stylish typography
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mzn",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TricolorNavy,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "live",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TricolorSaffron,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LIVE Broadcast Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = "Live Indicator",
                    tint = MznLiveRed.copy(alpha = pulseAlpha),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE NEWS & LOCAL MARKET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TricolorNavy,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Indian Tricolor Accent Strip
            TricolorAccentBar(
                modifier = Modifier
                    .width(100.dp)
                    .clip(RoundedCornerShape(2.dp)),
                height = 3.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Breaking News • Reels • Marketplace • Events",
                fontSize = 13.sp,
                color = MznLightTextSecondary,
                fontWeight = FontWeight.Normal
            )
        }

        // Bottom Watermark - Strictly as requested
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Powered by TalntVibe@2026",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.testTag("watermark_text")
                )
            }
        }
    }
}
