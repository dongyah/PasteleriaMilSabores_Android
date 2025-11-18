package com.example.pasteleriamilsabores.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pasteleriamilsabores.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Constantes para las credenciales de la pastelería
        val ADMIN_USER = "admin"
        val ADMIN_PASS = "123"

        // 1. Declaramos variables con los elementos layout
        val etUsername: EditText =  findViewById(R.id.etUsername)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)

        // Accion sobre el boton
        btnLogin.setOnClickListener{
            val enteredUser = etUsername.text.toString()
            val enteredPass = etPassword.text.toString()

            if(enteredUser == ADMIN_USER && enteredPass == ADMIN_PASS){

                // AUTENTICACIÓN EXITOSA

                // Crea el Intent para navegar a MainActivity2 (Dashboard)
                val dashboardIntent = Intent(this, MainActivity2::class.java)

                startActivity(dashboardIntent)

                // mensajde de bienvenida
                Toast.makeText(this, "Bienvenido, Administrador.", Toast.LENGTH_SHORT).show()

                // Cierra la Activity de Login para evitar volver atrás
                finish()


            }else {

                // AUTENTICACIÓN FALLIDA
                Toast.makeText(this, "Error: usuario o contraseña incorrectos.", Toast.LENGTH_LONG)
                    .show()

                // Limpiar solo el campo de contraseña
                etPassword.text
            }
        }
    }
}