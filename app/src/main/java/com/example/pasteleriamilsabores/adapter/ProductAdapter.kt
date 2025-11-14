package com.example.pasteleriamilsabores.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.model.Producto
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    // Lista de productos y la función de clic
    private val productos: List<Producto>,
    private val onClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductoViewHolder>() {

    /**
     * ViewHolder: Mantiene las referencias a las vistas de item_product.xml
     */
    inner class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // IDs del layout item_product.xml
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductDetails: TextView = view.findViewById(R.id.tvProductDetails) // Cambiado a Details
        val ivProductIcon: ImageView = view.findViewById(R.id.ivProductIcon)
        val ivEditCheck: ImageView = view.findViewById(R.id.ivEditCheck)

        // Formateador para mostrar el precio correctamente en CLP
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            maximumFractionDigits = 0
        }

        fun bind(producto: Producto) {
            tvProductName.text = producto.nombre

            val precioFormateado = currencyFormatter.format(producto.precio)

            // Unimos Stock y Precio en un solo TextView para reflejar el diseño
            tvProductDetails.text = "Stock: ${producto.stock} | Precio: ${precioFormateado}"

            // El icono de edición (ivEditCheck) es solo visual, el clic va en el item completo

            // Establecer el Listener para la acción de clic
            itemView.setOnClickListener {
                onClick(producto) // Llama a la función lambda en MainActivity2
            }

            // Lógica simple para la imagen (solo por si el campo imagen_url no es una URL)
            if (producto.imagen_url.isNotEmpty()) {
                // Aquí deberías usar una librería como Glide para cargar la imagen real
                // Glide.with(itemView.context).load("URL_COMPLETA_DE_LA_IMAGEN").into(ivProductIcon)
            } else {
                // Si no hay imagen, usar un ícono por defecto
                ivProductIcon.setImageResource(R.drawable.ic_menu)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        // Infla el layout del ítem que acabamos de definir
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size
}