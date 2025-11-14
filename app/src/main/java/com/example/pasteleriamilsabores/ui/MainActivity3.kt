package com.example.pasteleriamilsabores.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Necesario para Corrutinas
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.repository.ProductosApiRepository
import kotlinx.coroutines.launch

class MainActivity3 : AppCompatActivity() {

    // Declaración de las Vistas (lateinit var)
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

    // Lista para almacenar los objetos Categoria cargados de la API
    private var listaCategorias: List<Categoria> = emptyList()
    private var base64Image: String? = null // Usado para enviar la imagen codificada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // =======================================================
        // 1. Inicialización de Vistas
        // =======================================================
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

        // =======================================================
        // 2. Cargar Categorías y Listeners
        // =======================================================
        cargarCategorias() // Función para llenar el Spinner

        btnSaveProduct.setOnClickListener {
            guardarProducto() // Inicia la corrutina de guardado
        }
    }

    // =======================================================
    // MÉTODOS REQUERIDOS POR ANDROID:ONCLICK EN EL XML
    // =======================================================

    /**
     * Maneja el clic en el botón de la flecha de volver atrás.
     */
    fun onBackClicked(view: View) {
        // En caso de solo cerrar el formulario sin guardar, notificamos el cierre (CANCELADO)
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    /**
     * Maneja el clic en el FrameLayout para la selección de foto.
     */
    fun onImagePickerClicked(view: View) {
        // Aquí se usarían las clases de tu profesor (CameraManager, CamaraUtils)
        Toast.makeText(this, "Iniciando selección de foto (Lógica de cámara/galería).", Toast.LENGTH_LONG).show()
    }

    // =======================================================
    // LÓGICA DE LA APLICACIÓN
    // =======================================================

    /**
     * Usa Corrutinas para obtener la lista de categorías (GET) y llenar el Spinner.
     */
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

            }.onFailure { exception ->
                Toast.makeText(this@MainActivity3, "Error al cargar categorías: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("CATEGORIA_API", "Fallo al cargar categorías", exception)
            }
        }
    }

    /**
     * Función principal para recopilar datos y enviar a la API (POST) usando el Repositorio.
     */
    private fun guardarProducto() {

        // 1. Recolección de Datos
        val codigo = etProductCode.text.toString()
        val nombre = etProductName.text.toString()
        val descripcion = etProductDescription.text.toString()
        val precio = etProductPrice.text.toString().toIntOrNull() ?: 0
        val stock = etProductStock.text.toString().toIntOrNull() ?: 0
        val stockCritico = etCriticalStock.text.toString().toIntOrNull() ?: 0

        // Obtener el ID de la Categoría seleccionada
        val posicionSeleccionada = spinnerCategory.selectedItemPosition
        val categoriaId: Int = if (posicionSeleccionada >= 0 && listaCategorias.isNotEmpty()) {
            listaCategorias[posicionSeleccionada].id
        } else {
            1 // Valor de seguridad si la lista está vacía (asumimos ID 1)
        }

        // Usar la cadena Base64 (si existe) o el texto del campo URL
        val imagenData = base64Image ?: etImageUrl.text.toString()

        // 2. Validación Básica
        if (nombre.isEmpty() || precio <= 0 || stock <= 0 || codigo.isEmpty() || imagenData.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios.", Toast.LENGTH_LONG).show()
            return
        }

        // 3. Lanzar la Corrutina
        lifecycleScope.launch {

            val resultado = ProductosApiRepository.postProducto(
                codigo = codigo,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                stockCritico = stockCritico,
                imagenUrl = imagenData,
                categoriaId = categoriaId
            )

            // 4. Procesar el Resultado
            resultado.onSuccess { respuesta ->
                if (respuesta.status == "success") {
                    Toast.makeText(this@MainActivity3, "ÉXITO: ${respuesta.message}", Toast.LENGTH_LONG).show()

                    // ⭐️ CLAVE PARA EL REFRESCO: Indica éxito y cierra
                    setResult(Activity.RESULT_OK)
                    finish()

                } else {
                    Toast.makeText(this@MainActivity3, "ERROR BD: ${respuesta.message}", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_CANCELED) // Fallo, no recargar
                    finish()
                }
            }.onFailure { exception ->
                Toast.makeText(this@MainActivity3, "ERROR DE CONEXIÓN: ${exception.message}", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED) // Fallo, no recargar
                finish()
            }
        }
    }
}