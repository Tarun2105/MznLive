package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.util.emulatorScrollable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AiPriceComparisonResult
import com.example.data.model.PromotionalProduct
import com.example.data.remote.GeminiPriceComparisonService
import com.example.ui.components.InAppImageViewerDialog
import com.example.ui.theme.*
import com.example.util.ShareUtils
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsScreen(
    onNavigateToPlan: () -> Unit = {},
    onBuyProductWithRazorpay: (PromotionalProduct, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiPriceComparisonService() }

    // Catalog of local promotional products & deals
    val allProducts = remember { getPromotionalProductsCatalog() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Deals") }

    // State for AI Price Comparison Modal
    var comparingProduct by remember { mutableStateOf<PromotionalProduct?>(null) }
    var aiComparisonResult by remember { mutableStateOf<AiPriceComparisonResult?>(null) }
    var isComparingLoading by remember { mutableStateOf(false) }

    // State for Direct Buy Modal
    var buyingProduct by remember { mutableStateOf<PromotionalProduct?>(null) }

    // In-app media viewing for product images (stays inside app portal)
    var viewedProductImage by remember { mutableStateOf<PromotionalProduct?>(null) }

    // Filter products
    val filteredProducts = remember(searchQuery, selectedCategory, allProducts) {
        allProducts.filter { product ->
            val matchesCategory = (selectedCategory == "All Deals") || product.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    product.title.contains(searchQuery, ignoreCase = true) ||
                    product.shopName.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val featuredDeals = remember(allProducts) {
        allProducts.filter { it.isFeaturedDeal }
    }

    val categories = listOf(
        "All Deals",
        "Smartphones & Tech",
        "Ethnic Wear",
        "Desi Jaggery & Sweets",
        "Home & Kitchen",
        "Audio & Wearables",
        "Footwear"
    )

    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ads_screen"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Left: App Logo + "MznLive • Ads"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.applogo3dtrns),
                                contentDescription = "MznLive Logo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("top_left_app_logo")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
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
                                        text = " • Ads",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TricolorGreen
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = TricolorGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TricolorGreen,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Promotional deals & AI price compare",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Top Right: Merchant Promote Button
                        OutlinedButton(
                            onClick = onNavigateToPlan,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TricolorSaffron)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Ad", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("ads_search_input"),
                        placeholder = { Text("Search products, brands, or local shops...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            focusedBorderColor = TricolorSaffron,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .emulatorScrollable(listState)
                .testTag("ads_products_list"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Category Filter Chips Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TricolorSaffron,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("filter_chip_${cat.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            // Featured Promotional Carousel / Scroll Effect Section
            if (searchQuery.isBlank() && selectedCategory == "All Deals") {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = TricolorSaffron,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Exclusive Featured Deals",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Swipe →",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Horizontal Scrolling Featured Cards
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                        ) {
                            items(featuredDeals) { featured ->
                                FeaturedDealBannerCard(
                                    product = featured,
                                    onBuyClick = { buyingProduct = featured },
                                    onCompareClick = {
                                        comparingProduct = featured
                                        isComparingLoading = true
                                        aiComparisonResult = null
                                        coroutineScope.launch {
                                            aiComparisonResult = geminiService.compareProductPrice(featured)
                                            isComparingLoading = false
                                        }
                                    },
                                    onImageClick = { viewedProductImage = featured },
                                    onShareClick = {
                                        ShareUtils.shareProduct(
                                            context = context,
                                            productName = featured.title,
                                            priceInr = featured.priceInr,
                                            mrpInr = featured.originalMrpInr,
                                            shopName = featured.shopName,
                                            shopLocation = featured.shopLocation,
                                            productUrl = featured.imageUrl
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Product List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "All Deals") "All Local Store Deals (${filteredProducts.size})" else "$selectedCategory (${filteredProducts.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "AI Price Match Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TricolorGreen
                    )
                }
            }

            // Empty State
            if (filteredProducts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No products found for \"$searchQuery\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Try searching a different item or switch categories",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Product Cards List
            items(filteredProducts, key = { it.id }) { product ->
                ProductDealCard(
                    product = product,
                    onBuyClick = { buyingProduct = product },
                    onCompareClick = {
                        comparingProduct = product
                        isComparingLoading = true
                        aiComparisonResult = null
                        coroutineScope.launch {
                            aiComparisonResult = geminiService.compareProductPrice(product)
                            isComparingLoading = false
                        }
                    },
                    onImageClick = { viewedProductImage = product },
                    onShareClick = {
                        ShareUtils.shareProduct(
                            context = context,
                            productName = product.title,
                            priceInr = product.priceInr,
                            mrpInr = product.originalMrpInr,
                            shopName = product.shopName,
                            shopLocation = product.shopLocation,
                            productUrl = product.imageUrl
                        )
                    }
                )
            }
        }
    }

    // AI Price Comparison Dialog / Bottom Sheet
    if (comparingProduct != null) {
        AiPriceCompareDialog(
            product = comparingProduct!!,
            isLoading = isComparingLoading,
            comparisonResult = aiComparisonResult,
            onDismiss = { comparingProduct = null },
            onBuyNow = {
                val prod = comparingProduct
                comparingProduct = null
                buyingProduct = prod
            }
        )
    }

    // Direct Buy Dialog via Razorpay Portal
    if (buyingProduct != null) {
        DirectBuyDialog(
            product = buyingProduct!!,
            onDismiss = { buyingProduct = null },
            onProceedToRazorpay = { prod, phone, address ->
                buyingProduct = null
                onBuyProductWithRazorpay(prod, phone, address)
            }
        )
    }

    // In-App Product Image Viewer (stays inside app portal)
    if (viewedProductImage != null) {
        val p = viewedProductImage!!
        InAppImageViewerDialog(
            imageUrl = p.imageUrl,
            title = p.title,
            subtitle = "₹${p.priceInr} (MRP ₹${p.originalMrpInr}) • ${p.shopName}, ${p.shopLocation}",
            onDismiss = { viewedProductImage = null },
            onShare = {
                ShareUtils.shareProduct(
                    context = context,
                    productName = p.title,
                    priceInr = p.priceInr,
                    mrpInr = p.originalMrpInr,
                    shopName = p.shopName,
                    shopLocation = p.shopLocation,
                    productUrl = p.imageUrl
                )
            }
        )
    }
}

/**
 * Featured Deal Banner Card with vibrant gradient & scroll effect
 */
@Composable
fun FeaturedDealBannerCard(
    product: PromotionalProduct,
    onBuyClick: () -> Unit,
    onCompareClick: () -> Unit,
    onImageClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(310.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onImageClick() }
            .testTag("featured_deal_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            )

            // Scrim gradient for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Badges & Share Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = TricolorSaffron,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "HOT DEAL • ${product.discountPercent}% OFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Share Button
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape,
                        modifier = Modifier
                            .clickable { onShareClick() }
                            .testTag("share_featured_${product.id}")
                    ) {
                        Box(
                            modifier = Modifier.padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${product.rating}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom Product Details & Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = product.shopName,
                    fontSize = 11.sp,
                    color = Color(0xFFFFE0B2),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = product.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${product.priceInr}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.originalMrpInr}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // AI Compare Button
                        FilledTonalButton(
                            onClick = onCompareClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("compare_btn_${product.id}")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compare", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Buy Button
                        Button(
                            onClick = onBuyClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron),
                            modifier = Modifier.testTag("buy_btn_${product.id}")
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Main Product Deal Card in vertical feed
 */
@Composable
fun ProductDealCard(
    product: PromotionalProduct,
    onBuyClick: () -> Unit,
    onCompareClick: () -> Unit,
    onImageClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column {
            // Product Image with badges & click-to-view in-app
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Top Discount Tag
                Surface(
                    color = MznCrimson,
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "SAVE ${product.discountPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Delivery badge
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TricolorGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.deliverySpeed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Product Details
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.shopName} • ${product.shopLocation}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${product.rating} (${product.reviewsCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.priceInr}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${product.originalMrpInr}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = TricolorGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Save ₹${product.originalMrpInr - product.priceInr}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Buttons: Buy Button, Price Compare Button, & Share Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compare Price Button (AI Powered)
                    OutlinedButton(
                        onClick = onCompareClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("compare_price_btn_${product.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.horizontalGradient(
                                listOf(TricolorSaffron, MaterialTheme.colorScheme.primary)
                            )
                        )
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TricolorSaffron,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Compare",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Buy Now Button
                    Button(
                        onClick = onBuyClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("buy_now_btn_${product.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen)
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Buy Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Share Product Button
                    OutlinedButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("share_btn_${product.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Product",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * AI Price Comparison Dialog comparing Mznlive local price against Amazon India & Flipkart
 */
@Composable
fun AiPriceCompareDialog(
    product: PromotionalProduct,
    isLoading: Boolean,
    comparisonResult: AiPriceComparisonResult?,
    onDismiss: () -> Unit,
    onBuyNow: () -> Unit
) {
    val context = LocalContext.current
    val queryEncoded = remember(product.title) {
        try {
            URLEncoder.encode(product.title, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            product.title
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_price_compare_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TricolorSaffron,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI Price Comparison",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Powered by Gemini AI",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Summary Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = product.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Store: ${product.shopName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mznlive Deal: ₹${product.priceInr}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TricolorGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Comparison Matrix Table
                Text(
                    text = "Live Store Comparison",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mznlive Local Deal Card
                ComparisonRowCard(
                    storeName = "Mznlive Local Store",
                    priceText = "₹${product.priceInr}",
                    deliveryText = "⚡ Same-Day Instant Pickup / Delivery",
                    badgeText = "BEST DEAL",
                    badgeColor = TricolorGreen,
                    onOpenLink = null
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Amazon India Card
                val amazonEst = comparisonResult?.amazonEstimatedPrice ?: product.estimatedAmazonPrice
                ComparisonRowCard(
                    storeName = "Amazon India",
                    priceText = "₹$amazonEst",
                    deliveryText = "📦 2-3 Days Courier Shipping",
                    badgeText = "Online Store",
                    badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onOpenLink = {
                        openUrl(context, "https://www.amazon.in/s?k=$queryEncoded")
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Flipkart Card
                val flipkartEst = comparisonResult?.flipkartEstimatedPrice ?: product.estimatedFlipkartPrice
                ComparisonRowCard(
                    storeName = "Flipkart",
                    priceText = "₹$flipkartEst",
                    deliveryText = "📦 2-4 Days Courier Shipping",
                    badgeText = "Online Store",
                    badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onOpenLink = {
                        openUrl(context, "https://www.flipkart.com/search?q=$queryEncoded")
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // AI Analysis Box
                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TricolorSaffron.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TricolorSaffron,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gemini AI is analyzing live online pricing & dealer savings...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (comparisonResult != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFFBEB), Color(0xFFF0FDF4))
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, TricolorGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TricolorGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Smart Buyer Verdict",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = comparisonResult.verdict,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = comparisonResult.analysisText,
                            fontSize = 11.sp,
                            color = MznLightTextSecondary,
                            lineHeight = 15.sp
                        )

                        if (comparisonResult.localSavingsAmount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = TricolorGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Estimated Local Savings: ₹${comparisonResult.localSavingsAmount}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBuyNow,
                colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("ai_dialog_buy_now")
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buy at Mznlive Price (₹${product.priceInr})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ComparisonRowCard(
    storeName: String,
    priceText: String,
    deliveryText: String,
    badgeText: String,
    badgeColor: Color,
    onOpenLink: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = storeName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = deliveryText,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = priceText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (storeName.contains("Mznlive")) TricolorGreen else MaterialTheme.colorScheme.onSurface
                )
                if (onOpenLink != null) {
                    TextButton(
                        onClick = onOpenLink,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Check ↗", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Direct Buy Dialog to order product via Razorpay Service Portal
 */
@Composable
fun DirectBuyDialog(
    product: PromotionalProduct,
    onDismiss: () -> Unit,
    onProceedToRazorpay: (PromotionalProduct, String, String) -> Unit
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("Muzaffarnagar City, Uttar Pradesh") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("direct_buy_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3395FF), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Razorpay Checkout", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TricolorNavy)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Item preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = product.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(text = "Store: ${product.shopName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Deal Price: ₹${product.priceInr}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TricolorGreen)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Your Name") },
                    placeholder = { Text("e.g. Rahul Sharma") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it.filter { ch -> ch.isDigit() }.take(10) },
                    label = { Text("Mobile Number (for receipt)") },
                    placeholder = { Text("e.g. 9876543210") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerAddress,
                    onValueChange = { customerAddress = it },
                    label = { Text("Delivery Address / Landmark") },
                    placeholder = { Text("Gandhi Colony, Muzaffarnagar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Razorpay Security Trust Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF3395FF),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secured by Razorpay • UPI, Cards, NetBanking & Wallets",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerPhone.length < 10) {
                        Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                    } else {
                        onProceedToRazorpay(product, customerPhone, customerAddress)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_razorpay_direct_order_btn")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pay ₹${product.priceInr} via Razorpay", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Curated catalog of Muzaffarnagar promotional products & deals
 */
private fun getPromotionalProductsCatalog(): List<PromotionalProduct> {
    return listOf(
        PromotionalProduct(
            id = 101,
            title = "Banarasi Pure Silk Embroidered Bridal Saree",
            titleHi = "बनारसी सिल्क दुल्हन साड़ी",
            category = "Ethnic Wear",
            shopName = "Royal Heritage Sarees",
            shopLocation = "Court Road, Muzaffarnagar",
            priceInr = 1899,
            originalMrpInr = 3499,
            imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 280,
            description = "Intricate golden zari work with traditional floral border. Comes with matching unstitched blouse piece. Special festive discount.",
            whatsappContact = "919837012345",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Instant Pickup / 2-Hr Delivery in Mzn"
        ),
        PromotionalProduct(
            id = 102,
            title = "OnePlus Nord CE4 Lite 5G (8GB RAM, 128GB)",
            titleHi = "वनप्लस नॉर्ड 5G स्मार्टफोन",
            category = "Smartphones & Tech",
            shopName = "Gupta Electronics & Mobiles",
            shopLocation = "Bhagat Singh Road, Muzaffarnagar",
            priceInr = 17499,
            originalMrpInr = 20999,
            imageUrl = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600&auto=format&fit=crop&q=80",
            rating = 4.8f,
            reviewsCount = 340,
            description = "5500 mAh battery with 80W SUPERVOOC charging, 120Hz AMOLED display, Sony LYT-600 50MP OIS camera. Official warranty.",
            whatsappContact = "919897123456",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Same-Day Store Pickup & Demo"
        ),
        PromotionalProduct(
            id = 103,
            title = "Muzaffarnagar Organic Pure Desi Shakkar & Jaggery (5 Kg)",
            titleHi = "मुजफ्फरनगर की प्रसिद्ध शुद्ध देशी शक्कर एवं गुड़",
            category = "Desi Jaggery & Sweets",
            shopName = "Kisan Krishi Kendra & Gur Mandi",
            shopLocation = "Gandhi Colony, Muzaffarnagar",
            priceInr = 349,
            originalMrpInr = 550,
            imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 512,
            description = "100% natural, chemical-free sugarcane gur directly from the sugar capital of India. Rich in iron and minerals.",
            whatsappContact = "919837567890",
            isFeaturedDeal = true,
            deliverySpeed = "⚡ Fresh Daily Stock • Same-Day Delivery"
        ),
        PromotionalProduct(
            id = 104,
            title = "Noise Pulse 2 Max 1.85\" Bluetooth Calling Smartwatch",
            titleHi = "नॉइज़ स्मार्टवॉच ब्लूटूथ कॉलिंग",
            category = "Audio & Wearables",
            shopName = "TechZone Mobile Hub",
            shopLocation = "Roorkee Road, Muzaffarnagar",
            priceInr = 1199,
            originalMrpInr = 2999,
            imageUrl = "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&auto=format&fit=crop&q=80",
            rating = 4.7f,
            reviewsCount = 190,
            description = "Massive 1.85\" bright TFT LCD, 550 nits brightness, 10-day battery life, 100 sports modes with heart rate & SpO2 tracking.",
            whatsappContact = "919897998877",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Same-Day Delivery in Mzn"
        ),
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
        ),
        PromotionalProduct(
            id = 106,
            title = "boAt Rockerz 450 Pro On-Ear Wireless Headphones",
            titleHi = "बोट वायरलेस हेडफ़ोन 70 घंटे बैटरी",
            category = "Audio & Wearables",
            shopName = "Digital Wave Electronics",
            shopLocation = "Shiv Chowk, Muzaffarnagar",
            priceInr = 1499,
            originalMrpInr = 2990,
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
            rating = 4.7f,
            reviewsCount = 310,
            description = "Up to 70 hours non-stop playback, 40mm dynamic drivers with boAt Signature Sound, ASAP fast charging via Type-C.",
            whatsappContact = "919897334455",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Same-Day Delivery in Mzn"
        ),
        PromotionalProduct(
            id = 107,
            title = "Kalyan Special Pure Desi Ghee Doda Barfi (1 Kg Box)",
            titleHi = "कल्याण स्पेशल शुद्ध देशी घी डोडा बर्फी",
            category = "Desi Jaggery & Sweets",
            shopName = "Kalyan Sweets & Confectioners",
            shopLocation = "Naveen Mandi, Muzaffarnagar",
            priceInr = 580,
            originalMrpInr = 750,
            imageUrl = "https://images.unsplash.com/photo-1599785209707-a456fc1337bb?w=600&auto=format&fit=crop&q=80",
            rating = 4.9f,
            reviewsCount = 620,
            description = "Authentic heritage recipe made with rich sprouted wheat, khoya, dry fruits and pure desi cow ghee. Melt-in-mouth freshness guaranteed.",
            whatsappContact = "919837445566",
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Freshly Packed Daily • 1-Hour Local Delivery"
        ),
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
            isFeaturedDeal = false,
            deliverySpeed = "⚡ Store Trial & Same-Day Delivery"
        )
    )
}
