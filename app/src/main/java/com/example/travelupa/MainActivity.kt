package com.example.travelupa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.travelupa.screens.LoginScreen
import com.example.travelupa.screens.RegisterScreen
import com.example.travelupa.ui.theme.TravelupaTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            TravelupaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TravelupaApp()
                }
            }
        }
    }
}

@Composable
fun TravelupaApp() {
    // State untuk mengelola layar utama aplikasi (Login vs Halaman Utama)
    var currentAppScreen by remember {
        mutableStateOf(if (FirebaseAuth.getInstance().currentUser != null) AppScreen.MAIN else AppScreen.AUTH)
    }

    when (currentAppScreen) {
        AppScreen.MAIN -> {
            // Navigasi ke halaman utama setelah login berhasil
            RekomendasiTempatFirebaseScreen()
        }
        AppScreen.AUTH -> {
            // Tampilkan navigasi Login/Register jika belum login
            LoginRegisterNavigation(onLoginSuccess = { currentAppScreen = AppScreen.MAIN })
        }
    }
}

@Composable
fun LoginRegisterNavigation(onLoginSuccess: () -> Unit) {
    // State untuk menentukan layar mana yang ditampilkan (Login atau Register)
    var currentAuthScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    when (currentAuthScreen) {
        AuthScreen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = onLoginSuccess, // Panggil callback ini saat login sukses
                onRegisterClicked = {
                    currentAuthScreen = AuthScreen.REGISTER
                }
            )
        }
        AuthScreen.REGISTER -> {
            RegisterScreen(
                onRegisterSuccess = {
                    // Setelah registrasi berhasil, kembali ke login
                    currentAuthScreen = AuthScreen.LOGIN
                },
                onBackToLogin = {
                    currentAuthScreen = AuthScreen.LOGIN
                }
            )
        }
    }
}

// Enum untuk mengelola navigasi level atas
enum class AppScreen {
    AUTH,
    MAIN
}

// Enum untuk mengelola navigasi di dalam autentikasi
enum class AuthScreen {
    LOGIN,
    REGISTER
}
