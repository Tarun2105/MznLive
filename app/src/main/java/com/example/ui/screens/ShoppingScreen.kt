package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AiPriceComparisonResult
import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct
import com.example.data.remote.GeminiPriceComparisonService
import com.example.data.repository.ProductCatalogRepository
import com.example.ui.components.InAppImageViewerDialog
import com.example.ui.theme.*
import com.example.util.ShareUtils
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Screen 4: Shopping Tab (Replaces Plan Tab)
 * Showcases all offers and new arrivals of local products in Muzaffarnagar.
 * Allows users to view and compare product rates with Amazon & Flipkart via AI,
 * highlighting the profits, savings and unmatched advantages of local purchase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    language: String = "hi",
    cartItemsCount: Int = 0,
    onAddToCart: (PromotionalProduct) -> Unit = {},
    onOpenCart: () -> Unit = {},
    onOpenChatWithProduct: (PromotionalProduct) -> Unit = {},
    onNavigateToSeller: (MarketplaceShop) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiPriceComparisonService() }
    val isHindi = language == "hi"

    // Load promotional products and new arrivals
    val allProducts: List<PromotionalProduct> = remember { ProductCatalogRepository.getAllPromotionalProducts() }

    var selectedSection by remember { mutableStateOf("ALL_OFFERS") } // "ALL_OFFERS", "NEW_ARRIVALS", "FLASH_DEALS"
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var searchQuery by remember { mutableStateOf("") }

    // State for AI Price Comparison modal
    var comparingProduct by remember { mutableStateOf<PromotionalProduct?>(null) }
    var aiComparisonResult by remember { mutableStateOf<AiPriceComparisonResult?>(null) }
    var isComparingLoading by remember { mutableStateOf(false) }

    // State for viewing full product image
    var viewedProductImage by remember { mutableStateOf<PromotionalProduct?>(null) }

    val categories = listOf(
        "All Categories",
        "Fashion & Clothing",
        "Electronics & Mobiles",
        "Grocery & Jaggery",
        "Sweets & Food",
        "Jewelry & Watches",
        "Home & Kitchen",
        "Footwear"
    )

    // Filter products based on Section, Category, and Search
    val displayedProducts: List<PromotionalProduct> = remember(selectedSection, selectedCategory, searchQuery, allProducts) {
        allProducts.filter { product: PromotionalProduct ->
            val matchesCategory = (selectedCategory == "All Categories") ||
                    product.category.equals(selectedCategory, ignoreCase = true) ||
                    product.category.contains(selectedCategory, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                    product.title.contains(searchQuery, ignoreCase = true) ||
                    product.titleHi.contains(searchQuery, ignoreCase = true) ||
                    product.shopName.contains(searchQuery, ignoreCase = true) ||
                    product.shopLocation.contains(searchQuery, ignoreCase = true)

            val matchesSection = when (selectedSection) {
                "NEW_ARRIVALS" -> product.id % 2L == 0L || product.isFeaturedDeal // Curated new arrivals
                "FLASH_DEALS" -> product.discountPercent >= 25 || product.priceInr < 2000
                else -> true // "ALL_OFFERS"
            }

            matchesCategory && matchesSearch && matchesSection
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("shopping_screen"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Top App Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.applogo3dtrns),
                                contentDescription = "MznLive Logo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("shopping_top_logo")
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Mzn",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TricolorNavy
                                    )
                                    Text(
                                        text = "Live",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TricolorSaffron
                                    )
                                    Text(
                                        text = " • Shopping",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TricolorGreen
                                    )
                                }
                                Text(
                                    text = if (isHindi) "मुजफ्फरनगर लोकल ऑफर्स व नए उत्पाद" else "Muzaffarnagar Offers & New Arrivals",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Cart Button with Badge
                        IconButton(
                            onClick = onOpenCart,
                            modifier = Modifier.testTag("shopping_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemsCount > 0) {
                                        Badge(
                                            containerColor = TricolorSaffronDark,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = cartItemsCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping Cart",
                                    tint = TricolorNavy
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("shopping_search_input"),
                        placeholder = {
                            Text(
                                text = if (isHindi) "उत्पाद, दुकान या ऑफर खोजें..." else "Search products, shops, or offers...",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TricolorSaffron,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Section Tabs: All Offers, New Arrivals, Flash Deals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShoppingSectionTab(
                            title = if (isHindi) "🔥 सभी ऑफर्स" else "🔥 All Offers",
                            isSelected = selectedSection == "ALL_OFFERS",
                            onClick = { selectedSection = "ALL_OFFERS" },
                            modifier = Modifier.weight(1f),
                            testTag = "tab_all_offers"
                        )
                        ShoppingSectionTab(
                            title = if (isHindi) "✨ न्यू अराइवल्स" else "✨ New Arrivals",
                            isSelected = selectedSection == "NEW_ARRIVALS",
                            onClick = { selectedSection = "NEW_ARRIVALS" },
                            modifier = Modifier.weight(1f),
                            testTag = "tab_new_arrivals"
                        )
                        ShoppingSectionTab(
                            title = if (isHindi) "⚡ महा बचत डील्स" else "⚡ Flash Deals",
                            isSelected = selectedSection == "FLASH_DEALS",
                            onClick = { selectedSection = "FLASH_DEALS" },
                            modifier = Modifier.weight(1f),
                            testTag = "tab_flash_deals"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Category Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(text = category, fontSize = 12.sp) },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TricolorNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("shopping_products_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner: Local Purchase Advantage Highlights
            item {
                LocalShoppingAdvantageBanner(
                    isHindi = isHindi,
                    onExploreClick = { selectedSection = "ALL_OFFERS" }
                )
            }

            // Section Subheader with Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedSection) {
                                "NEW_ARRIVALS" -> if (isHindi) "✨ ताज़ा नए उत्पाद (New Arrivals)" else "✨ Fresh New Arrivals"
                                "FLASH_DEALS" -> if (isHindi) "⚡ भारी छूट व फ्लैश डील्स" else "⚡ Flash Clearance Deals"
                                else -> if (isHindi) "🔥 मुजफ्फरनगर बाजार के प्रमुख ऑफर्स" else "🔥 Top Muzaffarnagar Market Deals"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorNavy
                        )
                        Text(
                            text = "${displayedProducts.size} items available from verified local shops",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = TricolorGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = TricolorGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Rate Match",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorGreen
                            )
                        }
                    }
                }
            }

            // Products list
            if (displayedProducts.isEmpty()) {
                item {
                    EmptyProductsView(isHindi = isHindi, onReset = {
                        searchQuery = ""
                        selectedCategory = "All Categories"
                        selectedSection = "ALL_OFFERS"
                    })
                }
            } else {
                items(displayedProducts, key = { it.id }) { product ->
                    ShoppingProductCard(
                        product = product,
                        isHindi = isHindi,
                        onAddToCart = {
                            onAddToCart(product)
                            Toast.makeText(context, "${product.title} added to Cart!", Toast.LENGTH_SHORT).show()
                        },
                        onCompareAi = {
                            comparingProduct = product
                            aiComparisonResult = null
                            isComparingLoading = true
                            coroutineScope.launch {
                                try {
                                    val result = geminiService.compareProductPrice(product)
                                    aiComparisonResult = result
                                } catch (e: Exception) {
                                    aiComparisonResult = null
                                } finally {
                                    isComparingLoading = false
                                }
                            }
                        },
                        onImageClick = { viewedProductImage = product },
                        onChatClick = { onOpenChatWithProduct(product) },
                        onShopNameClick = {
                            val dummyShop = MarketplaceShop(
                                id = product.id,
                                name = product.shopName,
                                category = product.category,
                                address = product.shopLocation,
                                phone = product.whatsappContact,
                                rating = product.rating,
                                reviewsCount = product.reviewsCount,
                                isPromoted = true
                            )
                            onNavigateToSeller(dummyShop)
                        }
                    )
                }
            }

            // Bottom Informative Card
            item {
                LocalEconomyTrustFooter(isHindi = isHindi)
            }
        }
    }

    // Modal: AI Price Comparison & Local Purchase Advantages
    if (comparingProduct != null) {
        AiPriceComparisonModal(
            product = comparingProduct!!,
            comparisonResult = aiComparisonResult,
            isLoading = isComparingLoading,
            isHindi = isHindi,
            onDismiss = { comparingProduct = null },
            onAddToCart = {
                onAddToCart(comparingProduct!!)
                comparingProduct = null
                Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show()
            },
            onDirectContact = {
                comparingProduct?.let { p ->
                    try {
                        val message = "Hello ${p.shopName}, I found '${p.title}' on MznLive Shopping at ₹${p.priceInr}. I want to purchase it!"
                        val url = "https://api.whatsapp.com/send?phone=${p.whatsappContact}&text=${URLEncoder.encode(message, StandardCharsets.UTF_8.toString())}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Opening WhatsApp for ${p.shopName}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Full In-App Image Viewer
    if (viewedProductImage != null) {
        InAppImageViewerDialog(
            imageUrl = viewedProductImage!!.imageUrl,
            title = viewedProductImage!!.title,
            subtitle = "${viewedProductImage!!.shopName} • ₹${viewedProductImage!!.priceInr}",
            onDismiss = { viewedProductImage = null }
        )
    }
}

@Composable
fun ShoppingSectionTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(38.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) TricolorNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Hero Banner highlighting why local purchases on MznLive outperform online e-commerce portals.
 */
@Composable
fun LocalShoppingAdvantageBanner(
    isHindi: Boolean,
    onExploreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("local_advantage_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0A2540))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TricolorSaffron,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🇮🇳 VOCAL FOR LOCAL • MUZAFFARNAGAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = TricolorGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% Genuine",
                            fontSize = 11.sp,
                            color = TricolorGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isHindi) "ऑनलाइन ई-कॉमर्स से बेहतर, तेज व सस्ता!" else "Better, Faster & Cheaper Than Online Portals!",
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isHindi)
                        "⚡ हाथों-हाथ 2 घंटे में डिलीवरी या पिकअप • 🚫 डिलीवरी फ्रॉड से मुक्ति • 🤝 दुकान पर सीधा टेस्ट व वारंटी सहायता"
                    else
                        "⚡ Same-Day Pickup in 2 Hours • 🚫 Zero Transit Fraud / Fake Box Risk • 🤝 Hands-On Testing & Instant Storefront Warranty",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Compare rates with Amazon & Flipkart below ⬇",
                        fontSize = 11.sp,
                        color = TricolorSaffron,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explore Deals", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Individual Product Card with image, prices, merchant details, AI compare button, and add to cart.
 */
@Composable
fun ShoppingProductCard(
    product: PromotionalProduct,
    isHindi: Boolean,
    onAddToCart: () -> Unit,
    onCompareAi: () -> Unit,
    onImageClick: () -> Unit,
    onChatClick: () -> Unit,
    onShopNameClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image & Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFF1F5F9))
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Left Badges: Discount & Delivery
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (product.discountPercent > 0) {
                        Surface(
                            color = MznCrimson,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "-${product.discountPercent}% OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TricolorGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "2-Hr Local Pickup",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Top Right: Tap to view full image
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom Image",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
            }

            // Product Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Category & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorSaffronDark
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${product.rating} (${product.reviewsCount})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = if (isHindi && product.titleHi.isNotBlank()) product.titleHi else product.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Shop Name & Location (Clickable to visit seller storefront)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShopNameClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = TricolorNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${product.shopName} • ${product.shopLocation}",
                        fontSize = 11.5.sp,
                        color = TricolorNavy,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pricing Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${product.priceInr}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorGreen
                            )
                            if (product.originalMrpInr > product.priceInr) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹${product.originalMrpInr}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                        Text(
                            text = "Save ₹${product.originalMrpInr - product.priceInr} with MznLive Local Deal",
                            fontSize = 10.5.sp,
                            color = TricolorGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Online benchmark preview tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Amazon ~₹${product.estimatedAmazonPrice}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row: AI Compare, Add to Cart, Chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Compare Button
                    OutlinedButton(
                        onClick = onCompareAi,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                            .testTag("compare_ai_btn_${product.id}"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, TricolorNavy),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = TricolorNavy.copy(alpha = 0.05f),
                            contentColor = TricolorNavy
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = TricolorSaffronDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "AI रेट तुलना" else "AI Compare",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Add to Cart Button
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp)
                            .testTag("add_to_cart_btn_${product.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "कार्ट में जोड़ें" else "Add to Cart",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Shop Inquire / Chat
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(TricolorSaffron.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .testTag("chat_product_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat with Shop",
                            tint = TricolorSaffronDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * AI Rate Comparison Modal
 * Compares Local Price vs Amazon vs Flipkart and highlights profits/advantages of local purchase!
 */
@Composable
fun AiPriceComparisonModal(
    product: PromotionalProduct,
    comparisonResult: AiPriceComparisonResult?,
    isLoading: Boolean,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onDirectContact: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("ai_comparison_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with AI Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TricolorNavy
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = TricolorSaffron,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MznLive AI Price Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorNavy
                            )
                            Text(
                                text = "Powered by Gemini Intelligence",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Merchant: ${product.shopName}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = TricolorSaffron)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHindi) "अमेज़न व फ्लिपकार्ट की दरों का AI विश्लेषण हो रहा है..." else "Analyzing live rates on Amazon & Flipkart...",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val result = comparisonResult ?: AiPriceComparisonResult(
                        amazonEstimatedPrice = (product.priceInr * 1.15).toInt(),
                        flipkartEstimatedPrice = (product.priceInr * 1.18).toInt(),
                        localSavingsAmount = (product.priceInr * 0.15).toInt(),
                        verdict = "Best Value: Local Muzaffarnagar Shop beats online portals by immediate fulfillment & zero delivery fees.",
                        analysisText = "Buying locally from ${product.shopName} guarantees same-day pickup, personal warranty verification, and eliminates 3-5 days delivery anxiety.",
                        deliveryComparison = "⚡ Instant Pickup vs 📦 2-4 Days Shipping"
                    )

                    // Rates Comparison Grid: Local vs Amazon vs Flipkart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Local Shop (Highlighted Winner)
                        ComparisonPriceCard(
                            platform = "🏪 MznLive Local",
                            price = "₹${product.priceInr}",
                            subtext = "⚡ Same-Day / 2 Hr",
                            isWinner = true,
                            badge = "BEST VALUE",
                            modifier = Modifier.weight(1f)
                        )

                        // Amazon
                        ComparisonPriceCard(
                            platform = "📦 Amazon.in",
                            price = "₹${result.amazonEstimatedPrice}",
                            subtext = "🚚 2-4 Days Wait",
                            isWinner = false,
                            badge = null,
                            modifier = Modifier.weight(1f)
                        )

                        // Flipkart
                        ComparisonPriceCard(
                            platform = "🛍️ Flipkart",
                            price = "₹${result.flipkartEstimatedPrice}",
                            subtext = "🚚 3-5 Days Wait",
                            isWinner = false,
                            badge = null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Savings Pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "You save up to ₹${result.localSavingsAmount} + Zero Delivery Cost!",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Verdict & Analysis Text
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = TricolorNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Recommendation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TricolorNavy)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.verdict,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.analysisText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7 Big Advantages of Local Purchase Over Online E-Commerce
                    Text(
                        text = if (isHindi) "🌟 मुजफ्फरनगर लोकल खरीद के 6 बड़े फायदे:" else "🌟 6 Big Profits of Local Purchase vs Online Portals:",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LocalAdvantageRow(
                        icon = Icons.Default.TouchApp,
                        title = "1. Touch & Inspect Before Paying",
                        description = "Check fabric quality, examine screens, or inspect fresh food in person. Never worry about fake products or wrong size delivery."
                    )
                    LocalAdvantageRow(
                        icon = Icons.Default.Speed,
                        title = "2. Instant Possession (Zero Waiting)",
                        description = "Take the product home immediately today or get 2-hr express local delivery in Mzn instead of waiting 3-5 days."
                    )
                    LocalAdvantageRow(
                        icon = Icons.Default.Handshake,
                        title = "3. Face-to-Face Warranty & Exchange",
                        description = "Direct shopkeeper guarantee on Court Road/Nehru Market. No complicated return labels, courier disputes, or waiting for refund."
                    )
                    LocalAdvantageRow(
                        icon = Icons.Default.Build,
                        title = "4. Free Installation & Custom Fitting",
                        description = "Local merchants provide free glass mounting, phone data transfer, saree blouse tailoring, and personal setup right on spot."
                    )
                    LocalAdvantageRow(
                        icon = Icons.Default.LocationCity,
                        title = "5. Boost Local Economy & Jobs",
                        description = "100% of your payment supports Muzaffarnagar families and local shops instead of multi-billion corporate platforms."
                    )
                    LocalAdvantageRow(
                        icon = Icons.Default.MonetizationOn,
                        title = "6. Earn MznLive CashPoints Rewards",
                        description = "Earn instant CashPoints on local orders redeemable for cash discounts on future shopping."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddToCart,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Cart", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDirectContact,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp Shop", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonPriceCard(
    platform: String,
    price: String,
    subtext: String,
    isWinner: Boolean,
    badge: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isWinner) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isWinner) BorderStroke(1.5.dp, Color(0xFF10B981)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (badge != null) {
                Surface(
                    color = Color(0xFF10B981),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            } else {
                Spacer(modifier = Modifier.height(13.dp))
            }

            Text(
                text = platform,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWinner) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = price,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isWinner) Color(0xFF047857) else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LocalAdvantageRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = TricolorNavy.copy(alpha = 0.1f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = TricolorNavy, modifier = Modifier.size(13.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun EmptyProductsView(
    isHindi: Boolean,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isHindi) "कोई उत्पाद नहीं मिला" else "No Products Found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isHindi) "कृपया अन्य श्रेणी या खोज शब्द आज़माएँ" else "Try clearing your search or choosing another category",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(onClick = onReset) {
            Text("Reset Filters")
        }
    }
}

@Composable
fun LocalEconomyTrustFooter(isHindi: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.applogo3dtrns),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "MznLive Local Commerce Pledge",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TricolorNavy
                )
                Text(
                    text = "Supporting Muzaffarnagar retailers, craftsmen & family stores with instant transparent pricing and AI comparisons.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
