package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity for in-app messenger chat logs between users and shop owners,
 * as well as SMS and WhatsApp outbound contact logs.
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: Long = 0,
    val shopName: String,
    val customerPhone: String = "",
    val sender: String, // "user" or "shop"
    val message: String,
    val channel: String = "APP", // "APP", "SMS", "WHATSAPP"
    val timestamp: Long = System.currentTimeMillis()
)
