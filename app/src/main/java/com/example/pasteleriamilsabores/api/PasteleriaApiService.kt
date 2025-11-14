package com.example.pasteleriamilsabores.api

import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.model.Producto
import retrofit2.http.*

// Clase de datos simple para manejar las respuestas JSON de éxito o error de PHP.
data class RespuestaApi(
    val status: String, // succes o error
    val message: String // descripcion del mensaje
)

interface PasteleriaApiService {

    // get de los productos
    // Obtiene una lista completa de Productos para el catálogo.
    @GET("obtener_producto.php")
    suspend fun getProductos(): List<Producto>



    // get de las categorias
    // Obtiene una lista de categorías para llenar el Spinner.
    @GET("obtener_categoria.php")
    suspend fun getCategorias(): List<Categoria>


    // =======================================================
    // 3. OPERACIÓN DE INSERCIÓN (POST) - PRODUCTOS
    // =======================================================
    // Envía todos los datos del formulario al servidor para guardar el producto.
    // Usamos @FormUrlEncoded y @Field para enviar los datos como un formulario estándar.
    @POST("guardar_producto.php")
    @FormUrlEncoded
    suspend fun postProducto(
        @Field("codigo_producto") codigo: String,
        @Field("nombre") nombre: String,
        @Field("descripcion") descripcion: String,
        @Field("precio") precio: Int,
        @Field("stock") stock: Int,
        @Field("stock_critico") stockCritico: Int,
        @Field("imagen_url") imagenUrl: String,
        @Field("categoria_id") categoriaId: Int
    ): RespuestaApi
}