package com.example.travelupa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

// SESUAI MODUL HAL 42-43
@Dao
interface ImageDao {
    // SESUAI MODUL HAL 42: @Insert fun insert(image: ImageEntity): Long
    @Insert
    fun insert(image: ImageEntity): Long

    // SESUAI MODUL HAL 42: @Query("SELECT * FROM images WHERE id = :imageId")
    @Query("SELECT * FROM images WHERE id = :imageId")
    fun getImageById(imageId: Long): ImageEntity?

    // SESUAI MODUL HAL 42: @Query("SELECT * FROM images WHERE tempatWisataId = :firestoreId")
    @Query("SELECT * FROM images WHERE tempatWisataId = :firestoreId")
    fun getImageByTempatWisataId(firestoreId: String): ImageEntity?

    // SESUAI MODUL HAL 42: @Query("SELECT * FROM images") fun getAllImages(): Flow<List<ImageEntity>>
    @Query("SELECT * FROM images")
    fun getAllImages(): Flow<List<ImageEntity>>

    // SESUAI MODUL HAL 42: @Delete fun delete(image: ImageEntity)
    @Delete
    fun delete(image: ImageEntity)
}