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
public class HistoryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/movements")
    public List<Map<String, Object>> history() {
        String sql = """
                SELECT
                    CONCAT('scan-', l.id) AS id,
                    u.nombre AS workerName,
                    CAST(u.id AS CHAR) AS workerId,
                    l.tipo_accion AS action,
                    l.objetivo AS target,
                    CASE WHEN l.resultado_validacion THEN 'approved' ELSE 'denied' END AS result,
                    DATE_FORMAT(l.fecha_registro, '%Y-%m-%dT%H:%i:%sZ') AS timestamp,
                    l.mensaje_sync AS reason
                FROM logs_acceso l
                INNER JOIN usuarios u ON u.id = l.usuario_id
                ORDER BY l.fecha_registro DESC, l.id DESC
                """;
        return jdbcTemplate.queryForList(sql);
    }
}
