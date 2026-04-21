package com.inventario.control.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/access-targets")
    public List<Map<String, String>> accessTargets() {
        String sql = """
                SELECT value, label
                FROM (
                    SELECT DISTINCT zone_name AS value, zone_name AS label, 1 AS sort_order
                    FROM usuarios
                    WHERE zone_name IS NOT NULL AND zone_name <> ''

                    UNION

                    SELECT DISTINCT location AS value, location AS label, 2 AS sort_order
                    FROM inventario_items
                    WHERE location IS NOT NULL AND location <> ''

                    UNION

                    SELECT 'Zona restringida A' AS value, 'Zona restringida A' AS label, 3 AS sort_order
                    UNION
                    SELECT 'Jaula de seguridad' AS value, 'Jaula de seguridad' AS label, 4 AS sort_order
                    UNION
                    SELECT 'Laboratorio técnico' AS value, 'Laboratorio técnico' AS label, 5 AS sort_order
                    UNION
                    SELECT 'Control Central' AS value, 'Control Central' AS label, 6 AS sort_order
                ) t
                GROUP BY value, label, sort_order
                ORDER BY sort_order, label
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("value", rs.getString("value"));
            item.put("label", rs.getString("label"));
            return item;
        });
    }

    @GetMapping("/asset-targets")
    public List<Map<String, String>> assetTargets() {
        String sql = """
                SELECT
                    external_id AS value,
                    CONCAT(name, ' · ', serial_number) AS label
                FROM inventario_items
                ORDER BY name, serial_number
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("value", rs.getString("value"));
            item.put("label", rs.getString("label"));
            return item;
        });
    }
}