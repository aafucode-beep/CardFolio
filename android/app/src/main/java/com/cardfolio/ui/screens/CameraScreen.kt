package com.cardfolio.ui.screens

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cardfolio.data.ApiClient
import com.cardfolio.data.createImagePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onUploaded: (cardId: String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturingFor by remember { mutableStateOf<String?>(null) } // "front" | "back" | null
    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // ── Camera Preview ──
    Box(modifier = Modifier.fillMaxSize()) {
        key(lensFacing) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ── Overlay UI ──
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top: status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when {
                        frontImageUri != null && backImageUri != null -> "正反面已拍摄 ✓"
                        frontImageUri != null -> "正面已拍摄，可拍背面（可选）"
                        else -> "拍摄名片正面"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x80000000))
                        .padding(8.dp),
                )
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT
                        else
                            CameraSelector.LENS_FACING_BACK
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x80000000)),
                ) {
                    Icon(Icons.Default.Cameraswitch, contentDescription = "切换摄像头", tint = Color.White)
                }
            }

            // Center: capture indicator
            capturingFor?.let { side ->
                Text(
                    text = "正在拍摄${if (side == "front") "正面" else "背面"}...",
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x80000000))
                        .padding(8.dp),
                )
            }

            // Bottom: capture + upload buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Capture buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Front capture
                    CaptureButton(
                        label = "正面",
                        captured = frontImageUri != null,
                        onClick = {
                            capturingFor = "front"
                            takePhoto(context, imageCapture, cameraExecutor) { uri ->
                                frontImageUri = uri
                                capturingFor = null
                            }
                        },
                    )
                    // Back capture
                    CaptureButton(
                        label = "背面",
                        captured = backImageUri != null,
                        onClick = {
                            capturingFor = "back"
                            takePhoto(context, imageCapture, cameraExecutor) { uri ->
                                backImageUri = uri
                                capturingFor = null
                            }
                        },
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Upload button
                Button(
                    onClick = {
                        if (frontImageUri == null && backImageUri == null) return@Button
                        uploading = true
                        error = null
                        scope.launch {
                            try {
                                val frontFile = frontImageUri?.let { uriToFile(context, it) }
                                val backFile = backImageUri?.let { uriToFile(context, it) }
                                val frontPart = frontFile?.let { createImagePart(it, "front") }
                                val backPart = backFile?.let { createImagePart(it, "back") }
                                val resp = withContext(Dispatchers.IO) {
                                    ApiClient.api.uploadCard(frontPart, backPart)
                                }
                                uploading = false
                                onUploaded(resp.card.id)
                            } catch (e: Exception) {
                                uploading = false
                                error = e.message ?: "上传失败"
                            }
                        }
                    },
                    enabled = !uploading && (frontImageUri != null || backImageUri != null),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    if (uploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("OCR 识别中...")
                    } else {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("上传并识别")
                    }
                }

                // Retake button
                if (frontImageUri != null || backImageUri != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            frontImageUri = null
                            backImageUri = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("重新拍摄")
                    }
                }
            }
        }

        // Error dialog
        error?.let { msg ->
            AlertDialog(
                onDismissRequest = { error = null },
                title = { Text("错误") },
                text = { Text(msg) },
                confirmButton = {
                    Button(onClick = { error = null }) { Text("确定") }
                },
            )
        }
    }
}

@Composable
private fun CaptureButton(label: String, captured: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (captured) Color(0xFF10B981) else Color.White),
        ) {
            Icon(
                if (captured) Icons.Default.Check else Icons.Default.PhotoCamera,
                contentDescription = label,
                tint = if (captured) Color.White else Color.Black,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x80000000))
                .padding(horizontal = 4.dp),
        )
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onResult: (Uri) -> Unit,
) {
    val photoFile = File(
        context.cacheDir,
        "card_${System.currentTimeMillis()}.jpg",
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = Uri.fromFile(photoFile)
                ContextCompat.getMainExecutor(context).execute { onResult(uri) }
            }
            override fun onError(exception: ImageCaptureException) {
                ContextCompat.getMainExecutor(context).execute { /* swallow */ }
            }
        },
    )
}

private fun uriToFile(context: Context, uri: Uri): File {
    val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}
