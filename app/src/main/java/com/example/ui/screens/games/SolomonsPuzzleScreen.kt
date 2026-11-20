package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audio.OfflineSpeechController
import com.example.audio.SpeechUiState
import com.example.data.DiscoveryRepository
import com.example.ui.components.VoiceReaderPanel
import com.example.ui.theme.LocalSolomonColors

@Composable
fun SolomonsPuzzleScreen(
    initialClearedStage: Int,
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onStageCleared: (earnedPoints: Int, stageNumber: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val solomonColors = LocalSolomonColors.current
    val stages = DiscoveryRepository.solomonStages
    var stageIndex by remember {
        mutableIntStateOf(initialClearedStage.coerceIn(0, stages.lastIndex))
    }
    val stage = stages[stageIndex]

    var outerIdx by remember(stageIndex) {
        mutableIntStateOf((stage.targetOuterIndex + 2) % stage.outerRingOptions.size)
    }
    var middleIdx by remember(stageIndex) {
        mutableIntStateOf((stage.targetMiddleIndex + 1) % stage.middleRingOptions.size)
    }
    var innerIdx by remember(stageIndex) {
        mutableIntStateOf((stage.targetInnerIndex + 3) % stage.innerRingOptions.size)
    }
    var wrongFeedback by remember(stageIndex) { mutableStateOf(false) }
    var stageSolved by remember(stageIndex) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        GameTopHeader(
            title = "لغز سليمان",
            subtitle = "المرحلة ${stage.stageNumber} من ${stages.size} • ${stage.title}",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        val puzzleNarration = remember(stage.stageNumber, stageSolved) {
            if (stageSolved) {
                "${stage.title}. ${stage.clue} الحل: ${stage.explanation}"
            } else {
                "${stage.title}. ${stage.clue}"
            }
        }
        VoiceReaderPanel(
            textToRead = puzzleNarration,
            sourceId = "solomon_stage_${stage.stageNumber}",
            speechState = speechState,
            speechController = speechController,
            showHighlightedPreview = true,
            titleLabel = "استمع للغز المرحلة"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Concentric Golden Rings Visual Dial
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, solomonColors.gold.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(168.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val outerRingColor = if (stageSolved) solomonColors.emerald else solomonColors.gold
                    val midRingColor = if (stageSolved) solomonColors.emerald else solomonColors.goldBright
                    val innerRingColor = if (stageSolved) solomonColors.emerald else solomonColors.amber

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val maxR = size.minDimension / 2f
                        drawCircle(
                            color = outerRingColor.copy(alpha = 0.55f),
                            radius = maxR * 0.92f,
                            center = c,
                            style = Stroke(width = 6f)
                        )
                        drawCircle(
                            color = midRingColor.copy(alpha = 0.7f),
                            radius = maxR * 0.68f,
                            center = c,
                            style = Stroke(width = 6f)
                        )
                        drawCircle(
                            color = innerRingColor,
                            radius = maxR * 0.44f,
                            center = c,
                            style = Stroke(width = 6f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stage.outerRingOptions[outerIdx],
                            style = MaterialTheme.typography.labelMedium,
                            color = solomonColors.goldBright
                        )
                        Text(
                            text = stage.middleRingOptions[middleIdx],
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stage.innerRingOptions[innerIdx],
                            style = MaterialTheme.typography.labelMedium,
                            color = solomonColors.emerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stage.clue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        RingSelectorRow(
            ringLabel = "الحلقة الخارجية",
            selectedValue = stage.outerRingOptions[outerIdx],
            accent = solomonColors.gold,
            enabled = !stageSolved,
            onPrev = {
                wrongFeedback = false
                outerIdx = (outerIdx - 1 + stage.outerRingOptions.size) % stage.outerRingOptions.size
            },
            onNext = {
                wrongFeedback = false
                outerIdx = (outerIdx + 1) % stage.outerRingOptions.size
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        RingSelectorRow(
            ringLabel = "الحلقة الوسطى",
            selectedValue = stage.middleRingOptions[middleIdx],
            accent = solomonColors.goldBright,
            enabled = !stageSolved,
            onPrev = {
                wrongFeedback = false
                middleIdx = (middleIdx - 1 + stage.middleRingOptions.size) % stage.middleRingOptions.size
            },
            onNext = {
                wrongFeedback = false
                middleIdx = (middleIdx + 1) % stage.middleRingOptions.size
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        RingSelectorRow(
            ringLabel = "الحلقة الداخلية",
            selectedValue = stage.innerRingOptions[innerIdx],
            accent = solomonColors.emerald,
            enabled = !stageSolved,
            onPrev = {
                wrongFeedback = false
                innerIdx = (innerIdx - 1 + stage.innerRingOptions.size) % stage.innerRingOptions.size
            },
            onNext = {
                wrongFeedback = false
                innerIdx = (innerIdx + 1) % stage.innerRingOptions.size
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (wrongFeedback) {
            Text(
                text = "محاذاة الخاتم غير مكتملة بعد! راجع الدليل وحرّك الحلقات.",
                style = MaterialTheme.typography.bodyMedium,
                color = solomonColors.coral,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        AnimatedVisibility(visible = stageSolved) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = solomonColors.emerald.copy(alpha = 0.18f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, solomonColors.emerald, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = solomonColors.emerald
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تم فك ختم المرحلة ${stage.stageNumber}! (+35 نقطة)",
                            style = MaterialTheme.typography.titleMedium,
                            color = solomonColors.emerald
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stage.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!stageSolved) {
            Button(
                onClick = {
                    val matched = outerIdx == stage.targetOuterIndex &&
                        middleIdx == stage.targetMiddleIndex &&
                        innerIdx == stage.targetInnerIndex
                    if (matched) {
                        stageSolved = true
                        wrongFeedback = false
                        onStageCleared(35, stage.stageNumber)
                    } else {
                        wrongFeedback = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("solomon_verify_button")
            ) {
                Icon(Icons.Rounded.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تحقق من قفل الخاتم", fontWeight = FontWeight.Bold)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (stageIndex < stages.lastIndex) {
                    Button(
                        onClick = { stageIndex++ },
                        colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("المرحلة التالية", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { stageIndex = 0 },
                        colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إعادة من المرحلة 1", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("قائمة الألعاب")
                }
            }
        }
    }
}

@Composable
private fun RingSelectorRow(
    ringLabel: String,
    selectedValue: String,
    accent: Color,
    enabled: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = ringLabel,
            style = MaterialTheme.typography.labelLarge,
            color = accent,
            modifier = Modifier.padding(start = 6.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, enabled = enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "السابق"
                )
            }
            Text(
                text = selectedValue,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(110.dp),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNext, enabled = enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "التالي"
                )
            }
        }
    }
}
