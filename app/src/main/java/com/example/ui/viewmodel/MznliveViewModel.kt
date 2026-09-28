package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserSessionManager
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.remote.SupabaseRepository
import com.example.data.repository.CartItem
import com.example.data.repository.MarketplaceDatabaseRepository
import com.example.data.repository.NewsFeedRepository
import com.example.data.repository.ProductCatalogRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,          // Screen 1: 3-second splash with rotating 3D logo
    INTRO_CAROUSEL,  // Screen 2: 3 images, 1 second each from database
    LOGIN_PERMISSIONS, // Screen 3: First-time onboarding, video, permissions, OTP
    HOME,            // Screen 4: Full home & marketplace
    BUY_PLAN,        // Screen 5: Buy Plan screen with shop details, GPS location, QR scan & WhatsApp receipt
    NEWS_FEED,       // Screen 6: Scrollable local news feed with Coil image loading & filters
    SELLER_DETAIL,   // Screen 7: Seller shop profile, contacts, chat, whatsapp & product catalog
    SIGN_UP,         // Screen 8: Sign Up screen for User and Shop Owner with SMS/WhatsApp OTP & auto-fill
    DEDICATED_SHOP_PAGE, // Screen 9: Dedicated shop management page for registered shop owners
    PLAN_PAGE        // Dedicated promotion plans page where shop owner can choose and buy plans
}

enum class HomeBottomTab {
    HOME, ADS, MARKET, SHOPPING, ME
}

class MznliveViewModel(application: Application) : AndroidViewModel(application) {
    val sessionManager = UserSessionManager(application)
    private val supabaseRepo = SupabaseRepository()

    // Room Database local persistence for News Articles
    val database = AppDatabase.getInstance(application)
    val newsFeedRepository = NewsFeedRepository(database.newsArticleDao())

    val localNewsArticles: StateFlow<List<NewsArticleEntity>> = newsFeedRepository.allArticles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Room Database local persistence for Users, Shops, Products, and Chat
    val marketplaceDbRepo = MarketplaceDatabaseRepository(application)

    val allDbUsers: StateFlow<List<UserEntity>> = marketplaceDbRepo.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDbShops: StateFlow<List<ShopEntity>> = marketplaceDbRepo.rawShopsList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDbProducts: StateFlow<List<PromotionalProduct>> = marketplaceDbRepo.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawDbProducts: StateFlow<List<ProductEntity>> = marketplaceDbRepo.rawProductsList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDbChatMessages: StateFlow<List<ChatMessageEntity>> = marketplaceDbRepo.rawChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Room Database local persistence for payment transactions & receipts
    val allDbTransactions: StateFlow<List<PaymentTransactionEntity>> = database.paymentTransactionDao()
        .getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Home Bottom Tab
    private val _selectedBottomTab = MutableStateFlow(HomeBottomTab.HOME)
    val selectedBottomTab: StateFlow<HomeBottomTab> = _selectedBottomTab.asStateFlow()

    // Global scroll stream for mouse wheel and keyboard navigation (for emulator / desktop control)
    val emulatorScrollEvents = MutableSharedFlow<Float>(extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun dispatchMouseWheelScroll(vScroll: Float, hScroll: Float) {
        // In Android, vScroll > 0 means wheel scrolled upwards (scroll content down / view goes up)
        // vScroll < 0 means wheel scrolled downwards (scroll content up / view goes down)
        // In ScrollState.scrollBy(), positive delta scrolls content down, negative scrolls up
        val delta = -vScroll * 180f
        emulatorScrollEvents.tryEmit(delta)
    }

    fun handleKeyNavigation(keyCode: Int): Boolean {
        when (keyCode) {
            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                emulatorScrollEvents.tryEmit(160f)
                return true
            }
            android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                emulatorScrollEvents.tryEmit(-160f)
                return true
            }
            android.view.KeyEvent.KEYCODE_PAGE_DOWN, android.view.KeyEvent.KEYCODE_SPACE -> {
                emulatorScrollEvents.tryEmit(500f)
                return true
            }
            android.view.KeyEvent.KEYCODE_PAGE_UP -> {
                emulatorScrollEvents.tryEmit(-500f)
                return true
            }
            // Number keys 1..5 switch bottom tabs when on Home Screen
            android.view.KeyEvent.KEYCODE_1 -> {
                if (_currentScreen.value == AppScreen.HOME) {
                    selectBottomTab(HomeBottomTab.HOME)
                    return true
                }
            }
            android.view.KeyEvent.KEYCODE_2 -> {
                if (_currentScreen.value == AppScreen.HOME) {
                    selectBottomTab(HomeBottomTab.ADS)
                    return true
                }
            }
            android.view.KeyEvent.KEYCODE_3 -> {
                if (_currentScreen.value == AppScreen.HOME) {
                    selectBottomTab(HomeBottomTab.MARKET)
                    return true
                }
            }
            android.view.KeyEvent.KEYCODE_4 -> {
                if (_currentScreen.value == AppScreen.HOME) {
                    selectBottomTab(HomeBottomTab.SHOPPING)
                    return true
                }
            }
            android.view.KeyEvent.KEYCODE_5 -> {
                if (_currentScreen.value == AppScreen.HOME) {
                    selectBottomTab(HomeBottomTab.ME)
                    return true
                }
            }
            android.view.KeyEvent.KEYCODE_ESCAPE -> {
                if (_isCartOpen.value) {
                    closeCart()
                    return true
                }
                if (_zoomedAdvert.value != null) {
                    closeZoomAdvert()
                    return true
                }
                if (_chatActiveShop.value != null) {
                    closeChat()
                    return true
                }
            }
        }
        return false
    }

    private var introTimerJob: Job? = null

    // Screen 2: Intro slides
    private val _introSlides = MutableStateFlow<List<IntroSlide>>(emptyList())
    val introSlides: StateFlow<List<IntroSlide>> = _introSlides.asStateFlow()

    private val _currentIntroIndex = MutableStateFlow(0)
    val currentIntroIndex: StateFlow<Int> = _currentIntroIndex.asStateFlow()

    // Screen 3: Login & Permissions
    private val _onboardingVideoUrl = MutableStateFlow("https://iyppuawgyelubfohxthj.supabase.co/storage/v1/object/public/MznLive/MznlivepermissionVideo.mp4")
    val onboardingVideoUrl: StateFlow<String> = _onboardingVideoUrl.asStateFlow()

    private val _phoneInput = MutableStateFlow("")
    val phoneInput: StateFlow<String> = _phoneInput.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode.asStateFlow()

    private val _generatedOtp = MutableStateFlow("")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _locationGranted = MutableStateFlow(sessionManager.locationGranted.value)
    val locationGranted: StateFlow<Boolean> = _locationGranted.asStateFlow()

    private val _phoneGranted = MutableStateFlow(sessionManager.phoneGranted.value)
    val phoneGranted: StateFlow<Boolean> = _phoneGranted.asStateFlow()

    private val _notificationsGranted = MutableStateFlow(sessionManager.notificationsGranted.value)
    val notificationsGranted: StateFlow<Boolean> = _notificationsGranted.asStateFlow()

    private val _cameraGranted = MutableStateFlow(sessionManager.cameraGranted.value)
    val cameraGranted: StateFlow<Boolean> = _cameraGranted.asStateFlow()

    private val _permissionsSetupCompleted = MutableStateFlow(sessionManager.permissionsSetupCompleted.value)
    val permissionsSetupCompleted: StateFlow<Boolean> = _permissionsSetupCompleted.asStateFlow()

    // Screen 4: Home Content
    private val _language = MutableStateFlow(sessionManager.language.value)
    val language: StateFlow<String> = _language.asStateFlow()

    private val _liveNewsList = MutableStateFlow<List<LiveNewsItem>>(emptyList())
    val liveNewsList: StateFlow<List<LiveNewsItem>> = _liveNewsList.asStateFlow()

    private val _liveStream = MutableStateFlow<LiveStreamInfo?>(null)
    val liveStream: StateFlow<LiveStreamInfo?> = _liveStream.asStateFlow()

    private val _liveStreams = MutableStateFlow<List<LiveStreamInfo>>(emptyList())
    val liveStreams: StateFlow<List<LiveStreamInfo>> = _liveStreams.asStateFlow()

    private val _eventsList = MutableStateFlow<List<EventItem>>(emptyList())
    val eventsList: StateFlow<List<EventItem>> = _eventsList.asStateFlow()

    private val _sponsoredAdverts = MutableStateFlow<List<SponsoredAdvert>>(emptyList())
    val sponsoredAdverts: StateFlow<List<SponsoredAdvert>> = _sponsoredAdverts.asStateFlow()

    private val _currentAdvertIndex = MutableStateFlow(0)
    val currentAdvertIndex: StateFlow<Int> = _currentAdvertIndex.asStateFlow()

    private val _zoomedAdvert = MutableStateFlow<SponsoredAdvert?>(null)
    val zoomedAdvert: StateFlow<SponsoredAdvert?> = _zoomedAdvert.asStateFlow()

    private val _instagramReels = MutableStateFlow<List<InstagramReel>>(emptyList())
    val instagramReels: StateFlow<List<InstagramReel>> = _instagramReels.asStateFlow()

    // 3 Reel slots (each slot runs and loops reels)
    private val _activeReelSlot0 = MutableStateFlow<InstagramReel?>(null)
    val activeReelSlot0: StateFlow<InstagramReel?> = _activeReelSlot0.asStateFlow()

    private val _activeReelSlot1 = MutableStateFlow<InstagramReel?>(null)
    val activeReelSlot1: StateFlow<InstagramReel?> = _activeReelSlot1.asStateFlow()

    private val _activeReelSlot2 = MutableStateFlow<InstagramReel?>(null)
    val activeReelSlot2: StateFlow<InstagramReel?> = _activeReelSlot2.asStateFlow()

    // Database-driven status & filtering
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedReelCategory = MutableStateFlow("All")
    val selectedReelCategory: StateFlow<String> = _selectedReelCategory.asStateFlow()

    private val _activeHighlightReelId = MutableStateFlow<Long?>(null)
    val activeHighlightReelId: StateFlow<Long?> = _activeHighlightReelId.asStateFlow()

    private val _marketplaceShops = MutableStateFlow<List<MarketplaceShop>>(emptyList())
    val marketplaceShops: StateFlow<List<MarketplaceShop>> = _marketplaceShops.asStateFlow()

    // Dedicated Shop Owner State
    private val _dedicatedShop = MutableStateFlow<MarketplaceShop?>(null)
    val dedicatedShop: StateFlow<MarketplaceShop?> = _dedicatedShop.asStateFlow()

    // Responding Data Tables Dialog State
    private val _showDatabaseTablesDialog = MutableStateFlow(false)
    val showDatabaseTablesDialog: StateFlow<Boolean> = _showDatabaseTablesDialog.asStateFlow()

    // Selected Product for Chat Inquiry
    private val _chatInquiryProduct = MutableStateFlow<PromotionalProduct?>(null)
    val chatInquiryProduct: StateFlow<PromotionalProduct?> = _chatInquiryProduct.asStateFlow()

    // Chat
    private val _chatActiveShop = MutableStateFlow<MarketplaceShop?>(null)
    val chatActiveShop: StateFlow<MarketplaceShop?> = _chatActiveShop.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Supabase Health
    private val _supabaseStatus = MutableStateFlow("Connecting to Supabase...")
    val supabaseStatus: StateFlow<String> = _supabaseStatus.asStateFlow()

    // Promotion Plans
    private val _promotionPlans = MutableStateFlow<List<PromotionPlanItem>>(emptyList())
    val promotionPlans: StateFlow<List<PromotionPlanItem>> = _promotionPlans.asStateFlow()

    // Plan Purchase Screen State
    private val _selectedPlanForPurchase = MutableStateFlow<PromotionPlanItem?>(null)
    val selectedPlanForPurchase: StateFlow<PromotionPlanItem?> = _selectedPlanForPurchase.asStateFlow()

    private val _latestPurchaseOrder = MutableStateFlow<PlanPurchaseOrder?>(null)
    val latestPurchaseOrder: StateFlow<PlanPurchaseOrder?> = _latestPurchaseOrder.asStateFlow()

    // Seller Detail Screen State
    private val _selectedSeller = MutableStateFlow<MarketplaceShop?>(null)
    val selectedSeller: StateFlow<MarketplaceShop?> = _selectedSeller.asStateFlow()

    // Shopping Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isCartOpen = MutableStateFlow(false)
    val isCartOpen: StateFlow<Boolean> = _isCartOpen.asStateFlow()

    init {
        loadData()
        startAdvertAutoSlide()
        startReelsLoop()

        // Observe Room DB shops reactively
        viewModelScope.launch {
            marketplaceDbRepo.allShops.collect { shops ->
                if (shops.isNotEmpty()) {
                    _marketplaceShops.value = shops
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Seed local Room database if empty (News, Shops, Products, Users)
            newsFeedRepository.seedDefaultLocalNewsIfEmpty()
            marketplaceDbRepo.seedInitialDataIfEmpty()

            _introSlides.value = supabaseRepo.fetchIntroSlides()
            _onboardingVideoUrl.value = supabaseRepo.fetchOnboardingVideoUrl()
            val remoteNews = supabaseRepo.fetchLiveNews()
            _liveNewsList.value = remoteNews

            // Sync remote news into local Room database for offline access
            if (remoteNews.isNotEmpty()) {
                val entities = remoteNews.map { item ->
                    NewsArticleEntity(
                        id = item.id,
                        headline = item.titleEn,
                        headlineHi = item.titleHi,
                        summary = item.descriptionEn,
                        summaryHi = item.descriptionHi,
                        thumbnailUrl = item.imageUrl,
                        category = item.category,
                        author = "MznLive Bureau",
                        sourceName = item.sourceNewspaper,
                        publishedAt = System.currentTimeMillis() - (item.id % 24) * 3600 * 1000,
                        timeAgo = item.publishedTime,
                        videoUrl = item.websiteUrl,
                        isBreaking = item.isBreaking,
                        readCount = (120..750).random(),
                        isBookmarked = false
                    )
                }
                newsFeedRepository.insertArticles(entities)
            }

            val streams = supabaseRepo.fetchLiveStreams()
            _liveStreams.value = streams
            _liveStream.value = streams.firstOrNull()
            _eventsList.value = supabaseRepo.fetchEvents()
            _sponsoredAdverts.value = supabaseRepo.fetchSponsoredAdverts()
            _marketplaceShops.value = supabaseRepo.fetchMarketplaceShops()
            _promotionPlans.value = supabaseRepo.fetchPromotionPlans()

            val reels = supabaseRepo.fetchInstagramReels()
            _instagramReels.value = reels
            updateReelSlots(reels)

            // Test connection
            val (connected, msg) = supabaseRepo.testConnection()
            _supabaseStatus.value = if (connected) "Connected to Supabase (iyppuawgyelubfohxthj)" else "Offline / Fallback ($msg)"
        }
    }

    private fun updateReelSlots(reels: List<InstagramReel>) {
        if (reels.isNotEmpty()) {
            val slot0List = reels.filter { it.slotIndex == 0 }.ifEmpty { reels }
            val slot1List = reels.filter { it.slotIndex == 1 }.ifEmpty { reels }
            val slot2List = reels.filter { it.slotIndex == 2 }.ifEmpty { reels }

            _activeReelSlot0.value = slot0List.firstOrNull()
            _activeReelSlot1.value = slot1List.getOrNull(1) ?: slot1List.firstOrNull()
            _activeReelSlot2.value = slot2List.getOrNull(2) ?: slot2List.firstOrNull()
        }
    }

    // Navigation triggers
    fun onSplashFinished() {
        introTimerJob?.cancel()
        _currentScreen.value = AppScreen.INTRO_CAROUSEL
        startIntroCarouselTimer()
    }

    private fun startIntroCarouselTimer() {
        introTimerJob?.cancel()
        introTimerJob = viewModelScope.launch {
            // Show 3 images, 1 second each
            for (i in 0 until 3) {
                _currentIntroIndex.value = i
                delay(1000)
            }
            proceedAfterIntro()
        }
    }

    fun skipIntro() {
        proceedAfterIntro()
    }

    private fun proceedAfterIntro() {
        introTimerJob?.cancel()
        if (sessionManager.permissionsSetupCompleted.value) {
            _currentScreen.value = AppScreen.HOME
        } else {
            _currentScreen.value = AppScreen.LOGIN_PERMISSIONS
        }
    }

    // Login & Permissions logic
    fun setPhoneInput(phone: String) {
        val clean = phone.filter { it.isDigit() }.take(10)
        if (clean != _phoneInput.value) {
            _isOtpSent.value = false
            _otpCode.value = ""
            _generatedOtp.value = ""
        }
        _phoneInput.value = clean
        _loginError.value = null
    }

    fun setOtpCode(otp: String) {
        _otpCode.value = otp.filter { it.isDigit() }.take(6)
        _loginError.value = null
    }

    fun sendOtp(viaWhatsApp: Boolean = false) {
        val phone = _phoneInput.value.trim()
        if (phone.length < 10) {
            _loginError.value = "Please enter a valid 10-digit mobile number"
            return
        }
        val randomCode = (100000..999999).random().toString()
        _generatedOtp.value = randomCode
        _isOtpSent.value = true
        _loginError.value = null
        _otpCode.value = ""
    }

    fun verifyOtp() {
        val phone = _phoneInput.value.trim()
        if (phone.length < 10) {
            _loginError.value = "Please enter a valid 10-digit mobile number"
            return
        }
        if (!_isOtpSent.value) {
            _loginError.value = "Please request an OTP via SMS or WhatsApp first"
            return
        }
        val code = _otpCode.value.trim()
        if (code.isBlank() || code != _generatedOtp.value) {
            _loginError.value = "Invalid OTP. Please enter the correct 6-digit code"
            return
        }
        _loginError.value = null
        sessionManager.setLoggedIn("+91 $phone")
        _currentScreen.value = AppScreen.HOME
    }

    fun enterAsGuest() {
        sessionManager.setLoggedIn("Guest User")
        _currentScreen.value = AppScreen.HOME
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        sessionManager.setLocationPermissionGranted(granted)
        _locationGranted.value = granted
    }

    fun setPhonePermissionGranted(granted: Boolean) {
        sessionManager.setPhonePermissionGranted(granted)
        _phoneGranted.value = granted
    }

    fun setNotificationsPermissionGranted(granted: Boolean) {
        sessionManager.setNotificationsPermissionGranted(granted)
        _notificationsGranted.value = granted
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        sessionManager.setCameraPermissionGranted(granted)
        _cameraGranted.value = granted
    }

    fun completePermissionsAndProceedToHome() {
        sessionManager.setPermissionsSetupCompleted(true)
        _permissionsSetupCompleted.value = true
        _currentScreen.value = AppScreen.HOME
    }

    // Language Toggle & Selection
    fun toggleLanguage() {
        val newLang = if (_language.value == "hi") "en" else "hi"
        _language.value = newLang
        sessionManager.setLanguage(newLang)
    }

    fun setLanguage(lang: String) {
        if (lang == "hi" || lang == "en") {
            _language.value = lang
            sessionManager.setLanguage(lang)
        }
    }

    // Bottom Navigation
    fun selectBottomTab(tab: HomeBottomTab) {
        _selectedBottomTab.value = tab
    }

    // Plan Page & Buy Plan Screen Navigation & Actions
    fun navigateToPlanPage() {
        _currentScreen.value = AppScreen.PLAN_PAGE
    }

    fun navigateBackFromPlanPage() {
        _currentScreen.value = AppScreen.HOME
        _selectedBottomTab.value = HomeBottomTab.ME
    }

    fun navigateToBuyPlan(plan: PromotionPlanItem) {
        _selectedPlanForPurchase.value = plan
        _currentScreen.value = AppScreen.BUY_PLAN
    }

    fun navigateBackFromBuyPlan() {
        _currentScreen.value = AppScreen.HOME
        _selectedBottomTab.value = HomeBottomTab.ME
    }

    // News Feed Navigation & Room Database Operations
    fun navigateToNewsFeed() {
        _currentScreen.value = AppScreen.NEWS_FEED
    }

    fun navigateBackFromNewsFeed() {
        _currentScreen.value = AppScreen.HOME
    }

    // Sign Up Screen Navigation & State
    private val _signUpRole = MutableStateFlow("SHOP_OWNER")
    val signUpRole: StateFlow<String> = _signUpRole.asStateFlow()

    fun navigateToSignUp(role: String = "SHOP_OWNER") {
        _signUpRole.value = role
        _currentScreen.value = AppScreen.SIGN_UP
    }

    fun navigateBackFromSignUp() {
        _currentScreen.value = AppScreen.HOME
        _selectedBottomTab.value = HomeBottomTab.ME
    }

    fun onSignUpCompleted(
        role: String,
        name: String,
        businessName: String,
        category: String,
        address: String,
        mobile: String,
        whatsapp: String
    ) {
        viewModelScope.launch {
            val (userId, shopId) = marketplaceDbRepo.registerUser(
                role = role,
                name = name,
                businessName = businessName,
                category = category,
                address = address,
                mobile = mobile,
                whatsapp = whatsapp
            )

            sessionManager.saveRegistration(
                role = role,
                name = name,
                businessName = businessName,
                category = category,
                address = address,
                mobile = mobile,
                whatsapp = whatsapp
            )

            if (role == "SHOP_OWNER") {
                val createdShop = MarketplaceShop(
                    id = shopId ?: System.currentTimeMillis(),
                    name = businessName.ifBlank { "$name's Store" },
                    category = category.ifBlank { "Retails & Kirana (खुदरा और किराना)" },
                    address = address.ifBlank { "Main Market, Muzaffarnagar" },
                    phone = mobile,
                    whatsapp = whatsapp.ifBlank { mobile },
                    instagramLink = "https://instagram.com",
                    facebookLink = "https://facebook.com",
                    rating = 5.0f,
                    imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&auto=format&fit=crop&q=80",
                    featuredOffer = "Grand Opening Special Discount: Up to 25% OFF!"
                )
                _dedicatedShop.value = createdShop
                _currentScreen.value = AppScreen.DEDICATED_SHOP_PAGE
            } else {
                _currentScreen.value = AppScreen.HOME
                _selectedBottomTab.value = HomeBottomTab.ME
            }
        }
    }

    fun openDedicatedShop(shop: MarketplaceShop) {
        _dedicatedShop.value = shop
        _currentScreen.value = AppScreen.DEDICATED_SHOP_PAGE
    }

    fun navigateBackFromDedicatedShop() {
        _currentScreen.value = AppScreen.HOME
        _selectedBottomTab.value = HomeBottomTab.ME
    }

    fun addProductToShop(
        title: String,
        titleHi: String,
        category: String,
        description: String,
        priceInr: Int,
        mrpInr: Int,
        imageUrl: String
    ) {
        val shop = _dedicatedShop.value ?: _marketplaceShops.value.firstOrNull() ?: return
        viewModelScope.launch {
            marketplaceDbRepo.addProduct(
                shopId = shop.id,
                shopName = shop.name,
                title = title,
                titleHi = titleHi,
                category = category,
                description = description,
                priceInr = priceInr,
                originalMrpInr = mrpInr,
                imageUrl = imageUrl,
                phone = shop.phone,
                whatsapp = shop.whatsapp
            )
        }
    }

    fun deleteProductFromShop(productId: Long) {
        viewModelScope.launch {
            marketplaceDbRepo.deleteProduct(productId)
        }
    }

    fun openDatabaseTablesDialog() {
        _showDatabaseTablesDialog.value = true
    }

    fun closeDatabaseTablesDialog() {
        _showDatabaseTablesDialog.value = false
    }

    fun sendSmsContact(phone: String, message: String) {
        val shopName = _chatActiveShop.value?.name ?: "Local Shop"
        viewModelScope.launch {
            marketplaceDbRepo.logChatMessage(
                shopName = shopName,
                customerPhone = sessionManager.verifiedPhone.value,
                sender = "user",
                message = message,
                channel = "SMS"
            )
        }
    }

    fun sendWhatsAppContact(whatsapp: String, message: String) {
        val shopName = _chatActiveShop.value?.name ?: "Local Shop"
        viewModelScope.launch {
            marketplaceDbRepo.logChatMessage(
                shopName = shopName,
                customerPhone = sessionManager.verifiedPhone.value,
                sender = "user",
                message = message,
                channel = "WHATSAPP"
            )
        }
    }

    fun toggleNewsBookmark(id: Long, currentBookmark: Boolean) {
        viewModelScope.launch {
            newsFeedRepository.toggleBookmark(id, currentBookmark)
        }
    }

    fun addLocalNewsArticle(
        headline: String,
        headlineHi: String = "",
        summary: String = "",
        summaryHi: String = "",
        thumbnailUrl: String = "",
        category: String = "Local",
        isBreaking: Boolean = false,
        author: String = "Muzaffarnagar News Desk"
    ) {
        viewModelScope.launch {
            val article = NewsArticleEntity(
                headline = headline,
                headlineHi = headlineHi,
                summary = summary,
                summaryHi = summaryHi,
                thumbnailUrl = thumbnailUrl.ifBlank { "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=600&auto=format&fit=crop&q=80" },
                category = category,
                author = author.ifBlank { "Muzaffarnagar News Desk" },
                sourceName = "MznLive Local Desk",
                publishedAt = System.currentTimeMillis(),
                timeAgo = "Just now",
                isBreaking = isBreaking,
                readCount = 1,
                isBookmarked = false
            )
            newsFeedRepository.insertArticle(article)
        }
    }

    fun deleteLocalNewsArticle(id: Long) {
        viewModelScope.launch {
            newsFeedRepository.deleteArticle(id)
        }
    }

    fun clearAndResetLocalNews() {
        viewModelScope.launch {
            newsFeedRepository.resetToDefaultSeed()
        }
    }

    fun refreshNewsArticles() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                newsFeedRepository.seedDefaultLocalNewsIfEmpty()
                val remoteNews = supabaseRepo.fetchLiveNews()
                if (remoteNews.isNotEmpty()) {
                    _liveNewsList.value = remoteNews
                    val entities = remoteNews.map { item ->
                        NewsArticleEntity(
                            id = item.id,
                            headline = item.titleEn,
                            headlineHi = item.titleHi,
                            summary = item.descriptionEn,
                            summaryHi = item.descriptionHi,
                            thumbnailUrl = item.imageUrl,
                            category = item.category,
                            author = "MznLive Bureau",
                            sourceName = item.sourceNewspaper,
                            publishedAt = System.currentTimeMillis() - (item.id % 24) * 3600 * 1000,
                            timeAgo = item.publishedTime,
                            videoUrl = item.websiteUrl,
                            isBreaking = item.isBreaking,
                            readCount = 210,
                            isBookmarked = false
                        )
                    }
                    newsFeedRepository.insertArticles(entities)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Seller Detail Screen Navigation
    fun navigateToSeller(shop: MarketplaceShop) {
        _selectedSeller.value = shop
        _currentScreen.value = AppScreen.SELLER_DETAIL
    }

    fun navigateToSellerByName(shopName: String) {
        val foundShop = ProductCatalogRepository.findShopForMedia(shopName, null, _marketplaceShops.value)
        _selectedSeller.value = foundShop
        _currentScreen.value = AppScreen.SELLER_DETAIL
    }

    fun navigateBackFromSeller() {
        _currentScreen.value = AppScreen.HOME
    }

    // Shopping Cart Operations
    fun addToCart(product: PromotionalProduct, quantity: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentList.add(CartItem(product = product, quantity = quantity))
        }
        _cartItems.value = currentList
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartTotal(): Int {
        return _cartItems.value.sumOf { it.product.priceInr * it.quantity }
    }

    fun getCartItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun openCart() {
        _isCartOpen.value = true
    }

    fun closeCart() {
        _isCartOpen.value = false
    }

    fun submitPlanPurchase(
        customerName: String,
        shopName: String,
        address: String,
        latitude: Double?,
        longitude: Double?,
        mobileNumber: String,
        plan: PromotionPlanItem,
        transactionRef: String?,
        screenshotUri: String? = null,
        onComplete: (PlanPurchaseOrder) -> Unit
    ) {
        val now = Date()
        val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(now)
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)
        val randomSuffix = (100000..999999).random()
        val sysTxnNumber = "TXN-TLV-$dateCode-$randomSuffix"
        val sysReceiptNumber = "RCPT-TLV-$randomSuffix"

        val order = PlanPurchaseOrder(
            orderNumber = sysTxnNumber,
            transactionNumber = sysTxnNumber,
            receiptNumber = sysReceiptNumber,
            issuedBy = "MznLive By TalntVibe",
            dateTimeFormatted = dateFormatted,
            customerName = customerName,
            shopName = shopName,
            address = address,
            latitude = latitude,
            longitude = longitude,
            mobileNumber = mobileNumber,
            planId = plan.id,
            planTitle = plan.title,
            planDuration = plan.durationText,
            amountInr = plan.priceInr,
            paymentMethod = "UPI_QR_SCAN",
            paymentStatus = "completed",
            transactionRef = transactionRef?.ifBlank { null } ?: sysTxnNumber,
            screenshotUri = screenshotUri,
            whatsappNotified = true,
            merchantVpa = "9760077767@pnb",
            merchantName = "MznLive By TalntVibe",
            createdAt = now.time
        )
        _latestPurchaseOrder.value = order

        // Persist transaction to Room Database and Supabase
        viewModelScope.launch {
            try {
                val entity = PaymentTransactionEntity(
                    transactionNumber = sysTxnNumber,
                    receiptNumber = sysReceiptNumber,
                    issuedBy = "MznLive By TalntVibe",
                    dateTime = dateFormatted,
                    timestamp = now.time,
                    customerName = customerName,
                    shopName = shopName,
                    mobileNumber = mobileNumber,
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                    planId = plan.id,
                    planTitle = plan.title,
                    planDuration = plan.durationText,
                    amountInr = plan.priceInr.toDouble(),
                    upiId = "9760077767@pnb",
                    paymentMethod = "UPI_QR_SCAN",
                    userUtrRef = transactionRef,
                    screenshotUri = screenshotUri,
                    paymentStatus = "SUCCESS",
                    notes = "Issued on behalf of MznLive By TalntVibe"
                )
                database.paymentTransactionDao().insertTransaction(entity)
                supabaseRepo.createPlanPurchase(order)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        onComplete(order)
    }

    // Sliding Adverts logic: 1-second auto slide
    private fun startAdvertAutoSlide() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                // If not currently zoomed in, advance slide
                if (_zoomedAdvert.value == null && _sponsoredAdverts.value.isNotEmpty()) {
                    val nextIndex = (_currentAdvertIndex.value + 1) % _sponsoredAdverts.value.size
                    _currentAdvertIndex.value = nextIndex
                }
            }
        }
    }

    fun toggleZoomAdvert(advert: SponsoredAdvert) {
        if (_zoomedAdvert.value?.id == advert.id) {
            _zoomedAdvert.value = null
        } else {
            _zoomedAdvert.value = advert
        }
    }

    fun closeZoomAdvert() {
        _zoomedAdvert.value = null
    }

    // Instagram Reels Loop: when one reel finishes, loops to next
    private fun startReelsLoop() {
        viewModelScope.launch {
            var loopCounter = 0
            while (isActive) {
                delay(4000) // Loop transition interval
                val reels = _instagramReels.value
                if (reels.isNotEmpty()) {
                    loopCounter++
                    val slot0Reels = reels.filter { it.slotIndex == 0 }.ifEmpty { reels }
                    val slot1Reels = reels.filter { it.slotIndex == 1 }.ifEmpty { reels }
                    val slot2Reels = reels.filter { it.slotIndex == 2 }.ifEmpty { reels }

                    _activeReelSlot0.value = slot0Reels[loopCounter % slot0Reels.size]
                    _activeReelSlot1.value = slot1Reels[(loopCounter + 1) % slot1Reels.size]
                    _activeReelSlot2.value = slot2Reels[(loopCounter + 2) % slot2Reels.size]
                }
            }
        }
    }

    // Category filter for 3-column reels grid
    fun selectReelCategory(category: String) {
        _selectedReelCategory.value = category
    }

    // Refresh database-driven content from Supabase
    fun refreshDatabaseContent() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val newNews = supabaseRepo.fetchLiveNews()
                if (newNews.isNotEmpty()) {
                    _liveNewsList.value = newNews
                }
                val newAdverts = supabaseRepo.fetchSponsoredAdverts()
                if (newAdverts.isNotEmpty()) {
                    _sponsoredAdverts.value = newAdverts
                }
                val newReels = supabaseRepo.fetchInstagramReels()
                if (newReels.isNotEmpty()) {
                    _instagramReels.value = newReels
                    updateReelSlots(newReels)
                }
                val newPlans = supabaseRepo.fetchPromotionPlans()
                if (newPlans.isNotEmpty()) {
                    _promotionPlans.value = newPlans
                }
                val (connected, msg) = supabaseRepo.testConnection()
                _supabaseStatus.value = if (connected) "Database Synced (${_liveNewsList.value.size} News, ${_sponsoredAdverts.value.size} Ads, ${_instagramReels.value.size} Reels)" else "Offline Cache Active ($msg)"
            } catch (_: Exception) {
                // Keep local cached state active
            } finally {
                delay(600)
                _isRefreshing.value = false
            }
        }
    }

    // Database-driven update: Add new promotional Instagram reel
    fun addNewInstagramReel(
        title: String,
        shopName: String,
        reelUrl: String,
        thumbnailUrl: String,
        category: String = "General",
        likesCount: String = "1.5K"
    ) {
        val newReel = InstagramReel(
            id = System.currentTimeMillis(),
            title = title,
            shopName = shopName,
            reelUrl = reelUrl.ifBlank { "https://instagram.com/mznlive" },
            thumbnailUrl = thumbnailUrl.ifBlank { "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500&auto=format&fit=crop&q=80" },
            likesCount = likesCount,
            slotIndex = (_instagramReels.value.size % 3),
            category = category
        )
        // Immediate in-memory update for responsiveness
        val updated = listOf(newReel) + _instagramReels.value
        _instagramReels.value = updated
        updateReelSlots(updated)

        // Asynchronous database persistence to Supabase
        viewModelScope.launch {
            supabaseRepo.insertInstagramReel(newReel)
        }
    }

    // Database-driven update: Add new sponsored advert
    fun addNewSponsoredAdvert(
        businessName: String,
        productTitle: String,
        productDescription: String,
        price: String,
        discountTag: String,
        imageUrl: String,
        facebookPageUrl: String,
        whatsappContact: String
    ) {
        val newAdvert = SponsoredAdvert(
            id = System.currentTimeMillis(),
            businessName = businessName,
            productTitle = productTitle,
            productDescription = productDescription,
            price = price,
            discountTag = discountTag,
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=700&auto=format&fit=crop&q=80" },
            facebookPageUrl = facebookPageUrl.ifBlank { "https://facebook.com/mznlive" },
            whatsappContact = whatsappContact.ifBlank { "+919876543210" },
            displayDurationSeconds = 1,
            orderNum = 1
        )
        // Immediate in-memory update
        val updated = listOf(newAdvert) + _sponsoredAdverts.value
        _sponsoredAdverts.value = updated

        // Asynchronous database persistence to Supabase
        viewModelScope.launch {
            supabaseRepo.insertSponsoredAdvert(newAdvert)
        }
    }

    // Chat functionality
    fun openChatWithShop(shop: MarketplaceShop, product: PromotionalProduct? = null) {
        _chatActiveShop.value = shop
        _chatInquiryProduct.value = product
        val welcomeMsg = if (_language.value == "hi")
            "नमस्ते! ${shop.name} में आपका स्वागत है। हम आपकी क्या सहायता कर सकते हैं?"
        else
            "Hello! Welcome to ${shop.name} on Mznlive. How can we help you today?"

        _chatMessages.value = listOf(
            ChatMessage(
                shopId = shop.id,
                shopName = shop.name,
                sender = "shop",
                message = welcomeMsg
            )
        )
    }

    fun openGeneralChat() {
        val defaultShop = _marketplaceShops.value.firstOrNull() ?: MarketplaceShop(
            id = 1,
            name = "Mznlive Merchant Desk",
            category = "Customer Care & Local Shops",
            address = "Main Market Hub",
            phone = "+919876543210",
            whatsapp = "+919876543210",
            instagramLink = "https://instagram.com/mznlive",
            facebookLink = "https://facebook.com/mznlive",
            rating = 5.0f,
            imageUrl = "",
            featuredOffer = "Instant Shop Support"
        )
        openChatWithShop(defaultShop, null)
    }

    fun closeChat() {
        _chatActiveShop.value = null
        _chatInquiryProduct.value = null
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val shop = _chatActiveShop.value
        val shopName = shop?.name ?: "Shop"
        val shopId = shop?.id ?: 1
        val userPhone = sessionManager.verifiedPhone.value

        val current = _chatMessages.value.toMutableList()
        current.add(
            ChatMessage(
                shopId = shopId,
                shopName = shopName,
                sender = "user",
                message = text
            )
        )
        _chatMessages.value = current

        // Persist message to Room database
        viewModelScope.launch {
            marketplaceDbRepo.logChatMessage(
                shopId = shopId,
                shopName = shopName,
                customerPhone = userPhone,
                sender = "user",
                message = text,
                channel = "APP"
            )
        }

        // Shop auto-reply after 1.2 second
        viewModelScope.launch {
            delay(1200)
            val replyText = if (_language.value == "hi")
                "धन्यवाद! हमें आपका संदेश प्राप्त हुआ है। हमारी टीम शीघ्र ही आपसे व्हाट्सएप या कॉल पर संपर्क करेगी।"
            else
                "Thank you for reaching out! We received your message and will respond promptly or connect via WhatsApp."
            val updated = _chatMessages.value.toMutableList()
            updated.add(
                ChatMessage(
                    shopId = shopId,
                    shopName = shopName,
                    sender = "shop",
                    message = replyText
                )
            )
            _chatMessages.value = updated

            marketplaceDbRepo.logChatMessage(
                shopId = shopId,
                shopName = shopName,
                customerPhone = userPhone,
                sender = "shop",
                message = replyText,
                channel = "APP"
            )
        }
    }

    fun logout() {
        sessionManager.resetSession()
        _selectedBottomTab.value = HomeBottomTab.HOME
        _currentScreen.value = AppScreen.LOGIN_PERMISSIONS
    }
}
