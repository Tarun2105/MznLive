package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MarketplaceShop
import com.example.ui.theme.*
import com.example.util.ShareUtils

/**
 * Metadata definition for Marketplace business categories.
 */
data class MarketplaceCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val subCategories: List<String> = emptyList()
)

val defaultMarketplaceCategories = listOf(
    MarketplaceCategory(
        id = "All",
        title = "All",
        subtitle = "All verified",
        icon = Icons.Default.Storefront
    ),
    MarketplaceCategory(
        id = "Retail",
        title = "Retail",
        subtitle = "Stores & Shopping",
        icon = Icons.Default.ShoppingBag,
        subCategories = listOf("All Retail", "Fashion & Sarees", "Electronics & Mobiles", "Home Decor", "Jewelry")
    ),
    MarketplaceCategory(
        id = "Services",
        title = "Services",
        subtitle = "Repairs, Health & Care",
        icon = Icons.Default.HomeRepairService,
        subCategories = listOf("All Services", "Device & Tech Repairs", "Healthcare & Path Labs", "Salon, Spa & Wellness")
    ),
    MarketplaceCategory(
        id = "Restaurants",
        title = "Restaurants",
        subtitle = "Dining, Cafes & Food",
        icon = Icons.Default.Restaurant,
        subCategories = listOf("All Dining", "North Indian Dining", "Sweets & Bakery", "Dhaba & Tandoor")
    ),
    MarketplaceCategory(
        id = "Fashion",
        title = "Fashion",
        subtitle = "Sarees & Apparel",
        icon = Icons.Default.Checkroom
    ),
    MarketplaceCategory(
        id = "Electronics",
        title = "Electronics",
        subtitle = "Mobiles & Tech",
        icon = Icons.Default.Devices
    ),
    MarketplaceCategory(
        id = "Food & Sweets",
        title = "Food & Sweets",
        subtitle = "Mithai & Bakery",
        icon = Icons.Default.BakeryDining
    ),
    MarketplaceCategory(
        id = "Handicrafts",
        title = "Handicrafts",
        subtitle = "Art & Home Decor",
        icon = Icons.Default.Brush
    )
)

/**
 * Filter matching logic for Marketplace businesses.
 */
fun shopMatchesCategory(shop: MarketplaceShop, category: String, subCategory: String? = null): Boolean {
    val shopCat = shop.category.lowercase()
    val shopName = shop.name.lowercase()
    val shopOffer = shop.featuredOffer.lowercase()

    val matchesMain = when (category.lowercase()) {
        "all" -> true
        "retail" -> {
            shopCat.contains("retail") ||
                    shopCat.contains("fashion") ||
                    shopCat.contains("clothing") ||
                    shopCat.contains("saree") ||
                    shopCat.contains("electronic") ||
                    shopCat.contains("mobile") ||
                    shopCat.contains("decor") ||
                    shopCat.contains("handicraft") ||
                    shopCat.contains("jewel") ||
                    shopCat.contains("store") ||
                    shopCat.contains("shop")
        }
        "services" -> {
            shopCat.contains("service") ||
                    shopCat.contains("repair") ||
                    shopCat.contains("health") ||
                    shopCat.contains("diagnostic") ||
                    shopCat.contains("clinic") ||
                    shopCat.contains("lab") ||
                    shopCat.contains("salon") ||
                    shopCat.contains("spa") ||
                    shopCat.contains("grooming") ||
                    shopCat.contains("consult") ||
                    shopCat.contains("legal")
        }
        "restaurants" -> {
            shopCat.contains("restaurant") ||
                    shopCat.contains("cafe") ||
                    shopCat.contains("food") ||
                    shopCat.contains("sweet") ||
                    shopCat.contains("bakery") ||
                    shopCat.contains("dhaba") ||
                    shopCat.contains("dining") ||
                    shopCat.contains("kitchen") ||
                    shopCat.contains("bistro")
        }
        "fashion" -> shopCat.contains("fashion") || shopCat.contains("clothing") || shopCat.contains("saree")
        "electronics" -> shopCat.contains("electronic") || shopCat.contains("mobile") || shopCat.contains("gadget")
        "food & sweets" -> shopCat.contains("sweet") || shopCat.contains("bakery") || shopCat.contains("mithai") || shopCat.contains("food")
        "handicrafts" -> shopCat.contains("handicraft") || shopCat.contains("decor") || shopCat.contains("art")
        else -> shopCat.contains(category.lowercase()) || shopName.contains(category.lowercase())
    }

    if (!matchesMain) return false

    if (subCategory != null && !subCategory.startsWith("All", ignoreCase = true)) {
        val subLower = subCategory.lowercase()
        return shopCat.contains(subLower) || shopName.contains(subLower) || shopOffer.contains(subLower)
    }

    return true
}

/**
 * Polished Horizontal Filter Bar for browsing Marketplace businesses by category.
 */
@Composable
fun MarketplaceHorizontalFilterBar(
    categories: List<MarketplaceCategory> = defaultMarketplaceCategories,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    shops: List<MarketplaceShop>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("marketplace_horizontal_filter_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
    ) {
        items(categories) { cat ->
            val isSelected = selectedCategory.equals(cat.id, ignoreCase = true)
            val count = shops.count { shopMatchesCategory(it, cat.id) }

            Surface(
                onClick = { onSelectCategory(cat.id) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) TricolorNavy else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) TricolorSaffron else Color(0xFFE2E8F0)
                ),
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .testTag("filter_chip_${cat.id.lowercase().replace(" ", "_").replace("&", "and")}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) TricolorSaffron else Color(0xFFF1F5F9)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.title,
                            tint = if (isSelected) Color.White else TricolorNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cat.title,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) TricolorSaffron.copy(alpha = 0.35f) else Color(0xFFE2E8F0)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TricolorSaffronLight else Color(0xFF475569)
                                )
                            }
                        }

                        Text(
                            text = cat.subtitle,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sub-category refinement chip bar displayed when a specific category (Retail, Services, Restaurants) is active.
 */
@Composable
fun MarketplaceSubCategoryBar(
    subCategories: List<String>,
    selectedSubCategory: String?,
    onSelectSubCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (subCategories.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("marketplace_subcategory_filter_bar")
    ) {
        items(subCategories) { subCat ->
            val isSelected = (selectedSubCategory == null && subCat.startsWith("All", ignoreCase = true)) ||
                    selectedSubCategory == subCat

            FilterChip(
                selected = isSelected,
                onClick = { onSelectSubCategory(subCat) },
                label = {
                    Text(
                        text = subCat,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TricolorSaffron,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) TricolorSaffronDark else Color(0xFFCBD5E1)
                ),
                modifier = Modifier.testTag("sub_filter_${subCat.lowercase().replace(" ", "_")}")
            )
        }
    }
}

/**
 * Compact Shop Card for horizontal carousels (e.g. on HomeDashboardView).
 */
@Composable
fun MarketplaceCompactCard(
    shop: MarketplaceShop,
    onShopClick: (MarketplaceShop) -> Unit,
    onChatClick: (MarketplaceShop) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(220.dp)
            .clickable { onShopClick(shop) }
            .testTag("compact_shop_card_${shop.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
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
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MznAmber,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${shop.rating}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Verified Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TricolorGreen)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VERIFIED",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = shop.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = shop.category,
                    fontSize = 10.5.sp,
                    color = TricolorNavy,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = shop.address,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (shop.featuredOffer.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(TricolorSaffronLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = shop.featuredOffer,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorSaffronDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { onChatClick(shop) },
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Chat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            try {
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=${shop.whatsapp}&text=Hello! I saw your shop on Mznlive marketplace.")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("WA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
