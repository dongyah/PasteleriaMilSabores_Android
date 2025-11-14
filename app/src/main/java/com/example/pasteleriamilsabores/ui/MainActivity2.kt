package com.example.pasteleriamilsabores.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts // Necesario para esperar resultados
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Necesario para Corrutinas
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.adapter.ProductAdapter
import com.example.pasteleriamilsabores.model.Producto
import com.example.pasteleriamilsabores.repository.ProductosApiRepository
import kotlinx.coroutines.launch

class MainActivity2 : AppCompatActivity() {

    // Declaraciones de Vistas y Datos
    private var productosList: List<Producto> = emptyList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProductsCount: TextView

    // =======================================================
    // 1. LAUNCHER DE RESULTADOS (Para refrescar después de MainActivity3)
    // =======================================================
    private val cargarProductoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Este bloque se ejecuta al volver de MainActivity3
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "¡Operación exitosa! Recargando lista...", Toast.LENGTH_SHORT).show()

            // ⭐️ Llamada clave: Recarga los datos de la API
            cargarProductosDesdeApi()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Inicialización de Vistas
        tvProductsCount = findViewById(R.id.tvProductsCount)
        recyclerView = findViewById(R.id.recyclerViewProducts)

        // Configurar el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carga inicial de productos usando Corrutinas
        cargarProductosDesdeApi()
    }


    // =======================================================
    // 2. LÓGICA DE CARGA CON CORRUTINAS (GET)
    // =======================================================
    /**
     * Usa Corrutinas para obtener la lista de productos de la API.
     */
    private fun cargarProductosDesdeApi() {
        // Inicia una Corrutina en el ámbito de vida de la Activity
        lifecycleScope.launch {

            val resultado = ProductosApiRepository.getProductos()

            resultado.onSuccess { productos ->
                // Éxito: Los datos de la BD llegan correctamente
                productosList = productos
                actualizarUI(productosList)
            }.onFailure { exception ->
                // Falla: Error de red, PHP caído, o JSON malformado
                Toast.makeText(this@MainActivity2, "Error al cargar productos: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("API_LOAD", "Fallo al cargar productos desde la API", exception)
                // Si falla, muestra la lista vacía
                actualizarUI(emptyList())
            }
        }
    }

    // =======================================================
    // 3. ACTUALIZACIÓN DE LA INTERFAZ
    // =======================================================
    private fun actualizarUI(productos: List<Producto>) {
        // 1. Contador
        tvProductsCount.text = getString(R.string.products_loaded_count, productos.size)

        // 2. Adapter
        recyclerView.adapter = ProductAdapter(productos) { product ->
            // Acción al hacer clic en un ítem: Navegar a MainActivity3 para Editar
            val intent = Intent(this, MainActivity3::class.java).apply {
                // Pasamos el ID del producto seleccionado para la edición
                putExtra("PRODUCT_ID", product.id)
            }
            // Usamos el Launcher para abrir la actividad y esperar el resultado
            cargarProductoLauncher.launch(intent)
        }
    }


    // =======================================================
    // 4. MÉTODOS ONCLICK DEL XML
    // =======================================================

    /**
     * Maneja el clic en el botón '+' del XML.
     * Inicia MainActivity3 (Carga de Producto).
     */
    public fun onAddProductClicked(view: View) {
        val intent = Intent(this, MainActivity3::class.java)
        // Usamos el Launcher para abrir la actividad de Carga y esperar el resultado
        cargarProductoLauncher.launch(intent)
    }

    /**
     * Maneja el clic en el botón de Logout (simulación).
     */
    public fun onLogoutClicked(view: View) {
        Toast.makeText(this, "Cerrando sesión.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}