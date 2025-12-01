<?php

$host = "localhost";
$user = "root";
$pass = ""; 
$db   = "pasteleria_mil_sabores";
$port = 3306;


header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 
header('Access-Control-Allow-Methods: GET, OPTIONS');

$conexion = new mysqli($host, $user, $pass, $db);

if ($conexion->connect_error) {
    die(json_encode(array("error" => "Conexión con BD fallida: " . $conexion->connect_error)));
}

$id = (int)($_GET['id'] ?? 0);

if ($id <= 0) {
    die(json_encode(array()));
}

$sql = "SELECT id, codigo_producto, nombre, descripcion, precio, stock, stock_critico, imagen_url, categoria_id FROM productos WHERE id = $id";
$resultado = $conexion->query($sql);


if ($resultado && $resultado->num_rows == 1) {
    // Devuelve un ÚNICO OBJETO (no un array)
    $producto = $resultado->fetch_assoc();
    echo json_encode($producto); 
} else {
    echo json_encode(array());
}
$conexion->close();
?>