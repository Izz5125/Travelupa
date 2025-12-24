package com.example.travelupa

import androidx.room.Database
import androidx.room.RoomDatabase

// SESUAI MODUL HAL 42-43
@Database(entities = [ImageEntity::class], version = 1)  // SESUAI MODUL: version = 1
abstract class AppDatabase : RoomDatabase() {
    // SESUAI MODUL HAL 43: abstract fun imageDao(): ImageDao
    abstract fun imageDao(): ImageDao
}