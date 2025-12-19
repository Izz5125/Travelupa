package com.example.travelupa

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.travelupa.ui.theme.TravelupaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelupaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RekomendasiTempatScreen()
                }
            }
        }
    }
}

@Composable
fun GreetingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Selamat Datang di Travelupa!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Solusi buat kamu yang lupa kemana-mana",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* TODO: Navigasi ke halaman berikutnya */ },
                modifier = Modifier
                    .width(200.dp)
            ) {
                Text(text = "Mulai")
            }
        }
    }
}

// Data class untuk tempat wisata
data class TempatWisata(
    val nama: String,
    val deskripsi: String,
    val gambarResId: Int
)

// Daftar tempat wisata awal
val daftarTempatWisata = listOf(
    TempatWisata(
        "Tumpak Sewu",
        "Air terjun tercantik di Jawa Timur.",
        R.drawable.tumpak_sewu
    ),
    TempatWisata(
        "Gunung Bromo",
        "Matahari terbitnya bagus banget.",
        R.drawable.gunung_bromo
    ),
    TempatWisata(
        "Kawah Ijen",
        "Blue fire yang menakjubkan.",
        R.drawable.kawah_ijen
    ),
    TempatWisata(
        "Pantai Balekambang",
        "Pantai dengan tiga pulau kecil.",
        R.drawable.pantai_balekambang
    ),
    TempatWisata(
        "Candi Borobudur",
        "Warisan dunia UNESCO.",
        R.drawable.candi_borobudur
    )
)

@Composable
fun RekomendasiTempatScreen() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(daftarTempatWisata) { tempat ->
            TempatItem(tempat = tempat)
        }
    }
}

@Composable
fun TempatItem(tempat: TempatWisata) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Gambar tempat wisata
            Image(
                painter = painterResource(id = tempat.gambarResId),
                contentDescription = tempat.nama,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            // Informasi tempat
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = tempat.nama,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tempat.deskripsi,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}