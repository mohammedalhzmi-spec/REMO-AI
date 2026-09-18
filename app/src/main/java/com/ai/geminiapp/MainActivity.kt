package com.ai.geminiapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

val GoldPrimary = Color(0xFFD4AF37)
val GoldDark = Color(0xFF996515)
val DarkBackground = Color(0xFF0F0F0F)
val SurfaceDark = Color(0xFF1E1E1E)

class MainActivity : ComponentActivity() {
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UpdateNotificationHelper.createNotificationChannel(this)

        setContent {
            val viewModel: MainViewModel = viewModel()
            mainViewModel = viewModel

            LaunchedEffect(intent) {
                if (intent?.getBooleanExtra(UpdateNotificationHelper.EXTRA_OPEN_UPDATE_DIALOG, false) == true) {
                    viewModel.openUpdateDialog()
                }
            }

            val isDarkMode by viewModel.isDarkMode.collectAsState()

            val colorScheme = if (isDarkMode) {
                darkColorScheme(
                    primary = GoldPrimary,
                    secondary = GoldDark,
                    background = DarkBackground,
                    surface = SurfaceDark,
                    surfaceContainer = Color(0xFF1A1A1A)
                )
            } else {
                lightColorScheme(
                    primary = GoldDark,
                    secondary = GoldPrimary,
                    background = Color(0xFFF9F9F9),
                    surface = Color.White,
                    surfaceContainer = Color(0xFFF0F0F0)
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRootNavigator(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(UpdateNotificationHelper.EXTRA_OPEN_UPDATE_DIALOG, false)) {
            mainViewModel?.openUpdateDialog()
        }
    }
}

@Composable
fun AppRootNavigator(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showQuotaDialog by viewModel.showQuotaDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showSurpriseDialog by viewModel.showSurpriseDialog.collectAsState()
    val currentSurprise by viewModel.currentSurprise.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            is AppScreen.Welcome -> WelcomeScreen(viewModel)
            is AppScreen.Login -> LoginScreen(viewModel)
            is AppScreen.SignUp -> SignUpScreen(viewModel)
            is AppScreen.Dashboard -> DashboardScreen(viewModel)
            is AppScreen.Chat, is AppScreen.GeneralChat -> ChatScreen(viewModel, title = "ريمو - صديقك الذكي ✨", subtitle = "المساعد الحصري • محمد الحزمي")
            is AppScreen.ImageChat -> ChatScreen(viewModel, title = "استوديو ريمو للصور البصرية 🎨", subtitle = "توليد وتصميم الأفكار والصور")
            is AppScreen.CodeChat -> ChatScreen(viewModel, title = "استوديو ريمو للأكواد والبرمجة 💻", subtitle = "كتابة وتصحيح وتطوير البرمجيات")
            is AppScreen.AudioChat -> ChatScreen(viewModel, title = "استوديو ريمو للصوتيات والبودكاست 🎵", subtitle = "شعر، موسيقى، وسكريبتات بودكاست")
            is AppScreen.About -> AboutScreen(viewModel)
            is AppScreen.Templates -> TemplatesScreen(viewModel)
            is AppScreen.Favorites -> FavoritesScreen(viewModel)
            is AppScreen.Stats -> StatsScreen(viewModel)
            is AppScreen.Platform -> PlatformScreen(viewModel)
        }

        if (showSurpriseDialog && currentSurprise != null) {
            RemoSurpriseDialog(
                surprise = currentSurprise!!,
                onDismiss = { viewModel.dismissSurpriseDialog() },
                onNext = { viewModel.nextSurprise() },
                onChatAbout = { topic ->
                    viewModel.dismissSurpriseDialog()
                    viewModel.navigateTo(AppScreen.Chat)
                    viewModel.sendMessage("يا ريمو، أريد أن نتحدث ونتناقش حول: $topic")
                }
            )
        }

        val activeToolDialog by viewModel.activeToolDialog.collectAsState()
        if (activeToolDialog != null) {
            ServiceToolDialog(
                tool = activeToolDialog!!,
                onDismiss = { viewModel.closeToolDialog() },
                onLaunch = { input ->
                    viewModel.launchTool(activeToolDialog!!, input)
                }
            )
        }

        if (showUpdateDialog) {
            AppUpdateDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.dismissUpdateDialog() }
            )
        }

        if (showQuotaDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowQuotaDialog(false) },
                title = { Text("نفدت حصتك المجانية") },
                text = {
                    Text("لقد استهلكت حصتك المجانية اليومية المدعومة من المطور. للاستمرار في الاستخدام المجاني اللامحدود، يرجى الانتقال إلى Google AI Studio وإنشاء مفتاح API خاص بك ثم لصقه في الإعدادات.")
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setShowQuotaDialog(false)
                        viewModel.setShowSettingsDialog(true)
                    }) {
                        Text("إعدادات المفتاح")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowQuotaDialog(false) }) {
                        Text("إغلاق")
                    }
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "REMO AI",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                },
                actions = {
                    // Top Auth Quick Buttons
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.Login) }) {
                        Text("دخول", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.SignUp) }) {
                        Text("حساب جديد", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }

                    // Menu Button (زر القائمة المطلوب)
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "القائمة", tint = GoldPrimary)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("تسجيل الدخول") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.Login)
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("إنشاء حساب") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.SignUp)
                            },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("تغيير المظهر") },
                            onClick = {
                                menuExpanded = false
                                viewModel.setDarkMode(!viewModel.isDarkMode.value)
                            },
                            leadingIcon = { Icon(Icons.Default.Brightness4, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("تغيير اللغة (العربية / English)") },
                            onClick = {
                                menuExpanded = false
                                val newLang = if (viewModel.language.value == "ar") "en" else "ar"
                                viewModel.setLanguage(newLang)
                                Toast.makeText(context, "تم تغيير اللغة إلى: $newLang", Toast.LENGTH_SHORT).show()
                            },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("عن المطور") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.About)
                            },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("تشغيل منصة محمد الحزمي للذكاء الاصطناعي") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.Platform)
                            },
                            leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("التحقق من التحديثات 🚀") },
                            onClick = {
                                menuExpanded = false
                                viewModel.checkForUpdates(manual = true, context = context)
                            },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LandingPageView(viewModel = viewModel)
        }
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authError by viewModel.authError.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تسجيل الدخول",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )

                Text(
                    text = "أدخل بيانات حسابك للوصول إلى لوحة تحكم REMO AI",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                if (authError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearAuthError() },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearAuthError() },
                    label = { Text("كلمة المرور") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        viewModel.login(email, password) {
                            Toast.makeText(context, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("دخول", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                TextButton(onClick = { viewModel.navigateTo(AppScreen.SignUp) }) {
                    Text("ليس لديك حساب؟ إنشاء حساب جديد", color = GoldPrimary)
                }

                TextButton(onClick = { viewModel.navigateTo(AppScreen.Welcome) }) {
                    Text("العودة للرئيسية", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authError by viewModel.authError.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "إنشاء حساب جديد",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )

                Text(
                    text = "أنشئ حسابك الآن واستمتع بمميزات REMO AI الكاملة",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                if (authError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; viewModel.clearAuthError() },
                    label = { Text("الاسم الكامل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearAuthError() },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearAuthError() },
                    label = { Text("كلمة المرور") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        viewModel.signUp(name, email, password) {
                            Toast.makeText(context, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تسجيل وإنشاء الحساب", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                TextButton(onClick = { viewModel.navigateTo(AppScreen.Login) }) {
                    Text("لديك حساب بالفعل؟ تسجيل الدخول", color = GoldPrimary)
                }

                TextButton(onClick = { viewModel.navigateTo(AppScreen.Welcome) }) {
                    Text("العودة للرئيسية", color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val requestCount by viewModel.requestCount.collectAsState()
    val userApiKey by viewModel.userApiKey.collectAsState()
    val dailyQuotaInfo by viewModel.dailyQuotaInfo.collectAsState()
    val showDeveloperBanner by viewModel.showDeveloperBanner.collectAsState()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "أيقونة التطبيق",
                        modifier = Modifier
                            .padding(start = 12.dp, end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, GoldPrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                },
                title = {
                    Column {
                        Text(
                            text = "لوحة تحكم REMO AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = currentUser?.name ?: "المستخدم",
                            fontSize = 12.sp,
                            color = GoldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "القائمة")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("تغيير المظهر") },
                            onClick = {
                                menuExpanded = false
                                viewModel.setDarkMode(!viewModel.isDarkMode.value)
                            },
                            leadingIcon = { Icon(Icons.Default.Brightness4, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("تغيير اللغة") },
                            onClick = {
                                menuExpanded = false
                                val newLang = if (viewModel.language.value == "ar") "en" else "ar"
                                viewModel.setLanguage(newLang)
                                Toast.makeText(context, "تم تغيير اللغة إلى: $newLang", Toast.LENGTH_SHORT).show()
                            },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("إعدادات مفتاح الـ API") },
                            onClick = {
                                menuExpanded = false
                                viewModel.setShowSettingsDialog(true)
                            },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("عن المطور") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.About)
                            },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("تشغيل منصة محمد الحزمي AI") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.Platform)
                            },
                            leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("التحقق من التحديثات 🚀") },
                            onClick = {
                                menuExpanded = false
                                viewModel.checkForUpdates(manual = true, context = context)
                            },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("تسجيل الدخول") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.Login)
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("إنشاء حساب") },
                            onClick = {
                                menuExpanded = false
                                viewModel.navigateTo(AppScreen.SignUp)
                            },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("تسجيل الخروج", color = Color.Red) },
                            onClick = {
                                menuExpanded = false
                                viewModel.logout()
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // In-App Notification Banner for App Update
                AppUpdateBanner(viewModel = viewModel)

                // Remo Mascot Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box {
                                Image(
                                    painter = painterResource(id = R.drawable.img_remo_avatar),
                                    contentDescription = "ريمو صديقك الذكي",
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, GoldPrimary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                        .border(2.dp, Color.Black, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "ريمو - صديقك الذكي",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = GoldPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GoldPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "مساعد حصري",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "مدرب للدردشة بلا حدود، والبحث الموثق في الويب، وتقديم مفاجآت حصرية يومية!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.Chat) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("دردش مع ريمو 💬", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.openSurpriseDialog() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مفاجآت ريمو 🎁", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Quota / Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حصة Gemini API اليومية والخدمات",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Icon(
                                imageVector = if (dailyQuotaInfo.isQuotaExhausted) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (dailyQuotaInfo.isQuotaExhausted) Color(0xFFFF5252) else GoldPrimary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { dailyQuotaInfo.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (dailyQuotaInfo.isQuotaExhausted) Color(0xFFFF5252) else GoldPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = "المتبقي اليوم: ${dailyQuotaInfo.remainingRequests} من ${dailyQuotaInfo.maxDailyRequests} طلب. الذكاء المحلي الداخلي لريمو متوفر دائماً بلا انقطاع.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Text(
                    text = "الأقسام والمزايا الحصرية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Dashboard Grid Cards Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        title = "محادثة ريمو الذكي",
                        subtitle = "دردشة، صوت، وتوليد كود",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo(AppScreen.Chat)
                    }

                    DashboardCard(
                        title = "بحث الويب الموثق",
                        subtitle = "إجابات بالمصادر والروابط",
                        icon = Icons.Default.Language,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!viewModel.isWebSearchEnabled.value) {
                            viewModel.toggleWebSearch()
                        }
                        viewModel.navigateTo(AppScreen.Chat)
                    }
                }

                // Dashboard Grid Cards Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        title = "قوالب الأسئلة الجاهزة",
                        subtitle = "نماذج جاهزة للاستخدام",
                        icon = Icons.AutoMirrored.Filled.Article,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo(AppScreen.Templates)
                    }

                    DashboardCard(
                        title = "المفضلة المحفوظة",
                        subtitle = "ردود ريمو التي حفظتها",
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo(AppScreen.Favorites)
                    }
                }

                // Dashboard Grid Cards Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        title = "إحصائيات النشاط",
                        subtitle = "سجل تفاعلك واستخدامك",
                        icon = Icons.Default.BarChart,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo(AppScreen.Stats)
                    }

                    DashboardCard(
                        title = "منصة المطور الحزمي",
                        subtitle = "تشغيل المنصة بكافة أدواتها",
                        icon = Icons.Default.Public,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo(AppScreen.Platform)
                    }
                }

                // Dedicated In-App Platform Runner Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.Platform) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B2A)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldPrimary,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "منصة محمد الحزمي للذكاء الاصطناعي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GoldPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF00E676).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "متكاملة ⚡",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "تشغيل المنصة بكامل صفحاتها وأدواتها التفاعلية مباشرة داخل التطبيق",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                lineHeight = 16.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.Platform) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("تشغيل 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Dedicated Services on Dashboard requested by user: Code, Image, Audio
                Text(
                    text = "استوديوهات وخدمات ريمو التخصصية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GoldPrimary,
                    modifier = Modifier.padding(top = 10.dp)
                )

                DashboardServicesView(
                    onToolSelected = { tool ->
                        when (tool.category) {
                            "code" -> viewModel.navigateTo(AppScreen.CodeChat)
                            "image" -> viewModel.navigateTo(AppScreen.ImageChat)
                            "audio" -> viewModel.navigateTo(AppScreen.AudioChat)
                            else -> viewModel.navigateTo(AppScreen.GeneralChat)
                        }
                        viewModel.sendMessage(tool.defaultPrompt)
                    }
                )
            }

            // Animated Banner Notification showing and hiding automatically upon entering Dashboard
            AnimatedVisibility(
                visible = showDeveloperBanner,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GoldPrimary,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color.Black)
                        Text(
                            text = "هذا التطبيق برمجة وتطوير المطور محمد الحزمي\nجميع الحقوق محفوظة للمطور",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(28.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MainViewModel = viewModel(),
    title: String = "ريمو - صديقك الذكي ✨",
    subtitle: String = "المساعد الحصري • محمد الحزمي"
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isWebSearchEnabled by viewModel.isWebSearchEnabled.collectAsState()
    val requestCount by viewModel.requestCount.collectAsState()
    val userApiKey by viewModel.userApiKey.collectAsState()
    val dailyQuotaInfo by viewModel.dailyQuotaInfo.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        try {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // TTS ready
                }
            }
            tts = textToSpeech
        } catch (t: Throwable) {
            tts = null
        }
        onDispose {
            try {
                textToSpeech?.stop()
                textToSpeech?.shutdown()
            } catch (t: Throwable) {
                // Ignore
            }
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                inputPrompt = results[0]
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن مع ريمو...")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "التعرف الصوتي غير متوفر", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                val bitmap = ImageDecoder.decodeBitmap(source)
                viewModel.sendMessage(inputPrompt, bitmap = bitmap)
                inputPrompt = ""
            } catch (e: Exception) {
                Toast.makeText(context, "فشل تحميل الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.size - 1)
            } catch (t: Throwable) {
                // safely ignore
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatSessionsDrawerContent(
                sessions = chatSessions,
                activeSessionId = activeSessionId,
                quotaInfo = dailyQuotaInfo,
                onNewChat = { viewModel.createNewSession() },
                onSelectSession = { sessionId -> viewModel.selectSession(sessionId) },
                onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) },
                onClearAll = { viewModel.clearAllSessions() },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (viewModel.isLoggedIn.value) {
                                        viewModel.navigateTo(AppScreen.Dashboard)
                                    } else {
                                        viewModel.navigateTo(AppScreen.Welcome)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                            }
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "جلسات المحادثة 💬", tint = GoldPrimary)
                            }
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box {
                                Image(
                                    painter = painterResource(id = R.drawable.img_remo_avatar),
                                    contentDescription = "ريمو صديقك الذكي",
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, GoldPrimary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                        .border(1.dp, Color.Black, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                            Column {
                                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = subtitle,
                                    fontSize = 10.sp,
                                    color = GoldPrimary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.createNewSession() }) {
                            Icon(Icons.Default.AddComment, contentDescription = "دردشة جديدة ➕", tint = GoldPrimary)
                        }
                        IconButton(onClick = { viewModel.openSurpriseDialog() }) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = "مفاجآت ريمو 🎁", tint = GoldPrimary)
                        }
                        IconButton(onClick = { viewModel.toggleWebSearch() }) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "بحث الويب",
                                tint = if (isWebSearchEnabled) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. DataStore-based Daily Quota Progress Bar
                DailyQuotaProgressBar(
                    quotaInfo = dailyQuotaInfo,
                    onOpenSettings = { viewModel.setShowSettingsDialog(true) }
                )
            // Web Search mode active banner
            AnimatedVisibility(visible = isWebSearchEnabled) {
                Surface(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "وضع البحث الموثق في الويب مفعّل (توثيق بالمصادر والروابط) 🌐",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleWebSearch() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(280)) +
                                slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) +
                                scaleIn(initialScale = 0.94f, animationSpec = tween(240))
                    ) {
                        ChatBubble(message = message, viewModel = viewModel, tts = tts)
                    }
                }
                if (isLoading) {
                    item(key = "typing_indicator_active") {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(250)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                        ) {
                            TypingIndicatorAnimation()
                        }
                    }
                }
            }

            // Quick Suggestion Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = { viewModel.openSurpriseDialog() },
                    label = { Text("🎁 مفاجآت ريمو", fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = GoldPrimary.copy(alpha = 0.15f))
                )
                SuggestionChip(
                    onClick = { viewModel.toggleWebSearch() },
                    label = { Text(if (isWebSearchEnabled) "🌐 إلغاء بحث الويب" else "🌐 بحث في الويب", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { viewModel.sendMessage("يا ريمو، ألهمني بحكمة ذكية وجميلة لليوم") },
                    label = { Text("💡 حكمة اليوم", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { viewModel.sendMessage("يا ريمو، أعطني لغزاً وتحدي ذكاء مشوقاً مع الحل") },
                    label = { Text("🧩 لغز وتحدي", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { viewModel.sendMessage("يا ريمو، شاركني سراً تقنياً مفيداً يجهله الكثيرون") },
                    label = { Text("🚀 سر تقني", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { viewModel.sendMessage("يا ريمو، قل لي طرفة أو نكتة مبرمجين ذكية ترسم الابتسامة") },
                    label = { Text("😂 طرفة ذكية", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { viewModel.sendMessage("يا ريمو، ساعدني في كتابة فكرة كود برمجي احترافي") },
                    label = { Text("💻 كود برمجي", fontSize = 11.sp) }
                )
            }

            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "نظام الحماية مفعل: حظر وتطهير تلقائي للألفاظ المسيئة والإباحية",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "تحدث صوتياً", tint = GoldPrimary)
                        }

                        IconButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "إرفاق صورة", tint = GoldPrimary)
                        }

                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = { Text("تحدث مع ريمو صديقك الذكي...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prompt_input"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )

                        IconButton(
                            onClick = {
                                if (inputPrompt.isNotBlank()) {
                                    val text = inputPrompt
                                    inputPrompt = ""
                                    viewModel.sendMessage(text)
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(GoldPrimary, RoundedCornerShape(23.dp))
                                .testTag("send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ChatBubble(message: ChatMessage, viewModel: MainViewModel, tts: TextToSpeech? = null) {
    val context = LocalContext.current
    val bgColor = if (message.isUser) GoldPrimary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant
    val alignment = if (message.isUser) Alignment.End else Alignment.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!message.isUser) {
            Image(
                painter = painterResource(id = R.drawable.img_remo_avatar),
                contentDescription = "ريمو صديقك الذكي",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, GoldPrimary, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = bgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!message.isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "ريمو 🤖✨",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GoldPrimary
                            )
                        }
                    }

                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    if (message.sources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SourcesReferencesView(sources = message.sources)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (!message.isUser) {
                            // Listen button
                            IconButton(
                                onClick = { speakRemoText(tts, message.text, context) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "استمع إلى ريمو",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Copy button
                        IconButton(
                            onClick = { copyToClipboard(context, message.text) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "نسخ",
                                tint = GoldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (!message.isUser) {
                            // Favorite button
                            IconButton(
                                onClick = {
                                    viewModel.addFavorite(message.text)
                                    Toast.makeText(context, "تم الحفظ في المفضلة", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "حفظ للمفضلة",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Download button
                            IconButton(
                                onClick = { saveTextToDownloads(context, message.text) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "تحميل",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GoldPrimary.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "أنت",
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("عن المطور والمشروع") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(140.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, GoldPrimary)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dev_splash),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "REMO AI",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )

            Text(
                text = "برمجة وتطوير المطور محمد الحزمي\nجميع الحقوق محفوظة للمطور",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "تطبيق ذكاء اصطناعي متطور يعتمد على أحدث نماذج Gemini مع نظام إدارة حصص ذكي وواجهات عصرية فائقة الأداء.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    viewModel.navigateTo(AppScreen.Platform)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تشغيل منصة محمد الحزمي للذكاء الاصطناعي", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("إصدار التطبيق الحالي: v1.0", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text("التحديث المتاح: v2.5.0 الذهبي", color = GoldPrimary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { viewModel.checkForUpdates(manual = true, context = context) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("فحص التحديثات 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(viewModel: MainViewModel) {
    var tempKey by remember { mutableStateOf(viewModel.userApiKey.value) }
    val selectedModel by viewModel.selectedModel.collectAsState()
    val context = LocalContext.current

    val modelsList = listOf(
        "gemini-flash-latest" to "Gemini Flash Latest (الأحدث والأسرع - الموصى به ⚡)",
        "gemini-1.5-flash" to "Gemini 1.5 Flash (سريع ومستقر 🚀)",
        "gemini-2.5-flash" to "Gemini 2.5 Flash (إصدار متقدم فائق الذكاء 🧠)",
        "gemini-pro" to "Gemini Pro (احترافي للتحليل العميق 🔬)"
    )

    AlertDialog(
        onDismissRequest = { viewModel.setShowSettingsDialog(false) },
        title = { Text("⚙️ إدارة النماذج اللغوية وربط API") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🧠 إدارة النماذج اللغوية (Model Management):",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = "اختر النموذج اللغوي المفضل لتشغيل ذكاء ريمو:",
                    fontSize = 12.sp
                )

                modelsList.forEach { (modelId, modelLabel) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setSelectedModel(modelId) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = (selectedModel == modelId),
                            onClick = { viewModel.setSelectedModel(modelId) }
                        )
                        Text(text = modelLabel, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "💬 سياق المحادثة والذاكرة (Memory):",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = "✓ ريمو يحتفظ بسياق المحادثة والذاكرة (Memory) تلقائياً عبر كل رسالة جديدة لتجربة حوار مترابطة وذكية.",
                    fontSize = 12.sp
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "🔑 مفتاح الـ API المعتمد لخدمات التطبيق:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = tempKey,
                    onValueChange = { tempKey = it },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    text = "💡 ملاحظة: المساعد الذكي ريمو يعمل ذاتياً بتدريب وتطوير المطور محمد الحزمي.",
                    fontSize = 11.sp,
                    color = GoldPrimary
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.saveUserApiKey(tempKey)
                viewModel.setShowSettingsDialog(false)
                Toast.makeText(context, "تم حفظ إعدادات النموذج والـ API بنجاح ✨", Toast.LENGTH_SHORT).show()
            }) {
                Text("حفظ التغييرات")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.setShowSettingsDialog(false) }) {
                Text("إغلاق")
            }
        }
    )
}

fun saveTextToDownloads(context: Context, content: String) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "REMO_AI_Result_${System.currentTimeMillis()}.txt")
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                resolver.openOutputStream(it)?.use { os -> os.write(content.toByteArray()) }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, values, null, null)
            }
        }
        Toast.makeText(context, "تم الحفظ بنجاح في التنزيلات", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "فشل الحفظ: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("REMO AI Message", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ النص إلى الحافظة", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر النسخ", Toast.LENGTH_SHORT).show()
    }
}

fun speakRemoText(tts: TextToSpeech?, text: String, context: Context) {
    try {
        if (tts == null) {
            Toast.makeText(context, "جاري تهيئة الصوت...", Toast.LENGTH_SHORT).show()
            return
        }
        val clean = text.replace("*", "").replace("#", "").replace("`", "").trim()
        tts.language = java.util.Locale.forLanguageTag("ar")
        tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "RemoVoice_${System.currentTimeMillis()}")
    } catch (t: Throwable) {
        Toast.makeText(context, "خدمة الصوت غير متاحة على هذا الجهاز", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun RemoSurpriseDialog(
    surprise: RemoSurprise,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onChatAbout: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = surprise.icon, fontSize = 28.sp)
                Column {
                    Text(
                        text = surprise.category,
                        fontSize = 12.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = surprise.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = surprise.content,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = surprise.tip,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onChatAbout(surprise.title + ": " + surprise.content) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ناقش مع ريمو 💬", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onNext) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("أخرى 🔄")
                }
                TextButton(onClick = onDismiss) {
                    Text("إغلاق")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(viewModel: MainViewModel) {
    val templates = listOf(
        "كتابة مقال احترافي متكامل حول: " to "محتوى مقال",
        "مراجعة وتحسين كود برمجي (Kotlin / Compose) التالي: " to "مراجعة كود",
        "تلخيص النص التالي باختصار شديد: " to "تلخيص نص",
        "ابتكار 5 أفكار مبتكرة لتطبيق ذكاء اصطناعي في مجال: " to "أفكار تطبيقات",
        "ترجمة النص التالي بدقة واحترافية إلى اللغة الإنجليزية: " to "ترجمة فورية"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مكتبة القوالب والأوامر الذكية", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "اختر قالباً جاهزاً لبدء المحادثة السريعة مع REMO AI:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            items(templates) { template ->
                Card(
                    onClick = { viewModel.sendTemplateMessage(template.first) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = template.second, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = template.first, fontSize = 13.sp, color = Color.LightGray)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GoldPrimary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: MainViewModel) {
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المفضلة والأفكار المحفوظة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(48.dp))
                    Text(text = "لا توجد عناصر محفوظة في المفضلة حالياً", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { fav ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = fav.text, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("REMO AI", fav.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نسخ")
                                }
                                TextButton(onClick = { viewModel.deleteFavorite(fav) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حذف", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val requestCount by viewModel.requestCount.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val userApiKey by viewModel.userApiKey.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إحصائيات الاستخدام والنشاط", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "ملخص نشاط الحساب", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("اسم المستخدم:")
                        Text(currentUser?.name ?: "زائر", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الطلبات المستهلكة اليوم:")
                        Text("$requestCount / 10", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("العناصر المحفوظة بالمفضلة:")
                        Text("${favorites.size}", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("حالة مفتاح API:")
                        Text(if (userApiKey.isNotBlank()) "مفتاح خاص مفعل" else "المفتاح الافتراضي", color = if (userApiKey.isNotBlank()) Color.Green else GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
