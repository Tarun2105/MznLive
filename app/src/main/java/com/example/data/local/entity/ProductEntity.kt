package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing products catalog items listed by shop owners.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: Long = 0,
    val shopName: String,
    val title: String,
    val titleHi: String = "",
    val category: String,
    val description: String = "",
    val priceInr: Int,
    val originalMrpInr: Int,
    val imageUrl: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
