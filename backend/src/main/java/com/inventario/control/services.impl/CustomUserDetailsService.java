package com.inventario.control.services.impl;

import com.inventario.control.domain.entities.Employee;
import com.inventario.control.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscamos en la tabla 'usuarios' por el campo 'email'
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Como la contraseña se valida externamente o mediante TOTP,
        // pasamos una cadena vacía o una clave temporal.
        return User.builder()
                .username(employee.getEmail())
                .password("") // La validación real la haremos en un filtro personalizado con el TOTP
                .roles(employee.getRol().name()) // TRABAJADOR o ADMINISTRADOR
                .disabled(!employee.getActivo())
                .build();
    }
}