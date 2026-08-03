package com.red.feature.stories

import android.Manifest
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCaptureScreen(onImageCaptured: (android.net.Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    if (!cameraPermissionState.allPermissionsGranted) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { cameraPermissionState.launchMultiplePermissionRequest() }) {
                Text("Grant Camera Permission")
            }
        }
        return
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val outputDir = remember { context.cacheDir }

    DisposableEffect(Unit) { onDispose { executor.shutdown() } }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                bindCameraUseCases(previewView, lifecycleOwner.lifecycle, lensFacing) { capture ->
                    imageCapture.value = capture
                }
                previewView
            },
            update = { view ->
                bindCameraUseCases(view, lifecycleOwner.lifecycle, lensFacing) { capture ->
                    imageCapture.value = capture
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            }) {
                Icon(Icons.Default.Cached, "Flip", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = {
                val capture = imageCapture.value ?: return@IconButton
                val file = File(outputDir, "story_${System.currentTimeMillis()}.jpg")
                capture.takePicture(
                    ImageCapture.OutputFileOptions.Builder(file).build(),
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onImageCaptured(android.net.Uri.fromFile(file))
                        }
                        override fun onError(exc: ImageCaptureException) { }
                    }
                )
            }) {
                Icon(Icons.Default.CameraAlt, "Capture", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

private fun bindCameraUseCases(
    previewView: PreviewView,
    lifecycle: Lifecycle,
    lensFacing: Int,
    onCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
    cameraProviderFuture.addListener({
        val provider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val capture = ImageCapture.Builder().build()
        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycle,
                CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                preview, capture
            )
            onCaptureReady(capture)
        } catch (_: Exception) { }
    }, ContextCompat.getMainExecutor(previewView.context))
}
