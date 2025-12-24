package com.example.travelupa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val permissions = listOf(
        Manifest.permission.CAMERA,
    )
    var hasPermissions by remember {
        mutableStateOf(permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            hasPermissions = permissionsMap.values.all { it }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kamera") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            takePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                cameraExecutor = cameraExecutor,
                                onImageCaptured = onImageCaptured
                            )
                        },
                        modifier = Modifier.size(64.dp),
                        enabled = imageCapture != null
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Ambil Foto",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasPermissions) {
                CameraPreview(
                    onCameraInitialized = { imageCapture = it },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Izin kamera diperlukan untuk fitur ini.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(permissions.toTypedArray()) }) {
                        Text("Minta Izin")
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun CameraPreview(
    onCameraInitialized: (ImageCapture) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                onCameraInitialized(imageCapture)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Gagal melakukan bind use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView({ previewView }, modifier = modifier)
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    cameraExecutor: ExecutorService,
    onImageCaptured: (Uri) -> Unit
) {
    val photoFile = createImageFile(context)
    val photoUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        photoFile
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture?.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Beralih ke main thread untuk navigasi
                ContextCompat.getMainExecutor(context).execute {
                    onImageCaptured(photoUri)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Gagal mengambil foto: ", exception)
            }
        }
    )
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}
