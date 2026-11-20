package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audio.OfflineSpeechController
import com.example.audio.SpeechUiState
import com.example.ui.components.VoiceReaderPanel
import com.example.ui.theme.LocalSolomonColors

private data class CodeAttempt(
    val guess: String,
    val exactMatches: Int,
    val misplacedMatches: Int
)

@Composable
fun SecretNumberGameScreen(
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onCodeCracked: (earnedPoints: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val solomonColors = LocalSolomonColors.current
    var codeLength by remember { mutableIntStateOf(3) }
    var roundKey by remember { mutableIntStateOf(0) }

    val secretCode = remember(codeLength, roundKey) {
        (0..9).shuffled().take(codeLength).joinToString("")
    }

    var currentInput by remember(codeLength, roundKey) { mutableStateOf("") }
    val attempts = remember(codeLength, roundKey) { mutableStateListOf<CodeAttempt>() }
    var statusMessage by remember(codeLength, roundKey) { mutableStateOf<String?>(null) }
    var gameState by remember(codeLength, roundKey) { mutableStateOf("playing") } // playing, won, lost
    val maxAttempts = 8

    fun submitGuess() {
        if (currentInput.length != codeLength) {
            statusMessage = "أدخل $codeLength أرقام مختلفة أولاً"
            return
        }
        if (currentInput.toSet().size != codeLength) {
            statusMessage = "يجب أن تكون الأرقام غير مكررة"
            return
        }
        statusMessage = null
        var exact = 0
        var misplaced = 0
        for (i in 0 until codeLength) {
            if (currentInput[i] == secretCode[i]) {
                exact++
            } else if (currentInput[i] in secretCode) {
                misplaced++
            }
        }
        attempts.add(0, CodeAttempt(currentInput, exact, misplaced))
        if (exact == codeLength) {
            gameState = "won"
            val reward = if (codeLength == 4) 50 else 35
            onCodeCracked(reward)
        } else if (attempts.size >= maxAttempts) {
            gameState = "lost"
        }
        currentInput = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        GameTopHeader(
            title = "الرقم السري",
            subtitle = "اكسر الرمز السري من أرقام غير مكررة خلال $maxAttempts محاولات",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        val secretNarration = remember(codeLength, attempts.size, gameState) {
            val latest = attempts.firstOrNull()
            if (latest == null) {
                "مرحباً بك في تحدي الرقم السري لخزينة الذهب! أدخل $codeLength أرقام غير مكررة لكسر القفل خلال $maxAttempts محاولات."
            } else {
                "آخر محاولة كانت ${latest.guess}. لديك ${latest.exactMatches} رقم صحيح في مكانه، و${latest.misplacedMatches} رقم صحيح في مكان آخر."
            }
        }
        VoiceReaderPanel(
            textToRead = secretNarration,
            sourceId = "game_secret_number",
            speechState = speechState,
            speechController = speechController,
            showHighlightedPreview = true,
            titleLabel = "استمع للتعليمات والنتيجة"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = codeLength == 3,
                onClick = { codeLength = 3 },
                label = { Text("3 أرقام (+35 نقطة)") }
            )
            FilterChip(
                selected = codeLength == 4,
                onClick = { codeLength = 4 },
                label = { Text("4 أرقام (+50 نقطة)") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Input Slots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until codeLength) {
                val char = currentInput.getOrNull(i)?.toString() ?: "•"
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, solomonColors.goldBright, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.headlineSmall,
                        color = solomonColors.goldBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = solomonColors.goldBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (gameState == "playing") {
            // Numeric Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("DEL", "0", "OK")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKeys.forEach { key ->
                            val isOk = key == "OK"
                            val isDel = key == "DEL"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isOk -> solomonColors.gold
                                            isDel -> solomonColors.coral.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .border(1.dp, solomonColors.subtleBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        when {
                                            isOk -> submitGuess()
                                            isDel -> if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1)
                                            else -> if (currentInput.length < codeLength && key !in currentInput) {
                                                currentInput += key
                                            }
                                        }
                                    }
                                    .testTag("secret_key_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isOk -> Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "تحقق",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    isDel -> Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                        contentDescription = "حذف",
                                        tint = solomonColors.coral
                                    )
                                    else -> Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val won = gameState == "won"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (won) solomonColors.emerald else solomonColors.coral,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (won) Icons.Rounded.EmojiEvents else Icons.Rounded.LockOpen,
                        contentDescription = null,
                        tint = if (won) solomonColors.goldBright else solomonColors.coral,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (won) "تم فتح الخزينة!" else "انتهت المحاولات!",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "الرقم السري كان: $secretCode",
                        style = MaterialTheme.typography.titleMedium,
                        color = solomonColors.goldBright
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { roundKey++ },
                            colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رقم جديد", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("رجوع")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "سجل المحاولات (${attempts.size}/$maxAttempts)",
            style = MaterialTheme.typography.titleSmall,
            color = solomonColors.goldBright
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (attempts.isEmpty()) {
            Text(
                text = "اختر أرقاماً غير مكررة واضغط ✓ لمعرفة عدد الأرقام الصحيحة في مكانها أو في غير مكانها.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                attempts.forEach { attempt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = attempt.guess,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "✓ مكانه صح: ${attempt.exactMatches}",
                                style = MaterialTheme.typography.labelSmall,
                                color = solomonColors.emerald,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(solomonColors.emerald.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "↔ مكان آخر: ${attempt.misplacedMatches}",
                                style = MaterialTheme.typography.labelSmall,
                                color = solomonColors.amber,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(solomonColors.amber.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
