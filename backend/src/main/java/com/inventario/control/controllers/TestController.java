package com.inventario.control.controllers;

import com.inventario.control.domain.entities.Employee;
import com.inventario.control.repositories.EmployeeRepository;
import com.inventario.control.services.ITOTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ITOTPService totpService;

    // Endpoint para verificar si el código TOTP de un usuario es válido
    @GetMapping("/verify")
    public String verify(@RequestParam Integer odooId, @RequestParam String code) {
        // 1. Buscamos al empleado en la DB (Prueba del RA 2 / DAO)
        Optional<Employee> employeeOpt = employeeRepository.findByOdooUserId(odooId);

        if (employeeOpt.isEmpty()) {
            return "Error: Usuario no encontrado en MySQL";
        }

        Employee emp = employeeOpt.get();

        // 2. Validamos el código usando la lógica TOTP (Prueba del RA 3 / Multimedia)
        boolean isValid = totpService.verifyTOTP(emp.getSecretaTotp(), code);

        if (isValid) {
            return "¡Éxito! El código " + code + " para " + emp.getNombre() + " es VÁLIDO.";
        } else {
            return "Fallo: El código " + code + " no es correcto o ya caducó.";
        }
    }
}