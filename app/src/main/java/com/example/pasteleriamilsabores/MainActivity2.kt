package com.example.pasteleriamilsabores

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// creamos product.kt y adapter para crear producros de prueba que se muestran en el dashboard

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // 1. Crear datos de prueba (Mockup)
        val mockProducts = listOf(
            Product("001", "Torta de Chocolate Fusión", 12500.0, 15, "Deliciosa..."),
            Product("002", "Pie de Limón Clásico", 8900.0, 8, "El clásico...")
        )

        // 2. Corregir el contador de productos
        val tvProductsCount = findViewById<TextView>(R.id.tvProductsCount)
        tvProductsCount.text = getString(R.string.products_loaded_count, mockProducts.size)

        // 3. Inicializar RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Definir la acción de edición (navegación a MainActivity3 con datos)
        recyclerView.adapter = ProductAdapter(mockProducts) { product ->
            val intent = Intent(this, MainActivity3::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            startActivity(intent)
        }
    }


    public fun onAddProductClicked(view: View) {
        // Navegar a MainActivity3 (Carga/Edición de Producto)
        val intent = Intent(this, MainActivity3::class.java)
        startActivity(intent)

    }
    public fun onLogoutClicked(view: View) {
        Toast.makeText(this, "Cerrando sesión.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
