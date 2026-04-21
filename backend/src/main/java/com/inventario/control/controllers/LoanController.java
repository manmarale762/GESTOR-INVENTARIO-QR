package com.inventario.control.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workers")
public class LoanController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/{userId}/loans")
    public List<Map<String, Object>> getLoans(@PathVariable String userId) {
        String sql = """
                SELECT
                    p.external_id AS id,
                    i.external_id AS itemId,
                    i.name AS itemName,
                    i.serial_number AS serialNumber,
                    DATE_FORMAT(p.borrowed_at, '%Y-%m-%dT%H:%i:%sZ') AS borrowedAt,
                    DATE_FORMAT(p.due_at, '%Y-%m-%dT%H:%i:%sZ') AS dueAt,
                    p.status,
                    p.location
                FROM prestamos p
                INNER JOIN inventario_items i ON i.id = p.item_id
                WHERE p.user_id = ?
                ORDER BY p.borrowed_at DESC
                """;
        return jdbcTemplate.queryForList(sql, Integer.parseInt(userId));
    }
}
