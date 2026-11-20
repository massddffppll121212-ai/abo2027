package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.OfflineSpeechController
import com.example.audio.SpeechUiState
import com.example.model.GameType
import com.example.model.UserProgressState
import com.example.ui.components.QuickSpeakerButton
import com.example.ui.screens.games.MemoryGameScreen
import com.example.ui.screens.games.QuizGameScreen
import com.example.ui.screens.games.SecretNumberGameScreen
import com.example.ui.screens.games.SolomonsPuzzleScreen
import com.example.ui.theme.LocalSolomonColors

@Composable
fun PlayScreen(
    activeGame: GameType?,
    progress: UserProgressState,
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onOpenGame: (GameType) -> Unit,
    onCloseGame: () -> Unit,
    onQuizFinished: (earnedPoints: Int, score: Int, won: Boolean) -> Unit,
    onMemoryFinished: (earnedPoints: Int, moves: Int) -> Unit,
    onSolomonStageCleared: (earnedPoints: Int, stageNumber: Int) -> Unit,
    onSecretNumberCracked: (earnedPoints: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (activeGame) {
        GameType.QUIZ -> QuizGameScreen(
            speechState = speechState,
            speechController = speechController,
            onFinished = onQuizFinished,
            onBack = onCloseGame,
            modifier = modifier
        )
        GameType.MEMORY -> MemoryGameScreen(
            speechState = speechState,
            speechController = speechController,
            onFinished = onMemoryFinished,
            onBack = onCloseGame,
            modifier = modifier
        )
        GameType.SOLOMON_PUZZLE -> SolomonsPuzzleScreen(
            initialClearedStage = progress.solomonStagesCleared,
            speechState = speechState,
            speechController = speechController,
            onStageCleared = onSolomonStageCleared,
            onBack = onCloseGame,
            modifier = modifier
        )
        GameType.SECRET_NUMBER -> SecretNumberGameScreen(
            speechState = speechState,
            speechController = speechController,
            onCodeCracked = onSecretNumberCracked,
            onBack = onCloseGame,
            modifier = modifier
        )
        null -> PlayHubList(
            progress = progress,
            speechState = speechState,
            speechController = speechController,
            onOpenGame = onOpenGame,
            modifier = modifier
        )
    }
}

@Composable
private fun PlayHubList(
    progress: UserProgressState,
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onOpenGame: (GameType) -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "ساحة الألعاب والتحديات",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "العب واجمع النقاط لترتقي من مستكشف صغير إلى سيد الخاتم",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(GameType.entries) { game ->
            val accent = gameAccentColor(game)
            val icon = gameIcon(game)
            val recordBadge = when (game) {
                GameType.QUIZ -> if (progress.quizBestScore > 0) "أفضل نتيجة: ${progress.quizBestScore}/6" else "جاهز للتحدي"
                GameType.MEMORY -> if (progress.memoryBestMoves > 0) "أفضل رقم: ${progress.memoryBestMoves} حركة" else "6 أزواج ذهبية"
                GameType.SOLOMON_PUZZLE -> "المراحل المحلولة: ${progress.solomonStagesCleared}/5"
                GameType.SECRET_NUMBER -> "الرموز المكسورة: ${progress.secretCodesCracked}"
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onOpenGame(game) }
                    .testTag("game_card_${game.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = game.titleAr,
                            tint = accent,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.titleAr,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = game.subtitleAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = game.rewardText,
                                style = MaterialTheme.typography.labelSmall,
                                color = solomonColors.goldBright,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(solomonColors.gold.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                            Text(
                                text = recordBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accent.copy(alpha = 0.14f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    val gameCardSourceId = "hub_game_${game.name.lowercase()}"
                    val isReadingGame = speechState.activeSourceId == gameCardSourceId && speechState.isSpeaking
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSpeakerButton(
                            isActive = isReadingGame,
                            onClick = {
                                if (isReadingGame) {
                                    speechController.stop()
                                } else {
                                    speechController.speak(
                                        "${game.titleAr}. ${game.subtitleAr}. الجائزة: ${game.rewardText}.",
                                        gameCardSourceId
                                    )
                                }
                            },
                            modifier = Modifier.testTag("speak_game_${game.name.lowercase()}")
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "ابدأ اللعب",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun gameAccentColor(game: GameType): Color {
    val c = LocalSolomonColors.current
    return when (game) {
        GameType.QUIZ -> c.goldBright
        GameType.MEMORY -> c.emerald
        GameType.SOLOMON_PUZZLE -> c.amethyst
        GameType.SECRET_NUMBER -> c.sapphire
    }
}

private fun gameIcon(game: GameType): ImageVector = when (game) {
    GameType.QUIZ -> Icons.Rounded.Quiz
    GameType.MEMORY -> Icons.Rounded.GridView
    GameType.SOLOMON_PUZZLE -> Icons.Rounded.AutoAwesome
    GameType.SECRET_NUMBER -> Icons.Rounded.Dialpad
}
