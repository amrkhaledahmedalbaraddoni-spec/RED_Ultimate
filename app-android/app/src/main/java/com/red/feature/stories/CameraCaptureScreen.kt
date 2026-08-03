package com.red.feature.stories

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraCaptureScreen(onImageCaptured: (Uri) -> Unit) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
  var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
  var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
  val executor = remember { Executors.newSingleThreadExecutor() }

  // Re-bind the camera whenever the lens selection changes.
  LaunchedEffect(lensFacing) {
    imageCapture?.let { capture ->
      val cameraProvider = cameraProviderFuture.get()
      val preview = Preview.Builder().build()
      val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
      runCatching {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
      }
    }
  }

  // Always release the executor to avoid thread leaks.
  DisposableEffect(Unit) {
    onDispose { executor.shutdown() }
  }

  Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
    AndroidView(
      factory = { ctx ->
        val previewView = PreviewView(ctx)
        cameraProviderFuture.addListener({
          val cameraProvider = cameraProviderFuture.get()
          val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
          val capture = ImageCapture.Builder().build()
          imageCapture = capture
          val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
          runCatching {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
          }
        }, ContextCompat.getMainExecutor(ctx))
        previewView
      },
      modifier = Modifier.fillMaxSize()
    )

    Row(
      modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 48.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
          CameraSelector.LENS_FACING_FRONT
        } else {
          CameraSelector.LENS_FACING_BACK
        }
      }) {
        Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
      }

      Button(
        onClick = {
          val capture = imageCapture ?: return@Button
          val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
          val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
          capture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
              onImageCaptured(Uri.fromFile(file))
            }

            override fun onError(exception: ImageCaptureException) {
              exception.printStackTrace()
            }
          })
        },
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
      ) {
        Icon(Icons.Default.Camera, contentDescription = null, tint = Color.Black, modifier = Modifier.size(48.dp))
      }
    }
  }
}
