package com.example.pasteleriamilsabores.repository

import com.example.pasteleriamilsabores.api.PasteleriaApiService
import com.example.pasteleriamilsabores.api.RespuestaApi
import com.example.pasteleriamilsabores.api.RetrofitClient
import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Objeto singleton para manejar la lógica de datos y la llamada a la API
object ProductosApiRepository {

    // Inicializa el servicio de la API de forma perezosa (lazy)
    private val apiService: PasteleriaApiService = RetrofitClient.apiService

    // =======================================================
    // 1. OBTENER PRODUCTOS
    // =======================================================
    /**
     * Llama a la API para obtener la lista de productos.
     * Envuelve la llamada en Result para manejar errores de red/servidor.
     */
    suspend fun getProductos(): Result<List<Producto>> =
        withContext(Dispatchers.IO) { // Ejecuta en un hilo de fondo
            runCatching { // Captura cualquier excepción (ej. IOException)
                apiService.getProductos()
            }
        }

    // =======================================================
    // 2. OBTENER CATEGORÍAS
    // =======================================================
    /**
     * Llama a la API para obtener la lista de categorías.
     * Envuelve la llamada en Result para manejar errores.
     */
    suspend fun getCategorias(): Result<List<Categoria>> =
        withContext(Dispatchers.IO) { // Ejecuta en un hilo de fondo
            runCatching { // Captura cualquier excepción
                apiService.getCategorias()
            }
        }


    // =======================================================
    // 3. GUARDAR PRODUCTO (POST)
    // =======================================================
    /**
     * Envía los datos de un nuevo producto a la API.
     * Envuelve la llamada en Result para manejar errores.
     */
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
        withContext(Dispatchers.IO) { // Ejecuta en un hilo de fondo
            runCatching { // Captura cualquier excepción
                apiService.postProducto(
                    codigo,
                    nombre,
                    descripcion,
                    precio,
                    stock,
                    stockCritico,
                    imagenUrl,
                    categoriaId
                )
            }
        }
}