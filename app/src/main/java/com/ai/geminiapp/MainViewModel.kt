package com.ai.geminiapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class AppScreen {
    object Welcome : AppScreen()
    object Login : AppScreen()
    object SignUp : AppScreen()
    object Dashboard : AppScreen()
    object Chat : AppScreen()
    object About : AppScreen()
    object Templates : AppScreen()
    object Favorites : AppScreen()
    object Stats : AppScreen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Welcome)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val favorites: StateFlow<List<FavoriteEntity>> = _favorites.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _showDeveloperBanner = MutableStateFlow(false)
    val showDeveloperBanner: StateFlow<Boolean> = _showDeveloperBanner.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("ar")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "مرحباً يا صديقي! 👋 أنا **ريمو - صديقك ومساعدك الذكي** ✨\nتم ابتكاري وبرمجتي وتدريبي بالكامل بواسطة **المطور محمد الحزمي**.\nأنا هنا لأجيبك على كل أسئلتك، أبحث لك في الويب مع ذكر المصادر، وأشاركك أفكاراً ذكية ومفاجآت حصرية بدون أي حدود. كيف أستطيع إسعادك ومساعدتك اليوم؟ 🚀",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isWebSearchEnabled = MutableStateFlow(false)
    val isWebSearchEnabled: StateFlow<Boolean> = _isWebSearchEnabled.asStateFlow()

    private val _currentSurprise = MutableStateFlow<RemoSurprise?>(null)
    val currentSurprise: StateFlow<RemoSurprise?> = _currentSurprise.asStateFlow()

    private val _showSurpriseDialog = MutableStateFlow(false)
    val showSurpriseDialog: StateFlow<Boolean> = _showSurpriseDialog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _requestCount = MutableStateFlow(0)
    val requestCount: StateFlow<Int> = _requestCount.asStateFlow()

    private val _userApiKey = MutableStateFlow("")
    val userApiKey: StateFlow<String> = _userApiKey.asStateFlow()

    private val _showQuotaDialog = MutableStateFlow(false)
    val showQuotaDialog: StateFlow<Boolean> = _showQuotaDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    init {
        viewModelScope.launch {
            storageManager.requestCountFlow.collect { count ->
                _requestCount.value = count
            }
        }
        viewModelScope.launch {
            storageManager.userApiKeyFlow.collect { key ->
                _userApiKey.value = key
            }
        }
        viewModelScope.launch {
            storageManager.isDarkModeFlow.collect { dark ->
                _isDarkMode.value = dark
            }
        }
        viewModelScope.launch {
            storageManager.languageFlow.collect { lang ->
                _language.value = lang
            }
        }
        viewModelScope.launch {
            favoriteDao.getAllFavorites().collect { list ->
                _favorites.value = list
            }
        }
        viewModelScope.launch {
            storageManager.loggedInEmailFlow.collect { email ->
                if (email.isNotBlank()) {
                    val user = userDao.getUserByEmail(email)
                    if (user != null) {
                        _currentUser.value = user
                        _isLoggedIn.value = true
                        if (_currentScreen.value is AppScreen.Welcome) {
                            _currentScreen.value = AppScreen.Dashboard
                            triggerDeveloperBanner()
                        }
                    }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen is AppScreen.Dashboard) {
            triggerDeveloperBanner()
        }
    }

    private fun triggerDeveloperBanner() {
        viewModelScope.launch {
            _showDeveloperBanner.value = true
            kotlinx.coroutines.delay(4000L)
            _showDeveloperBanner.value = false
        }
    }

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch {
            storageManager.setDarkMode(dark)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            storageManager.setLanguage(lang)
        }
    }

    fun signUp(name: String, email: String, pass: String, onsuccess: () -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _authError.value = "يرجى تعبئة كافة الحقول"
                return@launch
            }
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                _authError.value = "البريد الإلكتروني مسجل مسبقاً"
                return@launch
            }
            val newUser = UserEntity(name = name, email = email, password = pass)
            userDao.insertUser(newUser)
            storageManager.saveLoggedInEmail(email)
            _currentUser.value = newUser
            _isLoggedIn.value = true
            _authError.value = null
            navigateTo(AppScreen.Dashboard)
            onsuccess()
        }
    }

    fun login(email: String, pass: String, onsuccess: () -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                _authError.value = "يرجى إدخال البريد وكلمة المرور"
                return@launch
            }
            val user = userDao.login(email, pass)
            if (user == null) {
                _authError.value = "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                return@launch
            }
            storageManager.saveLoggedInEmail(email)
            _currentUser.value = user
            _isLoggedIn.value = true
            _authError.value = null
            navigateTo(AppScreen.Dashboard)
            onsuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            storageManager.saveLoggedInEmail("")
            _currentUser.value = null
            _isLoggedIn.value = false
            navigateTo(AppScreen.Welcome)
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun setShowQuotaDialog(show: Boolean) {
        _showQuotaDialog.value = show
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun saveUserApiKey(key: String) {
        viewModelScope.launch {
            storageManager.saveApiKey(key)
        }
    }

    fun toggleWebSearch() {
        _isWebSearchEnabled.value = !_isWebSearchEnabled.value
    }

    fun openSurpriseDialog() {
        _currentSurprise.value = RemoBrain.getRandomSurprise()
        _showSurpriseDialog.value = true
    }

    fun dismissSurpriseDialog() {
        _showSurpriseDialog.value = false
    }

    fun nextSurprise() {
        _currentSurprise.value = RemoBrain.getRandomSurprise()
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                text = "مرحباً مجدداً يا صديقي! تم بدء محادثة جديدة مع ريمو صديقك الذكي ✨ أنا جاهز لأي استفسار أو مهمة جديدة.",
                isUser = false
            )
        )
    }

    fun sendMessage(
        prompt: String,
        bitmap: android.graphics.Bitmap? = null,
        isImageGeneration: Boolean = false,
        forceWebSearch: Boolean = false
    ) {
        if (prompt.isBlank() && bitmap == null) return

        val effectivePrompt = if (prompt.isBlank()) "ماذا ترى في هذه الصورة؟" else prompt
        val doWebSearch = forceWebSearch || _isWebSearchEnabled.value

        viewModelScope.launch {
            val userKey = _userApiKey.value
            storageManager.incrementAndCheckQuota()

            val displayPrompt = if (bitmap != null && prompt.isBlank()) "[صورة مرفقة] ماذا ترى في هذه الصورة؟" else prompt
            val userMsg = ChatMessage(text = displayPrompt, isUser = true)
            _messages.value = _messages.value + userMsg
            _isLoading.value = true

            try {
                val apiKeyToUse = if (userKey.isNotBlank()) userKey else (try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" })

                if (apiKeyToUse.isBlank() || apiKeyToUse == "MY_GEMINI_API_KEY") {
                    // Smart Offline Intelligence fallback
                    val smartAnswer = RemoBrain.getSmartOfflineResponse(effectivePrompt)
                    _messages.value = _messages.value + ChatMessage(
                        text = smartAnswer,
                        isUser = false
                    )
                    _isLoading.value = false
                    return@launch
                }

                val modelName = if (isImageGeneration) "gemini-2.5-flash-image" else "gemini-2.5-flash"

                val generativeModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKeyToUse,
                    systemInstruction = com.google.ai.client.generativeai.type.content {
                        text(RemoBrain.SYSTEM_PROMPT)
                    }
                )

                val promptForModel = if (doWebSearch) {
                    "يرجى البحث الموثق في الويب بدقة حول هذا الاستفسار، وتقديم إجابة غنية بالمعلومات، مع ذكر المصدر والرابط في النهاية تحت قسم '🔍 المصدر والتوثيق من الويب':\n$effectivePrompt"
                } else {
                    effectivePrompt
                }

                val response = if (bitmap != null) {
                    generativeModel.generateContent(
                        com.google.ai.client.generativeai.type.content {
                            image(bitmap)
                            text(promptForModel)
                        }
                    )
                } else {
                    generativeModel.generateContent(promptForModel)
                }

                val responseText = response.text ?: RemoBrain.getSmartOfflineResponse(effectivePrompt)

                _messages.value = _messages.value + ChatMessage(
                    text = responseText,
                    isUser = false
                )
            } catch (e: Exception) {
                // If network/quota error occurs, use Remo's smart local intelligence seamlessly
                val fallbackAnswer = RemoBrain.getSmartOfflineResponse(effectivePrompt)
                _messages.value = _messages.value + ChatMessage(
                    text = fallbackAnswer,
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFavorite(text: String) {
        viewModelScope.launch {
            favoriteDao.insertFavorite(FavoriteEntity(text = text))
        }
    }

    fun deleteFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            favoriteDao.deleteFavorite(favorite)
        }
    }

    fun sendTemplateMessage(prompt: String) {
        navigateTo(AppScreen.Chat)
        sendMessage(prompt)
    }
}
