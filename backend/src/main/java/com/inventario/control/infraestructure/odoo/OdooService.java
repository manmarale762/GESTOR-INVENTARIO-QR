package com.inventario.control.infraestructure.odoo;

import com.inventario.control.domain.entities.Employee;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class OdooService {

    @Value("${odoo.url}")
    private String odooUrl;

    @Value("${odoo.db}")
    private String odooDb;

    @Value("${odoo.username:}")
    private String odooUsername;

    @Value("${odoo.password:}")
    private String odooPassword;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean authenticate(String email, String password) {
        try {
            XmlRpcClientConfigImpl commonConfig = new XmlRpcClientConfigImpl();
            commonConfig.setServerURL(new URL(String.format("%s/xmlrpc/2/common", odooUrl)));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(commonConfig);

            Object uid = client.execute("authenticate", Arrays.asList(
                    odooDb, email, password, Map.of()
            ));

            return asInteger(uid) != null && asInteger(uid) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConfigured() {
        return !isBlank(odooUsername) && !isBlank(odooPassword) && !isBlank(odooDb) && !isBlank(odooUrl);
    }

    public Integer authenticateServiceUser() throws Exception {
        XmlRpcClient client = commonClient();
        Object uid = client.execute("authenticate", Arrays.asList(
                odooDb, odooUsername, odooPassword, Map.of()
        ));
        Integer value = asInteger(uid);
        if (value != null && value > 0) {
            return value;
        }
        return null;
    }

    public String syncEvent(Employee employee, String action, String target) {
        if (!isConfigured()) {
            return "Sincronización Odoo omitida: configuración incompleta.";
        }

        try {
            Integer uid = authenticateServiceUser();
            if (uid == null) {
                return "Sincronización Odoo omitida: no fue posible autenticarse contra Odoo.";
            }

            bootstrapEmployee(uid, employee);

            return switch (action) {
                case "access" -> {
                    syncAttendance(uid, employee);
                    yield "Acceso sincronizado con Odoo (Asistencias).";
                }
                case "checkout" -> syncCheckout(uid, employee, target);
                case "return" -> syncReturn(uid, employee, target);
                default -> "Acción no sincronizada con Odoo: " + action;
            };
        } catch (Exception ex) {
            return "Sincronización Odoo no completada: " + ex.getMessage();
        }
    }

    public void bootstrapEmployee(Integer uid, Employee employee) throws Exception {
        Integer departmentId = ensureDepartment(uid, employee.getDepartment());
        Integer userId = ensureUser(uid, employee);
        ensureHrEmployee(uid, employee, userId, departmentId);
        ensureLocation(uid, employee.getZoneName());
        ensureLocation(uid, employee.getNombre());
    }

    public void bootstrapItem(Integer uid, Map<String, Object> itemRow) throws Exception {
        String location = asString(itemRow.get("location"));
        String category = asString(itemRow.get("category"));
        String serialNumber = asString(itemRow.get("serial_number"));
        String externalId = asString(itemRow.get("external_id"));

        Integer locationId = ensureLocation(uid, location);
        Integer productId = ensureProduct(uid, itemRow);
        Integer lotId = ensureLot(uid, productId, serialNumber);

        Integer quantity = asInteger(itemRow.get("quantity"));
        if (quantity == null || quantity < 0) {
            quantity = 1;
        }

        ensureQuant(uid, productId, lotId, locationId, quantity);

        if (productId != null && !isBlank(externalId)) {
            jdbcTemplate.update(
                    "UPDATE inventario_items SET odoo_product_id = ? WHERE external_id = ?",
                    productId, externalId
            );
        }

        if (!isBlank(category)) {
            ensureProductCategory(uid, category);
        }
    }

    public Integer ensureDepartment(Integer uid, String departmentName) throws Exception {
        if (isBlank(departmentName)) {
            return null;
        }

        XmlRpcClient models = objectClient();
        Integer existingId = searchFirstId(
                models,
                uid,
                "hr.department",
                List.of(List.of("name", "=", departmentName)),
                List.of("id")
        );

        if (existingId != null) {
            return existingId;
        }

        return create(
                models,
                uid,
                "hr.department",
                Map.of("name", departmentName)
        );
    }

    public Integer ensureUser(Integer uid, Employee employee) throws Exception {
        XmlRpcClient models = objectClient();

        Integer existingId = searchFirstId(
                models,
                uid,
                "res.users",
                List.of(List.of("login", "=", employee.getEmail())),
                List.of("id", "login")
        );

        Map<String, Object> values = new HashMap<>();
        values.put("name", employee.getNombre());
        values.put("login", employee.getEmail());
        values.put("email", employee.getEmail());
        values.put("active", Boolean.TRUE);

        if (!isBlank(employee.getPasswordApp())) {
            values.put("password", employee.getPasswordApp());
        }

        if (existingId != null) {
            write(models, uid, "res.users", existingId, values);
            updateEmployeeOdooUserId(employee.getId(), existingId);
            return existingId;
        }

        Integer createdId = create(models, uid, "res.users", values);
        updateEmployeeOdooUserId(employee.getId(), createdId);
        return createdId;
    }

    public Integer ensureHrEmployee(Integer uid, Employee employee, Integer userId, Integer departmentId) throws Exception {
        XmlRpcClient models = objectClient();

        Integer existingId = searchFirstId(
                models,
                uid,
                "hr.employee",
                List.of(List.of("work_email", "=", employee.getEmail())),
                List.of("id", "work_email", "barcode")
        );

        if (existingId == null && !isBlank(employee.getEmployeeCode())) {
            existingId = searchFirstId(
                    models,
                    uid,
                    "hr.employee",
                    List.of(List.of("barcode", "=", employee.getEmployeeCode())),
                    List.of("id", "work_email", "barcode")
            );
        }

        Map<String, Object> values = new HashMap<>();
        values.put("name", employee.getNombre());
        values.put("work_email", employee.getEmail());
        values.put("barcode", employee.getEmployeeCode());

        if (!isBlank(employee.getDepartment()) && departmentId != null) {
            values.put("department_id", departmentId);
        }
        if (userId != null) {
            values.put("user_id", userId);
        }

        if (existingId != null) {
            write(models, uid, "hr.employee", existingId, values);
            return existingId;
        }

        return create(models, uid, "hr.employee", values);
    }

    public Integer ensureLocation(Integer uid, String locationName) throws Exception {
        if (isBlank(locationName)) {
            return null;
        }

        XmlRpcClient models = objectClient();

        Integer existingId = searchFirstId(
                models,
                uid,
                "stock.location",
                List.of(
                        List.of("name", "=", locationName),
                        List.of("usage", "=", "internal")
                ),
                List.of("id", "name", "usage")
        );

        if (existingId != null) {
            return existingId;
        }

        Map<String, Object> values = new HashMap<>();
        values.put("name", locationName);
        values.put("usage", "internal");

        return create(models, uid, "stock.location", values);
    }

    public Integer ensureProductCategory(Integer uid, String categoryName) throws Exception {
        if (isBlank(categoryName)) {
            return null;
        }

        XmlRpcClient models = objectClient();

        Integer existingId = searchFirstId(
                models,
                uid,
                "product.category",
                List.of(List.of("name", "=", categoryName)),
                List.of("id", "name")
        );

        if (existingId != null) {
            return existingId;
        }

        return create(
                models,
                uid,
                "product.category",
                Map.of("name", categoryName)
        );
    }

    public Integer ensureProduct(Integer uid, Map<String, Object> itemRow) throws Exception {
        XmlRpcClient models = objectClient();

        String externalId = asString(itemRow.get("external_id"));
        String name = asString(itemRow.get("name"));
        String category = asString(itemRow.get("category"));
        Boolean highValue = asBoolean(itemRow.get("high_value"));

        Integer existingProductId = null;
        if (!isBlank(externalId)) {
            existingProductId = searchFirstId(
                    models,
                    uid,
                    "product.product",
                    List.of(List.of("default_code", "=", externalId)),
                    List.of("id", "default_code", "product_tmpl_id")
            );
        }

        Integer categoryId = ensureProductCategory(uid, category);

        if (existingProductId != null) {
            return existingProductId;
        }

        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("name", name);
        templateValues.put("default_code", externalId);
        templateValues.put("detailed_type", "product");
        templateValues.put("tracking", "serial");
        templateValues.put("sale_ok", Boolean.FALSE);
        templateValues.put("purchase_ok", Boolean.FALSE);
        templateValues.put("list_price", 0.0);
        if (categoryId != null) {
            templateValues.put("categ_id", categoryId);
        }

        Integer templateId = create(models, uid, "product.template", templateValues);

        Integer variantId = searchFirstId(
                models,
                uid,
                "product.product",
                List.of(List.of("product_tmpl_id", "=", templateId)),
                List.of("id", "product_tmpl_id")
        );

        if (variantId == null) {
            throw new IllegalStateException("No se pudo localizar la variante del producto Odoo para " + name);
        }

        return variantId;
    }

    public Integer ensureLot(Integer uid, Integer productId, String serialNumber) throws Exception {
        if (productId == null || isBlank(serialNumber)) {
            return null;
        }

        XmlRpcClient models = objectClient();

        Integer existingId = searchFirstId(
                models,
                uid,
                "stock.lot",
                List.of(
                        List.of("name", "=", serialNumber),
                        List.of("product_id", "=", productId)
                ),
                List.of("id", "name")
        );

        if (existingId != null) {
            return existingId;
        }

        return create(
                models,
                uid,
                "stock.lot",
                Map.of(
                        "name", serialNumber,
                        "product_id", productId
                )
        );
    }

    public void ensureQuant(Integer uid, Integer productId, Integer lotId, Integer locationId, Integer quantity) throws Exception {
        if (productId == null || locationId == null || quantity == null) {
            return;
        }

        XmlRpcClient models = objectClient();

        List<List<Object>> domain = new ArrayList<>();
        domain.add(List.of("product_id", "=", productId));
        domain.add(List.of("location_id", "=", locationId));
        if (lotId != null) {
            domain.add(List.of("lot_id", "=", lotId));
        }

        Integer quantId = searchFirstId(
                models,
                uid,
                "stock.quant",
                domain,
                List.of("id", "quantity", "inventory_quantity")
        );

        if (quantId == null) {
            Map<String, Object> createValues = new HashMap<>();
            createValues.put("product_id", productId);
            createValues.put("location_id", locationId);
            createValues.put("inventory_quantity", quantity);
            if (lotId != null) {
                createValues.put("lot_id", lotId);
            }

            create(models, uid, "stock.quant", createValues);
        } else {
            Map<String, Object> updateValues = new HashMap<>();
            updateValues.put("inventory_quantity", quantity);
            write(models, uid, "stock.quant", quantId, updateValues);
        }

    
    }

    private void syncAttendance(Integer uid, Employee employee) throws Exception {
        XmlRpcClient models = objectClient();

        Integer employeeId = findHrEmployeeId(models, uid, employee);
        if (employeeId == null) {
            throw new IllegalStateException("No se ha encontrado el empleado en Odoo.");
        }

        Integer openAttendanceId = searchFirstId(
                models,
                uid,
                "hr.attendance",
                List.of(
                        List.of("employee_id", "=", employeeId),
                        List.of("check_out", "=", false)
                ),
                List.of("id", "check_in", "check_out")
        );

        String now = OffsetDateTime.now(ZoneOffset.UTC).toString();

        if (openAttendanceId != null) {
            write(
                    models,
                    uid,
                    "hr.attendance",
                    openAttendanceId,
                    Map.of("check_out", now)
            );
            return;
        }

        create(
                models,
                uid,
                "hr.attendance",
                Map.of(
                        "employee_id", employeeId,
                        "check_in", now
                )
        );
    }

    private String syncCheckout(Integer uid, Employee employee, String target) throws Exception {
        Map<String, Object> itemRow = findInventoryItem(target);
        if (itemRow == null) {
            return "Checkout validado localmente. No se encontró el activo en inventario para sincronizar en Odoo.";
        }

        Integer employeeLocationId = ensureLocation(uid, employee.getNombre());
        Integer currentLocationId = ensureLocation(uid, asString(itemRow.get("location")));
        Integer productId = ensureProduct(uid, itemRow);
        Integer lotId = ensureLot(uid, productId, asString(itemRow.get("serial_number")));

        if (currentLocationId != null) {
            ensureQuant(uid, productId, lotId, currentLocationId, 0);
        }
        ensureQuant(uid, productId, lotId, employeeLocationId, 1);

        return "Préstamo sincronizado con Odoo (ubicación interna del empleado).";
    }

    private String syncReturn(Integer uid, Employee employee, String target) throws Exception {
        Map<String, Object> itemRow = findInventoryItem(target);
        if (itemRow == null) {
            return "Devolución validada localmente. No se encontró el activo en inventario para sincronizar en Odoo.";
        }

        Integer employeeLocationId = ensureLocation(uid, employee.getNombre());
        Integer originalLocationId = ensureLocation(uid, asString(itemRow.get("location")));
        Integer productId = ensureProduct(uid, itemRow);
        Integer lotId = ensureLot(uid, productId, asString(itemRow.get("serial_number")));

        if (employeeLocationId != null) {
            ensureQuant(uid, productId, lotId, employeeLocationId, 0);
        }
        ensureQuant(uid, productId, lotId, originalLocationId, 1);

        return "Devolución sincronizada con Odoo (inventario).";
    }

    private Integer findHrEmployeeId(XmlRpcClient models, Integer uid, Employee employee) throws Exception {
        Integer employeeId = searchFirstId(
                models,
                uid,
                "hr.employee",
                List.of(List.of("work_email", "=", employee.getEmail())),
                List.of("id", "work_email", "barcode")
        );

        if (employeeId != null) {
            return employeeId;
        }

        if (!isBlank(employee.getEmployeeCode())) {
            employeeId = searchFirstId(
                    models,
                    uid,
                    "hr.employee",
                    List.of(List.of("barcode", "=", employee.getEmployeeCode())),
                    List.of("id", "work_email", "barcode")
            );
        }

        return employeeId;
    }

    private Map<String, Object> findInventoryItem(String target) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                SELECT *
                FROM inventario_items
                WHERE external_id = ?
                   OR serial_number = ?
                   OR name = ?
                LIMIT 1
                """,
                target, target, target
        );

        return rows.isEmpty() ? null : rows.get(0);
    }

    private void updateEmployeeOdooUserId(Integer employeeId, Integer odooUserId) {
        if (employeeId == null || odooUserId == null) {
            return;
        }

        jdbcTemplate.update(
                "UPDATE usuarios SET odoo_user_id = ? WHERE id = ?",
                odooUserId, employeeId
        );
    }

    private Integer searchFirstId(XmlRpcClient models, Integer uid, String model, List<?> domain, List<String> fields) throws Exception {
        Object result = executeKw(
                models,
                uid,
                model,
                "search_read",
                List.of(domain),
                Map.of("fields", fields, "limit", 1)
        );

        Map<String, Object> firstRow = firstRow(result);
        if (firstRow == null) {
            return null;
        }
        return asInteger(firstRow.get("id"));
    }

    private Integer create(XmlRpcClient models, Integer uid, String model, Map<String, Object> values) throws Exception {
        Object result = executeKw(
                models,
                uid,
                model,
                "create",
                List.of(values),
                Collections.emptyMap()
        );
        return asInteger(result);
    }

    private void write(XmlRpcClient models, Integer uid, String model, Integer id, Map<String, Object> values) throws Exception {
        executeKw(
                models,
                uid,
                model,
                "write",
                List.of(List.of(id), values),
                Collections.emptyMap()
        );
    }

    private Object executeKw(XmlRpcClient models, Integer uid, String model, String method, List<?> args, Map<String, ?> kwargs) throws Exception {
        List<Object> params = new ArrayList<>();
        params.add(odooDb);
        params.add(uid);
        params.add(odooPassword);
        params.add(model);
        params.add(method);
        params.add(args == null ? Collections.emptyList() : args);
        if (kwargs != null && !kwargs.isEmpty()) {
            params.add(kwargs);
        }
        return models.execute("execute_kw", params);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstRow(Object result) {
        if (result instanceof Object[] array && array.length > 0 && array[0] instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        if (result instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private XmlRpcClient commonClient() throws Exception {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", odooUrl)));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        return client;
    }

    private XmlRpcClient objectClient() throws Exception {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(String.format("%s/xmlrpc/2/object", odooUrl)));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        return client;
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        if (value instanceof Double doubleValue) {
            return doubleValue.intValue();
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (value instanceof Object[] arr && arr.length > 0) {
            return asInteger(arr[0]);
        }
        if (value instanceof List<?> list && !list.isEmpty()) {
            return asInteger(list.get(0));
        }
        return null;
    }

    private Boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        if (value instanceof String stringValue) {
            return "true".equalsIgnoreCase(stringValue) || "1".equals(stringValue);
        }
        return Boolean.FALSE;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}