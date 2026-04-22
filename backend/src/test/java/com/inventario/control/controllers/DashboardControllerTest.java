package com.inventario.control.controllers;

import com.inventario.control.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void dashboardReturnsMetrics() throws Exception {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM prestamos WHERE status IN ('active', 'overdue')"), eq(Integer.class))).thenReturn(2);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM inventario_items WHERE status = 'restricted'"), eq(Integer.class))).thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM inventario_items WHERE status = 'available'"), eq(Integer.class))).thenReturn(3);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM logs_acceso WHERE DATE(fecha_registro) = CURDATE()"), eq(Integer.class))).thenReturn(4);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeLoans").value(2))
                .andExpect(jsonPath("$.restrictedZones").value(1))
                .andExpect(jsonPath("$.availableAssets").value(3))
                .andExpect(jsonPath("$.todayScans").value(4));
    }
}
