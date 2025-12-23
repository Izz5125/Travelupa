package com.example.travelupa

// SESUAI MODUL HAL 38: Kita sudah memiliki 3 screen
sealed class Screen(val route: String) {
    object Greeting : Screen("greeting")
    object Login : Screen("login")
    object RekomendasiTempat : Screen("rekomendasi_tempat")
}