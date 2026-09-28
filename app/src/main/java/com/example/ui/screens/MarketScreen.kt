package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.util.emulatorScrollable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.ShareUtils

@Composable
fun MarketScreen(
    shops: List<MarketplaceShop>,
    onOpenChatWithShop: (MarketplaceShop) -> Unit,
    onNavigateToSeller: (MarketplaceShop) -> Unit = {},
    onAddToCart: (PromotionalProduct) -> Unit = {},
    onOpenCart: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSubCategory by remember { mutableStateOf<String?>(null) }

    // In-app media viewing states (never leaves app portal)
    var viewedShopImage by remember { mutableStateOf<MarketplaceShop?>(null) }
    var viewedShopSocialUrl by remember { mutableStateOf<Pair<String, String>?>(null) }

    val activeCategoryItem = defaultMarketplaceCategories.firstOrNull { it.id.equals(selectedCategory, ignoreCase = true) }

    val filteredShops = shops.filter { shop ->
        val matchesCategory = shopMatchesCategory(shop, selectedCategory, selectedSubCategory)
        val matchesSearch = shop.name.contains(searchQuery, ignoreCase = true) ||
                shop.category.contains(searchQuery, ignoreCase = true) ||
                shop.featuredOffer.contains(searchQuery, ignoreCase = true) ||
                shop.address.contains(searchQuery, ignoreCase = true)
        matchesCategory && (searchQuery.isBlank() || matchesSearch)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("market_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Branded Header with 3D Emblem Logo at Top Left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.applogo3dtrns),
                contentDescription = "MznLive Logo",
                modifier = Modifier
                    .size(42.dp)
                    .testTag("top_left_app_logo")
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
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
                        text = " • Market",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorGreen
                    )
                }
                Text(
                    text = "Browse & chat with local verified merchants",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Indian Tricolor Accent Line
        TricolorAccentBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp)),
            height = 2.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search local shops, products & offers...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("market_search_bar"),
            shape = RoundedCornerShape(28.dp),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Category Filter Bar (Retail, Services, Restaurants, etc.)
        MarketplaceHorizontalFilterBar(
            categories = defaultMarketplaceCategories,
            selectedCategory = selectedCategory,
            onSelectCategory = { newCat ->
                selectedCategory = newCat
                selectedSubCategory = null
            },
            shops = shops,
            modifier = Modifier.fillMaxWidth()
        )

        // Sub-category Filter Bar (if available for selected category)
        if (activeCategoryItem?.subCategories?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(8.dp))
            MarketplaceSubCategoryBar(
                subCategories = activeCategoryItem.subCategories,
                selectedSubCategory = selectedSubCategory,
                onSelectSubCategory = { selectedSubCategory = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Category / Results Status Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = activeCategoryItem?.icon ?: Icons.Default.Category,
                    contentDescription = null,
                    tint = TricolorSaffronDark,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${filteredShops.size} verified ${if (selectedCategory == "All") "businesses" else selectedCategory} available",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (selectedCategory != "All" || selectedSubCategory != null || searchQuery.isNotBlank()) {
                Surface(
                    onClick = {
                        selectedCategory = "All"
                        selectedSubCategory = null
                        searchQuery = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = TricolorSaffronLight,
                    border = BorderStroke(1.dp, TricolorSaffronBorder),
                    modifier = Modifier.testTag("clear_market_filter_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Reset filter",
                            tint = TricolorSaffronDark,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Reset",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorSaffronDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val listState = rememberLazyListState()

        // Shop Cards List
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .emulatorScrollable(listState)
        ) {
            if (filteredShops.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching shops found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing your search query or selecting 'All' category",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedCategory = "All"
                                selectedSubCategory = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffron),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                items(filteredShops) { shop ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column {
                        // Shop Banner Image & Offer Tag
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clickable { viewedShopImage = shop }
                        ) {
                            AsyncImage(
                                model = shop.imageUrl,
                                contentDescription = shop.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Rating Badge
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MznAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${shop.rating}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Featured Offer Pill
                            if (shop.featuredOffer.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MznCrimson)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = shop.featuredOffer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Shop Details
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = shop.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = shop.address,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons: Chat, WhatsApp, Share, Social
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Chat button
                                Button(
                                    onClick = { onOpenChatWithShop(shop) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MznCrimson),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // WhatsApp button
                                Button(
                                    onClick = {
                                        try {
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${shop.whatsapp}&text=Hello! I saw your shop on Mznlive marketplace.")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Share Shop Details Button
                                IconButton(
                                    onClick = {
                                        ShareUtils.shareShop(
                                            context = context,
                                            shopName = shop.name,
                                            category = shop.category,
                                            address = shop.address,
                                            phone = shop.phone,
                                            whatsapp = shop.whatsapp
                                        )
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .testTag("share_shop_${shop.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Shop",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Social / Media in-app viewer (runs inside app)
                                IconButton(
                                    onClick = {
                                        val url = shop.instagramLink.ifEmpty { shop.facebookLink }
                                        if (url.isNotBlank()) {
                                            viewedShopSocialUrl = Pair(url, shop.name)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = "Social Page",
                                        tint = FacebookBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onNavigateToSeller(shop) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("visit_shop_btn_${shop.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = TricolorSaffron,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View Products & Store",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }

    // In-App Shop Image Viewer (never leaves the app portal)
    if (viewedShopImage != null) {
        val s = viewedShopImage!!
        InAppImageViewerDialog(
            imageUrl = s.imageUrl,
            title = s.name,
            subtitle = "${s.category} • ${s.address}",
            onDismiss = { viewedShopImage = null },
            onShare = {
                ShareUtils.shareShop(
                    context = context,
                    shopName = s.name,
                    category = s.category,
                    address = s.address,
                    phone = s.phone,
                    whatsapp = s.whatsapp
                )
            }
        )
    }

    // In-App Social Page / Video / Media Viewer
    if (viewedShopSocialUrl != null) {
        val sShop = shops.find { it.name.equals(viewedShopSocialUrl!!.second, ignoreCase = true) }
        InAppVideoPlayerDialog(
            mediaUrl = viewedShopSocialUrl!!.first,
            title = "${viewedShopSocialUrl!!.second} • Media & Page",
            sourceLabel = viewedShopSocialUrl!!.second,
            sellerShop = sShop,
            availableShops = shops,
            onDismiss = { viewedShopSocialUrl = null },
            onBack = { viewedShopSocialUrl = null },
            onShare = {
                ShareUtils.shareText(
                    context = context,
                    title = viewedShopSocialUrl!!.second,
                    message = "Check out ${viewedShopSocialUrl!!.second} on MznLive!",
                    linkUrl = viewedShopSocialUrl!!.first
                )
            },
            onBuyClick = { product ->
                onAddToCart(product)
                onOpenCart()
            },
            onGoToSeller = { shop ->
                viewedShopSocialUrl = null
                onNavigateToSeller(shop)
            }
        )
    }
}
