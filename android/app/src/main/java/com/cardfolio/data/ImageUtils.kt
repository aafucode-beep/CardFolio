package com.cardfolio.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    /**
     * Copy a content Uri to a temporary file for upload.
     */
    fun uriToFile(context: Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "card_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    /**
     * Build the full URL for an image path returned by the API.
     */
    fun imageUrl(imagePath: String?): String? {
        if (imagePath == null) return null
        val base = ApiClient.baseUrl.trimEnd('/')
        // If path is absolute on server (e.g. /app/uploads/xxx.jpg), convert to /uploads/xxx.jpg
        val fileName = imagePath.substringAfterLast('/')
        return "$base/uploads/$fileName"
    }
}
