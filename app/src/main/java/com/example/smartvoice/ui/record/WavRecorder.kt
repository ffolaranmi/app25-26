package com.example.smartvoice.ui.record

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

class WavRecorder(
    private val sampleRate: Int = 44100,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var recorder: AudioRecord? = null
    private var isRecording = false

    @SuppressLint("MissingPermission")
    suspend fun start(outputWav: File, seconds: Int = 5) {
        withContext(Dispatchers.IO) {
            val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = maxOf(minBuf, sampleRate * 2)

            Log.d("WavRecorder", "Starting recording with bufferSize=$bufferSize, sampleRate=$sampleRate, seconds=$seconds")

            recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("WavRecorder", "AudioRecord failed to initialize!")
                throw IOException("Failed to initialize AudioRecord")
            }

            val pcmFile = File(outputWav.parentFile, outputWav.nameWithoutExtension + ".pcm")

            try {
                recorder?.startRecording()
                isRecording = true
                Log.d("WavRecorder", "AudioRecord started recording")

                FileOutputStream(pcmFile).use { out ->
                    val buffer = ByteArray(bufferSize)
                    val totalBytesToWrite = seconds * sampleRate * 2
                    var written = 0
                    var maxAmplitude = 0
                    var audioDetected = false

                    val startTime = System.currentTimeMillis()

                    while (isRecording && written < totalBytesToWrite) {
                        val read = recorder?.read(buffer, 0, min(buffer.size, totalBytesToWrite - written)) ?: 0

                        if (read > 0) {

                            var maxInBuffer = 0
                            for (i in 0 until read step 2) {
                                if (i + 1 < read) {

                                    val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
                                    val absSample = kotlin.math.abs(sample)
                                    if (absSample > maxInBuffer) {
                                        maxInBuffer = absSample
                                    }
                                    if (absSample > 500) {
                                        audioDetected = true
                                    }
                                }
                            }

                            if (maxInBuffer > maxAmplitude) {
                                maxAmplitude = maxInBuffer
                            }

                            out.write(buffer, 0, read)
                            written += read

                            if (written % (sampleRate * 10) == 0) {
                                Log.d("WavRecorder", "Recording progress: ${written / (sampleRate * 2)}s, max amp: $maxAmplitude")
                            }
                        }
                    }

                    val elapsed = System.currentTimeMillis() - startTime

                    Log.d("WavRecorder", "=== Recording Complete ===")
                    Log.d("WavRecorder", "Wrote $written bytes in ${elapsed}ms")
                    Log.d("WavRecorder", "Max amplitude: $maxAmplitude")
                    Log.d("WavRecorder", "Audio detected: $audioDetected")

                    if (!audioDetected) {
                        Log.e("WavRecorder", "CRITICAL: No voice detected! Check microphone permissions and hardware")
                    }

                    if (maxAmplitude < 1000) {
                        Log.w("WavRecorder", "Low audio level ($maxAmplitude). Speak louder or check microphone")
                    }
                }

                stop()
                Log.d("WavRecorder", "Converting PCM to WAV...")
                pcmToWav(pcmFile, outputWav, sampleRate, 1, 16)
                pcmFile.delete()

                if (outputWav.exists()) {
                    Log.d("WavRecorder", "✓ WAV file created: ${outputWav.absolutePath} (${outputWav.length()} bytes)")
                    verifyWavHeader(outputWav)
                } else {
                    Log.e("WavRecorder", "✗ WAV file was not created!")
                }
            } catch (e: Exception) {
                Log.e("WavRecorder", "Error during recording: ${e.message}", e)
                stop()
                throw e
            }
        }
    }

    private fun verifyWavHeader(wavFile: File) {
        try {
            val header = ByteArray(12)
            wavFile.inputStream().use { it.read(header) }
            val riffSig = String(header, 0, 4, Charsets.US_ASCII)
            val waveSig = String(header, 8, 4, Charsets.US_ASCII)
            Log.d("WavRecorder", "WAV header check - RIFF: $riffSig, WAVE: $waveSig")

            if (riffSig != "RIFF" || waveSig != "WAVE") {
                Log.e("WavRecorder", "Invalid WAV header!")
            } else {
                Log.d("WavRecorder", "✓ Valid WAV header")
            }
        } catch (e: Exception) {
            Log.e("WavRecorder", "Error reading WAV header", e)
        }
    }

    fun stop() {
        isRecording = false
        recorder?.apply {
            try {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    stop()
                    Log.d("WavRecorder", "AudioRecord stopped")
                }
            } catch (e: Exception) {
                Log.e("WavRecorder", "Error stopping recorder: ${e.message}")
            }
            try {
                release()
                Log.d("WavRecorder", "AudioRecord released")
            } catch (e: Exception) {
                Log.e("WavRecorder", "Error releasing recorder: ${e.message}")
            }
        }
        recorder = null
    }

    @Throws(IOException::class)
    private fun pcmToWav(pcm: File, wav: File, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val pcmData = pcm.readBytes()

        if (pcmData.isEmpty()) {
            Log.e("WavRecorder", "PCM data is empty!")
            return
        }

        Log.d("WavRecorder", "Converting PCM to WAV: size=${pcmData.size} bytes")

        val byteRate = sampleRate * channels * bitsPerSample / 8
        val dataSize = pcmData.size
        val chunkSize = 36 + dataSize

        FileOutputStream(wav).use { out ->

            out.write("RIFF".toByteArray(Charsets.US_ASCII))
            out.write(intToLittleEndian(chunkSize))
            out.write("WAVE".toByteArray(Charsets.US_ASCII))

            out.write("fmt ".toByteArray(Charsets.US_ASCII))
            out.write(intToLittleEndian(16))
            out.write(shortToLittleEndian(1))
            out.write(shortToLittleEndian(channels.toShort()))
            out.write(intToLittleEndian(sampleRate))
            out.write(intToLittleEndian(byteRate))
            out.write(shortToLittleEndian((channels * bitsPerSample / 8).toShort()))
            out.write(shortToLittleEndian(bitsPerSample.toShort()))

            out.write("data".toByteArray(Charsets.US_ASCII))
            out.write(intToLittleEndian(dataSize))
            out.write(pcmData)
        }

        if (wav.exists()) {
            Log.d("WavRecorder", "✓ WAV file created: ${wav.absolutePath} (${wav.length()} bytes)")
        } else {
            Log.e("WavRecorder", "✗ WAV file was not created!")
        }
    }

    private fun intToLittleEndian(value: Int): ByteArray =
        byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte()
        )

    private fun shortToLittleEndian(value: Short): ByteArray =
        byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            ((value.toInt() shr 8) and 0xff).toByte()
        )
}