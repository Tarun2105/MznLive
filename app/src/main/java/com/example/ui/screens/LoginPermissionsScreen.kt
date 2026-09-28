package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.components.InAppVideoPlayerDialog
import com.example.ui.theme.*
import com.example.util.emulatorScrollable

@Composable
fun LoginPermissionsScreen(
    videoUrl: String,
    locationGranted: Boolean,
    phoneGranted: Boolean,
    notificationsGranted: Boolean,
    cameraGranted: Boolean,
    onLocationPermissionResult: (Boolean) -> Unit,
    onPhonePermissionResult: (Boolean) -> Unit,
    onNotificationsPermissionResult: (Boolean) -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit,
    onProceedToHome: () -> Unit
) {
    val context = LocalContext.current
    val effectiveVideoUrl = remember(videoUrl) {
        val trimmed = videoUrl.trim()
        if (trimmed.isNotBlank() && !trimmed.contains("ForBiggerBlazes.mp4")) {
            trimmed
        } else {
            "https://iyppuawgyelubfohxthj.supabase.co/storage/v1/object/public/MznLive/MznlivepermissionVideo.mp4"
        }
    }
    var isFullscreenModalOpen by remember { mutableStateOf(false) }

    // System Permission Launchers
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        onLocationPermissionResult(fine || coarse)
    }

    val phoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPhonePermissionResult(isGranted)
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onNotificationsPermissionResult(isGranted)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onCameraPermissionResult(isGranted)
    }

    // Master launcher to allow all permissions in one click
    val allPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        onLocationPermissionResult(fine || coarse || locationGranted)

        val phone = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
        onPhonePermissionResult(phone || phoneGranted)

        val camera = permissions[Manifest.permission.CAMERA] ?: false
        onCameraPermissionResult(camera || cameraGranted)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notif = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            onNotificationsPermissionResult(notif || notificationsGranted)
        } else {
            onNotificationsPermissionResult(true)
        }
    }

    val grantedCount = (if (locationGranted) 1 else 0) +
            (if (phoneGranted) 1 else 0) +
            (if (notificationsGranted) 1 else 0) +
            (if (cameraGranted) 1 else 0)

    val allGranted = grantedCount == 4

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("login_permissions_screen"),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .emulatorScrollable(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with App Logo at Top Left
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.applogo3dtrns),
                    contentDescription = "MznLive Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("top_left_app_logo")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mzn",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TricolorNavy
                        )
                        Text(
                            text = "Live",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TricolorSaffron
                        )
                        Text(
                            text = " • Permissions",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorGreen
                        )
                    }
                    Text(
                        text = "First-Time App Setup & Permissions",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Indian Tricolor Accent Line
            TricolorAccentBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp)),
                height = 2.5.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // In-Screen Mode Video Player (Starts once in-screen when screen popped up)
            InScreenLoginVideoPlayer(
                videoUrl = effectiveVideoUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("ai_video_card"),
                onExpandInApp = { isFullscreenModalOpen = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Permissions Progress & Status Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (allGranted) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                ),
                border = BorderStroke(
                    1.dp,
                    if (allGranted) MznGreen.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Required App Permissions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (allGranted) "All features unlocked! Ready to enter." else "$grantedCount of 4 permissions allowed",
                                fontSize = 12.sp,
                                color = if (allGranted) MznGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (allGranted) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (!allGranted) {
                            Button(
                                onClick = {
                                    val list = mutableListOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.CAMERA
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        list.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    allPermissionsLauncher.launch(list.toTypedArray())
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("allow_all_permissions_button")
                            ) {
                                Text("Allow All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MznGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ready",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MznGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { grantedCount / 4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (allGranted) MznGreen else TricolorSaffron,
                        trackColor = Color(0xFFE2E8F0),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Permission Card 1: Location Access
            PermissionItemCard(
                title = "Location Access",
                description = "For real-time local breaking news, nearby verified marketplace shops & emergency updates.",
                icon = Icons.Default.LocationOn,
                isGranted = locationGranted,
                buttonTag = "location_permission_button",
                onAllowClick = {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            // Permission Card 2: Phone & Telephony Access
            PermissionItemCard(
                title = "Phone & Call State",
                description = "For seamless one-tap calling to local shops, merchant WhatsApp assistance & vendor contacts.",
                icon = Icons.Default.Phone,
                isGranted = phoneGranted,
                buttonTag = "phone_permission_button",
                onAllowClick = {
                    phoneLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                }
            )

            // Permission Card 3: Push Notifications
            PermissionItemCard(
                title = "Push Notifications",
                description = "For urgent breaking news headlines, live broadcast alerts, flash deals and cart status updates.",
                icon = Icons.Default.NotificationsActive,
                isGranted = notificationsGranted,
                buttonTag = "notifications_permission_button",
                onAllowClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onNotificationsPermissionResult(true)
                    }
                }
            )

            // Permission Card 4: Camera & Storefront Photos
            PermissionItemCard(
                title = "Camera & Storefront Photos",
                description = "For capturing shop storefront photos, attaching product images and verifying seller profiles.",
                icon = Icons.Default.CameraAlt,
                isGranted = cameraGranted,
                buttonTag = "camera_permission_button",
                onAllowClick = {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Master Action Button: Save & Redirect to Home Screen
            Button(
                onClick = onProceedToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_to_home_button")
                    .testTag("enter_as_guest_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allGranted) MznGreen else TricolorNavy,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(
                    imageVector = if (allGranted) Icons.Default.CheckCircle else Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (allGranted) "Permissions Saved • Enter Home" else "Save Permissions & Continue to Home",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Permissions are securely saved for future app use and can be changed anytime in your device Settings.",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

    if (isFullscreenModalOpen) {
        InAppVideoPlayerDialog(
            mediaUrl = effectiveVideoUrl,
            title = "Why Mznlive Asks For Permissions",
            sourceLabel = "Permission Guide",
            onDismiss = { isFullscreenModalOpen = false }
        )
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    buttonTag: String,
    onAllowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isGranted) MznGreen.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MznGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                    contentDescription = title,
                    tint = if (isGranted) MznGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isGranted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✓",
                            color = MznGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAllowClick,
                enabled = !isGranted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) MznGreen else TricolorNavy,
                    disabledContainerColor = MznGreen.copy(alpha = 0.18f),
                    disabledContentColor = MznGreen
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag(buttonTag)
            ) {
                Text(
                    text = if (isGranted) "Allowed" else "Allow",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * In-Screen Video Player for Permissions Screen.
 * Uses native Android VideoView and MediaPlayer for hardware-efficient, crash-proof playback.
 */
@Composable
fun InScreenLoginVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onExpandInApp: () -> Unit
) {
    val cleanUrl = remember(videoUrl) {
        val trimmed = videoUrl.trim()
        if (trimmed.isNotBlank() && !trimmed.contains("ForBiggerBlazes.mp4")) {
            trimmed
        } else {
            "https://iyppuawgyelubfohxthj.supabase.co/storage/v1/object/public/MznLive/MznlivepermissionVideo.mp4"
        }
    }

    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerInstance by remember { mutableStateOf<MediaPlayer?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableStateOf(0) }

    DisposableEffect(cleanUrl, reloadTrigger) {
        onDispose {
            try {
                videoViewInstance?.stopPlayback()
            } catch (_: Throwable) {}
            videoViewInstance = null
            mediaPlayerInstance = null
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar of In-Screen Player
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFF22C55E) else Color(0xFFEAB308))
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "IN-SCREEN VIDEO",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TricolorSaffron.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "PERMISSION GUIDE",
                            color = TricolorSaffron,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Auto-started once",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.5.sp
                )
            }

            // In-Screen Video View Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (!hasError) {
                    key(cleanUrl, reloadTrigger) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    setVideoURI(Uri.parse(cleanUrl))
                                    setOnPreparedListener { mp ->
                                        mediaPlayerInstance = mp
                                        mp.isLooping = true
                                        if (isMuted) {
                                            mp.setVolume(0f, 0f)
                                        } else {
                                            mp.setVolume(1f, 1f)
                                        }
                                        isBuffering = false
                                        hasError = false
                                        if (isPlaying) {
                                            start()
                                        }
                                    }
                                    setOnInfoListener { _, what, _ ->
                                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                                            isBuffering = true
                                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END || what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                            isBuffering = false
                                        }
                                        true
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        hasError = true
                                        isBuffering = false
                                        true
                                    }
                                    videoViewInstance = this
                                    start()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isBuffering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = TricolorSaffron,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Permission Video Guide",
                            tint = TricolorSaffron,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Permission Video Guide Ready",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    hasError = false
                                    reloadTrigger++
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Reload Video", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onExpandInApp,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Full Player", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // In-Screen Player Bottom Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Replay button
                    Surface(
                        onClick = {
                            videoViewInstance?.seekTo(0)
                            videoViewInstance?.start()
                            isPlaying = true
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Replay in screen",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Replay",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Play/Pause button
                    Surface(
                        onClick = {
                            if (isPlaying) {
                                videoViewInstance?.pause()
                                isPlaying = false
                            } else {
                                videoViewInstance?.start()
                                isPlaying = true
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPlaying) "Pause" else "Play",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Mute/Unmute button
                    Surface(
                        onClick = {
                            val nextMute = !isMuted
                            isMuted = nextMute
                            if (nextMute) {
                                mediaPlayerInstance?.setVolume(0f, 0f)
                            } else {
                                mediaPlayerInstance?.setVolume(1f, 1f)
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMuted) "Unmute" else "Mute",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // In-App Expand (Never leaves app to external browser)
                TextButton(
                    onClick = onExpandInApp,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand In App",
                        tint = TricolorSaffron,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expand",
                        color = TricolorSaffron,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
