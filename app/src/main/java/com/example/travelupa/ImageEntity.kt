package com.example.travelupa

import androidx.room.Entity
import androidx.room.PrimaryKey

// SESUAI MODUL HAL 42
@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,  // SESUAI MODUL: val id: Long = 0
    val localPath: String,
    val tempatWisataId: String? = null  // SESUAI MODUL: val tempatWisataId: String? = null
)