package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * 3D Futuristic Circular Language Button modeled after the user's uploaded button design.
 * Exactly sized to match the 38.dp app logo at the top-left corner, featuring:
 * - 3D metallic beveled outer rim with specular lighting
 * - Pulsing cyber neon glow channel (Cyan for English, Saffron/Ruby for Hindi)
 * - Deep obsidian glossy convex glass dome with reflective crescent highlight
 * - Center holographic globe with latitude & longitude meridian lines
 * - Bold bilingual indicator badge ("हि" / "EN") with 3D flip animation on toggle
 * - Tap to toggle language immediately; long-press to open language selector dropdown
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MznLanguageChangeButton(
    currentLanguage: String,
    onToggleLanguage: () -> Unit,
    onSelectLanguage: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    buttonSize: Dp = 38.dp
) {
    val isHindi = currentLanguage == "hi"
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var showDropdownMenu by remember { mutableStateOf(false) }

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "Lang3DGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    // Animated colors matching active language
    val activeGlowColor by animateColorAsState(
        targetValue = if (isHindi) Color(0xFFFF6D00) else Color(0xFF00E5FF),
        animationSpec = tween(400),
        label = "activeGlowColor"
    )
    val secondaryRingColor by animateColorAsState(
        targetValue = if (isHindi) Color(0xFFFF1744) else Color(0xFF0072FF),
        animationSpec = tween(400),
        label = "secondaryRingColor"
    )

    // 3D Flip animation when language changes
    val flipRotation by animateFloatAsState(
        targetValue = if (isHindi) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "flipRotation"
    )

    // Pressed tactile feedback
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(pressScale)
            .testTag("language_change_button"),
        contentAlignment = Alignment.Center
    ) {
        // Dropdown Menu anchored to button
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            modifier = Modifier
                .background(Color(0xFF000E24))
                .border(1.dp, activeGlowColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IndianFlagRoundel(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "हिंदी (Hindi)",
                            color = Color.White,
                            fontWeight = if (isHindi) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                },
                trailingIcon = {
                    if (isHindi) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color(0xFFFF9933),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onSelectLanguage("hi")
                    if (!isHindi) onToggleLanguage()
                }
            )
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UkFlagRoundel(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "English",
                            color = Color.White,
                            fontWeight = if (!isHindi) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                },
                trailingIcon = {
                    if (!isHindi) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onSelectLanguage("en")
                    if (isHindi) onToggleLanguage()
                }
            )
        }

        // 3D Circular Button Container
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onToggleLanguage() },
                    onLongClick = { showDropdownMenu = true }
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Layer 1: Ambient Outer Glow Ring & 3D Metallic Beveled Border Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val radius = (w.coerceAtMost(h) / 2f)

                    // 1. Ambient outer glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeGlowColor.copy(alpha = 0.45f * pulseGlow),
                                secondaryRingColor.copy(alpha = 0.20f * pulseGlow),
                                Color.Transparent
                            ),
                            center = Offset(w / 2f, h / 2f),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(w / 2f, h / 2f)
                    )

                    // 2. Outermost Metallic Chrome Bevel (Light source from top-left, shadow on bottom-right)
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE2E8F0), // Bright metallic rim top-left
                                Color(0xFF64748B),
                                Color(0xFF1E293B),
                                Color(0xFF0A0F1D)  // Dark shadow bottom-right
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        ),
                        radius = radius - 1.2f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = 2.4f)
                    )

                    // 3. High-Tech Cyber Neon Channel Ring
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                activeGlowColor,
                                secondaryRingColor,
                                activeGlowColor.copy(alpha = 0.6f),
                                secondaryRingColor,
                                activeGlowColor
                            ),
                            center = Offset(w / 2f, h / 2f)
                        ),
                        radius = radius - 3.2f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = 1.6f)
                    )

                    // 4. Deep Obsidian / High-Tech Glass Inner Face
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0F1E36), // Deep blue-cyan center
                                Color(0xFF070D1A),
                                Color(0xFF02040A)  // Pure deep cosmic black edge
                            ),
                            center = Offset(w / 2f, h / 2f),
                            radius = radius - 4.5f
                        ),
                        radius = radius - 4.5f,
                        center = Offset(w / 2f, h / 2f)
                    )

                    // 5. Holographic Globe Wireframe (Equator & Meridians)
                    val globeRadius = radius * 0.58f
                    val center = Offset(w / 2f, h / 2f)

                    // Globe outer subtle wireframe
                    drawCircle(
                        color = activeGlowColor.copy(alpha = 0.35f),
                        radius = globeRadius,
                        center = center,
                        style = Stroke(width = 1.1f)
                    )

                    // Globe horizontal equator
                    drawLine(
                        color = activeGlowColor.copy(alpha = 0.40f),
                        start = Offset(center.x - globeRadius, center.y),
                        end = Offset(center.x + globeRadius, center.y),
                        strokeWidth = 1.0f
                    )

                    // Globe latitude arc top
                    drawArc(
                        color = activeGlowColor.copy(alpha = 0.25f),
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(center.x - globeRadius * 0.85f, center.y - globeRadius * 0.85f),
                        size = Size(globeRadius * 1.7f, globeRadius * 0.8f),
                        style = Stroke(width = 0.9f)
                    )

                    // Globe latitude arc bottom
                    drawArc(
                        color = activeGlowColor.copy(alpha = 0.25f),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(center.x - globeRadius * 0.85f, center.y + globeRadius * 0.05f),
                        size = Size(globeRadius * 1.7f, globeRadius * 0.8f),
                        style = Stroke(width = 0.9f)
                    )

                    // Globe vertical meridian ellipse
                    drawOval(
                        color = activeGlowColor.copy(alpha = 0.35f),
                        topLeft = Offset(center.x - globeRadius * 0.48f, center.y - globeRadius),
                        size = Size(globeRadius * 0.96f, globeRadius * 2f),
                        style = Stroke(width = 0.9f)
                    )

                    // 6. Top Specular Convex Glass Dome Highlight (Glossy 3D reflection)
                    val glassHighlightPath = Path().apply {
                        moveTo(center.x - radius * 0.72f, center.y - radius * 0.15f)
                        cubicTo(
                            center.x - radius * 0.6f, center.y - radius * 0.82f,
                            center.x + radius * 0.6f, center.y - radius * 0.82f,
                            center.x + radius * 0.72f, center.y - radius * 0.15f
                        )
                        cubicTo(
                            center.x + radius * 0.45f, center.y - radius * 0.40f,
                            center.x - radius * 0.45f, center.y - radius * 0.40f,
                            center.x - radius * 0.72f, center.y - radius * 0.15f
                        )
                        close()
                    }
                    drawPath(
                        path = glassHighlightPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.32f),
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                }

                // Layer 2: Center Holographic Bilingual Indicator with 3D Flip
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // 3D Flip perspective effect
                            rotationY = flipRotation
                            cameraDistance = 12f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (flipRotation > 90f) {
                        // Hindi Face (Rotated back to readable)
                        Box(
                            modifier = Modifier
                                .graphicsLayer { rotationY = 180f }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "हि",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xFFFF5722),
                                            offset = Offset(0f, 1f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                                Text(
                                    text = "HI",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFAB40),
                                    letterSpacing = 0.5.sp,
                                    lineHeight = 7.sp
                                )
                            }
                        }
                    } else {
                        // English Face
                        Box(
                            modifier = Modifier.padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "EN",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.2.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xFF00E5FF),
                                            offset = Offset(0f, 1f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                                Text(
                                    text = "LANG",
                                    fontSize = 6.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF80D8FF),
                                    letterSpacing = 0.5.sp,
                                    lineHeight = 6.5.sp
                                )
                            }
                        }
                    }
                }

                // Layer 3: Micro Status LED Indicator at top-right edge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(5.5.dp)
                        .clip(CircleShape)
                        .background(activeGlowColor)
                        .border(0.6.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                )
            }
        }
    }
}

/**
 * Full Horizontal Language Banner Bar (retained for screens requiring the wide dual-pill selector).
 */
@Composable
fun MznLanguageBannerBar(
    currentLanguage: String,
    onToggleLanguage: () -> Unit,
    onSelectLanguage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isHindi = currentLanguage == "hi"

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF000E24),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF00C6FF),
                    if (isHindi) Color(0xFFFF5252) else Color(0xFF40C4FF),
                    Color(0xFF0072FF)
                )
            )
        ),
        shadowElevation = 4.dp,
        modifier = modifier.testTag("language_banner_bar")
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF001433), Color(0xFF000C1F))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            LanguagePill(
                isActive = isHindi,
                flag = { IndianFlagRoundel(modifier = Modifier.size(14.dp)) },
                label = "हिंदी",
                activeGradient = Brush.horizontalGradient(
                    listOf(Color(0xFFE50914), Color(0xFFB81D24))
                ),
                activeBorderColor = Color(0xFFFF5252),
                compact = true,
                testTag = "lang_btn_hindi",
                onClick = {
                    onSelectLanguage("hi")
                    if (!isHindi) onToggleLanguage()
                }
            )

            Spacer(modifier = Modifier.width(4.dp))

            Surface(
                onClick = onToggleLanguage,
                shape = CircleShape,
                color = Color(0xFF003366),
                border = BorderStroke(0.8.dp, Color(0xFF00C6FF)),
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Swap",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            LanguagePill(
                isActive = !isHindi,
                flag = { UkFlagRoundel(modifier = Modifier.size(14.dp)) },
                label = "English",
                activeGradient = Brush.horizontalGradient(
                    listOf(Color(0xFF0070F3), Color(0xFF0044B3))
                ),
                activeBorderColor = Color(0xFF40C4FF),
                compact = true,
                testTag = "lang_btn_english",
                onClick = {
                    onSelectLanguage("en")
                    if (isHindi) onToggleLanguage()
                }
            )
        }
    }
}

/**
 * Individual Language Pill (Hindi or English)
 */
@Composable
private fun LanguagePill(
    isActive: Boolean,
    flag: @Composable () -> Unit,
    label: String,
    activeGradient: Brush,
    activeBorderColor: Color,
    compact: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bgModifier = if (isActive) {
        Modifier.background(activeGradient)
    } else {
        Modifier.background(Color(0xFF0F172A).copy(alpha = 0.7f))
    }

    val borderStroke = if (isActive) {
        BorderStroke(1.2.dp, activeBorderColor)
    } else {
        BorderStroke(0.6.dp, Color(0xFF334155))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        border = borderStroke,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = bgModifier.padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = if (compact) 3.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            flag()
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = if (compact) 10.sp else 11.5.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Vector Indian Flag Roundel (🇮🇳)
 */
@Composable
fun IndianFlagRoundel(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .clip(CircleShape)
            .border(0.6.dp, Color.White.copy(alpha = 0.8f), CircleShape)
    ) {
        val h = size.height
        val w = size.width
        val stripeH = h / 3f

        // Top Saffron stripe
        drawRect(
            color = Color(0xFFFF9933),
            topLeft = Offset(0f, 0f),
            size = Size(w, stripeH)
        )

        // Middle White stripe
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, stripeH),
            size = Size(w, stripeH)
        )

        // Bottom Green stripe
        drawRect(
            color = Color(0xFF138808),
            topLeft = Offset(0f, stripeH * 2),
            size = Size(w, stripeH)
        )

        // Center Ashoka Chakra Navy Blue Circle
        val centerPoint = Offset(w / 2f, h / 2f)
        val chakraRadius = stripeH * 0.38f
        drawCircle(
            color = Color(0xFF000080),
            radius = chakraRadius,
            center = centerPoint,
            style = Stroke(width = 1.2f)
        )
        // Hub dot
        drawCircle(
            color = Color(0xFF000080),
            radius = chakraRadius * 0.28f,
            center = centerPoint
        )
    }
}

/**
 * Vector UK / English Flag Roundel (🇬🇧)
 */
@Composable
fun UkFlagRoundel(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .clip(CircleShape)
            .border(0.6.dp, Color.White.copy(alpha = 0.8f), CircleShape)
    ) {
        val w = size.width
        val h = size.height

        // Royal Blue Background
        drawRect(color = Color(0xFF00247D))

        // White diagonal saltire
        val diagStrokeWhite = w * 0.22f
        drawLine(
            color = Color.White,
            start = Offset(0f, 0f),
            end = Offset(w, h),
            strokeWidth = diagStrokeWhite,
            cap = StrokeCap.Square
        )
        drawLine(
            color = Color.White,
            start = Offset(w, 0f),
            end = Offset(0f, h),
            strokeWidth = diagStrokeWhite,
            cap = StrokeCap.Square
        )

        // Red diagonal saltire
        val diagStrokeRed = w * 0.10f
        drawLine(
            color = Color(0xFFCF142B),
            start = Offset(0f, 0f),
            end = Offset(w, h),
            strokeWidth = diagStrokeRed,
            cap = StrokeCap.Square
        )
        drawLine(
            color = Color(0xFFCF142B),
            start = Offset(w, 0f),
            end = Offset(0f, h),
            strokeWidth = diagStrokeRed,
            cap = StrokeCap.Square
        )

        // White St George's cross
        val crossWhite = w * 0.30f
        drawRect(
            color = Color.White,
            topLeft = Offset((w - crossWhite) / 2f, 0f),
            size = Size(crossWhite, h)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, (h - crossWhite) / 2f),
            size = Size(w, crossWhite)
        )

        // Red St George's cross
        val crossRed = w * 0.18f
        drawRect(
            color = Color(0xFFCF142B),
            topLeft = Offset((w - crossRed) / 2f, 0f),
            size = Size(crossRed, h)
        )
        drawRect(
            color = Color(0xFFCF142B),
            topLeft = Offset(0f, (h - crossRed) / 2f),
            size = Size(w, crossRed)
        )
    }
}
