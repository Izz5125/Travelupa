@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.travelupa

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RekomendasiTempatFirebaseScreen(
    firebaseService: FirebaseService = FirebaseService()
) {
    var daftarTempatWisata by remember { mutableStateOf<List<TempatWisata>>(emptyList()) }
    var showTambahDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) } // Mulai dengan loading
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // Fungsi untuk memuat ulang data
    val refreshData = {
        coroutineScope.launch(Dispatchers.IO) {
            val tempatList = firebaseService.getAllTempatWisata()
            withContext(Dispatchers.Main) {
                daftarTempatWisata = tempatList
                isLoading = false
            }
        }
    }

    // Muat data saat pertama kali
    LaunchedEffect(Unit) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi Tempat Wisata") },
                actions = {
                    IconButton(onClick = {
                        firebaseService.logout()
                        // TODO: Navigasi kembali ke layar login
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(daftarTempatWisata) { tempat ->
                        TempatItemFirebase(
                            tempat = tempat,
                            onDelete = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    firebaseService.deleteImage(tempat.gambarUrl ?: "")
                                    firebaseService.deleteTempatWisata(tempat.id)
                                    refreshData()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showTambahDialog) {
        TambahTempatFirebaseDialog(
            selectedImageUri = selectedImageUri,
            onDismiss = {
                showTambahDialog = false
                selectedImageUri = null
            },
            onPickImage = {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onTambah = { nama, deskripsi ->
                coroutineScope.launch(Dispatchers.IO) {
                    val gambarUrl = selectedImageUri?.let { firebaseService.uploadImage(it) }

                    val tempatBaru = TempatWisata(
                        id = "",
                        nama = nama,
                        deskripsi = deskripsi,
                        gambarUrl = gambarUrl,
                        gambarResId = null
                    )

                    firebaseService.addTempatWisata(tempatBaru)
                    refreshData()

                    withContext(Dispatchers.Main) {
                        showTambahDialog = false
                        selectedImageUri = null
                    }
                }
            }
        )
    }
}

@Composable
fun TempatItemFirebase(
    tempat: TempatWisata,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (tempat.gambarUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = tempat.gambarUrl),
                    contentDescription = tempat.nama,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = "No Image",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tempat.nama,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
fun TambahTempatFirebaseDialog(
    selectedImageUri: Uri?,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onTambah: (String, String) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tempat Wisata Baru") },
        text = {
            Column {
                selectedImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = it),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Filled.Photo, contentDescription = "Pick Image")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Gambar")
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && deskripsi.isNotBlank()) {
                        isAdding = true
                        onTambah(nama, deskripsi)
                    }
                },
                enabled = !isAdding && nama.isNotBlank() && deskripsi.isNotBlank()
            ) {
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Tambah")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isAdding) {
                Text("Batal")
            }
        }
    )
}
