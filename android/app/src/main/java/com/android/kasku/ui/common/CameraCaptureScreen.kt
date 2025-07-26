package com.android.kasku.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.util.Rational
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.android.kasku.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}

fun createFile(baseFolder: File, format: String, extension: String) =
    File(baseFolder, SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    outputDirectory: File,
    executor: ExecutorService,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    onPickFromGallery: (Uri?) -> Unit
) {
    val lensFacing = remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Camera permission denied", null))
        }
    }

    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val cameraControl = remember { mutableStateOf<CameraControl?>(null) }
    val zoomState = remember { mutableStateOf<ZoomState?>(null) }
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    val focusPoint = remember { mutableStateOf<Offset?>(null) }
    val isFocused = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Membutuhkan izin kamera untuk mengambil foto.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Berikan Izin")
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    previewViewRef.value = this

                    val scaleGestureDetector = android.view.ScaleGestureDetector(ctx,
                        object : android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
                                zoomState.value?.let { currentZoomState ->
                                    val newZoomRatio = currentZoomState.zoomRatio * detector.scaleFactor
                                    cameraControl.value?.setZoomRatio(newZoomRatio.coerceIn(
                                        currentZoomState.minZoomRatio,
                                        currentZoomState.maxZoomRatio
                                    ))
                                }
                                return true
                            }
                        })
                    setOnTouchListener { _, event ->
                        scaleGestureDetector.onTouchEvent(event)
                        false
                    }
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val selector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing.value)
                        .build()

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner, selector, preview, capture
                        )
                        imageCapture.value = capture
                        cameraControl.value = camera.cameraControl

                        camera.cameraInfo.zoomState.observe(lifecycleOwner) {
                            zoomState.value = it
                        }
                    } catch (e: Exception) {
                        Log.e("CameraX", "Binding failed", e)
                        onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Binding error", e))
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        focusPoint.value = offset
                        isFocused.value = false

                        previewViewRef.value?.let { preview ->
                            cameraControl.value?.let { control ->
                                val factory = preview.meteringPointFactory
                                if (factory != null) {
                                    val point = factory.createPoint(offset.x, offset.y)
                                    val action = FocusMeteringAction.Builder(point)
                                        .disableAutoCancel()
                                        .build()
                                    control.startFocusAndMetering(action)
                                        .addListener({
                                            isFocused.value = true
                                            coroutineScope.launch {
                                                delay(1000)
                                                focusPoint.value = null
                                            }
                                        }, ContextCompat.getMainExecutor(context))
                                } else {
                                    Log.e("CameraGesture", "MeteringPointFactory null")
                                }
                            }
                        }
                    }
                }
        )

        FocusBoxOverlay(focusPoint = focusPoint.value, isFocused = isFocused.value)

        zoomState.value?.let { currentZoom ->
            Slider(
                value = currentZoom.zoomRatio,
                onValueChange = {
                    cameraControl.value?.setZoomRatio(it)
                },
                valueRange = currentZoom.minZoomRatio..currentZoom.maxZoomRatio,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp) // naik dari tombol kamera
                    .width(220.dp)
                    .height(32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
        }

        FloatingActionButton(
            onClick = {
                imageCapture.value?.let { capture ->
                    val file = createFile(outputDirectory, "yyyy-MM-dd-HH-mm-ss-SSS", ".jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                    capture.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Log.e("CameraX", "Capture failed: ${exc.message}", exc)
                                onError(exc)
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val uri = output.savedUri ?: Uri.fromFile(file)
                                Log.d("CameraX", "Saved: $uri")
                                onImageCaptured(uri)
                            }
                        }
                    )
                } ?: onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Capture not ready", null))
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Camera, contentDescription = "Ambil Foto")
        }

        FloatingActionButton(
            onClick = { onPickFromGallery(null) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp)
                .offset(x = 80.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Galeri")
        }
    }
}

@Composable
fun FocusBoxOverlay(
    focusPoint: Offset?,
    isFocused: Boolean
) {
    if (focusPoint != null) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (focusPoint.x - 40).toInt(),
                        (focusPoint.y - 40).toInt()
                    )
                }
                .size(80.dp)
                .border(
                    width = 2.dp,
                    color = if (isFocused) Color.Blue else Color.White,
                    shape = RectangleShape
                )
        )
    }
}
