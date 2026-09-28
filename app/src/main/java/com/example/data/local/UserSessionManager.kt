package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mznlive_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, true))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "hi") ?: "hi") // Default to Hindi or English
    val language: StateFlow<String> = _language.asStateFlow()

    private val _verifiedPhone = MutableStateFlow(prefs.getString(KEY_PHONE, "Guest User") ?: "Guest User")
    val verifiedPhone: StateFlow<String> = _verifiedPhone.asStateFlow()

    private val _locationGranted = MutableStateFlow(prefs.getBoolean(KEY_LOCATION_GRANTED, false))
    val locationGranted: StateFlow<Boolean> = _locationGranted.asStateFlow()

    private val _phoneGranted = MutableStateFlow(prefs.getBoolean(KEY_PHONE_GRANTED, false))
    val phoneGranted: StateFlow<Boolean> = _phoneGranted.asStateFlow()

    private val _notificationsGranted = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_GRANTED, false))
    val notificationsGranted: StateFlow<Boolean> = _notificationsGranted.asStateFlow()

    private val _cameraGranted = MutableStateFlow(prefs.getBoolean(KEY_CAMERA_GRANTED, false))
    val cameraGranted: StateFlow<Boolean> = _cameraGranted.asStateFlow()

    private val _permissionsSetupCompleted = MutableStateFlow(prefs.getBoolean(KEY_PERMISSIONS_SETUP_COMPLETED, false))
    val permissionsSetupCompleted: StateFlow<Boolean> = _permissionsSetupCompleted.asStateFlow()

    // Sign Up & Profile Registration State
    private val _isRegistered = MutableStateFlow(prefs.getBoolean(KEY_IS_REGISTERED, false))
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _userRole = MutableStateFlow(prefs.getString(KEY_USER_ROLE, "USER") ?: "USER")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _businessName = MutableStateFlow(prefs.getString(KEY_BUSINESS_NAME, "") ?: "")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _businessCategory = MutableStateFlow(prefs.getString(KEY_BUSINESS_CATEGORY, "") ?: "")
    val businessCategory: StateFlow<String> = _businessCategory.asStateFlow()

    private val _userAddress = MutableStateFlow(prefs.getString(KEY_USER_ADDRESS, "") ?: "")
    val userAddress: StateFlow<String> = _userAddress.asStateFlow()

    private val _whatsappNumber = MutableStateFlow(prefs.getString(KEY_WHATSAPP_NUMBER, "") ?: "")
    val whatsappNumber: StateFlow<String> = _whatsappNumber.asStateFlow()

    private val _profileImageUri = MutableStateFlow(prefs.getString(KEY_PROFILE_IMAGE_URI, "") ?: "")
    val profileImageUri: StateFlow<String> = _profileImageUri.asStateFlow()

    private val _cashPoints = MutableStateFlow(prefs.getInt(KEY_CASH_POINTS, 350))
    val cashPoints: StateFlow<Int> = _cashPoints.asStateFlow()

    fun saveRegistration(
        role: String,
        name: String,
        businessName: String,
        category: String,
        address: String,
        mobile: String,
        whatsapp: String,
        imageUri: String = ""
    ) {
        prefs.edit {
            putBoolean(KEY_IS_REGISTERED, true)
            putString(KEY_USER_ROLE, role)
            putString(KEY_USER_NAME, name)
            putString(KEY_BUSINESS_NAME, businessName)
            putString(KEY_BUSINESS_CATEGORY, category)
            putString(KEY_USER_ADDRESS, address)
            putString(KEY_PHONE, mobile)
            putString(KEY_WHATSAPP_NUMBER, whatsapp)
            putString(KEY_PROFILE_IMAGE_URI, imageUri)
            putBoolean(KEY_IS_LOGGED_IN, true)
        }
        _isRegistered.value = true
        _userRole.value = role
        _userName.value = name
        _businessName.value = businessName
        _businessCategory.value = category
        _userAddress.value = address
        _verifiedPhone.value = mobile
        _whatsappNumber.value = whatsapp
        _profileImageUri.value = imageUri
        _isLoggedIn.value = true
    }

    fun setProfileImageUri(uri: String) {
        prefs.edit { putString(KEY_PROFILE_IMAGE_URI, uri) }
        _profileImageUri.value = uri
    }

    fun updateProfile(
        name: String,
        mobile: String,
        address: String,
        businessName: String = "",
        category: String = "",
        whatsapp: String = "",
        imageUri: String? = null
    ) {
        prefs.edit {
            putString(KEY_USER_NAME, name)
            putString(KEY_PHONE, mobile)
            putString(KEY_USER_ADDRESS, address)
            if (businessName.isNotBlank()) putString(KEY_BUSINESS_NAME, businessName)
            if (category.isNotBlank()) putString(KEY_BUSINESS_CATEGORY, category)
            if (whatsapp.isNotBlank()) putString(KEY_WHATSAPP_NUMBER, whatsapp)
            if (imageUri != null) putString(KEY_PROFILE_IMAGE_URI, imageUri)
        }
        _userName.value = name
        _verifiedPhone.value = mobile
        _userAddress.value = address
        if (businessName.isNotBlank()) _businessName.value = businessName
        if (category.isNotBlank()) _businessCategory.value = category
        if (whatsapp.isNotBlank()) _whatsappNumber.value = whatsapp
        if (imageUri != null) _profileImageUri.value = imageUri
    }

    fun addCashPoints(points: Int) {
        val updated = _cashPoints.value + points
        prefs.edit { putInt(KEY_CASH_POINTS, updated) }
        _cashPoints.value = updated
    }

    fun deductCashPoints(points: Int): Boolean {
        if (_cashPoints.value >= points) {
            val updated = _cashPoints.value - points
            prefs.edit { putInt(KEY_CASH_POINTS, updated) }
            _cashPoints.value = updated
            return true
        }
        return false
    }

    fun setLoggedIn(phone: String) {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_PHONE, phone)
        }
        _isLoggedIn.value = true
        _verifiedPhone.value = phone
    }

    fun setLanguage(lang: String) {
        prefs.edit { putString(KEY_LANGUAGE, lang) }
        _language.value = lang
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        prefs.edit { putBoolean(KEY_LOCATION_GRANTED, granted) }
        _locationGranted.value = granted
    }

    fun setPhonePermissionGranted(granted: Boolean) {
        prefs.edit { putBoolean(KEY_PHONE_GRANTED, granted) }
        _phoneGranted.value = granted
    }

    fun setNotificationsPermissionGranted(granted: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_GRANTED, granted) }
        _notificationsGranted.value = granted
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        prefs.edit { putBoolean(KEY_CAMERA_GRANTED, granted) }
        _cameraGranted.value = granted
    }

    fun setPermissionsSetupCompleted(completed: Boolean) {
        prefs.edit { putBoolean(KEY_PERMISSIONS_SETUP_COMPLETED, completed) }
        _permissionsSetupCompleted.value = completed
    }

    fun resetSession() {
        prefs.edit { clear() }
        _isLoggedIn.value = false
        _verifiedPhone.value = ""
        _locationGranted.value = false
        _phoneGranted.value = false
        _notificationsGranted.value = false
        _cameraGranted.value = false
        _permissionsSetupCompleted.value = false
        _isRegistered.value = false
        _userName.value = ""
        _businessName.value = ""
        _businessCategory.value = ""
        _userAddress.value = ""
        _whatsappNumber.value = ""
        _profileImageUri.value = ""
        _cashPoints.value = 350
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_LOCATION_GRANTED = "key_location_granted"
        private const val KEY_PHONE_GRANTED = "key_phone_granted"
        private const val KEY_NOTIFICATIONS_GRANTED = "key_notifications_granted"
        private const val KEY_CAMERA_GRANTED = "key_camera_granted"
        private const val KEY_PERMISSIONS_SETUP_COMPLETED = "key_permissions_setup_completed"
        private const val KEY_IS_REGISTERED = "key_is_registered"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_BUSINESS_NAME = "key_business_name"
        private const val KEY_BUSINESS_CATEGORY = "key_business_category"
        private const val KEY_USER_ADDRESS = "key_user_address"
        private const val KEY_WHATSAPP_NUMBER = "key_whatsapp_number"
        private const val KEY_PROFILE_IMAGE_URI = "key_profile_image_uri"
        private const val KEY_CASH_POINTS = "key_cash_points"
    }
}
