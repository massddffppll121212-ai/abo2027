package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.OfflineBolt
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.model.ExplorerRank
import com.example.model.UserProgressState
import com.example.ui.theme.LocalSolomonColors
import java.io.File

@Composable
fun AccountScreen(
    progress: UserProgressState,
    totalDiscoveries: Int,
    onUpdateName: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    val context = LocalContext.current
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val rank = progress.rank
    val nextPoints = rank.nextPoints
    val progressFraction = if (nextPoints == null) {
        1f
    } else {
        val span = (nextPoints - rank.minPoints).coerceAtLeast(1)
        ((progress.points - rank.minPoints).toFloat() / span).coerceIn(0f, 1f)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explorer Profile Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, solomonColors.gold.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    solomonColors.gold.copy(alpha = 0.16f),
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(solomonColors.gold.copy(alpha = 0.22f))
                                    .border(2.dp, solomonColors.goldBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (rank) {
                                        ExplorerRank.JUNIOR -> Icons.Rounded.Explore
                                        ExplorerRank.SENIOR -> Icons.Rounded.MilitaryTech
                                        ExplorerRank.MASTER -> Icons.Rounded.WorkspacePremium
                                    },
                                    contentDescription = rank.titleAr,
                                    tint = solomonColors.goldBright,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = progress.explorerName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = rank.titleAr,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = solomonColors.goldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditNameDialog = true },
                            modifier = Modifier.testTag("edit_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "تعديل الاسم",
                                tint = solomonColors.gold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = rank.subtitleAr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الرصيد الحالي: ${progress.points} نقطة",
                            style = MaterialTheme.typography.labelLarge,
                            color = solomonColors.goldBright
                        )
                        Text(
                            text = if (nextPoints != null) "المتبقي للرتبة التالية: ${(nextPoints - progress.points).coerceAtLeast(0)}" else "أعلى رتبة مكتملة!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = solomonColors.goldBright,
                        trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                    )
                }
            }
        }

        // Rank Ladder (مستكشف صغير، مستكشف كبير، سيد الخاتم)
        item {
            Text(
                text = "سُلّم المستويات والرتب",
                style = MaterialTheme.typography.titleMedium,
                color = solomonColors.goldBright
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExplorerRank.entries.forEach { level ->
                    val unlocked = progress.points >= level.minPoints
                    val isCurrent = rank == level
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) solomonColors.goldBright else solomonColors.subtleBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Stars,
                                    contentDescription = null,
                                    tint = if (unlocked) solomonColors.emerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = level.titleAr,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = level.subtitleAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${level.minPoints}+ نقطة",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isCurrent) solomonColors.goldBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Statistics Grid
        item {
            Text(
                text = "إحصائيات المستكشف",
                style = MaterialTheme.typography.titleMedium,
                color = solomonColors.goldBright
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        title = "الاكتشافات",
                        value = "${progress.discoveredIds.size} / $totalDiscoveries",
                        icon = Icons.Rounded.AutoAwesome,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "المحفوظة",
                        value = "${progress.favoriteIds.size}",
                        icon = Icons.Rounded.Bookmark,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        title = "انتصارات الألعاب",
                        value = "${progress.gamesWon}",
                        icon = Icons.Rounded.EmojiEvents,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "أفضل نتيجة أسئلة",
                        value = "${progress.quizBestScore}",
                        icon = Icons.Rounded.Stars,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        title = "أقل حركات ذاكرة",
                        value = if (progress.memoryBestMoves == 0) "—" else "${progress.memoryBestMoves}",
                        icon = Icons.Rounded.MilitaryTech,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "رموز سرية مكسورة",
                        value = "${progress.secretCodesCracked}",
                        icon = Icons.Rounded.LockOpen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Settings & Pure Offline Badge
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, solomonColors.subtleBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DarkMode,
                                contentDescription = null,
                                tint = solomonColors.gold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("المظهر الليلي الملكي", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = if (progress.isDarkTheme) "مفعل (أوبسيديان وذهب)" else "معطل (ورقي فاتح)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = progress.isDarkTheme,
                            onCheckedChange = { onToggleTheme() },
                            modifier = Modifier.testTag("theme_switch")
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(solomonColors.emerald.copy(alpha = 0.14f))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OfflineBolt,
                            contentDescription = null,
                            tint = solomonColors.emerald
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "يعمل بالكامل بدون إنترنت • بدون API • بدون حساب جوجل. جميع بياناتك محفوظة محلياً على جهازك.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = {
                            runCatching {
                                val outDir = File(context.cacheDir, "shared_art").apply { mkdirs() }
                                val outFile = File(outDir, "solomon-ring-v3.html")
                                context.assets.open("solomon-ring-v3.html").use { input ->
                                    outFile.outputStream().use { output -> input.copyTo(output) }
                                }
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    outFile
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/html"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(intent, "تصدير solomon-ring-v3.html")
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_html_button")
                    ) {
                        Icon(Icons.Rounded.Code, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير ملف solomon-ring-v3.html", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_progress_button")
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة ضبط النقاط والتقدم")
                    }
                }
            }
        }
    }

    if (showEditNameDialog) {
        var nameInput by remember { mutableStateOf(progress.explorerName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("اسم المستكشف") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    label = { Text("اكتب اسمك") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateName(nameInput)
                    showEditNameDialog = false
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("إعادة ضبط التقدم") },
            text = { Text("سيتم تصفير النقاط والاكتشافات وإحصائيات الألعاب على هذا الجهاز. هل أنت متأكد؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = solomonColors.amber)
                ) {
                    Text("تصفير التقدم")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("تراجع")
                }
            }
        )
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.border(1.dp, solomonColors.subtleBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = solomonColors.gold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
