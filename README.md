

# 🍰 Proyecto Final: Pastelería Mil Sabores - App de Gestión

Este proyecto es una aplicación móvil de gestión de inventario y catálogo, desarrollada en **Kotlin** para Android. Implementa el **CRUD completo** (Crear, Leer, Actualizar, Eliminar) utilizando una arquitectura limpia basada en **Corrutinas** y **Retrofit** para comunicarse con un servidor PHP/MySQL. Además, incluye la integración de **Inteligencia Artificial (IA) de Gemini** para enriquecer el contenido de los productos.

## 1\. ⚙️ Arquitectura y Tecnologías Clave

La aplicación sigue el patrón de **Capa de Repositorio** (Repository Pattern) para separar la UI de la lógica de datos, utilizando técnicas de programación asíncrona avanzada.

| Componente | Tecnología | Propósito |
| :--- | :--- | :--- |
| **Asincronía & Hilos** | **Corrutinas** (`lifecycleScope`, `Dispatchers.IO`) | Ejecución de tareas lentas (API, Base de Datos) sin bloquear el hilo principal (UI). |
| **Comunicación API** | **Retrofit 2** / **Moshi** | Cliente HTTP robusto para convertir las llamadas de Kotlin en solicitudes POST/GET a los scripts PHP. |
| **Backend** | **XAMPP (PHP & MySQL)** | Servidor local para alojar los *scripts* de la API y la base de datos `pasteleria_mil_sabores`. |
| **IA Generativa** | **Gemini (Vía PHP cURL)** | Generación de descripciones de productos y análisis simulado de imágenes. |
| **Imagen** | **CameraX** / **Base64** | Captura de fotos y codificación a formato de texto para envío y almacenamiento en la base de datos (`LONGTEXT`). |

## 2\. 🗂️ Estructura de Archivos y Responsabilidades (Kotlin)

El proyecto está organizado en paquetes siguiendo una arquitectura modular:

| Paquete | Archivos Principales | Responsabilidad |
| :--- | :--- | :--- |
| `ui` | `MainActivity2.kt` (Catálogo), `MainActivity3.kt` (Formulario), `CameraActivity.kt` | **Capa de Presentación:** Muestra la información y maneja eventos de usuario. `MainActivity3` contiene la lógica de Permisos y Launchers. |
| `repository` | `ProductosApiRepository.kt` | **Capa de Lógica de Datos:** Contiene la lógica `suspend fun` para las Corrutinas. Decide si llamar a una función CRUD o a una función de IA. |
| `api` | `PasteleriaApiService.kt`, `RetrofitClient.kt` | **Capa de Red:** Define el contrato de la API (`@GET`, `@POST`) y la configuración base de Retrofit. |
| `model` | `Producto.kt`, `Categoria.kt`, `Respuesta.kt` | **Modelos de Datos:** Clases `data class` para el parseo JSON (Moshi). |
| `adapter` | `ProductAdapter.kt` | **Visualización:** Maneja el `RecyclerView`. Contiene la lógica de **decodificación y rotación EXIF de la imagen Base64** para mostrar la foto. |
| `PCamara` | `CameraManager.kt`, `CamaraUtils.kt` | Lógica del profesor para controlar CameraX y codificar el `Bitmap` a Base64. |

-----

## 3\. 🖥️ Layouts y Funcionalidad del CRUD

| Layout (XML) | Uso en la Aplicación | Detalle de la Función |
| :--- | :--- | :--- |
| `activity_main2.xml` | **Catálogo/Gestión** | Muestra el listado con `RecyclerView`. Usa **`registerForActivityResult`** en Kotlin para esperar un resultado de `MainActivity3` y recargar la lista automáticamente. |
| `item_product.xml` | **Ítem del Catálogo** | Muestra la **Imagen Base64** decodificada, nombre, precio y stock. Contiene los botones de **Edición** y **Eliminación** que delegan las acciones a `MainActivity2`. |
| `activity_main3.xml` | **Formulario** | Contiene todos los `TextInputEditText` con *hints* de ejemplo y el `Spinner` de Categoría. Incluye botones dedicados para las funciones de IA. |
| `activity_camera.xml` | **Cámara/Galería** | Vista del visor de la cámara. Almacena `PreviewView` y botones para **Tomar Foto** o **Galería**. |

-----

## 4\. 🔗 Configuración del Servidor y Base de Datos

| Componente | Archivo(s) | Función Esencial |
| :--- | :--- | :--- |
| **Configuración XAMPP** | `my.ini` (Config), `php.ini` (Config) | **Ajustado el puerto MySQL a `3307`** para evitar el error de "shutdown unexpectedly". Límites de `post_max_size` aumentados a **50M** para aceptar la cadena Base64 de la imagen. |
| **Base de Datos** | `pasteleria_mil_sabores` (SQL Script) | Creada con las tablas `Productos` y `Categorias`. La columna `imagen_url` está configurada como **`LONGTEXT`** para almacenar el Base64. |
| **Conexión PHP** | Todos los 6 scripts | Todos los scripts usan la conexión **`new mysqli($host, $user, $pass, $db, 3307)`** para asegurar la comunicación con el puerto no estándar. |

### Scripts PHP para la API:

| Endpoint (Vía Kotlin) | Script PHP | Tarea en el Servidor |
| :--- | :--- | :--- |
| `getProductos()` | `obtener_producto.php` | SELECT \* FROM productos (Devuelve JSON Array). |
| `postProducto()` | `guardar_producto.php` | **INSERT INTO** productos. Contiene lógica para **limpiar** la cadena Base64. |
| `updateProducto()` | `actualizar_producto.php` | **UPDATE** producto WHERE id = $id. |
| `deleteProducto()` | `eliminar_producto.php` | **DELETE** FROM productos WHERE id = $id. |
| `generarDescripcion()` | `generar_ia.php` | **Llamada cURL a Gemini Pro** para generar texto. |

-----

## 5\. ⭐️ Funcionalidad de Inteligencia Artificial

Para cumplir con el requisito de IA de forma segura (evitando el error de dependencia en Kotlin), la lógica de Gemini se ejecuta en el *backend* de PHP.

| Función en Kotlin | Modelo Gemini | Proceso de Ejecución |
| :--- | :--- | :--- |
| `generarDescripcionIA()` | `gemini-pro` | Kotlin envía el nombre del producto $\rightarrow$ PHP llama a la API REST de Gemini $\rightarrow$ PHP devuelve la descripción generada a Kotlin para rellenar el `EditText`. |
| `mejorarImagenIA()` | `gemini-pro-vision` | Kotlin envía la Base64 $\rightarrow$ PHP simula el análisis de la imagen $\rightarrow$ PHP devuelve el resultado del análisis (en formato texto) al formulario. |

### Dependencias Clave de Terceros

```kotlin
// Build.gradle.kts (Module :app)

implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
implementation("com.google.ai.client.generativeai:generativeai:0.1.0") // SDK de Gemini
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
implementation("androidx.camera:camera-lifecycle:1.3.4")
implementation("androidx.exifinterface:exifinterface:1.3.6") // Para rotación de imagen
```
