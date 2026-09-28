package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.repository.ProductCatalogRepository
import com.example.ui.components.CartBottomSheet
import com.example.ui.components.DatabaseTablesDialog
import com.example.ui.screens.*
import com.example.ui.theme.MznliveTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MznliveViewModel
import com.example.util.LocalEmulatorScrollEvents

class MainActivity : ComponentActivity() {
    private val viewModel: MznliveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MznliveTheme {
                CompositionLocalProvider(LocalEmulatorScrollEvents provides viewModel.emulatorScrollEvents) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MznliveApp(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_SCROLL) {
            val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            val hScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL)
            if (vScroll != 0f || hScroll != 0f) {
                viewModel.dispatchMouseWheelScroll(vScroll, hScroll)
                return true
            }
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (viewModel.handleKeyNavigation(keyCode)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MznliveApp(viewModel: MznliveViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedTab by viewModel.selectedBottomTab.collectAsState()

    // Screen 2 Intro Slides
    val introSlides by viewModel.introSlides.collectAsState()
    val currentIntroIndex by viewModel.currentIntroIndex.collectAsState()

    // Screen 3 Login & Permissions
    val videoUrl by viewModel.onboardingVideoUrl.collectAsState()
    val locationGranted by viewModel.locationGranted.collectAsState()
    val phoneGranted by viewModel.phoneGranted.collectAsState()
    val notificationsGranted by viewModel.notificationsGranted.collectAsState()
    val cameraGranted by viewModel.cameraGranted.collectAsState()

    // Screen 4 Home & Marketplace
    val language by viewModel.language.collectAsState()
    val liveNewsList by viewModel.liveNewsList.collectAsState()
    val localNewsArticles by viewModel.localNewsArticles.collectAsState()
    val liveStream by viewModel.liveStream.collectAsState()
    val liveStreams by viewModel.liveStreams.collectAsState()
    val eventsList by viewModel.eventsList.collectAsState()
    val sponsoredAdverts by viewModel.sponsoredAdverts.collectAsState()
    val currentAdvertIndex by viewModel.currentAdvertIndex.collectAsState()
    val zoomedAdvert by viewModel.zoomedAdvert.collectAsState()
    val activeReel0 by viewModel.activeReelSlot0.collectAsState()
    val activeReel1 by viewModel.activeReelSlot1.collectAsState()
    val activeReel2 by viewModel.activeReelSlot2.collectAsState()
    val allReels by viewModel.instagramReels.collectAsState()
    val selectedReelCategory by viewModel.selectedReelCategory.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val marketplaceShops by viewModel.marketplaceShops.collectAsState()

    // Room Database States
    val dedicatedShop by viewModel.dedicatedShop.collectAsState()
    val showDatabaseTablesDialog by viewModel.showDatabaseTablesDialog.collectAsState()
    val chatInquiryProduct by viewModel.chatInquiryProduct.collectAsState()
    val allDbUsers by viewModel.allDbUsers.collectAsState()
    val allDbShops by viewModel.allDbShops.collectAsState()
    val allDbProducts by viewModel.allDbProducts.collectAsState()
    val rawDbProducts by viewModel.rawDbProducts.collectAsState()
    val allDbChatMessages by viewModel.allDbChatMessages.collectAsState()
    val allDbTransactions by viewModel.allDbTransactions.collectAsState()

    // Chat
    val chatActiveShop by viewModel.chatActiveShop.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Status
    val verifiedPhone by viewModel.sessionManager.verifiedPhone.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val promotionPlans by viewModel.promotionPlans.collectAsState()
    val selectedPlanForPurchase by viewModel.selectedPlanForPurchase.collectAsState()

    // Seller Detail & Shopping Cart
    val selectedSeller by viewModel.selectedSeller.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isCartOpen by viewModel.isCartOpen.collectAsState()
    val signUpRole by viewModel.signUpRole.collectAsState()

    Crossfade(targetState = currentScreen, label = "ScreenCrossfade") { screen ->
        when (screen) {
            AppScreen.SPLASH -> {
                SplashScreen(
                    onTimeout = { viewModel.onSplashFinished() }
                )
            }
            AppScreen.INTRO_CAROUSEL -> {
                IntroStoryScreen(
                    slides = introSlides,
                    currentIndex = currentIntroIndex,
                    onSkip = { viewModel.skipIntro() }
                )
            }
            AppScreen.LOGIN_PERMISSIONS -> {
                LoginPermissionsScreen(
                    videoUrl = videoUrl,
                    locationGranted = locationGranted,
                    phoneGranted = phoneGranted,
                    notificationsGranted = notificationsGranted,
                    cameraGranted = cameraGranted,
                    onLocationPermissionResult = { viewModel.setLocationPermissionGranted(it) },
                    onPhonePermissionResult = { viewModel.setPhonePermissionGranted(it) },
                    onNotificationsPermissionResult = { viewModel.setNotificationsPermissionGranted(it) },
                    onCameraPermissionResult = { viewModel.setCameraPermissionGranted(it) },
                    onProceedToHome = { viewModel.completePermissionsAndProceedToHome() }
                )
            }
            AppScreen.HOME -> {
                HomeScreen(
                    selectedTab = selectedTab,
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
                    marketplaceShops = marketplaceShops,
                    chatActiveShop = chatActiveShop,
                    chatMessages = chatMessages,
                    chatInquiryProduct = chatInquiryProduct,
                    verifiedPhone = verifiedPhone,
                    locationGranted = locationGranted,
                    phoneGranted = phoneGranted,
                    supabaseStatus = supabaseStatus,
                    allReels = allReels,
                    selectedReelCategory = selectedReelCategory,
                    isRefreshing = isRefreshing,
                    promotionPlans = promotionPlans,
                    cartItems = cartItems,
                    onSelectTab = { viewModel.selectBottomTab(it) },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSetLanguage = { viewModel.setLanguage(it) },
                    onAdvertClicked = { viewModel.toggleZoomAdvert(it) },
                    onCloseZoomAdvert = { viewModel.closeZoomAdvert() },
                    onOpenGeneralChat = { viewModel.openGeneralChat() },
                    onOpenShopChat = { viewModel.openChatWithShop(it) },
                    onCloseChat = { viewModel.closeChat() },
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onSendSms = { phone, msg -> viewModel.sendSmsContact(phone, msg) },
                    onSendWhatsApp = { wa, msg -> viewModel.sendWhatsAppContact(wa, msg) },
                    onOpenDedicatedShop = {
                        val shop = dedicatedShop ?: marketplaceShops.firstOrNull() ?: ProductCatalogRepository.findShopForMedia(null, null, marketplaceShops)
                        viewModel.openDedicatedShop(shop)
                    },
                    onOpenDataTables = { viewModel.openDatabaseTablesDialog() },
                    onLogout = { viewModel.logout() },
                    onSelectReelCategory = { viewModel.selectReelCategory(it) },
                    onRefreshDatabase = { viewModel.refreshDatabaseContent() },
                    onNavigateToPlan = { viewModel.navigateToPlanPage() },
                    onNavigateToBuyPlan = { viewModel.navigateToBuyPlan(it) },
                    onOpenNewsFeed = { viewModel.navigateToNewsFeed() },
                    onNavigateToSeller = { viewModel.navigateToSeller(it) },
                    onAddToCart = { viewModel.addToCart(it) },
                    onOpenCart = { viewModel.openCart() },
                    onNavigateToSignUp = { role -> viewModel.navigateToSignUp(role) },
                    onAddNewReel = { title, shop, reelUrl, thumbUrl, cat ->
                        viewModel.addNewInstagramReel(title, shop, reelUrl, thumbUrl, cat)
                    },
                    onAddNewAdvert = { b, t, d, p, dt, img, fb, wa ->
                        viewModel.addNewSponsoredAdvert(b, t, d, p, dt, img, fb, wa)
                    }
                )
            }
            AppScreen.SELLER_DETAIL -> {
                val activeSeller = selectedSeller ?: marketplaceShops.firstOrNull() ?: ProductCatalogRepository.findShopForMedia(null, null, marketplaceShops)
                SellerScreen(
                    shop = activeSeller,
                    cartItems = cartItems,
                    chatMessages = chatMessages,
                    dbProducts = allDbProducts,
                    onBack = { viewModel.navigateBackFromSeller() },
                    onAddToCart = { viewModel.addToCart(it) },
                    onOpenCart = { viewModel.openCart() },
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onSendSms = { phone, msg -> viewModel.sendSmsContact(phone, msg) },
                    onSendWhatsApp = { wa, msg -> viewModel.sendWhatsAppContact(wa, msg) },
                    onOpenDedicatedShop = { shop -> viewModel.openDedicatedShop(shop) }
                )
            }
            AppScreen.BUY_PLAN -> {
                BuyPlanScreen(
                    plan = selectedPlanForPurchase,
                    verifiedPhone = verifiedPhone,
                    onBack = { viewModel.navigateBackFromBuyPlan() },
                    onSubmitPurchase = { custName, sName, addr, lat, lng, mobile, p, ref, screenshot, onSuccess ->
                        viewModel.submitPlanPurchase(
                            customerName = custName,
                            shopName = sName,
                            address = addr,
                            latitude = lat,
                            longitude = lng,
                            mobileNumber = mobile,
                            plan = p,
                            transactionRef = ref,
                            screenshotUri = screenshot,
                            onComplete = onSuccess
                        )
                    }
                )
            }
            AppScreen.NEWS_FEED -> {
                NewsFeedScreen(
                    articles = localNewsArticles,
                    language = language,
                    isRefreshing = isRefreshing,
                    onNavigateBack = { viewModel.navigateBackFromNewsFeed() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onRefresh = { viewModel.refreshNewsArticles() },
                    onToggleBookmark = { id, current -> viewModel.toggleNewsBookmark(id, current) },
                    onAddArticle = { headline, headlineHi, summary, summaryHi, thumb, cat, isBreak, author ->
                        viewModel.addLocalNewsArticle(headline, headlineHi, summary, summaryHi, thumb, cat, isBreak, author)
                    },
                    onDeleteArticle = { id -> viewModel.deleteLocalNewsArticle(id) },
                    onResetSeed = { viewModel.clearAndResetLocalNews() }
                )
            }
            AppScreen.SIGN_UP -> {
                SignUpScreen(
                    initialRole = signUpRole,
                    language = language,
                    onNavigateBack = { viewModel.navigateBackFromSignUp() },
                    onSignUpSuccess = { role, name, bName, cat, addr, mob, wa ->
                        viewModel.onSignUpCompleted(role, name, bName, cat, addr, mob, wa)
                    }
                )
            }
            AppScreen.DEDICATED_SHOP_PAGE -> {
                val activeShop = dedicatedShop ?: marketplaceShops.firstOrNull() ?: ProductCatalogRepository.findShopForMedia(null, null, marketplaceShops)
                DedicatedShopOwnerScreen(
                    shop = activeShop,
                    products = allDbProducts,
                    language = language,
                    onBack = { viewModel.navigateBackFromDedicatedShop() },
                    onAddProduct = { title, titleHi, category, description, priceInr, mrpInr, imageUrl ->
                        viewModel.addProductToShop(title, titleHi, category, description, priceInr, mrpInr, imageUrl)
                    },
                    onDeleteProduct = { id -> viewModel.deleteProductFromShop(id) },
                    onPreviewCustomerStorefront = { shop -> viewModel.navigateToSeller(shop) },
                    onOpenDataTables = { viewModel.openDatabaseTablesDialog() }
                )
            }
            AppScreen.PLAN_PAGE -> {
                PlanScreen(
                    plans = promotionPlans,
                    language = language,
                    onRefresh = { viewModel.refreshDatabaseContent() },
                    onBuyPlan = { viewModel.navigateToBuyPlan(it) }
                )
                androidx.activity.compose.BackHandler {
                    viewModel.navigateBackFromPlanPage()
                }
            }
        }
    }

    // Global In-App Shopping Cart Dialog
    if (isCartOpen) {
        CartBottomSheet(
            cartItems = cartItems,
            customerPhone = verifiedPhone,
            onUpdateQuantity = { id, delta -> viewModel.updateCartQuantity(id, delta) },
            onRemoveItem = { id -> viewModel.removeFromCart(id) },
            onClearCart = { viewModel.clearCart() },
            onDismiss = { viewModel.closeCart() }
        )
    }

    // Responding Data Tables Inspector Dialog
    if (showDatabaseTablesDialog) {
        DatabaseTablesDialog(
            users = allDbUsers,
            shops = allDbShops,
            products = rawDbProducts,
            messages = allDbChatMessages,
            transactions = allDbTransactions,
            onDismiss = { viewModel.closeDatabaseTablesDialog() }
        )
    }
}
