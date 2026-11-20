package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserProgressState

class OfflineProgressStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): UserProgressState {
        return UserProgressState(
            explorerName = prefs.getString(KEY_NAME, "المستكشف") ?: "المستكشف",
            points = prefs.getInt(KEY_POINTS, 0),
            discoveredIds = prefs.getStringSet(KEY_DISCOVERED, emptySet())?.toSet() ?: emptySet(),
            favoriteIds = prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet(),
            gamesWon = prefs.getInt(KEY_GAMES_WON, 0),
            quizBestScore = prefs.getInt(KEY_QUIZ_BEST, 0),
            memoryBestMoves = prefs.getInt(KEY_MEMORY_BEST, 0),
            secretCodesCracked = prefs.getInt(KEY_CODES_CRACKED, 0),
            solomonStagesCleared = prefs.getInt(KEY_SOLOMON_STAGES, 0),
            isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, true)
        )
    }

    fun save(state: UserProgressState) {
        prefs.edit()
            .putString(KEY_NAME, state.explorerName)
            .putInt(KEY_POINTS, state.points)
            .putStringSet(KEY_DISCOVERED, state.discoveredIds)
            .putStringSet(KEY_FAVORITES, state.favoriteIds)
            .putInt(KEY_GAMES_WON, state.gamesWon)
            .putInt(KEY_QUIZ_BEST, state.quizBestScore)
            .putInt(KEY_MEMORY_BEST, state.memoryBestMoves)
            .putInt(KEY_CODES_CRACKED, state.secretCodesCracked)
            .putInt(KEY_SOLOMON_STAGES, state.solomonStagesCleared)
            .putBoolean(KEY_DARK_THEME, state.isDarkTheme)
            .apply()
    }

    fun reset(): UserProgressState {
        val currentTheme = prefs.getBoolean(KEY_DARK_THEME, true)
        prefs.edit().clear().putBoolean(KEY_DARK_THEME, currentTheme).apply()
        return load()
    }

    private companion object {
        const val PREFS_NAME = "solomons_ring_offline_prefs"
        const val KEY_NAME = "explorer_name"
        const val KEY_POINTS = "points"
        const val KEY_DISCOVERED = "discovered_ids"
        const val KEY_FAVORITES = "favorite_ids"
        const val KEY_GAMES_WON = "games_won"
        const val KEY_QUIZ_BEST = "quiz_best_score"
        const val KEY_MEMORY_BEST = "memory_best_moves"
        const val KEY_CODES_CRACKED = "secret_codes_cracked"
        const val KEY_SOLOMON_STAGES = "solomon_stages_cleared"
        const val KEY_DARK_THEME = "is_dark_theme"
    }
}
