package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.ChatMessage
import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository abstracting Room Database access for Users, Shops, Products,
 * and In-App / SMS / WhatsApp Chat Messages.
 */
class MarketplaceDatabaseRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val shopDao = db.shopDao()
    private val productDao = db.productDao()
    private val chatMessageDao = db.chatMessageDao()

    // Reactive streams mapped to domain models
    val allShops: Flow<List<MarketplaceShop>> = shopDao.getAllShops().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val allProducts: Flow<List<PromotionalProduct>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    val rawShopsList: Flow<List<ShopEntity>> = shopDao.getAllShops()

    val rawProductsList: Flow<List<ProductEntity>> = productDao.getAllProducts()

    val allChatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val rawChatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()

    fun getProductsForShop(shopName: String): Flow<List<PromotionalProduct>> {
        return productDao.getProductsByShopName(shopName).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getShopByOwnerMobile(mobile: String): Flow<MarketplaceShop?> {
        return shopDao.getShopByOwnerMobile(mobile).map { it?.toDomainModel() }
    }

    fun getShopByName(name: String): Flow<MarketplaceShop?> {
        return shopDao.getShopByName(name).map { it?.toDomainModel() }
    }

    suspend fun registerUser(
        role: String,
        name: String,
        businessName: String,
        category: String,
        address: String,
        mobile: String,
        whatsapp: String
    ): Pair<Long, Long?> = withContext(Dispatchers.IO) {
        val userEntity = UserEntity(
            role = role,
            name = name.trim(),
            businessName = businessName.trim(),
            category = category.trim(),
            address = address.trim(),
            mobile = mobile.trim(),
            whatsapp = whatsapp.trim().ifBlank { mobile.trim() },
            createdAt = System.currentTimeMillis()
        )
        val userId = userDao.insertUser(userEntity)

        var shopId: Long? = null
        if (role == "SHOP_OWNER" && businessName.isNotBlank()) {
            val cleanCat = category.ifBlank { "Retails & Kirana (खुदरा और किराना)" }
            val shopEntity = ShopEntity(
                ownerMobile = mobile.trim(),
                ownerName = name.trim(),
                shopName = businessName.trim(),
                category = cleanCat,
                address = address.trim().ifBlank { "Main Market, Muzaffarnagar" },
                phone = mobile.trim(),
                whatsapp = whatsapp.trim().ifBlank { mobile.trim() },
                description = "Official online store of ${businessName.trim()} in Muzaffarnagar. Verified Merchant.",
                rating = 4.9f,
                imageUrl = getCategoryDefaultImage(cleanCat),
                bannerUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&auto=format&fit=crop&q=80",
                featuredOffer = "Grand Opening Special Discount: Up to 25% OFF on first local order!",
                isVerified = true,
                createdAt = System.currentTimeMillis()
            )
            shopId = shopDao.insertShop(shopEntity)
        }

        Pair(userId, shopId)
    }

    suspend fun addProduct(
        shopId: Long = 0,
        shopName: String,
        title: String,
        titleHi: String = "",
        category: String,
        description: String,
        priceInr: Int,
        originalMrpInr: Int,
        imageUrl: String,
        phone: String = "",
        whatsapp: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val productEntity = ProductEntity(
            shopId = shopId,
            shopName = shopName.trim(),
            title = title.trim(),
            titleHi = titleHi.trim(),
            category = category.trim().ifBlank { "All" },
            description = description.trim(),
            priceInr = priceInr.coerceAtLeast(1),
            originalMrpInr = originalMrpInr.coerceAtLeast(priceInr),
            imageUrl = imageUrl.trim().ifBlank { getCategoryDefaultImage(category) },
            phone = phone.trim(),
            whatsapp = whatsapp.trim(),
            isFeatured = true,
            createdAt = System.currentTimeMillis()
        )
        productDao.insertProduct(productEntity)
    }

    suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    suspend fun logChatMessage(
        shopId: Long = 0,
        shopName: String,
        customerPhone: String = "",
        sender: String, // "user" or "shop"
        message: String,
        channel: String = "APP" // "APP", "SMS", "WHATSAPP"
    ): Long = withContext(Dispatchers.IO) {
        val chatEntity = ChatMessageEntity(
            shopId = shopId,
            shopName = shopName,
            customerPhone = customerPhone,
            sender = sender,
            message = message.trim(),
            channel = channel,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(chatEntity)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Seed default shops if empty
        val defaultShops = listOf(
            ShopEntity(
                ownerMobile = "9897123456",
                ownerName = "Rajesh Singhal",
                shopName = "Singhal Electronics & Mobiles",
                category = "Electronics & Appliances (इलेक्ट्रॉनिक्स)",
                address = "Shiv Chowk, Court Road, Muzaffarnagar",
                phone = "9897123456",
                whatsapp = "9897123456",
                description = "Authorised dealer for Smart TVs, 5G Smartphones, Laptops, ACs & Home Appliances with zero-percent EMI.",
                rating = 4.9f,
                imageUrl = "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=500&auto=format&fit=crop&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=800&auto=format&fit=crop&q=80",
                featuredOffer = "Flat ₹2,500 Instant Cashback on 5G Mobiles + Free Tempered Glass",
                isVerified = true
            ),
            ShopEntity(
                ownerMobile = "9837234567",
                ownerName = "Sunil Kumar Gupta",
                shopName = "Gupta Sweets & Namkeen",
                category = "Food, Sweets & Bakery (खाद्य व मिष्ठान)",
                address = "Bhagwan Mahavir Marg, New Mandi, Muzaffarnagar",
                phone = "9837234567",
                whatsapp = "9837234567",
                description = "Famous Pure Desi Ghee Jalebi, Motichoor Ladoo, Kaju Katli and fresh morning breakfast snacks.",
                rating = 4.8f,
                imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1517244683847-7456b63c5969?w=800&auto=format&fit=crop&q=80",
                featuredOffer = "Pure Desi Ghee Sweets @ 15% OFF for Local Festivities & Weddings",
                isVerified = true
            ),
            ShopEntity(
                ownerMobile = "9719345678",
                ownerName = "Vikas Jain",
                shopName = "Jain Silk Sarees & Ethnic Wear",
                category = "Clothing & Fashion (कपड़े और फैशन)",
                address = "Jhansi Rani Market, Muzaffarnagar",
                phone = "9719345678",
                whatsapp = "9719345678",
                description = "Exclusive bridal lehengas, pure Banarasi silk sarees, kurtis, and designer wedding sherwanis.",
                rating = 4.9f,
                imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=500&auto=format&fit=crop&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&auto=format&fit=crop&q=80",
                featuredOffer = "Wedding Season Sale: Buy 2 Sarees & Get Matching Dupatta Free!",
                isVerified = true
            ),
            ShopEntity(
                ownerMobile = "9897456789",
                ownerName = "Dinesh Chandra",
                shopName = "Chandra Kirana & Dry Fruits",
                category = "Retails & Kirana (खुदरा और किराना)",
                address = "Gole Market, Muzaffarnagar",
                phone = "9897456789",
                whatsapp = "9897456789",
                description = "Premium dry fruits, organic spices, pulses, rice, and complete monthly grocery packets with free door delivery.",
                rating = 4.7f,
                imageUrl = "https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=500&auto=format&fit=crop&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80",
                featuredOffer = "Free Same-Day Home Delivery on all grocery orders above ₹999",
                isVerified = true
            ),
            ShopEntity(
                ownerMobile = "9837567890",
                ownerName = "Pawan Verma",
                shopName = "Verma Jewellers",
                category = "Jewellery & Ornaments (ज्वेलरी व आभूषण)",
                address = "Sarafa Bazar, Muzaffarnagar",
                phone = "9837567890",
                whatsapp = "9837567890",
                description = "100% BIS Hallmarked 916 Gold jewellery, certified diamond rings, and pure silver artifacts.",
                rating = 5.0f,
                imageUrl = "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=500&auto=format&fit=crop&q=80",
                bannerUrl = "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=800&auto=format&fit=crop&q=80",
                featuredOffer = "Zero Making Charges on select Diamond & Gold Jewellery Sets",
                isVerified = true
            )
        )

        val defaultProducts = listOf(
            ProductEntity(
                shopId = 1,
                shopName = "Singhal Electronics & Mobiles",
                title = "OnePlus Nord CE 4 Lite 5G (8GB RAM, 128GB)",
                titleHi = "वनप्लस नॉर्ड 5G स्मार्टफोन",
                category = "Electronics & Appliances (इलेक्ट्रॉनिक्स)",
                description = "5500 mAh battery, 80W SuperVOOC fast charging, 120Hz AMOLED display. Brand warranty with local repair support.",
                priceInr = 17999,
                originalMrpInr = 20999,
                imageUrl = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500&auto=format&fit=crop&q=80",
                phone = "9897123456",
                whatsapp = "9897123456",
                isFeatured = true
            ),
            ProductEntity(
                shopId = 1,
                shopName = "Singhal Electronics & Mobiles",
                title = "Samsung 43-inch Crystal 4K UHD Smart TV",
                titleHi = "सैमसंग 43 इंच स्मार्ट टीवी",
                category = "Electronics & Appliances (इलेक्ट्रॉनिक्स)",
                description = "Crystal Processor 4K, HDR 10+, Dolby Digital Plus Audio, Voice remote with Netflix and Prime built-in.",
                priceInr = 28499,
                originalMrpInr = 34990,
                imageUrl = "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=500&auto=format&fit=crop&q=80",
                phone = "9897123456",
                whatsapp = "9897123456",
                isFeatured = true
            ),
            ProductEntity(
                shopId = 2,
                shopName = "Gupta Sweets & Namkeen",
                title = "Shuddh Desi Ghee Kaju Katli (1 Kg Special Box)",
                titleHi = "शुद्ध देसी घी काजू कतली",
                category = "Food, Sweets & Bakery (खाद्य व मिष्ठान)",
                description = "Handcrafted with premium Goan cashews, no artificial sugar syrup, garnished with edible silver vark.",
                priceInr = 850,
                originalMrpInr = 980,
                imageUrl = "https://images.unsplash.com/photo-1599785209707-a456fc1337bb?w=500&auto=format&fit=crop&q=80",
                phone = "9837234567",
                whatsapp = "9837234567",
                isFeatured = true
            ),
            ProductEntity(
                shopId = 3,
                shopName = "Jain Silk Sarees & Ethnic Wear",
                title = "Banarasi Katan Silk Bridal Saree (Crimson Red)",
                titleHi = "बनारसी सिल्क दुल्हन साड़ी",
                category = "Clothing & Fashion (कपड़े और फैशन)",
                description = "Zari floral jaal pattern, rich pallu with unstitched blouse piece. Perfect for weddings and auspicious functions.",
                priceInr = 4250,
                originalMrpInr = 6200,
                imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=500&auto=format&fit=crop&q=80",
                phone = "9719345678",
                whatsapp = "9719345678",
                isFeatured = true
            ),
            ProductEntity(
                shopId = 4,
                shopName = "Chandra Kirana & Dry Fruits",
                title = "California Jumbo Almonds (Badam Giri 1 Kg)",
                titleHi = "कैलिफ़ोर्निया बादाम गिरी (1 किग्रा)",
                category = "Retails & Kirana (खुदरा और किराना)",
                description = "100% natural, crisp and nutrient dense rich almonds. Vacuum packed for prolonged freshness.",
                priceInr = 780,
                originalMrpInr = 950,
                imageUrl = "https://images.unsplash.com/photo-1508061252445-5350f3722961?w=500&auto=format&fit=crop&q=80",
                phone = "9897456789",
                whatsapp = "9897456789",
                isFeatured = true
            )
        )

        val defaultUsers = listOf(
            UserEntity(
                role = "SHOP_OWNER",
                name = "Rajesh Singhal",
                businessName = "Singhal Electronics & Mobiles",
                category = "Electronics & Appliances (इलेक्ट्रॉनिक्स)",
                address = "Shiv Chowk, Court Road, Muzaffarnagar",
                mobile = "9897123456",
                whatsapp = "9897123456"
            ),
            UserEntity(
                role = "SHOP_OWNER",
                name = "Sunil Kumar Gupta",
                businessName = "Gupta Sweets & Namkeen",
                category = "Food, Sweets & Bakery (खाद्य व मिष्ठान)",
                address = "Bhagwan Mahavir Marg, New Mandi, Muzaffarnagar",
                mobile = "9837234567",
                whatsapp = "9837234567"
            ),
            UserEntity(
                role = "USER",
                name = "Amit Sharma",
                businessName = "",
                category = "",
                address = "Civil Lines, Muzaffarnagar",
                mobile = "9876543210",
                whatsapp = "9876543210"
            )
        )

        // Insert initial shops & products if empty
        shopDao.insertShops(defaultShops)
        productDao.insertProducts(defaultProducts)
        defaultUsers.forEach { userDao.insertUser(it) }

        // Initial welcome chat message
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                shopId = 1,
                shopName = "Singhal Electronics & Mobiles",
                customerPhone = "9876543210",
                sender = "shop",
                message = "Namaste! Welcome to Singhal Electronics online portal. Let us know if you need price quotes, home delivery, or product demonstration.",
                channel = "APP",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
    }

    private fun getCategoryDefaultImage(category: String): String {
        return when {
            category.contains("Electronics", ignoreCase = true) ->
                "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=500&auto=format&fit=crop&q=80"
            category.contains("Food", ignoreCase = true) || category.contains("Sweets", ignoreCase = true) ->
                "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop&q=80"
            category.contains("Clothing", ignoreCase = true) || category.contains("Fashion", ignoreCase = true) ->
                "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=500&auto=format&fit=crop&q=80"
            category.contains("Jewellery", ignoreCase = true) ->
                "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=500&auto=format&fit=crop&q=80"
            else ->
                "https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=500&auto=format&fit=crop&q=80"
        }
    }
}

// Extension functions to convert entities to domain models
fun ShopEntity.toDomainModel(): MarketplaceShop {
    return MarketplaceShop(
        id = id,
        name = shopName,
        category = category,
        address = address,
        phone = phone,
        whatsapp = whatsapp,
        instagramLink = "https://instagram.com",
        facebookLink = "https://facebook.com",
        rating = rating,
        imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=500&auto=format&fit=crop&q=80" },
        featuredOffer = featuredOffer.ifBlank { "Exclusive local store discounts available today!" }
    )
}

fun ProductEntity.toDomainModel(): PromotionalProduct {
    return PromotionalProduct(
        id = id,
        title = title,
        titleHi = titleHi,
        category = category,
        shopName = shopName,
        shopLocation = "Muzaffarnagar",
        priceInr = priceInr,
        originalMrpInr = originalMrpInr,
        imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80" },
        description = description,
        whatsappContact = whatsapp.ifBlank { phone }
    )
}

fun ChatMessageEntity.toDomainModel(): ChatMessage {
    return ChatMessage(
        id = id,
        shopId = shopId,
        shopName = shopName,
        sender = sender,
        message = message,
        timestamp = timestamp
    )
}
