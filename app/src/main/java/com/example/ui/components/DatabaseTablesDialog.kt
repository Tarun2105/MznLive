package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.data.local.entity.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class DatabaseTableTab(val label: String, val tableName: String) {
    USERS("Users", "users"),
    SHOPS("Shops", "shops"),
    PRODUCTS("Products", "products"),
    MESSAGES("Messages", "chat_messages"),
    TRANSACTIONS("Receipts", "payment_transactions")
}

/**
 * Responding Data Tables Viewer / Inspector dialog.
 * Live renders the Room/SQLite data tables for Users, Shops, Products, Chat Messages, and Payment Receipts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTablesDialog(
    users: List<UserEntity>,
    shops: List<ShopEntity>,
    products: List<ProductEntity>,
    messages: List<ChatMessageEntity>,
    transactions: List<PaymentTransactionEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(DatabaseTableTab.USERS) }
    var searchQuery by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("database_tables_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TricolorNavy)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Database",
                        tint = TricolorSaffron,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SQLite Schema & Data Tables",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TricolorGreen,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "LIVE ROOM DB",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Users (${users.size}) • Shops (${shops.size}) • Products (${products.size}) • Receipts (${transactions.size})",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                TricolorAccentBar(modifier = Modifier.fillMaxWidth(), height = 2.5.dp)

                // Table Selector Tabs (Scrollable for responsive screen sizes)
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 6.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = TricolorNavy
                ) {
                    DatabaseTableTab.values().forEach { tab ->
                        val count = when (tab) {
                            DatabaseTableTab.USERS -> users.size
                            DatabaseTableTab.SHOPS -> shops.size
                            DatabaseTableTab.PRODUCTS -> products.size
                            DatabaseTableTab.MESSAGES -> messages.size
                            DatabaseTableTab.TRANSACTIONS -> transactions.size
                        }
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = "${tab.label} ($count)",
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                // Table Meta & Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search in ${selectedTab.tableName}...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("table_search_input"),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Table Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    when (selectedTab) {
                        DatabaseTableTab.USERS -> {
                            val filtered = users.filter {
                                searchQuery.isBlank() ||
                                        it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.mobile.contains(searchQuery, ignoreCase = true) ||
                                        it.role.contains(searchQuery, ignoreCase = true) ||
                                        it.businessName.contains(searchQuery, ignoreCase = true)
                            }
                            UsersTableGrid(users = filtered, dateFormat = dateFormat)
                        }
                        DatabaseTableTab.SHOPS -> {
                            val filtered = shops.filter {
                                searchQuery.isBlank() ||
                                        it.shopName.contains(searchQuery, ignoreCase = true) ||
                                        it.category.contains(searchQuery, ignoreCase = true) ||
                                        it.phone.contains(searchQuery, ignoreCase = true) ||
                                        it.address.contains(searchQuery, ignoreCase = true)
                            }
                            ShopsTableGrid(shops = filtered, dateFormat = dateFormat)
                        }
                        DatabaseTableTab.PRODUCTS -> {
                            val filtered = products.filter {
                                searchQuery.isBlank() ||
                                        it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.shopName.contains(searchQuery, ignoreCase = true) ||
                                        it.category.contains(searchQuery, ignoreCase = true)
                            }
                            ProductsTableGrid(products = filtered, dateFormat = dateFormat)
                        }
                        DatabaseTableTab.MESSAGES -> {
                            val filtered = messages.filter {
                                searchQuery.isBlank() ||
                                        it.message.contains(searchQuery, ignoreCase = true) ||
                                        it.shopName.contains(searchQuery, ignoreCase = true) ||
                                        it.channel.contains(searchQuery, ignoreCase = true)
                            }
                            MessagesTableGrid(messages = filtered, dateFormat = dateFormat)
                        }
                        DatabaseTableTab.TRANSACTIONS -> {
                            val filtered = transactions.filter {
                                searchQuery.isBlank() ||
                                        it.transactionNumber.contains(searchQuery, ignoreCase = true) ||
                                        it.receiptNumber.contains(searchQuery, ignoreCase = true) ||
                                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                                        it.shopName.contains(searchQuery, ignoreCase = true) ||
                                        it.planTitle.contains(searchQuery, ignoreCase = true) ||
                                        (it.userUtrRef ?: "").contains(searchQuery, ignoreCase = true)
                            }
                            TransactionsTableGrid(transactions = filtered)
                        }
                    }
                }

                // Footer
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Table: ${selectedTab.tableName} • Engine: SQLite 3 / Android Room",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onDismiss) {
                            Text("Close", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersTableGrid(
    users: List<UserEntity>,
    dateFormat: SimpleDateFormat
) {
    if (users.isEmpty()) {
        EmptyTableState("No users in this table matching filter.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (user.role == "SHOP_OWNER") TricolorNavy else TricolorSaffronDark,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${user.id}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (user.role == "SHOP_OWNER") Color(0xFFE0F2FE) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = user.role,
                                color = if (user.role == "SHOP_OWNER") Color(0xFF0369A1) else Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (user.role == "SHOP_OWNER" && user.businessName.isNotBlank()) {
                        Text(
                            text = "Business: ${user.businessName} • ${user.category}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TricolorNavy
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Mobile: +91 ${user.mobile}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (user.whatsapp.isNotBlank()) {
                            Text(text = "WhatsApp: +91 ${user.whatsapp}", fontSize = 11.5.sp, color = Color(0xFF25D366))
                        }
                    }

                    if (user.address.isNotBlank()) {
                        Text(text = "Address: ${user.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(
                        text = "Registered: ${dateFormat.format(Date(user.createdAt))}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopsTableGrid(
    shops: List<ShopEntity>,
    dateFormat: SimpleDateFormat
) {
    if (shops.isEmpty()) {
        EmptyTableState("No shops recorded in this table.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shops) { shop ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = TricolorGreen,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "${shop.id}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = shop.shopName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (shop.isVerified) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("VERIFIED", color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Category: ${shop.category}", fontSize = 12.sp, color = TricolorNavy, fontWeight = FontWeight.Medium)
                    Text(text = "Address: ${shop.address}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Contact: +91 ${shop.phone}", fontSize = 11.5.sp)
                        Text(text = "Rating: ★ ${shop.rating}", fontSize = 11.5.sp, color = TricolorSaffronDark, fontWeight = FontWeight.Bold)
                    }

                    if (shop.featuredOffer.isNotBlank()) {
                        Text(text = "Offer: ${shop.featuredOffer}", fontSize = 11.sp, color = TricolorGreen, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductsTableGrid(
    products: List<ProductEntity>,
    dateFormat: SimpleDateFormat
) {
    if (products.isEmpty()) {
        EmptyTableState("No products listed in database.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { prod ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TricolorSaffronDark)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "Shop: ${prod.shopName} • ${prod.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "₹${prod.priceInr}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = TricolorGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (prod.originalMrpInr > prod.priceInr) {
                                Text(
                                    text = "₹${prod.originalMrpInr}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                )
                            }
                        }
                        if (prod.description.isNotBlank()) {
                            Text(text = prod.description, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagesTableGrid(
    messages: List<ChatMessageEntity>,
    dateFormat: SimpleDateFormat
) {
    if (messages.isEmpty()) {
        EmptyTableState("No messages recorded in database.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { msg ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (msg.channel) {
                                    "SMS" -> Color(0xFFEFF6FF)
                                    "WHATSAPP" -> Color(0xFFDCFCE7)
                                    else -> Color(0xFFFFF7ED)
                                }
                            ) {
                                Text(
                                    text = msg.channel,
                                    color = when (msg.channel) {
                                        "SMS" -> Color(0xFF2563EB)
                                        "WHATSAPP" -> Color(0xFF16A34A)
                                        else -> TricolorSaffronDark
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Shop: ${msg.shopName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Text(
                            text = dateFormat.format(Date(msg.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Sender: ${msg.sender.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TricolorNavy)
                    Text(text = msg.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun TransactionsTableGrid(transactions: List<PaymentTransactionEntity>) {
    val context = LocalContext.current
    if (transactions.isEmpty()) {
        EmptyTableState("No payment transactions recorded in database yet.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transactions) { txn ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Top line: System Transaction & Receipt Numbers + Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = txn.transactionNumber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TricolorNavy
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("TXN", txn.transactionNumber))
                                        Toast.makeText(context, "Txn No. Copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                            Text(
                                text = "Receipt: ${txn.receiptNumber} • ${txn.dateTime}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "₹${txn.amountInr.toInt()} • SUCCESS",
                                color = Color(0xFF16A34A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Merchant & Plan Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Shop: ${txn.shopName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customer: ${txn.customerName} (${txn.mobileNumber})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Plan: ${txn.planTitle} • ${txn.planDuration}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TricolorSaffronDark
                            )
                            Text(
                                text = "Payee: ${txn.issuedBy} (${txn.upiId})",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (!txn.userUtrRef.isNullOrBlank()) {
                                Text(
                                    text = "UTR Ref: ${txn.userUtrRef}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TricolorGreenDark
                                )
                            }
                        }

                        // Screenshot thumbnail if uploaded
                        if (!txn.screenshotUri.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, TricolorGreen),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    AsyncImage(
                                        model = txn.screenshotUri,
                                        contentDescription = "Payment Screenshot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(
                                    text = "Proof Attached",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TricolorGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTableState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}
