package com.example.travelupa

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tempat_wisata")
data class TempatWisata(
    @PrimaryKey val id: String,
    val nama: String,
    val deskripsi: String,
    val gambarUrl: String?,
    val gambarResId: Int?
)
