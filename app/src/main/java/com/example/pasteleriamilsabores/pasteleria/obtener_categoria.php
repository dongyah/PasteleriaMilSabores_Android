<?php

ob_start();

$host = "localhost";
$user = "root";
$pass = ""; 
$db   = "pasteleria_mil_sabores";
$port = 3306; 

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 

$conexion = new mysqli($host, $user, $pass, $db, $port);

if ($conexion->connect_error) {
    ob_end_clean();
    die(json_encode(array())); 
}

$sql = "SELECT id, nombre FROM categorias ORDER BY nombre ASC"; 
$resultado = $conexion->query($sql);

$categorias = array(); 

if ($resultado && $resultado->num_rows > 0) {
    while($fila = $resultado->fetch_assoc()) {
        $categorias[] = $fila; 
    }
}

ob_end_clean(); 

echo json_encode($categorias);

$conexion->close();
?>