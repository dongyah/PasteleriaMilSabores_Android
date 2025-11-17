🍰 Pastelería Mil Sabores: Aplicación de Gestión Móvil

Este proyecto es una aplicación de gestión de inventario para una pastelería, desarrollada en Kotlin para Android. Implementa un sistema CRUD completo (Crear, Leer, Actualizar, Eliminar) utilizando una arquitectura moderna basada en Corrutinas y Retrofit para comunicarse con un Backend simulado (XAMPP/PHP).

Además, integra funcionalidades avanzadas de Cámara y Galería (con codificación Base64) y simula el uso de Inteligencia Artificial (Gemini AI) para generar contenido.

🚀 1. Arquitectura y Tecnologías Clave

El proyecto está diseñado siguiendo una arquitectura de capas, donde la lógica de la Interfaz de Usuario (UI) está completamente separada de la lógica de datos y red.

Componente

Tecnología

Propósito

Frontend (App)

Kotlin / Android Studio

UI y manejo del flujo de la aplicación.

Backend (Servidor)

XAMPP (Apache, MySQL: Puerto 3307)

Base de datos y alojamiento de Web Services (PHP).

Comunicación API

Retrofit 2 / Moshi

Cliente HTTP robusto para la comunicación REST.

Asincronía

Corrutinas (lifecycleScope, Dispatchers.IO)

Gestión eficiente de todas las operaciones lentas (red y lectura de Base64).

Cámara/Imagen

CameraX / Base64 / EXIF

Captura de fotos y envío de la imagen binaria como cadena de texto.

IA

Gemini (Vía PHP cURL)

Generación de descripciones y análisis de imágenes.

2. ⚙️ Configuración del Entorno y Dependencias

Para compilar y ejecutar el proyecto, se requiere:

2.1 Dependencias Clave (build.gradle.kts)

El proyecto utiliza las siguientes librerías externas:

dependencies {
    // Corrutinas y Ciclo de Vida
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    
    // Retrofit y JSON (Moshi)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1") 
    
    // Conexión y Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Cámara (CameraX) y Manejo de Imagen
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("androidx.exifinterface:exifinterface:1.3.6") // Para rotación EXIF
    
    // Inteligencia Artificial (Google AI SDK) - Requerido para modelos
    implementation("com.google.ai.client.generativeai:generativeai:0.1.0")
}


2.2 Configuración del Backend (XAMPP)

Directorio: Todos los scripts PHP deben estar en C:\xampp\htdocs\pasteleria.

MySQL Puerto: MySQL debe iniciarse en el puerto 3307 (my.ini modificado).

Archivos de Datos: La base de datos pasteleria_mil_sabores debe existir y estar poblada.

PHP Configuración (php.ini): Se debe aumentar el límite de datos para aceptar la cadena Base64:

post_max_size = 50M
upload_max_filesize = 50M


Conexión PHP: Todos los scripts PHP (mysqli) deben especificar el puerto:

$port = 3306;
$conexion = new mysqli($host, $user, $pass, $db, $port);


3. 📂 Archivos y Componentes Clave (Kotlin)

El proyecto se divide en las siguientes carpetas y archivos, siguiendo el patrón enseñado:

a) Capa de Modelo (model/)

Archivo

Propósito

Producto.kt

data class principal (ID: Int, Precio: Int, Stock: Int, Imagen_url: String/Base64).

Categoria.kt

data class utilizada para cargar el Spinner.

b) Capa de Red (api/)

Archivo

Propósito

RetrofitClient.kt

Configuración del cliente HTTP y OkHttpClient con timeouts extendidos para la IA.

PasteleriaApiService.kt

Contrato de API. Define las 6 operaciones de la BD (@GET, @POST, @PUT, @DELETE) y las 2 llamadas a la IA (vía PHP).

c) Capa de Repositorio (repository/)

Archivo

Propósito

ProductosApiRepository.kt

Contiene el CRUD completo y las llamadas de IA. Implementa withContext(Dispatchers.IO) y Result<T> para el manejo asíncrono y seguro de los datos.

d) Capa de Presentación (ui/)

Archivo

Propósito

MainActivity2.kt

Catálogo/Dashboard. Carga la lista con Corrutinas (GET) y usa registerForActivityResult para recargar la lista automáticamente después de una acción de guardado o eliminación. Implementa la interfaz de Edición/Eliminación.

MainActivity3.kt

Formulario de CRUD. Maneja la lógica dual de Creación y Edición. Contiene la lógica de permisos, el cameraLauncher, y las funciones de IA y rotación EXIF.

CameraActivity.kt

Actividad que aloja el feed de CameraX, toma la foto, la codifica a Base64 y devuelve el resultado a MainActivity3.

e) Adaptador y Utilidades

Archivo

Propósito

ProductAdapter.kt

Adaptador del RecyclerView. Contiene la lógica para decodificar el Base64 a un Bitmap (con corrección de rotación) y manejar los clics de Edición/Eliminación delegándolos a MainActivity2.

PCamara/CameraManager.kt

Lógica de CameraX para iniciar la vista previa y capturar la imagen.

PCamara/CamaraUtils.kt

Lógica para la conversión final de Bitmap $\rightarrow$ Base64 para envío a la API.

4. 🌐 Flujo de Datos y Lógica (IA y Foto)

4.1 Captura de Imagen (Base64)

MainActivity3.onImagePickerClicked lanza CameraActivity usando un Launcher.

CameraActivity toma la foto, la rota (corrigiendo el problema de la imagen de lado), y usa CamaraUtils.convertirDeBitMapABase64 para crear una cadena de texto.

La cadena Base64 se devuelve al cameraLauncher de MainActivity3 y se guarda en la variable base64Image.

La función decodeBase64ToBitmap en MainActivity3 decodifica esa cadena para mostrar la vista previa.

Al presionar Guardar, base64Image se envía en el campo imagen_url al script PHP.
