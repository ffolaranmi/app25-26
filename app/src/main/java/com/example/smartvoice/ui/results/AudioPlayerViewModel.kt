package com.example.smartvoice.ui.results

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class AudioPlayerViewModel : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressUpdater: Handler? = null
    private val progressRunnable = Runnable { updateProgress() }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val progress: Float = 0f,
        val error: String? = null,
        val duration: Int = 0,
        val currentPosition: Int = 0,
        val isPrepared: Boolean = false,
        val isPaused: Boolean = false
    )

    fun playRecording(context: Context, recordingPath: String) {
        viewModelScope.launch {
            try {

                _playbackState.update { PlaybackState() }

                val file = File(recordingPath)
                Log.d("AudioPlayerVM", "=== Playback Debug ===")
                Log.d("AudioPlayerVM", "Attempting to play: $recordingPath")
                Log.d("AudioPlayerVM", "File exists: ${file.exists()}")
                Log.d("AudioPlayerVM", "File size: ${file.length()} bytes")

                if (!file.exists()) {
                    _playbackState.update { it.copy(error = "File not found: $recordingPath") }
                    return@launch
                }

                if (file.length() == 0L) {
                    _playbackState.update { it.copy(error = "File is empty (0 bytes)") }
                    return@launch
                }

                try {
                    FileInputStream(file).use { fis ->
                        val header = ByteArray(12)
                        fis.read(header)
                        val riff = String(header, 0, 4)
                        val wave = String(header, 8, 4)
                        if (riff != "RIFF" || wave != "WAVE") {
                            Log.e("AudioPlayerVM", "Invalid WAV header: $riff $wave")
                            _playbackState.update { it.copy(error = "Invalid WAV file format") }
                            return@launch
                        }
                        Log.d("AudioPlayerVM", "Valid WAV file: $riff $wave")
                    }
                } catch (e: Exception) {
                    Log.e("AudioPlayerVM", "Error reading WAV header", e)
                    _playbackState.update { it.copy(error = "Cannot read audio file") }
                    return@launch
                }

                releasePlayer()

                withContext(Dispatchers.Main) {
                    try {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(recordingPath)

                            setOnErrorListener { mp, what, extra ->
                                Log.e("AudioPlayerVM", "MediaPlayer error: what=$what, extra=$extra")
                                _playbackState.update {
                                    it.copy(error = "Playback error ($what)", isPlaying = false)
                                }
                                stopProgressUpdater()
                                true
                            }

                            setOnPreparedListener {
                                Log.d("AudioPlayerVM", "MediaPlayer prepared - duration: $duration ms")
                                _playbackState.update {
                                    it.copy(
                                        isPrepared = true,
                                        duration = duration
                                    )
                                }
                                start()
                            }

                            setOnCompletionListener {
                                Log.d("AudioPlayerVM", "Playback completed")
                                stopProgressUpdater()
                                _playbackState.update {
                                    PlaybackState(duration = duration)
                                }
                                releasePlayer()
                            }

                            prepare()

                            start()
                            Log.d("AudioPlayerVM", "MediaPlayer started")

                            _playbackState.update {
                                it.copy(
                                    isPlaying = true,
                                    isPaused = false,
                                    error = null
                                )
                            }

                            startProgressUpdater()
                        }
                    } catch (e: Exception) {
                        Log.e("AudioPlayerVM", "Error setting up MediaPlayer", e)
                        _playbackState.update { it.copy(error = e.message ?: "Playback failed") }
                        releasePlayer()
                    }
                }

            } catch (e: Exception) {
                Log.e("AudioPlayerVM", "Error playing audio", e)
                _playbackState.update { it.copy(error = e.message ?: "Unknown error") }
                releasePlayer()
            }
        }
    }

    private fun startProgressUpdater() {
        stopProgressUpdater()
        progressUpdater = Handler(Looper.getMainLooper()).apply {
            post(progressRunnable)
        }
    }

    private fun stopProgressUpdater() {
        progressUpdater?.removeCallbacks(progressRunnable)
        progressUpdater = null
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                val progress = player.currentPosition.toFloat() / player.duration.toFloat()
                _playbackState.update {
                    it.copy(
                        progress = progress,
                        currentPosition = player.currentPosition
                    )
                }

                progressUpdater?.postDelayed(progressRunnable, 50)
            }
        }
    }

    fun pausePlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    pause()
                    stopProgressUpdater()
                    _playbackState.update {
                        it.copy(
                            isPlaying = false,
                            isPaused = true
                        )
                    }
                    Log.d("AudioPlayerVM", "Playback paused at ${currentPosition}ms")
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerVM", "Error pausing playback", e)
        }
    }

    fun resumePlayback() {
        try {
            mediaPlayer?.apply {
                if (!isPlaying && _playbackState.value.currentPosition > 0) {
                    start()
                    startProgressUpdater()
                    _playbackState.update {
                        it.copy(
                            isPlaying = true,
                            isPaused = false
                        )
                    }
                    Log.d("AudioPlayerVM", "Playback resumed")
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerVM", "Error resuming playback", e)
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                stop()
                stopProgressUpdater()
                _playbackState.update { PlaybackState() }
                Log.d("AudioPlayerVM", "Playback stopped")
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerVM", "Error stopping playback", e)
        } finally {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        try {
            stopProgressUpdater()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayerVM", "Error releasing player", e)
        }
        mediaPlayer = null
    }

    fun seekTo(position: Float) {
        try {
            mediaPlayer?.let { player ->
                val targetPosition = (position * player.duration).toInt()
                player.seekTo(targetPosition)
                _playbackState.update { it.copy(currentPosition = targetPosition) }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerVM", "Error seeking", e)
        }
    }

    fun clearError() {
        _playbackState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
        releasePlayer()
    }
}