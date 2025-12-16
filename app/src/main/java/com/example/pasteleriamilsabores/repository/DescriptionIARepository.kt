package com.example.pasteleriamilsabores.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

import com.google.ai.client.generativeai.GenerativeModel

object DescriptionIARepository {

    // constantes de la api
    private const val GEMINI_API_TOKEN = "AIzaSyCS1GMiVWTEzyMlfw3J1WSn7FHJ8EHBGCQ"

    // función para generar la descripción del producto usando gemini
    suspend fun generateDescription(nombreProducto: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {

            // definición del prompt para el modelo
            val prompt = """
                Actúa como un experto copywriter gastronómico.
                Crea una descripción publicitaria, corta y deliciosa para el producto: "$nombreProducto".
                
                Reglas OBLIGATORIAS:
                1. Máximo 40 palabras.
                2. Enfócate en el sabor y la textura.
                3. IMPORTANTE: Responde ÚNICAMENTE con el texto de la descripción. 
                   NO incluyas saludos, introducciones (como "Aquí tienes"), ni comillas.
            """.trimIndent()

            // inicializa el modelo y la api key
            val model = GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = GEMINI_API_TOKEN
            )

            Log.d("GEMINI_CALL", "llamando a gemini con prompt: $nombreProducto")

            val response = model.generateContent(prompt)

            // devuelve la respuesta, o lanza error si está vacía
            return@runCatching response.text ?: throw IOException("gemini devolvió una respuesta vacía.")

        }.onFailure { e ->
            Log.e("GEMINI_ERROR", "fallo al generar descripción con gemini: ${e.message}")
        }
    }
}