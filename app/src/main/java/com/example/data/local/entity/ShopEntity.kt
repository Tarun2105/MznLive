package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing local businesses and verified shops.
 */
@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerMobile: String,
    val ownerName: String,
    val shopName: String,
    val category: String,
    val address: String,
    val phone: String,
    val whatsapp: String,
    val description: String = "",
    val rating: Float = 4.8f,
    val imageUrl: String = "",
    val bannerUrl: String = "",
    val featuredOffer: String = "",
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
