package com.inventario.control.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Integer activeLoans = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM prestamos WHERE status IN ('active', 'overdue')", Integer.class);
        Integer restrictedZones = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventario_items WHERE status = 'restricted'", Integer.class);
        Integer availableAssets = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventario_items WHERE status = 'available'", Integer.class);
        Integer todayScans = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM logs_acceso WHERE DATE(fecha_registro) = CURDATE()", Integer.class);

        return Map.of(
                "activeLoans", activeLoans == null ? 0 : activeLoans,
                "restrictedZones", restrictedZones == null ? 0 : restrictedZones,
                "availableAssets", availableAssets == null ? 0 : availableAssets,
                "todayScans", todayScans == null ? 0 : todayScans
        );
    }
}
