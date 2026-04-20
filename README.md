# 🔐 Gestor de Inventario con Control de Acceso mediante Códigos QR Dinámicos

Un sistema empresarial completo para controlar el acceso a zonas restringidas y gestionar préstamos/devoluciones de material de alto valor mediante **códigos QR dinámicos** basados en TOTP (autenticación de dos factores).

---

## 📋 Tabla de Contenidos

1. [¿Qué es este proyecto?](#-qué-es-este-proyecto)
2. [Requisitos previos](#-requisitos-previos)
3. [Instalación y arranque rápido](#-instalación-y-arranque-rápido)
4. [Cómo usar la aplicación](#-cómo-usar-la-aplicación)
5. [Estructura del proyecto](#-estructura-del-proyecto)
6. [Stack tecnológico](#-stack-tecnológico)
7. [Solución de problemas](#-solución-de-problemas)

---

## 🎯 ¿Qué es este proyecto?

Este sistema permite:

✅ **Autenticación segura** de trabajadores y administradores  
✅ **Generación de códigos QR dinámicos** (se renuevan automáticamente)  
✅ **Control de acceso** a zonas restringidas mediante escaneo QR  
✅ **Gestión de préstamos** de herramientas y equipos de alto valor  
✅ **Registro histórico completo** de todos los movimientos  
✅ **Sincronización automática** con Odoo (ERP empresarial)  
✅ **Interfaz mobile-first** para fácil uso en el terreno  

---

## 💻 Requisitos previos

Asegúrate de tener instalado en tu computadora:

| Requisito | Versión mínima | Descargar |
|-----------|---|---|
| **Docker** | 20.10+ | [docker.com](https://www.docker.com) |
| **Docker Compose** | 1.29+ | Incluido con Docker |
| **Git** | 2.0+ | [git-scm.com](https://git-scm.com) |

**Nota:** Si usas Windows, se recomienda usar WSL 2 (Windows Subsystem for Linux).

### Verificar que está todo instalado:
```bash
docker --version
docker-compose --version
git --version
```

---

## 🚀 Instalación y arranque rápido

### Paso 1: Clonar o descargar el proyecto

```bash
# Si usas Git
git clone <URL_DEL_REPOSITORIO>
cd gestor-inventario-qr-limpio

# O simplemente descomprime el archivo del proyecto
```

### Paso 2: Iniciar todos los servicios

Desde la carpeta principal del proyecto, ejecuta:

```bash
docker-compose up -d
```

Este comando iniciará automáticamente:
- **Base de datos MySQL** (para la aplicación)
- **Base de datos PostgreSQL** (para Odoo)
- **Odoo 17** (ERP)
- **Backend en Java** (API REST)
- **Frontend en React Native** (aplicación web)

### Paso 3: Esperar a que todo esté listo

Ejecuta este comando para verificar el estado:

```bash
docker-compose ps
```

Verás algo como:
```
NAME                 STATUS
mysql-app            Up 2 minutes (healthy)
postgres-odoo        Up 2 minutes (healthy)
odoo-erp             Up 2 minutes
backend-app          Up 2 minutes
frontend-app         Up 2 minutes
```

### Paso 4: Acceder a la aplicación

Una vez que todo esté corriendo:

| Componente | URL | Acceso |
|-----------|-----|--------|
| **Aplicación web** | http://localhost:3000 | Abierto |
| **Odoo ERP** | http://localhost:8069 | Abierto |
| **API Backend** | http://localhost:8080 | Interno |
| **MySQL** | localhost:3306 | Interno (usuario: `root`, contraseña: `root_password`) |
| **PostgreSQL** | localhost:5432 | Interno (usuario: `odoo`, contraseña: `odoo_password`) |

---

## 👥 Cómo usar la aplicación

### Para administradores:

1. **Accede a la aplicación** en http://localhost:3000
2. **Inicia sesión** con tus credenciales de administrador
3. Desde el panel de control puedes:
   - 📊 Ver el dashboard con estadísticas
   - 📦 Gestionar el inventario
   - 🔍 Escanear códigos QR de trabajadores
   - 📋 Ver el historial de movimientos
   - 🏢 Configurar zonas y permisos

### Para trabajadores:

1. **Accede a la aplicación** en http://localhost:3000
2. **Inicia sesión** con tus credenciales
3. Tu código QR se genera automáticamente (renovándose cada 30 segundos)
4. Puedes:
   - 🏠 Ver tu panel de inicio
   - 📤 Solicitar préstamos de materiales
   - 📥 Devolver materiales prestados
   - 📜 Ver tu historial de actividades

### Flujo típico de acceso a zona restringida:

1. 👷 El trabajador se acerca al administrador con su código QR visible
2. 📱 El administrador escanea el código QR usando la cámara
3. ✅ El sistema valida el QR dinámico (basado en TOTP)
4. 🚪 El acceso se autoriza o deniega según los permisos
5. 📝 El movimiento se registra automáticamente en el historial

---

## 📁 Estructura del proyecto

```
gestor-inventario-qr-limpio/
├── 📄 docker-compose.yml          # Configuración de todos los servicios
├── 📄 README.md                   # Este archivo
│
├── 📁 backend/                    # API REST en Java/Spring Boot
│   ├── src/
│   │   ├── main/java/com/inventario/control/
│   │   │   ├── controllers/       # Endpoints REST
│   │   │   ├── services/          # Lógica de negocio
│   │   │   ├── repositories/      # Acceso a BD
│   │   │   ├── domain/            # Entidades
│   │   │   └── config/            # Configuración de seguridad
│   │   └── resources/
│   │       └── application.properties  # Configuración de la app
│   ├── Dockerfile
│   └── pom.xml                    # Dependencias Maven
│
├── 📁 frontend/                   # Aplicación React Native/Expo
│   ├── src/
│   │   ├── screens/               # Pantallas de la app
│   │   ├── components/            # Componentes reutilizables
│   │   ├── services/              # Llamadas a API
│   │   ├── context/               # Gestión de estado
│   │   ├── navigation/            # Navegación de la app
│   │   ├── types/                 # Tipos TypeScript
│   │   └── theme/                 # Estilos globales
│   ├── Dockerfile
│   ├── package.json               # Dependencias npm
│   └── tailwind.config.js         # Configuración de estilos
│
├── 📁 database/
│   └── init.sql                   # Script inicial de base de datos
│
├── 📁 odoo/
│   └── odoo.conf                  # Configuración de Odoo ERP
│
└── 📄 plazos.txt                  # Información de deadlines
```

---

## 🛠️ Stack tecnológico

### Frontend
- **React Native** - Framework multiplataforma
- **Expo SDK 51** - Herramientas para desarrollo React Native
- **Expo Camera** - Acceso a la cámara para escanear QR
- **React Navigation** - Navegación entre pantallas
- **Expo Secure Store** - Almacenamiento seguro de datos
- **TypeScript** - Lenguaje tipado
- **Tailwind CSS** - Estilos predefinidos (NativeWind)

### Backend
- **Java 17** - Lenguaje de programación
- **Spring Boot** - Framework web
- **Spring Data JPA** - Acceso a datos
- **XML-RPC** - Integración con Odoo
- **MySQL Connector** - Driver para MySQL

### Bases de datos y ERP
- **MySQL 8** - Base de datos relacional (aplicación)
- **PostgreSQL 15** - Base de datos (Odoo)
- **Odoo 17** - ERP empresarial

### Infraestructura
- **Docker** - Contenedorización
- **Docker Compose** - Orquestación de servicios

---

## 🔧 Solución de problemas

### ❌ Los contenedores no inician

**Problema:** `docker-compose up` falla o los contenedores se apagan inmediatamente.

**Solución:**
```bash
# Ver los logs detallados
docker-compose logs -f

# Reiniciar todo desde cero
docker-compose down -v
docker-compose up -d
```

### ❌ No puedo acceder a la aplicación en http://localhost:3000

**Solución:**
```bash
# Verificar que los contenedores están corriendo
docker-compose ps

# Ver logs de la aplicación frontend
docker-compose logs frontend-app

# Esperar 30-60 segundos (la primera vez tarda más)
```

### ❌ Error de conexión a la base de datos

**Problema:** La aplicación no puede conectarse a MySQL o PostgreSQL.

**Solución:**
```bash
# Verificar que las bases de datos están saludables
docker-compose ps

# Reiniciar solo las bases de datos
docker-compose restart db-app db-odoo

# Esperar 20 segundos y reintentar
```

### ❌ Puertos ya en uso

**Problema:** `Error: port 3000 is already in use` u otro puerto.

**Solución:**
```bash
# En Windows (PowerShell)
netstat -ano | findstr :3000

# En Linux/Mac
lsof -i :3000

# Luego cambiar el puerto en docker-compose.yml
# O matar el proceso existente
```

### ❌ Sin conexión a internet en contenedores

**Solución:**
```bash
# Verificar la configuración de Docker
docker network ls

# Recrear los contenedores
docker-compose down
docker-compose up -d
```

---

## 📞 Soporte

Si encuentras problemas:

1. Revisa este README nuevamente
2. Consulta los logs: `docker-compose logs -f`
3. Verifica que tienes suficiente espacio en disco (mín. 5GB)
4. Intenta reiniciar todo: `docker-compose down && docker-compose up -d`

---

## 📝 Notas importantes

- **Credenciales por defecto:** Se crean en la primera ejecución
- **Datos persistentes:** Se guardan en volúmenes de Docker
- **TOTP:** Los códigos QR se renuevan cada 30 segundos
- **Sincronización Odoo:** Se realiza automáticamente cada cierto tiempo
- **Seguridad:** Cambia las contraseñas en producción

---

## 📄 Licencia y Documentación

Para más información técnica, consulta `MEMORIA PROYECTO.pdf`