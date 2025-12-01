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

    // operaciones de escritura (create/update/delete - híbrido)

    // crea un nuevo producto. si php falla, la operación falla.
    suspend fun insertProducto(context: Context, producto: Producto): Result<Long> =
        withContext(Dispatchers.IO) {
            val dbHelper = PasteleriaDbHelper(context)

            // 1. intentar subir a php
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
                // 2. éxito en php: guarda también en sqlite
                dbHelper.use {
                    val rowId = it.insertProducto(producto)
                    Log.i("SQLITE_SAVE", "guardado exitoso en php.")
                    Result.success(rowId)
                }
            } else {
                // 3. fallo: si falla la red, devuelve error.
                Log.e("REPO_SYNC", "guardar falló en php: ${apiResult.exceptionOrNull()?.message}")
                throw IOException("no se pudo conectar al servidor php.")
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
                // 2. éxito en php: actualiza sqlite
                dbHelper.use {
                    it.updateProducto(producto)
                }
                Log.i("REPO_SYNC", "actualización exitosa en php.")
                Result.success(Unit)
            } else {
                // 3. fallo: si falla la red/api, lanza una excepción.
                Log.e("REPO_SYNC", "actualización falló en php: ${apiResult.exceptionOrNull()?.message}")
                throw IOException("no se pudo conectar al servidor php.")
            }
        }

    // elimina un producto. si php falla, la operación falla.
    suspend fun deleteProducto(context: Context, id: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            val dbHelper = PasteleriaDbHelper(context)

            // 1. intentar eliminar en php
            val apiResult = runCatching {
                apiService.deleteProducto(id)
            }

            return@withContext if (apiResult.isSuccess) {
                // 2. éxito en php: elimina de sqlite
                dbHelper.use {
                    it.deleteProducto(id)
                }
                Log.i("REPO_SYNC", "eliminación exitosa en php.")
                Result.success(Unit)
            } else {
                // 3. fallo: si falla la red/api, lanza una excepción.
                Log.e("REPO_SYNC", "eliminación falló en php: ${apiResult.exceptionOrNull()?.message}")
                throw IOException("no se pudo conectar al servidor php.")
            }
        }

    // funciones de ia gemini
    // llama a gemini para generar una descripción de producto.
    suspend fun generateDescription(nombreProducto: String): Result<String> =
        withContext(Dispatchers.IO) {
            DescriptionIARepository.generateDescription(nombreProducto)
        }
}