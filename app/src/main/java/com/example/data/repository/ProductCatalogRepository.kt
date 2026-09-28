package com.example.data.repository

import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct

data class CartItem(
    val product: PromotionalProduct,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

object ProductCatalogRepository {

    // Comprehensive catalog of products mapped to local Muzaffarnagar merchants
    private val allProducts = listOf(
        // 1. Royal Heritage Silk Sarees (Court Road)
        PromotionalProduct(
            id = 101,
            title = "Banarasi Pure Silk Embroidered Bridal Saree",
            titleHi = "बनारसी सिल्क दुल्हन साड़ी",
            category = "Fashion & Clothing",
            shopName = "Royal Heritage Silk Sarees",
            shopLocation = "14 Court Road, Near Clock Tower, Muzaffarnagar",
            priceInr = 1899,
            originalMrpInr = 3499,
            imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 280,
            description = "Intricate golden zari work with traditional floral border. Comes with matching unstitched blouse piece. Special festive discount.",
            whatsappContact = "919876500001",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Instant Pickup / 2-Hr Delivery in Mzn"
        ),
        PromotionalProduct(
            id = 1012,
            title = "Kanjeevaram Gold Zari Festive Silk Saree",
            titleHi = "कांचीवरम गोल्ड जरी सिल्क साड़ी",
            category = "Fashion & Clothing",
            shopName = "Royal Heritage Silk Sarees",
            shopLocation = "14 Court Road, Near Clock Tower, Muzaffarnagar",
            priceInr = 2499,
            originalMrpInr = 4999,
            imageUrl = "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?w=600&auto=format&fit=crop&q=80",
            rating = 4.8f,
            reviewsCount = 195,
            description = "Handcrafted pure silk with contrast pallu and heavy border. Designed for weddings and special occasions.",
            whatsappContact = "919876500001",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Same-Day Store Delivery"
        ),
        PromotionalProduct(
            id = 1013,
            title = "Chanderi Floral Handloom Summer Saree",
            titleHi = "चंदेरी फ्लोरल हैंडलूम साड़ी",
            category = "Fashion & Clothing",
            shopName = "Royal Heritage Silk Sarees",
            shopLocation = "14 Court Road, Near Clock Tower, Muzaffarnagar",
            priceInr = 1299,
            originalMrpInr = 2299,
            imageUrl = "https://images.unsplash.com/photo-1609357605129-26f69add5d6e?w=600&auto=format&fit=crop&q=80",
            rating = 4.7f,
            reviewsCount = 142,
            description = "Lightweight breathable Chanderi cotton silk with delicate pastel floral prints. Very comfortable for day wear.",
            whatsappContact = "919876500001",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ 2-Hour Express Delivery"
        ),

        // 2. Galaxy Electronics & Smart Hub / Gupta Electronics (Nehru Market / Bhagat Singh Road)
        PromotionalProduct(
            id = 102,
            title = "OnePlus Nord CE4 Lite 5G (8GB RAM, 128GB)",
            titleHi = "वनप्लस नॉर्ड 5G स्मार्टफोन",
            category = "Electronics & Mobiles",
            shopName = "Galaxy Electronics & Smart Hub",
            shopLocation = "Shop 22, Nehru Market Complex, Muzaffarnagar",
            priceInr = 17499,
            originalMrpInr = 20999,
            imageUrl = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600&auto=format&fit=crop&q=80",
            rating = 4.8f,
            reviewsCount = 340,
            description = "5500 mAh battery with 80W SUPERVOOC charging, 120Hz AMOLED display, Sony LYT-600 50MP OIS camera. Official 1-year brand warranty.",
            whatsappContact = "919876500003",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Same-Day Store Pickup & Free Setup"
        ),
        PromotionalProduct(
            id = 104,
            title = "Noise Pulse 2 Max 1.85\" Bluetooth Calling Smartwatch",
            titleHi = "नॉइज़ स्मार्टवॉच ब्लूटूथ कॉलिंग",
            category = "Electronics & Mobiles",
            shopName = "Galaxy Electronics & Smart Hub",
            shopLocation = "Shop 22, Nehru Market Complex, Muzaffarnagar",
            priceInr = 1199,
            originalMrpInr = 2999,
            imageUrl = "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&auto=format&fit=crop&q=80",
            rating = 4.7f,
            reviewsCount = 190,
            description = "Massive 1.85\" bright TFT LCD, 550 nits brightness, 10-day battery life, 100 sports modes with heart rate & SpO2 tracking.",
            whatsappContact = "919876500003",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Same-Day Delivery in Mzn"
        ),
        PromotionalProduct(
            id = 106,
            title = "boAt Rockerz 450 Pro On-Ear Wireless Headphones",
            titleHi = "बोट वायरलेस हेडफ़ोन 70 घंटे बैटरी",
            category = "Electronics & Mobiles",
            shopName = "Galaxy Electronics & Smart Hub",
            shopLocation = "Shop 22, Nehru Market Complex, Muzaffarnagar",
            priceInr = 1499,
            originalMrpInr = 2990,
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
            rating = 4.7f,
            reviewsCount = 310,
            description = "Up to 70 hours non-stop playback, 40mm dynamic drivers with boAt Signature Sound, ASAP fast charging via Type-C.",
            whatsappContact = "919876500003",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Same-Day Delivery in Mzn"
        ),

        // 3. Shree Balaji Sweets & Bakery (Main Market)
        PromotionalProduct(
            id = 107,
            title = "Kalyan Special Pure Desi Ghee Doda Barfi (1 Kg Box)",
            titleHi = "कल्याण स्पेशल शुद्ध देशी घी डोडा बर्फी",
            category = "Food & Sweets",
            shopName = "Shree Balaji Sweets & Bakery",
            shopLocation = "Opposite G.P.O, Main Market, Muzaffarnagar",
            priceInr = 580,
            originalMrpInr = 750,
            imageUrl = "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 620,
            description = "Authentic heritage recipe made with rich sprouted wheat, khoya, dry fruits and pure desi cow ghee. Melt-in-mouth freshness guaranteed.",
            whatsappContact = "919876500002",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Freshly Packed Daily • 1-Hour Local Delivery"
        ),
        PromotionalProduct(
            id = 103,
            title = "Muzaffarnagar Organic Pure Desi Shakkar & Jaggery (5 Kg)",
            titleHi = "मुजफ्फरनगर की प्रसिद्ध शुद्ध देशी शक्कर एवं गुड़",
            category = "Food & Sweets",
            shopName = "Shree Balaji Sweets & Bakery",
            shopLocation = "Opposite G.P.O, Main Market, Muzaffarnagar",
            priceInr = 349,
            originalMrpInr = 550,
            imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 512,
            description = "100% natural, chemical-free sugarcane gur directly from the sugar capital of India. Rich in iron and minerals.",
            whatsappContact = "919876500002",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Fresh Daily Stock • Same-Day Delivery"
        ),
        PromotionalProduct(
            id = 1072,
            title = "Shudh Kaju Katli Premium Royal Gift Box (1 Kg)",
            titleHi = "शुद्ध काजू कतली प्रीमियम गिफ्ट बॉक्स",
            category = "Food & Sweets",
            shopName = "Shree Balaji Sweets & Bakery",
            shopLocation = "Opposite G.P.O, Main Market, Muzaffarnagar",
            priceInr = 799,
            originalMrpInr = 950,
            imageUrl = "https://images.unsplash.com/photo-1599785209707-a456fc1337bb?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 410,
            description = "Premium selected Goan cashews ground with pure silver vark. Delicious festive sweet packaged in airtight royal gift box.",
            whatsappContact = "919876500002",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ 1-Hour Express Local Delivery"
        ),

        // 4. Amritsari Zayka Restaurant (Civil Lines)
        PromotionalProduct(
            id = 1081,
            title = "Amritsari Stuffed Chur Chur Naan & Dal Makhani Combo",
            titleHi = "अमृतसरी चूर चूर नान एवं दाल मखनी कॉम्बो",
            category = "Restaurants & Cafes",
            shopName = "Amritsari Zayka Restaurant",
            shopLocation = "Civil Lines, Near Subhash Chowk, Muzaffarnagar",
            priceInr = 240,
            originalMrpInr = 320,
            imageUrl = "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 380,
            description = "Crispy buttered stuffed naan served with 12-hour slow cooked black lentils in pure butter, tangy pickle and mint chutney.",
            whatsappContact = "919876500005",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Hot & Fresh Delivery in 30 Mins"
        ),
        PromotionalProduct(
            id = 1082,
            title = "Family Royal Feast Thali Box (Serves 3-4)",
            titleHi = "शाही पारिवारिक थाली भोजन बॉक्स",
            category = "Restaurants & Cafes",
            shopName = "Amritsari Zayka Restaurant",
            shopLocation = "Civil Lines, Near Subhash Chowk, Muzaffarnagar",
            priceInr = 599,
            originalMrpInr = 850,
            imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600&auto=format&fit=crop&q=80",
            rating = 4.8f,
            reviewsCount = 295,
            description = "Paneer butter masala, Dal makhani, Jeera rice, 4 butter rotis, Gulab jamun and fresh salad. Packed in spill-proof containers.",
            whatsappContact = "919876500005",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Hot & Fresh Delivery in 35 Mins"
        ),

        // 5. Murad Artisans Store (Purani Tehsil Market)
        PromotionalProduct(
            id = 1091,
            title = "Handcrafted Sheesham Wood Brass Inlay Jewelry Box",
            titleHi = "हाथ से बनी शीशम की लकड़ी की ज्वैलरी बॉक्स",
            category = "Home Decor & Handicrafts",
            shopName = "Murad Artisans Store",
            shopLocation = "Purani Tehsil Market, Muzaffarnagar",
            priceInr = 699,
            originalMrpInr = 1200,
            imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80",
            rating = 4.7f,
            reviewsCount = 160,
            description = "Solid seasoned Sheesham hardwood with exquisite floral brass wire inlay. Velvet-lined interior with secure brass latch.",
            whatsappContact = "919876500006",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Same-Day Delivery in Mzn"
        ),
        PromotionalProduct(
            id = 1092,
            title = "Vintage Hand-Carved Wooden Jharokha Mirror Frame",
            titleHi = "प्राचीन नक्काशीदार लकड़ी का झरोखा दर्पण",
            category = "Home Decor & Handicrafts",
            shopName = "Murad Artisans Store",
            shopLocation = "Purani Tehsil Market, Muzaffarnagar",
            priceInr = 1399,
            originalMrpInr = 2499,
            imageUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600&auto=format&fit=crop&q=80",
            rating = 4.8f,
            reviewsCount = 115,
            description = "Traditional Rajasthani window arch frame hand-carved by local master craftsmen. Adds a majestic royal look to living rooms.",
            whatsappContact = "919876500006",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Same-Day Delivery in Mzn"
        ),

        // 6. Superfoot Shoemakers (Ansari Road)
        PromotionalProduct(
            id = 108,
            title = "Sparx Men's Ultra-Light Breathable Running & Gym Shoes",
            titleHi = "स्पार्क्स मेन्स रनिंग जूते",
            category = "Footwear",
            shopName = "Superfoot Shoemakers",
            shopLocation = "Ansari Road, Muzaffarnagar",
            priceInr = 849,
            originalMrpInr = 1499,
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80",
            rating = 4.6f,
            reviewsCount = 175,
            description = "Cushioned EVA memory foam sole with breathable mesh upper. Ideal for morning walks, jogging, gym workouts and casual daily wear.",
            whatsappContact = "919897667788",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Store Trial & Same-Day Delivery"
        ),

        // 7. Aggarwal Home Appliances (Shamli Road)
        PromotionalProduct(
            id = 105,
            title = "Prestige Iris 750W Mixer Grinder (3 Stainless Steel Jars)",
            titleHi = "प्रेस्टीज 750W मिक्सर ग्राइंडर",
            category = "Home & Kitchen",
            shopName = "Aggarwal Home Appliances",
            shopLocation = "Shamli Road, Muzaffarnagar",
            priceInr = 2749,
            originalMrpInr = 3895,
            imageUrl = "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=600&auto=format&fit=crop&q=80",
            rating = 4.8f,
            reviewsCount = 145,
            description = "Heavy duty 750 watt motor with overload protection, 3 multi-utility stainless steel jars and 1 transparent juicer jar. 2 years warranty.",
            whatsappContact = "919837223344",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Same-Day Delivery & Free Installation Demo"
        )
    )

    fun getAllProducts(): List<PromotionalProduct> = allProducts

    fun getProductsForShop(shopName: String): List<PromotionalProduct> {
        val cleanShop = shopName.trim().lowercase()
        val matched = allProducts.filter {
            val pShop = it.shopName.lowercase()
            pShop.contains(cleanShop) || cleanShop.contains(pShop) ||
                    (cleanShop.contains("royal") && pShop.contains("royal")) ||
                    (cleanShop.contains("saree") && pShop.contains("saree")) ||
                    (cleanShop.contains("balaji") && pShop.contains("balaji")) ||
                    (cleanShop.contains("sweets") && pShop.contains("sweets")) ||
                    (cleanShop.contains("galaxy") && pShop.contains("galaxy")) ||
                    (cleanShop.contains("gupta") && pShop.contains("electronics")) ||
                    (cleanShop.contains("amritsari") && pShop.contains("amritsari")) ||
                    (cleanShop.contains("murad") && pShop.contains("murad")) ||
                    (cleanShop.contains("superfoot") && pShop.contains("superfoot")) ||
                    (cleanShop.contains("aggarwal") && pShop.contains("aggarwal"))
        }

        return if (matched.isNotEmpty()) {
            matched
        } else {
            // Fallback: provide featured products so seller always has items to show
            allProducts.take(4)
        }
    }

    fun findShopForMedia(
        shopName: String?,
        title: String?,
        availableShops: List<MarketplaceShop>
    ): MarketplaceShop {
        val query = (shopName ?: title ?: "").trim().lowercase()

        // 1. Direct match by shopName in availableShops
        val directMatch = availableShops.firstOrNull { shop ->
            val sName = shop.name.lowercase()
            query.contains(sName) || sName.contains(query) ||
                    (query.contains("royal") && sName.contains("royal")) ||
                    (query.contains("balaji") && sName.contains("balaji")) ||
                    (query.contains("galaxy") && sName.contains("galaxy")) ||
                    (query.contains("amritsari") && sName.contains("amritsari")) ||
                    (query.contains("murad") && sName.contains("murad"))
        }
        if (directMatch != null) return directMatch

        // 2. Keyword based match from title
        if (query.contains("saree") || query.contains("silk") || query.contains("bridal") || query.contains("dress") || query.contains("fashion")) {
            return availableShops.firstOrNull { it.category.contains("Fashion", ignoreCase = true) }
                ?: availableShops.firstOrNull() ?: fallbackShop(1, "Royal Heritage Silk Sarees", "Fashion & Clothing")
        }
        if (query.contains("sweet") || query.contains("mithai") || query.contains("gur") || query.contains("jaggery") || query.contains("doda")) {
            return availableShops.firstOrNull { it.category.contains("Food", ignoreCase = true) || it.category.contains("Sweets", ignoreCase = true) }
                ?: fallbackShop(2, "Shree Balaji Sweets & Bakery", "Food & Sweets")
        }
        if (query.contains("phone") || query.contains("mobile") || query.contains("tech") || query.contains("smart") || query.contains("gadget")) {
            return availableShops.firstOrNull { it.category.contains("Electronics", ignoreCase = true) }
                ?: fallbackShop(3, "Galaxy Electronics & Smart Hub", "Electronics & Mobiles")
        }
        if (query.contains("food") || query.contains("thali") || query.contains("restaurant") || query.contains("cafe") || query.contains("naan")) {
            return availableShops.firstOrNull { it.category.contains("Restaurant", ignoreCase = true) }
                ?: fallbackShop(4, "Amritsari Zayka Restaurant", "Restaurants & Cafes")
        }
        if (query.contains("decor") || query.contains("wood") || query.contains("art") || query.contains("handicraft")) {
            return availableShops.firstOrNull { it.category.contains("Decor", ignoreCase = true) }
                ?: fallbackShop(5, "Murad Artisans Store", "Home Decor & Handicrafts")
        }

        // Default to first available shop or primary merchant
        return availableShops.firstOrNull() ?: fallbackShop(1, "Royal Heritage Silk Sarees", "Fashion & Clothing")
    }

    fun findProductForMedia(shopName: String?, title: String?): PromotionalProduct {
        val query = (shopName ?: title ?: "").trim().lowercase()

        // Match by title keywords
        val titleMatch = allProducts.firstOrNull { p ->
            val pTitle = p.title.lowercase()
            query.contains("saree") && pTitle.contains("saree") ||
                    query.contains("phone") && pTitle.contains("nord") ||
                    query.contains("watch") && pTitle.contains("watch") ||
                    query.contains("sweet") && pTitle.contains("barfi") ||
                    query.contains("gur") && pTitle.contains("jaggery") ||
                    query.contains("shoe") && pTitle.contains("shoe") ||
                    query.contains("mixer") && pTitle.contains("mixer") ||
                    query.contains("naan") && pTitle.contains("naan") ||
                    query.contains("wood") && pTitle.contains("wood")
        }
        if (titleMatch != null) return titleMatch

        // Match by shop name
        val shopProducts = getProductsForShop(shopName ?: "")
        return shopProducts.firstOrNull() ?: allProducts.first()
    }

    fun getAllPromotionalProducts(): List<PromotionalProduct> = allProducts

    private fun fallbackShop(id: Long, name: String, category: String) = MarketplaceShop(
        id = id,
        name = name,
        category = category,
        address = "Main Market Hub, Muzaffarnagar",
        phone = "+919876500001",
        whatsapp = "+919876500001",
        instagramLink = "https://instagram.com/mznlive",
        facebookLink = "https://facebook.com/mznlive",
        rating = 4.9f,
        imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&auto=format&fit=crop&q=80",
        featuredOffer = "Special 25% discount for Mznlive app users"
    )
}
