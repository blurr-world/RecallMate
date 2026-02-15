package com.madinaappstudio.recallmate.core.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.madinaappstudio.recallmate.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun setLog(any: Any?, tag: String = "StudyAssistant-Log") {
    Log.d(tag, "setLog: $any")
}

fun showToast(context: Context, any: Any?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, "${any}", duration).show()
}

fun extractJson(raw: String, isObject: Boolean = true): String {
    val start = if (isObject) raw.indexOf('{') else raw.indexOf('[')
    val end = if (isObject) raw.lastIndexOf('}') else raw.lastIndexOf(']')
    require(start != -1 && end != -1 && end > start) {
        "No valid JSON found"
    }
    return raw.substring(start, end + 1)
}

fun formatDate(millis: Long, isOnlyDate: Boolean = true): String {
    val formatter = if(isOnlyDate)
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    else
        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")

    val dateTime = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        .format(formatter)
    return dateTime
}

fun getFileName(context: Context, uri: Uri): String {
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return uri.path?.substringAfterLast('/') ?: "Unknown File"
}

fun showNoInternet(context: Context, view: View) {
    Snackbar.make(
        view,
        "No internet connection",
        Snackbar.LENGTH_LONG
    ).apply {
        setActionTextColor(context.resources.getColor(R.color.brand_primary, null))
        setAction("Open Settings") {
            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            context.startActivity(intent)
        }
    }.show()
}

fun MaterialButton.setLoading(
    isLoading: Boolean,
    defaultText: String,
    loadingText: String,
    fullView: View?
) {
    if (isLoading) {
        fullView?.alpha = .7f
        isEnabled = false
        text = loadingText
        val indicator = CircularProgressIndicator(context).apply {
            isIndeterminate = true
            trackThickness = 16
            setIndicatorColor(currentTextColor)
            trackColor = currentTextColor and 0x66FFFFFF
        }

        icon = indicator.indeterminateDrawable
    } else {
        fullView?.alpha = 1f
        isEnabled = true
        text = defaultText
        icon = null
    }
}