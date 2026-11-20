package com.example.audio

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class SpeechSpeed(val labelAr: String, val rate: Float, val msPerWordFallback: Long) {
    SLOW_KIDS("بطيء للأطفال الصغار", 0.68f, 520L),
    NORMAL("عادي", 1.0f, 340L),
    FAST("سريع", 1.38f, 230L)
}

enum class VoicePersona(val labelAr: String, val pitch: Float) {
    GIRL("صوت طفلة", 1.48f),
    MAN("صوت رجل", 0.78f),
    WOMAN("صوت امرأة", 1.12f)
}

data class WordToken(
    val text: String,
    val start: Int,
    val end: Int
)

data class SpeechUiState(
    val activeSourceId: String? = null,
    val currentText: String = "",
    val activeRange: IntRange? = null,
    val isSpeaking: Boolean = false,
    val isPaused: Boolean = false,
    val isOutroSpeaking: Boolean = false,
    val isCompleted: Boolean = false,
    val speed: SpeechSpeed = SpeechSpeed.NORMAL,
    val persona: VoicePersona = VoicePersona.WOMAN,
    val activeReactionLabel: String? = null,
    val goldFlashCounter: Int = 0
)

class OfflineSpeechController(context: Context) {

    companion object {
        const val OUTRO_PHRASE = "خلصت! أحسنت يا مستكشف"
        private const val UTTERANCE_MAIN = "solomon_main"
        private const val UTTERANCE_OUTRO = "solomon_outro"
        private val WORD_REGEX = Regex("\\S+")
    }

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(SpeechUiState())
    val state: StateFlow<SpeechUiState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingAutoStart = false

    private var currentOffsetBase = 0
    private var lastValidWordStart = 0
    private var lastTriggeredTokenStart = -1
    private var nativeRangeReceivedForUtterance = false
    private var fallbackJob: Job? = null
    private var reactionClearJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        tts = TextToSpeech(appContext) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
            if (ttsReady) {
                configureTtsEngine()
                if (pendingAutoStart) {
                    pendingAutoStart = false
                    val s = _state.value
                    if (s.isSpeaking && s.currentText.isNotEmpty()) {
                        startSpeakingFrom(s.currentText, lastValidWordStart)
                    }
                }
            }
        }
    }

    private fun configureTtsEngine() {
        val engine = tts ?: return
        val arabicLocale = Locale.forLanguageTag("ar")
        val langResult = engine.setLanguage(arabicLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Keep default locale if Arabic pack is absent on minimal emulator, while preserving pitch/rate
            engine.language = Locale.getDefault()
        }
        applyPersonaAndSpeed(engine)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                scope.launch {
                    if (utteranceId == UTTERANCE_MAIN) {
                        nativeRangeReceivedForUtterance = false
                        startFallbackHighlighterIfNeeded()
                    } else if (utteranceId == UTTERANCE_OUTRO) {
                        fallbackJob?.cancel()
                        _state.update {
                            it.copy(
                                isOutroSpeaking = true,
                                activeRange = null,
                                activeReactionLabel = "🎉 $OUTRO_PHRASE"
                            )
                        }
                    }
                }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                if (utteranceId != UTTERANCE_MAIN) return
                scope.launch {
                    nativeRangeReceivedForUtterance = true
                    fallbackJob?.cancel()
                    val fullText = _state.value.currentText
                    val globalStart = (currentOffsetBase + start).coerceIn(0, fullText.length)
                    val globalEnd = (currentOffsetBase + end).coerceIn(globalStart, fullText.length)
                    onWordActive(fullText, globalStart, globalEnd)
                }
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    fallbackJob?.cancel()
                    if (utteranceId == UTTERANCE_MAIN) {
                        speakOutro()
                    } else if (utteranceId == UTTERANCE_OUTRO) {
                        _state.update {
                            it.copy(
                                isSpeaking = false,
                                isPaused = false,
                                isOutroSpeaking = false,
                                isCompleted = true,
                                activeRange = null,
                                activeReactionLabel = "🎉 $OUTRO_PHRASE"
                            )
                        }
                        lastValidWordStart = 0
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                handleTtsError(utteranceId)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                handleTtsError(utteranceId)
            }
        })
    }

    private fun handleTtsError(utteranceId: String?) {
        scope.launch {
            if (utteranceId == UTTERANCE_MAIN && _state.value.isSpeaking) {
                // Run pure offline visual word-by-word reading + tactile triggers even if device lacks TTS voice
                startOfflineSimulatedReading(fromIndex = lastValidWordStart)
            } else {
                _state.update {
                    it.copy(
                        isSpeaking = false,
                        isPaused = false,
                        isOutroSpeaking = false,
                        activeRange = null
                    )
                }
            }
        }
    }

    private fun applyPersonaAndSpeed(engine: TextToSpeech) {
        val s = _state.value
        engine.setSpeechRate(s.speed.rate)
        engine.setPitch(s.persona.pitch)
        // Best-effort matching of local offline voice if multiple Arabic voices are installed
        val voices = runCatching { engine.voices }.getOrNull().orEmpty()
        val arabicVoices = voices.filter { it.locale.language == "ar" && !it.isNetworkConnectionRequired }
        if (arabicVoices.isNotEmpty()) {
            val preferred = when (s.persona) {
                VoicePersona.MAN -> arabicVoices.firstOrNull {
                    it.name.contains("male", ignoreCase = true) ||
                        it.name.contains("-m-", ignoreCase = true) ||
                        it.name.contains("ard", ignoreCase = true)
                }
                VoicePersona.WOMAN, VoicePersona.GIRL -> arabicVoices.firstOrNull {
                    it.name.contains("female", ignoreCase = true) ||
                        it.name.contains("-f-", ignoreCase = true) ||
                        it.name.contains("arc", ignoreCase = true)
                }
            }
            if (preferred != null) {
                runCatching { engine.voice = preferred }
            }
        }
    }

    fun speak(text: String, sourceId: String) {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return

        val current = _state.value
        if (current.activeSourceId == sourceId && current.isPaused && current.currentText == cleaned) {
            resume()
            return
        }

        stopInternal(resetSource = false)
        lastValidWordStart = 0
        lastTriggeredTokenStart = -1

        _state.update {
            it.copy(
                activeSourceId = sourceId,
                currentText = cleaned,
                activeRange = null,
                isSpeaking = true,
                isPaused = false,
                isOutroSpeaking = false,
                isCompleted = false,
                activeReactionLabel = null
            )
        }

        if (ttsReady && tts != null) {
            startSpeakingFrom(cleaned, 0)
        } else {
            pendingAutoStart = true
            // If TTS takes too long or isn't available on the environment, fallback after short grace
            scope.launch {
                delay(450)
                if (pendingAutoStart && _state.value.isSpeaking && _state.value.activeSourceId == sourceId) {
                    pendingAutoStart = false
                    startOfflineSimulatedReading(fromIndex = 0)
                }
            }
        }
    }

    fun pause() {
        val current = _state.value
        if (!current.isSpeaking || current.isPaused) return
        fallbackJob?.cancel()
        runCatching { tts?.stop() }
        _state.update {
            it.copy(
                isSpeaking = false,
                isPaused = true,
                isOutroSpeaking = false
            )
        }
    }

    fun resume() {
        val current = _state.value
        if (!current.isPaused || current.currentText.isEmpty()) return
        _state.update {
            it.copy(
                isSpeaking = true,
                isPaused = false,
                isCompleted = false
            )
        }
        if (ttsReady && tts != null) {
            startSpeakingFrom(current.currentText, lastValidWordStart)
        } else {
            startOfflineSimulatedReading(fromIndex = lastValidWordStart)
        }
    }

    fun stop() {
        stopInternal(resetSource = true)
    }

    fun setSpeed(speed: SpeechSpeed) {
        val wasSpeaking = _state.value.isSpeaking && !_state.value.isOutroSpeaking
        val activeSource = _state.value.activeSourceId
        val text = _state.value.currentText
        _state.update { it.copy(speed = speed) }
        tts?.let { if (ttsReady) applyPersonaAndSpeed(it) }
        if (wasSpeaking && activeSource != null && text.isNotEmpty()) {
            fallbackJob?.cancel()
            runCatching { tts?.stop() }
            if (ttsReady && tts != null) {
                startSpeakingFrom(text, lastValidWordStart)
            } else {
                startOfflineSimulatedReading(fromIndex = lastValidWordStart)
            }
        }
    }

    fun setPersona(persona: VoicePersona) {
        val wasSpeaking = _state.value.isSpeaking && !_state.value.isOutroSpeaking
        val activeSource = _state.value.activeSourceId
        val text = _state.value.currentText
        _state.update { it.copy(persona = persona) }
        tts?.let { if (ttsReady) applyPersonaAndSpeed(it) }
        if (wasSpeaking && activeSource != null && text.isNotEmpty()) {
            fallbackJob?.cancel()
            runCatching { tts?.stop() }
            if (ttsReady && tts != null) {
                startSpeakingFrom(text, lastValidWordStart)
            } else {
                startOfflineSimulatedReading(fromIndex = lastValidWordStart)
            }
        }
    }

    private fun stopInternal(resetSource: Boolean) {
        pendingAutoStart = false
        fallbackJob?.cancel()
        runCatching { tts?.stop() }
        lastValidWordStart = 0
        lastTriggeredTokenStart = -1
        _state.update {
            it.copy(
                activeSourceId = if (resetSource) null else it.activeSourceId,
                activeRange = null,
                isSpeaking = false,
                isPaused = false,
                isOutroSpeaking = false,
                isCompleted = false,
                activeReactionLabel = null
            )
        }
    }

    private fun startSpeakingFrom(fullText: String, charOffset: Int) {
        val engine = tts ?: return
        applyPersonaAndSpeed(engine)
        val safeOffset = charOffset.coerceIn(0, (fullText.length - 1).coerceAtLeast(0))
        currentOffsetBase = safeOffset
        val slice = fullText.substring(safeOffset)
        if (slice.isBlank()) {
            speakOutro()
            return
        }
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_MAIN)
        }
        val result = engine.speak(slice, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_MAIN)
        if (result == TextToSpeech.ERROR) {
            startOfflineSimulatedReading(fromIndex = safeOffset)
        }
    }

    private fun speakOutro() {
        val engine = tts
        if (ttsReady && engine != null) {
            applyPersonaAndSpeed(engine)
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_OUTRO)
            }
            val result = engine.speak(OUTRO_PHRASE, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_OUTRO)
            if (result == TextToSpeech.ERROR) {
                finishWithOutroFallback()
            }
        } else {
            finishWithOutroFallback()
        }
    }

    private fun finishWithOutroFallback() {
        scope.launch {
            _state.update {
                it.copy(
                    isOutroSpeaking = true,
                    activeRange = null,
                    activeReactionLabel = "🎉 $OUTRO_PHRASE"
                )
            }
            delay(1400)
            _state.update {
                it.copy(
                    isSpeaking = false,
                    isPaused = false,
                    isOutroSpeaking = false,
                    isCompleted = true,
                    activeRange = null,
                    activeReactionLabel = "🎉 $OUTRO_PHRASE"
                )
            }
            lastValidWordStart = 0
        }
    }

    private fun startFallbackHighlighterIfNeeded() {
        fallbackJob?.cancel()
        fallbackJob = scope.launch {
            delay(380)
            if (!nativeRangeReceivedForUtterance && _state.value.isSpeaking && !_state.value.isOutroSpeaking) {
                runWordHighlightLoop(fromIndex = currentOffsetBase, triggerOutroAtEnd = false)
            }
        }
    }

    private fun startOfflineSimulatedReading(fromIndex: Int) {
        fallbackJob?.cancel()
        fallbackJob = scope.launch {
            runWordHighlightLoop(fromIndex = fromIndex, triggerOutroAtEnd = true)
        }
    }

    private suspend fun runWordHighlightLoop(fromIndex: Int, triggerOutroAtEnd: Boolean) {
        val fullText = _state.value.currentText
        val tokens = WORD_REGEX.findAll(fullText)
            .map { WordToken(it.value, it.range.first, it.range.last + 1) }
            .filter { it.end > fromIndex }
            .toList()

        for (token in tokens) {
            if (!_state.value.isSpeaking || _state.value.isOutroSpeaking) return
            onWordActive(fullText, token.start, token.end)
            val baseDelay = _state.value.speed.msPerWordFallback
            val lengthFactor = (token.text.length.coerceIn(2, 10) * 18L)
            delay(baseDelay + lengthFactor)
        }

        if (triggerOutroAtEnd && _state.value.isSpeaking) {
            finishWithOutroFallback()
        }
    }

    private fun onWordActive(fullText: String, rawStart: Int, rawEnd: Int) {
        if (rawStart >= fullText.length) return
        // Snap range cleanly to full word boundaries
        var start = rawStart.coerceIn(0, fullText.length - 1)
        var end = rawEnd.coerceIn(start + 1, fullText.length)
        while (start > 0 && !fullText[start - 1].isWhitespace()) start--
        while (end < fullText.length && !fullText[end].isWhitespace()) end++

        lastValidWordStart = start
        _state.update { it.copy(activeRange = start until end) }

        if (start != lastTriggeredTokenStart) {
            lastTriggeredTokenStart = start
            val word = fullText.substring(start, end)
            checkKeywordReactions(word)
        }
    }

    private fun checkKeywordReactions(rawWord: String) {
        val normalized = rawWord
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "") // strip Arabic diacritics
            .replace(Regex("[،.؛:!?؟«»()\\[\\]\"']"), "")

        when {
            normalized.contains("أخطبوط") || normalized.contains("اخطبوط") -> {
                vibratePulse(durationMs = 200L, amplitude = 135)
                showReactionLabel("🐙 اهتزاز الأخطبوط!")
            }
            normalized.contains("نار") -> {
                vibrateStrongFire()
                showReactionLabel("🔥 نار! اهتزاز قوي!")
            }
            normalized.contains("ذهب") -> {
                _state.update { it.copy(goldFlashCounter = it.goldFlashCounter + 1) }
                showReactionLabel("✨ ذهب! لمعان الشاشة!")
            }
        }
    }

    private fun showReactionLabel(label: String) {
        reactionClearJob?.cancel()
        _state.update { it.copy(activeReactionLabel = label) }
        reactionClearJob = scope.launch {
            delay(1600)
            if (_state.value.activeReactionLabel == label) {
                _state.update { it.copy(activeReactionLabel = null) }
            }
        }
    }

    private fun vibratePulse(durationMs: Long, amplitude: Int) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        }
    }

    private fun vibrateStrongFire() {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0L, 160L, 50L, 260L)
                val amps = intArrayOf(0, 255, 0, 255)
                vib.vibrate(VibrationEffect.createWaveform(timings, amps, -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(450L)
            }
        }
    }

    fun release() {
        fallbackJob?.cancel()
        reactionClearJob?.cancel()
        scope.cancel()
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
    }
}

fun buildHighlightedText(
    text: String,
    activeRange: IntRange?,
    highlightBg: Color = Color(0xFFFDE047),
    highlightText: Color = Color(0xFF111827)
): AnnotatedString {
    if (activeRange == null || activeRange.first < 0 || activeRange.last >= text.length || activeRange.first > activeRange.last) {
        return AnnotatedString(text)
    }
    return buildAnnotatedString {
        append(text)
        addStyle(
            style = SpanStyle(
                background = highlightBg,
                color = highlightText,
                fontWeight = FontWeight.Bold
            ),
            start = activeRange.first,
            end = (activeRange.last + 1).coerceAtMost(text.length)
        )
    }
}
