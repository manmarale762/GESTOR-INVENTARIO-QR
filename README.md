# Gestor de Inventario QR

## 🚀 Inicio Rápido

### Requisitos
- Docker Desktop instalado y corriendo
- Puertos 3000, 8080, 8069, 3306, 5432 disponibles

### Pasos para levantar la aplicación

#### 1. Inicia todos los servicios
```bash
docker-compose up -d
```

#### 2. Espera a que todo inicie (primera vez ~60 segundos)
```bash
docker-compose ps
```
Verifica que todos los servicios estén en estado `Up`

#### 3. Accede a la aplicación
- **Frontend**: http://localhost:3000
- **Backend**: http://localhost:8080/api
- **Odoo**: http://localhost:8069

---

## 📋 Comandos Útiles

```bash
# Ver estado de los contenedores
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f backend
docker-compose logs -f frontend

# Detener servicios
docker-compose down

# Detener y eliminar datos (empezar de cero)
docker-compose down -v

# Reconstruir después de cambios
docker-compose up -d --build
```

---

## 🔗 Servicios Disponibles

| Servicio | URL | Puerto |
|----------|-----|--------|
| Frontend | http://localhost:3000 | 3000 |
| Backend API | http://localhost:8080/api | 8080 |
| Odoo ERP | http://localhost:8069 | 8069 |
| MySQL | localhost:3306 | 3306 |
| PostgreSQL | localhost:5432 | 5432 |

---

## 🗄️ Credenciales Base de Datos

| BD | Usuario | Contraseña |
|----|---------|-----------|
| MySQL | root | root_password |
| PostgreSQL | odoo | odoo_password |

---

## ⚠️ Problemas Comunes

**El backend no inicia**
```bash
docker-compose logs backend
```

**Puerto de Odoo en uso**
```bash
lsof -i :8069
# O el puerto que necesites
```

**Reconstruir todo desde cero**
```bash
docker-compose down -v
docker-compose up -d --build
```

---

## 📊 Arquitectura

```
Frontend (React Native Web)  →  Backend (Spring Boot)  →  MySQL
                                        ↓
                                    Odoo ERP  →  PostgreSQL
```

- Frontend y Backend se comunican por HTTP
- Backend se conecta a MySQL para datos locales
- Backend se conecta a Odoo para autenticación y datos principales
- Odoo usa PostgreSQL como base de datos

---

**¡Listo! Tu sistema está integrado y funcionando.** 🎉
