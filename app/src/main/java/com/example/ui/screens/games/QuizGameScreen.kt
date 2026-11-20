package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
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
fun QuizGameScreen(
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onFinished: (earnedPoints: Int, score: Int, won: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val solomonColors = LocalSolomonColors.current
    var roundKey by remember { mutableIntStateOf(0) }
    val questions = remember(roundKey) {
        DiscoveryRepository.quizQuestions.shuffled().take(6)
    }

    var currentIndex by remember(roundKey) { mutableIntStateOf(0) }
    var selectedOption by remember(roundKey) { mutableStateOf<Int?>(null) }
    var correctCount by remember(roundKey) { mutableIntStateOf(0) }
    var streak by remember(roundKey) { mutableIntStateOf(0) }
    var maxStreak by remember(roundKey) { mutableIntStateOf(0) }
    var isFinished by remember(roundKey) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        GameTopHeader(
            title = "تحدي أسئلة",
            subtitle = if (isFinished) "انتهت الجولة" else "السؤال ${currentIndex + 1} من ${questions.size}",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!isFinished) {
            val q = questions[currentIndex]
            val quizNarration = remember(q.id, selectedOption) {
                val base = "السؤال: ${q.question} الاختيارات: ${q.options.joinToString("، ")}"
                if (selectedOption != null) "$base. التفسير: ${q.explanation}" else base
            }
            VoiceReaderPanel(
                textToRead = quizNarration,
                sourceId = "quiz_${q.id}",
                speechState = speechState,
                speechController = speechController,
                showHighlightedPreview = true,
                titleLabel = "استمع للسؤال"
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size },
                color = solomonColors.goldBright,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الإجابات الصحيحة: $correctCount",
                    style = MaterialTheme.typography.labelLarge,
                    color = solomonColors.emerald
                )
                Text(
                    text = "سلسلة متتالية: $streak 🔥",
                    style = MaterialTheme.typography.labelLarge,
                    color = solomonColors.goldBright
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, solomonColors.subtleBorder, RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = q.question,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            q.options.forEachIndexed { idx, optionText ->
                val answered = selectedOption != null
                val isSelected = selectedOption == idx
                val isCorrect = idx == q.correctIndex

                val bgColor = when {
                    !answered -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> solomonColors.emerald.copy(alpha = 0.2f)
                    isSelected -> solomonColors.coral.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }
                val borderColor = when {
                    !answered -> solomonColors.subtleBorder
                    isCorrect -> solomonColors.emerald
                    isSelected -> solomonColors.coral
                    else -> solomonColors.subtleBorder
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                        .clickable(enabled = !answered) {
                            selectedOption = idx
                            if (idx == q.correctIndex) {
                                correctCount++
                                streak++
                                if (streak > maxStreak) maxStreak = streak
                            } else {
                                streak = 0
                            }
                        }
                        .padding(16.dp)
                        .testTag("quiz_option_$idx"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (answered && (isCorrect || isSelected)) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                            contentDescription = null,
                            tint = if (isCorrect) solomonColors.emerald else solomonColors.coral
                        )
                    }
                }
            }

            AnimatedVisibility(visible = selectedOption != null) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = solomonColors.gold.copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = q.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (currentIndex < questions.lastIndex) {
                                currentIndex++
                                selectedOption = null
                            } else {
                                isFinished = true
                                val earned = correctCount * 10 + if (maxStreak >= 3) 10 else 0
                                val won = correctCount >= 4
                                onFinished(earned, correctCount, won)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("quiz_next_button")
                    ) {
                        Text(
                            text = if (currentIndex < questions.lastIndex) "السؤال التالي" else "عرض النتيجة",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            val earned = correctCount * 10 + if (maxStreak >= 3) 10 else 0
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, solomonColors.gold.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = solomonColors.goldBright,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (correctCount >= 4) "أداء باهر يا مستكشف!" else "محاولة جيدة!",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "أجبت بشكل صحيح على $correctCount من ${questions.size} أسئلة",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "+$earned نقطة مضافة إلى رصيدك",
                        style = MaterialTheme.typography.titleMedium,
                        color = solomonColors.goldBright,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { roundKey++ },
                            colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("جولة جديدة", fontWeight = FontWeight.Bold)
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
    }
}

@Composable
fun GameTopHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    val solomonColors = LocalSolomonColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("game_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "رجوع"
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = solomonColors.goldBright
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
