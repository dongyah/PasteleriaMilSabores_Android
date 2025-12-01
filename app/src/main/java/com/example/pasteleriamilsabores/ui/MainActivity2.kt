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

    // declaraciones de vistas y datos
    private var productosList: List<Producto> = emptyList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProductsCount: TextView
    private lateinit var productAdapter: ProductAdapter

    // launcher de resultados (refresca lista al volver de mainactivity3)
    private val cargarProductoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Operación exitosa. recargando lista...", Toast.LENGTH_SHORT).show()
            cargarProductosDesdeApi()
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

        // carga inicial de datos
        cargarProductosDesdeApi()
    }


    // carga los productos desde el repositorio híbrido (php o sqlite)
    private fun cargarProductosDesdeApi() {
        lifecycleScope.launch {
            // llama a getallproductos y pasa el contexto
            val resultado = ProductosApiRepository.getAllProductos(applicationContext)

            resultado.onSuccess { productos ->
                // éxito: los datos llegan correctamente
                productosList = productos
                actualizarUI(productosList)
            }.onFailure { exception ->
                // falla: error de red o bd
                Toast.makeText(this@MainActivity2, "Error al cargar productos: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("DB_LOAD", "Fallo al cargar productos", exception)
                actualizarUI(emptyList())
            }
        }
    }

    // actualizacion de la interfaz
    private fun actualizarUI(productos: List<Producto>) {
        // 1. contador
        tvProductsCount.text = getString(R.string.products_loaded_count, productos.size)

        // 2. notificar al adaptador que los datos han cambiado
        (recyclerView.adapter as ProductAdapter).updateData(productos)
    }

    // edición: implementa onitemactionlistener
    override fun onEditClicked(productoId: Int) {
        val intent = Intent(this, MainActivity3::class.java).apply {
            // pasamos el id del producto seleccionado para la edición
            putExtra("PRODUCT_ID", productoId)
        }
        // usamos el launcher para abrir la actividad
        cargarProductoLauncher.launch(intent)
    }

    // eliminación: implementa onitemactionlistener
    override fun onDeleteClicked(productoId: Int) {
        // mostrar diálogo de confirmación antes de la eliminación
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el producto id $productoId?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                ejecutarEliminacion(productoId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // función de ejecución asíncrona para eliminar un producto
    private fun ejecutarEliminacion(productoId: Int) {
        lifecycleScope.launch {
            // llama a deleteproducto, requiere el contexto y devuelve result<unit>
            val resultado = ProductosApiRepository.deleteProducto(applicationContext, productoId)

            // el resultado es result<unit>, solo verificamos éxito o fallo
            resultado.onSuccess {
                Toast.makeText(this@MainActivity2, "Éxito: producto eliminado.", Toast.LENGTH_LONG).show()
                cargarProductosDesdeApi() // refresca la lista inmediatamente
            }.onFailure { exception ->
                // captura errores de red si el repositorio falló
                Toast.makeText(this@MainActivity2, "Error al eliminar: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // botón '+' del xml
    fun onAddProductClicked(view: View) {
        val intent = Intent(this, MainActivity3::class.java)
        cargarProductoLauncher.launch(intent)
    }

    // botón logout del xml
    fun onLogoutClicked(view: View) {
        Toast.makeText(this, "Cerrando sesión.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}