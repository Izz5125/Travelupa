package com.example.travelupa

// Update data class untuk support gambar dari URL dan Resource ID
data class TempatWisata(
    val id: String = "",  // Untuk Firestore document ID
    val nama: String,
    val deskripsi: String,
    val gambarUrl: String? = null,      // Untuk Firebase Storage URL
    val gambarResId: Int? = null,       // Untuk local resource ID
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)