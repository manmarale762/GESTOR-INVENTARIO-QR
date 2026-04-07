package com.inventario.control.repositories;

import com.inventario.control.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // Permite buscar al trabajador por su ID de Odoo para la validación (RA 3)
    Optional<Employee> findByOdooUserId(Integer odooUserId);

    // Permite buscar por email para el login
    Optional<Employee> findByEmail(String email);
}