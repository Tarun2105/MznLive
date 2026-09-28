package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ReceiptStorageHelper {
    /**
     * Copies the content of a user-picked image URI to the app's internal files directory.
     * Returns the file URI or string path.
     */
    fun persistScreenshot(context: Context, sourceUri: Uri): String? {
        return copyUriToInternalStorage(context, sourceUri, "screenshot_${System.currentTimeMillis()}.jpg")
    }

    /**
     * Copies the content of a user-picked image URI to the app's internal files directory.
     * Returns the file URI or string path.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri, preferredFileName: String): String? {
        return try {
            val dir = File(context.filesDir, "user_media").apply {
                if (!exists()) mkdirs()
            }
            val destFile = File(dir, preferredFileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }
}
