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
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException


class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance()


        // credenciales de la pastelería
        val ADMIN_USER = "admin"
        val ADMIN_PASS = "123"


        // 1. Declaramos variables con los elementos layout
        val etUsername: EditText = findViewById(R.id.etUsername)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val btnGoogleLogin: Button = findViewById(R.id.btnGoogleLogin)


        // Acción para el botón de login (usuario y contraseña)
        btnLogin.setOnClickListener {
            val enteredUser = etUsername.text.toString()
            val enteredPass = etPassword.text.toString()


            if (enteredUser == ADMIN_USER && enteredPass == ADMIN_PASS) {
                // AUTENTICACIÓN EXITOSA
                val dashboardIntent = Intent(this, MainActivity2::class.java)
                startActivity(dashboardIntent)
                Toast.makeText(this, "Bienvenido, Administrador.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // AUTENTICACIÓN FALLIDA
                Toast.makeText(this, "Error: usuario o contraseña incorrectos.", Toast.LENGTH_LONG)
                    .show()
                etPassword.text
            }
        }


        // Acción para el botón de Google Login
        btnGoogleLogin.setOnClickListener {
            lifecycleScope.launch {
                signInWithGoogle()
            }
        }
    }


    // Función para manejar el login con Google
    private suspend fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(this)


        // Este string normalmente lo genera google-services desde google-services.json
        val webClientId = getString(R.string.default_web_client_id)


        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()


        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()


        try {
            val result = credentialManager.getCredential(
                request = request,
                context = this
            )


            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(result.credential.data)


            val idToken = googleIdTokenCredential.idToken


            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()


            // ÉXITO → misma navegación que tu login admin
            startActivity(Intent(this, MainActivity2::class.java))
            Toast.makeText(this, "Sesión iniciada con Google", Toast.LENGTH_SHORT).show()
            finish()


        } catch (e: GetCredentialException) {
            Toast.makeText(this, "Google Sign-In cancelado o falló: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error autenticando en Firebase: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
