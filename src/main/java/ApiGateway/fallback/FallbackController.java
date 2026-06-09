package ApiGateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/core")
    public ResponseEntity<Map<String, String>> coreFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "CoreService no disponible", "mensaje", "El servicio de autenticación está temporalmente fuera de línea."));
    }

    @GetMapping("/citas")
    public ResponseEntity<Map<String, String>> citasFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "CitasService no disponible", "mensaje", "El servicio de citas está temporalmente fuera de línea."));
    }

    @GetMapping("/listas")
    public ResponseEntity<Map<String, String>> listasFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "ListasService no disponible", "mensaje", "El servicio de listas de espera está temporalmente fuera de línea."));
    }

    @GetMapping("/pacientes")
    public ResponseEntity<Map<String, String>> pacientesFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "PacientesService no disponible", "mensaje", "El servicio de pacientes está temporalmente fuera de línea."));
    }
}