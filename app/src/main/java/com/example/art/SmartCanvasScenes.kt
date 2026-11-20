package com.example.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import com.example.model.DiscoveryItem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun DrawScope.drawDiscoveryArtwork(
    item: DiscoveryItem,
    requestedStyle: ArtStyleMode,
    progress: Float,
    variationSeed: Int = 0
) {
    val palette = paletteFor(requestedStyle, item)
    val p = progress.coerceIn(0f, 1f)

    val bgProgress = (p / 0.25f).coerceIn(0f, 1f)
    val midProgress = ((p - 0.2f) / 0.4f).coerceIn(0f, 1f)
    val detailProgress = ((p - 0.5f) / 0.35f).coerceIn(0f, 1f)
    val finishProgress = ((p - 0.8f) / 0.2f).coerceIn(0f, 1f)

    // Layer 1: Style-specific backdrop
    drawStyleBackdrop(palette, bgProgress, variationSeed)

    // Layer 2 & 3: Subject-specific illustration
    if (midProgress > 0f) {
        when (item.id) {
            "octopus_three_hearts" -> drawOctopusScene(palette, midProgress, detailProgress)
            "immortal_jellyfish" -> drawJellyfishScene(palette, midProgress, detailProgress)
            "tardigrade_water_bear" -> drawTardigradeScene(palette, midProgress, detailProgress)
            "socotra_island" -> drawSocotraScene(palette, midProgress, detailProgress)
            "pink_lake_hillier" -> drawPinkLakeScene(palette, midProgress, detailProgress)
            "door_to_hell_darvaza" -> drawDarvazaScene(palette, midProgress, detailProgress, variationSeed)
            "derinkuyu_underground_city" -> drawDerinkuyuScene(palette, midProgress, detailProgress)
            "great_pyramid_giza" -> drawPyramidScene(palette, midProgress, detailProgress)
            "antikythera_mechanism" -> drawAntikytheraScene(palette, midProgress, detailProgress)
            "muller_lyer_illusion" -> drawIllusionGalleryScene(palette, midProgress, detailProgress, variant = 0)
            "hermann_grid_illusion" -> drawIllusionGalleryScene(palette, midProgress, detailProgress, variant = 1)
            "simultaneous_contrast_illusion" -> drawIllusionGalleryScene(palette, midProgress, detailProgress, variant = 2)
            "monty_hall_paradox" -> drawMontyHallScene(palette, midProgress, detailProgress)
            "birthday_paradox" -> drawBirthdayParadoxScene(palette, midProgress, detailProgress)
            else -> drawZeigarnikScene(palette, midProgress, detailProgress)
        }
    }

    // Layer 4: Finishing polish (magic stars/glow, photographic vignette, or pencil cross-hatching & brush tip)
    drawStyleFinish(palette, finishProgress, p, variationSeed)
}

private fun DrawScope.drawStyleBackdrop(
    palette: ArtPalette,
    bgProgress: Float,
    variationSeed: Int
) {
    val w = size.width
    val h = size.height

    if (palette.isPencil) {
        drawRect(color = palette.skyTop, size = size)
        // Sketchbook paper grain & faint pencil guide lines
        val lines = (24 * bgProgress).toInt()
        for (i in 0 until lines) {
            val y = (i / 24f) * h
            drawLine(
                color = palette.groundPrimary.copy(alpha = 0.25f),
                start = Offset(0f, y),
                end = Offset(w * bgProgress, y),
                strokeWidth = 1f
            )
        }
        // Hand-sketched border frame
        if (bgProgress > 0.3f) {
            drawRect(
                color = palette.strokeColor.copy(alpha = 0.35f),
                topLeft = Offset(14f, 14f),
                size = Size(w - 28f, h - 28f),
                style = Stroke(width = 2f)
            )
        }
    } else {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(palette.skyTop, palette.skyBottom)
            ),
            size = size,
            alpha = bgProgress.coerceAtLeast(0.25f)
        )

        if (palette.isFantasy) {
            val rng = Random(42 + variationSeed)
            val starCount = (32 * bgProgress).toInt()
            for (i in 0 until starCount) {
                val sx = rng.nextFloat() * w
                val sy = rng.nextFloat() * h * 0.72f
                val sr = 1.5f + rng.nextFloat() * 3.5f
                drawCircle(
                    color = palette.highlight.copy(alpha = 0.35f + rng.nextFloat() * 0.55f),
                    radius = sr,
                    center = Offset(sx, sy)
                )
            }
            // Dreamy golden moon/aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.accentPrimary.copy(alpha = 0.35f * bgProgress),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.8f, h * 0.22f),
                    radius = h * 0.36f
                ),
                radius = h * 0.36f,
                center = Offset(w * 0.8f, h * 0.22f)
            )
        } else {
            // Realistic atmospheric sun/light shaft
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.highlight.copy(alpha = 0.28f * bgProgress),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.24f, h * 0.16f),
                    radius = h * 0.45f
                ),
                radius = h * 0.45f,
                center = Offset(w * 0.24f, h * 0.16f)
            )
        }
    }
}

private fun DrawScope.drawStyleFinish(
    palette: ArtPalette,
    finishProgress: Float,
    totalProgress: Float,
    variationSeed: Int
) {
    val w = size.width
    val h = size.height

    if (palette.isFantasy && finishProgress > 0f) {
        val rng = Random(99 + variationSeed)
        val sparkles = (14 * finishProgress).toInt()
        for (i in 0 until sparkles) {
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val r = 6f * finishProgress
            drawLine(palette.accentPrimary.copy(alpha = 0.7f), Offset(x - r, y), Offset(x + r, y), 1.8f)
            drawLine(palette.accentPrimary.copy(alpha = 0.7f), Offset(x, y - r), Offset(x, y + r), 1.8f)
        }
    } else if (palette.isPencil && finishProgress > 0f) {
        // Corner artist hatching strokes
        val strokes = (10 * finishProgress).toInt()
        for (i in 0 until strokes) {
            val off = i * 9f
            drawLine(
                color = palette.strokeColor.copy(alpha = 0.3f),
                start = Offset(w - 90f + off, h - 20f),
                end = Offset(w - 75f + off, h - 42f),
                strokeWidth = 1.5f
            )
        }
    } else if (palette.isRealistic && finishProgress > 0f) {
        // Subtle photographic lens vignette
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f * finishProgress)),
                center = Offset(w / 2f, h / 2f),
                radius = w * 0.72f
            ),
            size = size
        )
    }

    // Active animated brush/pencil tip while drawing is in progress
    if (totalProgress in 0.02f..0.98f) {
        val bx = w * (0.15f + 0.7f * ((totalProgress * 2.7f) % 1f))
        val by = h * (0.25f + 0.5f * sin(totalProgress * PI * 4).toFloat().let { (it + 1f) / 2f })
        drawCircle(
            color = if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047),
            radius = 7f,
            center = Offset(bx, by)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = 13f,
            center = Offset(bx, by),
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawOctopusScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.42f

    val bodyColor = when {
        palette.isPencil -> palette.groundSecondary
        palette.isFantasy -> Color(0xFF9333EA)
        else -> Color(0xFFD9534F)
    }

    // 8 Sinuous Tentacles
    val tentacleCount = (8 * mid).toInt().coerceAtLeast(1)
    for (i in 0 until tentacleCount) {
        val spread = (i - 3.5f) / 3.5f
        val path = Path().apply {
            moveTo(cx + spread * w * 0.08f, cy + h * 0.08f)
            cubicTo(
                cx + spread * w * 0.22f, cy + h * 0.24f,
                cx + spread * w * 0.34f, cy + h * 0.35f,
                cx + spread * w * 0.40f, h * 0.88f
            )
        }
        drawPath(
            path = path,
            color = if (palette.isPencil) palette.strokeColor else bodyColor,
            style = Stroke(width = if (palette.isPencil) 5f else 13f, cap = StrokeCap.Round)
        )
        if (detail > 0.3f) {
            // Suction cups along tentacle
            val cupX = cx + spread * w * 0.26f
            val cupY = cy + h * 0.28f
            drawCircle(
                color = if (palette.isPencil) palette.skyTop else palette.highlight,
                radius = 4f * detail,
                center = Offset(cupX, cupY)
            )
        }
    }

    // Mantle Head
    val headRectTopLeft = Offset(cx - w * 0.14f * mid, cy - h * 0.24f * mid)
    val headSize = Size(w * 0.28f * mid, h * 0.34f * mid)
    if (palette.isPencil) {
        drawOval(
            color = palette.groundPrimary,
            topLeft = headRectTopLeft,
            size = headSize
        )
        drawOval(
            color = palette.strokeColor,
            topLeft = headRectTopLeft,
            size = headSize,
            style = Stroke(width = 3.5f)
        )
    } else {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(bodyColor.copy(alpha = 0.95f), bodyColor.copy(alpha = 0.65f)),
                center = Offset(cx - 20f, cy - 20f),
                radius = w * 0.22f
            ),
            topLeft = headRectTopLeft,
            size = headSize
        )
    }

    // Eyes & 3 Glowing Hearts!
    if (detail > 0f) {
        val eyeOffset = w * 0.06f
        listOf(-eyeOffset, eyeOffset).forEach { dx ->
            drawCircle(
                color = if (palette.isPencil) palette.skyTop else Color.White,
                radius = 9f * detail,
                center = Offset(cx + dx, cy + h * 0.02f)
            )
            drawCircle(
                color = palette.strokeColor,
                radius = 4.5f * detail,
                center = Offset(cx + dx, cy + h * 0.02f)
            )
        }

        // Three Hearts inside the mantle
        val heartPositions = listOf(
            Offset(cx, cy - h * 0.13f),
            Offset(cx - w * 0.045f, cy - h * 0.06f),
            Offset(cx + w * 0.045f, cy - h * 0.06f)
        )
        heartPositions.forEach { pos ->
            drawCircle(
                color = if (palette.isPencil) palette.strokeColor else Color(0xFF38BDF8),
                radius = 9f * detail,
                center = pos
            )
            if (!palette.isPencil) {
                drawCircle(
                    color = Color(0xFFFDE047).copy(alpha = 0.55f * detail),
                    radius = 14f * detail,
                    center = pos,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

private fun DrawScope.drawJellyfishScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.34f
    val bellColor = when {
        palette.isPencil -> palette.strokeColor
        palette.isFantasy -> Color(0xFF38BDF8)
        else -> Color(0xFFFB7185)
    }

    // Trailing glowing tentacles
    val count = (9 * mid).toInt().coerceAtLeast(1)
    for (i in 0 until count) {
        val xStart = cx + (i - 4) * (w * 0.032f)
        val path = Path().apply {
            moveTo(xStart, cy + h * 0.06f)
            cubicTo(
                xStart - 24f, cy + h * 0.25f,
                xStart + 24f, cy + h * 0.40f,
                xStart - 10f, h * 0.88f
            )
        }
        drawPath(
            path = path,
            color = bellColor.copy(alpha = 0.72f),
            style = Stroke(width = if (palette.isPencil) 2.2f else 3.8f, cap = StrokeCap.Round)
        )
    }

    // Dome bell
    val bellPath = Path().apply {
        moveTo(cx - w * 0.18f * mid, cy + h * 0.06f)
        cubicTo(
            cx - w * 0.18f * mid, cy - h * 0.22f * mid,
            cx + w * 0.18f * mid, cy - h * 0.22f * mid,
            cx + w * 0.18f * mid, cy + h * 0.06f
        )
        close()
    }
    if (palette.isPencil) {
        drawPath(bellPath, palette.groundPrimary)
        drawPath(bellPath, palette.strokeColor, style = Stroke(3f))
    } else {
        drawPath(
            bellPath,
            brush = Brush.verticalGradient(listOf(bellColor.copy(alpha = 0.9f), palette.accentPrimary.copy(alpha = 0.45f)))
        )
    }

    if (detail > 0f) {
        drawCircle(
            color = if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047),
            radius = 12f * detail,
            center = Offset(cx, cy - h * 0.04f),
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawTardigradeScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.52f
    val bodyTint = when {
        palette.isPencil -> palette.groundSecondary
        palette.isFantasy -> Color(0xFF2DD4BF)
        else -> Color(0xFFD4A373)
    }

    // 4 pairs of stubby legs
    for (i in 0 until 4) {
        val lx = cx - w * 0.16f + i * (w * 0.105f)
        drawLine(
            color = if (palette.isPencil) palette.strokeColor else bodyTint,
            start = Offset(lx, cy + h * 0.08f),
            end = Offset(lx - 10f, cy + h * 0.22f * mid),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )
    }

    // Plump segmented oval body
    val bodySize = Size(w * 0.50f * mid, h * 0.30f * mid)
    val topLeft = Offset(cx - bodySize.width / 2f, cy - bodySize.height / 2f)
    drawRoundRect(
        color = if (palette.isPencil) palette.groundPrimary else bodyTint,
        topLeft = topLeft,
        size = bodySize,
        cornerRadius = CornerRadius(80f, 80f)
    )
    drawRoundRect(
        color = palette.strokeColor,
        topLeft = topLeft,
        size = bodySize,
        cornerRadius = CornerRadius(80f, 80f),
        style = Stroke(width = 3f)
    )

    if (detail > 0f) {
        // Segment lines & tubular mouth
        for (s in 1..3) {
            val sx = topLeft.x + (bodySize.width * s / 4f)
            drawLine(
                color = palette.strokeColor.copy(alpha = 0.45f),
                start = Offset(sx, topLeft.y + 12f),
                end = Offset(sx, topLeft.y + bodySize.height - 12f),
                strokeWidth = 2.5f
            )
        }
        drawCircle(
            color = palette.strokeColor,
            radius = 8f * detail,
            center = Offset(topLeft.x + bodySize.width, cy)
        )
    }
}

private fun DrawScope.drawSocotraScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height

    // Sea & rocky cliff
    val cliffColor = when {
        palette.isPencil -> palette.groundPrimary
        palette.isFantasy -> Color(0xFF3B1D60)
        else -> Color(0xFF9C6644)
    }
    drawRect(
        color = cliffColor,
        topLeft = Offset(0f, h * 0.72f),
        size = Size(w * mid, h * 0.28f)
    )

    // Dragon Blood Tree Trunk
    val trunkX = w * 0.5f
    val trunkTopY = h * 0.45f
    drawLine(
        color = if (palette.isPencil) palette.strokeColor else Color(0xFF6F4E37),
        start = Offset(trunkX, h * 0.74f),
        end = Offset(trunkX, trunkTopY),
        strokeWidth = 22f * mid,
        cap = StrokeCap.Round
    )

    // Intertwined umbrella branches
    val branchCount = (7 * mid).toInt().coerceAtLeast(1)
    for (i in 0 until branchCount) {
        val bx = trunkX + (i - 3) * (w * 0.065f)
        drawLine(
            color = if (palette.isPencil) palette.strokeColor else Color(0xFF5C4033),
            start = Offset(trunkX, trunkTopY + 20f),
            end = Offset(bx, h * 0.34f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }

    // Iconic Mushroom Umbrella Canopy
    if (detail > 0f) {
        val canopyColor = when {
            palette.isPencil -> palette.groundSecondary
            palette.isFantasy -> Color(0xFF10B981)
            else -> Color(0xFF2D6A4F)
        }
        val canopyPath = Path().apply {
            moveTo(w * 0.22f, h * 0.35f)
            cubicTo(w * 0.25f, h * 0.14f, w * 0.75f, h * 0.14f, w * 0.78f, h * 0.35f)
            close()
        }
        drawPath(canopyPath, canopyColor)
        if (palette.isPencil) {
            drawPath(canopyPath, palette.strokeColor, style = Stroke(3f))
        }
        // Crimson dragon blood resin droplets
        listOf(0.46f, 0.53f).forEach { rx ->
            drawCircle(
                color = if (palette.isPencil) palette.strokeColor else Color(0xFFDC2626),
                radius = 5.5f * detail,
                center = Offset(w * rx, h * 0.58f)
            )
        }
    }
}

private fun DrawScope.drawPinkLakeScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height

    // Surrounding emerald forest & ocean strip
    val forestColor = when {
        palette.isPencil -> palette.groundPrimary
        palette.isFantasy -> Color(0xFF1E3A5F)
        else -> Color(0xFF1B4332)
    }
    drawRect(
        color = forestColor,
        topLeft = Offset(0f, h * 0.35f),
        size = Size(w * mid, h * 0.65f)
    )

    // Vivid Pink Lake Hillier
    val pinkColor = when {
        palette.isPencil -> palette.highlight
        palette.isFantasy -> Color(0xFFF472B6)
        else -> Color(0xFFEC4899)
    }
    val lakeTopLeft = Offset(w * 0.14f, h * 0.44f)
    val lakeSize = Size(w * 0.72f * mid, h * 0.38f * mid)
    drawOval(color = pinkColor, topLeft = lakeTopLeft, size = lakeSize)
    drawOval(
        color = if (palette.isPencil) palette.strokeColor else Color.White.copy(alpha = 0.65f),
        topLeft = lakeTopLeft,
        size = lakeSize,
        style = Stroke(width = 4f)
    )

    if (detail > 0f) {
        // Tree silhouettes & shoreline shimmer
        for (i in 0 until 8) {
            val tx = w * (0.12f + i * 0.11f)
            drawCircle(
                color = if (palette.isPencil) palette.strokeColor else Color(0xFF34D399),
                radius = 14f * detail,
                center = Offset(tx, h * 0.40f)
            )
        }
    }
}

private fun DrawScope.drawDarvazaScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float,
    variationSeed: Int
) {
    val w = size.width
    val h = size.height

    // Desert ground
    drawRect(
        color = if (palette.isPencil) palette.groundPrimary else Color(0xFF2B1B17),
        topLeft = Offset(0f, h * 0.56f),
        size = Size(w, h * 0.44f)
    )

    // Crater Rim
    val craterTopLeft = Offset(w * 0.18f, h * 0.52f)
    val craterSize = Size(w * 0.64f * mid, h * 0.32f * mid)
    drawOval(
        color = if (palette.isPencil) palette.groundSecondary else Color(0xFF1A0D0A),
        topLeft = craterTopLeft,
        size = craterSize
    )
    drawOval(
        color = if (palette.isPencil) palette.strokeColor else Color(0xFFEA580C),
        topLeft = craterTopLeft,
        size = craterSize,
        style = Stroke(width = 4f)
    )

    // Roaring Flames inside the crater
    if (detail > 0f) {
        val rng = Random(12 + variationSeed)
        for (i in 0 until 7) {
            val fx = w * (0.28f + i * 0.072f)
            val flameH = (h * (0.18f + rng.nextFloat() * 0.14f)) * detail
            val flamePath = Path().apply {
                moveTo(fx - 16f, h * 0.68f)
                quadraticTo(fx - 6f, h * 0.68f - flameH, fx, h * 0.68f - flameH)
                quadraticTo(fx + 14f, h * 0.68f - flameH * 0.5f, fx + 16f, h * 0.68f)
                close()
            }
            drawPath(
                flamePath,
                color = if (palette.isPencil) palette.strokeColor else if (i % 2 == 0) Color(0xFFF97316) else Color(0xFFFACC15)
            )
        }
    }
}

private fun DrawScope.drawDerinkuyuScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height

    // Underground Earth Cross-Section
    drawRect(
        color = if (palette.isPencil) palette.groundPrimary else Color(0xFF3E2723),
        topLeft = Offset(0f, h * 0.25f),
        size = Size(w * mid, h * 0.75f)
    )

    // Central Ventilation Shaft
    drawRect(
        color = if (palette.isPencil) palette.skyTop else Color(0xFF141010),
        topLeft = Offset(w * 0.47f, h * 0.14f),
        size = Size(w * 0.06f, h * 0.76f * mid)
    )

    // Carved Underground Tiers & Chambers
    val tiers = (4 * mid).toInt().coerceAtLeast(1)
    for (t in 0 until tiers) {
        val y = h * (0.34f + t * 0.15f)
        listOf(0.22f, 0.64f).forEach { rx ->
            drawRoundRect(
                color = if (palette.isPencil) palette.skyTop else Color(0xFF1F1612),
                topLeft = Offset(w * rx, y),
                size = Size(w * 0.18f, h * 0.09f),
                cornerRadius = CornerRadius(18f, 18f)
            )
            drawRoundRect(
                color = if (palette.isPencil) palette.strokeColor else Color(0xFFE6B84C),
                topLeft = Offset(w * rx, y),
                size = Size(w * 0.18f, h * 0.09f),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 2.5f)
            )
        }
        if (detail > 0.3f) {
            drawLine(
                color = if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047),
                start = Offset(w * 0.40f, y + h * 0.045f),
                end = Offset(w * 0.64f, y + h * 0.045f),
                strokeWidth = 3f
            )
        }
    }
}

private fun DrawScope.drawPyramidScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height

    // Golden desert floor
    drawRect(
        color = if (palette.isPencil) palette.groundPrimary else Color(0xFFC2955B),
        topLeft = Offset(0f, h * 0.72f),
        size = Size(w, h * 0.28f)
    )

    // Great Pyramid Main Face + Shaded Side
    val apex = Offset(w * 0.48f, h * 0.20f)
    val leftBase = Offset(w * 0.16f, h * 0.76f)
    val rightBase = Offset(w * 0.78f, h * 0.76f)
    val midBase = Offset(w * 0.56f, h * 0.78f)

    val litFace = Path().apply {
        moveTo(apex.x, apex.y)
        lineTo(leftBase.x, leftBase.y)
        lineTo(midBase.x, midBase.y)
        close()
    }
    val darkFace = Path().apply {
        moveTo(apex.x, apex.y)
        lineTo(midBase.x, midBase.y)
        lineTo(rightBase.x, rightBase.y)
        close()
    }

    if (mid > 0f) {
        drawPath(litFace, color = if (palette.isPencil) palette.highlight else Color(0xFFE6B86E))
        drawPath(darkFace, color = if (palette.isPencil) palette.groundSecondary else Color(0xFF9C7238))
        if (palette.isPencil) {
            drawPath(litFace, palette.strokeColor, style = Stroke(3f))
            drawPath(darkFace, palette.strokeColor, style = Stroke(3f))
        }
    }

    if (detail > 0f) {
        // Golden Capstone at the Apex!
        val capPath = Path().apply {
            moveTo(apex.x, apex.y)
            lineTo(apex.x - w * 0.048f, apex.y + h * 0.09f)
            lineTo(apex.x + w * 0.044f, apex.y + h * 0.09f)
            close()
        }
        drawPath(
            capPath,
            color = if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047)
        )
        // North Star alignment guide
        drawCircle(
            color = if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047),
            radius = 5f * detail,
            center = Offset(apex.x, h * 0.08f)
        )
        drawLine(
            color = (if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047)).copy(alpha = 0.5f),
            start = Offset(apex.x, h * 0.09f),
            end = apex,
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawAntikytheraScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height
    val bronze = when {
        palette.isPencil -> palette.strokeColor
        palette.isFantasy -> Color(0xFFE6B84C)
        else -> Color(0xFFB08D57)
    }

    fun drawGear(center: Offset, radius: Float, teeth: Int) {
        drawCircle(color = bronze, radius = radius * 0.78f, center = center, style = Stroke(width = 6f))
        drawCircle(color = bronze, radius = radius * 0.25f, center = center)
        for (i in 0 until teeth) {
            val angle = i * (2f * PI.toFloat() / teeth)
            val p1 = Offset(center.x + radius * 0.78f * cos(angle), center.y + radius * 0.78f * sin(angle))
            val p2 = Offset(center.x + radius * cos(angle), center.y + radius * sin(angle))
            drawLine(bronze, p1, p2, strokeWidth = 5f, cap = StrokeCap.Square)
        }
    }

    if (mid > 0f) {
        drawGear(Offset(w * 0.42f, h * 0.50f), h * 0.28f * mid, 14)
    }
    if (detail > 0f) {
        drawGear(Offset(w * 0.65f, h * 0.42f), h * 0.17f * detail, 10)
        drawGear(Offset(w * 0.62f, h * 0.67f), h * 0.13f * detail, 8)
    }
}

private fun DrawScope.drawIllusionGalleryScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float,
    variant: Int
) {
    val w = size.width
    val h = size.height
    val ink = if (palette.isPencil) palette.strokeColor else Color(0xFFF9DE7D)

    // Gallery frame
    drawRoundRect(
        color = ink,
        topLeft = Offset(w * 0.14f, h * 0.16f),
        size = Size(w * 0.72f * mid, h * 0.68f * mid),
        cornerRadius = CornerRadius(24f, 24f),
        style = Stroke(width = 4f)
    )

    if (detail > 0f) {
        when (variant) {
            0 -> {
                // Muller-Lyer stylized lines
                listOf(0.38f, 0.62f).forEachIndexed { idx, yf ->
                    val y = h * yf
                    drawLine(ink, Offset(w * 0.28f, y), Offset(w * 0.72f, y), strokeWidth = 5f)
                    val dir = if (idx == 0) -1f else 1f
                    drawLine(ink, Offset(w * 0.28f, y), Offset(w * 0.28f + dir * 24f, y - 22f), strokeWidth = 4f)
                    drawLine(ink, Offset(w * 0.28f, y), Offset(w * 0.28f + dir * 24f, y + 22f), strokeWidth = 4f)
                    drawLine(ink, Offset(w * 0.72f, y), Offset(w * 0.72f - dir * 24f, y - 22f), strokeWidth = 4f)
                    drawLine(ink, Offset(w * 0.72f, y), Offset(w * 0.72f - dir * 24f, y + 22f), strokeWidth = 4f)
                }
            }
            1 -> {
                // Hermann Grid miniature
                for (r in 0..2) {
                    for (c in 0..3) {
                        drawRect(
                            color = ink,
                            topLeft = Offset(w * (0.24f + c * 0.14f), h * (0.26f + r * 0.17f)),
                            size = Size(w * 0.10f, h * 0.12f)
                        )
                    }
                }
            }
            else -> {
                // Simultaneous Contrast dual squares
                drawRect(color = Color(0xFF1F2937), topLeft = Offset(w * 0.20f, h * 0.26f), size = Size(w * 0.30f, h * 0.48f))
                drawRect(color = Color(0xFFE5E7EB), topLeft = Offset(w * 0.50f, h * 0.26f), size = Size(w * 0.30f, h * 0.48f))
                drawRect(color = Color(0xFF888C96), topLeft = Offset(w * 0.30f, h * 0.40f), size = Size(w * 0.10f, h * 0.20f))
                drawRect(color = Color(0xFF888C96), topLeft = Offset(w * 0.60f, h * 0.40f), size = Size(w * 0.10f, h * 0.20f))
            }
        }
    }
}

private fun DrawScope.drawMontyHallScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height
    val doorColor = if (palette.isPencil) palette.groundSecondary else Color(0xFF7C5329)
    val frameColor = if (palette.isPencil) palette.strokeColor else Color(0xFFE6B84C)

    for (i in 0 until 3) {
        val x = w * (0.16f + i * 0.25f)
        val doorSize = Size(w * 0.18f, h * 0.54f * mid)
        val topLeft = Offset(x, h * 0.26f)
        drawRoundRect(color = doorColor, topLeft = topLeft, size = doorSize, cornerRadius = CornerRadius(26f, 26f))
        drawRoundRect(
            color = frameColor,
            topLeft = topLeft,
            size = doorSize,
            cornerRadius = CornerRadius(26f, 26f),
            style = Stroke(width = 4f)
        )
        if (detail > 0f && i == 1) {
            // Golden prize glow inside middle door
            drawCircle(
                color = if (palette.isPencil) palette.highlight else Color(0xFFFDE047),
                radius = 22f * detail,
                center = Offset(x + doorSize.width / 2f, h * 0.54f)
            )
        }
    }
}

private fun DrawScope.drawBirthdayParadoxScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height
    val nodeColor = if (palette.isPencil) palette.strokeColor else Color(0xFF60A5FA)
    val matchColor = if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047)

    val points = mutableListOf<Offset>()
    val rng = Random(23)
    val total = (23 * mid).toInt().coerceAtLeast(2)
    for (i in 0 until total) {
        points.add(Offset(w * (0.16f + rng.nextFloat() * 0.68f), h * (0.20f + rng.nextFloat() * 0.60f)))
    }
    points.forEachIndexed { idx, pt ->
        drawCircle(
            color = if (idx < 2) matchColor else nodeColor,
            radius = if (idx < 2) 11f else 6f,
            center = pt
        )
    }
    if (detail > 0f && points.size >= 2) {
        drawLine(
            color = matchColor,
            start = points[0],
            end = points[1],
            strokeWidth = 4f * detail,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawZeigarnikScene(
    palette: ArtPalette,
    mid: Float,
    detail: Float
) {
    val w = size.width
    val h = size.height
    val outline = if (palette.isPencil) palette.strokeColor else Color(0xFFF9DE7D)

    // Thought halo / circle
    drawCircle(
        color = outline.copy(alpha = 0.7f),
        radius = h * 0.28f * mid,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = 4f, join = StrokeJoin.Round)
    )

    if (detail > 0f) {
        // Completed blocks at bottom, glowing open puzzle piece at top
        drawRect(
            color = if (palette.isPencil) palette.groundSecondary else Color(0xFF2DD4BF),
            topLeft = Offset(w * 0.42f, h * 0.54f),
            size = Size(w * 0.16f, h * 0.12f)
        )
        drawCircle(
            color = if (palette.isPencil) palette.strokeColor else Color(0xFFFDE047),
            radius = 18f * detail,
            center = Offset(w * 0.5f, h * 0.36f)
        )
    }
}
