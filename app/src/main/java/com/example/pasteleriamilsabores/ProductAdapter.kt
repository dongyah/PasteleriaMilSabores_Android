package com.example.pasteleriamilsabores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

// 1. Define el ViewHolder (encuentra los elementos dentro de item_product.xml)
class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvProductName: TextView = view.findViewById(R.id.tvProductName)
    val tvProductDetails: TextView = view.findViewById(R.id.tvProductDetails)
    val ivEdit: ImageView = view.findViewById(R.id.ivEdit)
    // Agrega más elementos de tu item_product.xml (como la imagen) aquí.
}

// 2. Define el Adaptador
class ProductAdapter(
    private val productoList: List<Producto>,
    private val onEditClicked: (Producto) -> Unit // Función lambda para manejar el clic en editar
) : RecyclerView.Adapter<ProductViewHolder>() {

    // Crea el ViewHolder inflando el layout de la fila
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    // Vincula los datos del modelo (Producto) a los elementos de la vista (ViewHolder)
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productoList[position]

        holder.tvProductName.text = product.name
        holder.tvProductDetails.text = "Stock: ${product.stock} | Precio: $${product.price}"

        // Manejar el clic en el botón de edición
        holder.ivEdit.setOnClickListener {
            onEditClicked(product)
        }
    }

    override fun getItemCount() = productoList.size
}