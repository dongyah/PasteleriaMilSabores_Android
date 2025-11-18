package com.example.pasteleriamilsabores.repository

import com.example.pasteleriamilsabores.api.PasteleriaApiService
import com.example.pasteleriamilsabores.api.RespuestaApi
import com.example.pasteleriamilsabores.api.RetrofitClient
import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio central que maneja todas las operaciones de red (API).
 * Utiliza Corrutinas para mover las llamadas lentas a un hilo de fondo (Dispatchers.IO).
 */
object ProductosApiRepository {

    // Inicializa el servicio Retrofit
    private val apiService: PasteleriaApiService = RetrofitClient.apiService


    //LEER READ
    // Obtiene la lista de todos los productos (Catálogo)
    suspend fun getProductos(): Result<List<Producto>> =
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getProductos()
            }
        }

    // Obtiene la lista de categorías (Spinner)
    suspend fun getCategorias(): Result<List<Categoria>> =
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getCategorias()
            }
        }

    // Obtiene un solo producto por ID (para Edición)
    suspend fun getProductoById(id: Int): Result<Producto> =
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getProductoById(id)
            }
        }


    // CREATE
    // Inserta un producto nuevo en la BD
    suspend fun postProducto(
        codigo: String,
        nombre: String,
        descripcion: String,
        precio: Int,
        stock: Int,
        stockCritico: Int,
        imagenUrl: String,
        categoriaId: Int
    ): Result<RespuestaApi> =
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.postProducto(
                    codigo, nombre, descripcion, precio, stock, stockCritico, imagenUrl, categoriaId
                )
            }
        }


    //UPDATE
    // Modifica un producto existente, requiere el 'id'.
    suspend fun updateProducto(
        id: Int, // El ID es clave para la actualización
        codigo: String,
        nombre: String,
        descripcion: String,
        precio: Int,
        stock: Int,
        stockCritico: Int,
        imagenUrl: String,
        categoriaId: Int
    ): Result<RespuestaApi> =
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.updateProducto(
                    id, codigo, nombre, descripcion, precio, stock, stockCritico, imagenUrl, categoriaId
                )
            }
        }


    //DELETE
    // Elimina un producto por su ID.
    suspend fun deleteProducto(id: Int): Result<RespuestaApi> =
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteProducto(id)
            }
        }
}