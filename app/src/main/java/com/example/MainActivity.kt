package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.OfflineSpeechController
import com.example.model.AppTab
import com.example.ui.components.DiscoveryDetailSheet
import com.example.ui.components.GoldenRingHeader
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.CollectionScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.theme.LocalSolomonColors
import com.example.ui.theme.SolomonsRingTheme
import com.example.viewmodel.SolomonsRingViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SolomonsRingViewModel = viewModel()
            SolomonsRingApp(viewModel = viewModel)
        }
    }
}

@Composable
fun SolomonsRingApp(viewModel: SolomonsRingViewModel) {
    val context = LocalContext.current
    val speechController = remember(context) { OfflineSpeechController(context) }
    DisposableEffect(speechController) {
        onDispose { speechController.release() }
    }
    val speechState by speechController.state.collectAsStateWithLifecycle()

    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeDiscovery by viewModel.activeDiscovery.collectAsStateWithLifecycle()
    val isRandomRingReveal by viewModel.isRandomRingReveal.collectAsStateWithLifecycle()
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()
    val rewardBanner by viewModel.rewardBanner.collectAsStateWithLifecycle()
    val rankUpNotice by viewModel.rankUpNotice.collectAsStateWithLifecycle()

    val goldFlashAlpha = remember { Animatable(0f) }
    LaunchedEffect(speechState.goldFlashCounter) {
        if (speechState.goldFlashCounter > 0) {
            goldFlashAlpha.snapTo(0.65f)
            goldFlashAlpha.animateTo(0f, animationSpec = tween(750))
        }
    }

    LaunchedEffect(rewardBanner) {
        if (rewardBanner != null) {
            delay(2600)
            viewModel.dismissRewardBanner()
        }
    }

    SolomonsRingTheme(darkTheme = progress.isDarkTheme) {
        val solomonColors = LocalSolomonColors.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                GoldenRingHeader(
                    points = progress.points,
                    rank = progress.rank,
                    discoveredCount = progress.discoveredIds.size,
                    totalCount = viewModel.allDiscoveries.size,
                    onRingClick = { viewModel.onGoldenRingTapped() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 4.dp
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = tab == selectedTab
                        val icon = when (tab) {
                            AppTab.DISCOVER -> if (selected) Icons.Rounded.Explore else Icons.Outlined.Explore
                            AppTab.PLAY -> if (selected) Icons.Rounded.SportsEsports else Icons.Outlined.SportsEsports
                            AppTab.COLLECTION -> if (selected) Icons.Rounded.Bookmarks else Icons.Outlined.Bookmarks
                            AppTab.ACCOUNT -> if (selected) Icons.Rounded.Person else Icons.Outlined.Person
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.labelAr
                                )
                            },
                            label = {
                                Text(
                                    text = tab.labelAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = solomonColors.goldBright,
                                indicatorColor = solomonColors.gold,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)
                ) {
                    when (selectedTab) {
                        AppTab.DISCOVER -> DiscoverScreen(
                            allDiscoveries = viewModel.allDiscoveries,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            discoveredIds = progress.discoveredIds,
                            favoriteIds = progress.favoriteIds,
                            speechState = speechState,
                            speechController = speechController,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onOpenDiscovery = { viewModel.openDiscovery(it, fromGoldenRing = false) },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onRingBannerClick = viewModel::onGoldenRingTapped
                        )

                        AppTab.PLAY -> PlayScreen(
                            activeGame = activeGame,
                            progress = progress,
                            speechState = speechState,
                            speechController = speechController,
                            onOpenGame = viewModel::openGame,
                            onCloseGame = viewModel::closeGame,
                            onQuizFinished = viewModel::recordQuizCompletion,
                            onMemoryFinished = viewModel::recordMemoryCompletion,
                            onSolomonStageCleared = viewModel::recordSolomonStageCleared,
                            onSecretNumberCracked = viewModel::recordSecretNumberCracked
                        )

                        AppTab.COLLECTION -> CollectionScreen(
                            allDiscoveries = viewModel.allDiscoveries,
                            discoveredIds = progress.discoveredIds,
                            favoriteIds = progress.favoriteIds,
                            speechState = speechState,
                            speechController = speechController,
                            onOpenDiscovery = { viewModel.openDiscovery(it, fromGoldenRing = false) },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onTriggerRingDiscovery = viewModel::onGoldenRingTapped
                        )

                        AppTab.ACCOUNT -> AccountScreen(
                            progress = progress,
                            totalDiscoveries = viewModel.allDiscoveries.size,
                            onUpdateName = viewModel::updateExplorerName,
                            onToggleTheme = viewModel::toggleTheme,
                            onResetProgress = viewModel::resetProgress
                        )
                    }
                }

                // Floating Reward Banner
                AnimatedVisibility(
                    visible = rewardBanner != null,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = solomonColors.gold,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rewardBanner.orEmpty(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Golden Shimmer Flash Overlay when 'ذهب' is spoken
                if (goldFlashAlpha.value > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFDE047).copy(alpha = goldFlashAlpha.value),
                                        solomonColors.gold.copy(alpha = goldFlashAlpha.value * 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }

        // Discovery Detail Modal Sheet (supports both Card tap and Golden Ring tap)
        activeDiscovery?.let { item ->
            DiscoveryDetailSheet(
                item = item,
                isFromRandomRing = isRandomRingReveal,
                isFavorite = item.id in progress.favoriteIds,
                speechState = speechState,
                speechController = speechController,
                onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                onAnotherRandom = { viewModel.onGoldenRingTapped() },
                onDismiss = { viewModel.closeDiscovery() }
            )
        }

        // Rank-Up Celebration Dialog
        rankUpNotice?.let { newRank ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissRankUpNotice() },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.WorkspacePremium,
                        contentDescription = null,
                        tint = solomonColors.goldBright
                    )
                },
                title = {
                    Text("مبارك الترقية!")
                },
                text = {
                    Text("لقد وصلت الآن إلى رتبة «${newRank.titleAr}»!\n${newRank.subtitleAr}")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissRankUpNotice() },
                        colors = ButtonDefaults.buttonColors(containerColor = solomonColors.gold)
                    ) {
                        Text("متابعة الاستكشاف", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
