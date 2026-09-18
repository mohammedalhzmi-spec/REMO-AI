package com.ai.geminiapp

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingPageView(viewModel: MainViewModel) {
    val context = LocalContext.current
    var miniChatInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var expandedArticleId by remember { mutableStateOf<String?>(null) }

    val quickChips = listOf(
        "💻 اكتب لي كود Kotlin",
        "🐞 فحص وتصحيح كود برمجي",
        "🎨 تخيل صورة مدينة مستقبلية",
        "🎵 ألف لي كلمات قصيدة",
        "🌐 ابحث في الويب الموثق",
        "⚡ ما هي مزايا Gemini 1.5؟"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D11))
    ) {
        // Luxury Background Image with subtle gradient
        Image(
            painter = painterResource(id = R.drawable.remo_landing_bg),
            contentDescription = "خلفية ريمو الفاخرة",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.35f
        )

        // Dark gradient overlay for extreme contrast and readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0D0D11),
                            Color(0xE60D0D11),
                            Color(0xF50D0D11),
                            Color(0xFF0A0A0E)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Notification Banner for App Update
            AppUpdateBanner(viewModel = viewModel)

            // ================= 1. HERO SECTION & INTRO =================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Mascot Brand Avatar
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldPrimary.copy(alpha = 0.3f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_remo_avatar),
                        contentDescription = "شعار ريمو الذكي",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, GoldPrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GoldPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "🚀 منصة الذكاء الاصطناعي الشاملة",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "REMO AI",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldPrimary,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "رفيقك الذكي لكتابة الأكواد، تصميم وتوليد الصور، وهندسة الصوتيات والموسيقى",
                    fontSize = 15.sp,
                    color = Color(0xFFE2E8F0),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E1E28)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "ابتكار وبرمجة: المطور محمد الحزمي",
                            fontSize = 12.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.Platform) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E1E2C),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "تشغيل المنصة بكامل أدواتها وصفحاتها ⚡",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ================= 2. MINI INTERACTIVE CHAT IN THE CENTER =================
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181824)),
                border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "✨", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "ماذا تريد أن تصنع اليوم؟ أخبرني...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "جرب محادثة ريمو فوراً بدون أي تعقيد!",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    OutlinedTextField(
                        value = miniChatInput,
                        onValueChange = { miniChatInput = it },
                        placeholder = {
                            Text(
                                "اكتب فكرتك، سؤالك، أو طلبك هنا...",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF2E2E3E),
                            focusedContainerColor = Color(0xFF12121A),
                            unfocusedContainerColor = Color(0xFF12121A)
                        )
                    )

                    // Quick Chips Suggestions
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickChips) { chip ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF222234),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    miniChatInput = chip.substringAfter(" ")
                                }
                            ) {
                                Text(
                                    text = chip,
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val promptToLaunch = if (miniChatInput.isNotBlank()) miniChatInput else "مرحباً يا ريمو! أريدك أن تريني إمكانياتك الذكية اليوم."
                            viewModel.navigateTo(AppScreen.Chat)
                            viewModel.sendMessage(promptToLaunch)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ابدأ الآن 🚀",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // ================= 3. APP SERVICES SHOWCASE CARDS =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🌟", fontSize = 20.sp)
                    Text(
                        text = "خدمات التطبيق المتطورة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                // 1. Texts & Chat
                ServiceFeatureCard(
                    icon = "💬",
                    title = "توليد النصوص والمحادثة الفورية",
                    description = "مساعد ذكي مدرب على تقديم إجابات بلا حدود، صياغة مقالات، خطابات، وتلخيص الكتب بأسلوب لغوي راقٍ."
                )

                // 2. Code Studio
                ServiceFeatureCard(
                    icon = "💻",
                    title = "استوديو كتابة وتطوير الأكواد",
                    description = "توليد دوال وتطبيقات كاملة بأكثر من 20 لغة برمجة، فحص واكتشاف وتصحيح الأخطاء البرمجية (Debugging)، وهندسة قواعد البيانات."
                )

                // 3. Image Studio
                ServiceFeatureCard(
                    icon = "🎨",
                    title = "استوديو تصميم وتوليد وفحص الصور",
                    description = "صياغة برومبتات بصرية سينمائية، فحص وتحليل الصور، ابتكار الشعارات والهوية البصرية، وتفريغ النصوص بالـ OCR."
                )

                // 4. Audio & Music Studio
                ServiceFeatureCard(
                    icon = "🎵",
                    title = "استوديو الصوتيات والموسيقى",
                    description = "قارئ صوتي Text-to-Speech بنبرة عربية طبيعية، محادثة صوتية مباشرة، تأليف كلمات الأغاني وتلحينها، وسيناريوهات البودكاست."
                )

                // 5. Web Search Grounding
                ServiceFeatureCard(
                    icon = "🌐",
                    title = "البحث الموثق الحي في الويب",
                    description = "ربط مباشر بالويب لتقديم أحدث الأخبار والبيانات الحية الموثقة مع ذكر المصادر والروابط الرسمية المباشرة."
                )
            }

            // ================= 4. STEP-BY-STEP REGISTRATION GUIDE =================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                border = BorderStroke(1.dp, Color(0xFF2A2A3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = GoldPrimary)
                        Text(
                            text = "دليل خطوات التسجيل بالتطبيق",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    RegistrationStepItem(
                        stepNumber = "1",
                        title = "الضغط على زر إنشاء حساب جديد",
                        description = "انقر على زر 'حساب جديد' في أعلى الصفحة أو في الأسفل لفتح نموذج التسجيل الآمن."
                    )

                    RegistrationStepItem(
                        stepNumber = "2",
                        title = "إدخال بياناتك الأساسية",
                        description = "أدخل اسمك، بريدك الإلكتروني، وكلمة المرور المشفرة محلياً في قاعدة بيانات Room بأعلى درجات الخصوصية."
                    )

                    RegistrationStepItem(
                        stepNumber = "3",
                        title = "تفعيل المساعد والحصة المجانية",
                        description = "يعمل مساعد ريمو فوراً بحصته المجانية المدمجة دون الحاجة لأي إعداد. كما يمكنك إضافة مفتاح API خاص بك للاستخدام اللامحدود."
                    )

                    RegistrationStepItem(
                        stepNumber = "4",
                        title = "الانطلاق في لوحة التحكم",
                        description = "ستفتح لك لوحة التحكم الشاملة التي تمنحك وصولاً كاملاً لأدوات الأكواد، الصور، والصوتيات والمفضلة."
                    )
                }
            }

            // ================= 5. DETAILED ARTICLES ON GEMINI AI TOOLS =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📚", fontSize = 20.sp)
                    Text(
                        text = "مقالات تفصيلية عن أدوات الذكاء الاصطناعي",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                ServiceCatalog.geminiArticles.forEach { article ->
                    val isExpanded = expandedArticleId == article.id

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181826)),
                        border = BorderStroke(1.dp, if (isExpanded) GoldPrimary else Color(0xFF262638)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedArticleId = if (isExpanded) null else article.id
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = article.iconEmoji, fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GoldPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = article.tag,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = article.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = GoldPrimary
                                )
                            }

                            Text(
                                text = article.summary,
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                lineHeight = 18.sp
                            )

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .background(Color(0xFF11111A), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = article.content,
                                        fontSize = 12.sp,
                                        color = Color(0xFFD1D5DB),
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= 6. FINAL ACTION BUTTONS & ATTRIBUTION =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.SignUp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إنشاء حساب جديد مجاناً", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.Login) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GoldPrimary)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تسجيل الدخول إلى حسابك", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.navigateTo(AppScreen.Platform)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F2C)),
                    border = BorderStroke(1.5.dp, GoldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تشغيل منصة محمد الحزمي للذكاء الاصطناعي", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "جميع الحقوق محفوظة للمطور محمد الحزمي © 2026\nREMO AI Studio Edition",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ServiceFeatureCard(icon: String, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181826)),
        border = BorderStroke(1.dp, Color(0xFF262638)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = GoldPrimary.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 22.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun RegistrationStepItem(stepNumber: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}
