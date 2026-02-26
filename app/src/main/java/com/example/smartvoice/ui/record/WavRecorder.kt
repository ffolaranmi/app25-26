package com.example.smartvoice.ui.record

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

class WavRecorder(
    private val sampleRate: Int = 22050,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var recorder: AudioRecord? = null
    private var isRecording = false

    @SuppressLint("MissingPermission")
    fun start(outputWav: File, seconds: Int = 10) {
        val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = maxOf(minBuf, sampleRate * 2)

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val pcmFile = File(outputWav.parentFile, outputWav.nameWithoutExtension + ".pcm")

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
    }

    fun stop() {
        isRecording = false
        recorder?.apply {
            try { stop() } catch (_: Exception) {}
            release()
        }
        recorder = null
    }

    @Throws(IOException::class)
    private fun pcmToWav(pcm: File, wav: File, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val pcmData = pcm.readBytes()
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val dataSize = pcmData.size
        val chunkSize = 36 + dataSize

        FileOutputStream(wav).use { out ->
            out.write("RIFF".toByteArray())
            out.write(intToLittleEndian(chunkSize))
            out.write("WAVE".toByteArray())

            out.write("fmt ".toByteArray())
            out.write(intToLittleEndian(16))
            out.write(shortToLittleEndian(1))
            out.write(shortToLittleEndian(channels.toShort()))
            out.write(intToLittleEndian(sampleRate))
            out.write(intToLittleEndian(byteRate))
            out.write(shortToLittleEndian((channels * bitsPerSample / 8).toShort()))
            out.write(shortToLittleEndian(bitsPerSample.toShort()))

            out.write("data".toByteArray())
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