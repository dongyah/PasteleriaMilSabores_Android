package com.example.pasteleriamilsabores.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pasteleriamilsabores.model.Producto
import com.example.pasteleriamilsabores.model.Categoria

class PasteleriaDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    // 1. creación y actualización de la base de datos para sqlite
    // esta clase es solo para el CRUD con SQLITE
    override fun onCreate(db: SQLiteDatabase) {
        // crear tabla productos
        db.execSQL("""
            CREATE TABLE $TABLE_PRODUCTOS (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_CODIGO_PRODUCTO TEXT NOT NULL UNIQUE,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_DESCRIPCION TEXT,
                $COL_PRECIO INTEGER NOT NULL,
                $COL_STOCK INTEGER NOT NULL,
                $COL_STOCK_CRITICO INTEGER DEFAULT 5,
                $COL_IMAGEN_URL TEXT,
                $COL_CATEGORIA_ID INTEGER NOT NULL,
                $COL_ES_PENDIENTE INTEGER DEFAULT 0
            );
        """.trimIndent())

        // crear tabla categorias
        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIAS (
                $COL_ID_CAT INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE_CAT TEXT NOT NULL UNIQUE
            );
        """.trimIndent())

        // insertar datos iniciales, los datos de las categorias los insertaremos manualmente ya que no tenemos un form de categorias
        insertarCategoriasIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        // eliminar tablas antiguas si existen
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIAS")
        onCreate(db)
    }

    // 2. operaciones de productos (crud)

    // inserta un nuevo producto
    fun insertProducto(producto: Producto): Long {
        val cv = ContentValues().apply {
            put(COL_CODIGO_PRODUCTO, producto.codigo_producto)
            put(COL_NOMBRE, producto.nombre)
            put(COL_DESCRIPCION, producto.descripcion)
            put(COL_PRECIO, producto.precio)
            put(COL_STOCK, producto.stock)
            put(COL_STOCK_CRITICO, producto.stock_critico)
            put(COL_IMAGEN_URL, producto.imagen_url)
            put(COL_CATEGORIA_ID, producto.categoria_id)
            put(COL_ES_PENDIENTE, producto.es_pendiente)
        }
        return writableDatabase.insert(TABLE_PRODUCTOS, null, cv)
    }

    // obtiene todos los productos
    fun getAllProductos(): List<Producto> {
        val out = mutableListOf<Producto>()
        val sql = "SELECT * FROM $TABLE_PRODUCTOS ORDER BY $COL_ID DESC"
        val c: Cursor = readableDatabase.rawQuery(sql, null)
        c.use {
            while (it.moveToNext()) {
                out += cursorToProducto(it)
            }
        }
        return out
    }

    // obtiene un producto por id
    fun getProductoById(id: Int): Producto? {
        val sql = "SELECT * FROM $TABLE_PRODUCTOS WHERE $COL_ID = ?"
        val c: Cursor = readableDatabase.rawQuery(sql, arrayOf(id.toString()))
        c.use {
            return if (it.moveToFirst()) cursorToProducto(it) else null
        }
    }

    fun getProductosPendientes(): List<Producto> {
        val out = mutableListOf<Producto>()
        // Seleccionamos solo los que tienen es_pendiente = 1
        val sql = "SELECT * FROM $TABLE_PRODUCTOS WHERE $COL_ES_PENDIENTE = 1"
        val c: Cursor = readableDatabase.rawQuery(sql, null)
        c.use {
            while (it.moveToNext()) {
                out += cursorToProducto(it)
            }
        }
        return out
    }

    // actualiza un producto que ya existe
    fun updateProducto(producto: Producto): Int {
        val cv = ContentValues().apply {
            put(COL_CODIGO_PRODUCTO, producto.codigo_producto)
            put(COL_NOMBRE, producto.nombre)
            put(COL_DESCRIPCION, producto.descripcion)
            put(COL_PRECIO, producto.precio)
            put(COL_STOCK, producto.stock)
            put(COL_STOCK_CRITICO, producto.stock_critico)
            put(COL_IMAGEN_URL, producto.imagen_url)
            put(COL_CATEGORIA_ID, producto.categoria_id)
        }
        val whereClause = "$COL_ID = ?"
        val whereArgs = arrayOf(producto.id.toString())

        return writableDatabase.update(TABLE_PRODUCTOS, cv, whereClause, whereArgs)
    }

    // elimina un producto por id
    fun deleteProducto(id: Int): Int {
        val whereClause = "$COL_ID = ?"
        val whereArgs = arrayOf(id.toString())
        return writableDatabase.delete(TABLE_PRODUCTOS, whereClause, whereArgs)
    }

    // reemplaza todos los productos con la lista del servidor (para el caché)
// Reemplaza los productos del caché, PERO respeta los que están pendientes de subir
    fun replaceAllProductos(productos: List<Producto>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // CAMBIO CLAVE: Solo borramos los que NO son pendientes (es_pendiente = 0)
            // Así tu producto local sobrevive hasta que se sincronice.
            db.execSQL("DELETE FROM $TABLE_PRODUCTOS WHERE $COL_ES_PENDIENTE = 0")

            // Insertar los nuevos productos que llegaron del servidor
            productos.forEach { producto ->
                val cv = ContentValues().apply {
                    put(COL_ID, producto.id)
                    put(COL_CODIGO_PRODUCTO, producto.codigo_producto)
                    put(COL_NOMBRE, producto.nombre)
                    put(COL_DESCRIPCION, producto.descripcion)
                    put(COL_PRECIO, producto.precio)
                    put(COL_STOCK, producto.stock)
                    put(COL_STOCK_CRITICO, producto.stock_critico)
                    put(COL_IMAGEN_URL, producto.imagen_url)
                    put(COL_CATEGORIA_ID, producto.categoria_id)
                    put(COL_ES_PENDIENTE, 0) // Los que vienen del server ya están sincronizados
                }
                // Usamos CONFLICT_REPLACE para que si un ID coincide, se actualice
                db.insertWithOnConflict(TABLE_PRODUCTOS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // 3. operaciones de categorias

    // obtiene todas las categorias
    fun getAllCategorias(): List<Categoria> {
        val out = mutableListOf<Categoria>()
        val sql = "SELECT * FROM $TABLE_CATEGORIAS ORDER BY $COL_ID_CAT ASC"
        val c: Cursor = readableDatabase.rawQuery(sql, null)
        c.use {
            while (it.moveToNext()) {
                out += Categoria(
                    id = it.getInt(it.getColumnIndexOrThrow(COL_ID_CAT)),
                    nombre = it.getString(it.getColumnIndexOrThrow(COL_NOMBRE_CAT))
                )
            }
        }
        return out
    }

    // 4. lógica interna y datos iniciales

    // helper para crear objeto producto desde cursor
    private fun cursorToProducto(it: Cursor): Producto {
        return Producto(
            id = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
            codigo_producto = it.getString(it.getColumnIndexOrThrow(COL_CODIGO_PRODUCTO)),
            nombre = it.getString(it.getColumnIndexOrThrow(COL_NOMBRE)),
            descripcion = it.getString(it.getColumnIndexOrThrow(COL_DESCRIPCION)),
            precio = it.getInt(it.getColumnIndexOrThrow(COL_PRECIO)),
            stock = it.getInt(it.getColumnIndexOrThrow(COL_STOCK)),
            stock_critico = it.getInt(it.getColumnIndexOrThrow(COL_STOCK_CRITICO)),
            imagen_url = it.getString(it.getColumnIndexOrThrow(COL_IMAGEN_URL)),
            categoria_id = it.getInt(it.getColumnIndexOrThrow(COL_CATEGORIA_ID)),
            es_pendiente = if (it.getColumnIndex(COL_ES_PENDIENTE) != -1)
                it.getInt(it.getColumnIndexOrThrow(COL_ES_PENDIENTE))
            else 0
        )
    }

    // inserta las categorias iniciales al crear la db, las mismas que se usan en el servidor php/xampp
    private fun insertarCategoriasIniciales(db: SQLiteDatabase) {
        val categorias = listOf(
            Categoria(nombre = "Tortas Clásicas"),
            Categoria(nombre = "Postres Individuales"),
            Categoria(nombre = "Galletas"),
            Categoria(nombre = "Panadería")
        )
        categorias.forEach { categoria ->
            val cv = ContentValues().apply {
                put(COL_NOMBRE_CAT, categoria.nombre)
            }
            db.insert(TABLE_CATEGORIAS, null, cv)
        }
    }


    // 5. constantes
    companion object {
        private const val DB_NAME = "pasteleria_mil_sabores.db"
        private const val DB_VERSION = 2

        const val TABLE_PRODUCTOS = " productos"
        const val COL_ID = "id"
        const val COL_CODIGO_PRODUCTO = "codigo_producto"
        const val COL_NOMBRE = "nombre"
        const val COL_DESCRIPCION = "descripcion"
        const val COL_PRECIO = "precio"
        const val COL_STOCK = "stock"
        const val COL_STOCK_CRITICO = "stock_critico"
        const val COL_IMAGEN_URL = "imagen_url"
        const val COL_CATEGORIA_ID = "categoria_id"

        const val TABLE_CATEGORIAS = "categorias"
        const val COL_ID_CAT = "id"
        const val COL_NOMBRE_CAT = "nombre"

        const val COL_ES_PENDIENTE = "es_pendiente"

    }
}