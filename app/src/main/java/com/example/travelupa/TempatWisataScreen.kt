package com.example.travelupa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun RekomendasiTempatScreen(
    firestore: FirebaseFirestore,
    onBackToLogin: () -> Unit,
    onGallerySelected: () -> Unit
) {
    var daftarTempatWisata by remember { mutableStateOf<List<TempatWisata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val tempatList = withContext(Dispatchers.IO) {
                firestore.collection("tempat_wisata")
                    .get()
                    .await()
                    .documents.map { document ->
                        TempatWisata(
                            id = document.id,
                            nama = document.getString("nama") ?: "",
                            deskripsi = document.getString("deskripsi") ?: "",
                            gambarUrl = document.getString("gambarUrl"),
                            gambarResId = null // ganti dengan null karena kita pakai gambarUrl
                        )
                    }
            }
            daftarTempatWisata = tempatList
        } catch (e: Exception) {
            errorMessage = "Gagal memuat data: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Memuat data...")
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(daftarTempatWisata) { tempat ->
                TempatWisataItem(tempat = tempat)
            }
        }
    }

    errorMessage?.let {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun TempatWisataItem(tempat: TempatWisata) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (tempat.gambarUrl != null) {
                AsyncImage(
                    model = tempat.gambarUrl,
                    contentDescription = "Gambar ${tempat.nama}",
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(text = tempat.nama, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = tempat.deskripsi, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
