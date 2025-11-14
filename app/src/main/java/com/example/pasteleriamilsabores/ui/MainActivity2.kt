package com.example.pasteleriamilsabores.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.model.Producto
import com.example.pasteleriamilsabores.adapter.ProductAdapter // Importa tu adaptador

class MainActivity2 : AppCompatActivity() {

    // Variable para la lista de productos real (la llenaremos desde la API)
    private var productosList: List<Producto> = emptyList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProductsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // 1. Inicialización de Vistas
        tvProductsCount = findViewById(R.id.tvProductsCount)
        recyclerView = findViewById(R.id.recyclerViewProducts)

        // Configurar el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 2. Cargar Datos (usando Mockup por ahora)
        cargarDatosMockup()

        // 3. (Futuro) Llamada a la API con Corrutinas:
        // cargarProductosDesdeApi()
    }

    override fun onResume() {
        super.onResume()
        // Cuando volvemos de MainActivity3 (guardar/editar), la lista debería actualizarse
        // Si usas la función de API real, la llamas aquí:
        // cargarProductosDesdeApi()
    }

    // =======================================================
    // FUNCIÓN DE MOCKUP (DATOS DE PRUEBA)
    // =======================================================
    private fun cargarDatosMockup() {
        // Datos de prueba, asegurando que los tipos (INT, STRING) coincidan con Producto.kt
        val mockProductos = listOf(
            Producto(
                id = 1,
                codigo_producto = "P-001",
                nombre = "Torta de Chocolate Fusión",
                descripcion = "Bizcocho húmedo con ganache de chocolate y toques de frambuesa.",
                precio = 12500, // INT
                stock = 15,     // INT
                stock_critico = 5,
                imagen_url = "torta_choc.jpg",
                categoria_id = 1
            ),
            Producto(
                id = 2,
                codigo_producto = "P-002",
                nombre = "Pie de Limón Clásico",
                descripcion = "Base de galleta crujiente y crema ácida con merengue italiano.",
                precio = 8900, // INT
                stock = 8,      // INT
                stock_critico = 3,
                imagen_url = "pie_limon.jpg",
                categoria_id = 3
            )
        )

        // Asignar los datos y configurar el adaptador
        productosList = mockProductos
        actualizarUI(productosList)
    }

    // =======================================================
    // FUNCIÓN PARA ACTUALIZAR VISTAS (Contador y RecyclerView)
    // =======================================================
    private fun actualizarUI(productos: List<Producto>) {
        // 1. Contador
        tvProductsCount.text = getString(R.string.products_loaded_count, productos.size)

        // 2. Adapter (Conexión final)
        recyclerView.adapter = ProductAdapter(productos) { product ->
            // Acción al hacer clic: Navegar a MainActivity3 para Editar
            val intent = Intent(this, MainActivity3::class.java).apply {
                // Pasamos el ID del producto seleccionado para que la otra Activity sepa qué cargar
                putExtra("PRODUCT_ID", product.id)
            }
            startActivity(intent)
        }
    }


    // =======================================================
    // MÉTODOS ONCLICK DEL XML
    // =======================================================

    public fun onAddProductClicked(view: View) {
        // Navegar a MainActivity3 (Carga de Producto - SIN datos de edición)
        val intent = Intent(this, MainActivity3::class.java)
        startActivity(intent)
    }

    public fun onLogoutClicked(view: View) {
        Toast.makeText(this, "Cerrando sesión.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java) // Asumiendo MainActivity es la pantalla de Login
        startActivity(intent)
        finish()
    }

    // =======================================================
    // FUNCIÓN FUTURA DE LA API (Corrutinas)
    // =======================================================
    /*
    private fun cargarProductosDesdeApi() {
        lifecycleScope.launch {
            val resultado = ProductosApiRepository.getProductos()

            resultado.onSuccess { productos ->
                productosList = productos // Asigna los datos reales de la API
                actualizarUI(productosList) // Actualiza el RecyclerView
            }.onFailure { exception ->
                Toast.makeText(this@MainActivity2, "Error al cargar API: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    */
}