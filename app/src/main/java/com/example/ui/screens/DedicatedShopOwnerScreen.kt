package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

/**
 * Dedicated Online Shop Management Page for Shop Owners.
 * Allows the shop owner to:
 * - View and manage their online shop profile & storefront
 * - Add products with description, price, and image uploaded from Gallery or Camera
 * - View existing products catalog with live Room SQLite persistence
 * - Inspect SQLite data tables
 * - Preview customer storefront
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DedicatedShopOwnerScreen(
    shop: MarketplaceShop,
    products: List<PromotionalProduct>,
    language: String = "hi",
    onBack: () -> Unit,
    onAddProduct: (
        title: String,
        titleHi: String,
        category: String,
        description: String,
        priceInr: Int,
        mrpInr: Int,
        imageUrl: String
    ) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onPreviewCustomerStorefront: (MarketplaceShop) -> Unit,
    onOpenDataTables: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isHindi = language == "hi"

    var showAddProductDialog by remember { mutableStateOf(false) }

    Scaffold(
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = shop.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = TricolorGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Dedicated Shop Owner Portal • ${shop.category}",
                                color = TricolorSaffron,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // SQL Tables Inspector Shortcut
                        IconButton(
                            onClick = onOpenDataTables,
                            modifier = Modifier.testTag("shop_owner_view_tables_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Data Tables",
                                tint = Color.White
                            )
                        }

                        // Preview Storefront Button
                        IconButton(
                            onClick = { onPreviewCustomerStorefront(shop) },
                            modifier = Modifier.testTag("preview_storefront_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Preview Storefront",
                                tint = Color.White
                            )
                        }
                    }
                    TricolorAccentBar(modifier = Modifier.fillMaxWidth(), height = 2.dp)
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddProductDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isHindi) "नया उत्पाद जोड़ें" else "+ Add Product", fontWeight = FontWeight.Bold) },
                containerColor = TricolorSaffronDark,
                contentColor = Color.White,
                modifier = Modifier.testTag("dedicated_shop_add_product_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("dedicated_shop_page"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Shop Owner Profile Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Banner Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            AsyncImage(
                                model = shop.imageUrl,
                                contentDescription = shop.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TricolorGreen,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "LIVE ON MZN MARKET",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Details
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shop.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TricolorNavy
                                    )
                                    Text(
                                        text = shop.address,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = TricolorSaffronDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${shop.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TricolorNavy)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ShopStatItem(label = "Products", value = "${products.size}")
                                ShopStatItem(label = "Inquiries", value = "12+")
                                ShopStatItem(label = "Store Status", value = "Open")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Contact info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Mobile: +91 ${shop.phone}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "WhatsApp: +91 ${shop.whatsapp}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Action Shortcuts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showAddProductDialog = true },
                        modifier = Modifier.weight(1f).testTag("shop_add_product_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "उत्पाद जोड़ें" else "Add Product", fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = { onPreviewCustomerStorefront(shop) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "स्टोरफ्रंट देखें" else "Store View", fontSize = 12.5.sp)
                    }
                }
            }

            // 3. Products Catalog Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "दुकान के उत्पाद सूची (${products.size})" else "Listed Products Catalog (${products.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorNavy
                    )
                    TextButton(onClick = onOpenDataTables) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View SQL Tables", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. Products List
            if (products.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = TricolorSaffronDark,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isHindi) "अभी तक कोई उत्पाद नहीं जोड़ा गया" else "No products added yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHindi) "गैलरी या कैमरे से फोटो अपलोड करके अपने उत्पाद जोड़ें" else "Tap '+ Add Product' to upload photos via Gallery or Camera",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddProductDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TricolorSaffronDark)
                            ) {
                                Text("Add First Product")
                            }
                        }
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    ProductManagementCard(
                        product = product,
                        onDelete = { onDeleteProduct(product.id) }
                    )
                }
            }
        }
    }

    // Add Product Modal Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            shopCategory = shop.category,
            language = language,
            onDismiss = { showAddProductDialog = false },
            onSave = { title, titleHi, category, desc, price, mrp, img ->
                onAddProduct(title, titleHi, category, desc, price, mrp, img)
                showAddProductDialog = false
                Toast.makeText(context, "Product added successfully to shop!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun ShopStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TricolorNavy
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProductManagementCard(
    product: PromotionalProduct,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.priceInr}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = TricolorGreen
                    )
                    if (product.originalMrpInr > product.priceInr) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.originalMrpInr}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        )
                    }
                }
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Product",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Add Product Dialog supporting:
 * - Title, Hindi Title, Price, MRP, Description, Category
 * - Upload via Photo Picker (Gallery)
 * - Upload via Camera capture (Mobile Camera)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    shopCategory: String,
    language: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        titleHi: String,
        category: String,
        description: String,
        priceInr: Int,
        mrpInr: Int,
        imageUrl: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var titleHi by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var mrpText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(shopCategory.ifBlank { "Retails & Kirana (खुदरा और किराना)" }) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fallbackImageUrl by remember { mutableStateOf("") }

    // Android Zero-Permission Photo Picker for Gallery
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
            // Save bitmap to cache and create Uri
            try {
                val file = File(context.cacheDir, "camera_product_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedImageUri = Uri.fromFile(file)
            } catch (_: Exception) {}
        }
    }

    val categories = remember {
        listOf(
            "Retails & Kirana (खुदरा और किराना)",
            "Electronics & Appliances (इलेक्ट्रॉनिक्स)",
            "Clothing & Fashion (कपड़े और फैशन)",
            "Mobiles & Accessories (मोबाइल और गैजेट्स)",
            "Food, Sweets & Bakery (खाद्य व मिष्ठान)",
            "Restaurants & Cafes (रेस्टोरेंट और कैफे)",
            "Services & Repair (सेवाएं व मरम्मत)",
            "Handicrafts & Artisans (हस्तशिल्प और कला)",
            "Jewellery & Ornaments (ज्वेलरी व आभूषण)",
            "Health & Pharmacy (स्वास्थ्य व मेडिकल)",
            "Hardware & Building Material (हार्डवेयर)",
            "Other Category Listing (अन्य श्रेणी)"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Product to Shop",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TricolorNavy
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Image Upload Selector (Gallery or Mobile Camera)
                Text(
                    text = "Product Image (Gallery / Camera)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = TricolorNavy
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        capturedBitmap != null -> {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Camera Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Gallery Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        fallbackImageUrl.isNotBlank() -> {
                            AsyncImage(
                                model = fallbackImageUrl,
                                contentDescription = "Sample Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TricolorSaffronDark, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("No photo chosen yet", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                // Two Upload Buttons: Gallery & Camera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            galleryPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.weight(1f).testTag("product_upload_gallery_btn"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 11.5.sp)
                    }

                    FilledTonalButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f).testTag("product_upload_camera_btn"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 11.5.sp)
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title / Name *") },
                    placeholder = { Text("e.g. 5G Smartphone / Silk Saree") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_product_title_input")
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, fontSize = 12.sp) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Price & MRP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Selling Price (₹) *") },
                        placeholder = { Text("999") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_product_price_input")
                    )

                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Original MRP (₹)") },
                        placeholder = { Text("1299") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description & Features") },
                    placeholder = { Text("Details, warranty, size, package content...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter product title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val price = priceText.toIntOrNull() ?: 0
                    if (price <= 0) {
                        Toast.makeText(context, "Please enter valid price", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val mrp = mrpText.toIntOrNull() ?: (price * 1.2).toInt()
                    val imgUriString = selectedImageUri?.toString() ?: fallbackImageUrl.ifBlank {
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80"
                    }

                    onSave(title, titleHi, selectedCategory, description, price, mrp, imgUriString)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TricolorGreen),
                modifier = Modifier.testTag("save_product_confirm_btn")
            ) {
                Text("Publish to Store")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
