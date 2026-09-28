package com.example.data.model

data class IntroSlide(
    val id: Long,
    val orderNum: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val facebookPostUrl: String
)

data class LiveNewsItem(
    val id: Long,
    val titleEn: String,
    val titleHi: String,
    val sourceNewspaper: String,
    val websiteUrl: String,
    val category: String,
    val isBreaking: Boolean,
    val imageUrl: String = "",
    val descriptionEn: String = "",
    val descriptionHi: String = "",
    val publishedTime: String = "Today",
    val readMinutes: Int = 3
)

data class LiveStreamInfo(
    val id: Long,
    val titleEn: String,
    val titleHi: String,
    val youtubeVideoUrl: String,
    val youtubeVideoId: String,
    val channelName: String,
    val isLive: Boolean,
    val viewersCount: Int,
    val category: String = "News",
    val thumbnailUrl: String = ""
)

data class EventItem(
    val id: Long,
    val titleEn: String,
    val titleHi: String,
    val category: String,
    val location: String,
    val eventDate: String,
    val organizer: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val facebookEventUrl: String
)

data class SponsoredAdvert(
    val id: Long,
    val businessName: String,
    val productTitle: String,
    val productDescription: String,
    val price: String,
    val discountTag: String,
    val imageUrl: String,
    val facebookPageUrl: String,
    val whatsappContact: String,
    val displayDurationSeconds: Int = 1,
    val orderNum: Int = 1
)

data class InstagramReel(
    val id: Long,
    val title: String,
    val shopName: String,
    val reelUrl: String,
    val thumbnailUrl: String,
    val likesCount: String,
    val slotIndex: Int,
    val category: String = "All"
)

data class MarketplaceShop(
    val id: Long,
    val name: String,
    val category: String = "Retail",
    val address: String = "Muzaffarnagar",
    val phone: String = "9760077767",
    val whatsapp: String = "9760077767",
    val instagramLink: String = "",
    val facebookLink: String = "",
    val rating: Float = 4.8f,
    val imageUrl: String = "",
    val featuredOffer: String = "Special Deal",
    val isPromoted: Boolean = false,
    val reviewsCount: Int = 120
)

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val shopId: Long = 1,
    val shopName: String,
    val sender: String, // "user" or "shop"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PromotionPlanItem(
    val id: String,
    val title: String,
    val durationText: String, // e.g. "( 3 days )", "( 7 Days )", "( 1 Year )", etc.
    val priceInr: Int,
    val priceDisplay: String, // e.g. "INR 11 only", "INR 399 only", etc.
    val tagline: String,
    val description: String,
    val badge: String? = null,
    val isPopular: Boolean = false,
    val isBestValue: Boolean = false,
    val isFlashDeal: Boolean = false,
    val isCustom: Boolean = false,
    val features: List<String>,
    val graphicType: String = "rocket", // "flash", "rocket", "fire", "star", "diamond", "crown", "trophy", "custom"
    val gradientColors: List<Long> = listOf(0xFFFF9933, 0xFFFF5722)
)

data class PlanPurchaseOrder(
    val orderNumber: String,
    val transactionNumber: String = orderNumber,
    val receiptNumber: String = "",
    val issuedBy: String = "MznLive By TalntVibe",
    val dateTimeFormatted: String = "",
    val customerName: String,
    val shopName: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val mobileNumber: String,
    val planId: String,
    val planTitle: String,
    val planDuration: String,
    val amountInr: Int,
    val paymentMethod: String = "UPI_QR_SCAN",
    val paymentStatus: String = "completed",
    val transactionRef: String? = null,
    val screenshotUri: String? = null,
    val whatsappNotified: Boolean = true,
    val merchantVpa: String = "9760077767@pnb",
    val merchantName: String = "MznLive By TalntVibe",
    val createdAt: Long = System.currentTimeMillis()
)
