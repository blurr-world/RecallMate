package com.madinaappstudio.recallmate.core.api

import android.content.Context
import android.net.Uri
import com.madinaappstudio.recallmate.BuildConfig
import com.madinaappstudio.recallmate.core.utils.showToast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object GeminiFileUploader {

    private val client = OkHttpClient()

    fun upload(
        context: Context,
        uri: Uri,
        callback: GeminiUploadCallback
    ) {
        val fileBytes = uriToByteArray(context, uri)
            ?: run {
                callback.onError("File is too large")
                return
            }

        val mimeType = getMimeType(context, uri)

        val request = buildUploadRequest(fileBytes, mimeType)

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e.localizedMessage ?: "Upload failed")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body.string()

                handleUploadResponse(body, mimeType, callback)
            }
        })
    }

    private fun buildUploadRequest(
        fileBytes: ByteArray,
        mimeType: String
    ): Request {

        val metadataJson = JSONObject().apply {
            put("file", JSONObject().apply {
                put("displayName", "document")
            })
        }.toString()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "metadata",
                null,
                metadataJson.toRequestBody("application/json".toMediaType())
            )
            .addFormDataPart(
                "file",
                "document",
                fileBytes.toRequestBody(mimeType.toMediaType())
            )
            .build()

        return Request.Builder()
            .url(
                "https://generativelanguage.googleapis.com/upload/v1beta/files" +
                        "?uploadType=multipart&key=${BuildConfig.GEMINI_API_KEY}"
            )
            .post(requestBody)
            .build()
    }

    private fun handleUploadResponse(
        body: String,
        mimeType: String,
        callback: GeminiUploadCallback
    ) {
        val json = JSONObject(body)
        val file = json.getJSONObject("file")
        val state = file.getString("state")

        if (state == "ACTIVE") {
            callback.onSuccess(
                fileUri = file.getString("uri"),
                mimeType = mimeType
            )
        } else {
            callback.onError("File not active yet: $state")
        }
    }

    private fun uriToByteArray(
        context: Context,
        uri: Uri,
    ): ByteArray? {
        val maxSizeBytes = 10 * 1024 * 1024
        context.contentResolver.openInputStream(uri)?.use { input ->
            val bytes = input.readBytes()
            return if (bytes.size <= maxSizeBytes)
                bytes
            else
                null
        }
        throw IllegalArgumentException("Unable to read URI")
    }

    private fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri)
            ?: "application/octet-stream"
    }
}
