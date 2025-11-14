package com.example.pasteleriamilsabores.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Categoria(
    val id: Int,
    val nombre: String
)
