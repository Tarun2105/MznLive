package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted payment transaction record in the Room database.
 * Created on behalf of "MznLive By TalntVibe" with system-generated
 * transaction details, exact date/time, receipt number, and user screenshot URI.
 */
@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionNumber: String, // System-generated, e.g. "TXN-TLV-20260927-482910"
    val receiptNumber: String,     // System-generated, e.g. "RCPT-TLV-84920"
    val issuedBy: String = "MznLive By TalntVibe",
    val dateTime: String,          // Human-readable formatted string: "27 Sep 2026, 03:45 PM"
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String,
    val shopName: String,
    val mobileNumber: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val planId: String,
    val planTitle: String,
    val planDuration: String,
    val amountInr: Double,
    val upiId: String = "9760077767@pnb",
    val paymentMethod: String = "UPI_QR_SCAN",
    val userUtrRef: String? = null,
    val screenshotUri: String? = null, // URI of uploaded payment confirmation screenshot
    val paymentStatus: String = "SUCCESS",
    val notes: String? = null
)
