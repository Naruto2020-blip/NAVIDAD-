package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.R
import com.example.model.CarolTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val currentTrackId: String? = null,
    val progressSeconds: Int = 0,
    val durationSeconds: Int = 1,
)

class FestiveAudioManager(private val context: Context) {

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume: Float = 0.72f
    private var loadedTrackId: String? = null
    private var pausedForBackground: Boolean = false

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(audioAttributes)
        .build()
    private var bellSoundId: Int = 0
    private var bellLoaded: Boolean = false

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == bellSoundId) {
                bellLoaded = true
            }
        }
        bellSoundId = soundPool.load(context, R.raw.sfx_campanillas, 1)
    }

    fun startOrUpdateTrack(
        track: CarolTrack,
        shouldPlay: Boolean,
        volume: Float,
    ) {
        currentVolume = volume.coerceIn(0f, 1f)

        if (loadedTrackId != track.id || mediaPlayer == null) {
            releasePlayer()
            val player = MediaPlayer.create(context, track.rawResId)
            if (player != null) {
                player.setAudioAttributes(audioAttributes)
                player.isLooping = true
                player.setVolume(currentVolume, currentVolume)
                mediaPlayer = player
                loadedTrackId = track.id
                if (shouldPlay) {
                    player.start()
                    pausedForBackground = false
                }
            }
        } else {
            mediaPlayer?.let { player ->
                player.setVolume(currentVolume, currentVolume)
                if (shouldPlay && !player.isPlaying) {
                    player.start()
                    pausedForBackground = false
                } else if (!shouldPlay && player.isPlaying) {
                    player.pause()
                    pausedForBackground = false
                }
            }
        }
        syncState(track)
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(currentVolume, currentVolume)
    }

    fun refreshProgress(track: CarolTrack) {
        syncState(track)
    }

    fun ringBells() {
        if (bellLoaded && bellSoundId != 0) {
            val bellVol = (currentVolume.coerceAtLeast(0.35f)).coerceIn(0f, 1f)
            soundPool.play(bellSoundId, bellVol, bellVol, 1, 0, 1.0f)
        }
    }

    fun onEnterBackground() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            pausedForBackground = true
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
        }
    }

    fun onEnterForeground(shouldPlay: Boolean) {
        val player = mediaPlayer ?: return
        if ((pausedForBackground || shouldPlay) && shouldPlay && !player.isPlaying) {
            player.start()
            pausedForBackground = false
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
        }
    }

    private fun syncState(track: CarolTrack) {
        val player = mediaPlayer
        val isPlaying = try {
            player?.isPlaying == true
        } catch (_: IllegalStateException) {
            false
        }
        val posSec = try {
            (player?.currentPosition ?: 0) / 1000
        } catch (_: IllegalStateException) {
            0
        }
        val durSec = try {
            ((player?.duration ?: (track.durationSeconds * 1000)) / 1000).coerceAtLeast(1)
        } catch (_: IllegalStateException) {
            track.durationSeconds.coerceAtLeast(1)
        }
        _playbackState.value = AudioPlaybackState(
            isPlaying = isPlaying,
            currentTrackId = track.id,
            progressSeconds = posSec,
            durationSeconds = durSec,
        )
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
        } catch (_: IllegalStateException) {
        }
        mediaPlayer?.release()
        mediaPlayer = null
        loadedTrackId = null
    }

    fun releaseAll() {
        releasePlayer()
        soundPool.release()
    }
}
