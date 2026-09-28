package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct
import com.example.data.repository.ProductCatalogRepository
import com.example.ui.theme.TricolorGreen
import com.example.ui.theme.TricolorNavy
import com.example.ui.theme.TricolorSaffron
import com.example.util.ShareUtils

/**
 * Auto-Rearrange Aspect Ratio and Scaling Modes for Videos and Media Streams
 */
enum class VideoFitMode(
    val label: String,
    val shortLabel: String,
    val description: String
) {
    AUTO("Auto Rearrange", "⚡ Auto", "Adapts proportionally according to video & screen bounds"),
    REEL_9_16("9:16 Reel", "📱 9:16", "Vertical mobile video format"),
    WIDE_16_9("16:9 Cinema", "🖥️ 16:9", "Studio widescreen broadcast format"),
    FIT("Fit Screen", "↔️ Fit", "100% video visible inside player viewport with original proportions"),
    FILL("Fill Screen", "⤢ Fill", "Expands to cover full player container (edge-to-edge)");

    fun next(): VideoFitMode = when (this) {
        AUTO -> REEL_9_16
        REEL_9_16 -> WIDE_16_9
        WIDE_16_9 -> FIT
        FIT -> FILL
        FILL -> AUTO
    }
}

/**
 * In-App Image Viewer Dialog
 * Displays high-resolution images within the app portal with close and external share capabilities.
 */
@Composable
fun InAppImageViewerDialog(
    imageUrl: String,
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    onShare: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isFillMode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler { onDismiss() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("in_app_image_viewer_dialog")
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.applogo3dtrns),
                        contentDescription = "MznLive Logo",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mzn",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Live",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorSaffron
                            )
                            Text(
                                text = " • Photo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorGreen
                            )
                        }
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Auto Rearrange Size Mode Toggle (Fit Screen vs Fill Screen)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFillMode) TricolorSaffron else Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(
                            0.8.dp,
                            if (isFillMode) TricolorSaffron else Color.White.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .clickable { isFillMode = !isFillMode }
                            .testTag("toggle_image_rearrange_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFillMode) Icons.Default.FitScreen else Icons.Default.CropFree,
                                contentDescription = "Rearrange Image Size",
                                tint = if (isFillMode) Color.Black else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFillMode) "Fill Screen" else "Fit Screen",
                                color = if (isFillMode) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (onShare != null) {
                                onShare()
                            } else {
                                ShareUtils.shareText(
                                    context = context,
                                    title = title,
                                    message = subtitle ?: "Check out this image on MznLive!",
                                    linkUrl = imageUrl
                                )
                            }
                        },
                        modifier = Modifier.testTag("in_app_image_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Image",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("in_app_image_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            // Image Center Display with dynamic responsive auto-rearrange scaling
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 60.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                var isLoading by remember { mutableStateOf(true) }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .listener(
                            onSuccess = { _, _ -> isLoading = false },
                            onError = { _, _ -> isLoading = false }
                        )
                        .build(),
                    contentDescription = title,
                    contentScale = if (isFillMode) ContentScale.Crop else ContentScale.Fit,
                    modifier = if (isFillMode) {
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(12.dp))
                    }
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        color = TricolorSaffron,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // Bottom Caption & Quick Share Bar
            if (!subtitle.isNullOrBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = subtitle,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (onShare != null) {
                                    onShare()
                                } else {
                                    ShareUtils.shareText(
                                        context = context,
                                        title = title,
                                        message = subtitle,
                                        linkUrl = imageUrl
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * In-Screen Inline Media Player Component
 * Shows a high-resolution visual preview card of YouTube streams, Instagram reels, and videos in-screen
 * with play indicator, status badges, and interactive click handling.
 * Avoids spawning multiple WebView instances in scrolling feeds and grids, eliminating renderer crashes.
 * When tapped, [onClick] triggers the dedicated popup player with full audio/video playback!
 */
@Composable
fun InlineMutedMediaPreviewPlayer(
    mediaUrl: String,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    title: String = "",
    badgeLabel: String? = null,
    isLive: Boolean = false,
    aspectRatio: Float = 0f,
    onClick: () -> Unit,
    topEndContent: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val effectiveAspectRatio = remember(mediaUrl, aspectRatio) {
        if (aspectRatio > 0f) {
            aspectRatio
        } else {
            when {
                mediaUrl.contains("reel", ignoreCase = true) ||
                mediaUrl.contains("shorts", ignoreCase = true) ||
                mediaUrl.contains("instagram.com", ignoreCase = true) -> 9f / 16f
                mediaUrl.contains("live", ignoreCase = true) ||
                mediaUrl.contains("stream", ignoreCase = true) ||
                mediaUrl.contains("watch?v=", ignoreCase = true) ||
                mediaUrl.contains("youtu.be", ignoreCase = true) -> 16f / 9f
                else -> 16f / 9f
            }
        }
    }

    Card(
        modifier = modifier
            .aspectRatio(effectiveAspectRatio)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("inline_muted_media_player"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Fallback Thumbnail
            if (!thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Top gradient overlay for badge readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )

            // Center Play Pill Action
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bottom gradient overlay for content readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )

            // Top Status Badges (Live, Category, and Muted Indicator)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLive) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE53935))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (!badgeLabel.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Audio Indicator
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "Muted Preview",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Tap to Play",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top End Content (e.g. Share or Camera button)
            if (topEndContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    topEndContent()
                }
            }

            // Bottom Content
            if (bottomContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    bottomContent()
                }
            } else {
                // Default Bottom Prompt: "Tap to play with sound 🔊"
                Surface(
                    color = Color.Black.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .clickable { onClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play with Sound",
                            tint = TricolorSaffron,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tap to play with sound",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * In-App Video & Reel Player Dialog
 * Runs YouTube videos, Instagram Reels, and direct video clips directly inside the screen area
 * by default without forcing the user to leave the MznLive portal.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppVideoPlayerDialog(
    mediaUrl: String,
    title: String,
    sourceLabel: String = "MznLive Stream",
    shopName: String? = null,
    sellerShop: MarketplaceShop? = null,
    product: PromotionalProduct? = null,
    availableShops: List<MarketplaceShop> = emptyList(),
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onBuyClick: ((PromotionalProduct) -> Unit)? = null,
    onGoToSeller: ((MarketplaceShop) -> Unit)? = null
) {
    val context = LocalContext.current
    var isPlayerLoading by remember { mutableStateOf(true) }
    var hasRenderCrashed by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentFitMode by remember { mutableStateOf(VideoFitMode.AUTO) }

    val isReel = remember(mediaUrl) {
        mediaUrl.contains("reel", ignoreCase = true) ||
        mediaUrl.contains("shorts", ignoreCase = true) ||
        mediaUrl.contains("instagram.com", ignoreCase = true)
    }
    val isLive = remember(mediaUrl) {
        mediaUrl.contains("live", ignoreCase = true) ||
        mediaUrl.contains("stream", ignoreCase = true) ||
        mediaUrl.contains("watch?v=", ignoreCase = true) ||
        mediaUrl.contains("youtu.be", ignoreCase = true)
    }

    LaunchedEffect(currentFitMode) {
        applyFitModeToWebView(webViewInstance, currentFitMode)
    }

    // Resolve shop and product dynamically so every video/reel always has valid seller & product context
    val resolvedShop = remember(shopName, sourceLabel, title, sellerShop, availableShops) {
        sellerShop ?: ProductCatalogRepository.findShopForMedia(shopName ?: sourceLabel, title, availableShops)
    }
    val resolvedProduct = remember(shopName, sourceLabel, title, product) {
        product ?: ProductCatalogRepository.findProductForMedia(shopName ?: sourceLabel, title)
    }

    // Ensure audio stops when player is closed
    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.let { wv ->
                    wv.stopLoading()
                    wv.loadUrl("about:blank")
                    wv.onPause()
                    wv.destroy()
                }
            } catch (_: Throwable) {}
            webViewInstance = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler { onDismiss() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("in_app_video_player_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.applogo3dtrns),
                            contentDescription = "MznLive Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mzn",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Live",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TricolorSaffron
                                )
                                Text(
                                    text = " • Player",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TricolorGreen
                                )
                            }
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Aspect Ratio / Auto Rearrange Cycle Button
                        IconButton(
                            onClick = { currentFitMode = currentFitMode.next() },
                            modifier = Modifier.testTag("in_app_video_fit_toggle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Rearrange Mode: ${currentFitMode.label}",
                                tint = TricolorSaffron
                            )
                        }

                        IconButton(
                            onClick = {
                                if (onShare != null) {
                                    onShare()
                                } else {
                                    ShareUtils.shareReelOrVideo(
                                        context = context,
                                        title = title,
                                        creator = sourceLabel,
                                        videoUrl = mediaUrl
                                    )
                                }
                            },
                            modifier = Modifier.testTag("in_app_video_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("in_app_video_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Player",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Video Auto-Rearrange Mode Selector Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(0.5.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = null,
                                tint = TricolorSaffron,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Auto Rearrange:",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            VideoFitMode.values().forEach { mode ->
                                val isSelected = currentFitMode == mode
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) TricolorSaffron else Color(0xFF1E293B),
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (isSelected) TricolorSaffron else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .clickable { currentFitMode = mode }
                                        .testTag("fit_mode_${mode.name.lowercase()}")
                                ) {
                                    Text(
                                        text = mode.shortLabel,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Player Canvas (Embedded WebView configured for responsive inline HTML5 video / iframe playback)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val availWidth = maxWidth
                    val availHeight = maxHeight

                    val containerModifier = when (currentFitMode) {
                        VideoFitMode.AUTO -> {
                            if (isReel) {
                                val targetHeight = availHeight
                                val targetWidth = minOf(availWidth, targetHeight * (9f / 16f))
                                Modifier
                                    .width(targetWidth)
                                    .height(targetHeight)
                                    .clip(RoundedCornerShape(8.dp))
                            } else if (isLive) {
                                val targetWidth = availWidth
                                val targetHeight = minOf(availHeight, targetWidth * (9f / 16f))
                                Modifier
                                    .width(targetWidth)
                                    .height(targetHeight)
                            } else {
                                Modifier.fillMaxSize()
                            }
                        }
                        VideoFitMode.REEL_9_16 -> {
                            val targetHeight = availHeight
                            val targetWidth = minOf(availWidth, targetHeight * (9f / 16f))
                            Modifier
                                .width(targetWidth)
                                .height(targetHeight)
                                .clip(RoundedCornerShape(8.dp))
                        }
                        VideoFitMode.WIDE_16_9 -> {
                            val targetWidth = availWidth
                            val targetHeight = minOf(availHeight, targetWidth * (9f / 16f))
                            Modifier
                                .width(targetWidth)
                                .height(targetHeight)
                        }
                        VideoFitMode.FIT -> Modifier.fillMaxSize()
                        VideoFitMode.FILL -> Modifier.fillMaxSize()
                    }

                    Box(
                        modifier = containerModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        if (!hasRenderCrashed) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        setBackgroundColor(android.graphics.Color.BLACK)
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            mediaPlaybackRequiresUserGesture = false
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                            allowContentAccess = true
                                            allowFileAccess = false
                                        }
                                        webChromeClient = WebChromeClient()
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                isPlayerLoading = true
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isPlayerLoading = false
                                                applyFitModeToWebView(view, currentFitMode)
                                            }

                                            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                                try {
                                                    view?.let { wv ->
                                                        (wv.parent as? ViewGroup)?.removeView(wv)
                                                        wv.destroy()
                                                    }
                                                } catch (_: Throwable) {
                                                }
                                                webViewInstance = null
                                                hasRenderCrashed = true
                                                isPlayerLoading = false
                                                return true // CRITICAL: prevents Android app process from crashing!
                                            }

                                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                                super.onReceivedError(view, request, error)
                                            }
                                        }

                                        loadMediaContent(this, mediaUrl, currentFitMode)
                                        webViewInstance = this
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Resilient Fallback UI if Chromium renderer crashed
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = TricolorSaffron,
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Media Player Standby",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "The media playback engine paused. Tap below to reload seamlessly.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        hasRenderCrashed = false
                                        isPlayerLoading = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reload Video", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (isPlayerLoading && !hasRenderCrashed) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = TricolorSaffron)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Loading Media in MznLive...",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Bottom Section: Sound Info & The 3 Action Buttons (1. Back, 2. Buy, 3. Go to Seller's Product)
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        // Sound Status & Seller Highlight Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(TricolorGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Sound On",
                                    tint = TricolorGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Playing with Sound & Music",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Shop pill
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = resolvedShop.name,
                                    color = TricolorSaffron,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Product Preview line
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Product: ",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = resolvedProduct.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹${resolvedProduct.priceInr}",
                                color = TricolorGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Exactly the 3 requested buttons:
                        // 1. Back
                        // 2. Buy
                        // 3. Go to this seller's product
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. BACK BUTTON
                            OutlinedButton(
                                onClick = {
                                    if (onBack != null) onBack() else onDismiss()
                                },
                                modifier = Modifier
                                    .weight(0.85f)
                                    .height(48.dp)
                                    .testTag("player_btn_back"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.25f)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Back",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            // 2. BUY BUTTON
                            Button(
                                onClick = {
                                    if (onBuyClick != null) {
                                        onBuyClick(resolvedProduct)
                                    } else {
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("player_btn_buy"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TricolorSaffron,
                                    contentColor = Color.Black
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Buy",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Buy • ₹${resolvedProduct.priceInr}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 3. GO TO THIS SELLER'S PRODUCT BUTTON
                            Button(
                                onClick = {
                                    if (onGoToSeller != null) {
                                        onGoToSeller(resolvedShop)
                                    } else {
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.35f)
                                    .height(48.dp)
                                    .testTag("player_btn_go_to_seller"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TricolorNavy,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Go to this seller's product",
                                    modifier = Modifier.size(16.dp),
                                    tint = TricolorSaffron
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Seller's Store",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dynamically updates the HTML/CSS container inside the WebView to rearrange video sizing.
 */
fun applyFitModeToWebView(webView: WebView?, fitMode: VideoFitMode) {
    if (webView == null) return
    val jsCode = when (fitMode) {
        VideoFitMode.AUTO, VideoFitMode.FIT -> """
            (function() {
                var v = document.querySelector('video');
                if (v) { v.style.objectFit = 'contain'; v.style.width = '100%'; v.style.height = '100%'; }
                var ifr = document.querySelector('iframe');
                if (ifr) { ifr.style.width = '100%'; ifr.style.height = '100%'; ifr.style.maxWidth = '100%'; }
            })();
        """.trimIndent()
        VideoFitMode.FILL -> """
            (function() {
                var v = document.querySelector('video');
                if (v) { v.style.objectFit = 'cover'; v.style.width = '100vw'; v.style.height = '100vh'; }
                var ifr = document.querySelector('iframe');
                if (ifr) { ifr.style.width = '100%'; ifr.style.height = '100%'; ifr.style.maxWidth = '100%'; }
            })();
        """.trimIndent()
        VideoFitMode.REEL_9_16 -> """
            (function() {
                var v = document.querySelector('video');
                if (v) { v.style.objectFit = 'cover'; v.style.aspectRatio = '9/16'; }
                var ifr = document.querySelector('iframe');
                if (ifr) { ifr.style.aspectRatio = '9/16'; ifr.style.maxWidth = '480px'; }
            })();
        """.trimIndent()
        VideoFitMode.WIDE_16_9 -> """
            (function() {
                var v = document.querySelector('video');
                if (v) { v.style.objectFit = 'contain'; v.style.aspectRatio = '16/9'; }
                var ifr = document.querySelector('iframe');
                if (ifr) { ifr.style.aspectRatio = '16/9'; ifr.style.width = '100%'; }
            })();
        """.trimIndent()
    }
    try {
        webView.evaluateJavascript(jsCode, null)
    } catch (_: Throwable) {}
}

/**
 * Loads video or reel inside the WebView using appropriate HTML5 container or direct embed URL WITH SOUND.
 */
private fun loadMediaContent(webView: WebView, rawUrl: String, fitMode: VideoFitMode = VideoFitMode.AUTO) {
    val cleanUrl = rawUrl.trim()
    val objectFitCss = if (fitMode == VideoFitMode.FILL) "cover" else "contain"

    // 1. YouTube video check (autoplay WITH sound: mute=0)
    val youtubeVideoId = extractYoutubeId(cleanUrl)
    if (youtubeVideoId != null) {
        val embedHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                    iframe { width: 100%; height: 100%; border: none; }
                </style>
            </head>
            <body>
                <iframe src="https://www.youtube-nocookie.com/embed/$youtubeVideoId?autoplay=1&mute=0&playsinline=1&controls=1&rel=0&modestbranding=1" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                        allowfullscreen></iframe>
            </body>
            </html>
        """.trimIndent()
        webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "UTF-8", null)
        return
    }

    // 2. Instagram Reel check
    if (cleanUrl.contains("instagram.com/reel/") || cleanUrl.contains("instagram.com/p/")) {
        val reelId = extractInstagramReelId(cleanUrl)
        if (reelId != null) {
            val instagramEmbedHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body, html { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; display: flex; justify-content: center; align-items: center; overflow: hidden; }
                        iframe { width: 100%; height: 100%; max-width: 480px; border: none; }
                    </style>
                </head>
                <body>
                    <iframe src="https://www.instagram.com/reel/$reelId/embed" allow="autoplay; encrypted-media" allowfullscreen></iframe>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL("https://www.instagram.com", instagramEmbedHtml, "text/html", "UTF-8", null)
            return
        }
    }

    // 3. Direct video format (.mp4, .webm, etc.) with sound
    if (cleanUrl.endsWith(".mp4", ignoreCase = true) || cleanUrl.endsWith(".webm", ignoreCase = true) || cleanUrl.contains("storage.googleapis.com")) {
        val videoHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; display: flex; justify-content: center; align-items: center; }
                    video { width: 100%; height: 100%; max-height: 100vh; object-fit: $objectFitCss; }
                </style>
            </head>
            <body>
                <video src="$cleanUrl" autoplay controls playsinline></video>
            </body>
            </html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
        return
    }

    // 4. Default: Load the web page safely in WebView
    webView.loadUrl(cleanUrl)
}

/**
 * Loads video or reel inside the in-screen preview WebView MUTED WITHOUT SOUND.
 */
fun loadMutedPreviewContent(webView: WebView, rawUrl: String) {
    val cleanUrl = rawUrl.trim()

    // 1. YouTube video check (muted=1, autoplay=1, loop=1, controls=0)
    val youtubeVideoId = extractYoutubeId(cleanUrl)
    if (youtubeVideoId != null) {
        val embedHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body, html { width: 100%; height: 100%; background: #000; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                    iframe { width: 100%; height: 100%; border: none; pointer-events: none; }
                </style>
            </head>
            <body>
                <iframe src="https://www.youtube-nocookie.com/embed/$youtubeVideoId?autoplay=1&mute=1&playsinline=1&loop=1&playlist=$youtubeVideoId&controls=0&modestbranding=1&rel=0&iv_load_policy=3" 
                        allow="autoplay; encrypted-media"></iframe>
            </body>
            </html>
        """.trimIndent()
        webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "UTF-8", null)
        return
    }

    // 2. Instagram Reel check (muted embed)
    if (cleanUrl.contains("instagram.com/reel/") || cleanUrl.contains("instagram.com/p/")) {
        val reelId = extractInstagramReelId(cleanUrl)
        if (reelId != null) {
            val instagramEmbedHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body, html { width: 100%; height: 100%; background: #000; display: flex; justify-content: center; align-items: center; overflow: hidden; }
                        iframe { width: 100%; height: 100%; border: none; pointer-events: none; }
                    </style>
                </head>
                <body>
                    <iframe src="https://www.instagram.com/reel/$reelId/embed" allow="autoplay; encrypted-media"></iframe>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL("https://www.instagram.com", instagramEmbedHtml, "text/html", "UTF-8", null)
            return
        }
    }

    // 3. Direct video format (.mp4, .webm, etc.) with muted autoplay loop
    if (cleanUrl.endsWith(".mp4", ignoreCase = true) || cleanUrl.endsWith(".webm", ignoreCase = true) || cleanUrl.contains("storage.googleapis.com")) {
        val videoHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body, html { width: 100%; height: 100%; background: #000; display: flex; justify-content: center; align-items: center; overflow: hidden; }
                    video { width: 100%; height: 100%; object-fit: cover; pointer-events: none; }
                </style>
            </head>
            <body>
                <video src="$cleanUrl" autoplay muted loop playsinline></video>
            </body>
            </html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
        return
    }

    // 4. Fallback: Load web page
    webView.loadUrl(cleanUrl)
}

private fun extractYoutubeId(url: String): String? {
    if (url.isBlank()) return null
    val patterns = listOf(
        "v=([a-zA-Z0-9_-]{11})",
        "youtu\\.be/([a-zA-Z0-9_-]{11})",
        "embed/([a-zA-Z0-9_-]{11})",
        "shorts/([a-zA-Z0-9_-]{11})"
    )
    for (p in patterns) {
        val regex = Regex(p)
        val match = regex.find(url)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1]
        }
    }
    return null
}

private fun extractInstagramReelId(url: String): String? {
    if (url.isBlank()) return null
    val patterns = listOf(
        "reel/([a-zA-Z0-9_-]+)",
        "p/([a-zA-Z0-9_-]+)"
    )
    for (p in patterns) {
        val regex = Regex(p)
        val match = regex.find(url)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1]
        }
    }
    return null
}
