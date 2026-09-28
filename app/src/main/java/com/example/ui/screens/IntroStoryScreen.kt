package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.IntroSlide
import com.example.ui.theme.*

@Composable
fun IntroStoryScreen(
    slides: List<IntroSlide>,
    currentIndex: Int,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val currentSlide = slides.getOrNull(currentIndex) ?: IntroSlide(
        id = 1,
        orderNum = 1,
        title = "Welcome to Mznlive",
        description = "Connecting Local News & Local Business",
        imageUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&auto=format&fit=crop&q=80",
        facebookPostUrl = "https://facebook.com/mznlive"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("intro_story_screen")
    ) {
        // Background Image for the current 1-second slide
        AnimatedContent(
            targetState = currentSlide,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SlideTransition",
            modifier = Modifier.fillMaxSize()
        ) { slide ->
            AsyncImage(
                model = slide.imageUrl,
                contentDescription = slide.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Top Navigation: 3-part Story Progress Bars & Skip Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 3 Progress Bar Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until 3) {
                    val isActive = i <= currentIndex
                    val tricolorIndicatorColor = when (i) {
                        0 -> TricolorSaffron
                        1 -> Color.White
                        else -> TricolorGreen
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (isActive) tricolorIndicatorColor else Color.White.copy(alpha = 0.35f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Header Bar: App Logo & "MznLive • Spotlight" (Left) & Skip (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.applogo3dtrns),
                        contentDescription = "MznLive Logo",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .testTag("top_left_app_logo")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mzn",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Live",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TricolorSaffron
                        )
                        Text(
                            text = " • Spotlight",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorGreen
                        )
                    }
                }

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.testTag("skip_intro_button")
                ) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Skip",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Bottom Info Card: Slide Details & Database Facebook Link
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentSlide.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (currentSlide.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentSlide.description,
                    fontSize = 14.sp,
                    color = Color(0xFFE2E8F0),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Facebook Link button from Supabase database table
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentSlide.facebookPostUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("facebook_slide_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Facebook Post",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View on Facebook Page",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Slide ${currentIndex + 1} of 3 • Auto advancing (1s)",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
