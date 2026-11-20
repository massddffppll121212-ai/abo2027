package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.art.ArtStyleMode
import com.example.art.ArtworkExportHelper
import com.example.art.drawDiscoveryArtwork
import com.example.art.nextAlternative
import com.example.art.resolveEffectiveArtStyle
import com.example.audio.OfflineSpeechController
import com.example.model.DiscoveryItem
import com.example.ui.theme.LocalSolomonColors
import kotlinx.coroutines.launch

@Composable
fun SmartArtistStudio(
    item: DiscoveryItem,
    fullNarrationText: String,
    sourceId: String,
    speechController: OfflineSpeechController,
    autoLiveOnOpen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val solomonColors = LocalSolomonColors.current

    var selectedStyle by remember(item.id) { mutableStateOf(ArtStyleMode.AUTO) }
    var variationSeed by remember(item.id) { mutableIntStateOf(0) }
    var replayTrigger by remember(item.id) { mutableIntStateOf(0) }
    var liveEventActive by remember(item.id, autoLiveOnOpen) { mutableStateOf(autoLiveOnOpen) }
    var studioMessage by remember(item.id) { mutableStateOf<String?>(null) }

    val drawProgress = remember(item.id) { Animatable(0f) }
    val effectiveStyle = resolveEffectiveArtStyle(selectedStyle, item.category)

    LaunchedEffect(item.id, autoLiveOnOpen) {
        if (autoLiveOnOpen) {
            liveEventActive = true
            speechController.speak(fullNarrationText, sourceId)
            studioMessage = "الرسام والقارئ يعملان معاً — عيش الحدث!"
        }
    }

    LaunchedEffect(item.id, selectedStyle, variationSeed, replayTrigger) {
        drawProgress.snapTo(0f)
        val duration = if (liveEventActive) 6500 else 2400
        if (liveEventActive) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        )
        if (liveEventActive) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            liveEventActive = false
        }
    }

    // Extra haptic milestone pulses while "عيش الحدث" is painting
    LaunchedEffect(drawProgress.value, liveEventActive) {
        if (liveEventActive) {
            val v = drawProgress.value
            if (v in 0.34f..0.36f || v in 0.67f..0.69f) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, solomonColors.gold.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = solomonColors.goldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الرسام الذكي (Canvas)",
                        style = MaterialTheme.typography.titleSmall,
                        color = solomonColors.goldBright,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (selectedStyle == ArtStyleMode.AUTO) {
                        "تلقائي ← ${effectiveStyle.labelAr}"
                    } else {
                        effectiveStyle.labelAr
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = solomonColors.emerald,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(solomonColors.emerald.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smart Canvas Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, solomonColors.gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("smart_artist_canvas")
                ) {
                    drawDiscoveryArtwork(
                        item = item,
                        requestedStyle = selectedStyle,
                        progress = drawProgress.value,
                        variationSeed = variationSeed
                    )
                }
            }

            if (drawProgress.value < 0.99f) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { drawProgress.value },
                    color = solomonColors.goldBright,
                    trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Style Modes: تلقائي، واقعي، خيالي، قلم رصاص
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ArtStyleMode.entries.forEach { mode ->
                    val isSelected = selectedStyle == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            liveEventActive = false
                            selectedStyle = mode
                            studioMessage = mode.descriptionAr
                        },
                        label = {
                            Text(
                                text = mode.labelAr,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = solomonColors.gold.copy(alpha = 0.24f),
                            selectedLabelColor = solomonColors.goldBright
                        ),
                        modifier = Modifier.testTag("art_style_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hero Button: ارسم + اقرأ مع بعض - عيش الحدث
            Button(
                onClick = {
                    liveEventActive = true
                    replayTrigger++
                    speechController.speak(fullNarrationText, sourceId)
                    studioMessage = "جاري الرسم وسرد القصة معاً — عيش الحدث!"
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = solomonColors.emerald,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("draw_and_read_live_button")
            ) {
                Icon(Icons.Rounded.PlayCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🎨🔊 ارسم + اقرأ مع بعض - عيش الحدث",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: حفظ الرسم، مشاركة الرسم، ارسم بطريقة ثانية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val result = ArtworkExportHelper.saveArtworkToDevice(
                                context = context,
                                item = item,
                                style = selectedStyle,
                                variationSeed = variationSeed
                            )
                            studioMessage = result.getOrElse { "تعذر الحفظ: ${it.localizedMessage}" }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("save_artwork_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "حفظ الرسم",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("حفظ الرسم", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val result = ArtworkExportHelper.shareArtwork(
                                context = context,
                                item = item,
                                style = selectedStyle,
                                variationSeed = variationSeed
                            )
                            if (result.isFailure) {
                                studioMessage = "تعذر فتح المشاركة"
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("share_artwork_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "مشاركة الرسم",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("مشاركة الرسم", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = {
                    liveEventActive = false
                    selectedStyle = selectedStyle.nextAlternative(item.category)
                    variationSeed++
                    studioMessage = "تم تغيير أسلوب الرسم إلى: ${selectedStyle.labelAr}"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("draw_another_style_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Brush,
                    contentDescription = "ارسم بطريقة ثانية",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ارسم بطريقة ثانية",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = studioMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(solomonColors.gold.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = solomonColors.goldBright,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = studioMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
