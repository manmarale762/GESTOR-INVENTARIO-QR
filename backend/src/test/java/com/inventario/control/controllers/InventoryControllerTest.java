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

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void inventoryReturnsSerializedItems() throws Exception {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of(
                        "id", "item-01",
                        "name", "Escaner termico FLIR",
                        "serialNumber", "FL-8891",
                        "category", "Herramienta de diagnostico",
                        "location", "Laboratorio tecnico",
                        "quantity", 1,
                        "highValue", true,
                        "status", "loaned",
                        "assignedTo", "Laura Martin"
                )
        ));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("item-01"))
                .andExpect(jsonPath("$[0].serialNumber").value("FL-8891"))
                .andExpect(jsonPath("$[0].assignedTo").value("Laura Martin"));
    }
}
