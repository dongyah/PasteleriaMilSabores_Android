package com.example.pasteleriamilsabores.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.model.Producto
import com.example.pasteleriamilsabores.repository.ProductosApiRepository
import kotlinx.coroutines.launch
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix
import java.io.ByteArrayInputStream

class MainActivity3 : AppCompatActivity() {

    // Declaraciones de Vistas
    private lateinit var etProductCode: EditText
    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductStock: EditText
    private lateinit var etCriticalStock: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSaveProduct: Button
    private lateinit var ivProductPreview: ImageView
    private lateinit var flImagePicker: FrameLayout
    private lateinit var tvImagePlaceholder: TextView

    private var listaCategorias: List<Categoria> = emptyList()
    private var base64Image: String? = null // Cadena Base64 para enviar a la API

    // Variables de modo y edición
    private var productoIdParaEdicion: Int = 0
    private var productoCargado: Producto? = null
    private val CAMERA_REQUEST_CODE = 102


    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val base64 = result.data?.getStringExtra("IMAGE_BASE64")
            if (base64 != null) {
                base64Image = base64 // Asigna la cadena Base64

                val bitmap = decodeBase64ToBitmap(base64)
                if (bitmap != null) {
                    ivProductPreview.setImageBitmap(bitmap)
                    ivProductPreview.visibility = View.VISIBLE

                    // Oculta el placeholder y el campo de URL/texto
                    etImageUrl.visibility = View.GONE
                    tvImagePlaceholder.visibility = View.GONE
                }

                Toast.makeText(this, "Foto cargada y codificada.", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Error: No se recibió la imagen.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Captura cancelada.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        etProductCode = findViewById(R.id.etProductCode)
        etProductName = findViewById(R.id.etProductName)
        etProductDescription = findViewById(R.id.etProductDescription)
        etProductPrice = findViewById(R.id.etProductPrice)
        etProductStock = findViewById(R.id.etProductStock)
        etCriticalStock = findViewById(R.id.etCriticalStock)
        etImageUrl = findViewById(R.id.etImageUrl)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnSaveProduct = findViewById(R.id.btnSaveProduct)
        ivProductPreview = findViewById(R.id.ivProductPreview)
        flImagePicker = findViewById(R.id.flImagePicker)
        tvImagePlaceholder = findViewById(R.id.tvImagePlaceholder)

        productoIdParaEdicion = intent.getIntExtra("PRODUCT_ID", 0)
        val formTitle = findViewById<TextView>(R.id.tvFormTitle)

        if (productoIdParaEdicion != 0) {
            formTitle.text = "Editar Producto"
            btnSaveProduct.text = "ACTUALIZAR PRODUCTO"
            cargarDatosDeEdicion(productoIdParaEdicion)
        } else {
            formTitle.text = "Crear Nuevo Producto"
            btnSaveProduct.text = "GUARDAR PRODUCTO"
        }
        cargarCategorias()
        btnSaveProduct.setOnClickListener {
            guardarOActualizarProducto()
        }
    }

     //Convierte una cadena Base64 a un objeto Bitmap, limpiando la cadena
     //y aplicando la corrección de rotación EXIF (solución a la imagen de lado).

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        try {
            val cleanedBase64Str = base64Str.replace("\n", "").replace("\r", "").replace(" ", "")
            if (cleanedBase64Str.isEmpty()) return null

            val decodedBytes = Base64.decode(cleanedBase64Str, Base64.DEFAULT)

            val originalBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            val inputStream = ByteArrayInputStream(decodedBytes)
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            val matrix = Matrix()
            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            matrix.postRotate(rotationAngle)

            return Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )

        } catch (e: Exception) {
            // Si falla la rotación  devuelve un Bitmap simple
            try {
                val cleanedBase64Str = base64Str.replace("\n", "").replace("\r", "").replace(" ", "")
                val decodedBytes = Base64.decode(cleanedBase64Str, Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e2: Exception) {
                Log.e("BASE64_DECODE_FAIL", "Fallo total al decodificar: ${e.message}")
                return null
            }
        }
    }

    fun onImagePickerClicked(view: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {

            val intent = Intent(this, CameraActivity::class.java)
            cameraLauncher.launch(intent)

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(this, CameraActivity::class.java)
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_LONG).show()
        }
    }


    // LÓGICA DE crud
    private fun cargarCategorias() {
        lifecycleScope.launch {
            val resultado = ProductosApiRepository.getCategorias()

            resultado.onSuccess { categorias ->
                listaCategorias = categorias
                val nombresCategorias = categorias.map { it.nombre }

                val adapter = ArrayAdapter(
                    this@MainActivity3,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombresCategorias
                )
                spinnerCategory.adapter = adapter

                if (productoIdParaEdicion != 0 && productoCargado != null) {
                    seleccionarCategoriaEnSpinner(productoCargado!!.categoria_id)
                }
            }.onFailure { exception ->
                Toast.makeText(this@MainActivity3, "Error al cargar categorías.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarDatosDeEdicion(id: Int) {
        lifecycleScope.launch {
            val resultado = ProductosApiRepository.getProductoById(id)

            resultado.onSuccess { producto ->
                productoCargado = producto

                // Rellenar los campos
                etProductCode.setText(producto.codigo_producto)
                etProductName.setText(producto.nombre)
                etProductDescription.setText(producto.descripcion)
                etProductPrice.setText(producto.precio.toString())
                etProductStock.setText(producto.stock.toString())
                etCriticalStock.setText(producto.stock_critico.toString())
                etImageUrl.setText(producto.imagen_url)

                //  Cargar y mostrar la imagen Base64 rotada
                if (!producto.imagen_url.isNullOrEmpty()) {
                    val bitmap = decodeBase64ToBitmap(producto.imagen_url)
                    if (bitmap != null) {
                        ivProductPreview.setImageBitmap(bitmap)
                        ivProductPreview.visibility = View.VISIBLE
                        tvImagePlaceholder.visibility = View.GONE
                        etImageUrl.visibility = View.GONE
                        base64Image = producto.imagen_url // Mantener el Base64 cargado para actualizar
                    }
                }

                if (listaCategorias.isNotEmpty()) {
                    seleccionarCategoriaEnSpinner(producto.categoria_id)
                }

            }.onFailure { exception ->
                Toast.makeText(this@MainActivity3, "Error al cargar producto para edición.", Toast.LENGTH_LONG).show()
                productoIdParaEdicion = 0
                btnSaveProduct.text = "GUARDAR PRODUCTO"
            }
        }
    }

    private fun seleccionarCategoriaEnSpinner(categoriaId: Int) {
        if (listaCategorias.isNotEmpty()) {
            val index = listaCategorias.indexOfFirst { it.id == categoriaId }
            if (index != -1) {
                spinnerCategory.setSelection(index)
            }
        }
    }


    // FUNCIÓN DE GUARDADO O ACTUALIZACIÓN (POST / PUT)
    private fun guardarOActualizarProducto() {

        val codigo = etProductCode.text.toString()
        val nombre = etProductName.text.toString()
        val descripcion = etProductDescription.text.toString()
        val precio = etProductPrice.text.toString().toIntOrNull() ?: 0
        val stock = etProductStock.text.toString().toIntOrNull() ?: 0
        val stockCritico = etCriticalStock.text.toString().toIntOrNull() ?: 0

        val posicionSeleccionada = spinnerCategory.selectedItemPosition
        val categoriaId: Int = if (posicionSeleccionada >= 0 && listaCategorias.isNotEmpty()) {
            listaCategorias[posicionSeleccionada].id
        } else {
            1
        }

        val imagenUrlParaApi = base64Image ?: etImageUrl.text.toString()

        if (nombre.isEmpty() || precio <= 0 || stock <= 0 || codigo.isEmpty() || imagenUrlParaApi.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {

            val resultado = if (productoIdParaEdicion != 0) {
                //llma a put
                ProductosApiRepository.updateProducto(
                    id = productoIdParaEdicion,
                    codigo = codigo, nombre = nombre, descripcion = descripcion, precio = precio,
                    stock = stock, stockCritico = stockCritico, imagenUrl = imagenUrlParaApi, categoriaId = categoriaId
                )
            } else {
                ProductosApiRepository.postProducto(
                    codigo = codigo, nombre = nombre, descripcion = descripcion, precio = precio,
                    stock = stock, stockCritico = stockCritico, imagenUrl = imagenUrlParaApi, categoriaId = categoriaId
                )
            }
            resultado.onSuccess { respuesta ->
                if (respuesta.status == "success") {
                    Toast.makeText(this@MainActivity3, "ÉXITO: ${respuesta.message}", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@MainActivity3, "ERROR BD: ${respuesta.message}", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }.onFailure { exception ->
                Toast.makeText(this@MainActivity3, "ERROR DE CONEXIÓN: ${exception.message}", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    fun onBackClicked(view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}