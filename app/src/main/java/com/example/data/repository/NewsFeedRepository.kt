package com.example.data.repository

import com.example.data.local.dao.NewsArticleDao
import com.example.data.local.entity.NewsArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting Room Database data access for Local NewsFeed.
 */
class NewsFeedRepository(private val newsArticleDao: NewsArticleDao) {

    val allArticles: Flow<List<NewsArticleEntity>> = newsArticleDao.getAllArticles()
    val bookmarkedArticles: Flow<List<NewsArticleEntity>> = newsArticleDao.getBookmarkedArticles()

    fun getArticlesByCategory(category: String): Flow<List<NewsArticleEntity>> {
        return if (category.equals("All", ignoreCase = true)) {
            newsArticleDao.getAllArticles()
        } else {
            newsArticleDao.getArticlesByCategory(category)
        }
    }

    fun searchArticles(query: String): Flow<List<NewsArticleEntity>> {
        return newsArticleDao.searchArticles(query)
    }

    suspend fun insertArticles(articles: List<NewsArticleEntity>) =
        newsArticleDao.insertArticles(articles)

    suspend fun insertArticle(article: NewsArticleEntity): Long =
        newsArticleDao.insertArticle(article)

    suspend fun updateArticle(article: NewsArticleEntity) =
        newsArticleDao.updateArticle(article)

    suspend fun toggleBookmark(id: Long, currentBookmark: Boolean) =
        newsArticleDao.setBookmark(id, !currentBookmark)

    suspend fun deleteArticle(id: Long) =
        newsArticleDao.deleteArticleById(id)

    suspend fun clearAll() = newsArticleDao.clearAll()

    suspend fun getCount(): Int = newsArticleDao.getCount()

    /**
     * Seeds initial local news articles into Room Database if empty.
     */
    suspend fun seedDefaultLocalNewsIfEmpty() {
        if (newsArticleDao.getCount() == 0) {
            newsArticleDao.insertArticles(defaultLocalNewsSeed)
        }
    }

    /**
     * Clears all articles and re-seeds the verified default local news articles.
     */
    suspend fun resetToDefaultSeed() {
        newsArticleDao.clearAll()
        newsArticleDao.insertArticles(defaultLocalNewsSeed)
    }

    companion object {
        val defaultLocalNewsSeed = listOf(
            NewsArticleEntity(
                id = 1,
                headline = "Muzaffarnagar Smart City Upgrade: Clock Tower & Gandhi Colony LED Infrastructure Overhaul Completed",
                headlineHi = "मुजफ्फरनगर स्मार्ट सिटी अपग्रेड: क्लॉक टावर और गांधी कॉलोनी में एलईडी इन्फ्रास्ट्रक्चर पूरा",
                summary = "Municipal Corporation inaugurates the smart lighting and automated street monitoring system covering major city junctions.",
                summaryHi = "नगर निगम ने शहर के प्रमुख चौराहों को कवर करने वाली स्मार्ट लाइटिंग और ऑटोमेटेड निगरानी प्रणाली का शुभारंभ किया।",
                thumbnailUrl = "https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=600&auto=format&fit=crop&q=80",
                category = "Civic",
                author = "Rajesh Sharma",
                sourceName = "Mzn City Bureau",
                publishedAt = System.currentTimeMillis() - 15 * 60 * 1000,
                timeAgo = "15 mins ago",
                isBreaking = true,
                readCount = 540
            ),
            NewsArticleEntity(
                id = 2,
                headline = "Asia's Largest Jaggery Mandi Records Bumper Sugarcane Arrivals: Digital Testing Labs Inaugurated",
                headlineHi = "एशिया की सबसे बड़ी गुड़ मंडी में बंपर गन्ने की आवक: डिजिटल परीक्षण प्रयोगशाला का उद्घाटन",
                summary = "Farmers welcome computerized quality grading and instant direct bank payment counters at the Muzaffarnagar Mandi Parishad.",
                summaryHi = "मुजफ्फरनगर मंडी परिषद में किसानों ने कम्प्यूटरीकृत गुणवत्ता ग्रेडिंग और तत्काल बैंक भुगतान काउंटरों का स्वागत किया।",
                thumbnailUrl = "https://images.unsplash.com/photo-1595974482597-4b8da8879bc5?w=600&auto=format&fit=crop&q=80",
                category = "Agriculture",
                author = "Virendra Singh",
                sourceName = "Krishi Desk",
                publishedAt = System.currentTimeMillis() - 45 * 60 * 1000,
                timeAgo = "45 mins ago",
                isBreaking = false,
                readCount = 412
            ),
            NewsArticleEntity(
                id = 3,
                headline = "Delhi-Dehradun Greenfield Expressway: Muzaffarnagar Bypass Interchange Opens to Commuters",
                headlineHi = "दिल्ली-देहरादून एक्सप्रेसवे: मुजफ्फरनगर बाईपास इंटरचेंज यात्रियों के लिए खुला",
                summary = "Travel time between Delhi and Muzaffarnagar drops to 90 minutes as the 6-lane elevated expressway segment is commissioned.",
                summaryHi = "6-लेन एलिवेटेड एक्सप्रेसवे खंड शुरू होने से दिल्ली और मुजफ्फरनगर के बीच यात्रा का समय घटकर 90 मिनट रह गया है।",
                thumbnailUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600&auto=format&fit=crop&q=80",
                category = "Breaking",
                author = "Amit Malik",
                sourceName = "National Highway Bureau",
                publishedAt = System.currentTimeMillis() - 90 * 60 * 1000,
                timeAgo = "1.5 hours ago",
                isBreaking = true,
                readCount = 890
            ),
            NewsArticleEntity(
                id = 4,
                headline = "SD Inter College & DAV Centenary College Secure Top Honors at State Science & Tech Olympiad",
                headlineHi = "एसडी इंटर कॉलेज और डीएवी कॉलेज ने राज्य विज्ञान एवं प्रौद्योगिकी ओलंपियाड में शीर्ष स्थान प्राप्त किया",
                summary = "Students from Muzaffarnagar schools bagged 5 gold medals in robotics, clean energy, and AI model exhibitions in Lucknow.",
                summaryHi = "मुजफ्फरनगर के विद्यार्थियों ने लखनऊ में रोबोटिक्स, स्वच्छ ऊर्जा और एआई मॉडल प्रदर्शनियों में 5 स्वर्ण पदक जीते।",
                thumbnailUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=600&auto=format&fit=crop&q=80",
                category = "Education",
                author = "Pooja Verma",
                sourceName = "Campus Watch",
                publishedAt = System.currentTimeMillis() - 3 * 3600 * 1000,
                timeAgo = "3 hours ago",
                isBreaking = false,
                readCount = 310
            ),
            NewsArticleEntity(
                id = 5,
                headline = "District Hospital Civil Lines Launches 24x7 Digital Cardiology Wing and Advanced Dialysis Center",
                headlineHi = "जिला अस्पताल सिविल लाइंस ने 24x7 डिजिटल कार्डियोलॉजी विंग और उन्नत डायलिसिस केंद्र शुरू किया",
                summary = "New healthcare facility equipped with state-of-the-art ICU monitoring units provides free emergency treatments to local residents.",
                summaryHi = "अत्याधुनिक आईसीयू निगरानी इकाइयों से सुसज्जित नई स्वास्थ्य सुविधा स्थानीय नागरिकों को निःशुल्क आपातकालीन उपचार प्रदान करती है।",
                thumbnailUrl = "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600&auto=format&fit=crop&q=80",
                category = "Local",
                author = "Dr. S. K. Gupta",
                sourceName = "Health Pulse",
                publishedAt = System.currentTimeMillis() - 5 * 3600 * 1000,
                timeAgo = "5 hours ago",
                isBreaking = false,
                readCount = 525
            ),
            NewsArticleEntity(
                id = 6,
                headline = "Muzaffarnagar Handicraft & Commerce Fair Inaugurated at Purani Tehsil: 200+ Regional Merchants",
                headlineHi = "पुरानी तहसील में मुजफ्फरनगर हस्तशिल्प एवं व्यापार मेले का उद्घाटन: 200+ क्षेत्रीय व्यापारी",
                summary = "The 5-day exhibition showcases traditional wood carvings, embroidered banarasi silks, and organic agricultural products.",
                summaryHi = "5 दिवसीय प्रदर्शनी में पारंपरिक लकड़ी की नक्काशी, कढ़ाई वाली बनारसी सिल्क और जैविक कृषि उत्पादों का प्रदर्शन किया गया।",
                thumbnailUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600&auto=format&fit=crop&q=80",
                category = "Business",
                author = "Nitin Goel",
                sourceName = "Vyapar Samachar",
                publishedAt = System.currentTimeMillis() - 8 * 3600 * 1000,
                timeAgo = "8 hours ago",
                isBreaking = false,
                readCount = 380
            ),
            NewsArticleEntity(
                id = 7,
                headline = "Chaudhary Charan Singh Stadium Hosts Western UP State Wrestling & Kabaddi Championship Under Lights",
                headlineHi = "चौधरी चरण सिंह स्टेडियम में फ्लडलाइट्स में पश्चिमी यूपी राज्य कुश्ती और कबड्डी चैम्पियनशिप आयोजित",
                summary = "Over 40 teams from 12 districts compete in high-voltage matches with thousands of sports enthusiasts cheering in the stands.",
                summaryHi = "12 जिलों की 40 से अधिक टीमों ने हाई-वोल्टेज मैचों में भाग लिया और हजारों खेल प्रेमियों ने उनका उत्साहवर्धन किया।",
                thumbnailUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=600&auto=format&fit=crop&q=80",
                category = "Sports",
                author = "Randeep Chaudhary",
                sourceName = "Sports Beat",
                publishedAt = System.currentTimeMillis() - 12 * 3600 * 1000,
                timeAgo = "12 hours ago",
                isBreaking = false,
                readCount = 670
            ),
            NewsArticleEntity(
                id = 8,
                headline = "Irrigation Department Completes Pre-Monsoon Kali River Embankment Reinforcement Works",
                headlineHi = "सिंचाई विभाग ने मानसून पूर्व काली नदी तटबंध सुदृढ़ीकरण कार्य पूरा किया",
                summary = "Strengthened flood barriers and modernized sluice gates ensure zero waterlogging in low-lying residential settlements.",
                summaryHi = "मजबूत बाढ़ अवरोधक और आधुनिक स्लुइस गेट निचले रिहायशी इलाकों में जलभराव न होने को सुनिश्चित करते हैं।",
                thumbnailUrl = "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=600&auto=format&fit=crop&q=80",
                category = "Civic",
                author = "Arun Kashyap",
                sourceName = "Civic Watch",
                publishedAt = System.currentTimeMillis() - 24 * 3600 * 1000,
                timeAgo = "1 day ago",
                isBreaking = false,
                readCount = 295
            )
        )
    }
}
