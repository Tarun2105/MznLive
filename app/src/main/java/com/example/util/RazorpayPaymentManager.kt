package com.example.util

import android.app.Activity
import android.widget.Toast
import com.example.BuildConfig
import com.razorpay.Checkout
import org.json.JSONObject

object RazorpayPaymentManager {

    /**
     * Resolves the Razorpay Key ID. Uses BuildConfig.RAZORPAY_KEY_ID if set and valid,
     * otherwise falls back to a sandbox test key for immediate interactive operation.
     */
    fun getKeyId(): String {
        val configured = BuildConfig.RAZORPAY_KEY_ID
        return if (!configured.isNullOrBlank() && configured != "rzp_test_placeholder") {
            configured
        } else {
            "rzp_test_1DP5mmOlF5G5ag" // Valid Razorpay test key format
        }
    }

    /**
     * Opens the official Razorpay Checkout Service Portal in the host activity.
     */
    fun openCheckout(
        activity: Activity,
        amountInr: Int,
        orderTitle: String,
        description: String,
        customerPhone: String = "",
        customerEmail: String = "",
        notes: Map<String, String> = emptyMap()
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(getKeyId())

            val options = JSONObject()
            options.put("name", "Mznlive Marketplace")
            options.put("description", "$orderTitle • $description")
            // Razorpay uses amount in Paise (1 INR = 100 paise)
            options.put("amount", (amountInr * 100).toLong())
            options.put("currency", "INR")

            val prefill = JSONObject()
            if (customerPhone.isNotBlank()) {
                val cleanedPhone = customerPhone.replace(" ", "").replace("+91", "").trim()
                prefill.put("contact", cleanedPhone)
            }
            if (customerEmail.isNotBlank()) {
                prefill.put("email", customerEmail)
            } else {
                prefill.put("email", "customer@mznlive.com")
            }
            options.put("prefill", prefill)

            val theme = JSONObject()
            theme.put("color", "#FF9933")
            theme.put("backdrop_color", "#0C2340")
            options.put("theme", theme)

            val modal = JSONObject()
            modal.put("animation", true)
            modal.put("backdropclose", true)
            options.put("modal", modal)

            val notesObj = JSONObject()
            notesObj.put("merchant", "Mznlive Muzaffarnagar")
            notesObj.put("platform", "Android Compose")
            notes.forEach { (k, v) -> notesObj.put(k, v) }
            options.put("notes", notesObj)

            checkout.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error launching Razorpay: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
