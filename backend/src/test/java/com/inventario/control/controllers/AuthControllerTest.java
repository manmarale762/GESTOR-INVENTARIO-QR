package com.inventario.control.controllers;

import com.inventario.control.domain.entities.Employee;
import com.inventario.control.domain.entities.Role;
import com.inventario.control.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.inventario.control.config.SecurityConfig;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeRepository employeeRepository;

    @Test
    void loginReturnsSessionForValidCredentials() throws Exception {
        Employee employee = new Employee();
        employee.setId(2);
        employee.setNombre("Laura Martin");
        employee.setEmail("worker@demo.com");
        employee.setPasswordApp("123456");
        employee.setEmployeeCode("EMP-2048");
        employee.setZoneName("Almacen Norte");
        employee.setDepartment("Operaciones");
        employee.setAvatarColor("#2563eb");
        employee.setSecretaTotp("JBSWY3DPEHPK3PXP");
        employee.setRol(Role.TRABAJADOR);
        employee.setActivo(true);

        when(employeeRepository.findByEmail("worker@demo.com")).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "worker@demo.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.user.id").value("2"))
                .andExpect(jsonPath("$.user.role").value("worker"))
                .andExpect(jsonPath("$.user.employeeCode").value("EMP-2048"));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("worker@demo.com");
        employee.setPasswordApp("123456");
        employee.setActivo(true);

        when(employeeRepository.findByEmail("worker@demo.com")).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "worker@demo.com",
                                  "password": "bad-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Contraseña incorrecta."));
    }

    @Test
    void loginRejectsInvalidEmailFormat() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "worker-demo.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El correo indicado no tiene un formato válido."));
    }

    @Test
    void loginRejectsInactiveUser() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("worker@demo.com");
        employee.setPasswordApp("123456");
        employee.setActivo(false);

        when(employeeRepository.findByEmail("worker@demo.com")).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "worker@demo.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario no registrado o inactivo."));
    }
}
