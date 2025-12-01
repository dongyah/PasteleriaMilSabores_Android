<?php

$host = "localhost";
$user = "root";
$pass = ""; 
$db   = "pasteleria_mil_sabores";
$port = 3306; 

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 
header('Access-Control-Allow-Methods: POST, OPTIONS');

$conexion = new mysqli($host, $user, $pass, $db, $port);

if ($conexion->connect_error) {
    die(json_encode(array("status" => "error", "message" => "Conexión a MySQL fallida: " . $conexion->connect_error)));
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    die(json_encode(array("status" => "error", "message" => "Método no permitido. Se espera POST.")));
}


$codigo_producto = $conexion->real_escape_string($_POST['codigo_producto'] ?? '');
$nombre          = $conexion->real_escape_string($_POST['nombre'] ?? '');
$descripcion     = $conexion->real_escape_string($_POST['descripcion'] ?? '');
$imagen_url      = $conexion->real_escape_string($_POST['imagen_url'] ?? ''); 

$imagen_url = str_replace(array("\n", "\r", " "), "", $imagen_url);

$precio          = (int)($_POST['precio'] ?? 0);
$stock           = (int)($_POST['stock'] ?? 0);
$stock_critico   = (int)($_POST['stock_critico'] ?? 0);
$categoria_id    = (int)($_POST['categoria_id'] ?? 1);

if (empty($nombre) || $precio <= 0 || $stock <= 0 || empty($codigo_producto)) {
    die(json_encode(array("status" => "error", "message" => "Faltan datos obligatorios o son inválidos.")));
}

$sql = "INSERT INTO productos 
        (codigo_producto, nombre, descripcion, precio, stock, stock_critico, imagen_url, categoria_id) 
        VALUES 
        ('$codigo_producto', '$nombre', '$descripcion', $precio, $stock, $stock_critico, '$imagen_url', $categoria_id)";

if ($conexion->query($sql) === TRUE) {
    echo json_encode(array("status" => "success", "message" => "Producto '$nombre' guardado correctamente con ID: " . $conexion->insert_id));
} else {
    echo json_encode(array("status" => "error", "message" => "Error SQL: " . $conexion->error));
}

$conexion->close();
?>