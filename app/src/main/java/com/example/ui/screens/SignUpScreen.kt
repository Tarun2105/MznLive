package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.util.emulatorScrollable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Sign Up Screen supporting both "User" and "Shop Owner" registration.
 *
 * Fields included as requested:
 * - Name
 * - Business name
 * - Category of business with dropdown selection bar (Retails, Electronics, Clothing, Mobiles & others)
 * - Address
 * - Mobile number
 * - WhatsApp number
 * - OTP Verification by Mobile number via SMS & WhatsApp
 * - Auto-fill option for OTP to read automatically
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    initialRole: String = "SHOP_OWNER", // "USER" or "SHOP_OWNER"
    language: String = "hi",
    onNavigateBack: () -> Unit,
    onSignUpSuccess: (
        role: String,
        name: String,
        businessName: String,
        category: String,
        address: String,
        mobile: String,
        whatsapp: String
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isHindi = language == "hi"

    BackHandler { onNavigateBack() }

    // Role Selection
    var selectedRole by remember { mutableStateOf(initialRole) } // "USER" or "SHOP_OWNER"
    val isShopOwner = selectedRole == "SHOP_OWNER"

    // Form Fields
    var name by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var sameAsMobile by remember { mutableStateOf(true) }

    // Business Categories List
    val businessCategories = remember {
        listOf(
            "Retails & Kirana (खुदरा और किराना)",
            "Electronics & Appliances (इलेक्ट्रॉनिक्स)",
            "Clothing & Fashion (कपड़े और फैशन)",
            "Mobiles & Accessories (मोबाइल और गैजेट्स)",
            "Food, Sweets & Bakery (खाद्य व मिष्ठान)",
            "Restaurants & Cafes (रेस्टोरेंट और कैफे)",
            "Services & Repair (सेवाएं व मरम्मत)",
            "Handicrafts & Artisans (हस्तशिल्प और कला)",
            "Jewellery & Ornaments (ज्वेलरी व आभूषण)",
            "Health & Pharmacy (स्वास्थ्य व मेडिकल)",
            "Automobile & Spares (वाहन व ऑटोमोबाइल)",
            "Footwear & Bags (जूते व बैग्स)",
            "Hardware & Building Material (हार्डवेयर)",
            "Other Category Listing (अन्य श्रेणी)"
        )
    }

    // OTP Verification State
    var otpDeliveryChannel by remember { mutableStateOf("SMS") } // "SMS" or "WHATSAPP"
    var isOtpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var isOtpVerified by remember { mutableStateOf(false) }
    var isAutoReadingOtp by remember { mutableStateOf(false) }
    var otpTimerSeconds by remember { mutableIntStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Errors
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-fill countdown timer effect
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            otpTimerSeconds = 30
            while (otpTimerSeconds > 0) {
                delay(1000L)
                otpTimerSeconds--
            }
        }
    }

    // SMS Retriever / Auto-fill simulation
    fun triggerAutoFillOtp() {
        if (!isOtpSent) {
            Toast.makeText(context, if (isHindi) "कृपया पहले ओटीपी भेजें" else "Please send OTP first", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            isAutoReadingOtp = true
            Toast.makeText(
                context,
                if (isHindi) "ऑटो-रीड: एसएमएस/व्हाट्सएप से ओटीपी पढ़ा जा रहा है..." else "Auto-reading OTP from SMS/WhatsApp...",
                Toast.LENGTH_SHORT
            ).show()
            delay(1200L)
            enteredOtp = generatedOtp
            isAutoReadingOtp = false
            Toast.makeText(
                context,
                if (isHindi) "ओटीपी सफलतापूर्वक ऑटो-फिल हो गया: $generatedOtp" else "OTP Auto-filled successfully: $generatedOtp",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun sendOtp(channel: String) {
        if (mobileNumber.trim().length != 10) {
            errorMessage = if (isHindi) "कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें" else "Please enter a valid 10-digit mobile number"
            return
        }
        errorMessage = null
        otpDeliveryChannel = channel

        // Generate 6-digit secure OTP
        val randomOtp = (100000 + (Math.random() * 900000).toInt()).toString()
        generatedOtp = randomOtp
        isOtpSent = true
        isOtpVerified = false
        enteredOtp = ""

        val channelLabel = if (channel == "SMS") "SMS" else "WhatsApp"
        Toast.makeText(
            context,
            if (isHindi) "$channelLabel के माध्यम से 6 अंकों का ओटीपी भेजा गया: $randomOtp" else "6-digit OTP sent via $channelLabel: $randomOtp",
            Toast.LENGTH_LONG
        ).show()

        // Auto-read OTP trigger automatically after 2.5 seconds (Simulating Android SMS Retriever API)
        coroutineScope.launch {
            delay(2500L)
            if (enteredOtp.isEmpty() && isOtpSent) {
                enteredOtp = randomOtp
                Toast.makeText(
                    context,
                    if (isHindi) "⚡ ऑटो-रीड: एसएमएस से ओटीपी स्वतः भर दिया गया ($randomOtp)" else "⚡ Auto-read: OTP filled automatically ($randomOtp)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun submitRegistration() {
        if (name.isBlank()) {
            errorMessage = if (isHindi) "कृपया अपना नाम दर्ज करें" else "Please enter your name"
            return
        }
        if (isShopOwner) {
            if (businessName.isBlank()) {
                errorMessage = if (isHindi) "कृपया व्यापार / दुकान का नाम दर्ज करें" else "Please enter business/shop name"
                return
            }
            if (selectedCategory.isBlank()) {
                errorMessage = if (isHindi) "कृपया व्यापार की श्रेणी चुनें" else "Please select business category"
                return
            }
        }
        if (address.isBlank()) {
            errorMessage = if (isHindi) "कृपया पता दर्ज करें" else "Please enter address"
            return
        }
        if (mobileNumber.trim().length != 10) {
            errorMessage = if (isHindi) "कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें" else "Please enter a valid 10-digit mobile number"
            return
        }
        val finalWhatsapp = if (sameAsMobile) mobileNumber else whatsappNumber
        if (finalWhatsapp.trim().length != 10) {
            errorMessage = if (isHindi) "कृपया 10 अंकों का वैध व्हाट्सएप नंबर दर्ज करें" else "Please enter valid 10-digit WhatsApp number"
            return
        }
        if (!isOtpSent) {
            errorMessage = if (isHindi) "कृपया पहले मोबाइल नंबर पर ओटीपी भेजें" else "Please send OTP to verify mobile number"
            return
        }
        if (enteredOtp != generatedOtp) {
            errorMessage = if (isHindi) "दर्ज किया गया ओटीपी अमान्य है" else "Invalid OTP entered"
            return
        }

        errorMessage = null
        isOtpVerified = true
        showSuccessDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.applogo3dtrns),
                            contentDescription = "MznLive Logo",
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHindi) "एमजेडएन लाइव साइन अप" else "MznLive Sign Up",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorNavy
                            )
                            Text(
                                text = if (isShopOwner) {
                                    if (isHindi) "दुकान व व्यापारी पंजीकरण" else "Shop Owner Registration"
                                } else {
                                    if (isHindi) "उपयोगकर्ता खाता पंजीकरण" else "User Account Registration"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("signup_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TricolorNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .emulatorScrollable(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("sign_up_screen")
        ) {
            // Elegant Indian Tricolor Accent Line
            TricolorAccentBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp)),
                height = 3.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Role Selector Toggle (User vs Shop Owner)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // User Option
                    Surface(
                        onClick = { selectedRole = "USER" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isShopOwner) Color(0xFF001433) else Color.Transparent,
                        border = if (!isShopOwner) BorderStroke(1.5.dp, Color(0xFF00E5FF)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_role_user")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (!isShopOwner) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "यूजर साइन अप" else "Sign Up as User",
                                fontSize = 12.sp,
                                fontWeight = if (!isShopOwner) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isShopOwner) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Shop Owner Option
                    Surface(
                        onClick = { selectedRole = "SHOP_OWNER" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isShopOwner) Color(0xFF001433) else Color.Transparent,
                        border = if (isShopOwner) BorderStroke(1.5.dp, Color(0xFFFF9933)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_role_shop_owner")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = if (isShopOwner) Color(0xFFFF9933) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "दुकानदार साइन अप" else "Sign Up as Shop Owner",
                                fontSize = 12.sp,
                                fontWeight = if (isShopOwner) FontWeight.Bold else FontWeight.Medium,
                                color = if (isShopOwner) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error banner if any
            if (errorMessage != null) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage ?: "", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ================= FORM FIELDS =================
            // 1. Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (isHindi) "पूरा नाम *" else "Full Name *") },
                placeholder = { Text(if (isHindi) "अपना नाम दर्ज करें" else "Enter your full name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TricolorNavy) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Business Name (Required for Shop Owner, optional for User)
            AnimatedVisibility(visible = isShopOwner) {
                Column {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text(if (isHindi) "व्यापार / दुकान का नाम *" else "Business / Shop Name *") },
                        placeholder = { Text(if (isHindi) "जैसे: शर्मा साड़ी सेंटर, मुजफ्फरनगर" else "e.g. Sharma Saree Centre, Muzaffarnagar") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = TricolorSaffronDark) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("business_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 3. Category of the Business with Dropdown Selection Bar
            AnimatedVisibility(visible = isShopOwner) {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isHindi) "व्यापार की श्रेणी (Category) *" else "Business Category *") },
                            placeholder = { Text(if (isHindi) "श्रेणी चुनें (जैसे: Retails, Electronics...)" else "Select Category (Retails, Electronics...)") },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = TricolorGreen) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("category_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            businessCategories.forEach { categoryItem ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = categoryItem,
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedCategory == categoryItem) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedCategory == categoryItem) TricolorNavy else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = categoryItem
                                        categoryDropdownExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 4. Address Field
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = {
                    Text(
                        if (isShopOwner) {
                            if (isHindi) "दुकान / शोरूम का पूरा पता *" else "Shop / Store Address *"
                        } else {
                            if (isHindi) "पूरा पता / मोहल्ला *" else "Full Address / Locality *"
                        }
                    )
                },
                placeholder = {
                    Text(
                        if (isShopOwner) {
                            if (isHindi) "दुकान नंबर, बाजार, शिव चौक, मुजफ्फरनगर" else "Shop No., Market Road, Shiv Chowk, Muzaffarnagar"
                        } else {
                            if (isHindi) "मकान नंबर, मोहल्ला / कॉलोनी, मुजफ्फरनगर" else "House No., Colony / Area, Muzaffarnagar"
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE53935)) },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("address_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Mobile Number Field
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) mobileNumber = it },
                label = { Text(if (isHindi) "मोबाइल नंबर (+91) *" else "Mobile Number (+91) *") },
                placeholder = { Text("10 Digit Mobile Number") },
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                    ) {
                        Text(
                            text = "+91",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TricolorNavy
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6. WhatsApp Number with Same as Mobile checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { sameAsMobile = !sameAsMobile }
                    .padding(vertical = 2.dp)
            ) {
                Checkbox(
                    checked = sameAsMobile,
                    onCheckedChange = { sameAsMobile = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF25D366))
                )
                Text(
                    text = if (isHindi) "व्हाट्सएप नंबर मोबाइल नंबर जैसा ही है" else "WhatsApp number is same as mobile number",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(visible = !sameAsMobile) {
                Column {
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) whatsappNumber = it },
                        label = { Text(if (isHindi) "व्हाट्सएप नंबर (+91) *" else "WhatsApp Number (+91) *") },
                        placeholder = { Text("10 Digit WhatsApp Number") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.stat_notify_chat),
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= 7. OTP VERIFICATION SECTION =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, if (isOtpVerified) Color(0xFF10B981) else Color(0xFF00E5FF).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isOtpVerified) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF00E5FF).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOtpVerified) Icons.Default.CheckCircle else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isOtpVerified) Color(0xFF10B981) else Color(0xFF0072FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "मोबाइल नंबर सत्यापन (OTP)" else "Mobile Number OTP Verification",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorNavy
                            )
                        }

                        if (isOtpVerified) {
                            Text(
                                text = "VERIFIED",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isHindi) "ओटीपी माध्यम चुनें (SMS या WhatsApp):" else "Choose OTP verification channel (SMS or WhatsApp):",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons to send OTP via SMS or WhatsApp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Send via SMS
                        Button(
                            onClick = { sendOtp("SMS") },
                            enabled = mobileNumber.trim().length == 10 && otpTimerSeconds == 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0052CC),
                                disabledContainerColor = Color(0xFF0052CC).copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_otp_sms_button")
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "SMS से भेजें" else "Via SMS",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Send via WhatsApp
                        Button(
                            onClick = { sendOtp("WHATSAPP") },
                            enabled = mobileNumber.trim().length == 10 && otpTimerSeconds == 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                disabledContainerColor = Color(0xFF25D366).copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_otp_whatsapp_button")
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.stat_notify_chat),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "WhatsApp से भेजें" else "Via WhatsApp",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // OTP Input & Auto-fill Section
                    if (isOtpSent) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // SMS Auto Read simulation banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0284C7))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "एसएमएस स्वतः पढ़ें (Auto-detect): सक्रिय" else "SMS Auto-detect: Active",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0369A1),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Auto Fill Button
                            OutlinedButton(
                                onClick = { triggerAutoFillOtp() },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("autofill_otp_button")
                            ) {
                                Text(
                                    text = if (isHindi) "✨ ऑटो-फिल (Auto Fill)" else "✨ Auto Fill",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 6-digit OTP Input Field
                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) enteredOtp = it },
                            label = { Text(if (isHindi) "6-अंकों का ओटीपी दर्ज करें" else "Enter 6-Digit OTP") },
                            placeholder = { Text("123456") },
                            leadingIcon = { Icon(Icons.Default.Password, contentDescription = null, tint = TricolorGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (otpTimerSeconds > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isHindi) "ओटीपी पुनः भेजें: ${otpTimerSeconds}s" else "Resend OTP in: ${otpTimerSeconds}s",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= SUBMIT REGISTRATION BUTTON =================
            Button(
                onClick = { submitRegistration() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("verify_registration_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffronDark),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isShopOwner) {
                            if (isHindi) "दुकानदार पंजीकरण पूरा करें" else "Complete Shop Registration"
                        } else {
                            if (isHindi) "उपयोगकर्ता पंजीकरण पूरा करें" else "Complete User Registration"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isHindi) "पंजीकरण सफल रहा!" else "Registration Successful!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TricolorNavy,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isShopOwner) {
                            if (isHindi) {
                                "बधाई हो! आपकी दुकान \"$businessName\" एमजेडएन लाइव पर सफलतापूर्वक पंजीकृत हो गई है। आप अब उत्पादों का प्रचार कर सकते हैं।"
                            } else {
                                "Congratulations! Your shop \"$businessName\" has been successfully registered on MznLive. You can now showcase deals & products."
                            }
                        } else {
                            if (isHindi) {
                                "नमस्ते $name! आपका खाता सफलतापूर्वक सत्यापित हो गया है।"
                            } else {
                                "Hello $name! Your account has been verified successfully."
                            }
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "• Name: $name", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            if (isShopOwner) {
                                Text(text = "• Shop: $businessName", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "• Category: $selectedCategory", fontSize = 11.5.sp)
                            }
                            Text(text = "• Mobile: +91 $mobileNumber (Verified)", fontSize = 11.5.sp, color = TricolorGreen)
                            Text(text = "• WhatsApp: +91 ${if (sameAsMobile) mobileNumber else whatsappNumber}", fontSize = 11.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSignUpSuccess(
                            selectedRole,
                            name,
                            businessName,
                            selectedCategory,
                            address,
                            mobileNumber,
                            if (sameAsMobile) mobileNumber else whatsappNumber
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffronDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dialog_continue_button")
                ) {
                    Text(if (isHindi) "ऐप में आगे बढ़ें" else "Continue to App", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
