package com.example.travelupa

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

// SESUAI MODUL HAL 46-51
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    imageDao: ImageDao,
    onImageSelected: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val images by imageDao.getAllImages().collectAsState(initial = emptyList())
    var showAddImageDialog by remember { mutableStateOf(false) }
    var selectedImageEntity by remember { mutableStateOf<ImageEntity?>(null) }
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf<ImageEntity?>(null) }

    // SESUAI MODUL HAL 46: LaunchedEffect untuk log
    LaunchedEffect(images) {
        android.util.Log.d("GalleryScreen", "Total images: ${images.size}")
        images.forEachIndexed { index, image ->
            android.util.Log.d("GalleryScreen", "Image $index path: ${image.localPath}")
            val file = File(image.localPath)
            android.util.Log.d("GalleryScreen", "File exists: ${file.exists()}, is readable: ${file.canRead()}")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddImageDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Image")
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(images) { image ->
                Image(
                    painter = rememberAsyncImagePainter(model = image.localPath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                        .clickable {
                            selectedImageEntity = image
                            onImageSelected(Uri.parse(image.localPath))
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    // SESUAI MODUL HAL 47: AddImageDialog
    if (showAddImageDialog) {
        AddImageDialog(
            onDismiss = { showAddImageDialog = false },
            onImageAdded = { uri ->
                showAddImageDialog = false
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                            FileOutputStream(file).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                            val newImage = ImageEntity(localPath = file.absolutePath)
                            imageDao.insert(newImage)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ImageSave", "Failed to save image", e)
                    }
                }
            }
        )
    }

    // SESUAI MODUL HAL 47: ImageDetailDialog
    selectedImageEntity?.let { imageEntity ->
        ImageDetailDialog(
            imageEntity = imageEntity,
            onDismiss = { selectedImageEntity = null },
            onDelete = { imageToDelete ->
                showDeleteConfirmation = imageToDelete
            }
        )
    }

    // SESUAI MODUL HAL 48: Delete Confirmation Dialog
    showDeleteConfirmation?.let { imageToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            imageDao.delete(imageToDelete)
                            val file = File(imageToDelete.localPath)
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// SESUAI MODUL HAL 48-50: AddImageDialog
@Composable
fun AddImageDialog(
    onDismiss: () -> Unit,
    onImageAdded: (Uri) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // SESUAI MODUL HAL 48: Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // SESUAI MODUL HAL 48-49: Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val uri = saveBitmapToUri(context, it)
            imageUri = uri
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Image") },
        text = {
            Column {
                // SESUAI MODUL HAL 49: Preview image
                imageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SESUAI MODUL HAL 49: Buttons untuk pilih gambar atau ambil foto
                Row {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select from File")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Take Photo")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    imageUri?.let { uri ->
                        onImageAdded(uri)
                    }
                },
                enabled = imageUri != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// SESUAI MODUL HAL 50: ImageDetailDialog
@Composable
fun ImageDetailDialog(
    imageEntity: ImageEntity,
    onDismiss: () -> Unit,
    onDelete: (ImageEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Image(
                painter = rememberAsyncImagePainter(model = imageEntity.localPath),
                contentDescription = "Detailed Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        },
        confirmButton = {
            Row {
                Button(onClick = { onDelete(imageEntity) }) {
                    Text("Delete")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

// SESUAI MODUL HAL 51: saveBitmapToUri
fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    return Uri.fromFile(file)
}
