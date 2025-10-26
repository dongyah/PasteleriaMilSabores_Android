package com.example.pasteleriamilsabores

// Clase de datos que representa un producto de la pastelería
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val description: String,
    val imageUrl: String? = null // URL de la foto del producto (después de pasar por IA)
)
