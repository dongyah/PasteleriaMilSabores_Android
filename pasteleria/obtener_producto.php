<?php

$host = "localhost";
$user = "root";
$pass = ""; 
$db   = "pasteleria_mil_sabores"; 

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');

$port = 3306;
$conexion = new mysqli($host, $user, $pass, $db, $port);

if ($conexion->connect_error) {
    die(json_encode(array("error" => "Conexión con BD fallida: " . $conexion->connect_error)));
}


$sql = "SELECT id, codigo_producto, nombre, descripcion, precio, stock, stock_critico, imagen_url, categoria_id FROM productos ORDER BY nombre ASC"; 
$resultado = $conexion->query($sql);

$productos = array();

if ($resultado && $resultado->num_rows > 0) {
    while($fila = $resultado->fetch_assoc()) {
        $productos[] = $fila; // Agrega cada producto al array
    }
}

echo json_encode($productos); 

$conexion->close();
?>