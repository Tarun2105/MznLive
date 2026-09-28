package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.DraggableFloatingChatButton
import com.example.ui.components.MznTabButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeBottomTab

@Composable
fun HomeScreen(
    selectedTab: HomeBottomTab,
    language: String,
    liveNewsList: List<LiveNewsItem>,
    liveStream: LiveStreamInfo?,
    liveStreams: List<LiveStreamInfo> = emptyList(),
    eventsList: List<EventItem>,
    sponsoredAdverts: List<SponsoredAdvert>,
    currentAdvertIndex: Int,
    zoomedAdvert: SponsoredAdvert?,
    activeReel0: InstagramReel?,
    activeReel1: InstagramReel?,
    activeReel2: InstagramReel?,
    marketplaceShops: List<MarketplaceShop>,
    chatActiveShop: MarketplaceShop?,
    chatMessages: List<ChatMessage>,
    chatInquiryProduct: PromotionalProduct? = null,
    verifiedPhone: String,
    locationGranted: Boolean,
    phoneGranted: Boolean,
    supabaseStatus: String,
    allReels: List<InstagramReel> = emptyList(),
    selectedReelCategory: String = "All",
    isRefreshing: Boolean = false,
    promotionPlans: List<PromotionPlanItem> = emptyList(),
    cartItems: List<com.example.data.repository.CartItem> = emptyList(),
    onSelectTab: (HomeBottomTab) -> Unit,
    onToggleLanguage: () -> Unit,
    onSetLanguage: (String) -> Unit = {},
    onAdvertClicked: (SponsoredAdvert) -> Unit,
    onCloseZoomAdvert: () -> Unit,
    onOpenGeneralChat: () -> Unit,
    onOpenShopChat: (MarketplaceShop) -> Unit,
    onCloseChat: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendSms: (phone: String, msg: String) -> Unit = { _, _ -> },
    onSendWhatsApp: (wa: String, msg: String) -> Unit = { _, _ -> },
    onOpenDedicatedShop: () -> Unit = {},
    onOpenDataTables: () -> Unit = {},
    onLogout: () -> Unit,
    onSelectReelCategory: (String) -> Unit = {},
    onRefreshDatabase: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onNavigateToBuyPlan: (PromotionPlanItem) -> Unit = {},
    onOpenNewsFeed: () -> Unit = {},
    onNavigateToSeller: (MarketplaceShop) -> Unit = {},
    onAddToCart: (PromotionalProduct) -> Unit = {},
    onOpenCart: () -> Unit = {},
    onNavigateToSignUp: (String) -> Unit = {},
    onAddNewReel: ((title: String, shop: String, reelUrl: String, thumbUrl: String, category: String) -> Unit)? = null,
    onAddNewAdvert: ((business: String, title: String, desc: String, price: String, discount: String, imgUrl: String, fbUrl: String, wa: String) -> Unit)? = null
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_container"),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            // Strictly as requested:
            // "replace the Plan tab with Shopping where all offers and new arrival of the product should be showcased"
            // Elevated container with continuous futuristic dark theme extending into the system navigation bar,
            // while inner content uses navigationBarsPadding() so app tabs and device navigation never overlap or interfere!
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_navigation_bar"),
                color = Color(0xFF000E24),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Subtle, elegant Indian Tricolor accent line above navigation
                    TricolorAccentBar(
                        modifier = Modifier.fillMaxWidth(),
                        height = 2.5.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            Triple(HomeBottomTab.HOME, "Home", Icons.Default.Home),
                            Triple(HomeBottomTab.ADS, "Ads", Icons.Default.Campaign),
                            Triple(HomeBottomTab.MARKET, "Market", Icons.Default.Storefront),
                            Triple(HomeBottomTab.SHOPPING, "Shopping", Icons.Default.ShoppingBag),
                            Triple(HomeBottomTab.ME, "Me", Icons.Default.Person)
                        )

                        tabs.forEach { (tab, label, icon) ->
                            val isSelected = selectedTab == tab
                            MznTabButton(
                                label = label,
                                icon = icon,
                                isSelected = isSelected,
                                onClick = { onSelectTab(tab) },
                                modifier = Modifier.weight(1f),
                                testTag = "nav_tab_${label.lowercase()}"
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                HomeBottomTab.HOME -> {
                    HomeDashboardView(
                        language = language,
                        liveNewsList = liveNewsList,
                        liveStream = liveStream,
                        liveStreams = liveStreams,
                        eventsList = eventsList,
                        sponsoredAdverts = sponsoredAdverts,
                        currentAdvertIndex = currentAdvertIndex,
                        zoomedAdvert = zoomedAdvert,
                        activeReel0 = activeReel0,
                        activeReel1 = activeReel1,
                        activeReel2 = activeReel2,
                        allReels = allReels,
                        selectedReelCategory = selectedReelCategory,
                        isRefreshing = isRefreshing,
                        supabaseStatus = supabaseStatus,
                        marketplaceShops = marketplaceShops,
                        onToggleLanguage = onToggleLanguage,
                        onSetLanguage = onSetLanguage,
                        onAdvertClicked = onAdvertClicked,
                        onCloseZoomAdvert = onCloseZoomAdvert,
                        onOpenChat = onOpenGeneralChat,
                        onSelectReelCategory = onSelectReelCategory,
                        onRefreshDatabase = onRefreshDatabase,
                        onOpenNewsFeed = onOpenNewsFeed,
                        onNavigateToSeller = onNavigateToSeller,
                        onAddToCart = onAddToCart,
                        onOpenCart = onOpenCart,
                        onAddNewReel = onAddNewReel,
                        onAddNewAdvert = onAddNewAdvert
                    )
                }
                HomeBottomTab.ADS -> {
                    AdsScreen(
                        onNavigateToPlan = onNavigateToPlan
                    )
                }
                HomeBottomTab.MARKET -> {
                    MarketScreen(
                        shops = marketplaceShops,
                        onOpenChatWithShop = onOpenShopChat,
                        onNavigateToSeller = onNavigateToSeller,
                        onAddToCart = onAddToCart,
                        onOpenCart = onOpenCart
                    )
                }
                HomeBottomTab.SHOPPING -> {
                    ShoppingScreen(
                        language = language,
                        cartItemsCount = cartItems.size,
                        onAddToCart = onAddToCart,
                        onOpenCart = onOpenCart,
                        onOpenChatWithProduct = { product ->
                            val shop = marketplaceShops.firstOrNull { it.name.equals(product.shopName, ignoreCase = true) }
                                ?: MarketplaceShop(
                                    id = product.id,
                                    name = product.shopName,
                                    category = product.category,
                                    address = product.shopLocation,
                                    phone = product.whatsappContact,
                                    isPromoted = true
                                )
                            onOpenShopChat(shop)
                        },
                        onNavigateToSeller = onNavigateToSeller
                    )
                }
                HomeBottomTab.ME -> {
                    MeScreen(
                        verifiedPhone = verifiedPhone,
                        language = language,
                        locationGranted = locationGranted,
                        phoneGranted = phoneGranted,
                        cartItems = cartItems,
                        supabaseStatus = supabaseStatus,
                        onToggleLanguage = onToggleLanguage,
                        onNavigateToPlan = onNavigateToPlan,
                        onOpenCart = onOpenCart,
                        onNavigateToSignUp = onNavigateToSignUp,
                        onOpenDedicatedShop = onOpenDedicatedShop,
                        onOpenDataTables = onOpenDataTables,
                        onLogout = onLogout
                    )
                }
            }

            // Draggable Floating Chat Button (can be dragged anywhere with finger selection)
            if (chatActiveShop == null) {
                DraggableFloatingChatButton(
                    onClick = onOpenGeneralChat
                )
            }

            // User & Shop Owner Chat Dialog
            if (chatActiveShop != null) {
                ChatDialog(
                    shop = chatActiveShop,
                    selectedProduct = chatInquiryProduct,
                    messages = chatMessages,
                    onSendMessage = onSendMessage,
                    onSendSms = onSendSms,
                    onSendWhatsApp = onSendWhatsApp,
                    onDismiss = onCloseChat
                )
            }
        }
    }
}
