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
    private lateinit var productAdapter: ProductAdapter // Variable para guardar el adaptador

//LAUNCHER DE RESULTADOS (Para refrescar después de MainActivity3)
    private val cargarProductoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Si se devuelve RESULT_OK (se guardó o editó un producto)
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Operación exitosa. Recargando lista...", Toast.LENGTH_SHORT).show()

            // ⭐Recarga los datos reales de la API
            cargarProductosDesdeApi()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Inicialización de Vistas
        tvProductsCount = findViewById(R.id.tvProductsCount)
        recyclerView = findViewById(R.id.recyclerViewProducts)

        // Configurar el RecyclerView y el adaptador inicial
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializa el adaptador con una lista vacía y pasándose a sí mismo como listener
        productAdapter = ProductAdapter(productosList, this)
        recyclerView.adapter = productAdapter

        // Carga inicial de productos
        cargarProductosDesdeApi()
    }


     // implementamos Corrutinas para obtener la lista de productos de la API.

    private fun cargarProductosDesdeApi() {
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
                actualizarUI(emptyList())
            }
        }
    }

    // actualizacion de la interfaz
    private fun actualizarUI(productos: List<Producto>) {
        // 1. Contador
        tvProductsCount.text = getString(R.string.products_loaded_count, productos.size)

        // 2. Notificar al adaptador que los datos han cambiado
        (recyclerView.adapter as ProductAdapter).updateData(productos)

    }


     //Edición: Se llama cuando el usuario hace clic en el ítem o botón de edición OnItemActionListener

    override fun onEditClicked(productoId: Int) {
        val intent = Intent(this, MainActivity3::class.java).apply {
            // Pasamos el ID del producto seleccionado para la edición
            putExtra("PRODUCT_ID", productoId)
        }
        // Usamos el Launcher para abrir la actividad y esperar el resultado
        cargarProductoLauncher.launch(intent)
    }


     //Eliminación: Se llama cuando el usuario hace clic en el botón de eliminar.

    override fun onDeleteClicked(productoId: Int) {
        // Mostrar diálogo de confirmación antes de la eliminación
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el producto ID $productoId?")
            .setPositiveButton("Sí, Eliminar") { _, _ ->
                ejecutarEliminacion(productoId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Función de ejecución asíncrona para eliminar un producto
    private fun ejecutarEliminacion(productoId: Int) {
        lifecycleScope.launch {
            val resultado = ProductosApiRepository.deleteProducto(productoId)

            resultado.onSuccess { respuesta ->
                if (respuesta.status == "success") {
                    Toast.makeText(this@MainActivity2, "Éxito: ${respuesta.message}", Toast.LENGTH_LONG).show()
                    cargarProductosDesdeApi() // Refresca la lista inmediatamente
                } else {
                    Toast.makeText(this@MainActivity2, "Error al eliminar: ${respuesta.message}", Toast.LENGTH_LONG).show()
                }
            }.onFailure { exception ->
                Toast.makeText(this@MainActivity2, "Error de red al eliminar.", Toast.LENGTH_LONG).show()
            }
        }
    }


     //Maneja el clic en el botón '+' del XML (Crear Producto).

    public fun onAddProductClicked(view: View) {
        val intent = Intent(this, MainActivity3::class.java)
        // Usamos el Launcher para abrir la actividad y esperar el resultado
        cargarProductoLauncher.launch(intent)
    }


     //Maneja el clic en el botón de Logout (simulación).

    public fun onLogoutClicked(view: View) {
        Toast.makeText(this, "Cerrando sesión.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}