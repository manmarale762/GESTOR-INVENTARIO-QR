CREATE DATABASE IF NOT EXISTS inventario_db;
USE inventario_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    odoo_user_id INT NULL UNIQUE,
    odoo_employee_id INT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_app VARCHAR(100) NOT NULL,
    secreta_totp VARCHAR(64) NOT NULL,
    employee_code VARCHAR(30) NOT NULL,
    zone_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    avatar_color VARCHAR(20) NOT NULL DEFAULT '#2563eb',
    rol ENUM('TRABAJADOR', 'ADMINISTRADOR') DEFAULT 'TRABAJADOR',
    activo BOOLEAN DEFAULT TRUE,
    ultima_sincronizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_odoo_ref (odoo_user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventario_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    external_id VARCHAR(30) NOT NULL UNIQUE,
    odoo_product_id INT NULL,
    odoo_lot_id INT NULL,
    odoo_location_id INT NULL,
    name VARCHAR(120) NOT NULL,
    serial_number VARCHAR(60) NOT NULL,
    category VARCHAR(80) NOT NULL,
    location VARCHAR(120) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    assigned_to_user_id INT NULL,
    high_value BOOLEAN NOT NULL DEFAULT TRUE,
    status ENUM('available', 'loaned', 'maintenance', 'restricted') NOT NULL DEFAULT 'available',
    CONSTRAINT fk_items_usuario FOREIGN KEY (assigned_to_user_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS prestamos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    external_id VARCHAR(30) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    borrowed_at DATETIME NOT NULL,
    due_at DATETIME NOT NULL,
    returned_at DATETIME NULL,
    status ENUM('active', 'overdue', 'returned') NOT NULL DEFAULT 'active',
    location VARCHAR(120) NOT NULL,
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_prestamo_item FOREIGN KEY (item_id) REFERENCES inventario_items(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS logs_acceso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo_accion ENUM('access', 'checkout', 'return') NOT NULL,
    objetivo VARCHAR(150) NOT NULL,
    resultado_validacion BOOLEAN NOT NULL,
    token_utilizado VARCHAR(10),
    dispositivo_id VARCHAR(50),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    mensaje_sync VARCHAR(255) NULL,
    CONSTRAINT fk_usuario_log FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO usuarios (odoo_user_id, nombre, email, password_app, secreta_totp, employee_code, zone_name, department, avatar_color, rol)
VALUES
    (1, 'Carlos Ortega', 'admin@demo.com', '123456', 'KRUGS4ZANFZSAYJA', 'ADM-0099', 'Control Central', 'Seguridad e Inventario', '#16a34a', 'ADMINISTRADOR'),
    (2, 'Laura Martín', 'worker@demo.com', '123456', 'JBSWY3DPEHPK3PXP', 'EMP-2048', 'Almacén Norte', 'Operaciones', '#2563eb', 'TRABAJADOR')
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    password_app = VALUES(password_app),
    secreta_totp = VALUES(secreta_totp),
    employee_code = VALUES(employee_code),
    zone_name = VALUES(zone_name),
    department = VALUES(department),
    avatar_color = VALUES(avatar_color),
    rol = VALUES(rol),
    activo = TRUE;

INSERT INTO inventario_items (external_id, odoo_product_id, name, serial_number, category, location, quantity, assigned_to_user_id, high_value, status)
VALUES
    ('item-01', NULL, 'Escáner térmico FLIR', 'FL-8891', 'Herramienta de diagnóstico', 'Laboratorio técnico', 1, 2, TRUE, 'loaned'),
    ('item-02', NULL, 'Tableta industrial Zebra', 'ZB-1022', 'Dispositivo móvil', 'Zona de picking', 1, 2, TRUE, 'loaned'),
    ('item-03', NULL, 'Llave dinamométrica Bosch', 'BS-2214', 'Herramienta', 'Jaula de seguridad', 1, NULL, TRUE, 'available'),
    ('item-04', NULL, 'Kit de calibración Omega', 'OM-3011', 'Instrumentación', 'Zona restringida A', 1, NULL, TRUE, 'restricted'),
    ('item-05', NULL, 'Soldador portátil Weller', 'WL-9014', 'Herramienta', 'Taller 2', 1, NULL, FALSE, 'maintenance')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    serial_number = VALUES(serial_number),
    category = VALUES(category),
    location = VALUES(location),
    quantity = VALUES(quantity),
    assigned_to_user_id = VALUES(assigned_to_user_id),
    high_value = VALUES(high_value),
    status = VALUES(status);

INSERT INTO prestamos (external_id, user_id, item_id, borrowed_at, due_at, returned_at, status, location)
SELECT 'loan-01', 2, i.id, '2026-04-08 08:00:00', '2026-04-10 18:00:00', NULL, 'active', 'Laboratorio técnico'
FROM inventario_items i WHERE i.external_id = 'item-01'
ON DUPLICATE KEY UPDATE status = VALUES(status), due_at = VALUES(due_at), location = VALUES(location);

INSERT INTO prestamos (external_id, user_id, item_id, borrowed_at, due_at, returned_at, status, location)
SELECT 'loan-02', 2, i.id, '2026-04-07 09:30:00', '2026-04-09 16:30:00', NULL, 'overdue', 'Zona de picking'
FROM inventario_items i WHERE i.external_id = 'item-02'
ON DUPLICATE KEY UPDATE status = VALUES(status), due_at = VALUES(due_at), location = VALUES(location);

INSERT INTO logs_acceso (usuario_id, tipo_accion, objetivo, resultado_validacion, token_utilizado, dispositivo_id, fecha_registro)
VALUES
    (2, 'access', 'Zona restringida A', TRUE, '000000', 'seed-device', '2026-04-09 07:59:10'),
    (2, 'checkout', 'Escáner térmico FLIR', TRUE, '000000', 'seed-device', '2026-04-08 08:01:03'),
    (2, 'checkout', 'Tableta industrial Zebra', TRUE, '000000', 'seed-device', '2026-04-07 09:31:24')
ON DUPLICATE KEY UPDATE objetivo = VALUES(objetivo);

CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_password_segura';
GRANT SELECT, INSERT, UPDATE ON inventario_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;
