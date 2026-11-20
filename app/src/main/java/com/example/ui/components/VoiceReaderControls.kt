package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.OfflineSpeechController
import com.example.audio.SpeechSpeed
import com.example.audio.SpeechUiState
import com.example.audio.VoicePersona
import com.example.audio.buildHighlightedText
import com.example.model.DiscoveryItem
import com.example.ui.theme.LocalSolomonColors

fun DiscoveryItem.fullNarrationText(): String {
    val factsJoined = facts.joinToString(separator = " ")
    return "$title. $subtitle. $summary $factsJoined"
}

@Composable
fun VoiceReaderPanel(
    textToRead: String,
    sourceId: String,
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    showHighlightedPreview: Boolean = true,
    titleLabel: String = "القارئ الصوتي العربي",
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    val isThisSource = speechState.activeSourceId == sourceId
    val isSpeakingThis = isThisSource && speechState.isSpeaking
    val isPausedThis = isThisSource && speechState.isPaused
    val isCompletedThis = isThisSource && speechState.isCompleted
    val isOutroThis = isThisSource && speechState.isOutroSpeaking

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSpeakingThis) solomonColors.goldBright else solomonColors.subtleBorder,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = solomonColors.goldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = titleLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = solomonColors.goldBright,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = when {
                        isOutroThis -> "انتهى النص..."
                        isSpeakingThis -> "يقرأ الآن"
                        isPausedThis -> "متوقف مؤقتاً"
                        isCompletedThis -> "تمت القراءة"
                        else -> "جاهز"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSpeakingThis) solomonColors.emerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sensory / Outro reaction banner
            AnimatedVisibility(visible = isThisSource && speechState.activeReactionLabel != null) {
                Text(
                    text = speechState.activeReactionLabel.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFDE047))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            // Primary controls: اسمع / وقف / توقف
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isPausedThis) {
                            speechController.resume()
                        } else {
                            speechController.speak(textToRead, sourceId)
                        }
                    },
                    enabled = !isSpeakingThis,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = solomonColors.gold,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tts_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "اسمع",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اسمع", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { speechController.pause() },
                    enabled = isSpeakingThis && !isOutroThis,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tts_pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = "وقف",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("وقف")
                }

                OutlinedButton(
                    onClick = { speechController.stop() },
                    enabled = isSpeakingThis || isPausedThis,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tts_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = "توقف",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("توقف")
                }
            }

            // Speed Selection: بطيء للأطفال الصغار، عادي، سريع
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = "السرعة",
                    tint = solomonColors.gold,
                    modifier = Modifier.size(16.dp)
                )
                SpeechSpeed.entries.forEach { speed ->
                    val selected = speechState.speed == speed
                    FilterChip(
                        selected = selected,
                        onClick = { speechController.setSpeed(speed) },
                        label = {
                            Text(
                                text = speed.labelAr,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = solomonColors.gold.copy(alpha = 0.22f),
                            selectedLabelColor = solomonColors.goldBright
                        ),
                        modifier = Modifier.testTag("tts_speed_${speed.name.lowercase()}")
                    )
                }
            }

            // Voice Persona Selection: صوت طفلة، صوت رجل، صوت امرأة
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RecordVoiceOver,
                    contentDescription = "الصوت",
                    tint = solomonColors.emerald,
                    modifier = Modifier.size(16.dp)
                )
                VoicePersona.entries.forEach { persona ->
                    val selected = speechState.persona == persona
                    FilterChip(
                        selected = selected,
                        onClick = { speechController.setPersona(persona) },
                        label = {
                            Text(
                                text = persona.labelAr,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = solomonColors.emerald.copy(alpha = 0.22f),
                            selectedLabelColor = solomonColors.emerald
                        ),
                        modifier = Modifier.testTag("tts_voice_${persona.name.lowercase()}")
                    )
                }
            }

            // Highlighted Live Text Box (yellow highlight on current spoken word)
            if (showHighlightedPreview) {
                Spacer(modifier = Modifier.height(8.dp))
                val range = if (isThisSource) speechState.activeRange else null
                val highlighted = buildHighlightedText(textToRead, range)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = highlighted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickSpeakerButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isActive) solomonColors.gold else solomonColors.gold.copy(alpha = 0.16f)
            )
    ) {
        Icon(
            imageVector = if (isActive) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = if (isActive) "توقف عن القراءة" else "قارئ صوتي",
            tint = if (isActive) MaterialTheme.colorScheme.onPrimary else solomonColors.goldBright,
            modifier = Modifier.size(20.dp)
        )
    }
}
