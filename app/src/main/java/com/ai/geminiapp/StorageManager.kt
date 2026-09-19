package com.ai.geminiapp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Context.dataStore by preferencesDataStore(name = "remo_ai_prefs")

data class DailyQuotaInfo(
    val usedRequests: Int = 0,
    val maxDailyRequests: Int = 30,
    val remainingRequests: Int = 30,
    val progress: Float = 1.0f,
    val isQuotaExhausted: Boolean = false,
    val date: String = ""
)

class StorageManager(private val context: Context) {

    companion object {
        const val MAX_DAILY_GEMINI_QUOTA = 30

        val DEFAULT_SERVICES_API_KEY: String by lazy {
            try {
                val encoded = "QVEuQWI4Uk42TGtpZHczeVB6bTE2R2U3aVFCSF9zclNPTVlmVFd1b2ZQWkoyRklzMURRMkE="
                String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)).trim()
            } catch (e: Exception) {
                ""
            }
        }
        val API_KEY_KEY = stringPreferencesKey("user_api_key")
        val REQUEST_COUNT_KEY = intPreferencesKey("request_count")
        val LAST_DATE_KEY = stringPreferencesKey("last_date")
        val LOGGED_IN_EMAIL_KEY = stringPreferencesKey("logged_in_email")
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val SELECTED_MODEL_KEY = stringPreferencesKey("selected_model")

        // Dedicated DataStore Keys for Daily Gemini Usage & Chat Continuity
        val DAILY_GEMINI_USAGE_KEY = intPreferencesKey("daily_gemini_usage_count")
        val DAILY_GEMINI_DATE_KEY = stringPreferencesKey("daily_gemini_usage_date")
        val ACTIVE_CHAT_SESSION_ID_KEY = stringPreferencesKey("active_chat_session_id")
    }

    val dailyQuotaInfoFlow: Flow<DailyQuotaInfo> = context.dataStore.data
        .map { preferences ->
            val currentDate = getCurrentDate()
            val lastDate = preferences[DAILY_GEMINI_DATE_KEY] ?: ""
            val used = if (lastDate != currentDate) 0 else (preferences[DAILY_GEMINI_USAGE_KEY] ?: 0)
            val maxLimit = MAX_DAILY_GEMINI_QUOTA
            val remaining = (maxLimit - used).coerceAtLeast(0)
            val progressFraction = if (maxLimit > 0) remaining.toFloat() / maxLimit.toFloat() else 0f

            DailyQuotaInfo(
                usedRequests = used,
                maxDailyRequests = maxLimit,
                remainingRequests = remaining,
                progress = progressFraction.coerceIn(0f, 1f),
                isQuotaExhausted = remaining <= 0,
                date = currentDate
            )
        }

    val activeSessionIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[ACTIVE_CHAT_SESSION_ID_KEY] ?: ""
        }

    suspend fun saveActiveSessionId(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_CHAT_SESSION_ID_KEY] = sessionId
        }
    }

    suspend fun recordAndCheckGeminiUsage(): Boolean {
        var canProceed = true
        context.dataStore.edit { preferences ->
            val currentDate = getCurrentDate()
            val lastDate = preferences[DAILY_GEMINI_DATE_KEY] ?: ""
            var used = preferences[DAILY_GEMINI_USAGE_KEY] ?: 0

            if (lastDate != currentDate) {
                used = 0
                preferences[DAILY_GEMINI_DATE_KEY] = currentDate
            }

            val userKey = preferences[API_KEY_KEY] ?: ""
            val hasCustomKey = userKey.isNotBlank() && userKey != DEFAULT_SERVICES_API_KEY && userKey.startsWith("AIza")

            if (hasCustomKey) {
                // Users with custom key have unlimited personal quota, but we still count usage
                preferences[DAILY_GEMINI_USAGE_KEY] = used + 1
                canProceed = true
            } else {
                if (used < MAX_DAILY_GEMINI_QUOTA) {
                    preferences[DAILY_GEMINI_USAGE_KEY] = used + 1
                    canProceed = true
                } else {
                    canProceed = false
                }
            }
        }
        return canProceed
    }

    val userApiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            val custom = preferences[API_KEY_KEY] ?: ""
            if (custom.isNotBlank()) custom else DEFAULT_SERVICES_API_KEY
        }

    val loggedInEmailFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LOGGED_IN_EMAIL_KEY] ?: ""
        }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_KEY] ?: true // Default dark/gold theme matching Mohammed Alhazmi design
        }

    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "ar"
        }

    val selectedModelFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_MODEL_KEY] ?: "gemini-flash-latest"
        }

    val requestCountFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            val currentDate = getCurrentDate()
            val lastDate = preferences[LAST_DATE_KEY] ?: ""
            if (lastDate != currentDate) {
                0
            } else {
                preferences[REQUEST_COUNT_KEY] ?: 0
            }
        }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY_KEY] = apiKey.trim()
        }
    }

    suspend fun saveLoggedInEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGGED_IN_EMAIL_KEY] = email
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = lang
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL_KEY] = model
        }
    }

    suspend fun incrementAndCheckQuota(): Boolean {
        var canProceed = false
        context.dataStore.edit { preferences ->
            val currentDate = getCurrentDate()
            val lastDate = preferences[LAST_DATE_KEY] ?: ""
            
            var count = preferences[REQUEST_COUNT_KEY] ?: 0
            if (lastDate != currentDate) {
                count = 0
                preferences[LAST_DATE_KEY] = currentDate
            }

            val userKey = preferences[API_KEY_KEY] ?: ""
            if (userKey.isNotBlank()) {
                canProceed = true
            } else {
                if (count < 10) {
                    count++
                    preferences[REQUEST_COUNT_KEY] = count
                    canProceed = true
                } else {
                    canProceed = false
                }
            }
        }
        return canProceed
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
}
