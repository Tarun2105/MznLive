package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Draggable Floating Chat Button that users can drag anywhere on the screen with finger selection.
 * Tapping it opens the live Merchant & Support chat dialog.
 */
@Composable
fun DraggableFloatingChatButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Chat with Local Merchants"
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        val buttonSizeDp = 58.dp
        val buttonSizePx = with(density) { buttonSizeDp.toPx() }
        val edgeMarginPx = with(density) { 16.dp.toPx() }
        val bottomMarginPx = with(density) { 32.dp.toPx() }

        // Initial Position: bottom-right above navigation bar
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var isInitialized by remember { mutableStateOf(false) }

        LaunchedEffect(maxWidthPx, maxHeightPx) {
            if (!isInitialized && maxWidthPx > 0 && maxHeightPx > 0) {
                offsetX = (maxWidthPx - buttonSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx)
                offsetY = (maxHeightPx - buttonSizePx - bottomMarginPx).coerceAtLeast(edgeMarginPx)
                isInitialized = true
            } else if (isInitialized) {
                // Keep within screen bounds on orientation or window resize
                offsetX = offsetX.coerceIn(edgeMarginPx, (maxWidthPx - buttonSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx))
                offsetY = offsetY.coerceIn(edgeMarginPx, (maxHeightPx - buttonSizePx - bottomMarginPx).coerceAtLeast(edgeMarginPx))
            }
        }

        var isDragging by remember { mutableStateOf(false) }
        var totalDragDistance by remember { mutableFloatStateOf(0f) }

        // Scale animation when user picks up and drags the button
        val scale by animateFloatAsState(
            targetValue = if (isDragging) 1.15f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "dragScale"
        )

        // Pulsing glow animation for the live status ring
        val infiniteTransition = rememberInfiniteTransition(label = "ChatGlow")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseScale"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseAlpha"
        )

        if (isInitialized) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(buttonSizeDp)
                    .scale(scale)
                    .testTag("floating_draggable_chat_button")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                totalDragDistance = 0f
                            },
                            onDragEnd = {
                                isDragging = false
                                // If distance was very small, treat as a finger click/tap
                                if (totalDragDistance < 15f) {
                                    onClick()
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragDistance += dragAmount.getDistance()
                                val minX = edgeMarginPx
                                val maxX = (maxWidthPx - buttonSizePx - edgeMarginPx).coerceAtLeast(minX)
                                val minY = edgeMarginPx
                                val maxY = (maxHeightPx - buttonSizePx - edgeMarginPx).coerceAtLeast(minY)

                                offsetX = (offsetX + dragAmount.x).coerceIn(minX, maxX)
                                offsetY = (offsetY + dragAmount.y).coerceIn(minY, maxY)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulsing Glow Aura
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9933).copy(alpha = pulseAlpha))
                )

                // Main Floating Circle Surface
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    shadowElevation = if (isDragging) 16.dp else 8.dp,
                    border = BorderStroke(
                        2.dp,
                        Brush.linearGradient(
                            listOf(Color.White, Color(0xFFFFD54F), Color.White)
                        )
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFF9933),
                                        Color(0xFFFF6600),
                                        Color(0xFFE65100)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Chat Icon
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = contentDescription,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )

                        // Top-Right Live Online Green Indicator Dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF138808))
                                .border(1.8.dp, Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
