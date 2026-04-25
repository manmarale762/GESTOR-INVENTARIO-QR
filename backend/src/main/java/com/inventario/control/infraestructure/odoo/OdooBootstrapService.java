package com.inventario.control.infraestructure.odoo;

import com.inventario.control.domain.entities.Employee;
import com.inventario.control.repositories.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OdooBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(OdooBootstrapService.class);

    @Autowired
    private OdooService odooService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrapOdoo() {
        if (!odooService.isConfigured()) {
            log.warn("Bootstrap Odoo omitido: configuración incompleta.");
            return;
        }

        try {
            Integer uid = odooService.authenticateServiceUser();
            if (uid == null) {
                log.warn("Bootstrap Odoo omitido: no fue posible autenticarse con el usuario de servicio.");
                return;
            }

            bootstrapLocations(uid);
            bootstrapEmployees(uid);
            bootstrapInventory(uid);

            log.info("Bootstrap Odoo completado correctamente.");
        } catch (Exception ex) {
            log.error("Bootstrap Odoo falló: {}", ex.getMessage(), ex);
        }
    }

    private void bootstrapLocations(Integer uid) throws Exception {
        Set<String> locations = new LinkedHashSet<>();

        List<Employee> employees = employeeRepository.findAll();
        for (Employee employee : employees) {
            if (employee.getZoneName() != null && !employee.getZoneName().isBlank()) {
                locations.add(employee.getZoneName().trim());
            }
            if (employee.getNombre() != null && !employee.getNombre().isBlank()) {
                locations.add(employee.getNombre().trim());
            }
        }

        List<Map<String, Object>> itemRows = jdbcTemplate.queryForList(
                "SELECT DISTINCT location FROM inventario_items WHERE location IS NOT NULL AND location <> ''"
        );
        for (Map<String, Object> row : itemRows) {
            Object location = row.get("location");
            if (location != null) {
                locations.add(String.valueOf(location).trim());
            }
        }

        locations.add("Control Central");
        locations.add("Zona restringida A");
        locations.add("Jaula de seguridad");
        locations.add("Laboratorio técnico");
        locations.add("Zona de picking");
        locations.add("Taller 2");

        for (String location : locations) {
            odooService.ensureLocation(uid, location);
        }

        log.info("Bootstrap Odoo: {} ubicaciones aseguradas.", locations.size());
    }

    private void bootstrapEmployees(Integer uid) throws Exception {
        List<Employee> employees = employeeRepository.findAll();

        for (Employee employee : employees) {
            odooService.bootstrapEmployee(uid, employee);
        }

        log.info("Bootstrap Odoo: {} empleados sincronizados.", employees.size());
    }

    private void bootstrapInventory(Integer uid) throws Exception {
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
                """
                SELECT external_id, odoo_product_id, name, serial_number, category, location, quantity, assigned_to_user_id, high_value, status
                FROM inventario_items
                ORDER BY id
                """
        );

        int ok = 0;
        int ko = 0;

        for (Map<String, Object> item : items) {
            try {
                odooService.bootstrapItem(uid, item);
                ok++;
            } catch (Exception ex) {
                ko++;
                log.warn("Bootstrap Odoo: error sincronizando activo {}: {}", item.get("external_id"), ex.getMessage());
            }
        }

        log.info("Bootstrap Odoo: {} activos sincronizados. {} activos con incidencia.", ok, ko);
    }
}