package com.example.travelupa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import com.example.travelupa.ui.theme.TravelupaTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MainActivity : ComponentActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageDao: ImageDao  // SESUAI MODUL: deklarasikan ImageDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // SESUAI MODUL HAL 43: Buat database Room
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "travelupa-database"  // SESUAI MODUL: "travelupa-database"
        ).build()

        imageDao = db.imageDao()  // SESUAI MODUL: Dapatkan imageDao dari database

        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        setContent {
            TravelupaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AppNavigation(currentUser, firestore, storage, imageDao)  // Tambahkan imageDao
                }
            }
        }
    }
}