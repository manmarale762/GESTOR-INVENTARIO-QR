package com.inventario.control.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InventoryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/inventory")
    public List<Map<String, Object>> inventory() {
        String sql = """
                SELECT
                    external_id AS id,
                    name,
                    serial_number AS serialNumber,
                    category,
                    location,
                    quantity,
                    high_value AS highValue,
                    status,
                    u.nombre AS assignedTo
                FROM inventario_items i
                LEFT JOIN usuarios u ON u.id = i.assigned_to_user_id
                ORDER BY i.id
                """;
        return jdbcTemplate.queryForList(sql);
    }
}
