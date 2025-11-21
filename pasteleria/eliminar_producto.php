<?php
require_once 'config.php';

header('Content-Type: application/json');

$conn = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_NAME, DB_PORT);

if ($conn->connect_error) {
    die(json_encode(array("status" => "error", "message" => "ERROR DE CONEXIÓN A MYSQL: " . $conn->connect_error)));
}

parse_str(file_get_contents("php://input"), $DELETE);
$id = (int)($DELETE['id'] ?? 0);

if ($id <= 0) {
    $conn->close();
    die(json_encode(array("status" => "error", "message" => "ERROR: ID no fue recibido o es 0.")));
}

$sql = "DELETE FROM productos WHERE id = ?";

if ($stmt = $conn->prepare($sql)) {
    
    $stmt->bind_param("i", $id);

    if ($stmt->execute()) {
        
        $filas_afectadas = $stmt->affected_rows;
        
        if ($filas_afectadas > 0) {
            echo json_encode(array("status" => "success", "message" => "ÉXITO: Producto ID $id ha sido eliminado. $filas_afectadas filas afectadas."));
        } else {
            echo json_encode(array("status" => "warning", "message" => "AVISO: Producto ID $id no encontrado, 0 filas afectadas."));
        }
        
    } else {
        echo json_encode(array("status" => "error", "message" => "ERROR AL EJECUTAR: " . $stmt->error));
    }
    
    $stmt->close();
    
} else {
    echo json_encode(array("status" => "error", "message" => "ERROR AL PREPARAR: " . $conn->error));
}

$conn->close();

?>