package com.inventario.control.controllers;

import com.inventario.control.domain.entities.Employee;
import com.inventario.control.infraestructure.odoo.OdooService;
import com.inventario.control.repositories.EmployeeRepository;
import com.inventario.control.services.ITOTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ITOTPService totpService;

    @Autowired
    private OdooService odooService; // Inyectamos el servicio de Odoo

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        String code = loginRequest.get("code");

        // 1. Buscar usuario en nuestra base de datos local (MySQL)
        Employee user = employeeRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("Usuario no registrado en el sistema de inventario");
        }

        // 2. VALIDACIÓN REAL EN ODOO vía XML-RPC
        // Esto usa la URL y la DB definidas en tu application.properties
        boolean isOdooValid = odooService.authenticate(email, password);

        if (!isOdooValid) {
            return ResponseEntity.status(401).body("Credenciales de Odoo (email/password) incorrectas");
        }

        // 3. VALIDACIÓN REAL DEL TOTP
        // Compara el código enviado por el front con la semilla 'secretaTotp' de la DB
        boolean isTotpValid = totpService.verifyTOTP(user.getSecretaTotp(), code);

        if (isTotpValid) {
            // Login exitoso: Devolvemos información útil para el Frontend
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "mensaje", "Autenticación de doble factor completada",
                    "data", Map.of(
                            "nombre", user.getNombre(),
                            "rol", user.getRol(),
                            "odooId", user.getOdooUserId()
                    )
            ));
        } else {
            return ResponseEntity.status(401).body("Código TOTP inválido o expirado");
        }
    }
}