package com.ai.geminiapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Animated 3-dot typing indicator when waiting for Gemini API response.
 */
@Composable
fun TypingIndicatorAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_transition")

    // Dot 1 animation
    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0)
        ),
        label = "dot1"
    )
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0)
        ),
        label = "dot1_alpha"
    )

    // Dot 2 animation (delayed 150ms)
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(150)
        ),
        label = "dot2"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(150)
        ),
        label = "dot2_alpha"
    )

    // Dot 3 animation (delayed 300ms)
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(300)
        ),
        label = "dot3"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(300)
        ),
        label = "dot3_alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_remo_avatar),
            contentDescription = "ريمو يفكر",
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .border(1.5.dp, GoldPrimary, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            ),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "ريمو يفكر ويكتب لك...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dot 1
                    Box(
                        modifier = Modifier
                            .offset(y = dot1Offset.dp)
                            .size(9.dp)
                            .background(GoldPrimary.copy(alpha = dot1Alpha), CircleShape)
                    )
                    // Dot 2
                    Box(
                        modifier = Modifier
                            .offset(y = dot2Offset.dp)
                            .size(9.dp)
                            .background(GoldPrimary.copy(alpha = dot2Alpha), CircleShape)
                    )
                    // Dot 3
                    Box(
                        modifier = Modifier
                            .offset(y = dot3Offset.dp)
                            .size(9.dp)
                            .background(GoldPrimary.copy(alpha = dot3Alpha), CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * Modern DataStore-based Daily Quota Progress Bar for Gemini API requests.
 */
@Composable
fun DailyQuotaProgressBar(
    quotaInfo: DailyQuotaInfo,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = quotaInfo.progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "quota_progress"
    )

    val progressColor = when {
        quotaInfo.isQuotaExhausted -> Color(0xFFFF5252)
        quotaInfo.remainingRequests <= 5 -> Color(0xFFFFB300)
        else -> GoldPrimary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, progressColor.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "الحصة اليومية لـ Gemini API",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (quotaInfo.isQuotaExhausted) {
                            "استنفدت الحصة (${quotaInfo.usedRequests}/${quotaInfo.maxDailyRequests})"
                        } else {
                            "المتبقي: ${quotaInfo.remainingRequests} من ${quotaInfo.maxDailyRequests}"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (quotaInfo.isQuotaExhausted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "يواصل ريمو الرد بالذكاء المحلي، أو أدخل مفتاحك لطلبات غير محدودة",
                        fontSize = 10.sp,
                        color = Color(0xFFFF8A80)
                    )
                    if (onOpenSettings != null) {
                        Text(
                            text = "إدخال مفتاح ⚙️",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            modifier = Modifier.clickable { onOpenSettings() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Side Navigation Drawer Sheet for managing saved chat sessions & continuity.
 */
@Composable
fun ChatSessionsDrawerContent(
    sessions: List<ChatSessionEntity>,
    activeSessionId: String,
    quotaInfo: DailyQuotaInfo,
    onNewChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearAll: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(310.dp),
        drawerContainerColor = Color(0xFF14141E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_remo_avatar),
                        contentDescription = "ريمو",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, GoldPrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = "جلسات دردشة ريمو 💬",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "حفظ واستمرارية المحادثات",
                            fontSize = 10.sp,
                            color = GoldPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                }
            }

            // "New Chat" Prominent Button
            Button(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "دردشة جديدة ➕",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(color = Color(0xFF27273A), thickness = 1.dp)

            // Saved Sessions Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المحادثات السابقة (${sessions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
                if (sessions.isNotEmpty()) {
                    Text(
                        text = "مسح الكل",
                        fontSize = 11.sp,
                        color = Color(0xFFFF8A80),
                        modifier = Modifier.clickable { onClearAll() }
                    )
                }
            }

            // Sessions List
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "لا توجد جلسات دردشة سابقة",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "ابدأ محادثة جديدة مع ريمو وستُحفظ هنا تلقائياً",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions, key = { it.sessionId }) { session ->
                        val isActive = session.sessionId == activeSessionId
                        val containerColor = if (isActive) GoldPrimary.copy(alpha = 0.15f) else Color(0xFF1B1B28)
                        val borderColor = if (isActive) GoldPrimary else Color(0xFF2A2A3E)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = containerColor,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSession(session.sessionId)
                                    onCloseDrawer()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = if (isActive) GoldPrimary else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.title,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (isActive) GoldPrimary else Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatSessionDate(session.lastUpdatedAt),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteSession(session.sessionId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف الجلسة",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF27273A), thickness = 1.dp)

            // Drawer Footer: Daily Quota Info & Branding
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حصة اليوم: ${quotaInfo.remainingRequests} / ${quotaInfo.maxDailyRequests}",
                        fontSize = 11.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "محمد الحزمي 🇸🇦",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun formatSessionDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000L -> "الآن"
        diff < 3600_000L -> "منذ ${diff / 60_000L} دقيقة"
        diff < 86400_000L -> "اليوم ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
        diff < 172800_000L -> "أمس"
        else -> SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

/**
 * Dedicated Sources and References Component for displaying web search results and links in chat.
 */
@Composable
fun SourcesReferencesView(sources: List<WebSource>) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    if (sources.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "المصادر والمراجع الموثقة من الويب (2026):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldPrimary
                    )
                }

                sources.forEach { source ->
                    Surface(
                        onClick = {
                            try {
                                if (source.url.isNotBlank()) {
                                    uriHandler.openUri(source.url)
                                }
                            } catch (e: Throwable) {
                                // Ignore invalid uri
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = source.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (source.snippet.isNotBlank()) {
                                    Text(
                                        text = source.snippet,
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "فتح الرابط",
                                tint = GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
