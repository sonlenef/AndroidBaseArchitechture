package dev.sonle.androidbasearchitecture.core.util

import dev.sonle.androidbasearchitecture.core.config.EnvironmentConfig

/**
 * Application constants
 */
object Constants {
    
    // Database
    const val DATABASE_NAME = "baseapp_database"
    const val DATABASE_VERSION = 1
    
    // API
    val API_BASE_URL = EnvironmentConfig.apiBaseUrl
    val API_TIMEOUT_SECONDS = EnvironmentConfig.apiTimeoutSeconds
    val MAX_ITEMS_PER_PAGE = EnvironmentConfig.maxItemsPerPage
    
    // Preferences
    val PREFERENCES_NAME = EnvironmentConfig.preferencesName
    
    // User preferences keys
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_USER_NAME = "user_name"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_FCM_TOKEN = "fcm_token"
    const val PREF_LAST_SYNC_TIME = "last_sync_time"
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 50
    const val MAX_EMAIL_LENGTH = 100
    const val MAX_PHONE_LENGTH = 20
    const val MAX_WEBSITE_LENGTH = 200
    
    // UI
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    
    // File upload
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024
    const val IMAGE_COMPRESSION_QUALITY = 80
    
    // Error messages
    const val ERROR_NETWORK = "Network error occurred"
    const val ERROR_UNKNOWN = "An unknown error occurred"
    const val ERROR_VALIDATION = "Validation error"
    const val ERROR_AUTHENTICATION = "Authentication failed"
    const val ERROR_PERMISSION = "Permission denied"
    
    // Success messages
    const val SUCCESS_LOGIN = "Login successful"
    const val SUCCESS_LOGOUT = "Logout successful"
    const val SUCCESS_SAVE = "Data saved successfully"
    const val SUCCESS_DELETE = "Data deleted successfully"
    const val SUCCESS_UPDATE = "Data updated successfully"
}
