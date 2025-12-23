package com.example.travelupa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM tempat_wisata")
    fun getAllTempatWisata(): Flow<List<TempatWisata>>

    @Insert
    suspend fun insertTempatWisata(tempatWisata: TempatWisata)
}
