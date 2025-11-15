package com.example.pasteleriamilsabores.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pasteleriamilsabores.R
import com.example.pasteleriamilsabores.model.Producto
import java.text.NumberFormat
import java.util.Locale

/**
 * Interfaz que define las acciones que debe implementar MainActivity2 (el host del RecyclerView).
 * Esto asegura que los clics en los botones de Edición/Eliminación se manejen en la Activity.
 */
interface OnItemActionListener {
    fun onEditClicked(productoId: Int)
    fun onDeleteClicked(productoId: Int)
}

class ProductAdapter(
    // La lista de productos debe ser 'var' para poder actualizarla con updateData
    private var productos: List<Producto>,
    private val listener: OnItemActionListener // ⭐️ El listener de la Activity2
) : RecyclerView.Adapter<ProductAdapter.ProductoViewHolder>() {

    /**
     * ViewHolder: Mantiene las referencias a las vistas de item_product.xml
     */
    inner class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // IDs del layout item_product.xml
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductDetails: TextView = view.findViewById(R.id.tvProductDetails)
        val ivProductIcon: ImageView = view.findViewById(R.id.ivProductIcon) // Icono o Imagen
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete) // ⭐️ Botón de Eliminar

        // Formateador para mostrar el precio correctamente en CLP
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            maximumFractionDigits = 0
        }

        fun bind(producto: Producto) {
            tvProductName.text = producto.nombre

            val precioFormateado = currencyFormatter.format(producto.precio)

            // Muestra Stock y Precio
            tvProductDetails.text = "Stock: ${producto.stock} | Precio: ${precioFormateado}"

            // Lógica de Imagen/Ícono por Defecto
            if (producto.imagen_url.isNotEmpty()) {
                // Aquí usarías GLIDE o PICASSO para cargar la imagen real desde la URL/nombre
                // Ejemplo: Glide.with(itemView.context).load(URL_BASE + producto.imagen_url).into(ivProductIcon)
            } else {
                // Usar un ícono por defecto si no hay imagen
                ivProductIcon.setImageResource(R.drawable.ic_menu)
            }

            // =======================================================
            // LISTENERS DE ACCIÓN CLAVE
            // =======================================================

            // 1. Acción de Edición (Se lanza al hacer clic en el ítem completo)
            itemView.setOnClickListener {
                listener.onEditClicked(producto.id)
            }

            // 2. Acción de Eliminación (Llamada al listener de la Activity)
            btnDelete.setOnClickListener {
                listener.onDeleteClicked(producto.id)
            }
        }
    }

    /**
     * Se llama cuando el RecyclerView necesita un nuevo ViewHolder (fila).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        // Infla el layout del ítem
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductoViewHolder(view)
    }

    /**
     * Se llama para mostrar los datos en la posición especificada.
     */
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    /**
     * Retorna el número total de ítems en la lista.
     */
    override fun getItemCount(): Int = productos.size

    /**
     * Función clave para actualizar los datos del RecyclerView sin recrear el adaptador.
     * Esto es más eficiente que asignar un nuevo adaptador.
     */
    fun updateData(newProducts: List<Producto>) {
        productos = newProducts // Reemplaza la lista antigua por la nueva
        notifyDataSetChanged() // Notifica al RecyclerView que debe redibujar
    }
}