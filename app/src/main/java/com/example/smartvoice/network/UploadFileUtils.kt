package com.example.smartvoice.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun fileToMultipart(file: File): MultipartBody.Part {
    val body = file.asRequestBody("audio/wav".toMediaType())
    return MultipartBody.Part.createFormData("file", file.name, body)
}
