package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MarketplaceShop
import com.example.data.model.PromotionalProduct
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Integrated Application Chat Messenger between Users and Shop Owners.
 * Supports:
 * - Direct in-app messaging (saved into SQLite database)
 * - 1-Click SMS messaging directly to shop owner's mobile
 * - 1-Click WhatsApp messaging directly to shop owner's WhatsApp
 * - Product inquiry banner with prefilled message
 */
@Composable
fun ChatDialog(
    shop: MarketplaceShop?,
    selectedProduct: PromotionalProduct? = null,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onSendSms: (phone: String, message: String) -> Unit = { _, _ -> },
    onSendWhatsApp: (whatsapp: String, message: String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }
    val context = LocalContext.current

    val initialText = remember(selectedProduct) {
        if (selectedProduct != null) {
            "Namaste! I am interested in '${selectedProduct.title}' (₹${selectedProduct.priceInr}). Is it currently available for immediate order or store pickup?"
        } else {
            ""
        }
    }

    var textInput by remember { mutableStateOf(initialText) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("chat_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                    Image(
                        painter = painterResource(id = R.drawable.applogo3dtrns),
                        contentDescription = "MznLive Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .testTag("top_left_app_logo")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mzn",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Live",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TricolorSaffron
                            )
                            Text(
                                text = " • Chat",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorGreen
                            )
                        }
                        Text(
                            text = shop?.name ?: "Direct Merchant & User Chat • Online",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Chat",
                            tint = Color.White
                        )
                    }
                }

                // Indian Tricolor Accent Line under header
                TricolorAccentBar(
                    modifier = Modifier.fillMaxWidth(),
                    height = 2.5.dp
                )

                // Optional Selected Product Banner
                if (selectedProduct != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = selectedProduct.imageUrl,
                                contentDescription = selectedProduct.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedProduct.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "₹${selectedProduct.priceInr} • ${selectedProduct.shopName}",
                                    fontSize = 11.sp,
                                    color = TricolorGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Quick Contact Bar: SMS & WhatsApp Buttons
                val targetPhone = shop?.phone?.ifBlank { "9897123456" } ?: "9897123456"
                val targetWhatsApp = shop?.whatsapp?.ifBlank { targetPhone } ?: targetPhone

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contact Direct:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TricolorNavy
                    )

                    // WhatsApp Contact Button
                    FilledTonalButton(
                        onClick = {
                            val msg = textInput.ifBlank { "Namaste! Connecting via MznLive." }
                            onSendWhatsApp(targetWhatsApp, msg)
                            openWhatsAppIntent(context, targetWhatsApp, msg)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFDCFCE7),
                            contentColor = Color(0xFF16A34A)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("chat_whatsapp_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // SMS Contact Button
                    FilledTonalButton(
                        onClick = {
                            val msg = textInput.ifBlank { "Namaste! Connecting via MznLive." }
                            onSendSms(targetPhone, msg)
                            openSmsIntent(context, targetPhone, msg)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFEFF6FF),
                            contentColor = Color(0xFF2563EB)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("chat_sms_button")
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SMS Msg", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isUser) 14.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 14.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) TricolorSaffron else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (!isUser) {
                                        Text(
                                            text = msg.shopName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TricolorGreen
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text(
                                        text = msg.message,
                                        fontSize = 13.sp,
                                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Bar
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type message to shop owner...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TricolorSaffron)
                            .testTag("send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun openWhatsAppIntent(context: Context, rawPhone: String, message: String) {
    try {
        val cleanNumber = rawPhone.filter { it.isDigit() }
        val targetNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        val uri = Uri.parse("https://wa.me/$targetNumber?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "WhatsApp is not installed. Sending message in-app.", Toast.LENGTH_SHORT).show()
    }
}

private fun openSmsIntent(context: Context, rawPhone: String, message: String) {
    try {
        val cleanNumber = rawPhone.filter { it.isDigit() }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$cleanNumber")
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Cannot open SMS app.", Toast.LENGTH_SHORT).show()
    }
}
