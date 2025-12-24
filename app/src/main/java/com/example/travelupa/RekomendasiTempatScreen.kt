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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiTempatScreen(
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    imageDao: ImageDao,  // Tambahkan ImageDao parameter
    onBackToLogin: () -> Unit,
    onGallerySelected: () -> Unit
) {
    var daftarTempatWisata by remember { mutableStateOf<List<TempatWisata>>(emptyList()) }
    var showTambahDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Load data dari Firestore
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = firestore.collection("tempat_wisata").get().await()
                val tempatList = result.documents.map { document ->
                    TempatWisata(
                        id = document.id,
                        nama = document.getString("nama") ?: "",
                        deskripsi = document.getString("deskripsi") ?: "",
                        gambarUrl = document.getString("gambarUrl")
                    )
                }
                daftarTempatWisata = tempatList
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi Tempat Wisata") },
                actions = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
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
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(daftarTempatWisata) { tempat ->
                TempatItemDatabase(
                    tempat = tempat,
                    onDelete = {
                        CoroutineScope(Dispatchers.IO).launch {
                            // Hapus dari Firestore
                            firestore.collection("tempat_wisata")
                                .document(tempat.id)
                                .delete()
                                .await()

                            // Hapus dari local database jika ada gambar
                            tempat.gambarUrl?.let { imageUrl ->
                                val imageEntity = imageDao.getImageByTempatWisataId(tempat.id)
                                imageEntity?.let { imageDao.delete(it) }
                            }

                            // Refresh list
                            val result = firestore.collection("tempat_wisata").get().await()
                            val tempatList = result.documents.map { document ->
                                TempatWisata(
                                    id = document.id,
                                    nama = document.getString("nama") ?: "",
                                    deskripsi = document.getString("deskripsi") ?: "",
                                    gambarUrl = document.getString("gambarUrl")
                                )
                            }
                            daftarTempatWisata = tempatList
                        }
                    }
                )
            }
        }
    }

    // Dialog untuk menambah data dengan Room Database
    if (showTambahDialog) {
        TambahTempatDatabaseDialog(
            selectedImageUri = selectedImageUri,
            onDismiss = {
                showTambahDialog = false
                selectedImageUri = null
            },
            onPickImage = {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onTambah = { nama, deskripsi ->
                val tempatBaru = TempatWisata(
                    nama = nama,
                    deskripsi = deskripsi
                )

                // Jika ada gambar yang dipilih, upload ke Firestore dan simpan lokal
                selectedImageUri?.let { uri ->
                    DatabaseUtils.uploadImageToFirestore(
                        firestore = firestore,
                        context = context,
                        imageUri = uri,
                        tempatWisata = tempatBaru,
                        onSuccess = { updatedTempat ->
                            // Refresh list
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = firestore.collection("tempat_wisata").get().await()
                                val tempatList = result.documents.map { document ->
                                    TempatWisata(
                                        id = document.id,
                                        nama = document.getString("nama") ?: "",
                                        deskripsi = document.getString("deskripsi") ?: "",
                                        gambarUrl = document.getString("gambarUrl")
                                    )
                                }
                                daftarTempatWisata = tempatList
                            }
                            showTambahDialog = false
                            selectedImageUri = null
                        },
                        onFailure = { e ->
                            // Handle error
                            showTambahDialog = false
                            selectedImageUri = null
                        }
                    )
                } ?: run {
                    // Jika tidak ada gambar, simpan langsung ke Firestore
                    CoroutineScope(Dispatchers.IO).launch {
                        firestore.collection("tempat_wisata")
                            .add(tempatBaru)
                            .await()

                        // Refresh list
                        val result = firestore.collection("tempat_wisata").get().await()
                        val tempatList = result.documents.map { document ->
                            TempatWisata(
                                id = document.id,
                                nama = document.getString("nama") ?: "",
                                deskripsi = document.getString("deskripsi") ?: "",
                                gambarUrl = document.getString("gambarUrl")
                            )
                        }
                        daftarTempatWisata = tempatList

                        showTambahDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun TempatItemDatabase(
    tempat: TempatWisata,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Tampilkan gambar dari URL jika ada, atau gambar default
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
                Image(
                    painter = painterResource(id = R.drawable.default_image),
                    contentDescription = tempat.nama,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
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
fun TambahTempatDatabaseDialog(
    selectedImageUri: Uri?,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onTambah: (String, String) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tempat Wisata Baru") },
        text = {
            Column {
                // Preview gambar yang dipilih
                selectedImageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Tombol pilih gambar
                Button(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Filled.Image, contentDescription = "Pick Image", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Gambar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form input
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
                        onTambah(nama, deskripsi)
                    }
                },
                enabled = nama.isNotBlank() && deskripsi.isNotBlank()
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