package com.example.travelupa

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun saveImageToDatabase(context: Context, imageDao: ImageDao, uri: Uri) {
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val file = File(context.filesDir, "IMG_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            val newImage = ImageEntity(localPath = file.absolutePath)
            imageDao.insert(newImage)
        }
    }
}
