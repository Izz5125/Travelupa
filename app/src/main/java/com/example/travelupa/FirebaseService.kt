package com.example.travelupa

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Sealed class untuk hasil registrasi yang lebih detail
sealed class RegistrationResult {
    object Success : RegistrationResult()
    data class Failure(val reason: RegistrationFailureReason) : RegistrationResult()
}

enum class RegistrationFailureReason {
    EMAIL_ALREADY_EXISTS,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    UNKNOWN
}

@Singleton
class FirebaseService @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // ============ AUTHENTICATION =============
    suspend fun loginWithEmail(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registerWithEmail(email: String, password: String): RegistrationResult {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            RegistrationResult.Success
        } catch (e: FirebaseAuthException) {
            Log.e("FirebaseService", "Registrasi gagal: ${e.errorCode} - ${e.message}")
            val reason = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> RegistrationFailureReason.EMAIL_ALREADY_EXISTS
                "ERROR_WEAK_PASSWORD" -> RegistrationFailureReason.WEAK_PASSWORD
                "ERROR_INVALID_EMAIL" -> RegistrationFailureReason.INVALID_EMAIL
                else -> RegistrationFailureReason.UNKNOWN
            }
            RegistrationResult.Failure(reason)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Galat registrasi tidak diketahui", e)
            RegistrationResult.Failure(RegistrationFailureReason.UNKNOWN)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser

    // ============ FIRESTORE - TEMPAT WISATA ============
    suspend fun getAllTempatWisata(): List<TempatWisata> {
        return try {
            val snapshot = firestore.collection("tempat_wisata").get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject<TempatWisata>()?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addTempatWisata(tempat: TempatWisata): String? {
        return try {
            val documentReference = firestore.collection("tempat_wisata").add(tempat).await()
            documentReference.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateTempatWisata(id: String, tempat: TempatWisata): Boolean {
        return try {
            firestore.collection("tempat_wisata").document(id).set(tempat).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTempatWisata(id: String): Boolean {
        return try {
            firestore.collection("tempat_wisata").document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ============ STORAGE - UPLOAD GAMBAR ============
    suspend fun uploadImage(imageUri: Uri): String? {
        return try {
            val timestamp = System.currentTimeMillis()
            val fileName = "image_$timestamp.jpg"
            val storageRef = storage.reference.child("images/$fileName")

            storageRef.putFile(imageUri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteImage(imageUrl: String): Boolean {
        if (imageUrl.isBlank()) return false
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
