package com.example.travelupa

// Update Screen definitions untuk menambahkan Camera dan Gallery
sealed class Screen(val route: String) {
    object Greeting : Screen("greeting")
    object Login : Screen("login")
    object RekomendasiTempat : Screen("rekomendasi_tempat")
    object Camera : Screen("camera")  // Tambahkan Camera screen
    object Gallery : Screen("gallery") // Tambahkan Gallery screen
}