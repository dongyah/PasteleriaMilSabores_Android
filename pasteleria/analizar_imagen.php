<?php

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 
header('Access-Control-Allow-Methods: POST, OPTIONS');

$json_data = file_get_contents("php://input");
$data = json_decode($json_data, true);

$base64_image = $data['base64_image'] ?? '';
$image_size_kb = (strlen($base64_image) / 1024);

if (empty($base64_image)) {
    die(json_encode(["status" => "error", "message" => "No se recibió la imagen Base64 para análisis."]));
}


$analysis_description = "Se recibió la imagen con éxito (Tamaño: " . number_format($image_size_kb, 2) . " KB). Fondo detectado como un interior de cocina. La IA sugiere reemplazar el fondo por blanco puro para un mejor marketing.";

echo json_encode([
    "status" => "success",
    "description" => $analysis_description,
    "message" => "Análisis de imagen completado (Simulado)."
]);
?>