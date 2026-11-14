package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.R
import com.example.model.BackgroundSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FestiveAudioManager(private val context: Context) {

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared: Boolean = false
    private var isPreparing: Boolean = false
    private var desiredPlaying: Boolean = false
    private var pausedForBackground: Boolean = false
    private val backgroundVolume = 0.72f

    private val _currentSong = MutableStateFlow(BackgroundSong.CAMPANA_SOBRE_CAMPANA)
    val currentSong: StateFlow<BackgroundSong> = _currentSong.asStateFlow()

    private val soundPool: SoundPool? = try {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
    } catch (_: Exception) {
        null
    }
    private var bellSoundId: Int = 0
    private var bellLoaded: Boolean = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        try {
            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == bellSoundId) {
                    bellLoaded = true
                }
            }
            bellSoundId = soundPool?.load(context, R.raw.sfx_campanillas, 1) ?: 0
        } catch (_: Exception) {
            bellSoundId = 0
            bellLoaded = false
        }
    }

    fun setPlaybackEnabled(shouldPlay: Boolean) {
        desiredPlaying = shouldPlay
        pausedForBackground = false

        if (mediaPlayer == null && !isPreparing) {
            initAndPreparePlayer(_currentSong.value)
            return
        }

        if (!isPrepared) {
            _isPlaying.value = shouldPlay
            return
        }

        val player = mediaPlayer ?: return
        try {
            if (shouldPlay && !player.isPlaying) {
                player.start()
            } else if (!shouldPlay && player.isPlaying) {
                player.pause()
            }
            _isPlaying.value = player.isPlaying
        } catch (_: Exception) {
            _isPlaying.value = false
        }
    }

    fun nextSong() {
        val songs = BackgroundSong.entries
        val nextIndex = (songs.indexOf(_currentSong.value) + 1) % songs.size
        val next = songs[nextIndex]
        _currentSong.value = next
        desiredPlaying = true
        pausedForBackground = false
        releasePlayerOnly()
        initAndPreparePlayer(next)
    }

    private fun rawResForSong(song: BackgroundSong): Int = when (song) {
        BackgroundSong.CAMPANA_SOBRE_CAMPANA -> R.raw.carol_campana_sobre_campana
        BackgroundSong.NOCHE_DE_PAZ -> R.raw.carol_noche_de_paz
    }

    private fun initAndPreparePlayer(song: BackgroundSong) {
        isPreparing = true
        isPrepared = false
        try {
            val afd = context.resources.openRawResourceFd(rawResForSong(song))
            val player = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = false
                setVolume(backgroundVolume, backgroundVolume)
                setOnPreparedListener { mp ->
                    isPreparing = false
                    isPrepared = true
                    try {
                        if (desiredPlaying && !pausedForBackground) {
                            mp.start()
                        }
                        _isPlaying.value = mp.isPlaying
                    } catch (_: Exception) {
                        _isPlaying.value = false
                    }
                }
                setOnCompletionListener {
                    if (desiredPlaying && !pausedForBackground) {
                        nextSong()
                    }
                }
                setOnErrorListener { _, _, _ ->
                    isPreparing = false
                    isPrepared = false
                    _isPlaying.value = false
                    true
                }
            }
            afd.close()
            mediaPlayer = player
            player.prepareAsync()
        } catch (_: Exception) {
            isPreparing = false
            isPrepared = false
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
        }
    }

    private fun releasePlayerOnly() {
        isPreparing = false
        isPrepared = false
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    fun ringBells() {
        try {
            if (bellLoaded && bellSoundId != 0) {
                soundPool?.play(bellSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
            }
        } catch (_: Exception) {
        }
    }

    fun onEnterBackground() {
        pausedForBackground = true
        val player = mediaPlayer ?: return
        try {
            if (isPrepared && player.isPlaying) {
                player.pause()
            }
        } catch (_: Exception) {
        }
        _isPlaying.value = false
    }

    fun onEnterForeground(shouldPlay: Boolean) {
        pausedForBackground = false
        setPlaybackEnabled(shouldPlay)
    }

    fun releaseAll() {
        releasePlayerOnly()
        try {
            soundPool?.release()
        } catch (_: Exception) {
        }
    }
}
