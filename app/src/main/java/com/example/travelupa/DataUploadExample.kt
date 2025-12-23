package com.example.travelupa

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Contoh upload image dengan coroutines
 * SESUAI DENGAN POLA DI MODUL
 */
fun uploadImageToFirestore(
    firestore: FirebaseFirestore,
    context: Context,
    imageUri: Uri,
    tempatWisata: TempatWisata,
    onSuccess: (TempatWisata) -> Unit,
    onFailure: (Exception) -> Unit
) {
    // SESUAI MODUL HAL 43: Gunakan CoroutineScope
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Simpan lokal dulu
            val localPath = saveImageLocally(context, imageUri)

            // Upload ke Firestore
            val data = hashMapOf(
                "nama" to tempatWisata.nama,
                "deskripsi" to tempatWisata.deskripsi,
                "gambarUrl" to localPath, // Dalam implementasi nyata, ini URL Firebase Storage
                "createdAt" to System.currentTimeMillis()
            )

            val result = firestore.collection("tempat_wisata")
                .add(data)
                .await()

            val updatedTempat = tempatWisata.copy(
                // id = result.id // Jika perlu menyimpan ID
            )

            // Panggil callback di main thread
            withContext(Dispatchers.Main) {
                onSuccess(updatedTempat)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onFailure(e)
            }
        }
    }
}

/**
 * Fungsi save image locally - SESUAI MODUL HAL 43-44
 */
fun saveImageLocally(context: Context, uri: Uri): String {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Log seperti di modul
        android.util.Log.d("ImageSave", "Image saved successfully to: ${file.absolutePath}")
        file.absolutePath

    } catch (e: Exception) {
        android.util.Log.e("ImageSave", "Error saving image", e)
        throw e
    }
}