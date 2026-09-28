package com.example.data.model

data class PromotionalProduct(
    val id: Long,
    val title: String,
    val titleHi: String = "",
    val category: String,
    val shopName: String,
    val shopLocation: String = "Muzaffarnagar",
    val priceInr: Int,
    val originalMrpInr: Int,
    val discountPercent: Int = if (originalMrpInr > priceInr) {
        (((originalMrpInr - priceInr).toFloat() / originalMrpInr) * 100).toInt()
    } else 15,
    val imageUrl: String,
    val rating: Float = 4.8f,
    val reviewsCount: Int = 120,
    val description: String,
    val whatsappContact: String = "919876543210",
    val estimatedAmazonPrice: Int = (priceInr * 1.15).toInt(),
    val estimatedFlipkartPrice: Int = (priceInr * 1.18).toInt(),
    val isFeaturedDeal: Boolean = false,
    val deliverySpeed: String = "Same-Day Local Delivery"
)

data class AiPriceComparisonResult(
    val amazonEstimatedPrice: Int,
    val flipkartEstimatedPrice: Int,
    val localSavingsAmount: Int,
    val verdict: String,
    val analysisText: String,
    val deliveryComparison: String,
    val isLocalCheaper: Boolean = true,
    val isAiGenerated: Boolean = true
)
