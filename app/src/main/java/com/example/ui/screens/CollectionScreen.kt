package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.DiscoveryCategory
import com.example.model.DiscoveryItem
import com.example.ui.components.fullNarrationText
import com.example.ui.theme.LocalSolomonColors

@Composable
fun CollectionScreen(
    allDiscoveries: List<DiscoveryItem>,
    discoveredIds: Set<String>,
    favoriteIds: Set<String>,
    speechState: SpeechUiState,
    speechController: OfflineSpeechController,
    onOpenDiscovery: (DiscoveryItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onTriggerRingDiscovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solomonColors = LocalSolomonColors.current
    var selectedSegment by remember { mutableIntStateOf(0) } // 0 = Saved, 1 = Discovered

    val displayedItems = remember(allDiscoveries, discoveredIds, favoriteIds, selectedSegment) {
        if (selectedSegment == 0) {
            allDiscoveries.filter { it.id in favoriteIds }
        } else {
            allDiscoveries.filter { it.id in discoveredIds }
        }
    }

    val categories = remember {
        DiscoveryCategory.entries.filter { it != DiscoveryCategory.ALL }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "مجموعتي الذهبية",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "احفظ عجائبك المفضلة وتابع تقدمك في أقسام الموسوعة",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category Mastery Strip
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val totalInCat = allDiscoveries.count { it.category == cat }
                    val doneInCat = allDiscoveries.count { it.category == cat && it.id in discoveredIds }
                    val accent = categoryColor(cat)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.border(
                            1.dp,
                            if (doneInCat == totalInCat && totalInCat > 0) accent else solomonColors.subtleBorder,
                            RoundedCornerShape(14.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = categoryIcon(cat),
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.titleAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$doneInCat / $totalInCat",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedSegment == 0,
                    onClick = { selectedSegment = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("collection_tab_saved")
                ) {
                    Text("المحفوظة (${favoriteIds.size})")
                }
                SegmentedButton(
                    selected = selectedSegment == 1,
                    onClick = { selectedSegment = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("collection_tab_discovered")
                ) {
                    Text("المكتشفة (${discoveredIds.size})")
                }
            }
        }

        if (displayedItems.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, solomonColors.subtleBorder, RoundedCornerShape(22.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selectedSegment == 0) Icons.Rounded.Bookmark else Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = solomonColors.goldBright,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedSegment == 0) "لا توجد بطاقات محفوظة بعد" else "لم تفتح أي اكتشاف بعد",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedSegment == 0) {
                            "اضغط على أيقونة الحفظ في أي اكتشاف لإضافته هنا، أو اطلب من الخاتم اكتشافاً عشوائياً."
                        } else {
                            "اضغط على الخاتم الذهبي في الأعلى لكشف أول سر موثق وكسب النقاط!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onTriggerRingDiscovery,
                        colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اكتشاف عشوائي بالخاتم", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(displayedItems, key = { it.id }) { item ->
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
