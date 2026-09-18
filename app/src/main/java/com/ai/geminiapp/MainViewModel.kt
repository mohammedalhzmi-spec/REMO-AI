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
import kotlinx.coroutines.delay

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<WebSource> = emptyList()
)

data class ServiceToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "code", "image", "audio"
    val iconEmoji: String,
    val defaultPrompt: String,
    val inputHint: String,
    val sampleInputs: List<String> = emptyList()
)

sealed class AppScreen {
    object Welcome : AppScreen()
    object Login : AppScreen()
    object SignUp : AppScreen()
    object Dashboard : AppScreen()
    object Chat : AppScreen()
    object GeneralChat : AppScreen()
    object ImageChat : AppScreen()
    object CodeChat : AppScreen()
    object AudioChat : AppScreen()
    object About : AppScreen()
    object Templates : AppScreen()
    object Favorites : AppScreen()
    object Stats : AppScreen()
    object Platform : AppScreen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
    private val chatSessionDao = AppDatabase.getDatabase(application).chatSessionDao()

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

    // DataStore-based Daily Quota Tracking
    private val _dailyQuotaInfo = MutableStateFlow(DailyQuotaInfo())
    val dailyQuotaInfo: StateFlow<DailyQuotaInfo> = _dailyQuotaInfo.asStateFlow()

    // Room & DataStore Chat Sessions & Continuity
    private val _chatSessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    val chatSessions: StateFlow<List<ChatSessionEntity>> = _chatSessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow("")
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

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

    private val _selectedModel = MutableStateFlow("gemini-flash-latest")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
        viewModelScope.launch {
            storageManager.saveSelectedModel(model)
        }
    }

    private val _showQuotaDialog = MutableStateFlow(false)
    val showQuotaDialog: StateFlow<Boolean> = _showQuotaDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _activeToolDialog = MutableStateFlow<ServiceToolItem?>(null)
    val activeToolDialog: StateFlow<ServiceToolItem?> = _activeToolDialog.asStateFlow()

    // App Update Notifications and Dialog State
    private val _updateInfo = MutableStateFlow(AppUpdateInfo())
    val updateInfo: StateFlow<AppUpdateInfo> = _updateInfo.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _showUpdateBanner = MutableStateFlow(true)
    val showUpdateBanner: StateFlow<Boolean> = _showUpdateBanner.asStateFlow()

    fun openUpdateDialog() {
        _showUpdateDialog.value = true
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun dismissUpdateBanner() {
        _showUpdateBanner.value = false
    }

    fun checkForUpdates(manual: Boolean = false, context: android.content.Context? = null) {
        if (manual) {
            _showUpdateDialog.value = true
            context?.let {
                android.widget.Toast.makeText(it, "يوجد تحديث جديد متاح: v${_updateInfo.value.latestVersion} 🚀", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            _showUpdateBanner.value = true
        }
    }

    fun openToolDialog(tool: ServiceToolItem) {
        _activeToolDialog.value = tool
    }

    fun closeToolDialog() {
        _activeToolDialog.value = null
    }

    fun launchTool(tool: ServiceToolItem, customInput: String = "") {
        _activeToolDialog.value = null
        navigateTo(AppScreen.Chat)
        val promptToSend = if (customInput.isNotBlank()) {
            "${tool.defaultPrompt}\n\nالمدخل / المطلوب:\n$customInput"
        } else {
            tool.defaultPrompt
        }
        val isImg = tool.category == "image"
        sendMessage(prompt = promptToSend, isImageGeneration = isImg)
    }

    init {
        viewModelScope.launch {
            storageManager.requestCountFlow.collect { count ->
                _requestCount.value = count
            }
        }
        viewModelScope.launch {
            storageManager.dailyQuotaInfoFlow.collect { quota ->
                _dailyQuotaInfo.value = quota
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
            storageManager.selectedModelFlow.collect { model ->
                _selectedModel.value = model
            }
        }
        viewModelScope.launch {
            favoriteDao.getAllFavorites().collect { list ->
                _favorites.value = list
            }
        }
        viewModelScope.launch {
            chatSessionDao.getAllSessions().collect { sessions ->
                _chatSessions.value = sessions
                if (sessions.isEmpty()) {
                    initializeDefaultSession()
                }
            }
        }
        viewModelScope.launch {
            storageManager.activeSessionIdFlow.collect { activeId ->
                if (activeId.isNotBlank()) {
                    _activeSessionId.value = activeId
                    loadSessionMessages(activeId)
                }
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
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200L)
            UpdateNotificationHelper.showSystemUpdateNotification(application, _updateInfo.value)
        }
    }

    private fun initializeDefaultSession() {
        viewModelScope.launch {
            val defaultId = java.util.UUID.randomUUID().toString()
            val defaultSession = ChatSessionEntity(
                sessionId = defaultId,
                title = "محادثة ترحيبية مع ريمو الذكي ✨",
                createdAt = System.currentTimeMillis(),
                lastUpdatedAt = System.currentTimeMillis()
            )
            chatSessionDao.insertSession(defaultSession)
            storageManager.saveActiveSessionId(defaultId)
            _activeSessionId.value = defaultId

            val initialText = "مرحباً يا صديقي! 👋 أنا **ريمو - صديقك ومساعدك الذكي** ✨\nتم ابتكاري وبرمجتي وتدريبي بالكامل بواسطة **المطور محمد الحزمي**.\nأنا هنا لأجيبك على كل أسئلتك، أبحث لك في الويب مع ذكر المصادر، وأشاركك أفكاراً ذكية ومفاجآت حصرية بدون أي حدود. كيف أستطيع إسعادك ومساعدتك اليوم؟ 🚀"
            val initialMsg = ChatMessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                sessionId = defaultId,
                text = initialText,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            chatSessionDao.insertMessage(initialMsg)
            _messages.value = listOf(
                ChatMessage(id = initialMsg.id, text = initialMsg.text, isUser = false, timestamp = initialMsg.timestamp)
            )
        }
    }

    private fun loadSessionMessages(sessionId: String) {
        viewModelScope.launch {
            val entities = chatSessionDao.getMessagesForSession(sessionId)
            if (entities.isNotEmpty()) {
                _messages.value = entities.map {
                    ChatMessage(id = it.id, text = it.text, isUser = it.isUser, timestamp = it.timestamp)
                }
            } else {
                _messages.value = listOf(
                    ChatMessage(
                        text = "مرحباً بك مجدداً يا صديقي في هذه المحادثة! 💬 أنا ريمو جاهز للمتابعة معك.",
                        isUser = false
                    )
                )
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val newId = java.util.UUID.randomUUID().toString()
            val newSession = ChatSessionEntity(
                sessionId = newId,
                title = "محادثة جديدة 💬",
                createdAt = System.currentTimeMillis(),
                lastUpdatedAt = System.currentTimeMillis()
            )
            chatSessionDao.insertSession(newSession)
            storageManager.saveActiveSessionId(newId)
            _activeSessionId.value = newId

            val welcomeText = "أهلاً بك في جلسة محادثة جديدة! 🚀 أنا ريمو معك دائماً، تفضل بأي سؤال أو فكرة تريد مناقشتها."
            val welcomeMsg = ChatMessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                sessionId = newId,
                text = welcomeText,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            chatSessionDao.insertMessage(welcomeMsg)
            _messages.value = listOf(
                ChatMessage(id = welcomeMsg.id, text = welcomeMsg.text, isUser = false, timestamp = welcomeMsg.timestamp)
            )
        }
    }

    fun selectSession(sessionId: String) {
        viewModelScope.launch {
            storageManager.saveActiveSessionId(sessionId)
            _activeSessionId.value = sessionId
            loadSessionMessages(sessionId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatSessionDao.deleteMessagesForSession(sessionId)
            chatSessionDao.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                val remaining = chatSessionDao.getAllSessions().first()
                if (remaining.isNotEmpty()) {
                    selectSession(remaining.first().sessionId)
                } else {
                    createNewSession()
                }
            }
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            chatSessionDao.deleteAllMessages()
            chatSessionDao.deleteAllSessions()
            createNewSession()
        }
    }

    private var _previousScreen: AppScreen = AppScreen.Welcome

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != AppScreen.Platform && screen == AppScreen.Platform) {
            _previousScreen = _currentScreen.value
        }
        _currentScreen.value = screen
        if (screen is AppScreen.Dashboard) {
            triggerDeveloperBanner()
        }
    }

    fun navigateBackFromPlatform() {
        _currentScreen.value = _previousScreen
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
        createNewSession()
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
            // Ensure an active session ID exists
            var currentSessionId = _activeSessionId.value
            if (currentSessionId.isBlank()) {
                currentSessionId = java.util.UUID.randomUUID().toString()
                val newSess = ChatSessionEntity(
                    sessionId = currentSessionId,
                    title = if (effectivePrompt.length > 35) effectivePrompt.take(35) + "..." else effectivePrompt,
                    createdAt = System.currentTimeMillis(),
                    lastUpdatedAt = System.currentTimeMillis()
                )
                chatSessionDao.insertSession(newSess)
                storageManager.saveActiveSessionId(currentSessionId)
                _activeSessionId.value = currentSessionId
            } else {
                // Update session title if default
                val currSession = chatSessionDao.getSessionById(currentSessionId)
                if (currSession != null && (currSession.title.startsWith("محادثة جديدة") || currSession.title.startsWith("محادثة ترحيبية"))) {
                    val cleanTitle = if (effectivePrompt.length > 35) effectivePrompt.take(35) + "..." else effectivePrompt
                    chatSessionDao.updateSession(currSession.copy(title = cleanTitle, lastUpdatedAt = System.currentTimeMillis()))
                }
            }

            // 🛡️ فحص الأمان الأخلاقي: حجب وحذف الألفاظ المسيئة والإباحية تلقائياً وعدم الرد عليها
            if (ContentSafetyFilter.containsExplicitOrAbusiveContent(effectivePrompt)) {
                val sanitizedDisplay = ContentSafetyFilter.sanitizeAndRedact(effectivePrompt)
                val displayMsg = if (bitmap != null) "[صورة مرفقة] $sanitizedDisplay" else sanitizedDisplay
                val userMsg = ChatMessage(text = displayMsg, isUser = true)
                _messages.value = _messages.value + userMsg

                chatSessionDao.insertMessage(
                    ChatMessageEntity(
                        id = userMsg.id,
                        sessionId = currentSessionId,
                        text = userMsg.text,
                        isUser = true,
                        timestamp = userMsg.timestamp
                    )
                )

                val rejectionMsg = ChatMessage(
                    text = ContentSafetyFilter.getRejectionResponse(),
                    isUser = false
                )
                _messages.value = _messages.value + rejectionMsg

                chatSessionDao.insertMessage(
                    ChatMessageEntity(
                        id = rejectionMsg.id,
                        sessionId = currentSessionId,
                        text = rejectionMsg.text,
                        isUser = false,
                        timestamp = rejectionMsg.timestamp
                    )
                )
                _isLoading.value = false
                return@launch
            }

            val userKey = _userApiKey.value
            val quotaAllowed = storageManager.recordAndCheckGeminiUsage()
            storageManager.incrementAndCheckQuota()

            val displayPrompt = if (bitmap != null && prompt.isBlank()) "[صورة مرفقة] ماذا ترى في هذه الصورة؟" else prompt
            val userMsg = ChatMessage(text = displayPrompt, isUser = true)
            _messages.value = _messages.value + userMsg

            chatSessionDao.insertMessage(
                ChatMessageEntity(
                    id = userMsg.id,
                    sessionId = currentSessionId,
                    text = userMsg.text,
                    isUser = true,
                    timestamp = userMsg.timestamp
                )
            )
            _isLoading.value = true

            fun recordAndSaveAiResponse(text: String, sources: List<WebSource> = emptyList()) {
                val aiMsg = ChatMessage(text = text, isUser = false, sources = sources)
                _messages.value = _messages.value + aiMsg
                viewModelScope.launch {
                    chatSessionDao.insertMessage(
                        ChatMessageEntity(
                            id = aiMsg.id,
                            sessionId = currentSessionId,
                            text = aiMsg.text,
                            isUser = false,
                            timestamp = aiMsg.timestamp
                        )
                    )
                    val s = chatSessionDao.getSessionById(currentSessionId)
                    if (s != null) {
                        chatSessionDao.updateSession(s.copy(lastUpdatedAt = System.currentTimeMillis()))
                    }
                }
            }

            val currentScreenState = _currentScreen.value
            val activeSystemPrompt = when (currentScreenState) {
                is AppScreen.ImageChat -> RemoBrain.IMAGE_CHAT_SYSTEM_PROMPT
                is AppScreen.CodeChat -> RemoBrain.CODE_CHAT_SYSTEM_PROMPT
                is AppScreen.AudioChat -> RemoBrain.AUDIO_CHAT_SYSTEM_PROMPT
                else -> RemoBrain.SYSTEM_PROMPT
            }

            val needsWebSearch = doWebSearch || effectivePrompt.contains("ابحث") || effectivePrompt.contains("بحث") || 
                                 effectivePrompt.contains("ما هو") || effectivePrompt.contains("ما هي") || 
                                 effectivePrompt.contains("أخبار") || effectivePrompt.contains("معلومات") ||
                                 effectivePrompt.contains("من هو") || effectivePrompt.contains("متى")

            val webSources = if (needsWebSearch) {
                WebSearchHelper.searchWeb(effectivePrompt)
            } else {
                emptyList()
            }

            // 1. المساعد الذكي ريمو: استجابة فورية ذكية لأسئلة الهوية والترحيب والشخصية (فقط في الدردشة العامة لـ ريمو)
            if (bitmap == null && !doWebSearch && !isImageGeneration && currentScreenState is AppScreen.GeneralChat && RemoBrain.isRemoNativePersonaQuery(effectivePrompt)) {
                delay(200)
                val remoAnswer = RemoBrain.getSmartOfflineResponse(effectivePrompt)
                recordAndSaveAiResponse(remoAnswer, webSources)
                _isLoading.value = false
                return@launch
            }

            // 2. فحص توفر مفتاح Gemini صالح ومراعاة الحصة
            val configuredKey = if (userKey.isNotBlank()) userKey else (try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" })
            val hasValidKey = configuredKey.isNotBlank() && configuredKey != "MY_GEMINI_API_KEY" && configuredKey.startsWith("AIza")

            if (hasValidKey && quotaAllowed) {
                try {
                    val modelName = _selectedModel.value
                    val generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = configuredKey,
                        systemInstruction = com.google.ai.client.generativeai.type.content {
                            text(activeSystemPrompt)
                        }
                    )

                    val promptForModel = if (needsWebSearch && webSources.isNotEmpty()) {
                        val sourcesText = webSources.joinToString("\n") { "- ${it.title}: ${it.url} (${it.snippet})" }
                        "بناءً على نتائج البحث والتوثيق التالية من الويب:\n$sourcesText\n\nأجب عن استفسار المستخدم بدقة واحترافية: $effectivePrompt"
                    } else {
                        effectivePrompt
                    }

                    // Conversation Context / Memory: pass past messages as history
                    val historyMessages = _messages.value.dropLast(1)
                    val chatHistory = historyMessages.map { msg ->
                        com.google.ai.client.generativeai.type.content(role = if (msg.isUser) "user" else "model") {
                            text(msg.text)
                        }
                    }

                    val chat = generativeModel.startChat(history = chatHistory)

                    val response = if (bitmap != null) {
                        chat.sendMessage(
                            com.google.ai.client.generativeai.type.content {
                                image(bitmap)
                                text(promptForModel)
                            }
                        )
                    } else {
                        chat.sendMessage(promptForModel)
                    }

                    val responseText = response.text ?: RemoBrain.getSmartOfflineResponse(
                        effectivePrompt,
                        isWebSearch = needsWebSearch,
                        isImageGen = isImageGeneration,
                        hasImage = (bitmap != null)
                    )

                    recordAndSaveAiResponse(responseText, webSources)
                } catch (t: Throwable) {
                    val errorMsg = t.message ?: ""
                    val isQuotaOrRateLimit = errorMsg.contains("resource_exhausted", ignoreCase = true) || 
                                           errorMsg.contains("quota", ignoreCase = true) || 
                                           errorMsg.contains("rate-limit", ignoreCase = true) ||
                                           errorMsg.contains("429")
                    
                    val baseFallback = RemoBrain.getSmartOfflineResponse(
                        effectivePrompt,
                        isWebSearch = needsWebSearch,
                        isImageGen = isImageGeneration,
                        hasImage = (bitmap != null)
                    )
                    
                    val finalFallback = if (isQuotaOrRateLimit) {
                        "⚠️ **تنبيه السحابة (Rate Limit / Quota Exceeded):**\nلقد تم بلوغ الحد المؤقت للطلبات في الخادم السحابي الحالي.\n\n🛡️ **التحويل التلقائي:** قام ريمو بالتحويل الفوري إلى **الوضع المحلي الذكي والمدرب** لخدمتك فوراً دون انقطاع!\n\n$baseFallback"
                    } else {
                        baseFallback
                    }
                    recordAndSaveAiResponse(finalFallback, webSources)
                } finally {
                    _isLoading.value = false
                }
            } else {
                // إذا نفدت الحصة اليومية أو لم يتم توفير مفتاح، يجيب ريمو بذكائه الداخلي
                try {
                    delay(300)
                    val baseOfflineAnswer = RemoBrain.getSmartOfflineResponse(
                        effectivePrompt,
                        isWebSearch = needsWebSearch,
                        isImageGen = isImageGeneration,
                        hasImage = (bitmap != null)
                    )
                    val finalAnswer = if (!quotaAllowed && !hasValidKey) {
                        "$baseOfflineAnswer\n\n*(⚡ تم الوصول للحد اليومي لطلبات السحابة المجانية اليومية، يجيب ريمو حالياً بالذكاء المحلي المدرب!)*"
                    } else {
                        baseOfflineAnswer
                    }
                    recordAndSaveAiResponse(finalAnswer, webSources)
                } catch (t: Throwable) {
                    recordAndSaveAiResponse("أهلاً بك يا صديقي! أنا ريمو معك دائماً للإجابة على كافة أسئلتك.", webSources)
                } finally {
                    _isLoading.value = false
                }
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
