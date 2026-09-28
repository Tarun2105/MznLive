package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Centralized utility to share articles, product items, and business shop details
 * to external platforms (WhatsApp, Telegram, SMS, Social Media, etc.) via Android's native share sheet.
 */
object ShareUtils {

    fun shareText(context: Context, title: String, message: String, linkUrl: String? = null) {
        try {
            val shareBody = buildString {
                append("📢 *$title*\n\n")
                if (message.isNotBlank()) {
                    append("$message\n\n")
                }
                if (!linkUrl.isNullOrBlank()) {
                    append("🔗 Link: $linkUrl\n\n")
                }
                append("📲 Shared via MznLive • Muzaffarnagar City Portal\nWebsite: https://talntvibe.wordpress.com")
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, shareBody)
            }
            val chooser = Intent.createChooser(sendIntent, "Share via")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open share menu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareArticle(
        context: Context,
        title: String,
        description: String,
        source: String,
        url: String? = null
    ) {
        val body = buildString {
            if (description.isNotBlank()) append("$description\n\n")
            append("📰 Source: $source")
        }
        shareText(context, title, body, url)
    }

    fun shareProduct(
        context: Context,
        productName: String,
        priceInr: Int,
        mrpInr: Int,
        shopName: String,
        shopLocation: String,
        productUrl: String? = null
    ) {
        val discount = if (mrpInr > priceInr) {
            val percent = ((mrpInr - priceInr) * 100) / mrpInr
            " (Save $percent% - MRP ₹$mrpInr)"
        } else ""

        val body = buildString {
            append("🏷️ Best Deal: ₹$priceInr$discount\n")
            append("🏪 Available at: $shopName ($shopLocation)\n")
            append("⚡ Muzaffarnagar Fast Delivery Available!")
        }
        shareText(context, "Deal Alert: $productName", body, productUrl)
    }

    fun shareShop(
        context: Context,
        shopName: String,
        category: String,
        address: String,
        phone: String,
        whatsapp: String? = null
    ) {
        val body = buildString {
            append("🛍️ Category: $category\n")
            append("📍 Address: $address, Muzaffarnagar\n")
            append("📞 Call: $phone\n")
            if (!whatsapp.isNullOrBlank()) {
                append("💬 WhatsApp: $whatsapp\n")
            }
            append("⭐ Verified Muzaffarnagar Merchant on MznLive")
        }
        shareText(context, "Shop Spotlight: $shopName", body, null)
    }

    fun shareReelOrVideo(
        context: Context,
        title: String,
        creator: String,
        videoUrl: String
    ) {
        val body = "🎬 Watch this Reel/Video by $creator on MznLive!"
        shareText(context, title, body, videoUrl)
    }
}
