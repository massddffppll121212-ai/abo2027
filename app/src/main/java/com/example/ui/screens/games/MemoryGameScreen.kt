package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.OfflineSpeechController
import com.example.audio.SpeechUiState
import com.example.data.DiscoveryRepository
import com.example.model.MemorySymbol
import com.example.ui.components.VoiceReaderPanel
import com.example.ui.theme.LocalSolomonColors
import kotlinx.coroutines.delay

private data class MemoryBoardTile(
    val uid: Int,
    val symbol: MemorySymbol,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun MemoryGameScreen(
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onFinished: (earnedPoints: Int, moves: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val solomonColors = LocalSolomonColors.current
    var roundKey by remember { mutableIntStateOf(0) }
    var moves by remember(roundKey) { mutableIntStateOf(0) }
    var isLocked by remember(roundKey) { mutableStateOf(false) }
    var isWon by remember(roundKey) { mutableStateOf(false) }

    val tiles = remember(roundKey) {
        val chosen = DiscoveryRepository.memorySymbols.shuffled().take(6)
        val paired = (chosen + chosen).shuffled()
        mutableStateListOf<MemoryBoardTile>().apply {
            paired.forEachIndexed { idx, sym ->
                add(MemoryBoardTile(uid = idx, symbol = sym))
            }
        }
    }

    val faceUpUnmatchedIndices = tiles.indices.filter { tiles[it].isFaceUp && !tiles[it].isMatched }

    LaunchedEffect(faceUpUnmatchedIndices.size, roundKey) {
        if (faceUpUnmatchedIndices.size == 2) {
            isLocked = true
            val first = faceUpUnmatchedIndices[0]
            val second = faceUpUnmatchedIndices[1]
            if (tiles[first].symbol.id == tiles[second].symbol.id) {
                delay(260)
                tiles[first] = tiles[first].copy(isMatched = true)
                tiles[second] = tiles[second].copy(isMatched = true)
                if (tiles.all { it.isMatched }) {
                    isWon = true
                    val earned = (45 - (moves - 6).coerceAtLeast(0) * 2).coerceAtLeast(20)
                    onFinished(earned, moves)
                }
            } else {
                delay(650)
                tiles[first] = tiles[first].copy(isFaceUp = false)
                tiles[second] = tiles[second].copy(isFaceUp = false)
            }
            isLocked = false
        }
    }

    val matchedPairs = tiles.count { it.isMatched } / 2

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        GameTopHeader(
            title = "لعبة ذاكرة",
            subtitle = "طابق الأزواج الستة بأقل عدد من الحركات",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        VoiceReaderPanel(
            textToRead = "أهلاً بك في لعبة الذاكرة الذهبية! افتح بطاقتين في كل مرة لمطابقة رموز الأخطبوط والأهرامات وسقطرى والنار والذهب بأقل عدد من الحركات. لقد طابقت حتى الآن $matchedPairs من 6 أزواج.",
            sourceId = "game_memory",
            speechState = speechState,
            speechController = speechController,
            showHighlightedPreview = true,
            titleLabel = "استمع لشرح اللعبة"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "الحركات: $moves",
                style = MaterialTheme.typography.titleSmall,
                color = solomonColors.goldBright
            )
            Text(
                text = "الأزواج المكتشفة: $matchedPairs / 6",
                style = MaterialTheme.typography.titleSmall,
                color = solomonColors.emerald
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isWon) {
            // 3 columns x 4 rows grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val tile = tiles[index]
                            MemoryTileCard(
                                tile = tile,
                                onClick = {
                                    if (!isLocked && !tile.isFaceUp && !tile.isMatched) {
                                        tiles[index] = tile.copy(isFaceUp = true)
                                        if (tiles.count { it.isFaceUp && !it.isMatched } == 2) {
                                            moves++
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("memory_tile_$index")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { roundKey++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("خلط البطاقات من جديد")
            }
        } else {
            val earned = (45 - (moves - 6).coerceAtLeast(0) * 2).coerceAtLeast(20)
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
                        text = "ذاكرة فولاذية!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "أنهيت المطابقة في $moves حركة فقط",
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
                            Text("لعب مجدداً", fontWeight = FontWeight.Bold)
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
    }
}

@Composable
private fun MemoryTileCard(
    tile: MemoryBoardTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    val revealed = tile.isFaceUp || tile.isMatched

    val scale by animateFloatAsState(
        targetValue = if (revealed) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.55f),
        label = "tile_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            tile.isMatched -> solomonColors.emerald.copy(alpha = 0.22f)
            tile.isFaceUp -> solomonColors.gold.copy(alpha = 0.22f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "tile_bg"
    )
    val borderColor = when {
        tile.isMatched -> solomonColors.emerald
        tile.isFaceUp -> solomonColors.goldBright
        else -> solomonColors.subtleBorder
    }

    Box(
        modifier = modifier
            .aspectRatio(0.95f)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = !revealed, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (revealed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tile.symbol.glyph,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tile.symbol.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "بطاقة مغلقة",
                tint = solomonColors.gold.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
