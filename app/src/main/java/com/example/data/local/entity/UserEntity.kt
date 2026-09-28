package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing registered users (both regular users and shop owners).
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "USER" or "SHOP_OWNER"
    val name: String,
    val businessName: String = "",
    val category: String = "",
    val address: String = "",
    val mobile: String,
    val whatsapp: String = "",
    val imageUri: String = "",
    val cashPoints: Int = 350,
    val createdAt: Long = System.currentTimeMillis()
)
