package com.ai.geminiapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Data Model for App Update Information
 */
data class AppUpdateInfo(
    val currentVersion: String = "1.0",
    val latestVersion: String = "2.5.0",
    val releaseTitle: String = "تحديث ريمو AI الذهبي الجديد (v2.5.0) 🚀",
    val releaseDate: String = "2026-09-16",
    val updateUrl: String = PLATFORM_URL,
    val isUpdateAvailable: Boolean = true,
    val features: List<String> = listOf(
        "تشغيل منصة محمد الحزمي للذكاء الاصطناعي بكامل صفحاتها وأدواتها داخل التطبيق 🌐",
        "استوديو متقدم لكتابة وتصحيح وشرح الأكواد البرمجية لـ +20 لغة برمجية 💻",
        "استوديو الصور والفحص البصري وتوليد التصاميم بالذكاء الاصطناعي 🎨",
        "استوديو الصوتيات والمحادثة الصوتية التفاعلية المباشرة مع ريمو 🎙️",
        "محرك الطوارئ الذكي للردود الفورية حتى عند ضعف الاتصال السحابي ⚡",
        "تحسينات فائقة في السرعة، وتوافق تام مع أحدث أنظمة أندرويد الحديثة ✨"
    )
)

/**
 * Helper to handle Android system status bar notifications
 */
object UpdateNotificationHelper {
    const val CHANNEL_ID = "remo_app_updates_channel"
    const val NOTIFICATION_ID = 2026
    const val EXTRA_OPEN_UPDATE_DIALOG = "open_update_dialog"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تحديثات ريمو AI"
            val descriptionText = "إشعارات إطلاق التحديثات والتحسينات الجديدة للتطبيق"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showSystemUpdateNotification(context: Context, updateInfo: AppUpdateInfo) {
        try {
            createNotificationChannel(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_UPDATE_DIALOG, true)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Direct Update action button in the notification
            val updateActionIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_UPDATE_DIALOG, true)
            }
            val updatePendingIntent = PendingIntent.getActivity(
                context,
                102,
                updateActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(updateInfo.releaseTitle)
                .setContentText("يتوفر إصدار جديد ${updateInfo.latestVersion}. اضغط هنا للاطلاع والتحديث الفوري!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("يتوفر إصدار جديد ${updateInfo.latestVersion} لتطبيق ريمو AI يتضمن تشغيل المنصة المتكاملة واستوديوهات الأكواد والصوت والصور. اضغط لتحديث التطبيق الآن!")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_upload, "تحديث الآن 🚀", updatePendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (t: Throwable) {
            // Silently fallback if notifications fail
        }
    }
}

/**
 * Top Notification Banner displayed inside the app when a new update is released.
 */
@Composable
fun AppUpdateBanner(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val showBanner by viewModel.showUpdateBanner.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()

    AnimatedVisibility(
        visible = showBanner && updateInfo.isUpdateAvailable,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C12)),
            border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Glowing Icon
                Surface(
                    shape = CircleShape,
                    color = GoldPrimary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🚀", fontSize = 20.sp)
                    }
                }

                // Text Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "إشعار إطلاق تحديث جديد!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GoldPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = updateInfo.latestVersion,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = GoldPrimary
                            )
                        }
                    }
                    Text(
                        text = "تشغيل المنصة بكامل أدواتها + استوديوهات الذكاء الاصطناعي",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Update Action Button
                Button(
                    onClick = { viewModel.openUpdateDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "تحديث 🚀",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Dismiss Button
                IconButton(
                    onClick = { viewModel.dismissUpdateBanner() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق الإشعار",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modern In-App Update Dialog with full details, release notes, and update action.
 */
@Composable
fun AppUpdateDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val updateInfo by viewModel.updateInfo.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13131D)),
            border = BorderStroke(1.5.dp, GoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Icon & Badges
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldPrimary.copy(alpha = 0.35f), Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldPrimary,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🚀", fontSize = 28.sp)
                        }
                    }
                }

                // Dialog Title
                Text(
                    text = "تحديث جديد متاح للتطبيق! 🎉",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Version Comparison Tag
                Row(
                    modifier = Modifier
                        .background(Color(0xFF1F1F2F), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الإصدار الحالي: v${updateInfo.currentVersion}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "الإصدار الجديد: v${updateInfo.latestVersion}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                // What's New Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✨ أبرز ما جاء في التحديث الجديد:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GoldPrimary
                    )
                }

                // Features List
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1A1A26),
                    border = BorderStroke(1.dp, Color(0xFF2B2B3D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(updateInfo.features) { feature ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Text(
                                    text = feature,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Developer Signature Note
                Text(
                    text = "برمجة وتطوير المطور محمد الحزمي 🇸🇦",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary Action: Open Update Window / Platform In-App
                    Button(
                        onClick = {
                            onDismiss()
                            Toast.makeText(context, "جاري فتح صفحة التحديث والمنصة التفاعلية...", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(AppScreen.Platform)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحديث وتشغيل المنصة الآن 🚀",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Secondary Action: Open External Browser Link
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.updateUrl))
                                context.startActivity(intent)
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح المتصفح الخارجي", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فتح رابط صفحة التحديث بالمتصفح 🌐",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Dismiss Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "تذكيري لاحقاً ⏰",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
