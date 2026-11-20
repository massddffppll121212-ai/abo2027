package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ExplorerRank
import com.example.ui.theme.LocalSolomonColors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GoldenRingHeader(
    points: Int,
    rank: ExplorerRank,
    discoveredCount: Int,
    totalCount: Int,
    onRingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    val haptic = LocalHapticFeedback.current
    var pressedPulse by remember { mutableStateOf(false) }

    LaunchedEffect(pressedPulse) {
        if (pressedPulse) {
            delay(220)
            pressedPulse = false
        }
    }

    val tapScale by animateFloatAsState(
        targetValue = if (pressedPulse) 1.14f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f),
        label = "ring_tap_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ring_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotation"
    )
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            solomonColors.gold.copy(alpha = 0.14f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Glowing Golden Solomon's Ring Interactive Emblem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            pressedPulse = true
                            onRingClick()
                        }
                        .padding(vertical = 4.dp)
                        .testTag("golden_ring_button")
                        .semantics {
                            contentDescription = "خاتم سليمان الذهبي، اضغط للحصول على اكتشاف عشوائي"
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .scale(tapScale),
                        contentAlignment = Alignment.Center
                    ) {
                        GoldenRingCanvas(
                            glowPulse = glowPulse,
                            rotationAngle = rotationAngle,
                            shimmerOffset = shimmerOffset,
                            goldPrimary = solomonColors.gold,
                            goldBright = solomonColors.goldBright,
                            goldDeep = solomonColors.goldDeep,
                            modifier = Modifier.size(66.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "خاتم سليمان",
                                style = MaterialTheme.typography.titleLarge,
                                color = solomonColors.goldBright,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Solomon's Ring",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = solomonColors.gold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "اضغط الخاتم لاكتشاف عشوائي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Points & Rank Pill
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(solomonColors.gold.copy(alpha = 0.16f))
                            .border(
                                width = 1.dp,
                                color = solomonColors.gold.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Stars,
                            contentDescription = "النقاط",
                            tint = solomonColors.goldBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$points نقطة",
                            style = MaterialTheme.typography.labelMedium,
                            color = solomonColors.goldBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${rank.titleAr} • $discoveredCount/$totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GoldenRingCanvas(
    glowPulse: Float,
    rotationAngle: Float,
    shimmerOffset: Float,
    goldPrimary: Color,
    goldBright: Color,
    goldDeep: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // 1. Pulsing ambient golden glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    goldBright.copy(alpha = 0.42f * glowPulse),
                    goldPrimary.copy(alpha = 0.18f * glowPulse),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // 2. Rotating subtle eight-pointed aura rays
        rotate(degrees = rotationAngle, pivot = center) {
            val rayCount = 8
            for (i in 0 until rayCount) {
                val angleRad = Math.toRadians((i * 360.0 / rayCount))
                val inner = Offset(
                    x = center.x + (radius * 0.56f * cos(angleRad)).toFloat(),
                    y = center.y + (radius * 0.56f * sin(angleRad)).toFloat()
                )
                val outer = Offset(
                    x = center.x + (radius * 0.88f * cos(angleRad)).toFloat(),
                    y = center.y + (radius * 0.88f * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = goldBright.copy(alpha = 0.38f * glowPulse),
                    start = inner,
                    end = outer,
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 3. Outer burnished gold ring band
        val ringRadius = radius * 0.58f
        val ringBrush = Brush.linearGradient(
            colors = listOf(goldBright, goldPrimary, goldDeep, goldBright),
            start = Offset(center.x - ringRadius, center.y - ringRadius),
            end = Offset(center.x + ringRadius, center.y + ringRadius)
        )
        drawCircle(
            brush = ringBrush,
            radius = ringRadius,
            center = center,
            style = Stroke(width = radius * 0.22f)
        )

        // 4. Inner dark bezel & engraved Solomon 8-point star
        drawCircle(
            color = Color(0xFF151C2C),
            radius = ringRadius * 0.72f,
            center = center
        )

        val starRadius = ringRadius * 0.62f
        val starPath1 = Path().apply {
            for (i in 0 until 4) {
                val angle = Math.toRadians((i * 90.0) - 90.0)
                val x = center.x + (starRadius * cos(angle)).toFloat()
                val y = center.y + (starRadius * sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        val starPath2 = Path().apply {
            for (i in 0 until 4) {
                val angle = Math.toRadians((i * 90.0) - 45.0)
                val x = center.x + (starRadius * cos(angle)).toFloat()
                val y = center.y + (starRadius * sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(starPath1, color = goldBright, style = Stroke(width = 2.4f))
        drawPath(starPath2, color = goldPrimary, style = Stroke(width = 2.4f))
        drawCircle(color = goldBright, radius = radius * 0.09f, center = center)

        // 5. Specular shimmer sweep across the ring
        val shimmerX = center.x + shimmerOffset * ringRadius
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.55f),
                    Color.Transparent
                ),
                center = Offset(shimmerX.coerceIn(center.x - ringRadius, center.x + ringRadius), center.y - ringRadius * 0.45f),
                radius = radius * 0.34f
            ),
            radius = ringRadius,
            center = center,
            style = Stroke(width = radius * 0.16f)
        )
    }
}
