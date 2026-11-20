package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.DiscoveryRepository
import com.example.data.OfflineProgressStore
import com.example.model.AppTab
import com.example.model.DiscoveryCategory
import com.example.model.DiscoveryItem
import com.example.model.ExplorerRank
import com.example.model.GameType
import com.example.model.UserProgressState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class SolomonsRingViewModel(application: Application) : AndroidViewModel(application) {

    private val store = OfflineProgressStore(application.applicationContext)

    private val _progress = MutableStateFlow(store.load())
    val progress: StateFlow<UserProgressState> = _progress.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.DISCOVER)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(DiscoveryCategory.ALL)
    val selectedCategory: StateFlow<DiscoveryCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeDiscovery = MutableStateFlow<DiscoveryItem?>(null)
    val activeDiscovery: StateFlow<DiscoveryItem?> = _activeDiscovery.asStateFlow()

    private val _isRandomRingReveal = MutableStateFlow(false)
    val isRandomRingReveal: StateFlow<Boolean> = _isRandomRingReveal.asStateFlow()

    private val _activeGame = MutableStateFlow<GameType?>(null)
    val activeGame: StateFlow<GameType?> = _activeGame.asStateFlow()

    private val _rewardBanner = MutableStateFlow<String?>(null)
    val rewardBanner: StateFlow<String?> = _rewardBanner.asStateFlow()

    private val _rankUpNotice = MutableStateFlow<ExplorerRank?>(null)
    val rankUpNotice: StateFlow<ExplorerRank?> = _rankUpNotice.asStateFlow()

    val allDiscoveries: List<DiscoveryItem> = DiscoveryRepository.discoveries

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
        if (tab != AppTab.PLAY) {
            _activeGame.value = null
        }
    }

    fun selectCategory(category: DiscoveryCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onGoldenRingTapped() {
        val currentDiscovered = _progress.value.discoveredIds
        val undiscovered = allDiscoveries.filterNot { it.id in currentDiscovered }
        val pool = undiscovered.ifEmpty { allDiscoveries }
        val lastId = _activeDiscovery.value?.id
        val filteredPool = if (pool.size > 1 && lastId != null) {
            pool.filterNot { it.id == lastId }
        } else {
            pool
        }
        val picked = filteredPool[Random.nextInt(filteredPool.size)]
        openDiscovery(picked, fromGoldenRing = true)
    }

    fun openDiscovery(item: DiscoveryItem, fromGoldenRing: Boolean = false) {
        _isRandomRingReveal.value = fromGoldenRing
        _activeDiscovery.value = item
        val wasDiscovered = item.id in _progress.value.discoveredIds
        if (!wasDiscovered) {
            updateProgress { current ->
                current.copy(
                    points = current.points + item.pointsReward,
                    discoveredIds = current.discoveredIds + item.id
                )
            }
            _rewardBanner.value = "+${item.pointsReward} نقطة • تم توثيق اكتشاف جديد!"
        }
    }

    fun closeDiscovery() {
        _activeDiscovery.value = null
        _isRandomRingReveal.value = false
    }

    fun toggleFavorite(discoveryId: String) {
        updateProgress { current ->
            val updatedFavs = if (discoveryId in current.favoriteIds) {
                current.favoriteIds - discoveryId
            } else {
                current.favoriteIds + discoveryId
            }
            current.copy(favoriteIds = updatedFavs)
        }
    }

    fun openGame(game: GameType) {
        _activeGame.value = game
    }

    fun closeGame() {
        _activeGame.value = null
    }

    fun recordQuizCompletion(earnedPoints: Int, score: Int, won: Boolean) {
        updateProgress { current ->
            current.copy(
                points = current.points + earnedPoints,
                gamesWon = if (won) current.gamesWon + 1 else current.gamesWon,
                quizBestScore = maxOf(current.quizBestScore, score)
            )
        }
        if (earnedPoints > 0) {
            _rewardBanner.value = "+$earnedPoints نقطة من تحدي الأسئلة!"
        }
    }

    fun recordMemoryCompletion(earnedPoints: Int, moves: Int) {
        updateProgress { current ->
            val best = if (current.memoryBestMoves == 0) moves else minOf(current.memoryBestMoves, moves)
            current.copy(
                points = current.points + earnedPoints,
                gamesWon = current.gamesWon + 1,
                memoryBestMoves = best
            )
        }
        _rewardBanner.value = "+$earnedPoints نقطة من لعبة الذاكرة!"
    }

    fun recordSolomonStageCleared(earnedPoints: Int, stageNumber: Int) {
        updateProgress { current ->
            current.copy(
                points = current.points + earnedPoints,
                gamesWon = current.gamesWon + 1,
                solomonStagesCleared = maxOf(current.solomonStagesCleared, stageNumber)
            )
        }
        _rewardBanner.value = "+$earnedPoints نقطة • فككت ختم المرحلة $stageNumber!"
    }

    fun recordSecretNumberCracked(earnedPoints: Int) {
        updateProgress { current ->
            current.copy(
                points = current.points + earnedPoints,
                gamesWon = current.gamesWon + 1,
                secretCodesCracked = current.secretCodesCracked + 1
            )
        }
        _rewardBanner.value = "+$earnedPoints نقطة • تم كسر الرقم السري!"
    }

    fun updateExplorerName(newName: String) {
        val cleaned = newName.trim().ifEmpty { "المستكشف" }
        updateProgress { it.copy(explorerName = cleaned) }
    }

    fun toggleTheme() {
        updateProgress { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun resetProgress() {
        val fresh = store.reset()
        _progress.value = fresh
        _rewardBanner.value = "تمت إعادة ضبط التقدم بنجاح"
    }

    fun dismissRewardBanner() {
        _rewardBanner.value = null
    }

    fun dismissRankUpNotice() {
        _rankUpNotice.value = null
    }

    private fun updateProgress(transform: (UserProgressState) -> UserProgressState) {
        val previousRank = _progress.value.rank
        _progress.update { current ->
            val updated = transform(current)
            store.save(updated)
            updated
        }
        val newRank = _progress.value.rank
        if (newRank.ordinal > previousRank.ordinal) {
            _rankUpNotice.value = newRank
        }
    }
}
