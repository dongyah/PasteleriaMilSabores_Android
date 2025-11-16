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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix
import java.io.ByteArrayInputStream // Nuevo: Para leer EXIF desde el array de bytes

interface OnItemActionListener {
    fun onEditClicked(productoId: Int)
    fun onDeleteClicked(productoId: Int)
}

class ProductAdapter(
    private var productos: List<Producto>,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<ProductAdapter.ProductoViewHolder>() {

    // =======================================================
    // FUNCIÓN CLAVE: DECODIFICACIÓN Y ROTACIÓN BASE64
    // =======================================================
    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        try {
            val cleanedBase64Str = base64Str
                .replace("\n", "").replace("\r", "").replace(" ", "")

            if (cleanedBase64Str.isEmpty()) return null

            // 1. Obtener los bytes de la imagen
            val decodedBytes = Base64.decode(cleanedBase64Str, Base64.DEFAULT)

            // 2. Decodificar el Bitmap original sin rotar
            val originalBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            // 3. Leer la orientación EXIF de los bytes
            val inputStream = ByteArrayInputStream(decodedBytes)
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            // 4. Calcular el ángulo de rotación necesario
            val matrix = Matrix()
            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            // 5. Aplicar la rotación
            matrix.postRotate(rotationAngle)

            // 6. Crear y devolver el Bitmap corregido
            return Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )

        } catch (e: Exception) {
            Log.e("BASE64_ROTATE", "Error al decodificar y rotar: ${e.message}")
            return null
        }
    }


    /**
     * ViewHolder: Mantiene las referencias a las vistas de item_product.xml
     */
    inner class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductDetails: TextView = view.findViewById(R.id.tvProductDetails)
        val ivProductIcon: ImageView = view.findViewById(R.id.ivProductIcon)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            maximumFractionDigits = 0
        }

        fun bind(producto: Producto) {
            tvProductName.text = producto.nombre
            val precioFormateado = currencyFormatter.format(producto.precio)
            tvProductDetails.text = "Stock: ${producto.stock} | Precio: ${precioFormateado}"

            // LÓGICA DE VISUALIZACIÓN DE IMAGEN (BASE64)
            val base64Image = producto.imagen_url

            if (!base64Image.isNullOrEmpty()) {
                val bitmap = decodeBase64ToBitmap(base64Image) //  Llama a la función con ROTACIÓN
                if (bitmap != null) {
                    ivProductIcon.setImageBitmap(bitmap)
                    ivProductIcon.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    ivProductIcon.setImageResource(R.drawable.ic_menu)
                    ivProductIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            } else {
                ivProductIcon.setImageResource(R.drawable.ic_menu)
                ivProductIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }

            // LISTENERS DE ACCIÓN CLAVE
            itemView.setOnClickListener { listener.onEditClicked(producto.id) }
            btnDelete.setOnClickListener { listener.onDeleteClicked(producto.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size

    fun updateData(newProducts: List<Producto>) {
        productos = newProducts
        notifyDataSetChanged()
    }
}