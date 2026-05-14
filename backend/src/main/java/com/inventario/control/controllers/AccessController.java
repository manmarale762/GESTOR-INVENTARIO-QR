package com.inventario.control.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventario.control.domain.entities.Employee;
import com.inventario.control.infraestructure.odoo.OdooService;
import com.inventario.control.repositories.EmployeeRepository;
import com.inventario.control.services.ITOTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ITOTPService totpService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OdooService odooService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.qr-grace-window-seconds:5}")
    private long qrGraceWindowSeconds;

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, String> payload) {
        String qrContent = payload.getOrDefault("qrContent", "");
        String action = payload.getOrDefault("action", "access");
        String target = payload.getOrDefault("target", "");

        if (qrContent.isBlank() || target.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "La petición debe incluir qrContent y target."));
        }

        try {
            JsonNode qr = objectMapper.readTree(qrContent);
            String workerId = qr.path("workerId").asText("");
            String token = qr.path("token").asText("");
            long expiresAt = qr.path("expiresAt").asLong(0L);

            if (workerId.isBlank() || token.isBlank() || expiresAt == 0L) {
                return denied(null, action, target, token, "QR incompleto o mal formado.");
            }

            Optional<Employee> employeeOpt = employeeRepository.findById(Integer.parseInt(workerId));
            if (employeeOpt.isEmpty()) {
                return denied(null, action, target, token, "Usuario no reconocido.");
            }

            Employee employee = employeeOpt.get();
            long now = Instant.now().toEpochMilli();
            if (now > expiresAt + qrGraceWindowSeconds * 1000L) {
                return denied(employee, action, target, token, "El código QR ha expirado.");
            }

            boolean valid = totpService.verifyTOTP(employee.getSecretaTotp(), token);
            if (!valid) {
                return denied(employee, action, target, token, "Token TOTP inválido.");
            }

            String syncMessage = odooService.syncEvent(employee, action, target);
            jdbcTemplate.update(
                    "INSERT INTO logs_acceso (usuario_id, tipo_accion, objetivo, resultado_validacion, token_utilizado, dispositivo_id, mensaje_sync) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    employee.getId(), action, target, true, token, "scanner-app", syncMessage
            );

            Long logId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            return ResponseEntity.ok(Map.of(
                    "approved", true,
                    "message", employee.getNombre() + " autorizado para " + action + ".",
                    "record", Map.of(
                            "id", "scan-" + (logId == null ? System.currentTimeMillis() : logId),
                            "workerName", employee.getNombre(),
                            "workerId", String.valueOf(employee.getId()),
                            "action", action,
                            "target", target,
                            "result", "approved",
                            "timestamp", Instant.now().toString(),
                            "reason", syncMessage
                    )
            ));
        } catch (NumberFormatException ex) {
            return denied(null, action, target, null, "El QR no contiene un identificador de usuario válido.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "No se pudo procesar el QR: " + ex.getMessage()));
        }
    }

    private ResponseEntity<Map<String, Object>> denied(Employee employee, String action, String target, String token, String reason) {
        if (employee != null) {
            jdbcTemplate.update(
                    "INSERT INTO logs_acceso (usuario_id, tipo_accion, objetivo, resultado_validacion, token_utilizado, dispositivo_id, mensaje_sync) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    employee.getId(), action, target, false, token, "scanner-app", reason
            );
        }

        return ResponseEntity.ok(Map.of(
                "approved", false,
                "message", reason,
                "record", Map.of(
                        "id", "scan-" + System.currentTimeMillis(),
                        "workerName", employee == null ? "Desconocido" : employee.getNombre(),
                        "workerId", employee == null ? "unknown" : String.valueOf(employee.getId()),
                        "action", action,
                        "target", target,
                        "result", "denied",
                        "timestamp", Instant.now().toString(),
                        "reason", reason
                )
        ));
    }
}
