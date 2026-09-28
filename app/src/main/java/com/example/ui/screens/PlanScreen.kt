package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.util.emulatorScrollable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.PromotionPlanItem
import com.example.ui.theme.*

// Pre-defined default plans strictly conforming to user requirements
val DefaultPromotionPlansList = listOf(
    PromotionPlanItem(
        id = "plan_3_days",
        title = "Micro Test Boost",
        durationText = "( 3 days )",
        priceInr = 11,
        priceDisplay = "INR 11 only",
        tagline = "Super Flash Trial Deal",
        description = "Introductory test drive to showcase your shop items on Mznlive feed with real local buyer traffic.",
        badge = "⚡ FLASH DEAL",
        isPopular = false,
        isBestValue = false,
        isFlashDeal = true,
        isCustom = false,
        features = listOf(
            "1 High-visibility sponsored sliding advert",
            "Direct WhatsApp click-to-chat button",
            "Local city feed broadcast for 72 hours",
            "Instant listing in Mznlive Marketplace"
        ),
        graphicType = "flash",
        gradientColors = listOf(0xFFFF5722, 0xFFFF9800)
    ),
    PromotionPlanItem(
        id = "plan_7_days",
        title = "Weekly Flash Sprint",
        durationText = "( 7 Days )",
        priceInr = 399,
        priceDisplay = "INR 399 only",
        tagline = "Rapid Footfall Catalyst",
        description = "Targeted 7-day high impact promotion to drive weekend shoppers and immediate inquiries directly to your store.",
        badge = "🔥 POPULAR WEEKLY",
        isPopular = false,
        isBestValue = false,
        isFlashDeal = false,
        isCustom = false,
        features = listOf(
            "Top rotation in sliding sponsorship banner",
            "1 Instagram Reel promotion slot (9:16 layout)",
            "Direct call & WhatsApp enquiry button",
            "Featured banner in Marketplace directory",
            "Push highlight during peak evening hours"
        ),
        graphicType = "fire",
        gradientColors = listOf(0xFFE91E63, 0xFFFF5722)
    ),
    PromotionPlanItem(
        id = "plan_15_days",
        title = "Bi-Weekly Growth Blast",
        durationText = "( 15 Days )",
        priceInr = 599,
        priceDisplay = "INR 599 only",
        tagline = "Steady Customer Magnet",
        description = "Two weeks of continuous merchant visibility across live news and advert carousels with verified seller badge.",
        badge = "🚀 VALUE ACCELERATOR",
        isPopular = false,
        isBestValue = false,
        isFlashDeal = false,
        isCustom = false,
        features = listOf(
            "Guaranteed 1,500+ daily banner impressions",
            "2 Instagram Reels rotating in 9:16 showcase",
            "Verified Local Merchant Blue Badge",
            "Direct Facebook page & Instagram link",
            "Priority customer lead routing on WhatsApp"
        ),
        graphicType = "rocket",
        gradientColors = listOf(0xFF673AB7, 0xFF3F51B5)
    ),
    PromotionPlanItem(
        id = "plan_30_days",
        title = "Monthly Pro Dominance",
        durationText = "( 30 Days )",
        priceInr = 899,
        priceDisplay = "INR 899 only",
        tagline = "Most Loved by Local Merchants",
        description = "Complete monthly dominance in local search, news broadcasts, and sliding banner rotation across the city.",
        badge = "⭐ MOST POPULAR",
        isPopular = true,
        isBestValue = false,
        isFlashDeal = false,
        isCustom = false,
        features = listOf(
            "Prime top spot in sliding adverts banner",
            "3 Instagram Reels featured in looping grid",
            "Full digital storefront in Marketplace tab",
            "Verified Gold Merchant Badge",
            "Direct lead capture & dedicated chat support",
            "Weekly performance summary report"
        ),
        graphicType = "star",
        gradientColors = listOf(0xFFFF9933, 0xFFFF5722)
    ),
    PromotionPlanItem(
        id = "plan_3_months",
        title = "Quarterly Market Leader",
        durationText = "( 3 Months )",
        priceInr = 1299,
        priceDisplay = "INR 1299 only",
        tagline = "Seasonal Sales Multiplier",
        description = "90 days of sustained presence across festival seasons, summer sales, and city-wide events at just ₹433/mo.",
        badge = "💎 HIGH ROI",
        isPopular = false,
        isBestValue = false,
        isFlashDeal = false,
        isCustom = false,
        features = listOf(
            "Uninterrupted 90-day carousel ad placement",
            "Unlimited rotation of promotional reels",
            "Live news ticker sponsor branding tag",
            "Exclusive festive festival campaign boost",
            "Dedicated merchant manager support",
            "Free graphic design support for banners"
        ),
        graphicType = "diamond",
        gradientColors = listOf(0xFF009688, 0xFF00BCD4)
    ),
    PromotionPlanItem(
        id = "plan_6_months",
        title = "Half-Year Super Saver",
        durationText = "( 6 Months )",
        priceInr = 1599,
        priceDisplay = "INR 1599 only",
        tagline = "Maximum Savings & Brand Equity",
        description = "Long-term local brand authority for top retailers, doctors, coaching centers, and manufacturers.",
        badge = "🏆 BEST VALUE",
        isPopular = false,
        isBestValue = true,
        isFlashDeal = false,
        isCustom = false,
        features = listOf(
            "Unbeatable value at just ₹266 per month!",
            "Permanent Gold Featured Partner listing",
            "Top position in Marketplace directory",
            "5 Custom Instagram Reels production assistance",
            "Special live news stream banner mentions",
            "Direct CRM lead forwarding & WhatsApp VIP desk"
        ),
        graphicType = "trophy",
        gradientColors = listOf(0xFF138808, 0xFF4CAF50)
    ),
    PromotionPlanItem(
        id = "plan_1_year",
        title = "Annual VIP City Partner",
        durationText = "( 1 Year )",
        priceInr = 2599,
        priceDisplay = "INR 2599 only",
        tagline = "The Ultimate Brand Domination",
        description = "Year-round brand ownership on Mznlive. Become the household name in your category across the entire region.",
        badge = "👑 VIP ANNUAL",
        isPopular = false,
        isBestValue = false,
        isFlashDeal = false,
        isCustom = false,
        features = listOf(
            "365 Days complete presence across all tabs",
            "Permanent VIP crown badge on storefront",
            "Priority ad slots during breaking live news",
            "Unlimited Instagram reels & ad banner updates",
            "Quarterly video promotion showcase",
            "Dedicated 24/7 business account manager"
        ),
        graphicType = "crown",
        gradientColors = listOf(0xFF7B1FA2, 0xFFE040FB)
    ),
    PromotionPlanItem(
        id = "plan_custom",
        title = "Customized Plans",
        durationText = "Cusomized Plans also available",
        priceInr = 0,
        priceDisplay = "Cusomized Plans also available",
        tagline = "Tailored for Brands & Multi-Outlets",
        description = "Need custom durations, multiple showroom branches, custom video ads, or omni-channel campaigns? We craft it for you.",
        badge = "🛠️ CUSTOM SOLUTIONS",
        isPopular = false,
        isBestValue = false,
        isFlashDeal = false,
        isCustom = true,
        features = listOf(
            "Custom duration (10 days, 45 days, 2 years)",
            "Multi-branch & franchise coverage",
            "Professional video shoot & scriptwriting",
            "Interactive survey & contest campaigns",
            "Custom analytics & dedicated growth advisor"
        ),
        graphicType = "custom",
        gradientColors = listOf(0xFF0F2B5C, 0xFF1E3A8A)
    )
)

@Composable
fun PlanScreen(
    plans: List<PromotionPlanItem> = emptyList(),
    language: String = "en",
    onRefresh: () -> Unit = {},
    onBuyPlan: (PromotionPlanItem) -> Unit = {}
) {
    val context = LocalContext.current
    val effectivePlans = if (plans.isNotEmpty()) plans else DefaultPromotionPlansList

    // State for fallback inquiry
    var showCustomInquiryDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val filterCategories = listOf("All", "Flash & Weekly", "Pro Monthly", "Long Term", "Custom")

    val displayedPlans = remember(selectedCategoryFilter, effectivePlans) {
        when (selectedCategoryFilter) {
            "Flash & Weekly" -> effectivePlans.filter { it.priceInr in 1..400 }
            "Pro Monthly" -> effectivePlans.filter { it.priceInr in 401..1300 && !it.isCustom }
            "Long Term" -> effectivePlans.filter { it.priceInr > 1300 && !it.isCustom }
            "Custom" -> effectivePlans.filter { it.isCustom }
            else -> effectivePlans
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MznLightBg)
            .verticalScroll(scrollState)
            .emulatorScrollable(scrollState)
            .padding(bottom = 32.dp)
            .testTag("plan_screen")
    ) {
        // 1. Top Header with Official Branding
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.applogo3dtrns),
                        contentDescription = "MznLive Logo",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .testTag("top_left_app_logo")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mzn",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorNavy
                            )
                            Text(
                                text = "Live",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorSaffron
                            )
                            Text(
                                text = if (language == "hi") " • योजनाएं" else " • Plans",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TricolorSaffron.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFICIAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TricolorSaffronDark
                                )
                            }
                        }
                        Text(
                            text = if (language == "hi")
                                "मुजफ्फरनगर के 2.5 लाख से अधिक ग्राहकों तक अपनी दुकान का विज्ञापन पहुंचाएं"
                            else
                                "Boost local footfall with sliding ads, reels & WhatsApp orders",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            onRefresh()
                            Toast.makeText(context, "Updating promotional plans from database...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("refresh_plans_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Plans",
                            tint = TricolorSaffron
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TricolorAccentBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp)),
                    height = 2.5.dp
                )
            }
        }

        // 2. High-Impact Value Proposition Banner
        PromotionHeroBanner(language = language)

        // 3. Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterCategories) { cat ->
                val isSelected = selectedCategoryFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = cat },
                    label = {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TricolorSaffron,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) TricolorSaffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // 4. Plans List - Strictly Every Plan in a Box with "Buy now" Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            displayedPlans.forEach { plan ->
                PlanCardItemBox(
                    plan = plan,
                    language = language,
                    onBuyNow = {
                        onBuyPlan(plan)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Why Advertise with Mznlive Trust Section
        AdvertisingTrustSection(language = language)
    }

    // Customized Plans Inquiry Dialog
    if (showCustomInquiryDialog) {
        CustomPlanInquiryDialog(
            onDismiss = { showCustomInquiryDialog = false }
        )
    }
}

/**
 * Individual Eye-Catching Box for Each Promotion Plan
 * Strictly includes:
 * - Graphic illustration / visual badge
 * - Duration e.g. ( 3 days ), ( 7 Days ), etc.
 * - Price e.g. INR 11 only, INR 399 only, etc.
 * - Plan description with graphics
 * - Prominent "Buy now" button
 */
@Composable
fun PlanCardItemBox(
    plan: PromotionPlanItem,
    language: String,
    onBuyNow: () -> Unit
) {
    val context = LocalContext.current
    val gradient = Brush.linearGradient(
        colors = plan.gradientColors.map { Color(it) }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (plan.isPopular || plan.isBestValue) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("plan_box_${plan.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (plan.isPopular) 2.5.dp else if (plan.isBestValue || plan.isFlashDeal) 1.8.dp else 1.dp,
            color = if (plan.isPopular) TricolorSaffron
            else if (plan.isBestValue) TricolorGreen
            else if (plan.isFlashDeal) Color(0xFFFF5722)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Ribbon & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Graphic Icon Emblem
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(gradient)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPlanIcon(plan.graphicType),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Ribbon / Badge
                plan.badge?.let { badgeText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (plan.isPopular) TricolorSaffron
                                else if (plan.isBestValue) TricolorGreen
                                else if (plan.isFlashDeal) Color(0xFFFF5722)
                                else TricolorNavy
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration and Price Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorNavy
                    )
                    Text(
                        text = plan.durationText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TricolorSaffronDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = plan.priceDisplay,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (plan.isPopular) TricolorSaffronDark else if (plan.isBestValue) TricolorGreenDark else TricolorNavy
                    )
                    // Per Day Calculation Tag for visual appeal
                    val perDayTag = getPerDayTag(plan.id)
                    if (perDayTag != null) {
                        Text(
                            text = perDayTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Plan Tagline & Description with Graphics
            Surface(
                color = MznLightSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = TricolorSaffron,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = plan.tagline,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = plan.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Key Deliverables / Features Checklist
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                plan.features.forEach { feat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(TricolorGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = TricolorGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feat,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // =========================================================================
            // Prominent "Buy now" Button after every plan
            // =========================================================================
            Button(
                onClick = onBuyNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("buy_now_button_${plan.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (plan.isPopular) TricolorSaffron
                    else if (plan.isBestValue) TricolorGreen
                    else if (plan.isFlashDeal) Color(0xFFFF5722)
                    else TricolorNavy
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (plan.isCustom) Icons.Default.Tune else Icons.Default.ShoppingCart,
                        contentDescription = "Buy Now Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (plan.isCustom) "Buy now / Customize Plan" else "Buy now (${plan.priceDisplay})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Value Proposition Hero Banner at top of Plan screen
 */
@Composable
fun PromotionHeroBanner(language: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TricolorNavy)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TricolorSaffron),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GROW YOUR LOCAL BUSINESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TricolorSaffron
                    )
                    Text(
                        text = "Real Muzaffarnagar Customers",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatMiniPill(label = "City Audience", value = "2.5L+")
                StatMiniPill(label = "Daily Views", value = "50K+")
                StatMiniPill(label = "WhatsApp Leads", value = "Direct")
                StatMiniPill(label = "Commission", value = "0%")
            }
        }
    }
}

@Composable
fun StatMiniPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TricolorSaffron
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

/**
 * Trust & Guarantee Section
 */
@Composable
fun AdvertisingTrustSection(language: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Why Promote on Mznlive?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TricolorNavy
            )
            Spacer(modifier = Modifier.height(10.dp))

            TrustRowItem(
                icon = Icons.Default.Speed,
                title = "15-Minute Instant Activation",
                subtitle = "Your sliding advert and Instagram reels go live immediately after UPI payment."
            )
            Spacer(modifier = Modifier.height(8.dp))
            TrustRowItem(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Direct Customer Chat & Calls",
                subtitle = "Customers connect directly to your shop WhatsApp with zero middlemen."
            )
            Spacer(modifier = Modifier.height(8.dp))
            TrustRowItem(
                icon = Icons.Default.DesignServices,
                title = "Free Professional Banner Design",
                subtitle = "Our design team crafts eye-catching graphics & video reels for your store."
            )
        }
    }
}

@Composable
fun TrustRowItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TricolorGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Customized Plans Builder Dialog
 */
@Composable
fun CustomPlanInquiryDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedDuration by remember { mutableStateOf("45 Days") }
    var shopCategory by remember { mutableStateOf("Retail Shop") }
    var wantBanners by remember { mutableStateOf(true) }
    var wantReels by remember { mutableStateOf(true) }
    var wantLiveNewsMention by remember { mutableStateOf(true) }

    val durations = listOf("10 Days", "45 Days", "90 Days", "1 Year", "2 Years")
    val categories = listOf("Retail Shop", "Food & Sweets", "Clothing/Sarees", "Coaching/School", "Doctor/Clinic", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    Column {
                        Text(
                            text = "Customized Promotion Plan",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorNavy
                        )
                        Text(
                            text = "Build a plan tailored for your store",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Desired Duration:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(durations) { dur ->
                        FilterChip(
                            selected = selectedDuration == dur,
                            onClick = { selectedDuration = dur },
                            label = { Text(dur, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Business Category:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = shopCategory == cat,
                            onClick = { shopCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Desired Marketing Channels:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = wantBanners, onCheckedChange = { wantBanners = it })
                    Text("Home Sliding Adverts", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = wantReels, onCheckedChange = { wantReels = it })
                    Text("Instagram 9:16 Viral Reels", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = wantLiveNewsMention, onCheckedChange = { wantLiveNewsMention = it })
                    Text("Live News Stream Broadcast Mention", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val channels = mutableListOf<String>()
                        if (wantBanners) channels.add("Sliding Banners")
                        if (wantReels) channels.add("Instagram Reels")
                        if (wantLiveNewsMention) channels.add("Live News Stream")

                        val msg = "Hi Mznlive Team! I need a Customized Promotion Plan for my business ($shopCategory). Duration: $selectedDuration. Channels: ${channels.joinToString(", ")}. Please provide a quote!"
                        val encoded = Uri.encode(msg)
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=+919876543210&text=$encoded")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Custom Quote via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// Helper to get vector icon for plan graphic
fun getPlanIcon(graphicType: String): ImageVector {
    return when (graphicType) {
        "flash" -> Icons.Default.Bolt
        "fire" -> Icons.Default.LocalFireDepartment
        "rocket" -> Icons.Default.RocketLaunch
        "star" -> Icons.Default.Star
        "diamond" -> Icons.Default.Diamond
        "trophy" -> Icons.Default.EmojiEvents
        "crown" -> Icons.Default.WorkspacePremium
        "custom" -> Icons.Default.Tune
        else -> Icons.Default.RocketLaunch
    }
}

// Helper to provide daily cost breakdown pill
fun getPerDayTag(planId: String): String? {
    return when (planId) {
        "plan_3_days" -> "Just ₹3.6 / day"
        "plan_7_days" -> "₹57 / day"
        "plan_15_days" -> "₹40 / day"
        "plan_30_days" -> "₹30 / day (Bestseller)"
        "plan_3_months" -> "₹14.4 / day (Save 52%)"
        "plan_6_months" -> "₹8.8 / day (Save 70%)"
        "plan_1_year" -> "₹7.1 / day (Save 76%)"
        else -> null
    }
}
