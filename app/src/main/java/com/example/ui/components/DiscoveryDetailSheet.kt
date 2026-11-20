package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.OfflineSpeechController
import com.example.audio.SpeechUiState
import com.example.audio.buildHighlightedText
import com.example.model.DiscoveryItem
import com.example.model.MindPuzzleType
import com.example.model.OpticalIllusionType
import com.example.ui.theme.LocalSolomonColors
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryDetailSheet(
    item: DiscoveryItem,
    isFromRandomRing: Boolean,
    isFavorite: Boolean,
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onToggleFavorite: () -> Unit,
    onAnotherRandom: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val solomonColors = LocalSolomonColors.current
    val sourceId = "discovery_${item.id}"
    val fullText = remember(item.id) { item.fullNarrationText() }
    val isThisReading = speechState.activeSourceId == sourceId
    val globalRange = if (isThisReading) speechState.activeRange else null

    // Compute exact slice offsets inside fullNarrationText(): "$title. $subtitle. $summary ${facts.joinToString(" ")}"
    val titleStart = 0
    val subtitleStart = item.title.length + 2
    val summaryStart = subtitleStart + item.subtitle.length + 2
    val factStarts = remember(item.id) {
        val list = mutableListOf<Int>()
        var cursor = summaryStart + item.summary.length + 1
        item.facts.forEach { f ->
            list.add(cursor)
            cursor += f.length + 1
        }
        list
    }

    fun localHighlightRange(segmentStart: Int, segmentLength: Int): IntRange? {
        val g = globalRange ?: return null
        val segmentEnd = segmentStart + segmentLength
        if (g.last < segmentStart || g.first >= segmentEnd) return null
        val s = (g.first - segmentStart).coerceIn(0, segmentLength - 1)
        val e = (g.last - segmentStart).coerceIn(s, segmentLength - 1)
        return s..e
    }

    val goldFlashAlpha = remember { Animatable(0f) }
    LaunchedEffect(speechState.goldFlashCounter) {
        if (speechState.goldFlashCounter > 0) {
            goldFlashAlpha.snapTo(0.65f)
            goldFlashAlpha.animateTo(0f, animationSpec = tween(750))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Top bar with optional Random Ring Banner & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFromRandomRing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(solomonColors.gold.copy(alpha = 0.2f))
                            .border(1.dp, solomonColors.gold.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = solomonColors.goldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اكتشاف عشوائي من الخاتم",
                            style = MaterialTheme.typography.labelMedium,
                            color = solomonColors.goldBright
                        )
                    }
                } else {
                    Text(
                        text = item.category.titleAr,
                        style = MaterialTheme.typography.labelLarge,
                        color = solomonColors.gold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_discovery_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "إغلاق"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smart Offline Canvas Artist (تلقائي، واقعي، خيالي، قلم رصاص + ارسم واقرأ مع بعض)
            SmartArtistStudio(
                item = item,
                fullNarrationText = fullText,
                sourceId = sourceId,
                speechController = speechController,
                autoLiveOnOpen = isFromRandomRing
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Voice Reader Panel (اسمع، وقف، توقف، سرعات، أصوات)
            VoiceReaderPanel(
                textToRead = fullText,
                sourceId = sourceId,
                speechState = speechState,
                speechController = speechController,
                showHighlightedPreview = false,
                titleLabel = "استمع إلى القصة والاكتشاف"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = buildHighlightedText(item.title, localHighlightRange(titleStart, item.title.length)),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildHighlightedText(item.subtitle, localHighlightRange(subtitleStart, item.subtitle.length)),
                style = MaterialTheme.typography.titleSmall,
                color = solomonColors.gold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location / Origin & Verification chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(icon = Icons.Rounded.LocationOn, text = item.locationOrOrigin)
                InfoChip(icon = Icons.Rounded.Verified, text = item.badgeTag)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildHighlightedText(item.summary, localHighlightRange(summaryStart, item.summary.length)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Interactive Optical Illusion Demo
            if (item.illusionType != OpticalIllusionType.NONE) {
                Spacer(modifier = Modifier.height(16.dp))
                OpticalIllusionInteractiveLab(type = item.illusionType)
            }

            // Interactive Mind Riddle Demo
            if (item.mindPuzzleType != MindPuzzleType.NONE) {
                Spacer(modifier = Modifier.height(16.dp))
                MindPuzzleInteractiveLab(type = item.mindPuzzleType)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "حقائق موثقة",
                style = MaterialTheme.typography.titleMedium,
                color = solomonColors.goldBright
            )
            Spacer(modifier = Modifier.height(8.dp))

            item.facts.forEachIndexed { index, fact ->
                val fStart = factStarts[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = solomonColors.emerald,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = buildHighlightedText(fact, localHighlightRange(fStart, fact.length)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "المصدر: ${item.referenceNote}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) solomonColors.emerald else solomonColors.gold,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("favorite_discovery_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFavorite) "محفوظة في مجموعتي" else "حفظ في مجموعتي",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isFromRandomRing) {
                    OutlinedButton(
                        onClick = onAnotherRandom,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("another_random_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "اكتشاف آخر"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اكتشاف آخر")
                    }
                }
            }
        }
        if (goldFlashAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFDE047).copy(alpha = goldFlashAlpha.value),
                                solomonColors.gold.copy(alpha = goldFlashAlpha.value * 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    val solomonColors = LocalSolomonColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, solomonColors.subtleBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = solomonColors.gold,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun OpticalIllusionInteractiveLab(type: OpticalIllusionType) {
    val solomonColors = LocalSolomonColors.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, solomonColors.gold.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "مختبر الخدعة البصرية التفاعلي",
                style = MaterialTheme.typography.titleSmall,
                color = solomonColors.goldBright
            )
            Spacer(modifier = Modifier.height(10.dp))

            when (type) {
                OpticalIllusionType.MULLER_LYER -> {
                    var showGuideLines by remember { mutableStateOf(false) }
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0E1525))
                    ) {
                        val leftX = size.width * 0.25f
                        val rightX = size.width * 0.75f
                        val topY = size.height * 0.32f
                        val bottomY = size.height * 0.68f
                        val fin = 24f

                        // Top line with outward fins (>----<)
                        drawLine(Color(0xFFF9DE7D), Offset(leftX, topY), Offset(rightX, topY), strokeWidth = 6f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(leftX, topY), Offset(leftX - fin, topY - fin), strokeWidth = 5f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(leftX, topY), Offset(leftX - fin, topY + fin), strokeWidth = 5f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(rightX, topY), Offset(rightX + fin, topY - fin), strokeWidth = 5f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(rightX, topY), Offset(rightX + fin, topY + fin), strokeWidth = 5f, cap = StrokeCap.Round)

                        // Bottom line with inward fins (<---->)
                        drawLine(Color(0xFFF9DE7D), Offset(leftX, bottomY), Offset(rightX, bottomY), strokeWidth = 6f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(leftX, bottomY), Offset(leftX + fin, bottomY - fin), strokeWidth = 5f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(leftX, bottomY), Offset(leftX + fin, bottomY + fin), strokeWidth = 5f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(rightX, bottomY), Offset(rightX - fin, bottomY - fin), strokeWidth = 5f, cap = StrokeCap.Round)
                        drawLine(Color(0xFFF9DE7D), Offset(rightX, bottomY), Offset(rightX - fin, bottomY + fin), strokeWidth = 5f, cap = StrokeCap.Round)

                        if (showGuideLines) {
                            drawLine(Color(0xFF2DD4BF), Offset(leftX, 16f), Offset(leftX, size.height - 16f), strokeWidth = 3f)
                            drawLine(Color(0xFF2DD4BF), Offset(rightX, 16f), Offset(rightX, size.height - 16f), strokeWidth = 3f)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showGuideLines) "لاحظ أن الخطين متطابقان تماماً!" else "أظهر خطوط القياس للتحقق",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(checked = showGuideLines, onCheckedChange = { showGuideLines = it })
                    }
                }

                OpticalIllusionType.HERMANN_GRID -> {
                    Text(
                        text = "حرّك نظرك عبر الشبكة ولاحظ ظهور نقاط رمادية شبحية عند التقاطعات البيضاء!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                    ) {
                        val cols = 5
                        val rows = 4
                        val gap = 14f
                        val boxW = (size.width - gap * (cols + 1)) / cols
                        val boxH = (size.height - gap * (rows + 1)) / rows
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                drawRect(
                                    color = Color(0xFF0E1525),
                                    topLeft = Offset(gap + c * (boxW + gap), gap + r * (boxH + gap)),
                                    size = Size(boxW, boxH)
                                )
                            }
                        }
                    }
                }

                OpticalIllusionType.SIMULTANEOUS_CONTRAST -> {
                    var mergeCenters by remember { mutableStateOf(false) }
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        val halfW = size.width / 2f
                        drawRect(color = Color(0xFF141824), topLeft = Offset.Zero, size = Size(halfW, size.height))
                        drawRect(color = Color(0xFFF3EFE6), topLeft = Offset(halfW, 0f), size = Size(halfW, size.height))

                        val innerSize = size.height * 0.42f
                        val topY = (size.height - innerSize) / 2f
                        val sameGray = Color(0xFF888C96)
                        if (mergeCenters) {
                            drawRect(
                                color = sameGray,
                                topLeft = Offset(halfW - innerSize, topY),
                                size = Size(innerSize * 2f, innerSize)
                            )
                        } else {
                            drawRect(
                                color = sameGray,
                                topLeft = Offset(halfW * 0.5f - innerSize / 2f, topY),
                                size = Size(innerSize, innerSize)
                            )
                            drawRect(
                                color = sameGray,
                                topLeft = Offset(halfW * 1.5f - innerSize / 2f, topY),
                                size = Size(innerSize, innerSize)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (mergeCenters) "اللون الرمادي متطابق 100%!" else "ادمج المربعين لإثبات التطابق",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(checked = mergeCenters, onCheckedChange = { mergeCenters = it })
                    }
                }

                OpticalIllusionType.NONE -> Unit
            }
        }
    }
}

@Composable
private fun MindPuzzleInteractiveLab(type: MindPuzzleType) {
    val solomonColors = LocalSolomonColors.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, solomonColors.emerald.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "تجربة عقلية سريعة",
                style = MaterialTheme.typography.titleSmall,
                color = solomonColors.emerald
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (type) {
                MindPuzzleType.MONTY_HALL -> {
                    var switchWins by remember { mutableIntStateOf(0) }
                    var stayWins by remember { mutableIntStateOf(0) }
                    var totalTrials by remember { mutableIntStateOf(0) }

                    Text(
                        text = "اختبر 100 جولة عشوائية لمقارنة البقاء مقابل التبديل:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            var sw = 0
                            var st = 0
                            repeat(100) {
                                val prize = Random.nextInt(3)
                                val choice = Random.nextInt(3)
                                if (choice == prize) st++ else sw++
                            }
                            switchWins += sw
                            stayWins += st
                            totalTrials += 100
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = solomonColors.emerald)
                    ) {
                        Text("تشغيل 100 جولة محاكاة", fontWeight = FontWeight.Bold)
                    }
                    AnimatedVisibility(visible = totalTrials > 0) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            val switchPct = if (totalTrials == 0) 0 else (switchWins * 100f / totalTrials).roundToInt()
                            val stayPct = if (totalTrials == 0) 0 else (stayWins * 100f / totalTrials).roundToInt()
                            Text(
                                text = "إجمالي الجولات: $totalTrials",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "• الفوز عند التبديل: $switchWins ($switchPct%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = solomonColors.emerald,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• الفوز عند البقاء: $stayWins ($stayPct%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = solomonColors.coral
                            )
                        }
                    }
                }

                MindPuzzleType.BIRTHDAY_PARADOX -> {
                    var peopleCount by remember { mutableFloatStateOf(23f) }
                    val n = peopleCount.roundToInt()
                    val probability = remember(n) {
                        var noMatch = 1.0
                        for (i in 0 until n) {
                            noMatch *= (365.0 - i) / 365.0
                        }
                        ((1.0 - noMatch) * 100.0).roundToInt()
                    }
                    Text(
                        text = "عدد الأشخاص في القاعة: $n شخصاً",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = peopleCount,
                        onValueChange = { peopleCount = it },
                        valueRange = 2f..70f
                    )
                    Text(
                        text = "احتمال تطابق عيد ميلاد شخصين على الأقل: $probability%",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (probability >= 50) solomonColors.emerald else solomonColors.goldBright
                    )
                }

                MindPuzzleType.ZEIGARNIK -> {
                    var completedTask by remember { mutableStateOf(false) }
                    Text(
                        text = if (completedTask) {
                            "أغلقت المهمة! الآن يخفض دماغك مستوى التوتر المعرفي تجاهها."
                        } else {
                            "لديك الآن لغز ذهني مفتوح: ما العدد التالي في المتتالية (2، 6، 12، 20، ...؟)"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { completedTask = !completedTask }) {
                        Text(if (completedTask) "إعادة فتح المهمة الذهنية" else "اكشف الحل وأغلق المهمة (30)")
                    }
                }

                MindPuzzleType.NONE -> Unit
            }
        }
    }
}
