package com.example.pasteleriamilsabores.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.PCamara.CameraManager
import com.example.pasteleriamilsabores.PCamara.CamaraUtils

class CameraActivity : AppCompatActivity() {

    private var cameraManager: CameraManager? = null
    private lateinit var previewView: PreviewView
    private lateinit var contenedorFoto: ImageView
    private lateinit var btnTomarFoto: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnCargarGaleria: Button

    private val CAMERA_PERMISSION_REQUEST_CODE = 101 // Código para la solicitud de permiso

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handleGalleryImageUri(uri) // Procesa la imagen seleccionada
        } else {
            Toast.makeText(this, "Selección de galería cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        btnTomarFoto = findViewById(R.id.btn_tomar_foto)
        btnCancelar = findViewById(R.id.btn_cancelar_foto)
        btnCargarGaleria = findViewById(R.id.btn_cargar_galeria) // Inicializa botón de galería
        contenedorFoto = findViewById(R.id.imgv_foto)
        previewView = findViewById(R.id.previewView)

        btnTomarFoto.isEnabled = false

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            setupCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }

        btnTomarFoto.setOnClickListener {
            tomarFotoYDevolverResultado()
        }

        btnCancelar.setOnClickListener {
            setResult(Activity.RESULT_CANCELED) // Enviar código de cancelación
            finish()
        }

        btnCargarGaleria.setOnClickListener {
            // Lanza el selector de imágenes de la galería
            galleryLauncher.launch("image/*")
        }
    }


// LOGICA DE CAMARA Y BASE64

    //Toma la foto con CameraX, codifica y devuelve el resultado a MainActivity3.

    private fun tomarFotoYDevolverResultado() {
        cameraManager?.takePhoto { bitmap ->
            if (bitmap != null) {
                // Muestra la foto capturada al usuario
                previewView.visibility = View.GONE
                contenedorFoto.visibility = View.VISIBLE
                contenedorFoto.setImageBitmap(bitmap)

                // Codificar a Base64
                val base64 = CamaraUtils.convertirDeBitMapABase64(bitmap)

                // Devolver resultado a MainActivity3
                val resultIntent = Intent()
                resultIntent.putExtra("IMAGE_BASE64", base64)

                setResult(Activity.RESULT_OK, resultIntent)
                finish()

            } else {
                Toast.makeText(this, "Error al capturar", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        } ?: Toast.makeText(this, "Cámara no lista", Toast.LENGTH_SHORT).show()
    }

    // PROCESA LA IMAGEN QUE ESCOGEMOS DE LA GALERIA Y LA CONVIERTE A BASE64
    private fun handleGalleryImageUri(uri: Uri) {
        try {
            // Lee la URI como un Bitmap (maneja compatibilidad de versiones)
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            // Muestra y convierte a Base64
            previewView.visibility = View.GONE
            contenedorFoto.visibility = View.VISIBLE
            contenedorFoto.setImageBitmap(bitmap)

            val base64 = CamaraUtils.convertirDeBitMapABase64(bitmap)

            // Devolver resultado a MainActivity3
            val resultIntent = Intent()
            resultIntent.putExtra("IMAGE_BASE64", base64)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        } catch (e: Exception) {
            Log.e("GALLERY_LOAD", "Error al procesar imagen de galería: ${e.message}", e)
            Toast.makeText(this, "Error al cargar la imagen de galería.", Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }



    //Inicializa el CameraManager y la vista previa.

    private fun setupCamera() {
        cameraManager = CameraManager(this)
        cameraManager?.startCamera(previewView)
        btnTomarFoto.isEnabled = true
    }


    // MANEJO DE PERMISOS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            setupCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}