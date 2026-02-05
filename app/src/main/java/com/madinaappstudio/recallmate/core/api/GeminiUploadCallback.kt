package com.madinaappstudio.recallmate.core.api

interface GeminiUploadCallback {
    fun onSuccess(fileUri: String, mimeType: String)
    fun onError(message: String)
}