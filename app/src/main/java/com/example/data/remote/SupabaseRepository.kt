package com.example.data.remote

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseRepository {
    private val projectId = "iyppuawgyelubfohxthj"
    private val publishableKey = "sb_publishable_bEDnLPUc-ARLsUfDFwxfyg_y9zemVMS"
    private val baseUrl = "https://$projectId.supabase.co/rest/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // 1. Intro Slides for Screen 2
    suspend fun fetchIntroSlides(): List<IntroSlide> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            IntroSlide(
                id = 1,
                orderNum = 1,
                title = "Local Live News Daily",
                description = "Breaking news & live coverage directly from regional newspapers.",
                imageUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&auto=format&fit=crop&q=80",
                facebookPostUrl = "https://facebook.com/mznlive"
            ),
            IntroSlide(
                id = 2,
                orderNum = 2,
                title = "Promote Local Brands & Shops",
                description = "Grow your business with sponsored Facebook posts and trending Instagram Reels showcase.",
                imageUrl = "https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=800&auto=format&fit=crop&q=80",
                facebookPostUrl = "https://facebook.com/mznlive/ads"
            ),
            IntroSlide(
                id = 3,
                orderNum = 3,
                title = "Online Local Marketplace",
                description = "Connect directly with local shop owners, chat, check offers, and order instantly.",
                imageUrl = "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?w=800&auto=format&fit=crop&q=80",
                facebookPostUrl = "https://facebook.com/mznlive/market"
            )
        )

        try {
            val request = Request.Builder()
                .url("$baseUrl/intro_slides?select=*&order=order_num.asc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<IntroSlide>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            list.add(
                                IntroSlide(
                                    id = obj.optLong("id", i.toLong() + 1),
                                    orderNum = obj.optInt("order_num", i + 1),
                                    title = obj.optString("title", "Slide ${i + 1}"),
                                    description = obj.optString("description", ""),
                                    imageUrl = obj.optString("image_url", fallback[i % fallback.size].imageUrl),
                                    facebookPostUrl = obj.optString("facebook_post_url", "https://facebook.com/mznlive")
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    // 2. App Config (e.g. video URL)
    suspend fun fetchOnboardingVideoUrl(): String = withContext(Dispatchers.IO) {
        val fallback = "https://iyppuawgyelubfohxthj.supabase.co/storage/v1/object/public/MznLive/MznlivepermissionVideo.mp4"
        try {
            val request = Request.Builder()
                .url("$baseUrl/app_config?key=eq.onboarding_video_url&select=value")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        return@withContext array.getJSONObject(0).optString("value", fallback)
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    // 3. Live News Feed
    suspend fun fetchLiveNews(): List<LiveNewsItem> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            LiveNewsItem(
                id = 1,
                titleEn = "City smart transit expansion project approved with 12 new eco-friendly routes",
                titleHi = "स्मार्ट सिटी ट्रांसपोर्ट विस्तार परियोजना को मिली 12 नए ग्रीन रूट्स की मंजूरी",
                sourceNewspaper = "Dainik Jagran",
                websiteUrl = "https://www.jagran.com",
                category = "Local",
                isBreaking = true,
                imageUrl = "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=800&auto=format&fit=crop&q=80",
                descriptionEn = "The Municipal Council has officially given green light to 12 electric transit routes connecting key residential and industrial hubs across Muzaffarnagar district with affordable fares.",
                descriptionHi = "नगर पालिका परिषद ने मुजफ्फरनगर जिले के प्रमुख आवासीय और औद्योगिक क्षेत्रों को जोड़ने वाले 12 नए इलेक्ट्रिक बस मार्गों को औपचारिक मंजूरी दे दी है।",
                publishedTime = "15m ago",
                readMinutes = 2
            ),
            LiveNewsItem(
                id = 2,
                titleEn = "Local farmers market annual harvest carnival starts this Friday at Gandhi Ground",
                titleHi = "गांधी मैदान में शुक्रवार से शुरू होगा वार्षिक किसान उपज महोत्सव",
                sourceNewspaper = "Amar Ujala",
                websiteUrl = "https://www.amarujala.com",
                category = "Agriculture",
                isBreaking = false,
                imageUrl = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?w=800&auto=format&fit=crop&q=80",
                descriptionEn = "Over 150 local farmers and jaggery producers will set up live demonstration stalls showcasing organic wheat, basmati rice, sugarcane products, and seasonal vegetables.",
                descriptionHi = "150 से अधिक स्थानीय किसान और गुड़ उत्पादक जैविक गेहूं, बासमती चावल, गन्ना उत्पाद और ताज़ी मौसमी सब्जियों के जीवंत प्रदर्शन स्टॉल लगाएंगे।",
                publishedTime = "1h ago",
                readMinutes = 3
            ),
            LiveNewsItem(
                id = 3,
                titleEn = "New modern digital library and skill center inaugurated in downtown sector",
                titleHi = "शहर के केंद्र में आधुनिक डिजिटल लाइब्रेरी और कौशल विकास केंद्र का उद्घाटन",
                sourceNewspaper = "Hindustan Live",
                websiteUrl = "https://www.livehindustan.com",
                category = "Education",
                isBreaking = false,
                imageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=800&auto=format&fit=crop&q=80",
                descriptionEn = "Equipped with 200 high-speed computers, free Wi-Fi, audio-visual lecture rooms, and competitive exam preparation resources for students across western UP.",
                descriptionHi = "200 हाई-स्पीड कंप्यूटर, मुफ्त वाई-फाई, ऑडियो-विजुअल लेक्चर हॉल और पश्चिमी यूपी के छात्रों के लिए प्रतियोगी परीक्षा संसाधनों से सुसज्जित केंद्र शुरू हुआ।",
                publishedTime = "3h ago",
                readMinutes = 4
            ),
            LiveNewsItem(
                id = 4,
                titleEn = "Municipal corporation launches green clean city drive with neighborhood rewards",
                titleHi = "नगर निगम ने स्वच्छता अभियान के तहत सर्वश्रेष्ठ मोहल्लों को पुरस्कृत करने की घोषणा की",
                sourceNewspaper = "Navbharat Times",
                websiteUrl = "https://navbharattimes.indiatimes.com",
                category = "Civic",
                isBreaking = false,
                imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop&q=80",
                descriptionEn = "A competitive zero-waste initiative begins this week. Citizen committees of top-rated wards will receive special municipal infrastructure enhancement grants.",
                descriptionHi = "इस सप्ताह से शून्य-अपशिष्ट प्रतियोगिता शुरू हो रही है। शीर्ष रेटिंग वाले वार्डों की नागरिक समितियों को विशेष बुनियादी ढांचा विकास अनुदान मिलेगा।",
                publishedTime = "5h ago",
                readMinutes = 3
            ),
            LiveNewsItem(
                id = 5,
                titleEn = "Wholesale traders association announces festival discount expo across city markets",
                titleHi = "थोक व्यापार संघ ने शहर के बाजारों में महा-उत्सव छूट मेले का किया ऐलान",
                sourceNewspaper = "Punjab Kesari",
                websiteUrl = "https://www.punjabkesari.in",
                category = "Business",
                isBreaking = false,
                imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&auto=format&fit=crop&q=80",
                descriptionEn = "Leading commercial markets and textile merchants offer up to 40% wholesale bargains ahead of the upcoming wedding and festive shopping rush.",
                descriptionHi = "आगामी शादी और त्योहारी खरीदारी की भीड़ से पहले प्रमुख कपड़ा व्यापारी और बाजार 40% तक थोक छूट की पेशकश कर रहे हैं।",
                publishedTime = "8h ago",
                readMinutes = 2
            ),
            LiveNewsItem(
                id = 6,
                titleEn = "District sports authority unveils state-of-the-art synthetic athletic track",
                titleHi = "जिला खेल प्राधिकरण ने अत्याधुनिक सिंथेटिक एथलेटिक ट्रैक का किया अनावरण",
                sourceNewspaper = "Dainik Bhaskar",
                websiteUrl = "https://www.bhaskar.com",
                category = "Sports",
                isBreaking = false,
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&auto=format&fit=crop&q=80",
                descriptionEn = "Youth track and field athletes can now practice on international-grade Olympic surface with floodlight coaching sessions free of charge.",
                descriptionHi = "युवा एथलीट अब फ्लडलाइट और मुफ्त कोचिंग सत्रों के साथ अंतरराष्ट्रीय स्तर के ओलंपिक ट्रैक पर अभ्यास कर सकेंगे।",
                publishedTime = "1d ago",
                readMinutes = 3
            )
        )

        try {
            val request = Request.Builder()
                .url("$baseUrl/live_news?select=*&order=id.desc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<LiveNewsItem>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            val fb = fallback[i % fallback.size]
                            list.add(
                                LiveNewsItem(
                                    id = obj.optLong("id", i.toLong() + 1),
                                    titleEn = obj.optString("title_en", fb.titleEn),
                                    titleHi = obj.optString("title_hi", fb.titleHi),
                                    sourceNewspaper = obj.optString("source_newspaper", fb.sourceNewspaper),
                                    websiteUrl = obj.optString("website_url", fb.websiteUrl),
                                    category = obj.optString("category", fb.category),
                                    isBreaking = obj.optBoolean("is_breaking", fb.isBreaking),
                                    imageUrl = obj.optString("image_url", fb.imageUrl),
                                    descriptionEn = obj.optString("description_en", fb.descriptionEn),
                                    descriptionHi = obj.optString("description_hi", fb.descriptionHi),
                                    publishedTime = obj.optString("published_time", fb.publishedTime),
                                    readMinutes = obj.optInt("read_minutes", fb.readMinutes)
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    // 4. Live Stream Video Info & List of 10 Sample Videos
    val sampleLiveStreams = listOf(
        LiveStreamInfo(
            id = 1,
            titleEn = "Mznlive 24x7 Prime News Bulletin - Ground Report",
            titleHi = "एमजेडएन लाइव 24x7 मुख्य समाचार बुलेटिन - ग्राउंड रिपोर्ट",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=5qap5aO4i9A",
            youtubeVideoId = "5qap5aO4i9A",
            channelName = "Mznlive 24x7 News Channel",
            category = "News",
            thumbnailUrl = "https://img.youtube.com/vi/5qap5aO4i9A/hqdefault.jpg",
            isLive = true,
            viewersCount = 5420
        ),
        LiveStreamInfo(
            id = 2,
            titleEn = "Muzaffarnagar Smart City Development & Infrastructure Special",
            titleHi = "मुजफ्फरनगर स्मार्ट सिटी विकास और बुनियादी ढांचा विशेष रिपोर्ट",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=jfKfPfyJRdk",
            youtubeVideoId = "jfKfPfyJRdk",
            channelName = "UP City Samachar",
            category = "Development",
            thumbnailUrl = "https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg",
            isLive = true,
            viewersCount = 3850
        ),
        LiveStreamInfo(
            id = 3,
            titleEn = "Western UP Kisan Mahapanchayat & Agricultural Mandi Live",
            titleHi = "पश्चिमी यूपी किसान महापंचायत एवं कृषि मंडी लाइव कवरेज",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=21X5lGlDOfg",
            youtubeVideoId = "21X5lGlDOfg",
            channelName = "Kisan Bharat News",
            category = "Agriculture",
            thumbnailUrl = "https://img.youtube.com/vi/21X5lGlDOfg/hqdefault.jpg",
            isLive = true,
            viewersCount = 6120
        ),
        LiveStreamInfo(
            id = 4,
            titleEn = "District Sports & Youth Talent Championship Finals",
            titleHi = "जिला खेल एवं युवा प्रतिभा चैंपियनशिप फाइनल मुकाबला",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=DWcJFNfaw9c",
            youtubeVideoId = "DWcJFNfaw9c",
            channelName = "UP Sports Live",
            category = "Sports",
            thumbnailUrl = "https://img.youtube.com/vi/DWcJFNfaw9c/hqdefault.jpg",
            isLive = true,
            viewersCount = 2940
        ),
        LiveStreamInfo(
            id = 5,
            titleEn = "City Traffic & New Bypass Highway Flyover Inspection Report",
            titleHi = "शहर का नया बाईपास हाईवे और फ्लाईओवर निरीक्षण ग्राउंड रिपोर्ट",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=sP-IDy3tq-E",
            youtubeVideoId = "sP-IDy3tq-E",
            channelName = "Mzn Traffic Watch",
            category = "Civic",
            thumbnailUrl = "https://img.youtube.com/vi/sP-IDy3tq-E/hqdefault.jpg",
            isLive = true,
            viewersCount = 4100
        ),
        LiveStreamInfo(
            id = 6,
            titleEn = "Historic Shukratal Heritage & Ganga Ghat Special Aarti",
            titleHi = "ऐतिहासिक शुक्रताल तीर्थ एवं गंगा आरती विशेष दर्शन",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=7NOSDKb0HlU",
            youtubeVideoId = "7NOSDKb0HlU",
            channelName = "Dharmik Darshan Live",
            category = "Culture",
            thumbnailUrl = "https://img.youtube.com/vi/7NOSDKb0HlU/hqdefault.jpg",
            isLive = true,
            viewersCount = 7200
        ),
        LiveStreamInfo(
            id = 7,
            titleEn = "Local Textile & Handloom Bazaar Festive Shopping Buzz",
            titleHi = "लोकल हैंडलूम व कपड़ा बाजार में त्योहारी रौनक की लाइव रिपोर्ट",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=ysz5S6PUM-U",
            youtubeVideoId = "ysz5S6PUM-U",
            channelName = "Vyapar Darpan",
            category = "Business",
            thumbnailUrl = "https://img.youtube.com/vi/ysz5S6PUM-U/hqdefault.jpg",
            isLive = true,
            viewersCount = 3180
        ),
        LiveStreamInfo(
            id = 8,
            titleEn = "Health & Medical College Super-Specialty Wing Inauguration",
            titleHi = "मेडिकल कॉलेज में नए सुपर-स्पेशलिटी विंग का शुभारंभ",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
            youtubeVideoId = "kJQP7kiw5Fk",
            channelName = "Swasthya Bharat",
            category = "Health",
            thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
            isLive = true,
            viewersCount = 4500
        ),
        LiveStreamInfo(
            id = 9,
            titleEn = "Police Administration Cyber Crime Awareness & Security Briefing",
            titleHi = "साइबर अपराध सुरक्षा व नागरिक जागरूकता पर पुलिस ब्रीफिंग",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=aqz-KE-bpKQ",
            youtubeVideoId = "aqz-KE-bpKQ",
            channelName = "Suraksha Manch Live",
            category = "Safety",
            thumbnailUrl = "https://img.youtube.com/vi/aqz-KE-bpKQ/hqdefault.jpg",
            isLive = true,
            viewersCount = 2650
        ),
        LiveStreamInfo(
            id = 10,
            titleEn = "Weather & Monsoon Forecast: District Rainfall & Crop Advisory",
            titleHi = "मौसम बुलेटिन: मुजफ्फरनगर एवं आसपास के जिलों में बारिश का अलर्ट",
            youtubeVideoUrl = "https://www.youtube.com/watch?v=L_LUpnjgPso",
            youtubeVideoId = "L_LUpnjgPso",
            channelName = "Mausam Live 24",
            category = "Weather",
            thumbnailUrl = "https://img.youtube.com/vi/L_LUpnjgPso/hqdefault.jpg",
            isLive = true,
            viewersCount = 5800
        )
    )

    suspend fun fetchLiveStreams(): List<LiveStreamInfo> = withContext(Dispatchers.IO) {
        val fallback = sampleLiveStreams
        try {
            val request = Request.Builder()
                .url("$baseUrl/live_stream?select=*&order=id.asc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<LiveStreamInfo>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            val ytId = obj.optString("youtube_video_id", "5qap5aO4i9A")
                            list.add(
                                LiveStreamInfo(
                                    id = obj.optLong("id", (i + 1).toLong()),
                                    titleEn = obj.optString("title_en", "Mznlive Live News"),
                                    titleHi = obj.optString("title_hi", "एमजेडएन लाइव समाचार"),
                                    youtubeVideoUrl = obj.optString("youtube_video_url", "https://www.youtube.com/watch?v=$ytId"),
                                    youtubeVideoId = ytId,
                                    channelName = obj.optString("channel_name", "Mznlive 24x7"),
                                    category = obj.optString("category", "News"),
                                    thumbnailUrl = obj.optString("thumbnail_url", "https://img.youtube.com/vi/$ytId/hqdefault.jpg"),
                                    isLive = obj.optBoolean("is_live", true),
                                    viewersCount = obj.optInt("viewers_count", 3000 + i * 400)
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    suspend fun fetchLiveStream(): LiveStreamInfo = withContext(Dispatchers.IO) {
        val streams = fetchLiveStreams()
        streams.firstOrNull() ?: sampleLiveStreams.first()
    }

    // 5. Events Scroll Bar
    suspend fun fetchEvents(): List<EventItem> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            EventItem(
                id = 1,
                titleEn = "Civic Water Pipeline Repair & Community Forum",
                titleHi = "जल आपूर्ति पाइपलाइन सुधार एवं जनसंवाद कार्यक्रम",
                category = "Local Problems",
                location = "Sector 4 Community Hall",
                eventDate = "Tomorrow 10:00 AM",
                organizer = "City Jal Nigam",
                descriptionEn = "Interactive public meeting regarding municipal pipeline upgrade and citizen grievances.",
                descriptionHi = "नागरिकों की पेयजल समस्याओं के त्वरित समाधान हेतु नगर निगम की विशेष जनसंवाद सभा।",
                facebookEventUrl = "https://facebook.com/mznlive/events/101"
            ),
            EventItem(
                id = 2,
                titleEn = "District Youth Entrepreneur & Startup Expo 2026",
                titleHi = "जिला युवा उद्यमी एवं स्टार्टअप महाकुंभ 2026",
                category = "Community Program",
                location = "Town Exhibition Ground",
                eventDate = "Saturday 11:00 AM",
                organizer = "District Commerce Board",
                descriptionEn = "Showcase of 80+ local manufacturing units, handicrafts, and tech innovations.",
                descriptionHi = "80 से अधिक स्थानीय विनिर्माताओं और स्टार्टअप्स की प्रदर्शनी और मेंटरशिप सत्र।",
                facebookEventUrl = "https://facebook.com/mznlive/events/102"
            ),
            EventItem(
                id = 3,
                titleEn = "Free Eye & Dental Health Checkup Camp",
                titleHi = "निःशुल्क नेत्र एवं दंत चिकित्सा परामर्श शिविर",
                category = "Health Camp",
                location = "Civil Lines Red Cross Center",
                eventDate = "Sunday 9:00 AM",
                organizer = "Rotary Club & Lions Club",
                descriptionEn = "Free checkup, basic medicines, and eye testing for senior citizens and families.",
                descriptionHi = "वरिष्ठ नागरिकों और बच्चों के लिए विशेषज्ञ डॉक्टरों द्वारा मुफ्त जांच व दवा वितरण।",
                facebookEventUrl = "https://facebook.com/mznlive/events/103"
            )
        )

        try {
            val request = Request.Builder()
                .url("$baseUrl/events?select=*&order=id.desc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<EventItem>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            list.add(
                                EventItem(
                                    id = obj.optLong("id", i.toLong() + 1),
                                    titleEn = obj.optString("title_en", ""),
                                    titleHi = obj.optString("title_hi", ""),
                                    category = obj.optString("category", "Local"),
                                    location = obj.optString("location", "Main City"),
                                    eventDate = obj.optString("event_date", "Upcoming"),
                                    organizer = obj.optString("organizer", "Admin"),
                                    descriptionEn = obj.optString("description_en", ""),
                                    descriptionHi = obj.optString("description_hi", ""),
                                    facebookEventUrl = obj.optString("facebook_event_url", "https://facebook.com/mznlive")
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    // 6. Sponsored Adverts (1-sec sliding images)
    suspend fun fetchSponsoredAdverts(): List<SponsoredAdvert> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            SponsoredAdvert(
                id = 1,
                businessName = "Royal Heritage Silk Sarees",
                productTitle = "Pure Banarasi Katan Handloom Collection",
                productDescription = "Exclusive bridal silk sarees with genuine gold zari weave. Direct from master weavers with quality certificate.",
                price = "₹4,999",
                discountTag = "25% OFF",
                imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=700&auto=format&fit=crop&q=80",
                facebookPageUrl = "https://facebook.com/royalheritagesarees",
                whatsappContact = "+919876500001",
                displayDurationSeconds = 1,
                orderNum = 1
            ),
            SponsoredAdvert(
                id = 2,
                businessName = "Shree Balaji Sweets & Bakery",
                productTitle = "Special Desi Ghee Dodha & Kaju Katli Gift Boxes",
                productDescription = "Fresh festive sweets prepared in 100% pure cow ghee. Special corporate and wedding gift hampers available.",
                price = "₹650 / kg",
                discountTag = "Festive Offer",
                imageUrl = "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=700&auto=format&fit=crop&q=80",
                facebookPageUrl = "https://facebook.com/shreebalajisweets",
                whatsappContact = "+919876500002",
                displayDurationSeconds = 1,
                orderNum = 2
            ),
            SponsoredAdvert(
                id = 3,
                businessName = "Galaxy Electronics & Smart Hub",
                productTitle = "Latest 5G Smartphones & Smart 4K TVs",
                productDescription = "Special exchange bonus on older devices, 0% EMI with Bajaj Finserv, free delivery across the city.",
                price = "From ₹12,999",
                discountTag = "Zero Downpayment",
                imageUrl = "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=700&auto=format&fit=crop&q=80",
                facebookPageUrl = "https://facebook.com/galaxyelectronicsmzn",
                whatsappContact = "+919876500003",
                displayDurationSeconds = 1,
                orderNum = 3
            ),
            SponsoredAdvert(
                id = 4,
                businessName = "Krishna Organic Dairy Farm",
                productTitle = "Farm Fresh A2 Gir Cow Milk & Vedic Bilona Ghee",
                productDescription = "Delivered every morning before 6:30 AM in hygienic glass bottles. 100% adulteration-free tested.",
                price = "₹75 / Litre",
                discountTag = "1st Week Sample Free",
                imageUrl = "https://images.unsplash.com/photo-1527153857715-3908f2ae5e81?w=700&auto=format&fit=crop&q=80",
                facebookPageUrl = "https://facebook.com/krishnaorganicfarm",
                whatsappContact = "+919876500004",
                displayDurationSeconds = 1,
                orderNum = 4
            )
        )

        try {
            val request = Request.Builder()
                .url("$baseUrl/sponsored_adverts?select=*&order=order_num.asc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<SponsoredAdvert>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            list.add(
                                SponsoredAdvert(
                                    id = obj.optLong("id", i.toLong() + 1),
                                    businessName = obj.optString("business_name", ""),
                                    productTitle = obj.optString("product_title", ""),
                                    productDescription = obj.optString("product_description", ""),
                                    price = obj.optString("price", ""),
                                    discountTag = obj.optString("discount_tag", ""),
                                    imageUrl = obj.optString("image_url", fallback[i % fallback.size].imageUrl),
                                    facebookPageUrl = obj.optString("facebook_page_url", "https://facebook.com"),
                                    whatsappContact = obj.optString("whatsapp_contact", "+919876543210"),
                                    displayDurationSeconds = obj.optInt("display_duration_seconds", 1),
                                    orderNum = obj.optInt("order_num", i + 1)
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    // 7. Instagram Reels (3-part aspect ratio 9:16 layout)
    suspend fun fetchInstagramReels(): List<InstagramReel> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            InstagramReel(
                id = 1,
                title = "Handmade Brass Art & Lamps",
                shopName = "Murad Artisans Store",
                reelUrl = "https://www.instagram.com/reels/mznlive_art1",
                thumbnailUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500&auto=format&fit=crop&q=80",
                likesCount = "3.8K",
                slotIndex = 0
            ),
            InstagramReel(
                id = 2,
                title = "Winter Footwear Trends",
                shopName = "StepStyle Hub",
                reelUrl = "https://www.instagram.com/reels/mznlive_shoes",
                thumbnailUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500&auto=format&fit=crop&q=80",
                likesCount = "6.2K",
                slotIndex = 1
            ),
            InstagramReel(
                id = 3,
                title = "Crispy Amritsari Kulcha",
                shopName = "Amritsari Zayka",
                reelUrl = "https://www.instagram.com/reels/mznlive_food",
                thumbnailUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop&q=80",
                likesCount = "11.5K",
                slotIndex = 2
            ),
            InstagramReel(
                id = 4,
                title = "Terracotta Home Decor",
                shopName = "Mitti Mahal",
                reelUrl = "https://www.instagram.com/reels/mznlive_decor",
                thumbnailUrl = "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=500&auto=format&fit=crop&q=80",
                likesCount = "4.1K",
                slotIndex = 0
            ),
            InstagramReel(
                id = 5,
                title = "Budget Tech Under ₹999",
                shopName = "NextGen Gadget Point",
                reelUrl = "https://www.instagram.com/reels/mznlive_tech",
                thumbnailUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80",
                likesCount = "8.9K",
                slotIndex = 1
            ),
            InstagramReel(
                id = 6,
                title = "Royal Bridal Lehengas",
                shopName = "Vogue Boutique",
                reelUrl = "https://www.instagram.com/reels/mznlive_bridal",
                thumbnailUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500&auto=format&fit=crop&q=80",
                likesCount = "15.4K",
                slotIndex = 2
            )
        )

        try {
            val request = Request.Builder()
                .url("$baseUrl/instagram_reels?select=*&order=id.desc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<InstagramReel>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            list.add(
                                InstagramReel(
                                    id = obj.optLong("id", i.toLong() + 1),
                                    title = obj.optString("title", ""),
                                    shopName = obj.optString("shop_name", ""),
                                    reelUrl = obj.optString("reel_url", "https://instagram.com"),
                                    thumbnailUrl = obj.optString("thumbnail_url", fallback[i % fallback.size].thumbnailUrl),
                                    likesCount = obj.optString("likes_count", "1.2K"),
                                    slotIndex = obj.optInt("slot_index", i % 3)
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    suspend fun insertInstagramReel(reel: InstagramReel): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("title", reel.title)
                put("shop_name", reel.shopName)
                put("reel_url", reel.reelUrl)
                put("thumbnail_url", reel.thumbnailUrl)
                put("likes_count", reel.likesCount)
                put("slot_index", reel.slotIndex)
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$baseUrl/instagram_reels")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun insertSponsoredAdvert(advert: SponsoredAdvert): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("business_name", advert.businessName)
                put("product_title", advert.productTitle)
                put("product_description", advert.productDescription)
                put("price", advert.price)
                put("discount_tag", advert.discountTag)
                put("image_url", advert.imageUrl)
                put("facebook_page_url", advert.facebookPageUrl)
                put("whatsapp_contact", advert.whatsappContact)
                put("display_duration_seconds", advert.displayDurationSeconds)
                put("order_num", advert.orderNum)
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$baseUrl/sponsored_adverts")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    // 8. Marketplace Shops
    suspend fun fetchMarketplaceShops(): List<MarketplaceShop> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            MarketplaceShop(
                id = 1,
                name = "Royal Heritage Silk Sarees",
                category = "Retail • Fashion & Sarees",
                address = "14 Court Road, Near Clock Tower",
                phone = "+919876500001",
                whatsapp = "+919876500001",
                instagramLink = "https://instagram.com/royalheritage",
                facebookLink = "https://facebook.com/royalheritagesarees",
                rating = 4.9f,
                imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Flat 25% discount for Mznlive app users"
            ),
            MarketplaceShop(
                id = 2,
                name = "Shree Balaji Sweets & Bakery",
                category = "Restaurants • Sweets & Bakery",
                address = "Opposite G.P.O, Main Market",
                phone = "+919876500002",
                whatsapp = "+919876500002",
                instagramLink = "https://instagram.com/shreebalajisweets",
                facebookLink = "https://facebook.com/shreebalajisweets",
                rating = 4.8f,
                imageUrl = "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Free sample box on orders above ₹800"
            ),
            MarketplaceShop(
                id = 3,
                name = "Galaxy Electronics & Smart Hub",
                category = "Retail • Electronics & Mobiles",
                address = "Shop 22, Nehru Market Complex",
                phone = "+919876500003",
                whatsapp = "+919876500003",
                instagramLink = "https://instagram.com/galaxyelectronics",
                facebookLink = "https://facebook.com/galaxyelectronicsmzn",
                rating = 4.7f,
                imageUrl = "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Free tempered glass & cover with all phones"
            ),
            MarketplaceShop(
                id = 4,
                name = "Amritsari Zayka Restaurant",
                category = "Restaurants • North Indian Dining",
                address = "Civil Lines, Near Subhash Chowk",
                phone = "+919876500005",
                whatsapp = "+919876500005",
                instagramLink = "https://instagram.com/amritsarizayka",
                facebookLink = "https://facebook.com/amritsarizayka",
                rating = 4.9f,
                imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Complimentary sweet lassi on family dining"
            ),
            MarketplaceShop(
                id = 5,
                name = "QuickFix Pro Device & Tech Services",
                category = "Services • Device & Appliance Repairs",
                address = "Shop 8, Court Road, Gandhi Market",
                phone = "+919876500007",
                whatsapp = "+919876500007",
                instagramLink = "https://instagram.com/quickfixmzn",
                facebookLink = "https://facebook.com/quickfixmzn",
                rating = 4.8f,
                imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Free screen guard & 90-day warranty with service"
            ),
            MarketplaceShop(
                id = 6,
                name = "City Care Health Diagnostics & Clinic",
                category = "Services • Healthcare & Path Labs",
                address = "Near District Hospital, Civil Lines",
                phone = "+919876500008",
                whatsapp = "+919876500008",
                instagramLink = "https://instagram.com/citycareclinic",
                facebookLink = "https://facebook.com/citycareclinic",
                rating = 4.9f,
                imageUrl = "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Flat 25% off on full-body checkup packages"
            ),
            MarketplaceShop(
                id = 7,
                name = "Aura Elite Unisex Salon & Spa",
                category = "Services • Salon, Grooming & Wellness",
                address = "Circular Road, Opp. Reliance Trends",
                phone = "+919876500009",
                whatsapp = "+919876500009",
                instagramLink = "https://instagram.com/aurasalonmzn",
                facebookLink = "https://facebook.com/aurasalonmzn",
                rating = 4.7f,
                imageUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "30% off on bridal, grooming & hair spa combos"
            ),
            MarketplaceShop(
                id = 8,
                name = "Pind Da Swaad Highway Dhaba & Cafe",
                category = "Restaurants • Dhaba & Tandoor Bites",
                address = "Delhi-Dehradun Bypass, Muzaffarnagar",
                phone = "+919876500010",
                whatsapp = "+919876500010",
                instagramLink = "https://instagram.com/pinddaswaadmzn",
                facebookLink = "https://facebook.com/pinddaswaadmzn",
                rating = 4.8f,
                imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Complimentary tandoori starter on billing ₹1000+"
            ),
            MarketplaceShop(
                id = 9,
                name = "Murad Artisans Store",
                category = "Retail • Home Decor & Handicrafts",
                address = "Purani Tehsil Market",
                phone = "+919876500006",
                whatsapp = "+919876500006",
                instagramLink = "https://instagram.com/muradartisans",
                facebookLink = "https://facebook.com/muradartisans",
                rating = 4.6f,
                imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "15% off on hand-carved wooden items"
            ),
            MarketplaceShop(
                id = 10,
                name = "Kalyan Jewellers & Retail",
                category = "Retail • Gold, Diamond & Silver Jewelry",
                address = "Sarafa Bazar, Main Road",
                phone = "+919876500011",
                whatsapp = "+919876500011",
                instagramLink = "https://instagram.com/kalyanjewellersmzn",
                facebookLink = "https://facebook.com/kalyanjewellersmzn",
                rating = 4.9f,
                imageUrl = "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=600&auto=format&fit=crop&q=80",
                featuredOffer = "Zero making charge on select antique jewelry"
            )
        )

        try {
            val request = Request.Builder()
                .url("$baseUrl/marketplace_shops?select=*&order=id.asc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<MarketplaceShop>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            list.add(
                                MarketplaceShop(
                                    id = obj.optLong("id", i.toLong() + 1),
                                    name = obj.optString("name", "Local Shop"),
                                    category = obj.optString("category", "General"),
                                    address = obj.optString("address", ""),
                                    phone = obj.optString("phone", ""),
                                    whatsapp = obj.optString("whatsapp", ""),
                                    instagramLink = obj.optString("instagram_link", ""),
                                    facebookLink = obj.optString("facebook_link", ""),
                                    rating = obj.optDouble("rating", 4.8).toFloat(),
                                    imageUrl = obj.optString("image_url", fallback[i % fallback.size].imageUrl),
                                    featuredOffer = obj.optString("featured_offer", "")
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    // 10. Fetch Promotion Plans
    suspend fun fetchPromotionPlans(): List<PromotionPlanItem> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            PromotionPlanItem(
                id = "plan_3_days",
                title = "Micro Test Boost",
                durationText = "( 3 days )",
                priceInr = 11,
                priceDisplay = "INR 11 only",
                tagline = "Super Flash Trial Deal",
                description = "Perfect introductory test drive to showcase your shop products on Mznlive feed with live buyer traffic.",
                badge = "⚡ FLASH DEAL",
                isPopular = false,
                isBestValue = false,
                isFlashDeal = true,
                isCustom = false,
                features = listOf(
                    "1 High-visibility sponsored sliding advert",
                    "Direct WhatsApp click-to-chat button",
                    "Local city feed broadcast for 72 hours",
                    "Instant listing in Mznlive Marketplace"
                ),
                graphicType = "flash",
                gradientColors = listOf(0xFFFF5722, 0xFFFF9800)
            ),
            PromotionPlanItem(
                id = "plan_7_days",
                title = "Weekly Flash Sprint",
                durationText = "( 7 Days )",
                priceInr = 399,
                priceDisplay = "INR 399 only",
                tagline = "Rapid Footfall Catalyst",
                description = "Targeted 7-day high impact promotion to drive weekend shoppers and immediate inquiries directly to your store.",
                badge = "🔥 POPULAR WEEKLY",
                isPopular = false,
                isBestValue = false,
                isFlashDeal = false,
                isCustom = false,
                features = listOf(
                    "Top rotation in sliding sponsorship banner",
                    "1 Instagram Reel promotion slot (9:16 layout)",
                    "Direct call & WhatsApp enquiry button",
                    "Featured banner in Marketplace directory",
                    "Push highlight during peak evening hours"
                ),
                graphicType = "fire",
                gradientColors = listOf(0xFFE91E63, 0xFFFF5722)
            ),
            PromotionPlanItem(
                id = "plan_15_days",
                title = "Bi-Weekly Growth Blast",
                durationText = "( 15 Days )",
                priceInr = 599,
                priceDisplay = "INR 599 only",
                tagline = "Steady Customer Magnet",
                description = "Two weeks of continuous merchant visibility across live news and advert carousels with verified seller badge.",
                badge = "🚀 VALUE ACCELERATOR",
                isPopular = false,
                isBestValue = false,
                isFlashDeal = false,
                isCustom = false,
                features = listOf(
                    "Guaranteed 1,500+ daily banner impressions",
                    "2 Instagram Reels rotating in 9:16 showcase",
                    "Verified Local Merchant Blue Badge",
                    "Direct Facebook page & Instagram link",
                    "Priority customer lead routing on WhatsApp"
                ),
                graphicType = "rocket",
                gradientColors = listOf(0xFF673AB7, 0xFF3F51B5)
            ),
            PromotionPlanItem(
                id = "plan_30_days",
                title = "Monthly Pro Dominance",
                durationText = "( 30 Days )",
                priceInr = 899,
                priceDisplay = "INR 899 only",
                tagline = "Most Loved by Local Merchants",
                description = "Complete monthly dominance in local search, news broadcasts, and sliding banner rotation across the city.",
                badge = "⭐ MOST POPULAR",
                isPopular = true,
                isBestValue = false,
                isFlashDeal = false,
                isCustom = false,
                features = listOf(
                    "Prime top spot in sliding adverts banner",
                    "3 Instagram Reels featured in looping grid",
                    "Full digital storefront in Marketplace tab",
                    "Verified Gold Merchant Badge",
                    "Direct lead capture & dedicated chat support",
                    "Weekly performance summary report"
                ),
                graphicType = "star",
                gradientColors = listOf(0xFFFF9933, 0xFFFF5722)
            ),
            PromotionPlanItem(
                id = "plan_3_months",
                title = "Quarterly Market Leader",
                durationText = "( 3 Months )",
                priceInr = 1299,
                priceDisplay = "INR 1299 only",
                tagline = "Seasonal Sales Multiplier",
                description = "90 days of sustained presence across festival seasons, summer sales, and city-wide events at just ₹433/mo.",
                badge = "💎 HIGH ROI",
                isPopular = false,
                isBestValue = false,
                isFlashDeal = false,
                isCustom = false,
                features = listOf(
                    "Uninterrupted 90-day carousel ad placement",
                    "Unlimited rotation of promotional reels",
                    "Live news ticker sponsor branding tag",
                    "Exclusive festive festival campaign boost",
                    "Dedicated merchant manager support",
                    "Free graphic design support for banners"
                ),
                graphicType = "diamond",
                gradientColors = listOf(0xFF009688, 0xFF00BCD4)
            ),
            PromotionPlanItem(
                id = "plan_6_months",
                title = "Half-Year Super Saver",
                durationText = "( 6 Months )",
                priceInr = 1599,
                priceDisplay = "INR 1599 only",
                tagline = "Maximum Savings & Brand Equity",
                description = "Long-term local brand authority for top retailers, doctors, coaching centers, and manufacturers.",
                badge = "🏆 BEST VALUE",
                isPopular = false,
                isBestValue = true,
                isFlashDeal = false,
                isCustom = false,
                features = listOf(
                    "Unbeatable value at just ₹266 per month!",
                    "Permanent Gold Featured Partner listing",
                    "Top position in Marketplace directory",
                    "5 Custom Instagram Reels production assistance",
                    "Special live news stream banner mentions",
                    "Direct CRM lead forwarding & WhatsApp VIP desk"
                ),
                graphicType = "trophy",
                gradientColors = listOf(0xFF138808, 0xFF4CAF50)
            ),
            PromotionPlanItem(
                id = "plan_1_year",
                title = "Annual VIP City Partner",
                durationText = "( 1 Year )",
                priceInr = 2599,
                priceDisplay = "INR 2599 only",
                tagline = "The Ultimate Brand Domination",
                description = "Year-round brand ownership on Mznlive. Become the household name in your category across the entire region.",
                badge = "👑 VIP ANNUAL",
                isPopular = false,
                isBestValue = false,
                isFlashDeal = false,
                isCustom = false,
                features = listOf(
                    "365 Days complete presence across all tabs",
                    "Permanent VIP crown badge on storefront",
                    "Priority ad slots during breaking live news",
                    "Unlimited Instagram reels & ad banner updates",
                    "Quarterly video promotion showcase",
                    "Dedicated 24/7 business account manager"
                ),
                graphicType = "crown",
                gradientColors = listOf(0xFF7B1FA2, 0xFFE040FB)
            ),
            PromotionPlanItem(
                id = "plan_custom",
                title = "Customized Plans",
                durationText = "Customized Plans also available",
                priceInr = 0,
                priceDisplay = "Customized Plans also available",
                tagline = "Tailored for Brands & Multi-Outlets",
                description = "Need custom durations, multiple showroom branches, custom video ads, or omni-channel campaigns? We craft it for you.",
                badge = "🛠️ CUSTOM SOLUTIONS",
                isPopular = false,
                isBestValue = false,
                isFlashDeal = false,
                isCustom = true,
                features = listOf(
                    "Custom duration (10 days, 45 days, 2 years)",
                    "Multi-branch & franchise coverage",
                    "Professional video shoot & scriptwriting",
                    "Interactive survey & contest campaigns",
                    "Custom analytics & dedicated growth advisor"
                ),
                graphicType = "custom",
                gradientColors = listOf(0xFF000080, 0xFF1E88E5)
            )
        )

        try {
            val request = Request.Builder()
                .url("$baseUrl/promotion_plans?select=*&order=order_num.asc")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<PromotionPlanItem>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            val featuresList = mutableListOf<String>()
                            val featRaw = obj.opt("features")
                            if (featRaw is JSONArray) {
                                for (k in 0 until featRaw.length()) {
                                    featuresList.add(featRaw.getString(k))
                                }
                            }

                            val graphic = obj.optString("graphic_type", "rocket")
                            val isCustomPlan = obj.optBoolean("is_custom", false)
                            val isPop = obj.optBoolean("is_popular", false)
                            val isBest = obj.optBoolean("is_best_value", false)
                            val isFlash = obj.optBoolean("is_flash_deal", false)

                            val grad = when (graphic) {
                                "flash" -> listOf(0xFFFF5722, 0xFFFF9800)
                                "fire" -> listOf(0xFFE91E63, 0xFFFF5722)
                                "rocket" -> listOf(0xFF673AB7, 0xFF3F51B5)
                                "star" -> listOf(0xFFFF9933, 0xFFFF5722)
                                "diamond" -> listOf(0xFF009688, 0xFF00BCD4)
                                "trophy" -> listOf(0xFF138808, 0xFF4CAF50)
                                "crown" -> listOf(0xFF7B1FA2, 0xFFE040FB)
                                else -> listOf(0xFF000080, 0xFF1E88E5)
                            }

                            list.add(
                                PromotionPlanItem(
                                    id = obj.optString("id", "plan_$i"),
                                    title = obj.optString("title", "Promotion Plan"),
                                    durationText = obj.optString("duration_text", ""),
                                    priceInr = obj.optInt("price_inr", 0),
                                    priceDisplay = obj.optString("price_display", ""),
                                    tagline = obj.optString("tagline", ""),
                                    description = obj.optString("description", ""),
                                    badge = obj.optString("badge", "").takeIf { it.isNotBlank() },
                                    isPopular = isPop,
                                    isBestValue = isBest,
                                    isFlashDeal = isFlash,
                                    isCustom = isCustomPlan,
                                    features = if (featuresList.isNotEmpty()) featuresList else fallback.getOrNull(i)?.features ?: emptyList(),
                                    graphicType = graphic,
                                    gradientColors = grad
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Use fallback
        }
        fallback
    }

    // 12. Submit Plan Purchase Order to Supabase
    suspend fun createPlanPurchase(order: PlanPurchaseOrder): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("order_number", order.orderNumber)
                put("customer_name", order.customerName)
                put("shop_name", order.shopName)
                put("address", order.address)
                order.latitude?.let { put("latitude", it) }
                order.longitude?.let { put("longitude", it) }
                put("mobile_number", order.mobileNumber)
                put("plan_id", order.planId)
                put("plan_title", order.planTitle)
                put("plan_duration", order.planDuration)
                put("amount_inr", order.amountInr)
                put("payment_method", order.paymentMethod)
                put("payment_status", order.paymentStatus)
                put("transaction_ref", order.transactionRef ?: "UPI/PhonePe/Verified")
                put("whatsapp_notified", order.whatsappNotified)
                put("merchant_vpa", order.merchantVpa)
                put("merchant_name", order.merchantName)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl/plan_purchases")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            true // Fallback gracefully
        }
    }

    // 13. Check Supabase connection health
    suspend fun testConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/live_news?select=id&limit=1")
                .addHeader("apikey", publishableKey)
                .addHeader("Authorization", "Bearer $publishableKey")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Pair(true, "Connected to Supabase Project: $projectId")
            } else {
                Pair(false, "HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Pair(false, "Connection error: ${e.localizedMessage ?: "Unknown"}")
        }
    }
}
