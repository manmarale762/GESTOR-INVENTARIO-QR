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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
@Import(SecurityConfig.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void workerLoansReturnsAssignedLoans() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), eq(2))).thenReturn(List.of(
                Map.of(
                        "id", "loan-01",
                        "itemId", "item-01",
                        "itemName", "Escaner termico FLIR",
                        "serialNumber", "FL-8891",
                        "borrowedAt", "2026-04-08T08:00:00Z",
                        "dueAt", "2026-04-10T18:00:00Z",
                        "status", "active",
                        "location", "Laboratorio tecnico"
                )
        ));

        mockMvc.perform(get("/api/workers/2/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId").value("item-01"))
                .andExpect(jsonPath("$[0].status").value("active"));
    }
}
