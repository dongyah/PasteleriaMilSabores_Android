package com.example.pasteleriamilsabores.model

import com.squareup.moshi.JsonClass

data class Producto(
    val id: Int = 0,
    val codigo_producto: String,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val stock: Int,
    val stock_critico: Int,
    val imagen_url: String, // Base64
    val categoria_id: Int,

    //Para la última entrega, le añadiremos un nuevo campo para saber si está sincronizado o no
    val es_pendiente: Int = 0
)