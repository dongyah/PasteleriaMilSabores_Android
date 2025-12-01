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


class MainActivity3 : AppCompatActivity() {

    // declaraciones de vistas
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

    // botón de ia
    private lateinit var btnAiDescription: Button

    private var listaCategorias: List<Categoria> = emptyList()
    private var base64Image: String? = null

    private var productoIdParaEdicion: Int = 0
    private var productoCargado: Producto? = null
    private val CAMERA_REQUEST_CODE = 102

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val base64 = result.data?.getStringExtra("IMAGE_BASE64")

            if (base64.isNullOrEmpty()) {
                Toast.makeText(this, "Error: no se recibió la cadena base64.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            base64Image = base64

            val bitmap = decodeBase64ToBitmap(base64)

            if (bitmap != null) {
                // éxito: muestra la imagen
                ivProductPreview.setImageBitmap(bitmap)
                ivProductPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                ivProductPreview.visibility = View.VISIBLE
                tvImagePlaceholder.visibility = View.GONE
                etImageUrl.visibility = View.GONE

                Toast.makeText(this, "Foto cargada y codificada.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("IMAGE_LOAD", "Fallo al decodificar base64 a bitmap.")
                base64Image = base64
            }
        } else {
            Toast.makeText(this, "Captura cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // inicializar vistas
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

        // inicializar y asignar listeners al botón de ia
        btnAiDescription = findViewById(R.id.btnAiDescription)

        // detección de modo edición
        productoIdParaEdicion = intent.getIntExtra("PRODUCT_ID", 0)
        val formTitle = findViewById<TextView>(R.id.tvFormTitle)

        if (productoIdParaEdicion != 0) {
            formTitle.text = "EDITAR PRODUCTO"
            btnSaveProduct.text = "ACTUALIZAR PRODUCTO"
            cargarDatosDeEdicion(productoIdParaEdicion)
        } else {
            formTitle.text = "CREAR NUEVO PRODUCTO"
            btnSaveProduct.text = "GUARDAR PRODUCTO"
        }

        cargarCategorias()

        btnSaveProduct.setOnClickListener {
            guardarOActualizarProducto()
        }

        // listeners ia
        btnAiDescription.setOnClickListener { generarDescripcionIA() }
        // la función mejorarimagenia fue eliminada del xml/código.
    }

    // convierte base64 a bitmap de forma simple para la vista previa.
    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        try {
            // 1. limpiar la cadena
            val cleanedBase64Str = base64Str
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "")

            if (cleanedBase64Str.isEmpty()) return null

            // 2. decodificar la cadena base64
            val decodedBytes = Base64.decode(cleanedBase64Str, Base64.DEFAULT)

            // 3. crear el bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        } catch (e: Exception) {
            Log.e("BASE64_DECODE_FAIL", "Error al decodificar y mostrar el bitmap: ${e.message}")
            return null
        }
    }


    // función de generación de descripción con ia (gemini)
    private fun generarDescripcionIA() {
        val nombreProducto = etProductName.text.toString().trim()

        if (nombreProducto.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el nombre del producto primero.", Toast.LENGTH_SHORT).show()
            return
        }

        // deshabilitar ui mientras procesa
        btnAiDescription.isEnabled = false
        etProductDescription.setText("Generando descripción con ia...")

        lifecycleScope.launch {

            // 1. llamar al repositorio (gemini)
            val resultado = ProductosApiRepository.generateDescription(nombreProducto)

            // 2. procesar el resultado
            resultado.onSuccess { descripcion ->
                // éxito: rellenar el campo de texto
                etProductDescription.setText(descripcion)
                Toast.makeText(this@MainActivity3, "Descripción generada con éxito.", Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                // falla: mostrar el error y limpiar el campo
                etProductDescription.setText("")
                Toast.makeText(this@MainActivity3, "Fallo al generar descripción: ${exception.message}", Toast.LENGTH_LONG).show()
            }

            // 3. restaurar ui
            btnAiDescription.isEnabled = true
        }
    }


    // lanza la actividad de la camara
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


    // carga las categorias desde el repositorio
    private fun cargarCategorias() {
        lifecycleScope.launch {
            // requiere applicationcontext para acceder a la db híbrida
            val resultado = ProductosApiRepository.getAllCategorias(applicationContext)

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
                Toast.makeText(this@MainActivity3, "Error de red al cargar categorías: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // carga los datos de un producto para edición
    private fun cargarDatosDeEdicion(id: Int) {
        lifecycleScope.launch {
            // requiere applicationcontext para acceder a la db híbrida
            val resultado = ProductosApiRepository.getProductoById(applicationContext, id)

            resultado.onSuccess { producto ->
                productoCargado = producto

                // rellenar los campos
                etProductCode.setText(producto?.codigo_producto)
                etProductName.setText(producto?.nombre)
                etProductDescription.setText(producto?.descripcion)
                etProductPrice.setText(producto?.precio.toString())
                etProductStock.setText(producto?.stock.toString())
                etCriticalStock.setText(producto?.stock_critico.toString())
                etImageUrl.setText(producto?.imagen_url)

                // cargar y mostrar la imagen base64
                if (!producto?.imagen_url.isNullOrEmpty()) {
                    val bitmap = decodeBase64ToBitmap(producto!!.imagen_url)
                    if (bitmap != null) {
                        ivProductPreview.setImageBitmap(bitmap)
                        ivProductPreview.visibility = View.VISIBLE
                        tvImagePlaceholder.visibility = View.GONE
                        etImageUrl.visibility = View.GONE
                        base64Image = producto.imagen_url
                    }
                }

                if (listaCategorias.isNotEmpty() && producto != null) {
                    seleccionarCategoriaEnSpinner(producto.categoria_id)
                }

            }.onFailure { exception ->
                Toast.makeText(this@MainActivity3, "Error de red al cargar producto para edición.", Toast.LENGTH_LONG).show()
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


    // función de guardado o actualización (create/update)
    private fun guardarOActualizarProducto() {

        // recolección de datos
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

        // crear el objeto producto para la base de datos
        val productoGuardar = Producto(
            id = productoIdParaEdicion,
            codigo_producto = codigo,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            stock_critico = stockCritico,
            imagen_url = imagenUrlParaApi,
            categoria_id = categoriaId
        )

        // ejecutar corrutina y repositorio
        lifecycleScope.launch {

            val resultado = if (productoIdParaEdicion != 0) {
                // update
                ProductosApiRepository.updateProducto(
                    context = applicationContext,
                    producto = productoGuardar
                )
            } else {
                // create
                ProductosApiRepository.insertProducto(
                    context = applicationContext,
                    producto = productoGuardar
                )
            }

            // procesar resultado
            resultado.onSuccess {
                Toast.makeText(this@MainActivity3, "Éxito: producto guardado (intentando php).", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_OK)
                finish()
            }.onFailure { exception ->
                // este mensaje capturará si el servidor php falló
                Toast.makeText(this@MainActivity3, "Error de sincronización: ${exception.message}", Toast.LENGTH_LONG).show()
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