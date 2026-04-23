package com.inventario.control.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventario.control.config.SecurityConfig;
import com.inventario.control.infraestructure.odoo.OdooService;
import com.inventario.control.repositories.EmployeeRepository;
import com.inventario.control.services.ITOTPService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessController.class)
@Import(SecurityConfig.class)
class AccessControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private ITOTPService totpService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private OdooService odooService;

    @Test
    void validateRejectsUnsupportedAction() throws Exception {
        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildPayload("delete", "Zona restringida A")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La acción indicada no es válida. Usa access, checkout o return."));
    }

    @Test
    void validateRejectsMissingTarget() throws Exception {
        mockMvc.perform(post("/api/access/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildPayload("access", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La petición debe incluir qrContent y target."));
    }

    private String buildPayload(String action, String target) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "qrContent", "{\"workerId\":\"2\",\"token\":\"123456\",\"expiresAt\":1893456000000}",
                "action", action,
                "target", target
        ));
    }
}
