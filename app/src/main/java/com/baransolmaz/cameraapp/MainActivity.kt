package com.baransolmaz.cameraapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.baransolmaz.cameraapp.camera.CameraView
import com.baransolmaz.cameraapp.fcm.MyNotification
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var photoUri: Uri

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)
    private var shouldUploadPhoto: MutableState<Boolean> = mutableStateOf(false)

    private var openCameraButton: MutableState<Boolean> = mutableStateOf(true)
    private var showPhotoButton: MutableState<Boolean> = mutableStateOf(false)
    private var uploadButton: MutableState<Boolean> = mutableStateOf(false)

    private var storageRef : StorageReference = Firebase.storage.reference

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("permission", "Permission granted")

        } else {
            Log.i("permission", "Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                val updatedToken: String = instanceIdResult
                Log.e("FCMToken", updatedToken)
            }
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.LightGray
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Button(
                        onClick = {
                            shouldShowCamera.value = true
                            showPhotoButton.value=true
                            openCameraButton.value=false
                        },
                        enabled = openCameraButton.value
                    ){
                        Text(text = "Take Photo")
                    }
                    Button(
                        onClick = {
                            shouldShowPhoto.value = photoUri.toString().isNotEmpty()
                            showPhotoButton.value=false
                            uploadButton.value=true
                        },
                        enabled = showPhotoButton.value
                    ) {
                        Text(text = "Show Photo")
                    }
                    Button(
                        onClick = {
                            shouldUploadPhoto.value = true
                            uploadButton.value=false
                            openCameraButton.value=true
                        },
                        enabled = uploadButton.value
                    ) {
                        Text(text = "Upload Photo")
                    }
                }
                OpenCamera()
                ShowImage()
                UploadImage()
            }
        }

        requestCameraPermission()

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @Composable
    fun OpenCamera(){
        if (shouldShowCamera.value) {
            CameraView(
                outputDirectory = outputDirectory,
                executor = cameraExecutor,
                onImageCaptured = ::handleImageCapture,
                onError = { Log.e("CardView", "View error:", it) }
            )
        }
    }
    @Composable
    fun ShowImage(){
        if (shouldShowPhoto.value) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.LightGray
            ){
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { shouldShowPhoto.value=false },
                    ) {
                        Text(text = "Back")
                    }
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

            }
        }

    }
    @Composable
    fun UploadImage(){
        if (shouldUploadPhoto.value){
            storageRef.child("${photoUri.lastPathSegment}").putFile(photoUri)
                .addOnSuccessListener { // Image uploaded successfully
                    Log.i("UPLOAD","SUCCESS")
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Image Uploaded!!",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                    val notice=
                        MyNotification(applicationContext,
                            "Upload Image",
                            "Success")
                    notice.fireNotification()
                }
                .addOnFailureListener { e -> // Error, Image not uploaded
                    Log.i("UPLOAD","FAILURE")
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Failed " + e.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
        }
    }
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("permission", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("permission", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun handleImageCapture(uri: Uri) {
        Log.i("image_capture", "Image captured: $uri")
        shouldShowCamera.value = false

        photoUri = uri
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}