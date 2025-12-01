package com.example.pasteleriamilsabores.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

import com.google.ai.client.generativeai.GenerativeModel

object DescriptionIARepository {

    // constantes de la api
    private const val GEMINI_API_TOKEN = "AIzaSyDAP-NcunC1l8Q9mCFQA1fTRJHKe7vOSrI"

    // función para generar la descripción del producto usando gemini
    suspend fun generateDescription(nombreProducto: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {

            // definición del prompt para el modelo
            val prompt = """
                eres un experto pastelero y redactor publicitario. 
                genera una descripción de producto corta, atractiva y profesional 
                para una pastelería. máximo 40 palabras.
                
                producto: "$nombreProducto"
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