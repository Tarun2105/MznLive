package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.entity.NewsArticleEntity
import com.example.util.ShareUtils
import com.example.util.emulatorScrollable

// Color Palette consistent with Mznlive branding
private val TricolorSaffron = Color(0xFFFF9933)
private val TricolorGreen = Color(0xFF138808)
private val TricolorNavy = Color(0xFF001A3A)
private val NewsAccentRed = Color(0xFFE53935)
private val CardBorderColor = Color(0xFFE2E8F0)
private val ChipSelectedBg = Color(0xFF001A3A)
private val ChipUnselectedBg = Color(0xFFF1F5F9)

/**
 * NewsFeed screen powered by local Room Database.
 * Displays local news articles with headlines, thumbnails, category filtering,
 * live Room DB persistence, bookmarking, and local article creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    articles: List<NewsArticleEntity>,
    language: String = "hi",
    isRefreshing: Boolean = false,
    onNavigateBack: () -> Unit,
    onToggleLanguage: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onToggleBookmark: (Long, Boolean) -> Unit = { _, _ -> },
    onAddArticle: (
        headline: String,
        headlineHi: String,
        summary: String,
        summaryHi: String,
        thumbnailUrl: String,
        category: String,
        isBreaking: Boolean,
        author: String
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDeleteArticle: (Long) -> Unit = {},
    onResetSeed: () -> Unit = {}
) {
    // Hardware & Gesture back navigation to previous / Home screen
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val isHindi = language == "hi"

    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Search and filtering state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Dialog state for adding a new local news article into Room DB
    var showAddArticleDialog by remember { mutableStateOf(false) }

    // Dialog state for reading full article in-app
    var activeArticleForReading by remember { mutableStateOf<NewsArticleEntity?>(null) }

    // Categories list
    val categories = remember(isHindi) {
        listOf(
            "All" to if (isHindi) "सभी" else "All",
            "Breaking" to if (isHindi) "ब्रेकिंग" else "Breaking",
            "Local" to if (isHindi) "स्थानीय" else "Local",
            "Civic" to if (isHindi) "नागरिक" else "Civic",
            "Agriculture" to if (isHindi) "कृषि" else "Agriculture",
            "Education" to if (isHindi) "शिक्षा" else "Education",
            "Business" to if (isHindi) "व्यापार" else "Business",
            "Sports" to if (isHindi) "खेल" else "Sports",
            "Saved" to if (isHindi) "सहेजे गए" else "Saved"
        )
    }

    // Filtered list computation from Room Database flow
    val filteredArticles = remember(articles, searchQuery, selectedCategory, isHindi) {
        articles.filter { item ->
            // Category filter
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Breaking" -> item.isBreaking
                "Saved" -> item.isBookmarked
                else -> item.category.equals(selectedCategory, ignoreCase = true)
            }

            // Search filter
            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                item.headline.lowercase().contains(query) ||
                item.headlineHi.lowercase().contains(query) ||
                item.summary.lowercase().contains(query) ||
                item.summaryHi.lowercase().contains(query) ||
                item.sourceName.lowercase().contains(query) ||
                item.author.lowercase().contains(query) ||
                item.category.lowercase().contains(query)

            matchesCategory && matchesSearch
        }
    }

    val breakingNews = remember(articles) {
        articles.firstOrNull { it.isBreaking }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("news_feed_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.applogo3dtrns),
                            contentDescription = "MznLive Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("top_left_app_logo")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mzn",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Live",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = TricolorSaffron
                                )
                                Text(
                                    text = if (isHindi) " • समाचार" else " • News",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TricolorGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Live indicator
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NewsAccentRed)
                                )
                            }
                            Text(
                                text = if (isHindi) "रूम डेटाबेस • स्थानीय दैनिक समाचार" else "Room Database • Local News Feed",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("news_feed_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Refresh Button
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        modifier = Modifier.testTag("news_feed_refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = TricolorSaffron
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh News",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TricolorNavy
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddArticleDialog = true },
                containerColor = TricolorSaffron,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Post Local News"
                    )
                },
                text = {
                    Text(
                        text = if (isHindi) "समाचार पोस्ट करें" else "Post News",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .testTag("add_news_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Search Bar & Filter Strip
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_search_field"),
                        placeholder = {
                            Text(
                                text = if (isHindi) "स्थानीय समाचार खोजें (स्मार्ट सिटी, मंडी, कृषि...)" else "Search local news, topics, headlines...",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TricolorNavy
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TricolorNavy,
                            unfocusedBorderColor = CardBorderColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { (key, label) ->
                            val isSelected = selectedCategory == key
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) ChipSelectedBg else ChipUnselectedBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) ChipSelectedBg else CardBorderColor
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedCategory = key }
                                    .testTag("news_filter_chip_$key")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    if (key == "Breaking") {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(NewsAccentRed)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else if (key == "Saved") {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = if (isSelected) Color.White else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Local Room Database status strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Local Room DB",
                                tint = TricolorGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi)
                                    "स्थानीय रूम डेटाबेस: ${filteredArticles.size} समाचार उपलब्ध"
                                else
                                    "Room Database: ${filteredArticles.size} articles stored offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }

                        Text(
                            text = if (isHindi) "डिफ़ॉल्ट रीसेट" else "Reset Seed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorSaffron,
                            modifier = Modifier
                                .clickable {
                                    onResetSeed()
                                    Toast.makeText(
                                        context,
                                        if (isHindi) "डिफ़ॉल्ट समाचार डेटाबेस में पुनः लोड किए गए" else "Default news reloaded into Room Database",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("reset_room_seed_button")
                        )
                    }
                }
            }

            // Linear Progress Indicator when refreshing
            AnimatedVisibility(
                visible = isRefreshing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .testTag("news_refresh_progress"),
                    color = TricolorSaffron,
                    trackColor = TricolorNavy.copy(alpha = 0.1f)
                )
            }

            // Pull-To-Refresh Box wrapping scrollable news content
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("news_pull_refresh_box"),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = TricolorNavy,
                        color = TricolorSaffron
                    )
                }
            ) {
                if (filteredArticles.isEmpty()) {
                    // Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Newspaper,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(68.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isHindi) "कोई समाचार लेख नहीं मिला" else "No News Articles Found",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TricolorNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isHindi)
                                    "स्थानिक रूम डेटाबेस में इस श्रेणी के लिए समाचार उपलब्ध नहीं हैं।"
                                else
                                    "No articles in Room DB match the current filter or query.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        searchQuery = ""
                                        selectedCategory = "All"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy)
                                ) {
                                    Text(if (isHindi) "सभी देखें" else "View All")
                                }

                                OutlinedButton(
                                    onClick = { onResetSeed() }
                                ) {
                                    Text(if (isHindi) "रीसेट करें" else "Restore Seed")
                                }
                            }
                        }
                    }
                } else {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .emulatorScrollable(listState)
                            .testTag("news_feed_list")
                    ) {
                        // Breaking News Spotlight Banner (only if All or Breaking is selected and search is empty)
                        if (breakingNews != null && (selectedCategory == "All" || selectedCategory == "Breaking") && searchQuery.isEmpty()) {
                            item(key = "spotlight_breaking_${breakingNews.id}") {
                                BreakingNewsSpotlightCard(
                                    news = breakingNews,
                                    isHindi = isHindi,
                                    onOpenArticle = {
                                        activeArticleForReading = breakingNews
                                    },
                                    onShare = {
                                        val title = if (isHindi && breakingNews.headlineHi.isNotBlank()) breakingNews.headlineHi else breakingNews.headline
                                        val desc = if (isHindi && breakingNews.summaryHi.isNotBlank()) breakingNews.summaryHi else breakingNews.summary
                                        ShareUtils.shareArticle(context, title, desc, breakingNews.sourceName, "https://mznlive.in/news/${breakingNews.id}")
                                    }
                                )
                            }
                        }

                        // Feed Items from Room Database
                        items(
                            items = filteredArticles,
                            key = { it.id }
                        ) { article ->
                            NewsArticleCard(
                                article = article,
                                isHindi = isHindi,
                                onToggleBookmark = {
                                    onToggleBookmark(article.id, article.isBookmarked)
                                    val msg = if (!article.isBookmarked) {
                                        if (isHindi) "रूम डेटाबेस में बुकमार्क सहेजा गया" else "Saved to Room DB bookmarks"
                                    } else {
                                        if (isHindi) "बुकमार्क हटाया गया" else "Bookmark removed"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                onOpenArticle = {
                                    activeArticleForReading = article
                                },
                                onDeleteArticle = {
                                    onDeleteArticle(article.id)
                                    Toast.makeText(
                                        context,
                                        if (isHindi) "समाचार रूम डेटाबेस से हटाया गया" else "Article deleted from Room DB",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onShare = {
                                    val aTitle = if (isHindi && article.headlineHi.isNotBlank()) article.headlineHi else article.headline
                                    val aDesc = if (isHindi && article.summaryHi.isNotBlank()) article.summaryHi else article.summary
                                    ShareUtils.shareArticle(context, aTitle, aDesc, article.sourceName, "https://mznlive.in/news/${article.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Local News Article Dialog (saves directly to Room Database)
    if (showAddArticleDialog) {
        AddNewsArticleDialog(
            isHindi = isHindi,
            onDismiss = { showAddArticleDialog = false },
            onSave = { headline, headlineHi, summary, summaryHi, thumbUrl, category, isBreaking, author ->
                onAddArticle(headline, headlineHi, summary, summaryHi, thumbUrl, category, isBreaking, author)
                showAddArticleDialog = false
                Toast.makeText(
                    context,
                    if (isHindi) "नया समाचार रूम डेटाबेस में जोड़ा गया!" else "Article saved to local Room Database!",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    // Full In-App Article Reader Dialog
    if (activeArticleForReading != null) {
        val article = activeArticleForReading!!
        ArticleReaderDialog(
            article = article,
            isHindi = isHindi,
            onDismiss = { activeArticleForReading = null },
            onToggleBookmark = {
                onToggleBookmark(article.id, article.isBookmarked)
                // Update local state for immediate feedback
                activeArticleForReading = article.copy(isBookmarked = !article.isBookmarked)
            },
            onShare = {
                val aTitle = if (isHindi && article.headlineHi.isNotBlank()) article.headlineHi else article.headline
                val aDesc = if (isHindi && article.summaryHi.isNotBlank()) article.summaryHi else article.summary
                ShareUtils.shareArticle(context, aTitle, aDesc, article.sourceName, "https://mznlive.in/news/${article.id}")
            }
        )
    }
}

/**
 * Featured Spotlight Card for Breaking News with Coil Image & Gradient Overlay
 */
@Composable
private fun BreakingNewsSpotlightCard(
    news: NewsArticleEntity,
    isHindi: Boolean,
    onOpenArticle: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val title = if (isHindi && news.headlineHi.isNotBlank()) news.headlineHi else news.headline
    val description = if (isHindi && news.summaryHi.isNotBlank()) news.summaryHi else news.summary
    val fallbackImg = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&auto=format&fit=crop&q=80"
    val imageUrl = if (news.thumbnailUrl.isNotBlank()) news.thumbnailUrl else fallbackImg

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenArticle() }
            .testTag("breaking_news_spotlight_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            // Coil AsyncImage loading with crop
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // High-contrast gradient overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Breaking Badge & Source
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NewsAccentRed)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "ब्रेकिंग न्यूज़" else "BREAKING NEWS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = news.sourceName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom Content
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = TricolorSaffron,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = news.timeAgo,
                                color = TricolorSaffron,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onShare,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TricolorGreen,
                                modifier = Modifier.clickable { onOpenArticle() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (isHindi) "पूरा पढ़ें" else "Read",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard News Article Card with Coil Image, Headline, Summary, Source Badge and Room DB Actions
 */
@Composable
private fun NewsArticleCard(
    article: NewsArticleEntity,
    isHindi: Boolean,
    onToggleBookmark: () -> Unit,
    onOpenArticle: () -> Unit,
    onDeleteArticle: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val title = if (isHindi && article.headlineHi.isNotBlank()) article.headlineHi else article.headline
    val description = if (isHindi && article.summaryHi.isNotBlank()) article.summaryHi else article.summary
    val fallbackImg = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&auto=format&fit=crop&q=80"
    val imageUrl = if (article.thumbnailUrl.isNotBlank()) article.thumbnailUrl else fallbackImg

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenArticle() }
            .testTag("news_card_${article.id}")
    ) {
        Column {
            // Header Image loaded via Coil AsyncImage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Category & Breaking badge at top-left
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (article.isBreaking) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NewsAccentRed
                        ) {
                            Text(
                                text = "BREAKING",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TricolorNavy.copy(alpha = 0.88f)
                    ) {
                        Text(
                            text = article.category.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                // Top Right Action Buttons: Bookmark & Delete
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Bookmark Icon button (saves to Room DB)
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { onToggleBookmark() }
                            .testTag("bookmark_button_${article.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (article.isBookmarked) "Remove bookmark" else "Save to Room DB",
                                tint = if (article.isBookmarked) TricolorSaffron else Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Delete button (for local management)
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { onDeleteArticle() }
                            .testTag("delete_news_button_${article.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete article",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // Source label at bottom-left of thumbnail
                Surface(
                    shape = RoundedCornerShape(topEnd = 8.dp),
                    color = Color.Black.copy(alpha = 0.72f),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = article.sourceName,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Article Body
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // Headline
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Summary
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer metadata & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time and Author
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = article.timeAgo,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = article.author,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 110.dp)
                        )
                    }

                    // Share and Read Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share News",
                                tint = TricolorNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        FilledTonalButton(
                            onClick = onOpenArticle,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = TricolorNavy.copy(alpha = 0.08f),
                                contentColor = TricolorNavy
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (isHindi) "पढ़ें" else "Read",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a new local news article directly to Room Database
 */
@Composable
private fun AddNewsArticleDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        headline: String,
        headlineHi: String,
        summary: String,
        summaryHi: String,
        thumbnailUrl: String,
        category: String,
        isBreaking: Boolean,
        author: String
    ) -> Unit
) {
    var headline by remember { mutableStateOf("") }
    var headlineHi by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var summaryHi by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Local") }
    var isBreaking by remember { mutableStateOf(false) }
    var author by remember { mutableStateOf("Muzaffarnagar Reporter") }
    var thumbnailUrl by remember {
        mutableStateOf("https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=600&auto=format&fit=crop&q=80")
    }

    val availableCategories = listOf("Local", "Civic", "Agriculture", "Education", "Business", "Sports")

    // Quick Thumbnail Presets for easy local article publishing
    val thumbnailPresets = listOf(
        "Smart City" to "https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=600&auto=format&fit=crop&q=80",
        "Mandi / Agriculture" to "https://images.unsplash.com/photo-1595974482597-4b8da8879bc5?w=600&auto=format&fit=crop&q=80",
        "Highway / Express" to "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600&auto=format&fit=crop&q=80",
        "College / Tech" to "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=600&auto=format&fit=crop&q=80",
        "Hospital / Health" to "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600&auto=format&fit=crop&q=80"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_news_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "स्थानीय समाचार पोस्ट करें" else "Post Local News Article",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TricolorNavy
                        )
                        Text(
                            text = if (isHindi) "रूम डेटाबेस में स्थानीय रूप से सहेजा जाएगा" else "Stores locally in Room Database (SQLite)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Headline English
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text(if (isHindi) "शीर्षक (English) *" else "Headline (English) *") },
                    placeholder = { Text("e.g. New Flyover Commissioned on Circular Road") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_news_headline"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Headline Hindi (Optional)
                OutlinedTextField(
                    value = headlineHi,
                    onValueChange = { headlineHi = it },
                    label = { Text(if (isHindi) "शीर्षक (हिन्दी)" else "Headline (Hindi - Optional)") },
                    placeholder = { Text("उदा. सर्कुलर रोड पर नए फ्लाईओवर का शुभारंभ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_news_headline_hi"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Summary English
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(if (isHindi) "विवरण (Summary) *" else "Summary Description *") },
                    placeholder = { Text("Brief local report, key details, impact...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("input_news_summary"),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection
                Text(
                    text = if (isHindi) "श्रेणी चुनें" else "Select Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCategories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) TricolorNavy else ChipUnselectedBg,
                            border = BorderStroke(1.dp, if (isSelected) TricolorNavy else CardBorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breaking Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF1F2))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NewsAccentRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "ब्रेकिंग न्यूज़ चिन्हित करें" else "Mark as Breaking News",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NewsAccentRed
                        )
                    }
                    Switch(
                        checked = isBreaking,
                        onCheckedChange = { isBreaking = it },
                        modifier = Modifier.testTag("switch_is_breaking")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thumbnail Preset selector
                Text(
                    text = if (isHindi) "थंबनेल छवि चुनें" else "Choose Thumbnail Image",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(thumbnailPresets) { (name, url) ->
                        val isSelected = thumbnailUrl == url
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, if (isSelected) TricolorSaffron else Color.Transparent),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { thumbnailUrl = url }
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(88.dp)
                                    .background(Color(0xFFF8FAFC))
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp, 50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = name,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Author Name
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(if (isHindi) "रिपोर्टर / ब्यूरो का नाम" else "Author / Reporter Desk") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_news_author"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isHindi) "रद्द करें" else "Cancel")
                    }

                    Button(
                        onClick = {
                            if (headline.isNotBlank() && summary.isNotBlank()) {
                                onSave(
                                    headline.trim(),
                                    headlineHi.trim(),
                                    summary.trim(),
                                    summaryHi.trim(),
                                    thumbnailUrl,
                                    selectedCategory,
                                    isBreaking,
                                    author.trim()
                                )
                            }
                        },
                        enabled = headline.isNotBlank() && summary.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_news_article_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "रूम DB में सहेजें" else "Save to Room DB")
                    }
                }
            }
        }
    }
}

/**
 * Full in-app reader modal dialog displaying the complete article content
 */
@Composable
private fun ArticleReaderDialog(
    article: NewsArticleEntity,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit
) {
    val title = if (isHindi && article.headlineHi.isNotBlank()) article.headlineHi else article.headline
    val summary = if (isHindi && article.summaryHi.isNotBlank()) article.summaryHi else article.summary
    val fallbackImg = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&auto=format&fit=crop&q=80"
    val imageUrl = if (article.thumbnailUrl.isNotBlank()) article.thumbnailUrl else fallbackImg

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("article_detail_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (article.isBreaking) NewsAccentRed else TricolorNavy,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (article.isBreaking) "BREAKING • ${article.category.uppercase()}" else article.category.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Article Content Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Desk and Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = article.author,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = TricolorSaffron,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = article.timeAgo,
                                fontSize = 11.sp,
                                color = TricolorSaffron,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CardBorderColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary / Body Paragraphs
                    Text(
                        text = summary,
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Local verification card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = TricolorGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = article.sourceName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TricolorNavy
                                )
                                Text(
                                    text = if (isHindi)
                                        "मुजफ्फरनगर स्थानीय समाचार डेस्क द्वारा सत्यापित व रूम डेटाबेस में सुरक्षित"
                                    else
                                        "Verified by Muzaffarnagar local desk & securely persisted in Room DB",
                                    fontSize = 10.5.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Footer Bar with Bookmark & Share
                Surface(
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onToggleBookmark
                        ) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (article.isBookmarked) TricolorSaffron else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (article.isBookmarked) {
                                    if (isHindi) "सहेजा गया" else "Bookmarked"
                                } else {
                                    if (isHindi) "सहेजें" else "Bookmark"
                                },
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onShare,
                            colors = ButtonDefaults.buttonColors(containerColor = TricolorNavy)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "शेयर करें" else "Share News",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
