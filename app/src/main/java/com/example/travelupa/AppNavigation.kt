package com.example.travelupa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun AppNavigation(
    currentUser: FirebaseUser?,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    imageDao: ImageDao
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null)
            Screen.RekomendasiTempat.route
        else
            Screen.Greeting.route
    ) {
        composable(Screen.Greeting.route) {
            GreetingScreen(
                onStart = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.RekomendasiTempat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {}
            )
        }

        composable(Screen.RekomendasiTempat.route) {
            RekomendasiTempatScreen(
                firestore = firestore,
                storage = storage,
                imageDao = imageDao,
                onBackToLogin = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Greeting.route) {
                        popUpTo(Screen.RekomendasiTempat.route) { inclusive = true }
                    }
                },
                onGallerySelected = {
                    navController.navigate(Screen.Gallery.route)
                },
                onCameraSelected = {
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { uri ->
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val file = File(context.filesDir, "IMG_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(file).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                                val newImage = ImageEntity(localPath = file.absolutePath)
                                imageDao.insert(newImage)
                            }
                        }
                        navController.navigate(Screen.Gallery.route) {
                            popUpTo(Screen.Camera.route) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen(
                imageDao = imageDao,
                onImageSelected = { uri ->
                    // Handle when an image is selected from the gallery
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}