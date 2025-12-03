# 🍰 Pastelería Mil Sabores - Gestión de Productos

## 🔖 Rama: `entrega-3`

Aplicación móvil desarrollada en **Kotlin (Android)** para la gestión completa del inventario de productos de una pastelería. Esta versión integra una arquitectura híbrida con persistencia local (SQLite) y sincronización con un servidor remoto (MySQL/PHP), e incorpora funcionalidades de Inteligencia Artificial para la automatización de contenido.

-----

## ✨ Características Principales

### 1\. Persistencia Híbrida y Offline-First

  * **Sincronización Inteligente:** Utiliza una arquitectura **Offline-First**, permitiendo a la aplicación operar sin conexión.
  * **Base de Datos Local (SQLite):** Almacenamiento rápido y fiable de todos los productos y categorías en el dispositivo.
  * **Base de Datos Remota (MySQL/PHP):** Sincronización de datos mediante una API REST desarrollada en PHP (XAMPP).
  * **Manejo de Conflictos:** El repositorio de datos intenta primero la conexión con PHP, pero si falla (por red o servidor), realiza la operación CRUD **localmente en SQLite** para garantizar la continuidad operativa.

### 2\. Funcionalidades CRUD Completas

  * **Create (Crear):** Registro de nuevos productos con código, nombre, precio, stock y stock crítico.
  * **Read (Leer):** Visualización del catálogo de productos.
  * **Update (Actualizar):** Edición de datos de productos existentes.
  * **Delete (Eliminar):** Eliminación de productos, con sincronización de la baja en la base de datos remota.

### 3\. Integración de Inteligencia Artificial (Novedad)

Esta entrega introduce la automatización de la carga de productos mediante la integración con APIs de IA:

  * **Generación de Descripciones (Gemini):** Usa el modelo **Gemini / GenerativeModel** para crear automáticamente descripciones atractivas y detalladas del producto, utilizando el nombre y la categoría como *prompt* (instrucción) de entrada.

-----

## 💻 Configuración del Entorno

### A. Back-end (XAMPP / MySQL)

Para el correcto funcionamiento, es necesario configurar la base de datos remota:

1.  **Instalar y Configurar XAMPP:** Instalar XAMPP y asegurar que los módulos **Apache** y **MySQL** estén activos.
      * **NOTA DE CONFLICTO:** Si el puerto 80 u 8080 está ocupado, Apache debe configurarse para usar un puerto libre (ej: **8081**) en el archivo `httpd.conf`.
2.  **Crear Base de Datos:** Acceder a PHPMyAdmin y crear la base de datos `pasteleria_mil_sabores`.
3.  **Ejecutar Script SQL:** Importar la estructura de las tablas `categorias` y `productos`.

-- 1. ELIMINAR LA BASE DE DATOS EXISTENTE (DROP)
-- Elimina la base de datos si ya existe, lo cual es útil al recrear el entorno.
DROP DATABASE IF EXISTS pasteleria_mil_sabores;

-- 2. CREAR LA BASE DE DATOS
CREATE DATABASE pasteleria_mil_sabores
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

-- Seleccionar la base de datos para usarla
USE pasteleria_mil_sabores;

-- 3. CREAR LA TABLA CATEGORIAS
-- Usada para clasificar los productos (Tortas, Galletas, etc.).
CREATE TABLE Categorias (
    id INT(11) NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

-- 4. CREAR LA TABLA PRODUCTOS
CREATE TABLE Productos (
    id INT(11) NOT NULL AUTO_INCREMENT,
    codigo_producto VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion LONGTEXT,
    precio INT NOT NULL, 
    stock INT(11) NOT NULL,
    stock_critico INT(11) DEFAULT 5,
    imagen_url LONGTEXT, -- Almacena la cadena Base64 o URL
    categoria_id INT(11) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (categoria_id) REFERENCES Categorias(id) ON DELETE CASCADE
);

-- 5. POBLAR LA TABLA CATEGORIAS
INSERT INTO Categorias (nombre) VALUES
('Tortas'),
('Cupcakes'),
('Galletas'),
('Postres Frios'),
('Panadería');

-- 7. POBLAR LA TABLA PRODUCTOS (Datos de Prueba)
INSERT INTO Productos (codigo_producto, nombre, descripcion, precio, stock, stock_critico, imagen_url, categoria_id) VALUES
('TORTA-001', 'Torta de Chocolate Mil Sabores', 'Una deliciosa torta con tres capas de chocolate, rellena de ganache y cubierta con virutas de cacao.', 18990, 15, 5, 'torta_chocolate.jpg', 1),
('POSTRE-002', 'Pie de Limón Clásico', 'Base crujiente, relleno de crema de limón ácida y suave merengue tostado.', 12500, 22, 10, 'pie_limon.jpg', 4),
('GALLETA-003', 'Galletas de Avena y Pasas (Pack x6)', 'Galletas suaves y masticables hechas con avena integral y pasas.', 4500, 50, 10, 'galletas_avena.jpg', 3),
('PAN-004', 'Pan Amasado (Unidad)', 'Pan tradicional chileno, perfecto para la once, crujiente por fuera y suave por dentro.', 1350, 30, 20, 'pan_amasado.jpg', 5),
('POSTRE-005', 'Cheesecake de Frutos Rojos', 'Cheesecake cremoso con base de galleta y una cobertura vibrante de salsa de frutos rojos frescos.', 16000, 8, 3, 'cheesecake.jpg', 4);

5.  **Desplegar API:** Colocar los scripts PHP (`config.php`, `guardar_producto.php`, `eliminar_producto.php`, etc.) en la carpeta del servidor web (ej: `C:\xampp\htdocs\pasteleria\`).

### B. Aplicación Android (Kotlin)

1.  **Configuración de IP:** La aplicación debe saber dónde está el servidor XAMPP.
      * En el archivo **`RetrofitClient.kt`**, configurar la `BASE_URL` con la IP local de tu máquina y el puerto de Apache.
        ```kotlin
        // Ejemplo con puerto 8081
        private const val BASE_URL = "http://192.168.18.64:8081/pasteleria/"
        ```
2.  **Tokens de IA:** Las funcionalidades de IA requieren la configuración de las claves de API (Gemini) en el repositorio o *build config* de la aplicación.
3.  **Compilación:** Sincronizar Gradle y compilar en un dispositivo o emulador conectado a la misma red WiFi que el servidor XAMPP.

-----

## 🛠️ Tecnologías Utilizadas

  * **Lenguaje:** Kotlin
  * **Persistencia:** SQLite (local)
  * **Frameworks:** Android SDK
  * **Networking:** Retrofit, OkHttp
  * **Base de Datos Remota:** MySQL
  * **API:** PHP (XAMPP)
  * **Inteligencia Artificial:** Google Gemini (GenerativeModel), Remove.bg API.
