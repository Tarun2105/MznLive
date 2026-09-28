package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.UserSessionManager
import com.example.data.repository.CartItem
import com.example.ui.components.MznSignUpActionButton
import com.example.ui.theme.*
import com.example.util.ReceiptStorageHelper
import com.example.util.emulatorScrollable

/**
 * Screen 5: Me Tab
 * Fully upgraded to show profile page with photo upload & edit profile.
 * - When signed up as USER: displays Cart, earned CashPoints rewards, order history & related info.
 * - When signed up as SHOP OWNER: displays 2 prominent banners:
 *   1. Promotion Plan Banner (clicking redirects to Plan page to choose and buy plan)
 *   2. MznLiveVendors App Banner (clicking redirects to the second app named MznLiveVendors)
 */
@Composable
fun MeScreen(
    verifiedPhone: String,
    language: String,
    locationGranted: Boolean,
    phoneGranted: Boolean,
    cartItems: List<CartItem> = emptyList(),
    supabaseStatus: String = "",
    onToggleLanguage: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onOpenCart: () -> Unit = {},
    onNavigateToSignUp: (String) -> Unit = {},
    onOpenDedicatedShop: () -> Unit = {},
    onOpenDataTables: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager(context) }
    val isRegistered by sessionManager.isRegistered.collectAsState()
    val userRole by sessionManager.userRole.collectAsState()
    val registeredName by sessionManager.userName.collectAsState()
    val registeredBusinessName by sessionManager.businessName.collectAsState()
    val registeredCategory by sessionManager.businessCategory.collectAsState()
    val registeredAddress by sessionManager.userAddress.collectAsState()
    val profileImageUri by sessionManager.profileImageUri.collectAsState()
    val cashPoints by sessionManager.cashPoints.collectAsState()
    val isHindi = language == "hi"

    // Dialog state for Editing Profile
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Dialog state for MznLiveVendors app redirect info
    var showVendorAppDialog by remember { mutableStateOf(false) }

    // Photo picker for profile image (Zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedUri = ReceiptStorageHelper.copyUriToInternalStorage(context, uri, "profile_avatar_${System.currentTimeMillis()}.jpg")
            if (savedUri != null) {
                sessionManager.setProfileImageUri(savedUri.toString())
                Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var businessName by remember { mutableStateOf("") }
    var fbPageLink by remember { mutableStateOf("") }
    var igReelLink by remember { mutableStateOf("") }
    var productDesc by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .emulatorScrollable(scrollState)
            .padding(16.dp)
            .testTag("me_screen")
    ) {
        // Top Header with App Logo at Top Left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.applogo3dtrns),
                    contentDescription = "MznLive Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("top_left_app_logo")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mzn",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TricolorNavy
                        )
                        Text(
                            text = "Live",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TricolorSaffron
                        )
                        Text(
                            text = " • Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorGreen
                        )
                    }
                    Text(
                        text = if (isHindi) "यूजर प्रोफाइल व मर्चेंट डैशबोर्ड" else "User Profile & Merchant Hub",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Role Switcher Button (Allows easy toggle between USER and SHOP_OWNER for testing)
            Surface(
                onClick = {
                    val nextRole = if (userRole == "SHOP_OWNER") "USER" else "SHOP_OWNER"
                    sessionManager.saveRegistration(
                        role = nextRole,
                        name = if (registeredName.isNotBlank()) registeredName else (if (nextRole == "SHOP_OWNER") "Sharma Ji" else "Rahul Verma"),
                        businessName = if (nextRole == "SHOP_OWNER") "Sharma General Store" else "",
                        category = if (nextRole == "SHOP_OWNER") "Kirana & General Store" else "",
                        address = if (registeredAddress.isNotBlank()) registeredAddress else "Civil Lines, Muzaffarnagar",
                        mobile = if (verifiedPhone.isNotBlank()) verifiedPhone else "9876543210",
                        whatsapp = "9876543210",
                        imageUri = profileImageUri
                    )
                    Toast.makeText(context, "Switched role to: $nextRole", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(16.dp),
                color = if (userRole == "SHOP_OWNER") TricolorNavy else TricolorSaffronDark,
                modifier = Modifier.testTag("role_switcher_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (userRole == "SHOP_OWNER") Icons.Default.Storefront else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (userRole == "SHOP_OWNER") "Merchant" else "Customer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // ==========================================
        // 1. USER / SHOP OWNER PROFILE CARD WITH UPLOAD & EDIT
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_profile_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                TricolorAccentBar(
                    modifier = Modifier.fillMaxWidth(),
                    height = 3.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar with Upload Camera Button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .border(2.dp, TricolorSaffron, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("profile_avatar_upload_trigger")
                    ) {
                        if (profileImageUri.isNotBlank()) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.applogo3dtrns),
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        }

                        // Floating camera badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp),
                            shape = CircleShape,
                            color = TricolorNavy
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Upload Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (registeredName.isNotBlank()) registeredName else "MznLive Member",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (verifiedPhone.isNotBlank()) verifiedPhone else "+91 9760077767",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (userRole == "SHOP_OWNER" && registeredBusinessName.isNotBlank()) {
                            Text(
                                text = "🏪 $registeredBusinessName (${registeredCategory.ifBlank { "Retail" }})",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TricolorSaffronDark
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = TricolorGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userRole == "SHOP_OWNER") "Verified Muzaffarnagar Merchant" else "OTP Verified Local Customer",
                                fontSize = 11.sp,
                                color = TricolorGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Edit Profile Button
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = TricolorNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 2. CONDITIONAL SECTIONS BASED ON SIGNED-UP ROLE
        // ==========================================

        if (userRole == "USER") {
            // ------------------------------------------
            // A. USER ROLE: Shows Cart, Earned CashPoints & Related Information
            // ------------------------------------------

            // 1. My Cart Section
            UserCartSummaryCard(
                cartItems = cartItems,
                onOpenCart = onOpenCart
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Earned CashPoints Rewards Card
            UserCashPointsCard(
                cashPoints = cashPoints,
                onRedeemClick = {
                    Toast.makeText(context, "₹$cashPoints CashPoints applied to your checkout discount!", Toast.LENGTH_LONG).show()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Related Information Section
            UserRelatedInformationSection(
                address = registeredAddress.ifBlank { "Civil Lines, Near Clock Tower, Muzaffarnagar" },
                onEditAddress = { showEditProfileDialog = true }
            )

        } else {
            // ------------------------------------------
            // B. SHOP OWNER ROLE: Shows 2 Interactive Banners
            //    Banner 1 -> Promotion Plan page (choose and buy plan)
            //    Banner 2 -> Second app MznLiveVendors
            // ------------------------------------------

            // BANNER 1: Shop Promotion Plan Banner (Redirects to Plan Page)
            ShopOwnerPromotionPlanBanner(
                onClick = onNavigateToPlan,
                testTag = "banner_promotion_plan"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // BANNER 2: Second App MznLiveVendors Banner (Redirects to MznLiveVendors)
            ShopOwnerVendorsAppBanner(
                onClick = {
                    try {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.aistudio.mznlivevendors")
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            showVendorAppDialog = true
                        }
                    } catch (e: Exception) {
                        showVendorAppDialog = true
                    }
                },
                testTag = "banner_mznlive_vendors_app"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Shop Management Quick Actions
            ShopOwnerManagementSection(
                businessName = registeredBusinessName.ifBlank { "My Muzaffarnagar Store" },
                onOpenDedicatedShop = onOpenDedicatedShop
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Live SQLite Schema & Data Tables Inspector Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable { onOpenDataTables() }
                .testTag("me_open_sql_tables_btn")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = TricolorSaffronDark,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SQLite Database & Transaction Tables",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TricolorNavy
                        )
                        Text(
                            text = "Live inspection of Room DB users, payments & products",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
            }
        }

        // Account Registration Buttons (if user wants to re-register)
        Text(
            text = if (isHindi) "साइन अप व खाता विकल्प" else "Account & Registration Options",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        MznSignUpActionButton(
            title = if (isHindi) "यूजर के रूप में साइन अप करें" else "Sign Up as User",
            subtitle = if (isHindi) "स्थानीय ऑफर्स, कार्ट व कैशपॉइंट्स के लिए पर्सनल खाता" else "Personal account for shopping, cart & CashPoints",
            roleTag = "Personal User",
            isShopOwner = false,
            isAlreadyRegistered = isRegistered && userRole == "USER",
            onClick = { onNavigateToSignUp("USER") },
            testTag = "me_signup_as_user_button"
        )

        Spacer(modifier = Modifier.height(8.dp))

        MznSignUpActionButton(
            title = if (isHindi) "दुकानदार / मर्चेंट साइन अप" else "Sign Up as Shop Owner",
            subtitle = if (isHindi) "दुकान जोड़ें, प्रमोशन प्लान लें व विक्रेता ऐप से जुड़ें" else "Register shop, buy promotion plans & connect to vendor app",
            roleTag = "Merchant",
            isShopOwner = true,
            isAlreadyRegistered = isRegistered && userRole == "SHOP_OWNER",
            onClick = { onNavigateToSignUp("SHOP_OWNER") },
            testTag = "me_signup_as_shop_owner_button"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Device Permissions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Device Permissions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Location Access", fontSize = 12.5.sp)
                    }
                    Text(if (locationGranted) "Allowed" else "Not Allowed", color = if (locationGranted) MznGreen else MznAmber, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Phone & OTP Auto-read", fontSize = 12.5.sp)
                    }
                    Text(if (phoneGranted) "Allowed" else "Not Allowed", color = if (phoneGranted) MznGreen else MznAmber, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Promotion Links
        Text(
            text = "Submit External Promotion Links",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Featured on homepage after verification",
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Shop / Business Name") },
            placeholder = { Text("e.g. Royal Heritage Sarees") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fbPageLink,
            onValueChange = { fbPageLink = it },
            label = { Text("Facebook / Web Page Link") },
            placeholder = { Text("https://facebook.com/yourbrand") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = FacebookBlue) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = igReelLink,
            onValueChange = { igReelLink = it },
            label = { Text("Instagram Reel Link (9:16)") },
            placeholder = { Text("https://instagram.com/reel/...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, tint = InstagramGradientMiddle) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = productDesc,
            onValueChange = { productDesc = it },
            label = { Text("Product & Offer Details") },
            placeholder = { Text("Special discounts, pricing, and details...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (businessName.isBlank() || (fbPageLink.isBlank() && igReelLink.isBlank())) {
                    Toast.makeText(context, "Please enter business name and at least one link", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Submitted! Our admin team will review and approve.", Toast.LENGTH_LONG).show()
                    businessName = ""
                    fbPageLink = ""
                    igReelLink = ""
                    productDesc = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_ad_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MznCrimson),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Submit for Promotion", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout / Reset
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MznCrimson),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Re-test First-Time Login & Permissions Screen", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Powered By TalntVibe@2026" Button with App Logo
        Button(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://talntvibe.wordpress.com"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Redirecting to https://talntvibe.wordpress.com", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("powered_by_talntvibe_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF001A3A),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.applogo3dtrns),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Powered By TalntVibe@2026",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "https://talntvibe.wordpress.com",
                        fontSize = 10.5.sp,
                        color = TricolorSaffron
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Redirect to website",
                    tint = TricolorSaffron,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ==========================================
    // DIALOG: EDIT PROFILE FUNCTION
    // ==========================================
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = registeredName,
            currentMobile = verifiedPhone,
            currentAddress = registeredAddress,
            currentBusinessName = registeredBusinessName,
            currentCategory = registeredCategory,
            isShopOwner = userRole == "SHOP_OWNER",
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, mobile, address, bName, cat ->
                sessionManager.updateProfile(
                    name = name,
                    mobile = mobile,
                    address = address,
                    businessName = bName,
                    category = cat
                )
                showEditProfileDialog = false
                Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ==========================================
    // DIALOG: MZNLIVE VENDORS APP REDIRECT INFO
    // ==========================================
    if (showVendorAppDialog) {
        MznLiveVendorsAppDialog(
            shopName = registeredBusinessName.ifBlank { "My Muzaffarnagar Store" },
            onDismiss = { showVendorAppDialog = false }
        )
    }
}

/**
 * Cart section for USER role: displays current items count, total, and checkout shortcut.
 */
@Composable
fun UserCartSummaryCard(
    cartItems: List<CartItem>,
    onOpenCart: () -> Unit
) {
    val totalAmount = cartItems.sumOf { it.product.priceInr * it.quantity }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_cart_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TricolorGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = TricolorGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("My Shopping Cart", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TricolorNavy)
                        Text(
                            text = if (cartItems.isEmpty()) "Cart is empty" else "${cartItems.size} items • Total: ₹$totalAmount",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onOpenCart,
                    colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (cartItems.isEmpty()) "View Cart" else "Checkout (₹$totalAmount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (cartItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.product.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(item.product.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("₹${item.product.priceInr} x ${item.quantity}", fontSize = 10.sp, color = TricolorGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Earned CashPoints Rewards Card for USER role.
 */
@Composable
fun UserCashPointsCard(
    cashPoints: Int,
    onRedeemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_cash_points_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFFDE68A))
                    )
                )
                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD97706),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("MznLive CashPoints Balance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            Text("1 CashPoint = ₹1 Direct Cash Discount", fontSize = 11.sp, color = Color(0xFFB45309))
                        }
                    }

                    Text(
                        text = "$cashPoints Pts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF92400E)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Breakdown pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CashPointPill(label = "🎁 Welcome Bonus", pts = "+100", modifier = Modifier.weight(1f))
                    CashPointPill(label = "🛍️ Local Purchases", pts = "+150", modifier = Modifier.weight(1f))
                    CashPointPill(label = "⭐ Reviews Given", pts = "+100", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Worth ₹$cashPoints applicable on any local store checkout",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F),
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = onRedeemClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply to Cart", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CashPointPill(label: String, pts: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.7f),
        border = BorderStroke(0.5.dp, Color(0xFFF59E0B))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFF92400E), maxLines = 1)
            Text(pts, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
        }
    }
}

/**
 * Related information for USER role: recent order, saved address, referral code.
 */
@Composable
fun UserRelatedInformationSection(
    address: String,
    onEditAddress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Saved Information & Orders", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TricolorNavy)

            Spacer(modifier = Modifier.height(10.dp))

            // Saved Address
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditAddress() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = TricolorSaffron, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Primary Delivery Address", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(address, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Recent Purchase Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TricolorGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Recent Local Order: #ORD-77291", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Banarasi Pure Silk Saree • ₹1,899 • Delivered from Royal Heritage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Refer & Earn 50 CashPoints
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = TricolorNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Refer Friends to MznLive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Earn +50 CashPoints per signup (Code: MZNLIVE50)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * BANNER 1 FOR SHOP OWNER:
 * "if shopowner want to get promotion plan he has to click first image after this he is redirected
 * to the plan page where he/she can choose the plan and can buy the plan"
 */
@Composable
fun ShopOwnerPromotionPlanBanner(
    onClick: () -> Unit,
    testTag: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF001B3A), Color(0xFF003366), Color(0xFF004C87))
                    )
                )
                .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFF59E0B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⭐ PROMOTION PLANS • PLANS START @ ₹11",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFCD34D), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Top Feed Boost", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🚀 Promote Your Shop on MznLive",
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Reach 50,000+ local buyers across Muzaffarnagar! Get top sliding advert, verified merchant tick & direct WhatsApp customer leads.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.88f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Micro ₹11 • Weekly ₹399 • VIP ₹2,999",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCD34D)
                    )

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Choose Plan & Buy ➜", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
            }
        }
    }
}

/**
 * BANNER 2 FOR SHOP OWNER:
 * "and then below this there should be a second banner using the second image when shop owner
 * clicked redirect the person to the second app name MznLiveVendors that will be discussed in the next prompt"
 */
@Composable
fun ShopOwnerVendorsAppBanner(
    onClick: () -> Unit,
    testTag: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669))
                    )
                )
                .border(1.5.dp, Color(0xFF34D399), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF34D399),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🏪 DEDICATED MERCHANT APP",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF064E3B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("MznLiveVendors", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "📱 Open MznLiveVendors Merchant App",
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Manage your store inventory, live order alerts, barcode billing, customer chat & daily UPI settlement in our second dedicated app.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.88f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Stock • Orders • Billing",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA7F3D0)
                    )

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Launch MznLiveVendors ➜", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF064E3B))
                    }
                }
            }
        }
    }
}

/**
 * Dedicated shop management actions for SHOP OWNER.
 */
@Composable
fun ShopOwnerManagementSection(
    businessName: String,
    onOpenDedicatedShop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Storefront Management", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TricolorNavy)

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDedicatedShop() },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Open Dedicated Shop Management Page", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text("Add products, edit shop details & preview storefront", fontSize = 11.sp, color = Color(0xFF047857))
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Dialog for Editing User & Shop Owner Profile.
 */
@Composable
fun EditProfileDialog(
    currentName: String,
    currentMobile: String,
    currentAddress: String,
    currentBusinessName: String,
    currentCategory: String,
    isShopOwner: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, mobile: String, address: String, businessName: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var mobile by remember { mutableStateOf(currentMobile) }
    var address by remember { mutableStateOf(currentAddress) }
    var businessName by remember { mutableStateOf(currentBusinessName) }
    var category by remember { mutableStateOf(currentCategory) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("edit_profile_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isShopOwner) "Edit Shop Owner Profile" else "Edit User Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorNavy
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isShopOwner) "Shop Address in Muzaffarnagar" else "Delivery Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )

                if (isShopOwner) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Shop / Business Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Business Category") },
                        placeholder = { Text("e.g. Sarees, Electronics, Kirana") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onSave(name, mobile, address, businessName, category) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Informative redirect dialog for the second app: MznLiveVendors.
 */
@Composable
fun MznLiveVendorsAppDialog(
    shopName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("mznlive_vendors_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF047857),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("MznLiveVendors App", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TricolorNavy)
                            Text("Merchant Companion Platform", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Connected Merchant: $shopName", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MznLiveVendors is our dedicated companion app for shop owners. It provides live POS barcode scanning, automated SMS order alerts, live product stock management, and end-of-day UPI settlement.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF047857),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        try {
                            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.aistudio.mznlivevendors"))
                            context.startActivity(marketIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Redirecting to MznLiveVendors Merchant Portal...", Toast.LENGTH_LONG).show()
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch / Download MznLiveVendors", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
