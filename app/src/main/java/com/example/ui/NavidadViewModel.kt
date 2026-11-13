package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackState
import com.example.audio.FestiveAudioManager
import com.example.data.FestivePreferences
import com.example.data.FestivePreferencesRepository
import com.example.model.CarolTrack
import com.example.model.CountdownSnapshot
import com.example.model.FestiveCatalog
import com.example.model.FestiveMessage
import com.example.model.FestiveTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class NavidadUiState(
    val countdown: CountdownSnapshot,
    val preferences: FestivePreferences,
    val currentTrack: CarolTrack,
    val audioState: AudioPlaybackState,
    val dailyMessage: FestiveMessage,
    val bellRingCount: Int = 0,
)

class NavidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FestivePreferencesRepository(application)
    private val audioManager = FestiveAudioManager(application)

    private val nowMillisFlow = MutableStateFlow(System.currentTimeMillis())
    private val bellRingCountFlow = MutableStateFlow(0)

    private val preferencesFlow = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FestivePreferences(),
    )

    val uiState: StateFlow<NavidadUiState> = combine(
        nowMillisFlow,
        preferencesFlow,
        audioManager.playbackState,
        bellRingCountFlow,
    ) { nowMillis, prefs, audioState, bellCount ->
        val track = FestiveCatalog.carols.firstOrNull { it.id == prefs.selectedTrackId }
            ?: FestiveCatalog.carols.first()

        val countdown = computeCountdown(nowMillis, prefs.selectedTarget)
        val dayIndex = ((nowMillis / 86_400_000L) % FestiveCatalog.dailyMessages.size).toInt()
        val message = FestiveCatalog.dailyMessages[dayIndex.coerceAtLeast(0)]

        NavidadUiState(
            countdown = countdown,
            preferences = prefs,
            currentTrack = track,
            audioState = audioState,
            dailyMessage = message,
            bellRingCount = bellCount,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NavidadUiState(
            countdown = computeCountdown(System.currentTimeMillis(), FestiveTarget.NAVIDAD),
            preferences = FestivePreferences(),
            currentTrack = FestiveCatalog.carols.first(),
            audioState = AudioPlaybackState(),
            dailyMessage = FestiveCatalog.dailyMessages.first(),
        ),
    )

    init {
        // Real-time 1-second countdown and audio progress ticker
        viewModelScope.launch {
            while (true) {
                nowMillisFlow.value = System.currentTimeMillis()
                val prefs = preferencesFlow.value
                val track = FestiveCatalog.carols.firstOrNull { it.id == prefs.selectedTrackId }
                    ?: FestiveCatalog.carols.first()
                audioManager.refreshProgress(track)
                delay(500L)
            }
        }

        // Sync MediaPlayer whenever track, play toggle, or volume preferences change
        viewModelScope.launch {
            repository.preferencesFlow.collect { prefs ->
                val track = FestiveCatalog.carols.firstOrNull { it.id == prefs.selectedTrackId }
                    ?: FestiveCatalog.carols.first()
                audioManager.startOrUpdateTrack(
                    track = track,
                    shouldPlay = prefs.musicEnabled,
                    volume = prefs.volume,
                )
            }
        }
    }

    fun selectTarget(target: FestiveTarget) {
        viewModelScope.launch {
            repository.setSelectedTarget(target)
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            val nextPlay = !preferencesFlow.value.musicEnabled
            repository.setMusicEnabled(nextPlay)
        }
    }

    fun selectTrack(track: CarolTrack) {
        viewModelScope.launch {
            repository.setSelectedTrack(track.id)
            repository.setMusicEnabled(true)
        }
    }

    fun nextTrack() {
        val carols = FestiveCatalog.carols
        val currentId = preferencesFlow.value.selectedTrackId
        val idx = carols.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        val next = carols[(idx + 1) % carols.size]
        selectTrack(next)
    }

    fun previousTrack() {
        val carols = FestiveCatalog.carols
        val currentId = preferencesFlow.value.selectedTrackId
        val idx = carols.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        val prev = carols[(idx - 1 + carols.size) % carols.size]
        selectTrack(prev)
    }

    fun setVolume(volume: Float) {
        audioManager.setVolume(volume)
        viewModelScope.launch {
            repository.setVolume(volume)
        }
    }

    fun toggleSnow() {
        viewModelScope.launch {
            repository.setSnowEnabled(!preferencesFlow.value.snowEnabled)
        }
    }

    fun toggleAmbiance() {
        viewModelScope.launch {
            repository.setDarkAmbiance(!preferencesFlow.value.darkAmbiance)
        }
    }

    fun ringBells() {
        audioManager.ringBells()
        bellRingCountFlow.value += 1
    }

    fun onAppForeground() {
        audioManager.onEnterForeground(shouldPlay = preferencesFlow.value.musicEnabled)
    }

    fun onAppBackground() {
        audioManager.onEnterBackground()
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.releaseAll()
    }

    companion object {
        private val SPANISH_MONTHS = arrayOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
        )

        fun computeCountdown(nowMillis: Long, target: FestiveTarget): CountdownSnapshot {
            val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val currentMonth = now.get(Calendar.MONTH) + 1
            val currentDay = now.get(Calendar.DAY_OF_MONTH)

            val isCelebrationToday = currentMonth == target.month && currentDay == target.dayOfMonth
            if (isCelebrationToday) {
                val monthName = SPANISH_MONTHS[(target.month - 1).coerceIn(0, 11)]
                val year = now.get(Calendar.YEAR)
                return CountdownSnapshot(
                    target = target,
                    days = 0L,
                    hours = 0L,
                    minutes = 0L,
                    seconds = 0L,
                    progressFraction = 1f,
                    isCelebrationToday = true,
                    targetDateLabel = "${target.dayOfMonth} de $monthName de $year",
                )
            }

            val targetCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.MONTH, target.month - 1)
                set(Calendar.DAY_OF_MONTH, target.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (!targetCal.after(now)) {
                targetCal.add(Calendar.YEAR, 1)
            }

            val prevYearCal = (targetCal.clone() as Calendar).apply {
                add(Calendar.YEAR, -1)
            }

            val diffMillis = (targetCal.timeInMillis - nowMillis).coerceAtLeast(0L)
            val totalSeconds = diffMillis / 1000L
            val days = totalSeconds / 86_400L
            val hours = (totalSeconds % 86_400L) / 3_600L
            val minutes = (totalSeconds % 3_600L) / 60L
            val seconds = totalSeconds % 60L

            val yearSpan = (targetCal.timeInMillis - prevYearCal.timeInMillis).coerceAtLeast(1L)
            val elapsed = (nowMillis - prevYearCal.timeInMillis).coerceIn(0L, yearSpan)
            val progress = (elapsed.toFloat() / yearSpan.toFloat()).coerceIn(0f, 1f)

            val monthName = SPANISH_MONTHS[(target.month - 1).coerceIn(0, 11)]
            val targetYear = targetCal.get(Calendar.YEAR)

            return CountdownSnapshot(
                target = target,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                progressFraction = progress,
                isCelebrationToday = false,
                targetDateLabel = "${target.dayOfMonth} de $monthName de $targetYear",
            )
        }
    }
}
