package com.example.pasteleriamilsabores.PCamara

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import android.app.Activity

class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null

    // Metodo que inicializa la camara y carga el elemento PreviewView
    fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val windowManager = (context as Activity).windowManager
            val rotation = windowManager.defaultDisplay.rotation

            val preview = Preview.Builder()
                .setTargetRotation(rotation)
                .build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(rotation)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Error al iniciar cámara", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Funcion tomar foto genera y retorna un bitmap
    fun takePhoto(onResult: (Bitmap?) -> Unit) {
        val capture = imageCapture ?: return

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)

                    // Aplicar la rotación del sensor
                    val rotationDegrees = image.imageInfo.rotationDegrees
                    val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees)

                    image.close()
                    onResult(rotatedBitmap) // Devolver el Bitmap ya corregido
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Error al capturar foto", exception)
                    onResult(null)
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * Gira el Bitmap a la orientación correcta usando Matrix.
     */
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap // No necesita rotación

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat()) // Aplica la rotación
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true // Filtro para mejor calidad
        )
    }
}