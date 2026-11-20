package com.example.art

import androidx.compose.ui.graphics.Color
import com.example.model.DiscoveryCategory
import com.example.model.DiscoveryItem

enum class ArtStyleMode(
    val labelAr: String,
    val descriptionAr: String
) {
    AUTO("تلقائي", "يختار الرسام بنفسه حسب الموضوع"),
    REALISTIC("واقعي", "تفاصيل دقيقة كصورة حقيقية"),
    FANTASY("خيالي", "ألوان سحرية ونجوم وضوء ذهبي"),
    PENCIL("قلم رصاص", "رسم يدوي أبيض وأسود")
}

fun resolveEffectiveArtStyle(
    requested: ArtStyleMode,
    category: DiscoveryCategory
): ArtStyleMode {
    if (requested != ArtStyleMode.AUTO) return requested
    return when (category) {
        DiscoveryCategory.STRANGE_ANIMALS -> ArtStyleMode.REALISTIC
        DiscoveryCategory.MYSTERIOUS_PLACES -> ArtStyleMode.FANTASY
        DiscoveryCategory.HISTORICAL_SECRETS,
        DiscoveryCategory.OPTICAL_ILLUSIONS,
        DiscoveryCategory.MIND_RIDDLES,
        DiscoveryCategory.ALL -> ArtStyleMode.PENCIL
    }
}

fun ArtStyleMode.nextAlternative(currentCategory: DiscoveryCategory): ArtStyleMode {
    val effective = resolveEffectiveArtStyle(this, currentCategory)
    return when (effective) {
        ArtStyleMode.REALISTIC -> ArtStyleMode.FANTASY
        ArtStyleMode.FANTASY -> ArtStyleMode.PENCIL
        ArtStyleMode.PENCIL, ArtStyleMode.AUTO -> ArtStyleMode.REALISTIC
    }
}

data class ArtPalette(
    val effectiveStyle: ArtStyleMode,
    val skyTop: Color,
    val skyBottom: Color,
    val groundPrimary: Color,
    val groundSecondary: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val highlight: Color,
    val strokeColor: Color,
    val isPencil: Boolean,
    val isFantasy: Boolean,
    val isRealistic: Boolean
)

fun paletteFor(style: ArtStyleMode, item: DiscoveryItem): ArtPalette {
    val effective = resolveEffectiveArtStyle(style, item.category)
    return when (effective) {
        ArtStyleMode.PENCIL -> ArtPalette(
            effectiveStyle = effective,
            skyTop = Color(0xFFF6F1E4),
            skyBottom = Color(0xFFE9E1CE),
            groundPrimary = Color(0xFFD5CCB8),
            groundSecondary = Color(0xFFB5AC99),
            accentPrimary = Color(0xFF2B2B2B),
            accentSecondary = Color(0xFF5A5A5A),
            highlight = Color(0xFFFAF7EE),
            strokeColor = Color(0xFF1F1F1F),
            isPencil = true,
            isFantasy = false,
            isRealistic = false
        )

        ArtStyleMode.FANTASY -> ArtPalette(
            effectiveStyle = effective,
            skyTop = Color(0xFF140D2E),
            skyBottom = Color(0xFF311B5C),
            groundPrimary = Color(0xFF1E1442),
            groundSecondary = Color(0xFF4C287A),
            accentPrimary = Color(0xFFF9DE7D),
            accentSecondary = Color(0xFF2DD4BF),
            highlight = Color(0xFFFFF3B0),
            strokeColor = Color(0xFFE6B84C),
            isPencil = false,
            isFantasy = true,
            isRealistic = false
        )

        ArtStyleMode.REALISTIC, ArtStyleMode.AUTO -> ArtPalette(
            effectiveStyle = ArtStyleMode.REALISTIC,
            skyTop = Color(0xFF0E345A),
            skyBottom = Color(0xFF2A6F97),
            groundPrimary = Color(0xFF1A435E),
            groundSecondary = Color(0xFF2B5876),
            accentPrimary = Color(0xFFE68A4C),
            accentSecondary = Color(0xFF38BDF8),
            highlight = Color(0xFFF8FAFC),
            strokeColor = Color(0xFF0B1F33),
            isPencil = false,
            isFantasy = false,
            isRealistic = true
        )
    }
}
