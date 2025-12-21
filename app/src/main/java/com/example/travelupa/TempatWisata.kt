package com.example.travelupa

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class TempatWisata(
    val id: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val gambarUrl: String? = null,
    val gambarResId: Int? = null
)
