package com.inventario.control.controllers;

import com.inventario.control.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
@Import(SecurityConfig.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void movementsReturnsOrderedHistoryPayload() throws Exception {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of(
                        "id", "scan-1",
                        "workerName", "Laura Martin",
                        "workerId", "2",
                        "action", "access",
                        "target", "Zona restringida A",
                        "result", "approved",
                        "timestamp", "2026-04-22T10:00:00Z",
                        "reason", "Acceso sincronizado con Odoo (Asistencias)."
                )
        ));

        mockMvc.perform(get("/api/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workerName").value("Laura Martin"))
                .andExpect(jsonPath("$[0].action").value("access"))
                .andExpect(jsonPath("$[0].result").value("approved"));
    }
}
