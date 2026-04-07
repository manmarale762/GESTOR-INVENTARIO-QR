-- database/init.sql

-- 1. Configuración del Entorno
CREATE DATABASE IF NOT EXISTS inventario_db;
USE inventario_db;

-- 2. Tabla de Usuarios (Evidencia 5: Gestión de Identidad)
CREATE TABLE IF NOT EXISTS usuarios (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        odoo_user_id INT NOT NULL UNIQUE,          -- ID de 'res.users' en Odoo para XML-RPC
                                        nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    secreta_totp VARCHAR(64) NOT NULL,         -- Semilla Base32 para validar el QR dinámico
    rol ENUM('trabajador', 'administrador') DEFAULT 'trabajador',
    activo BOOLEAN DEFAULT TRUE,
    ultima_sincronizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_odoo_ref (odoo_user_id)           -- Optimización para el puente Java-Odoo
    ) ENGINE=InnoDB;

-- 3. Tabla de Movimientos de Inventario
-- Registra qué material se mueve antes de la sincronización con Odoo
CREATE TABLE IF NOT EXISTS movimientos_inventario (
                                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                                      odoo_user_id INT NOT NULL,                 -- Quién realiza la acción
                                                      odoo_product_id INT NOT NULL,              -- Qué material (ID de Odoo)
                                                      tipo_movimiento ENUM('ENTRADA', 'SALIDA') NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_movimiento
    FOREIGN KEY (odoo_user_id) REFERENCES usuarios(odoo_user_id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- 4. Tabla de Logs de Acceso (Evidencia 1: Auditoría de Seguridad)
-- Registra cada intento de escaneo para control de accesos físico
CREATE TABLE IF NOT EXISTS logs_acceso (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           usuario_id INT NOT NULL,
                                           tipo_accion ENUM('entrada', 'salida', 'prestamo', 'devolucion') NOT NULL,
    resultado_validacion BOOLEAN NOT NULL,     -- TRUE si el TOTP fue correcto
    token_utilizado VARCHAR(10),               -- El código de 6 dígitos usado
    dispositivo_id VARCHAR(50),                -- ID del terminal que escaneó
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_log FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- 5. Datos de Prueba (Seeders)
INSERT INTO usuarios (odoo_user_id, nombre, email, secreta_totp, rol) VALUES
                                                                          (1, 'Admin Odoo', 'admin@ejemplo.com', 'JBSWY3DPEHPK3PXP', 'administrador'),
                                                                          (2, 'Operario Almacen', 'worker1@ejemplo.com', 'HXDMVJECJJWSRB3D', 'trabajador');

-- 6. Seguridad: Usuario de Aplicación (Privilegio Mínimo)
-- Creamos el usuario limitado para el backend Java
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_password_segura';
GRANT SELECT, INSERT, UPDATE ON inventario_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;