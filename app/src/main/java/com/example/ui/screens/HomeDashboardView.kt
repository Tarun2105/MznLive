package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.util.emulatorScrollable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.ShareUtils
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private fun extractYoutubeIdForNews(url: String): String? {
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

/**
 * Single Large In-Screen News & Live Broadcast Player.
 * Strictly plays the video from the database link in-screen mode without redirecting to other pages or popups.
 */
@Composable
fun InScreenLiveNewsPlayer(
    videoUrl: String,
    title: String,
    channelName: String,
    category: String,
    thumbnailUrl: String,
    streamIndex: Int,
    totalStreams: Int,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onPlayToggle: (Boolean) -> Unit = {},
    onNextStream: () -> Unit,
    onPrevStream: () -> Unit
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var hasRenderCrashed by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(videoUrl, reloadTrigger) {
        onDispose {
            try {
                webViewInstance?.apply {
                    onPause()
                    pauseTimers()
                    stopLoading()
                    loadUrl("about:blank")
                    destroy()
                }
            } catch (_: Throwable) {}
            webViewInstance = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B1120))
    ) {
        // Player Header Bar: Live Indicator, Channel, Stream Counter, Sound Toggle & Reload
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Red LIVE indicator with white dot
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MznLiveRed)
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
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "#${streamIndex + 1}/$totalStreams",
                        color = TricolorSaffron,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = channelName,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Actions: Mute/Unmute & Reload
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isMuted) Color.White.copy(alpha = 0.15f) else TricolorSaffron.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, if (isMuted) Color.White.copy(alpha = 0.3f) else TricolorSaffron),
                    modifier = Modifier
                        .clickable {
                            isMuted = !isMuted
                            webViewInstance?.let { wv ->
                                val script = if (isMuted) {
                                    "(function(){ var v=document.querySelector('video'); if(v) v.muted=true; var ifr=document.querySelector('iframe'); if(ifr) ifr.contentWindow.postMessage('{\"event\":\"command\",\"func\":\"mute\",\"args\":\"\"}', '*'); })();"
                                } else {
                                    "(function(){ var v=document.querySelector('video'); if(v) { v.muted=false; v.volume=1.0; } var ifr=document.querySelector('iframe'); if(ifr) ifr.contentWindow.postMessage('{\"event\":\"command\",\"func\":\"unMute\",\"args\":\"\"}', '*'); })();"
                                }
                                wv.evaluateJavascript(script, null)
                            }
                        }
                        .testTag("live_news_sound_toggle_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Video" else "Mute Video",
                            tint = if (isMuted) Color.White else TricolorSaffron,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isMuted) "Muted" else "Sound ON",
                            color = if (isMuted) Color.White else TricolorSaffron,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isPlaying) Color.Black.copy(alpha = 0.5f) else YouTubeRed,
                    modifier = Modifier
                        .clickable { onPlayToggle(!isPlaying) }
                        .testTag("live_news_play_toggle_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Broadcast" else "Play Broadcast",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPlaying) "Pause" else "Play",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable { reloadTrigger++ }
                        .testTag("live_news_reload_btn")
                ) {
                    Box(modifier = Modifier.padding(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Player",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // In-Screen Video Frame (Strictly stays in screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(215.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying && !hasRenderCrashed) {
                key(videoUrl, reloadTrigger) {
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
                                        isBuffering = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isBuffering = false
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        // Strictly stay in-screen only mode: DO NOT REDIRECT!
                                        return true
                                    }

                                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                        try {
                                            view?.let { wv ->
                                                (wv.parent as? ViewGroup)?.removeView(wv)
                                                wv.destroy()
                                            }
                                        } catch (_: Throwable) {}
                                        webViewInstance = null
                                        hasRenderCrashed = true
                                        isBuffering = false
                                        return true
                                    }
                                }

                                val cleanUrl = videoUrl.trim()
                                val effectiveYtId = extractYoutubeIdForNews(cleanUrl)

                                if (effectiveYtId != null) {
                                    val embedHtml = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                            <style>
                                                * { margin: 0; padding: 0; box-sizing: border-box; }
                                                body, html { width: 100%; height: 100%; background: #000; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                                                iframe { width: 100%; height: 100%; border: none; }
                                            </style>
                                        </head>
                                        <body>
                                            <iframe src="https://www.youtube-nocookie.com/embed/$effectiveYtId?autoplay=1&mute=${if (isMuted) 1 else 0}&playsinline=1&controls=1&rel=0&modestbranding=1&enablejsapi=1" 
                                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                                    allowfullscreen></iframe>
                                        </body>
                                        </html>
                                    """.trimIndent()
                                    loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "UTF-8", null)
                                } else {
                                    val videoHtml = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                            <style>
                                                * { margin: 0; padding: 0; box-sizing: border-box; }
                                                body, html { width: 100%; height: 100%; background: #000; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                                                video { width: 100%; height: 100%; max-height: 100%; object-fit: contain; background: #000; }
                                            </style>
                                        </head>
                                        <body>
                                            <video id="newsPlayer" src="$cleanUrl" autoplay ${if (isMuted) "muted" else ""} playsinline controls preload="auto"></video>
                                            <script>
                                                var v = document.getElementById('newsPlayer');
                                                var p = v.play();
                                                if (p !== undefined) {
                                                    p.catch(function() {
                                                        v.muted = true;
                                                        v.play();
                                                    });
                                                }
                                            </script>
                                        </body>
                                        </html>
                                    """.trimIndent()
                                    loadDataWithBaseURL("https://mznlive.com", videoHtml, "text/html", "UTF-8", null)
                                }

                                webViewInstance = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (isBuffering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = TricolorSaffron,
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            } else if (!isPlaying && !hasRenderCrashed) {
                // Interactive In-Screen Poster with One-Tap Live Play
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPlayToggle(true) }
                        .testTag("in_screen_player_poster_box")
                ) {
                    val fallbackThumb = if (thumbnailUrl.isNotBlank()) {
                        thumbnailUrl
                    } else {
                        val effectiveYtId = extractYoutubeIdForNews(videoUrl)
                        if (effectiveYtId != null) "https://img.youtube.com/vi/$effectiveYtId/hqdefault.jpg" else ""
                    }
                    if (fallbackThumb.isNotBlank()) {
                        AsyncImage(
                            model = fallbackThumb,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Center Play Button & Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = YouTubeRed,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Broadcast",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "WATCH LIVE BROADCAST",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Plays strictly in-screen • Tap to watch",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                // Crash recovery fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Live News Ready",
                        tint = TricolorSaffron,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Live Broadcast In-Screen Ready",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            hasRenderCrashed = false
                            reloadTrigger++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reload Stream", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom Info Bar with Channel Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = TricolorSaffron.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        color = TricolorSaffron,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Prev & Next Buttons for Direct Live Stream Switching
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clickable { onPrevStream() }
                        .testTag("live_news_prev_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Stream",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Prev", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TricolorSaffron,
                    modifier = Modifier
                        .clickable { onNextStream() }
                        .testTag("live_news_next_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Next", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Stream",
                            tint = Color.Black,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeDashboardView(
    language: String,
    liveNewsList: List<LiveNewsItem>,
    liveStream: LiveStreamInfo?,
    liveStreams: List<LiveStreamInfo> = emptyList(),
    eventsList: List<EventItem>,
    sponsoredAdverts: List<SponsoredAdvert>,
    currentAdvertIndex: Int = 0,
    zoomedAdvert: SponsoredAdvert? = null,
    activeReel0: InstagramReel? = null,
    activeReel1: InstagramReel? = null,
    activeReel2: InstagramReel? = null,
    allReels: List<InstagramReel> = emptyList(),
    selectedReelCategory: String = "All",
    isRefreshing: Boolean = false,
    supabaseStatus: String = "Database Synced",
    marketplaceShops: List<MarketplaceShop> = emptyList(),
    onToggleLanguage: () -> Unit,
    onSetLanguage: (String) -> Unit = {},
    onAdvertClicked: (SponsoredAdvert) -> Unit,
    onCloseZoomAdvert: () -> Unit,
    onOpenChat: () -> Unit,
    onSelectReelCategory: (String) -> Unit = {},
    onRefreshDatabase: () -> Unit = {},
    onOpenNewsFeed: () -> Unit = {},
    onNavigateToSeller: (MarketplaceShop) -> Unit = {},
    onAddToCart: (PromotionalProduct) -> Unit = {},
    onOpenCart: () -> Unit = {},
    onAddNewReel: ((title: String, shop: String, reelUrl: String, thumbUrl: String, category: String) -> Unit)? = null,
    onAddNewAdvert: ((business: String, title: String, desc: String, price: String, discount: String, imgUrl: String, fbUrl: String, wa: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var showAddReelDialog by remember { mutableStateOf(false) }
    var showAddAdvertDialog by remember { mutableStateOf(false) }

    // In-app media player and image viewer states (keeps user inside app portal)
    var viewedMediaVideo by remember { mutableStateOf<Pair<String, String>?>(null) }
    var viewedMediaImage by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // Selected news story in the large player panel (null = live broadcast stream)
    var activeLeftNewsStory by remember { mutableStateOf<LiveNewsItem?>(null) }
    var viewingNewsDetailModal by remember { mutableStateOf<LiveNewsItem?>(null) }

    val isHindi = language == "hi"
    var selectedMarketCategory by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .emulatorScrollable(scrollState)
            .testTag("home_dashboard_view")
    ) {
        // ====================================================================
        // 1. TOP BAR
        // Strict placement:
        // - Top Left: strictly booked for Mznlive Logo
        // - Middle & Center: Google Ads Banner
        // - Right most: dedicated Chat button between users and shop owners
        // ====================================================================
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP LEFT: App Logo & "MznLive • Home"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("top_logo_area")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.applogo3dtrns),
                        contentDescription = "MznLive Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("top_left_app_logo")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mzn",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorNavy
                            )
                            Text(
                                text = "Live",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorSaffron
                            )
                            Text(
                                text = " • Home",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorGreen
                            )
                        }
                        Text(
                            text = "मुज़फ़्फ़रनगर",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // MIDDLE & CENTER: Google Ads area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com/ads"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .testTag("top_google_ads_banner"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MznAmber)
                                .padding(horizontal = 4.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "Ad",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google Ads",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // TOP RIGHT: 3D Futuristic Language Button matching top-left logo size (38.dp)
                MznLanguageChangeButton(
                    currentLanguage = language,
                    onToggleLanguage = onToggleLanguage,
                    onSelectLanguage = onSetLanguage,
                    buttonSize = 38.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_language_change_button")
                )
            }

            // Subtle Tricolor Accent Line under Top Bar
            TricolorAccentBar(
                modifier = Modifier.fillMaxWidth(),
                height = 2.dp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ====================================================================
        // 2. NEWS SCROLL BAR (Continuous Looping Ticker with Blinking Pointer)
        // Heading at left: Live News button
        // Slowly blinking arrow pointing at live news button with comment "Read All News"
        // Scrolling news items loop continuously
        // Right section: Language change option (only Hindi & English)
        // ====================================================================
        val tickerListState = rememberLazyListState()

        // Continuous loop auto-scroll for news ticker
        LaunchedEffect(liveNewsList) {
            if (liveNewsList.isNotEmpty()) {
                while (true) {
                    try {
                        tickerListState.scrollBy(1.2f)
                    } catch (_: Exception) {
                        // Resilient against user touch interruption
                    }
                    delay(16L) // ~60 FPS smooth scrolling
                }
            }
        }

        // Slowly blinking arrow pointing at Live News button with comment "Read All News"
        val infiniteTransition = rememberInfiniteTransition(label = "LiveNewsTickerTransition")
        val tickerBlinkAlpha by infiniteTransition.animateFloat(
            initialValue = 0.22f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 950, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tickerBlinkAlpha"
        )
        val tickerArrowNudge by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 3.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tickerArrowNudge"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .testTag("news_ticker_bar"),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Heading: Live News button (tap to open full feed)
                Surface(
                    onClick = onOpenNewsFeed,
                    shape = RoundedCornerShape(6.dp),
                    color = MznLiveRed,
                    modifier = Modifier.testTag("news_headline_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "लाइव न्यूज़" else "Live News",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Slowly blinking arrow pointing at Live News button with comment "Read All News"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MznLiveRed.copy(alpha = 0.10f))
                        .clickable { onOpenNewsFeed() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .alpha(tickerBlinkAlpha)
                        .testTag("read_all_news_pointer")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Read All News",
                        tint = MznLiveRed,
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = (-tickerArrowNudge).dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Read All News",
                        color = MznLiveRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Continuous Looping News Scroller Line
                val newsCount = liveNewsList.size
                LazyRow(
                    state = tickerListState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (newsCount > 0) {
                        items(count = Int.MAX_VALUE) { index ->
                            val news = liveNewsList[index % newsCount]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    activeLeftNewsStory = news
                                }
                            ) {
                                if (news.isBreaking) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MznLiveRed)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("BREAKING", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (isHindi) news.titleHi else news.titleEn,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${news.sourceNewspaper})",
                                    fontSize = 10.sp,
                                    color = TricolorNavy,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ====================================================================
        // 2B. CURRENT OFFERS SCROLL BAR (Continuous Looping Ticker with Blinking Pointer)
        // Same component like the scroll bar as 10 live news, arranged below it
        // ====================================================================
        val offersTickerListState = rememberLazyListState()
        var selectedOfferDialog by remember { mutableStateOf<CurrentOfferData?>(null) }

        val localCurrentOffers = remember {
            listOf(
                CurrentOfferData(
                    id = "off_1",
                    titleEn = "Flat 30% Festive Discount on Ethnic & Party Sarees",
                    titleHi = "पार्टी व ब्राइडल साड़ियों पर फ्लैट 30% उत्सव छूट",
                    shopName = "Sharma Sarees",
                    tag = "FESTIVE DEAL",
                    badgeColor = Color(0xFFE65100),
                    details = "Get flat 30% discount on all designer sarees, lehengas & bridal dupattas. Valid for limited days at Shiv Chowk, Muzaffarnagar."
                ),
                CurrentOfferData(
                    id = "off_2",
                    titleEn = "Buy 1 Get 1 Free on all Cold Brews & Thick Shakes",
                    titleHi = "सभी कोल्ड ब्रू व थिक शेक पर 1 खरीदें 1 मुफ्त पाएं",
                    shopName = "Cafe Royale",
                    tag = "BOGO FREE",
                    badgeColor = Color(0xFF00897B),
                    details = "Enjoy buy 1 get 1 free on all handcrafted cold coffees, frappes and Belgian chocolate shakes on Roorkee Road."
                ),
                CurrentOfferData(
                    id = "off_3",
                    titleEn = "₹5,000 Instant Exchange Bonus on 5G Smartphones",
                    titleHi = "5G स्मार्टफोन्स पर ₹5,000 का त्वरित एक्सचेंज बोनस",
                    shopName = "Mzn Mobile Hub",
                    tag = "HOT DEAL",
                    badgeColor = Color(0xFFD81B60),
                    details = "Upgrade your phone today with ₹5000 instant discount and zero-cost EMI on all top brands."
                ),
                CurrentOfferData(
                    id = "off_4",
                    titleEn = "Flat 20% Cashback on Fresh Groceries above ₹999",
                    titleHi = "₹999 से अधिक की ताज़ा किराना खरीद पर 20% कैशबैक",
                    shopName = "Daily Fresh Supermart",
                    tag = "SUPER SAVER",
                    badgeColor = Color(0xFF43A047),
                    details = "Save big on pulses, spices, oils and daily household essentials with doorstep delivery."
                ),
                CurrentOfferData(
                    id = "off_5",
                    titleEn = "Up to 50% OFF Mega Sale on Smart 4K TVs & Inverter ACs",
                    titleHi = "स्मार्ट 4K टीवी व इन्वर्टर एसी पर 50% तक महा सेल",
                    shopName = "Verma Electronics",
                    tag = "MEGA SALE",
                    badgeColor = Color(0xFF1E88E5),
                    details = "Unbeatable prices on Samsung, LG and Sony smart televisions with free installation."
                ),
                CurrentOfferData(
                    id = "off_6",
                    titleEn = "Weekend Feast: 2 Large Cheese Pizzas + Garlic Bread @ ₹499",
                    titleHi = "वीकेंड स्पेशल: 2 बड़े चीज़ पिज्जा + गार्लिक ब्रेड मात्र ₹499",
                    shopName = "Pizza Treat",
                    tag = "FOOD COMBO",
                    badgeColor = Color(0xFFFB8C00),
                    details = "Delight your taste buds with cheesy loaded pizzas and garlic bread with dip."
                ),
                CurrentOfferData(
                    id = "off_7",
                    titleEn = "0% Making Charges on Hallmarked Gold & Solitaire Jewellery",
                    titleHi = "हॉलमार्क सोने व सॉलिटेयर आभूषणों पर 0% मेकिंग चार्ज",
                    shopName = "Kalyan Jewellers",
                    tag = "JEWELLERY",
                    badgeColor = Color(0xFFFFB300),
                    details = "Exclusive certified bridal collection with zero making charges during the wedding season."
                ),
                CurrentOfferData(
                    id = "off_8",
                    titleEn = "Flat 40% Clearance OFF on Branded Running & Sports Shoes",
                    titleHi = "ब्रांडेड रनिंग और स्पोर्ट्स शूज पर फ्लैट 40% क्लीयरेंस छूट",
                    shopName = "Metro Footwear",
                    tag = "CLEARANCE",
                    badgeColor = Color(0xFF8E24AA),
                    details = "Original branded sneakers and athletic footwear at half price at Bhagat Singh Road."
                ),
                CurrentOfferData(
                    id = "off_9",
                    titleEn = "Free Full Body Vital Checkup + 15% OFF on Medicines",
                    titleHi = "मुफ्त वाइटल जांच + सभी दवाओं पर 15% की छूट",
                    shopName = "Apollo Pharmacy",
                    tag = "HEALTHCARE",
                    badgeColor = Color(0xFF00ACC1),
                    details = "Free blood pressure & sugar screening along with flat 15% discount on genuine prescription medicines."
                ),
                CurrentOfferData(
                    id = "off_10",
                    titleEn = "Complete Car Foam Wash & High-Gloss Wax Polish @ ₹299",
                    titleHi = "कार फोम वॉश व हाई-ग्लॉस वैक्स पॉलिश मात्र ₹299 में",
                    shopName = "Speed Clean Spa",
                    tag = "AUTO SPA",
                    badgeColor = Color(0xFF3949AB),
                    details = "Give your car a showroom shine with triple-layer foam wash and interior vacuuming."
                )
            )
        }

        // Auto-scroll loop for Current Offers ticker
        LaunchedEffect(localCurrentOffers) {
            if (localCurrentOffers.isNotEmpty()) {
                while (true) {
                    try {
                        offersTickerListState.scrollBy(1.2f)
                    } catch (_: Exception) {}
                    delay(16L) // ~60 FPS smooth scrolling
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .testTag("current_offers_bar"),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Heading: Current Offers button
                Surface(
                    onClick = {
                        selectedOfferDialog = localCurrentOffers.firstOrNull()
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = TricolorSaffronDark,
                    modifier = Modifier.testTag("current_offers_headline_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "करंट ऑफर्स" else "Current Offers",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Slowly blinking arrow pointing at Current Offers button with comment "Grab Deals"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TricolorSaffronDark.copy(alpha = 0.12f))
                        .clickable { selectedOfferDialog = localCurrentOffers.firstOrNull() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .alpha(tickerBlinkAlpha)
                        .testTag("grab_deals_pointer")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Grab Deals",
                        tint = TricolorSaffronDark,
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = (-tickerArrowNudge).dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isHindi) "ऑफर देखें" else "Grab Deals",
                        color = TricolorSaffronDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Continuous Looping Offers Scroller Line
                val offersCount = localCurrentOffers.size
                LazyRow(
                    state = offersTickerListState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (offersCount > 0) {
                        items(count = Int.MAX_VALUE) { index ->
                            val offer = localCurrentOffers[index % offersCount]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    selectedOfferDialog = offer
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(offer.badgeColor)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = offer.tag,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isHindi) offer.titleHi else offer.titleEn,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${offer.shopName})",
                                    fontSize = 10.sp,
                                    color = TricolorGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Deal details dialog when user taps any offer
        if (selectedOfferDialog != null) {
            val offer = selectedOfferDialog!!
            AlertDialog(
                onDismissRequest = { selectedOfferDialog = null },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TricolorSaffron.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = TricolorSaffronDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (isHindi) offer.titleHi else offer.titleEn,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorNavy,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = offer.badgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, offer.badgeColor)
                        ) {
                            Text(
                                text = "🏷️ ${offer.tag} • ${offer.shopName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = offer.badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = offer.details,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedOfferDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffronDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ====================================================================
        // 3. LIVE NEWS & BROADCAST SECTION (10 Supabase Videos & Transfer Layout)
        // - 10 Sample Videos from Supabase public.live_stream
        // - Large Screen: Video auto-changes randomly every 3 seconds to access all videos
        // - News runs in the large screen ONLY when user clicks on any news
        // - Clicking any of the 3 side panel news blocks transfers that news to the large screen
        // - Layout swap option (Large Screen on Left or Right)
        // - All 10 videos are playable and accessible via direct tap
        // ====================================================================
        val fallback10Streams = remember {
            listOf(
                LiveStreamInfo(
                    id = 1,
                    titleEn = "Mznlive 24x7 Prime News Bulletin - Ground Report",
                    titleHi = "एमजेडएन लाइव 24x7 मुख्य समाचार बुलेटिन - ग्राउंड रिपोर्ट",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=5qap5aO4i9A",
                    youtubeVideoId = "5qap5aO4i9A",
                    channelName = "Mznlive 24x7 News Channel",
                    category = "News",
                    thumbnailUrl = "https://img.youtube.com/vi/5qap5aO4i9A/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 5420
                ),
                LiveStreamInfo(
                    id = 2,
                    titleEn = "Muzaffarnagar Smart City Development & Infrastructure Special",
                    titleHi = "मुजफ्फरनगर स्मार्ट सिटी विकास और बुनियादी ढांचा विशेष रिपोर्ट",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=jfKfPfyJRdk",
                    youtubeVideoId = "jfKfPfyJRdk",
                    channelName = "UP City Samachar",
                    category = "Development",
                    thumbnailUrl = "https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 3850
                ),
                LiveStreamInfo(
                    id = 3,
                    titleEn = "Western UP Kisan Mahapanchayat & Agricultural Mandi Live",
                    titleHi = "पश्चिमी यूपी किसान महापंचायत एवं कृषि मंडी लाइव कवरेज",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=21X5lGlDOfg",
                    youtubeVideoId = "21X5lGlDOfg",
                    channelName = "Kisan Bharat News",
                    category = "Agriculture",
                    thumbnailUrl = "https://img.youtube.com/vi/21X5lGlDOfg/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 6120
                ),
                LiveStreamInfo(
                    id = 4,
                    titleEn = "District Sports & Youth Talent Championship Finals",
                    titleHi = "जिला खेल एवं युवा प्रतिभा चैंपियनशिप फाइनल मुकाबला",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=DWcJFNfaw9c",
                    youtubeVideoId = "DWcJFNfaw9c",
                    channelName = "UP Sports Live",
                    category = "Sports",
                    thumbnailUrl = "https://img.youtube.com/vi/DWcJFNfaw9c/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 2940
                ),
                LiveStreamInfo(
                    id = 5,
                    titleEn = "City Traffic & New Bypass Highway Flyover Inspection Report",
                    titleHi = "शहर का नया बाईपास हाईवे और फ्लाईओवर निरीक्षण ग्राउंड रिपोर्ट",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=sP-IDy3tq-E",
                    youtubeVideoId = "sP-IDy3tq-E",
                    channelName = "Mzn Traffic Watch",
                    category = "Civic",
                    thumbnailUrl = "https://img.youtube.com/vi/sP-IDy3tq-E/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 4100
                ),
                LiveStreamInfo(
                    id = 6,
                    titleEn = "Historic Shukratal Heritage & Ganga Ghat Special Aarti",
                    titleHi = "ऐतिहासिक शुक्रताल तीर्थ एवं गंगा आरती विशेष दर्शन",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=7NOSDKb0HlU",
                    youtubeVideoId = "7NOSDKb0HlU",
                    channelName = "Dharmik Darshan Live",
                    category = "Culture",
                    thumbnailUrl = "https://img.youtube.com/vi/7NOSDKb0HlU/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 7200
                ),
                LiveStreamInfo(
                    id = 7,
                    titleEn = "Local Textile & Handloom Bazaar Festive Shopping Buzz",
                    titleHi = "लोकल हैंडलूम व कपड़ा बाजार में त्योहारी रौनक की लाइव रिपोर्ट",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=ysz5S6PUM-U",
                    youtubeVideoId = "ysz5S6PUM-U",
                    channelName = "Vyapar Darpan",
                    category = "Business",
                    thumbnailUrl = "https://img.youtube.com/vi/ysz5S6PUM-U/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 3180
                ),
                LiveStreamInfo(
                    id = 8,
                    titleEn = "Health & Medical College Super-Specialty Wing Inauguration",
                    titleHi = "मेडिकल कॉलेज में नए सुपर-स्पेशलिटी विंग का शुभारंभ",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
                    youtubeVideoId = "kJQP7kiw5Fk",
                    channelName = "Swasthya Bharat",
                    category = "Health",
                    thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 4500
                ),
                LiveStreamInfo(
                    id = 9,
                    titleEn = "Police Administration Cyber Crime Awareness & Security Briefing",
                    titleHi = "साइबर अपराध सुरक्षा व नागरिक जागरूकता पर पुलिस ब्रीफिंग",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=aqz-KE-bpKQ",
                    youtubeVideoId = "aqz-KE-bpKQ",
                    channelName = "Suraksha Manch Live",
                    category = "Safety",
                    thumbnailUrl = "https://img.youtube.com/vi/aqz-KE-bpKQ/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 2650
                ),
                LiveStreamInfo(
                    id = 10,
                    titleEn = "Weather & Monsoon Forecast: District Rainfall & Crop Advisory",
                    titleHi = "मौसम बुलेटिन: मुजफ्फरनगर एवं आसपास के जिलों में बारिश का अलर्ट",
                    youtubeVideoUrl = "https://www.youtube.com/watch?v=L_LUpnjgPso",
                    youtubeVideoId = "L_LUpnjgPso",
                    channelName = "Mausam Live 24",
                    category = "Weather",
                    thumbnailUrl = "https://img.youtube.com/vi/L_LUpnjgPso/hqdefault.jpg",
                    isLive = true,
                    viewersCount = 5800
                )
            )
        }

        val availableLiveStreams = remember(liveStreams, liveStream, fallback10Streams) {
            if (liveStreams.size >= 10) {
                liveStreams
            } else if (liveStreams.isNotEmpty()) {
                val combined = liveStreams.toMutableList()
                for (fb in fallback10Streams) {
                    if (combined.none { it.youtubeVideoId == fb.youtubeVideoId }) {
                        combined.add(fb)
                    }
                }
                combined
            } else if (liveStream != null) {
                listOf(liveStream) + fallback10Streams.drop(1)
            } else {
                fallback10Streams
            }
        }

        var currentLiveVideoIndex by remember { mutableIntStateOf(0) }
        var isLiveVideoPlaying by remember { mutableStateOf(false) }

        val safeVideoIndex = currentLiveVideoIndex.coerceIn(0, (availableLiveStreams.size - 1).coerceAtLeast(0))
        val activeLiveStreamItem = availableLiveStreams.getOrNull(safeVideoIndex) ?: availableLiveStreams.first()
        val ytUrl = activeLiveStreamItem.youtubeVideoUrl
        val liveTitle = if (isHindi) activeLiveStreamItem.titleHi else activeLiveStreamItem.titleEn
        val thumb = if (activeLiveStreamItem.thumbnailUrl.isNotBlank()) activeLiveStreamItem.thumbnailUrl else "https://img.youtube.com/vi/${activeLiveStreamItem.youtubeVideoId}/hqdefault.jpg"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("live_news_video_section")
        ) {
            // Section Header: Title, In-Screen Live Badge, and Watching Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MznLiveRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "लाइव न्यूज़ बुलेटिन व वीडियो" else "Live News & Video Broadcast",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = TricolorGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "IN-SCREEN",
                            color = TricolorGreen,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(YouTubeRed.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${activeLiveStreamItem.viewersCount} Watching",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = YouTubeRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SINGLE LARGE NEWS SCREEN (Plays strictly in in-screen only mode without redirecting)
            InScreenLiveNewsPlayer(
                videoUrl = ytUrl,
                title = if (activeLeftNewsStory != null) {
                    if (isHindi) activeLeftNewsStory!!.titleHi else activeLeftNewsStory!!.titleEn
                } else {
                    liveTitle
                },
                channelName = if (activeLeftNewsStory != null) {
                    activeLeftNewsStory!!.sourceNewspaper
                } else {
                    activeLiveStreamItem.channelName
                },
                category = if (activeLeftNewsStory != null) {
                    activeLeftNewsStory!!.category
                } else {
                    activeLiveStreamItem.category
                },
                thumbnailUrl = thumb,
                streamIndex = safeVideoIndex,
                totalStreams = availableLiveStreams.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("large_news_player_panel"),
                isPlaying = isLiveVideoPlaying || activeLeftNewsStory != null,
                onPlayToggle = { playing ->
                    isLiveVideoPlaying = playing
                    if (!playing) activeLeftNewsStory = null
                },
                onNextStream = {
                    currentLiveVideoIndex = (safeVideoIndex + 1) % availableLiveStreams.size.coerceAtLeast(1)
                    activeLeftNewsStory = null
                    isLiveVideoPlaying = true
                },
                onPrevStream = {
                    val count = availableLiveStreams.size.coerceAtLeast(1)
                    currentLiveVideoIndex = (safeVideoIndex - 1 + count) % count
                    activeLeftNewsStory = null
                    isLiveVideoPlaying = true
                }
            )

            if (activeLeftNewsStory != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) activeLeftNewsStory!!.titleHi else activeLeftNewsStory!!.titleEn,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${activeLeftNewsStory!!.sourceNewspaper} • ${activeLeftNewsStory!!.publishedTime}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = { activeLeftNewsStory = null },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Reset", fontSize = 11.sp, color = MznLiveRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ============================================================
            // 10 SAMPLE PLAYABLE LIVE STREAM VIDEOS ROW (Direct Access)
            // Allows user to click any video to play immediately & switch large screen
            // ============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "सभी 10 लाइव वीडियो (सुपाबेस डाटाबेस से • चलाने हेतु टैप करें)" else "All 10 Live Streams (Supabase • Tap to Play)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${availableLiveStreams.size} Videos",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val liveStreamsListState = rememberLazyListState()
            LazyRow(
                state = liveStreamsListState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .emulatorScrollable(liveStreamsListState)
                    .testTag("all_10_live_streams_carousel")
            ) {
                items(availableLiveStreams.size) { idx ->
                    val stream = availableLiveStreams[idx]
                    val isCurrent = idx == safeVideoIndex && activeLeftNewsStory == null
                    val cardTitle = if (isHindi) stream.titleHi else stream.titleEn
                    val cardThumb = if (stream.thumbnailUrl.isNotBlank()) stream.thumbnailUrl else "https://img.youtube.com/vi/${stream.youtubeVideoId}/hqdefault.jpg"

                    Card(
                        modifier = Modifier
                            .width(135.dp)
                            .height(110.dp)
                            .clickable {
                                currentLiveVideoIndex = idx
                                activeLeftNewsStory = null
                                isLiveVideoPlaying = true
                            }
                            .testTag("live_stream_chip_$idx"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) TricolorSaffron.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 0.5.dp,
                            color = if (isCurrent) TricolorSaffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(62.dp)
                            ) {
                                AsyncImage(
                                    model = cardThumb,
                                    contentDescription = cardTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Video badge & category
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp)
                                        .align(Alignment.TopStart),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = if (isCurrent) TricolorSaffron else Color.Black.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(3.dp)
                                    ) {
                                        Text(
                                            text = "#${idx + 1}",
                                            color = if (isCurrent) Color.Black else Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    Surface(
                                        color = MznLiveRed,
                                        shape = RoundedCornerShape(3.dp)
                                    ) {
                                        Text(
                                            text = stream.category.uppercase(),
                                            color = Color.White,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                // Center play icon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.60f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cardTitle,
                                    fontSize = 8.5.sp,
                                    lineHeight = 10.5.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stream.channelName,
                                        fontSize = 7.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "▶ Play",
                                        fontSize = 7.5.sp,
                                        color = if (isCurrent) TricolorSaffron else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ====================================================================
        // 4. EVENTS SCROLL BAR
        // Heading at left: “Events”
        // Local events, community program or local problems from database
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("events_section")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Events",
                        tint = MznCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "स्थानीय कार्यक्रम व समस्याएं" else "Events & Community",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Admin Verified",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Events Horizontal Scroll Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(eventsList) { event ->
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable {
                                viewedMediaVideo = Pair(
                                    event.facebookEventUrl,
                                    if (isHindi) event.titleHi else event.titleEn
                                )
                            }
                            .testTag("event_card_${event.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (event.category.contains("Problem", ignoreCase = true)) Color(0xFFFFEBEE) else Color(0xFFE0F2FE))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = event.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (event.category.contains("Problem", ignoreCase = true)) Color(0xFFC62828) else Color(0xFF0284C7)
                                    )
                                }
                                Text(
                                    text = event.eventDate,
                                    fontSize = 10.sp,
                                    color = MznCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isHindi) event.titleHi else event.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = event.location,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        ShareUtils.shareText(
                                            context = context,
                                            title = if (isHindi) event.titleHi else event.titleEn,
                                            message = "${if (isHindi) event.titleHi else event.titleEn}\nDate: ${event.eventDate}\nLocation: ${event.location}",
                                            linkUrl = event.facebookEventUrl
                                        )
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("share_event_${event.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share Event",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ====================================================================
        // 5. SPONSORSHIP & ADVERT AREA (INTERACTIVE IMAGE SLIDER)
        // - Interactive image slider for local products or brands (paid adverts)
        // - Horizontal swipeable carousel with auto-sliding & touch-aware pause
        // - Page indicators, navigation chevrons, WhatsApp CTA, and Zoom modal
        // - Database-driven updates with "+ Promote Brand" submission dialog
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("sponsorship_sliding_section")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Sponsorship",
                        tint = MznAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "प्रायोजित विज्ञापन (लोकल ब्रांड्स)" else "Sponsored Local Brands",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Database-driven promote button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = TricolorSaffron.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, TricolorSaffron),
                        modifier = Modifier
                            .clickable { showAddAdvertDialog = true }
                            .testTag("btn_promote_brand")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Promote Brand",
                                tint = TricolorSaffronDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindi) "+ विज्ञापन जोड़ें" else "+ Promote Brand",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorSaffronDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (sponsoredAdverts.isNotEmpty()) {
                val advertPagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { sponsoredAdverts.size }
                )

                // Auto-advance slider coroutine (pauses when touching or when zoom dialog is active)
                LaunchedEffect(advertPagerState.currentPage, zoomedAdvert, sponsoredAdverts.size, advertPagerState.isScrollInProgress) {
                    if (zoomedAdvert == null && sponsoredAdverts.isNotEmpty() && !advertPagerState.isScrollInProgress) {
                        delay(3500)
                        val nextPage = (advertPagerState.currentPage + 1) % sponsoredAdverts.size
                        advertPagerState.animateScrollToPage(nextPage)
                    }
                }

                // Interactive Image Slider Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("sliding_advert_card"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Horizontal Pager Image Slider
                        HorizontalPager(
                            state = advertPagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val advert = sponsoredAdverts[page % sponsoredAdverts.size]

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onAdvertClicked(advert) }
                            ) {
                                AsyncImage(
                                    model = advert.imageUrl,
                                    contentDescription = advert.productTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Dark gradient scrim for legibility
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )

                                // Top bar overlay: Brand chip & Discount badge
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = TricolorSaffron,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "SPONSORED PROMOTION",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (advert.discountTag.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF00C853))
                                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = advert.discountTag,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        // Slide counter indicator (e.g. 1/4)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "${page + 1}/${sponsoredAdverts.size}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // Bottom Details & Quick CTA Bar
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = advert.businessName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MznAmber
                                            )
                                            Text(
                                                text = advert.productTitle,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (advert.price.isNotBlank()) {
                                                Text(
                                                    text = advert.price,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF00E676)
                                                )
                                            }
                                        }

                                        // Action buttons group
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // WhatsApp direct order button
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF25D366),
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clickable {
                                                        try {
                                                            val cleanWa = advert.whatsappContact.replace(Regex("[^0-9]"), "")
                                                            val waUrl = "https://wa.me/$cleanWa?text=Hi, I am interested in ${advert.productTitle} seen on Mznlive!"
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                                            context.startActivity(intent)
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, "Contact: ${advert.whatsappContact}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                                        contentDescription = "WhatsApp",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // Zoom in button
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.White.copy(alpha = 0.25f),
                                                modifier = Modifier.clickable { onAdvertClicked(advert) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ZoomIn,
                                                        contentDescription = "Zoom In",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "Zoom",
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Left Navigation Chevron Overlay
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 6.dp)
                                .size(30.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        val prev = (advertPagerState.currentPage - 1 + sponsoredAdverts.size) % sponsoredAdverts.size
                                        advertPagerState.animateScrollToPage(prev)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Slide",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Right Navigation Chevron Overlay
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 6.dp)
                                .size(30.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        val next = (advertPagerState.currentPage + 1) % sponsoredAdverts.size
                                        advertPagerState.animateScrollToPage(next)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Slide",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Modern Slider Page Indicator Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sponsoredAdverts.forEachIndexed { index, _ ->
                        val isSelected = advertPagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(5.dp)
                                .width(if (isSelected) 22.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isSelected) TricolorSaffron else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        advertPagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
            }
        }

        // ====================================================================
        // ZOOMED IN ADVERT MODAL DIALOG
        // User clicked on sliding image -> zooms in and shows full details
        // User clicks again -> zooms out and sliding resumes
        // ====================================================================
        if (zoomedAdvert != null) {
            AlertDialog(
                onDismissRequest = onCloseZoomAdvert,
                confirmButton = {
                    Button(
                        onClick = onCloseZoomAdvert,
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron)
                    ) {
                        Text("Zoom Out & Resume", color = Color.White)
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // WhatsApp button
                        if (zoomedAdvert.whatsappContact.isNotBlank()) {
                            Button(
                                onClick = {
                                    try {
                                        val cleanWa = zoomedAdvert.whatsappContact.replace(Regex("[^0-9]"), "")
                                        val waUrl = "https://wa.me/$cleanWa?text=Hi, I am interested in ${zoomedAdvert.productTitle} seen on Mznlive!"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Contact: ${zoomedAdvert.whatsappContact}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", color = Color.White)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (zoomedAdvert.facebookPageUrl.isNotBlank()) {
                                    viewedMediaVideo = Pair(zoomedAdvert.facebookPageUrl, "${zoomedAdvert.businessName} • ${zoomedAdvert.productTitle}")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Page")
                        }

                        // Share Advert Button
                        OutlinedButton(
                            onClick = {
                                ShareUtils.shareText(
                                    context = context,
                                    title = zoomedAdvert.productTitle,
                                    message = "${zoomedAdvert.businessName}\n${zoomedAdvert.productDescription}\nPrice: ${zoomedAdvert.price}",
                                    linkUrl = zoomedAdvert.facebookPageUrl.ifBlank { null }
                                )
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Advert", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share")
                        }
                    }
                },
                title = {
                    Column {
                        Text(
                            text = zoomedAdvert.businessName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorNavy
                        )
                        Text(
                            text = zoomedAdvert.productTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCloseZoomAdvert() } // Click once again to zoom out
                    ) {
                        AsyncImage(
                            model = zoomedAdvert.imageUrl,
                            contentDescription = zoomedAdvert.productTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = zoomedAdvert.price,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorSaffron
                            )
                            if (zoomedAdvert.discountTag.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = zoomedAdvert.discountTag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = zoomedAdvert.productDescription,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "💡 Tap image again to zoom out and resume auto-sliding.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ====================================================================
        // 6. INSTAGRAM REELS PROMOTIONS (THREE-COLUMN GRID LAYOUT)
        // - Three-column grid layout displaying promotional Instagram Reels
        // - Exact 9:16 aspect ratio vertical cards with video preview aesthetics
        // - Category filter pills for targeted local content exploration
        // - Database-driven content updates with live sync & "+ Promote Reel" dialog
        // - Direct Instagram app or web launch on reel tap
        // ====================================================================
        val fallbackReels = listOfNotNull(activeReel0, activeReel1, activeReel2)
        val effectiveReels = allReels.ifEmpty { fallbackReels }

        val reelCategories = listOf("All", "Trending", "Shops & Crafts", "Food & Dining", "Fashion & Style")
        val filteredReels = when (selectedReelCategory) {
            "All" -> effectiveReels
            "Trending" -> effectiveReels.filter {
                it.likesCount.contains("K") || (it.likesCount.replace("K", "").toDoubleOrNull() ?: 0.0) >= 4.0
            }.ifEmpty { effectiveReels }
            "Shops & Crafts" -> effectiveReels.filter {
                it.category.contains("Craft", ignoreCase = true) || it.title.contains("Art", ignoreCase = true) || it.shopName.contains("Artisan", ignoreCase = true) || it.title.contains("Decor", ignoreCase = true)
            }.ifEmpty { effectiveReels }
            "Food & Dining" -> effectiveReels.filter {
                it.category.contains("Food", ignoreCase = true) || it.title.contains("Kulcha", ignoreCase = true) || it.shopName.contains("Zayka", ignoreCase = true)
            }.ifEmpty { effectiveReels }
            "Fashion & Style" -> effectiveReels.filter {
                it.category.contains("Fashion", ignoreCase = true) || it.title.contains("Footwear", ignoreCase = true) || it.title.contains("Lehenga", ignoreCase = true) || it.shopName.contains("StepStyle", ignoreCase = true) || it.shopName.contains("Boutique", ignoreCase = true)
            }.ifEmpty { effectiveReels }
            else -> effectiveReels
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("instagram_reels_section")
        ) {
            // Header with Instagram Branding, DB Status Badge, Refresh, and Promote Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(InstagramGradientStart, InstagramGradientMiddle, InstagramGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Instagram Reels",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isHindi) "इंस्टाग्राम रील्स प्रमोशन" else "Instagram Reels Showcase",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00C853))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live DB: ${effectiveReels.size} Active Reels",
                                fontSize = 10.sp,
                                color = Color(0xFF00C853),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Database Refresh Button
                    IconButton(
                        onClick = { onRefreshDatabase() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("btn_refresh_database")
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "RefreshSpin")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "SpinAngle"
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Database",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .then(if (isRefreshing) Modifier.rotate(angle) else Modifier)
                        )
                    }

                    // Promote Reel Button (+ Promote with Instagram Gradient)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(InstagramGradientStart, InstagramGradientMiddle, InstagramGradientEnd)
                                )
                            )
                            .clickable { showAddReelDialog = true }
                            .testTag("btn_promote_reel")
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.Transparent)
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Reel",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isHindi) "+ रील जोड़ें" else "+ Promote",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Pills Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(reelCategories) { category ->
                    val isSelected = selectedReelCategory == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) TricolorNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { onSelectReelCategory(category) }
                            .testTag("reel_filter_${category.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ====================================================================
            // THREE-COLUMN GRID LAYOUT (9:16 Aspect Ratio)
            // Displays reels chunked into 3 equal columns per row
            // Dynamic multi-row support for database-driven content
            // ====================================================================
            val reelRows = filteredReels.chunked(3)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("three_column_reels_grid"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reelRows.forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (colIndex in 0 until 3) {
                            val reel = rowItems.getOrNull(colIndex)
                            if (reel != null) {
                                ReelCardItem(
                                    modifier = Modifier.weight(1f),
                                    reel = reel,
                                    slotNumber = rowIndex * 3 + colIndex + 1,
                                    onPlayClick = {
                                        viewedMediaVideo = Pair(
                                            reel.reelUrl,
                                            "${reel.shopName} • ${reel.title}"
                                        )
                                    },
                                    onShareClick = {
                                        ShareUtils.shareReelOrVideo(
                                            context = context,
                                            title = reel.title,
                                            creator = reel.shopName,
                                            videoUrl = reel.reelUrl
                                        )
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ====================================================================
        // 7. LOCAL BUSINESS MARKETPLACE (Retail, Services, Restaurants, etc.)
        // Horizontal Category Filter Bar & Verified Local Businesses
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("home_business_marketplace_section")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(TricolorNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Business Marketplace",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (isHindi) "व्यापार बाज़ार (मार्केटप्लेस)" else "Business Marketplace",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isHindi) "रिटेल, सेवाएं एवं रेस्टोरेंट ब्राउज़ करें" else "Browse local Retail, Services & Restaurants",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Filter Bar
            MarketplaceHorizontalFilterBar(
                categories = defaultMarketplaceCategories,
                selectedCategory = selectedMarketCategory,
                onSelectCategory = { selectedMarketCategory = it },
                shops = marketplaceShops,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val homeFilteredShops = marketplaceShops.filter { shop ->
                shopMatchesCategory(shop, selectedMarketCategory)
            }

            if (homeFilteredShops.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_marketplace_shops_carousel")
                ) {
                    items(homeFilteredShops) { shop ->
                        MarketplaceCompactCard(
                            shop = shop,
                            onShopClick = { onNavigateToSeller(shop) },
                            onChatClick = { onOpenChat() }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ====================================================================
        // 8. GOOGLE ADS AREA BLOCK
        // Below reels, dedicated Google Ads container block
        // ====================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clickable {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com/ads"))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
                .testTag("bottom_google_ads_block"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MznAmber)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Google Ad",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Text(
                        text = "Sponsored • Google AdMob Network",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdsClick,
                            contentDescription = "Ad",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Promote Your Business on Mznlive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Reach 50,000+ local customers every day with reels & banner ads.",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com/ads"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Visit", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ====================================================================
    // DATABASE-DRIVEN DIALOG: ADD NEW INSTAGRAM REEL
    // ====================================================================
    if (showAddReelDialog) {
        AddInstagramReelDialog(
            onDismiss = { showAddReelDialog = false },
            onSubmit = { title, shop, reelUrl, thumbUrl, category ->
                onAddNewReel?.invoke(title, shop, reelUrl, thumbUrl, category)
                showAddReelDialog = false
                Toast.makeText(context, "Reel submitted to database!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ====================================================================
    // DATABASE-DRIVEN DIALOG: ADD NEW SPONSORED ADVERT
    // ====================================================================
    if (showAddAdvertDialog) {
        AddSponsoredAdvertDialog(
            onDismiss = { showAddAdvertDialog = false },
            onSubmit = { bName, pTitle, pDesc, pPrice, pDisc, pImg, pFb, pWa ->
                onAddNewAdvert?.invoke(bName, pTitle, pDesc, pPrice, pDisc, pImg, pFb, pWa)
                showAddAdvertDialog = false
                Toast.makeText(context, "Advert submitted to database!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ====================================================================
    // IN-APP VIDEO PLAYER DIALOG (YouTube Live Stream, Reels, Facebook Videos)
    // Retains user in app portal without launching external apps
    // ====================================================================
    if (viewedMediaVideo != null) {
        val (url, title) = viewedMediaVideo!!
        InAppVideoPlayerDialog(
            mediaUrl = url,
            title = title,
            availableShops = marketplaceShops,
            onDismiss = { viewedMediaVideo = null },
            onBack = { viewedMediaVideo = null },
            onShare = {
                ShareUtils.shareReelOrVideo(
                    context = context,
                    title = title,
                    creator = "MznLive Stream",
                    videoUrl = url
                )
            },
            onBuyClick = { product ->
                onAddToCart(product)
                onOpenCart()
            },
            onGoToSeller = { shop ->
                viewedMediaVideo = null
                onNavigateToSeller(shop)
            }
        )
    }

    // ====================================================================
    // IN-APP IMAGE VIEWER DIALOG (Adverts, Events, Products)
    // ====================================================================
    if (viewedMediaImage != null) {
        val (url, title, subtitle) = viewedMediaImage!!
        InAppImageViewerDialog(
            imageUrl = url,
            title = title,
            subtitle = subtitle,
            onDismiss = { viewedMediaImage = null },
            onShare = {
                ShareUtils.shareText(
                    context = context,
                    title = title,
                    message = "$title\n$subtitle",
                    linkUrl = url
                )
            }
        )
    }

    // ====================================================================
    // IN-APP NEWS ARTICLE DETAIL DIALOG (For Shifted Story Reading)
    // ====================================================================
    if (viewingNewsDetailModal != null) {
        val story = viewingNewsDetailModal!!
        Dialog(onDismissRequest = { viewingNewsDetailModal = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("news_detail_modal_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (story.isBreaking) MznLiveRed else TricolorNavy,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (story.isBreaking) "BREAKING NEWS" else story.category.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewingNewsDetailModal = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // News image
                    AsyncImage(
                        model = story.imageUrl,
                        contentDescription = story.titleEn,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Source & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = story.sourceNewspaper,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorSaffron
                        )
                        Text(
                            text = story.publishedTime,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = if (isHindi) story.titleHi else story.titleEn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Body
                    Text(
                        text = if (isHindi) story.descriptionHi else story.descriptionEn,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewingNewsDetailModal = null
                                onOpenNewsFeed()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("All News", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewingNewsDetailModal = null
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(story.websiteUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onOpenNewsFeed()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy)
                        ) {
                            Text("Read Source", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 9:16 ASPECT RATIO INSTAGRAM REEL CARD (Three-Column Grid Item)
// ====================================================================
@Composable
fun ReelCardItem(
    modifier: Modifier = Modifier,
    reel: InstagramReel?,
    slotNumber: Int,
    onPlayClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val context = LocalContext.current

    if (reel != null && reel.reelUrl.isNotBlank()) {
        // Active Reel with muted auto-playing in-screen preview
        InlineMutedMediaPreviewPlayer(
            mediaUrl = reel.reelUrl,
            thumbnailUrl = reel.thumbnailUrl,
            title = reel.title,
            badgeLabel = "#$slotNumber",
            aspectRatio = 9f / 16f,
            modifier = modifier
                .aspectRatio(9f / 16f)
                .testTag("reel_slot_$slotNumber"),
            onClick = { onPlayClick() },
            topEndContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Views / Likes Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Views",
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = reel.likesCount.ifBlank { "2.5K" },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Share icon button
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { onShareClick() }
                            .testTag("share_reel_btn_$slotNumber"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Reel",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    // Instagram Mini Camera Icon
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(InstagramGradientStart, InstagramGradientMiddle, InstagramGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "IG",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            },
            bottomContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.90f))
                            )
                        )
                        .padding(6.dp)
                ) {
                    Column {
                        Text(
                            text = reel.shopName.ifBlank { "Local Merchant" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MznAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = reel.title.ifBlank { "Promotional Reel" },
                            fontSize = 9.sp,
                            color = Color.White,
                            maxLines = 1,
                            lineHeight = 11.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = TricolorSaffron,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Tap for sound",
                                color = TricolorSaffron,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        )
    } else {
        // Fallback or Empty Slot Card
        Card(
            modifier = modifier
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onPlayClick() }
                .testTag("reel_slot_$slotNumber"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1B4B)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(InstagramGradientStart, InstagramGradientMiddle, InstagramGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reel Slot $slotNumber",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Slot #$slotNumber",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Add Reel",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ====================================================================
// DIALOG: ADD NEW INSTAGRAM REEL (Database-driven updates)
// ====================================================================
@Composable
fun AddInstagramReelDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, shop: String, reelUrl: String, thumbUrl: String, category: String) -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var reelUrl by remember { mutableStateOf("") }
    var thumbUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Shops & Crafts") }

    val presetThumbs = listOf(
        Pair("Brass Lamp", "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500&auto=format&fit=crop&q=80"),
        Pair("Local Food", "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop&q=80"),
        Pair("Fashion", "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500&auto=format&fit=crop&q=80"),
        Pair("Footwear", "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500&auto=format&fit=crop&q=80")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(InstagramGradientStart, InstagramGradientMiddle, InstagramGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Instagram Reel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Broadcast your promotional reel across the three-column grid.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop / Brand Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reel Caption / Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reelUrl,
                    onValueChange = { reelUrl = it },
                    label = { Text("Instagram Reel URL") },
                    placeholder = { Text("https://instagram.com/reel/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = thumbUrl,
                    onValueChange = { thumbUrl = it },
                    label = { Text("Thumbnail Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick preset thumbnail selector
                Text("Or select quick preset photo:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetThumbs.forEach { (label, url) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (thumbUrl == url) TricolorSaffron else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { thumbUrl = url }
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                color = if (thumbUrl == url) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                // Category selection
                Text("Category:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Shops & Crafts", "Food & Dining", "Fashion & Style").forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (category == cat) TricolorNavy else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = cat.split(" ").first(),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Exciting Local Offer" }
                    val finalShop = shopName.ifBlank { "Local Store" }
                    val finalReel = reelUrl.ifBlank { "https://instagram.com" }
                    val finalThumb = thumbUrl.ifBlank { presetThumbs.first().second }
                    onSubmit(finalTitle, finalShop, finalReel, finalThumb, category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron)
            ) {
                Text("Save to Database", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ====================================================================
// DIALOG: ADD NEW SPONSORED ADVERT (Database-driven updates)
// ====================================================================
@Composable
fun AddSponsoredAdvertDialog(
    onDismiss: () -> Unit,
    onSubmit: (business: String, title: String, desc: String, price: String, discount: String, imgUrl: String, fbUrl: String, wa: String) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var productTitle by remember { mutableStateOf("") }
    var productDesc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var discountTag by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var fbPageUrl by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }

    val presetImages = listOf(
        Pair("Electronics", "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=700&auto=format&fit=crop&q=80"),
        Pair("Sarees", "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=700&auto=format&fit=crop&q=80"),
        Pair("Sweets", "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=700&auto=format&fit=crop&q=80"),
        Pair("Dairy", "https://images.unsplash.com/photo-1527153857715-3908f2ae5e81?w=700&auto=format&fit=crop&q=80")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = TricolorSaffron)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Sponsored Advert", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Feature your business banner on the home screen image slider.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business / Shop Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = productTitle,
                    onValueChange = { productTitle = it },
                    label = { Text("Product / Service Headline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = productDesc,
                    onValueChange = { productDesc = it },
                    label = { Text("Description & Features") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (e.g. ₹999)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountTag,
                        onValueChange = { discountTag = it },
                        label = { Text("Discount Tag (e.g. 20% OFF)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text("WhatsApp Contact (+91...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fbPageUrl,
                    onValueChange = { fbPageUrl = it },
                    label = { Text("Facebook Page URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Banner Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick preset photo selector
                Text("Or select quick banner photo:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetImages.forEach { (label, url) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (imageUrl == url) TricolorSaffron else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { imageUrl = url }
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                color = if (imageUrl == url) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalBusiness = businessName.ifBlank { "Local Partner" }
                    val finalTitle = productTitle.ifBlank { "Quality Local Products" }
                    val finalDesc = productDesc.ifBlank { "Special festival discount for all residents." }
                    val finalPrice = price.ifBlank { "Best Price" }
                    val finalDiscount = discountTag.ifBlank { "Special Offer" }
                    val finalImg = imageUrl.ifBlank { presetImages.first().second }
                    val finalFb = fbPageUrl.ifBlank { "https://facebook.com/mznlive" }
                    val finalWa = whatsappNumber.ifBlank { "+919876543210" }

                    onSubmit(finalBusiness, finalTitle, finalDesc, finalPrice, finalDiscount, finalImg, finalFb, finalWa)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron)
            ) {
                Text("Save to Database", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class CurrentOfferData(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val shopName: String,
    val tag: String,
    val badgeColor: Color,
    val details: String
)
