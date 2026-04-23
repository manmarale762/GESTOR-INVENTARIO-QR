package com.inventario.control.controllers;

import com.inventario.control.domain.entities.Employee;
import com.inventario.control.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = normalize(loginRequest.get("email"));
        String password = normalizePassword(loginRequest.get("password"));

        if (email.isBlank() || password.isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "Debes informar correo y contraseña.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return error(HttpStatus.BAD_REQUEST, "El correo indicado no tiene un formato válido.");
        }

        Employee user = employeeRepository.findByEmail(email).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getActivo())) {
            return error(HttpStatus.UNAUTHORIZED, "Usuario no registrado o inactivo.");
        }

        if (!password.equals(user.getPasswordApp())) {
            return error(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta.");
        }

        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("id", String.valueOf(user.getId()));
        userPayload.put("fullName", user.getNombre());
        userPayload.put("email", user.getEmail());
        userPayload.put("role", user.getRol() == null || user.getRol().name().equals("TRABAJADOR") ? "worker" : "admin");
        userPayload.put("employeeCode", user.getEmployeeCode());
        userPayload.put("zoneName", user.getZoneName());
        userPayload.put("department", user.getDepartment());
        userPayload.put("avatarColor", user.getAvatarColor());
        userPayload.put("totpSecret", user.getSecretaTotp());
        if (user.getOdooUserId() != null) {
            userPayload.put("odooUserId", user.getOdooUserId());
        }

        return ResponseEntity.ok(Map.of(
                "token", UUID.randomUUID().toString(),
                "refreshToken", UUID.randomUUID().toString(),
                "user", userPayload
        ));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String normalizePassword(String value) {
        return value == null ? "" : value.trim();
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
