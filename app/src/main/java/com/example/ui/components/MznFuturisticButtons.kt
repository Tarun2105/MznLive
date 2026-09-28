package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TricolorNavy
import com.example.ui.theme.TricolorSaffron
import com.example.ui.theme.TricolorGreen

/**
 * 3D High-Tech Tab Button designed according to the attached futuristic button aesthetic.
 * Sized appropriately to fit seamlessly within the app's bottom navigation bar without altering
 * standard tab heights or crowding screen estate.
 */
@Composable
fun MznTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "tab_button_${label.lowercase()}"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TabGlowTransition")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tabGlowPulse"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E3A5F).copy(alpha = 0.4f),
        label = "tabBorderColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabScale"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF001433) else Color(0xFF000C1F).copy(alpha = 0.85f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            brush = if (isSelected) {
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF00E5FF),
                        Color(0xFFFF9933).copy(alpha = glowPulse),
                        Color(0xFF0072FF)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1A365D).copy(alpha = 0.4f),
                        Color(0xFF0F172A).copy(alpha = 0.4f)
                    )
                )
            }
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        modifier = modifier
            .scale(scale)
            .height(54.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color(0xFF00E5FF)),
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isSelected) {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF00224D),
                                Color(0xFF001026),
                                Color(0xFF001A3D)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0A1128).copy(alpha = 0.6f),
                                Color(0xFF030712).copy(alpha = 0.8f)
                            )
                        )
                    }
                )
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with glowing effect if active
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(if (isSelected) 21.dp else 19.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Label
            Text(
                text = label,
                fontSize = if (isSelected) 10.5.sp else 10.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Active indicator pip
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(width = 12.dp, height = 2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFFFF9933))
                            )
                        )
                )
            }
        }
    }
}

/**
 * High-Tech 3D Sign Up Action Button designed strictly matching the uploaded futuristic button style.
 * Used for "Signup as User" and "Signup as Shop Owner" on the Me screen.
 */
@Composable
fun MznSignUpActionButton(
    title: String,
    subtitle: String,
    roleTag: String,
    isShopOwner: Boolean,
    isAlreadyRegistered: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SignUpBtnGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "signUpPulseGlow"
    )

    val primaryGlowColor = if (isShopOwner) Color(0xFFFF9933) else Color(0xFF00E5FF)
    val accentGlowColor = if (isShopOwner) Color(0xFFFFD700) else Color(0xFF10B981)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF000E24),
        border = BorderStroke(
            width = 1.8.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    primaryGlowColor,
                    accentGlowColor.copy(alpha = pulseGlow),
                    if (isShopOwner) Color(0xFFCC5500) else Color(0xFF0072FF)
                )
            )
        ),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = primaryGlowColor),
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF00183B),
                            Color(0xFF000C1F),
                            Color(0xFF001433)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Mini Futuristic Indicator Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "▶",
                        color = if (isShopOwner) Color(0xFFFF5252) else Color(0xFF00E5FF),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = roleTag.uppercase(),
                        color = if (isShopOwner) Color(0xFFFFD700) else Color(0xFF38BDF8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "◀",
                        color = if (isShopOwner) Color(0xFFFF5252) else Color(0xFF00E5FF),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (isAlreadyRegistered) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF10B981),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Main Interactive Row with Icon Emblem, Title/Subtitle, and Action Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3D Circular Emblem with App / Role Logo
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (isShopOwner) Color(0xFF4A1A02) else Color(0xFF003366),
                                    Color(0xFF000E24)
                                )
                            )
                        )
                        .border(1.5.dp, primaryGlowColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isShopOwner) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Shop Owner",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Subtitle Description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Action Pill Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryGlowColor,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAlreadyRegistered) "VIEW" else "SIGN UP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isShopOwner) Color(0xFF000C1F) else Color(0xFF00183B)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = if (isShopOwner) Color(0xFF000C1F) else Color(0xFF00183B),
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }
        }
    }
}
