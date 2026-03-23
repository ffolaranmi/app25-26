package com.example.smartvoice.ui.record

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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

            recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
                throw IOException("Failed to initialize AudioRecord")
            }

            val pcmFile = File(outputWav.parentFile, outputWav.nameWithoutExtension + ".pcm")

            try {
                recorder?.startRecording()
                isRecording = true

                FileOutputStream(pcmFile).use { out ->
                    val buffer = ByteArray(bufferSize)
                    val totalBytesToWrite = seconds * sampleRate * 2
                    var written = 0

                    while (isRecording && written < totalBytesToWrite) {
                        val read = recorder?.read(buffer, 0, min(buffer.size, totalBytesToWrite - written)) ?: 0
                        if (read > 0) {
                            out.write(buffer, 0, read)
                            written += read
                        }
                    }
                }

                stop()
                pcmToWav(pcmFile, outputWav, sampleRate, 1, 16)
                pcmFile.delete()
            } catch (e: Exception) {
                stop()
                throw e
            }
        }
    }

    fun stop() {
        isRecording = false
        recorder?.apply {
            try {
                if (state == AudioRecord.STATE_INITIALIZED) stop()
            } catch (_: Exception) {}
            try {
                release()
            } catch (_: Exception) {}
        }
        recorder = null
    }

    @Throws(IOException::class)
    private fun pcmToWav(pcm: File, wav: File, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val pcmData = pcm.readBytes()
        if (pcmData.isEmpty()) return

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