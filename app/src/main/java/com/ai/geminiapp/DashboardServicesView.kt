package com.ai.geminiapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardServicesView(
    onToolSelected: (ServiceToolItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ================= 1. CODE STUDIO =================
        DashboardSectionCard(
            sectionTitle = "استوديو كتابة وهندسة الأكواد البرمجية",
            sectionSubtitle = "أدوات متقدمة لتوليد الأكواد، الفحص والتصحيح (Debugging)، وشرح وتفكيك المنطق البرمجي",
            badgeText = "5 أدوات برمجية",
            iconEmoji = "💻",
            accentColor = Color(0xFF64B5F6),
            tools = ServiceCatalog.codeTools,
            onToolSelected = onToolSelected
        )

        // ================= 2. IMAGE STUDIO =================
        DashboardSectionCard(
            sectionTitle = "استوديو تصميم وتوليد وفحص الصور",
            sectionSubtitle = "صياغة برومبتات بصرية واقعية، فحص وتحليل الصور، ابتكار الشعارات، وتفريغ النصوص OCR",
            badgeText = "4 أدوات بصرية",
            iconEmoji = "🎨",
            accentColor = Color(0xFFFFB74D),
            tools = ServiceCatalog.imageTools,
            onToolSelected = onToolSelected
        )

        // ================= 3. AUDIO & MUSIC STUDIO =================
        DashboardSectionCard(
            sectionTitle = "استوديو الصوت والموسيقى والبودكاست",
            sectionSubtitle = "القارئ الصوتي الذكي (TTS)، المحادثة الصوتية الحية، تأليف وتلحين الأغاني، وسيناريو البودكاست",
            badgeText = "4 أدوات صوتية",
            iconEmoji = "🎵",
            accentColor = Color(0xFF81C784),
            tools = ServiceCatalog.audioTools,
            onToolSelected = onToolSelected
        )
    }
}

@Composable
fun DashboardSectionCard(
    sectionTitle: String,
    sectionSubtitle: String,
    badgeText: String,
    iconEmoji: String,
    accentColor: Color,
    tools: List<ServiceToolItem>,
    onToolSelected: (ServiceToolItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = iconEmoji, fontSize = 22.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sectionTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = sectionSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Tool Items in Vertical Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tools.forEach { tool ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(0.7.dp, Color.Gray.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToolSelected(tool) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = tool.iconEmoji, fontSize = 24.sp)

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tool.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tool.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Button(
                                onClick = { onToolSelected(tool) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "استخدام الأداة 🚀",
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
