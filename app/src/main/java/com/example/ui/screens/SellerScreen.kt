package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.util.emulatorScrollable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessage
import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct
import com.example.data.repository.CartItem
import com.example.data.repository.ProductCatalogRepository
import com.example.ui.theme.*
import com.example.util.ShareUtils

@Composable
fun SellerScreen(
    shop: MarketplaceShop,
    cartItems: List<CartItem> = emptyList(),
    chatMessages: List<ChatMessage> = emptyList(),
    dbProducts: List<PromotionalProduct> = emptyList(),
    onBack: () -> Unit,
    onAddToCart: (PromotionalProduct) -> Unit,
    onOpenCart: () -> Unit,
    onSendMessage: (String) -> Unit = {},
    onSendSms: (phone: String, msg: String) -> Unit = { _, _ -> },
    onSendWhatsApp: (wa: String, msg: String) -> Unit = { _, _ -> },
    onOpenDedicatedShop: ((MarketplaceShop) -> Unit)? = null
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    val products = remember(shop.name, dbProducts) {
        val dbShopProducts = dbProducts.filter { it.shopName.equals(shop.name, ignoreCase = true) }
        val repoProducts = ProductCatalogRepository.getProductsForShop(shop.name)
        (dbShopProducts + repoProducts).distinctBy { it.title }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var showChatDialog by remember { mutableStateOf(false) }
    var selectedProductForChat by remember { mutableStateOf<PromotionalProduct?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val cartItemCount = cartItems.sumOf { it.quantity }
    val cartTotal = cartItems.sumOf { it.product.priceInr * it.quantity }

    val filteredProducts = remember(products, selectedFilter) {
        when (selectedFilter) {
            "Deals" -> products.filter { it.isFeaturedDeal }
            "Under ₹1000" -> products.filter { it.priceInr < 1000 }
            else -> products
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("seller_screen_scaffold"),
        topBar = {
            Surface(
                color = TricolorNavy,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("seller_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = shop.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Merchant",
                                        tint = TricolorGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = "Verified Muzaffarnagar Merchant",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Manage Shop Button (if owner)
                            if (onOpenDedicatedShop != null) {
                                IconButton(
                                    onClick = { onOpenDedicatedShop(shop) },
                                    modifier = Modifier.testTag("seller_manage_shop_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = "Manage Shop Products",
                                        tint = TricolorSaffron
                                    )
                                }
                            }

                            // Share Button
                            IconButton(
                                onClick = {
                                    ShareUtils.shareText(
                                        context = context,
                                        title = shop.name,
                                        message = "Explore authentic products from ${shop.name} located at ${shop.address} on MznLive!",
                                        linkUrl = shop.instagramLink.ifBlank { "https://mznlive.in" }
                                    )
                                },
                                modifier = Modifier.testTag("seller_share_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Shop",
                                    tint = Color.White
                                )
                            }

                            // Shopping Cart Button with Dynamic Badge
                            Box(modifier = Modifier.testTag("seller_cart_btn")) {
                                IconButton(onClick = onOpenCart) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Shopping Cart",
                                        tint = if (cartItemCount > 0) TricolorSaffron else Color.White
                                    )
                                }
                                if (cartItemCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE53935)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$cartItemCount",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    TricolorAccentBar(modifier = Modifier.fillMaxWidth(), height = 2.5.dp)
                }
            }
        },
        bottomBar = {
            // Floating Sticky Cart Checkout Bar when user has items in cart
            AnimatedVisibility(
                visible = cartItemCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 10.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TricolorSaffron),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$cartItemCount Item(s) in Cart",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total: ₹$cartTotal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TricolorGreen
                                )
                            }
                        }

                        Button(
                            onClick = onOpenCart,
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("seller_bottom_view_cart_btn")
                        ) {
                            Text(
                                text = "View Cart & Buy",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
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
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .emulatorScrollable(listState)
                    .testTag("seller_screen_scrollable_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Hero Seller Showcase & Banner
                item {
                    SellerHeroHeader(shop = shop)
                }

                // 2. The 3 Essential Contact Actions: 1. Contact / Call, 2. Chat, 3. WhatsApp
                item {
                    SellerContactActionsBar(
                        shop = shop,
                        onOpenChat = { showChatDialog = true }
                    )
                }

                // 3. Special Offer & Merchant Guarantee Highlight
                item {
                    SellerOfferCard(shop = shop)
                }

                // 4. Products Section Title & Filter Chips
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Store Products & Offers",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Buy direct from store with genuine price guarantee",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = TricolorNavy.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${filteredProducts.size} Items",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TricolorNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Deals", "Under ₹1000").forEach { filter ->
                                val isSelected = selectedFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TricolorSaffron,
                                        selectedLabelColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Products List
                items(filteredProducts, key = { it.id }) { product ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        SellerProductCard(
                            product = product,
                            onAddToCart = { prod ->
                                onAddToCart(prod)
                                snackbarMessage = "Added ${prod.title.take(24)}... to cart!"
                            },
                            onBuyNow = { prod ->
                                onAddToCart(prod)
                                onOpenCart()
                            },
                            onInquire = { prod ->
                                selectedProductForChat = prod
                                showChatDialog = true
                            }
                        )
                    }
                }
            }

            // Snackbar feedback overlay
            AnimatedVisibility(
                visible = snackbarMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, TricolorGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TricolorGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = snackbarMessage ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = onOpenCart,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "VIEW CART",
                                color = TricolorSaffron,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                LaunchedEffect(snackbarMessage) {
                    kotlinx.coroutines.delay(2600)
                    snackbarMessage = null
                }
            }

            // In-App Interactive Chat with this Seller
            if (showChatDialog) {
                ChatDialog(
                    shop = shop,
                    selectedProduct = selectedProductForChat,
                    messages = chatMessages,
                    onSendMessage = onSendMessage,
                    onSendSms = onSendSms,
                    onSendWhatsApp = onSendWhatsApp,
                    onDismiss = {
                        showChatDialog = false
                        selectedProductForChat = null
                    }
                )
            }
        }
    }
}

@Composable
fun SellerHeroHeader(shop: MarketplaceShop) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Shop Background Banner Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(shop.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = shop.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Seller details inside banner
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Pill
                Surface(
                    color = TricolorSaffron,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = shop.category,
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // Rating Pill
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${shop.rating} ★",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Shop Name
            Text(
                text = shop.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Address Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    tint = TricolorSaffron,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = shop.address,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SellerContactActionsBar(
    shop: MarketplaceShop,
    onOpenChat: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Direct Seller Contacts",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. CONTACT / PHONE CALL
                OutlinedCard(
                    onClick = {
                        val cleanPhone = shop.phone.filter { it.isDigit() || it == '+' }
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                        try {
                            context.startActivity(dialIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Call: ${shop.phone}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp)
                        .testTag("seller_contact_call_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, TricolorNavy.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Seller",
                            tint = TricolorNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Call Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorNavy
                        )
                    }
                }

                // 2. LIVE IN-APP CHAT
                Card(
                    onClick = onOpenChat,
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp)
                        .testTag("seller_chat_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TricolorNavy)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "In-App Chat",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Live Chat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 3. WHATSAPP DIRECT
                Card(
                    onClick = {
                        val cleanWa = shop.whatsapp.filter { it.isDigit() }
                        val prefill = "Hello ${shop.name}, I found your shop on MznLive and I would like to inquire about your products."
                        val waUrl = "https://api.whatsapp.com/send?phone=$cleanWa&text=${Uri.encode(prefill)}"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp: ${shop.whatsapp}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp)
                        .testTag("seller_whatsapp_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TricolorGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "WhatsApp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerOfferCard(shop: MarketplaceShop) {
    if (shop.featuredOffer.isNotBlank()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = TricolorSaffron.copy(alpha = 0.12f)
            ),
            border = BorderStroke(1.dp, TricolorSaffron.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = "Featured Offer",
                    tint = TricolorSaffronDark,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Merchant Special Offer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorSaffronDark
                    )
                    Text(
                        text = shop.featuredOffer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SellerProductCard(
    product: PromotionalProduct,
    onAddToCart: (PromotionalProduct) -> Unit,
    onBuyNow: (PromotionalProduct) -> Unit,
    onInquire: (PromotionalProduct) -> Unit = {}
) {
    val context = LocalContext.current
    val discountPercent = remember(product.priceInr, product.originalMrpInr) {
        if (product.originalMrpInr > product.priceInr) {
            ((product.originalMrpInr - product.priceInr) * 100) / product.originalMrpInr
        } else 0
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seller_product_card_${product.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Product Image with Discount Badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (discountPercent > 0) {
                        Surface(
                            color = Color(0xFFE53935),
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "$discountPercent% OFF",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Product Details
                Column(modifier = Modifier.weight(1f)) {
                    // English & Hindi Title
                    Text(
                        text = product.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (product.titleHi.isNotBlank()) {
                        Text(
                            text = product.titleHi,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rating & Reviews
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${product.rating} (${product.reviewsCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Price & Savings
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "₹${product.priceInr}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TricolorGreen
                        )
                        if (product.originalMrpInr > product.priceInr) {
                            Text(
                                text = "₹${product.originalMrpInr}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    // Delivery Speed Notice
                    Text(
                        text = product.deliverySpeed,
                        fontSize = 10.sp,
                        color = TricolorNavy,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Description preview
            if (product.description.isNotBlank()) {
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // The Action Buttons: "Add to Cart" and "Buy Now"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Direct Chat / Inquire Button
                IconButton(
                    onClick = { onInquire(product) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDCFCE7))
                        .testTag("product_chat_inquire_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat with Shop",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Add to Cart Button
                OutlinedButton(
                    onClick = { onAddToCart(product) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("product_add_to_cart_${product.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TricolorNavy
                    ),
                    border = BorderStroke(1.dp, TricolorNavy.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to Cart",
                        modifier = Modifier.size(16.dp),
                        tint = TricolorNavy
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add to Cart",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorNavy
                    )
                }

                // Buy Now Button
                Button(
                    onClick = { onBuyNow(product) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("product_buy_now_${product.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TricolorSaffron,
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Buy Now",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Buy Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
