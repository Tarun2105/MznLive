package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity for local storage and offline persistence of news articles.
 */
@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val headline: String,
    val headlineHi: String = "",
    val summary: String = "",
    val summaryHi: String = "",
    val thumbnailUrl: String = "",
    val category: String = "Local",
    val author: String = "Muzaffarnagar News Desk",
    val sourceName: String = "MznLive Bureau",
    val publishedAt: Long = System.currentTimeMillis(),
    val timeAgo: String = "Just now",
    val videoUrl: String? = null,
    val isBreaking: Boolean = false,
    val readCount: Int = 120,
    val isBookmarked: Boolean = false
)
