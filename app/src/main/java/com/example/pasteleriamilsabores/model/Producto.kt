package com.example.pasteleriamilsabores.model

import com.squareup.moshi.JsonClass

// Clase de datos que representa un producto de la pastelería
@JsonClass(generateAdapter = true)
data class Producto(
    val id: Int,
    val codigo_producto: String,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val stock: Int,
    val stock_critico: Int,
    val imagen_url: String,
    val categoria_id: Int
)