<?php
// 1. CONFIGURACIÓN DE HEADERS
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); 
header('Access-Control-Allow-Methods: POST, OPTIONS');

$GEMINI_API_KEY = "AIzaSyDDH4na3Zea96V4jTbdad_UPcQV0TFAvj0"; 

$GEMINI_MODEL = "gemini-2.5-flash";
$GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/{$GEMINI_MODEL}:generateContent?key=" . $GEMINI_API_KEY;

$json_data = file_get_contents("php://input");
$POST = json_decode($json_data, true);

$nombre_producto = $POST['nombre'] ?? '';

if (empty($nombre_producto)) {
    echo json_encode(["status" => "error", "message" => "El nombre del producto es obligatorio."]);
    exit();
}

$prompt = "Escribe una descripción corta (máximo 40 palabras) para el postre llamado {$nombre_producto} de la Pastelería Mil Sabores.";
$data = [
    "contents" => [
        [
            "role" => "user", 
            "parts" => [
                ["text" => $prompt]
            ]
        ]
    ],
    "generationConfig" => [ 
        "maxOutputTokens" => 200
    ]
];
$json_payload = json_encode($data);

$ch = curl_init($GEMINI_ENDPOINT);

curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json')); 
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, $json_payload); 
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true); 
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);

$response = curl_exec($ch);
$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$error = curl_error($ch);
curl_close($ch);


if ($http_code != 200) {
    $error_details = json_decode($response, true);
    $error_message = $error_details['error']['message'] ?? 'Fallo HTTP desconocido o JSON inválido en respuesta.';
    
    echo json_encode(["status" => "error", "message" => "Gemini API (HTTP {$http_code}): {$error_message}"]);
    
} elseif ($error) {
    echo json_encode(["status" => "error", "message" => "Error cURL de red: {$error}"]);

} else {
    $gemini_data = json_decode($response, true);
    
    $description = "No se pudo extraer la descripción.";
    $finishReason = 'UNKNOWN';

    if (isset($gemini_data['candidates'][0])) {
        $candidate = $gemini_data['candidates'][0];
        $finishReason = $candidate['finishReason'] ?? 'UNKNOWN';

        if (isset($candidate['content']['parts'][0]['text'])) {
            $description = $candidate['content']['parts'][0]['text'];
        }
    }
    
    if ($finishReason !== 'STOP') {
        echo json_encode(["status" => "error", "message" => "Texto bloqueado por filtros de seguridad. Razón: {$finishReason}"]);
    } else {
        if (empty($description) || $description == "No se pudo extraer la descripción.") {
             echo json_encode(["status" => "error", "message" => "La IA no generó texto válido (Respuesta vacía o malformada)."]);
        } else {
             echo json_encode(["status" => "success", "description" => $description]);
        }
    }
}
?>