package com.example.travelupa

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

// SESUAI MODUL HAL 43-44
object DatabaseUtils {

    /**
     * SESUAI MODUL HAL 43: Fungsi untuk upload image ke Firestore dan simpan lokal
     * Nama fungsi: uploadImageToFirestore - sama dengan modul
     */
    fun uploadImageToFirestore(
        firestore: FirebaseFirestore,
        context: Context,
        imageUri: Uri,  // SESUAI MODUL: parameter imageUri
        tempatWisata: TempatWisata,
        onSuccess: (TempatWisata) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // SESUAI MODUL HAL 43: Buat database Room
        val db = androidx.room.Room.databaseBuilder(
            context,
            AppDatabase::class.java, "travelupa-database"
        ).build()

        val imageDao = db.imageDao()
        val localPath = saveImageLocally(context, imageUri)

        // SESUAI MODUL HAL 43: CoroutineScope(Dispatchers.IO).launch
        CoroutineScope(Dispatchers.IO).launch {
            // SESUAI MODUL HAL 43: val imageId = imageDao.insert(ImageEntity(localPath = localPath))
            val imageId = imageDao.insert(ImageEntity(localPath = localPath))

            // SESUAI MODUL HAL 43: val updatedTempatWisata = tempatWisata.copy(gambarUriString = localPath)
            val updatedTempatWisata = tempatWisata.copy(gambarUrl = localPath)

            // SESUAI MODUL HAL 43: Simpan ke Firestore
            firestore.collection("tempat_wisata")
                .add(updatedTempatWisata)
                .addOnSuccessListener {
                    onSuccess(updatedTempatWisata)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        }
    }

    /**
     * SESUAI MODUL HAL 43-44: Fungsi untuk menyimpan gambar secara lokal
     * Nama fungsi: saveImageLocally - sama dengan modul
     */
    fun saveImageLocally(context: Context, uri: Uri): String {
        try {
            // SESUAI MODUL HAL 43: val inputStream = context.contentResolver.openInputStream(uri)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            // SESUAI MODUL HAL 43: val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")
            val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")

            // SESUAI MODUL HAL 43-44: inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // SESUAI MODUL HAL 44: Log.d("ImageSave", "Image saved successfully to: ${file.absolutePath}")
            android.util.Log.d("ImageSave", "Image saved successfully to: ${file.absolutePath}")
            return file.absolutePath

        } catch (e: Exception) {
            // SESUAI MODUL HAL 44: Log.e("ImageSave", "Error saving image", e)
            android.util.Log.e("ImageSave", "Error saving image", e)
            throw e
        }
    }
}