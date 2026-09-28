package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.NewsArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local News Articles table.
 */
@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM news_articles ORDER BY isBreaking DESC, publishedAt DESC")
    fun getAllArticles(): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM news_articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM news_articles WHERE id = :id LIMIT 1")
    fun getArticleById(id: Long): Flow<NewsArticleEntity?>

    @Query("SELECT * FROM news_articles WHERE headline LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR headlineHi LIKE '%' || :query || '%' ORDER BY publishedAt DESC")
    fun searchArticles(query: String): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<NewsArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: NewsArticleEntity): Long

    @Update
    suspend fun updateArticle(article: NewsArticleEntity)

    @Query("UPDATE news_articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, isBookmarked: Boolean)

    @Query("DELETE FROM news_articles WHERE id = :id")
    suspend fun deleteArticleById(id: Long)

    @Query("DELETE FROM news_articles")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM news_articles")
    suspend fun getCount(): Int
}
