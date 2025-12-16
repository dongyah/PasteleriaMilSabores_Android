package com.example.pasteleriamilsabores.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.adapter.ProductAdapter
import com.example.pasteleriamilsabores.adapter.OnItemActionListener
import com.example.pasteleriamilsabores.model.Producto
import com.example.pasteleriamilsabores.repository.ProductosApiRepository
import kotlinx.coroutines.launch

class MainActivity2 : AppCompatActivity(), OnItemActionListener {

    // Declaraciones de Vistas y Datos
    private var productosList: List<Producto> = emptyList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProductsCount: TextView
    private lateinit var productAdapter: ProductAdapter

    // Launcher de resultados
    private val cargarProductoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Operación exitosa.", Toast.LENGTH_SHORT).show()
            // No cargamos aquí explícitamente porque onResume lo hará al volver
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        tvProductsCount = findViewById(R.id.tvProductsCount)
        recyclerView = findViewById(R.id.recyclerViewProducts)

        recyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = ProductAdapter(productosList, this)
        recyclerView.adapter = productAdapter

        // NOTA: Quitamos cargarProductosDesdeApi() de aquí para evitar
        // que se ejecute doble vez (onCreate + onResume).
    }

    // ---------------------------------------------------------------
    // NUEVO: Sincronización Automática al volver a la pantalla
    // ---------------------------------------------------------------
    override fun onResume() {
        super.onResume()

        // 1. Carga inmediata de lo que ya existe localmente (SQLite)
        cargarProductosDesdeApi()

        // 2. Intenta sincronizar cambios pendientes en segundo plano
        lifecycleScope.launch {
            // Esta función (que agregamos al Repo) subirá los pendientes si hay internet
            ProductosApiRepository.sincronizarProductos(applicationContext)

            // 3. Volvemos a cargar por si la sincronización trajo IDs nuevos o actualizaciones
            cargarProductosDesdeApi()
        }
    }
    // ---------------------------------------------------------------

    private fun cargarProductosDesdeApi() {
        lifecycleScope.launch {
            val resultado = ProductosApiRepository.getAllProductos(applicationContext)

            resultado.onSuccess { productos ->
                productosList = productos
                actualizarUI(productosList)
            }.onFailure { exception ->
                Toast.makeText(this@MainActivity2, "Error al cargar: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("DB_LOAD", "Fallo al cargar productos", exception)
                actualizarUI(emptyList())
            }
        }
    }

    private fun actualizarUI(productos: List<Producto>) {
        tvProductsCount.text = getString(R.string.products_loaded_count, productos.size)
        (recyclerView.adapter as ProductAdapter).updateData(productos)
    }

    override fun onEditClicked(productoId: Int) {
        val intent = Intent(this, MainActivity3::class.java).apply {
            putExtra("PRODUCT_ID", productoId)
        }
        cargarProductoLauncher.launch(intent)
    }

    override fun onDeleteClicked(productoId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el producto ID $productoId?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                ejecutarEliminacion(productoId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarEliminacion(productoId: Int) {
        lifecycleScope.launch {
            val resultado = ProductosApiRepository.deleteProducto(applicationContext, productoId)

            resultado.onSuccess {
                Toast.makeText(this@MainActivity2, "Éxito: Producto eliminado.", Toast.LENGTH_LONG).show()
                cargarProductosDesdeApi()
            }.onFailure { exception ->
                Toast.makeText(this@MainActivity2, "Error al eliminar: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onAddProductClicked(view: View) {
        val intent = Intent(this, MainActivity3::class.java)
        cargarProductoLauncher.launch(intent)
    }

    fun onLogoutClicked(view: View) {
        Toast.makeText(this, "Cerrando sesión.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}