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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelupaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RekomendasiTempatScreenWithState()
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

@Composable
fun RekomendasiTempatScreenWithState() {
    // State untuk daftar tempat wisata
    var daftarTempatWisata by remember {
        mutableStateOf(
            listOf(
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
        )
    }

    // State untuk dialog tambah data
    var showTambahDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTambahDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Tambah Tempat Wisata"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(daftarTempatWisata) { tempat ->
                    TempatItemEditable(
                        tempat = tempat,
                        onDelete = {
                            // Hapus item dari daftar
                            daftarTempatWisata = daftarTempatWisata.filter { it != tempat }
                        }
                    )
                }
            }
        }
    }

    // Dialog untuk menambah data
    if (showTambahDialog) {
        TambahTempatDialog(
            onDismiss = { showTambahDialog = false },
            onTambah = { nama, deskripsi ->
                // Tambah data baru
                val tempatBaru = TempatWisata(
                    nama = nama,
                    deskripsi = deskripsi,
                    gambarResId = R.drawable.default_image // Gambar default
                )
                daftarTempatWisata = daftarTempatWisata + tempatBaru
                showTambahDialog = false
            }
        )
    }
}

@Composable
fun TempatItemEditable(
    tempat: TempatWisata,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TambahTempatDialog(
    onDismiss: () -> Unit,
    onTambah: (String, String) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tempat Wisata Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Tempat") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && deskripsi.isNotBlank()) {
                        onTambah(nama, deskripsi)
                    }
                }
            ) {
                Text("Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}