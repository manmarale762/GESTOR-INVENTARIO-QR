package com.inventario.control.controllers;

import com.inventario.control.domain.entities.Employee;
import com.inventario.control.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = normalize(loginRequest.get("email"));
        String password = loginRequest.getOrDefault("password", "");

        if (email.isBlank() || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Debes informar correo y contraseña."));
        }

        Employee user = employeeRepository.findByEmail(email).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getActivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Usuario no registrado o inactivo."));
        }

        if (!password.equals(user.getPasswordApp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Contraseña incorrecta."));
        }

        return ResponseEntity.ok(Map.of(
                "token", UUID.randomUUID().toString(),
                "refreshToken", UUID.randomUUID().toString(),
                "user", Map.of(
                        "id", String.valueOf(user.getId()),
                        "fullName", user.getNombre(),
                        "email", user.getEmail(),
                        "role", user.getRol() == null || user.getRol().name().equals("TRABAJADOR") ? "worker" : "admin",
                        "employeeCode", user.getEmployeeCode(),
                        "zoneName", user.getZoneName(),
                        "department", user.getDepartment(),
                        "avatarColor", user.getAvatarColor(),
                        "totpSecret", user.getSecretaTotp(),
                        "odooUserId", user.getOdooUserId()
                )
        ));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
