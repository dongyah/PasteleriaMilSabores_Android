<?php
$host = "localhost";
$user = "root";
$pass = ""; 
$db   = "pasteleria_mil_sabores";
$port = 3306;


header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 
header('Access-Control-Allow-Methods: PUT, OPTIONS'); 

$conexion = new mysqli($host, $user, $pass, $db);

if ($conexion->connect_error) {
    die(json_encode(array("status" => "error", "message" => "Conexión con BD fallida: " . $conexion->connect_error)));
}

parse_str(file_get_contents("php://input"), $PUT);

$id              = (int)($PUT['id'] ?? 0);
$codigo_producto = $conexion->real_escape_string($PUT['codigo_producto'] ?? '');
$nombre          = $conexion->real_escape_string($PUT['nombre'] ?? '');
$descripcion     = $conexion->real_escape_string($PUT['descripcion'] ?? '');
$imagen_url      = $conexion->real_escape_string($PUT['imagen_url'] ?? '');

$precio          = (int)($PUT['precio'] ?? 0);
$stock           = (int)($PUT['stock'] ?? 0);
$stock_critico   = (int)($PUT['stock_critico'] ?? 0);
$categoria_id    = (int)($PUT['categoria_id'] ?? 1);

if ($id <= 0 || empty($nombre)) {
    die(json_encode(array("status" => "error", "message" => "ID o Nombre son obligatorios para actualizar.")));
}

$sql = "UPDATE productos SET 
        codigo_producto = '$codigo_producto',
        nombre = '$nombre', 
        descripcion = '$descripcion', 
        precio = $precio, 
        stock = $stock,
        stock_critico = $stock_critico,
        imagen_url = '$imagen_url',
        categoria_id = $categoria_id
        WHERE id = $id";

if ($conexion->query($sql) === TRUE) {
    echo json_encode(array("status" => "success", "message" => "Producto ID $id actualizado correctamente."));
} else {
    echo json_encode(array("status" => "error", "message" => "Error al actualizar: " . $conexion->error));
}
$conexion->close();
?>