package com.example.pasteleriamilsabores.repository

import android.content.Context
import android.util.Log
import com.example.pasteleriamilsabores.database.PasteleriaDbHelper
import com.example.pasteleriamilsabores.model.Producto
import com.example.pasteleriamilsabores.model.Categoria
import com.example.pasteleriamilsabores.api.RetrofitClient
import com.example.pasteleriamilsabores.api.PasteleriaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// este archivo es el repositorio. decide si usar php (remoto) o sqlite (local).
object ProductosApiRepository {

    private val apiService: PasteleriaApiService = RetrofitClient.apiService

    // operaciones de lectura (read - híbrido)
    // intenta pedir datos a php. si funciona, guarda la copia en el teléfono emulador (sqlite).
    // si falla (sin internet), lee la copia guardada en el teléfono.
    suspend fun getAllProductos(context: Context): Result<List<Producto>> =
        withContext(Dispatchers.IO) {
            val dbHelper = PasteleriaDbHelper(context)

            // 1. intentar pedir a php
            val apiResult = runCatching {
                apiService.getProductos()
            }

            return@withContext if (apiResult.isSuccess) {
                val remoteProductos = apiResult.getOrThrow()

                // 2. éxito: guarda la lista nueva en sqlite (la caché)
                dbHelper.use {
                    it.replaceAllProductos(remoteProductos)
                }

                Log.i("REPO_SYNC", "datos cargados de php.")
                Result.success(remoteProductos)

            } else {
                // 3. fallo: no hay red, lee desde sqlite
                Log.e("REPO_SYNC", "fallo de red, cargando desde el teléfono.")
                dbHelper.use {
                    Result.success(it.getAllProductos())
                }
            }
        }

    // hace un get de las categorías (sqlite)
    suspend fun getAllCategorias(context: Context): Result<List<Categoria>> =
        withContext(Dispatchers.IO) {
            runCatching {
                PasteleriaDbHelper(context).use { it.getAllCategorias() }
            }
        }

    // hace get de un solo producto por id (sqlite)
    suspend fun getProductoById(context: Context, id: Int): Result<Producto?> =
        withContext(Dispatchers.IO) {
            runCatching {
                PasteleriaDbHelper(context).use { it.getProductoById(id) }
            }
        }


    suspend fun insertProducto(context: Context, producto: Producto): Result<Long> =
        withContext(Dispatchers.IO) {
            val dbHelper = PasteleriaDbHelper(context)

            // 1. Intentar subir a PHP
            val apiResult = runCatching {
                apiService.postProducto(
                    codigo = producto.codigo_producto,
                    nombre = producto.nombre,
                    descripcion = producto.descripcion,
                    precio = producto.precio,
                    stock = producto.stock,
                    stockCritico = producto.stock_critico,
                    imagenUrl = producto.imagen_url,
                    categoriaId = producto.categoria_id
                )
            }

            return@withContext if (apiResult.isSuccess) {
                val respuesta = apiResult.getOrThrow()

                if (respuesta.status == "success") {

                    try {
                        dbHelper.use {
                            val productoSincronizado = producto.copy(es_pendiente = 0)
                            val rowId = it.insertProducto(productoSincronizado)
                            Log.i("SQLITE_SAVE", "Guardado exitoso en PHP y replicado en SQLite.")
                            Result.success(rowId)
                        }
                    } catch (e: Exception) {
                        Log.e("SQLITE_ERROR", "Se guardó en PHP pero falló en SQLite: ${e.message}")
                        Result.success(1L) // Retornamos éxito porque en el servidor sí quedó
                    }
                } else {
                    Log.w("REPO_SYNC", "API retornó error: ${respuesta.message}. Guardando offline.")
                    guardarComoPendiente(dbHelper, producto)
                }
            } else {
                Log.w("REPO_SYNC", "Fallo de conexión. Guardando localmente para sincronizar después.")
                guardarComoPendiente(dbHelper, producto)
            }
        }

    // Función auxiliar para no repetir código de guardado offline
    private fun guardarComoPendiente(dbHelper: PasteleriaDbHelper, producto: Producto): Result<Long> {
        return try {
            dbHelper.use {
                // IMPORTANTE: Aquí marcamos el producto como pendiente (1)
                val productoPendiente = producto.copy(es_pendiente = 1)
                val rowId = it.insertProducto(productoPendiente)

                if (rowId > 0) {
                    Result.success(rowId)
                } else {
                    // Si rowId es -1, suele ser porque el código del producto ya existe (UNIQUE constraint)
                    Result.failure(IOException("No se pudo guardar offline. ¿El código del producto ya existe?"))
                }
            }
        } catch (e: Exception) {
            Log.e("SQLITE_SAVE", "Fallo crítico al guardar offline: ${e.message}")
            Result.failure(e)
        }
    }

    // actualiza un producto existente. si php falla, la operación falla.
    suspend fun updateProducto(context: Context, producto: Producto): Result<Unit> =
        withContext(Dispatchers.IO) {
            val dbHelper = PasteleriaDbHelper(context)

            // 1. intentar actualizar en php
            val apiResult = runCatching {
                apiService.updateProducto(
                    id = producto.id,
                    codigo = producto.codigo_producto,
                    nombre = producto.nombre,
                    descripcion = producto.descripcion,
                    precio = producto.precio,
                    stock = producto.stock,
                    stockCritico = producto.stock_critico,
                    imagenUrl = producto.imagen_url,
                    categoriaId = producto.categoria_id
                )
            }

            return@withContext if (apiResult.isSuccess) {
                // 2. éxito en php: actualiza en sqlite
                dbHelper.use {
                    it.updateProducto(producto)
                }
                Log.i("REPO_SYNC", "update exitoso en php y sqlite.")
                Result.success(Unit)
            } else {
                val localRows = dbHelper.use { it.updateProducto(producto) }

                if (localRows > 0) {
                    Log.w("REPO_SYNC", "actualización guardada OFFLINE. requiere sincronización.")
                    Result.success(Unit)
                } else {
                    // Si no se encontró la fila localmente.
                    throw IOException("fallo al guardar offline. producto no encontrado.")
                }
            }
        }

    // Sincronización: Sube los productos pendientes (es_pendiente = 1) al servidor
    suspend fun sincronizarProductos(context: Context) {
        withContext(Dispatchers.IO) {
            val dbHelper = PasteleriaDbHelper(context)

            // 1. Buscar qué productos están pendientes
            // (Asegúrate de haber creado esta función en PasteleriaDbHelper como te indiqué antes)
            val pendientes = try {
                dbHelper.use { it.getProductosPendientes() }
            } catch (e: Exception) {
                emptyList()
            }

            if (pendientes.isEmpty()) {
                Log.i("SYNC", "Nada pendiente para subir.")
                return@withContext
            }

            Log.i("SYNC", "Iniciando sincronización de ${pendientes.size} productos...")

            var cambiosRealizados = false

            // 2. Intentar subir cada uno
            pendientes.forEach { producto ->
                val result = runCatching {
                    apiService.postProducto(
                        codigo = producto.codigo_producto,
                        nombre = producto.nombre,
                        descripcion = producto.descripcion,
                        precio = producto.precio,
                        stock = producto.stock,
                        stockCritico = producto.stock_critico,
                        imagenUrl = producto.imagen_url,
                        categoriaId = producto.categoria_id
                    )
                }

                if (result.isSuccess) {
                    val respuesta = result.getOrThrow()
                    if (respuesta.status == "success") {
                        // ÉXITO: Marcar como sincronizado en SQLite (es_pendiente = 0)
                        try {
                            dbHelper.use {
                                // Importante: actualizamos usando el ID local para encontrarlo
                                val actualizado = producto.copy(es_pendiente = 0)
                                it.updateProducto(actualizado)
                            }
                            Log.i("SYNC", "Producto '${producto.nombre}' sincronizado correctamente.")
                            cambiosRealizados = true
                        } catch (e: Exception) {
                            Log.e("SYNC", "Error al actualizar estado local: ${e.message}")
                        }
                    }
                } else {
                    Log.e("SYNC", "Fallo al subir '${producto.nombre}'. Se intentará en la próxima conexión.")
                }
            }

            // 3. Si hubo cambios, forzar una recarga completa desde el servidor para tener los IDs reales
            if (cambiosRealizados) {
                Log.i("SYNC", "Sincronización completada. Recargando datos...")
                getAllProductos(context)
            }
        }
    }

    // elimina un producto. si php falla, cambia a sqlite
    suspend fun deleteProducto(context: Context, id: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            val dbHelper = PasteleriaDbHelper(context)

            val apiResult = runCatching { apiService.deleteProducto(id) }

            return@withContext if (apiResult.isSuccess) {
                // Éxito en PHP: elimina de SQLite.
                dbHelper.use {
                    it.deleteProducto(id)
                }
                Log.i("REPO_SYNC", "eliminación exitosa en php.")
                Result.success(Unit)
            } else {
                // FALLBACK OFFLINE. Eliminar SÓLO de SQLite.
                dbHelper.use { it.deleteProducto(id) }
                Log.w("REPO_SYNC", "eliminación guardada OFFLINE.")
                Result.success(Unit)
            }
        }

    // funciones de ia gemini
    // llama a gemini para generar una descripción de producto.
    suspend fun generateDescription(nombreProducto: String): Result<String> =
        withContext(Dispatchers.IO) {
            // Asumiendo que ahora usamos DescriptionIARepository
            DescriptionIARepository.generateDescription(nombreProducto)
        }
}