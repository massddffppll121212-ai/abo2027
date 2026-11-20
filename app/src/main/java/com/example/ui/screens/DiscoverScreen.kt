package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.OfflineSpeechController
import com.example.audio.SpeechUiState
import com.example.model.DiscoveryCategory
import com.example.model.DiscoveryItem
import com.example.ui.components.QuickSpeakerButton
import com.example.ui.components.fullNarrationText
import com.example.ui.theme.LocalSolomonColors

@Composable
fun DiscoverScreen(
    allDiscoveries: List<DiscoveryItem>,
    selectedCategory: DiscoveryCategory,
    searchQuery: String,
    discoveredIds: Set<String>,
    favoriteIds: Set<String>,
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onSelectCategory: (DiscoveryCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenDiscovery: (DiscoveryItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRingBannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current

    val filteredItems = remember(allDiscoveries, selectedCategory, searchQuery) {
        allDiscoveries.filter { item ->
            val matchesCategory = selectedCategory == DiscoveryCategory.ALL || item.category == selectedCategory
            val q = searchQuery.trim()
            val matchesSearch = q.isEmpty() ||
                item.title.contains(q, ignoreCase = true) ||
                item.subtitle.contains(q, ignoreCase = true) ||
                item.locationOrOrigin.contains(q, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 680.dp) 2 else 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Search & Progress Header
            item(span = { GridItemSpan(columns) }) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "ابحث عن الأخطبوط، سقطرى، الأهرامات...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = "بحث")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "مسح البحث")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = solomonColors.gold,
                            unfocusedBorderColor = solomonColors.subtleBorder,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("discover_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Chips (All + 5 Categories)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(DiscoveryCategory.entries) { category ->
                            val isSelected = category == selectedCategory
                            val accent = categoryColor(category)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(category) },
                                label = {
                                    Text(
                                        text = category.titleAr,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = categoryIcon(category),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent.copy(alpha = 0.22f),
                                    selectedLabelColor = accent,
                                    selectedLeadingIconColor = accent
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = solomonColors.subtleBorder,
                                    selectedBorderColor = accent
                                ),
                                modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Encyclopedia Progress Strip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        solomonColors.gold.copy(alpha = 0.16f),
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .border(1.dp, solomonColors.subtleBorder, RoundedCornerShape(16.dp))
                            .clickable(onClick = onRingBannerClick)
                            .padding(14.dp)
                            .testTag("random_discovery_banner")
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تم توثيق ${discoveredIds.size} من ${allDiscoveries.size} اكتشافاً حقيقياً",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (allDiscoveries.isEmpty()) 0f
                                    else discoveredIds.size.toFloat() / allDiscoveries.size
                                },
                                color = solomonColors.goldBright,
                                trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.45f),
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(solomonColors.gold.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "اكتشاف عشوائي",
                                tint = solomonColors.goldBright
                            )
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = solomonColors.gold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لم نعثر على نتائج مطابقة لبحثك",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "جرّب البحث بكلمة أخرى أو اختر قسم «الكل»",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    val cardSourceId = "discovery_${item.id}"
                    val isReadingThis = speechState.activeSourceId == cardSourceId && speechState.isSpeaking
                    DiscoveryCard(
                        item = item,
                        isDiscovered = item.id in discoveredIds,
                        isFavorite = item.id in favoriteIds,
                        isSpeaking = isReadingThis,
                        onSpeakClick = {
                            if (isReadingThis) {
                                speechController.stop()
                            } else {
                                speechController.speak(item.fullNarrationText(), cardSourceId)
                            }
                        },
                        onClick = { onOpenDiscovery(item) },
                        onToggleFavorite = { onToggleFavorite(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoveryCard(
    item: DiscoveryItem,
    isDiscovered: Boolean,
    isFavorite: Boolean,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    val accent = categoryColor(item.category)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isDiscovered) accent.copy(alpha = 0.45f) else solomonColors.subtleBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("discovery_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = categoryIcon(item.category),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = item.category.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDiscovered) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "مكتشف",
                            tint = solomonColors.emerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Text(
                            text = "+${item.pointsReward}",
                            style = MaterialTheme.typography.labelSmall,
                            color = solomonColors.goldBright,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(solomonColors.gold.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    QuickSpeakerButton(
                        isActive = isSpeaking,
                        onClick = onSpeakClick,
                        modifier = Modifier.testTag("speak_card_${item.id}")
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = if (isFavorite) "إزالة من مجموعتي" else "حفظ في مجموعتي",
                            tint = if (isFavorite) solomonColors.goldBright else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.locationOrOrigin,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
                Text(
                    text = if (isDiscovered) "اقرأ التفاصيل ←" else "افتح للاكتشاف ←",
                    style = MaterialTheme.typography.labelMedium,
                    color = solomonColors.gold
                )
            }
        }
    }
}

@Composable
fun categoryColor(category: DiscoveryCategory): Color {
    val c = LocalSolomonColors.current
    return when (category) {
        DiscoveryCategory.ALL -> c.gold
        DiscoveryCategory.STRANGE_ANIMALS -> c.emerald
        DiscoveryCategory.MYSTERIOUS_PLACES -> c.sapphire
        DiscoveryCategory.OPTICAL_ILLUSIONS -> c.amethyst
        DiscoveryCategory.MIND_RIDDLES -> c.coral
        DiscoveryCategory.HISTORICAL_SECRETS -> c.amber
    }
}

fun categoryIcon(category: DiscoveryCategory): ImageVector = when (category) {
    DiscoveryCategory.ALL -> Icons.Rounded.AutoAwesome
    DiscoveryCategory.STRANGE_ANIMALS -> Icons.Rounded.Pets
    DiscoveryCategory.MYSTERIOUS_PLACES -> Icons.Rounded.Explore
    DiscoveryCategory.OPTICAL_ILLUSIONS -> Icons.Rounded.Visibility
    DiscoveryCategory.MIND_RIDDLES -> Icons.Rounded.Psychology
    DiscoveryCategory.HISTORICAL_SECRETS -> Icons.Rounded.AccountBalance
}
