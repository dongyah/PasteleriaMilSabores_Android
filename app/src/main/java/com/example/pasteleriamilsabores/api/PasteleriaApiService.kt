package com.example.pasteleriamilsabores.api

import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.model.Producto
import retrofit2.http.*

data class RespuestaApi(
    val status: String,
    val message: String
)

interface PasteleriaApiService {

    // 1. LEER TODO (Catálogo)
    @GET("obtener_producto.php")
    suspend fun getProductos(): List<Producto>

    // 2. LEER CATEGORÍAS (Spinner)
    @GET("obtener_categoria.php")
    suspend fun getCategorias(): List<Categoria>

    // 3. LEER POR ID (EDITAR)
    @GET("obtener_producto_por_id.php")
    suspend fun getProductoById(@Query("id") id: Int): Producto

    // 4. CREAR (POST)
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

    // 5. ACTUALIZAR (PUT)
    @PUT("actualizar_producto.php")
    @FormUrlEncoded
    suspend fun updateProducto(
        @Field("id") id: Int,
        @Field("codigo_producto") codigo: String,
        @Field("nombre") nombre: String,
        @Field("descripcion") descripcion: String,
        @Field("precio") precio: Int,
        @Field("stock") stock: Int,
        @Field("stock_critico") stockCritico: Int,
        @Field("imagen_url") imagenUrl: String,
        @Field("categoria_id") categoriaId: Int
    ): RespuestaApi

    // 6. ELIMINAR (DELETE):  CON EL METODO @DELETE NOS DABA ERROR DE RED,
    // @DELETE ESTÁ DISEÑADO PARA ELIMINAR UN RECURSO POR LA UTL EJEM /PRODUCTOS/123
    // CON @POST SE GARANTIZA QUE EL ID SE MANDE DE MANERA FIABLE EN EL CUERPO DE LA SOLICITUD Y EL PHP DE ELIMINAR
    // LO LEE CORRECTAMENTE USANDO parse_str(file_get_contents("php://input"), $DELETE)
    @POST("eliminar_producto.php")
    @FormUrlEncoded
    suspend fun deleteProducto(
        @Field("id") id: Int
    ): RespuestaApi
}