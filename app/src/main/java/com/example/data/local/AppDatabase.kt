package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

/**
 * Main Room Database for local data persistence in MznLive.
 * Persists News Articles, Registered Users, Local Shops/Businesses,
 * Shop Products, and In-App & External Contact Chat Messages.
 */
@Database(
    entities = [
        NewsArticleEntity::class,
        UserEntity::class,
        ShopEntity::class,
        ProductEntity::class,
        ChatMessageEntity::class,
        PaymentTransactionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun newsArticleDao(): NewsArticleDao
    abstract fun userDao(): UserDao
    abstract fun shopDao(): ShopDao
    abstract fun productDao(): ProductDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mznlive_local_news.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
