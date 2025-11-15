package com.example.pasteleriamilsabores.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.model.Producto
import com.example.pasteleriamilsabores.repository.ProductosApiRepository
import kotlinx.coroutines.launch

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

    private var listaCategorias: List<Categoria> = emptyList()
    private var base64Image: String? = null

    // ⭐️ Variable clave para manejar el modo (0 = Crear, > 0 = Edición)
    private var productoIdParaEdicion: Int = 0
    private var productoCargado: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // 1. Inicialización de Vistas
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

        // 2. DETECCIÓN DE MODO EDICIÓN/CREACIÓN
        productoIdParaEdicion = intent.getIntExtra("PRODUCT_ID", 0)

        val formTitle = findViewById<TextView>(R.id.tvFormTitle)

        if (productoIdParaEdicion != 0) {
            // Modo Edición: Cargar datos y ajustar la UI
            formTitle.text = "Editar Producto"
            btnSaveProduct.text = "ACTUALIZAR PRODUCTO"
            cargarDatosDeEdicion(productoIdParaEdicion)
        } else {
            // Modo Creación
            formTitle.text = "Crear Nuevo Producto"
            btnSaveProduct.text = "GUARDAR PRODUCTO"
        }

        // 3. Listeners
        cargarCategorias()
        btnSaveProduct.setOnClickListener {
            guardarOActualizarProducto()
        }
    }

    // =======================================================
    // MÉTODOS ONCLICK DEL XML
    // =======================================================

    /**
     * Maneja el clic en la flecha de volver atrás.
     */
    fun onBackClicked(view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    /**
     * Maneja el clic en el FrameLayout para la selección de foto.
     */
    fun onImagePickerClicked(view: View) {
        Toast.makeText(this, "Iniciando selección de foto (Lógica de cámara/galería).", Toast.LENGTH_LONG).show()
    }


    // =======================================================
    // LÓGICA DE CARGA DE DATOS Y CATEGORÍAS (GET)
    // =======================================================

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

                // Si ya estamos editando y el producto se cargó antes, seleccionamos la opción.
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

                // Seleccionar la categoría después de que la lista de categorías se haya cargado (si ya lo hizo)
                if (listaCategorias.isNotEmpty()) {
                    seleccionarCategoriaEnSpinner(producto.categoria_id)
                }

            }.onFailure { exception ->
                Toast.makeText(this@MainActivity3, "Error al cargar producto para edición.", Toast.LENGTH_LONG).show()
                // Si falla la carga, volvemos a modo Creación
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


    // =======================================================
    // FUNCIÓN DE GUARDADO O ACTUALIZACIÓN (POST / PUT)
    // =======================================================
    private fun guardarOActualizarProducto() {

        // 1. Recolección de Datos
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

        val imagenData = base64Image ?: etImageUrl.text.toString()

        // 2. Validación
        if (nombre.isEmpty() || precio <= 0 || stock <= 0 || codigo.isEmpty() || imagenData.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios.", Toast.LENGTH_LONG).show()
            return
        }

        // 3. Ejecutar Corrutina y API
        lifecycleScope.launch {

            val resultado = if (productoIdParaEdicion != 0) {
                // MODO EDICIÓN: Llama a UPDATE (PUT)
                ProductosApiRepository.updateProducto(
                    id = productoIdParaEdicion,
                    codigo = codigo, nombre = nombre, descripcion = descripcion, precio = precio,
                    stock = stock, stockCritico = stockCritico, imagenUrl = imagenData, categoriaId = categoriaId
                )
            } else {
                // MODO CREACIÓN: Llama a POST
                ProductosApiRepository.postProducto(
                    codigo = codigo, nombre = nombre, descripcion = descripcion, precio = precio,
                    stock = stock, stockCritico = stockCritico, imagenUrl = imagenData, categoriaId = categoriaId
                )
            }

            // 4. Procesar Resultado
            resultado.onSuccess { respuesta ->
                if (respuesta.status == "success") {
                    Toast.makeText(this@MainActivity3, "ÉXITO: ${respuesta.message}", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_OK) // Indica que el listado debe refrescar
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
}