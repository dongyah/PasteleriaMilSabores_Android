¡Entendido\! Eliminaremos **todas** las referencias a la Inteligencia Artificial (IA) y Gemini, dejando el proyecto enfocado únicamente en la funcionalidad **CRUD, CameraX, Base64, y la arquitectura Corrutinas/Retrofit**.

También ajustaré la configuración de XAMPP para usar el puerto **3306** como valor por defecto, asumiendo que el error se resolvió.

Aquí tienes el archivo **README.md** final y limpio, listo para GitHub:

-----

# 🍰 Proyecto Final: Pastelería Mil Sabores - App de Gestión

Este proyecto es una aplicación móvil de gestión de inventario y catálogo, desarrollada en **Kotlin** para Android. Implementa el **CRUD completo** (Crear, Leer, Actualizar, Eliminar) utilizando una arquitectura limpia basada en **Corrutinas** y **Retrofit** para comunicarse con un servidor **PHP/MySQL** local.

## 1\. ⚙️ Setup del Entorno Local (XAMPP Requerido)

Para ejecutar y probar la aplicación de forma local, es necesario configurar un servidor web y la base de datos.

### A. Requisitos

  * **XAMPP:** Instalado y con los módulos **Apache** y **MySQL** corriendo.

### B. Configuración del Servidor PHP y Rutas

1.  **Directorio de la API:** Copia todos los archivos **`.php`** (los scripts CRUD y GET) en la siguiente ruta de tu instalación de XAMPP:

    $$C:\xampp\htdocs\pasteleria$$

2.  **Configuración de Conexión:** Todos los scripts PHP están configurados para conectarse a MySQL en el puerto **`3306`** (puerto estándar). Si este puerto está ocupado, debe ajustarse a **`3307`** en los *scripts* PHP y en el archivo `my.ini`.

3.  **URL de Acceso:** La aplicación Kotlin se conecta a: `http://10.0.2.2/pasteleria/`

### C. Configuración de la Base de Datos

  * **Acción:** Utiliza el *script* **SQL completo** del proyecto (que contiene `DROP` y `CREATE DATABASE`) para crear la estructura de la base de datos (`pasteleria_mil_sabores`) y poblar las tablas (`Productos`, `Categorias`) en **phpMyAdmin**.

-----

## 2\. 💻 Arquitectura y Tecnologías Clave

La aplicación utiliza un patrón de **Capa de Repositorio** para separar la lógica de la UI y utiliza técnicas de programación asíncrona:

| Componente | Tecnología | Propósito |
| :--- | :--- | :--- |
| **Asincronía & Hilos** | **Corrutinas** (`lifecycleScope`, `Dispatchers.IO`) | Ejecuta tareas lentas de red sin bloquear la interfaz de usuario, garantizando fluidez. |
| **Comunicación API** | **Retrofit 2** / **Moshi** | Cliente HTTP que traduce las llamadas de Kotlin (`suspend fun`) a solicitudes **POST/GET/PUT/DELETE** HTTP. |
| **Backend** | **PHP & MySQL** | *Web Services* que actúan como intermediarios entre Kotlin y la base de datos SQL. |
| **Imagen** | **CameraX** / **Base64** | Captura la imagen, la codifica a Base64 para el envío por red, y la almacena en la columna **`LONGTEXT`** de la BD. |

-----

## 3\. 🗂️ Estructura del Código y Componentes

| Paquete | Archivos Principales | Responsabilidad |
| :--- | :--- | :--- |
| `ui` | `MainActivity2.kt` (Catálogo), `MainActivity3.kt` (Formulario), `CameraActivity.kt` | **Capa de Presentación:** Muestra la interfaz y maneja todos los *eventos de usuario* y la navegación. |
| `repository` | `ProductosApiRepository.kt` | **Capa de Lógica de Datos:** Contiene el **CRUD** envuelto en Corrutinas (`Result<T>`) para la seguridad y control del flujo de datos. |
| `api` | `PasteleriaApiService.kt`, `RetrofitClient.kt` | **Capa de Red:** Define el contrato de la API (`@GET`, `@POST`, etc.) y la configuración base de la conexión. |
| `adapter` | `ProductAdapter.kt` | **Visualización:** Maneja el `RecyclerView`. Contiene la lógica de **decodificación y rotación EXIF** del Base64 para mostrar las imágenes. |
| `PCamara` | `CameraManager.kt`, `CamaraUtils.kt` | Lógica para controlar la cámara y realizar la conversión de **`Bitmap` a Base64**. |

-----

## 4\. 🎯 Flujo de Datos y Metodología

### A. Metodología CRUD

  * **Creación/Edición:** La lógica de guardar utiliza una sola función (`guardarOActualizarProducto()`) que determina si debe ejecutar un **POST** (Crear) o un **PUT** (Actualizar) basándose en la existencia de un `PRODUCT_ID`.
  * **Refresco de Catálogo:** Se utiliza **`registerForActivityResult`** en `MainActivity2` para detectar cuando el formulario (`MainActivity3`) regresa con un resultado `RESULT_OK`, forzando la recarga de datos (`cargarProductosDesdeApi()`) y asegurando que la lista siempre esté actualizada.

### B. Flujo de Imagen

1.  **Captura:** `CameraActivity` captura la imagen.
2.  **Codificación:** La imagen se convierte a una cadena **Base64**.
3.  **Envío:** `MainActivity3` envía la cadena Base64 en el campo `imagen_url` por la red.
4.  **Decodificación:** El `ProductAdapter` lee la cadena, la **limpia** de caracteres extra, y la decodifica a **`Bitmap`** en memoria, aplicando una corrección de rotación **EXIF** para que la imagen se muestre en la orientación correcta.

-----

### Dependencias Clave de Terceros

```kotlin
// Build.gradle.kts (Module :app)

implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
implementation("androidx.camera:camera-lifecycle:1.3.4")
implementation("androidx.exifinterface:exifinterface:1.3.6")
```
