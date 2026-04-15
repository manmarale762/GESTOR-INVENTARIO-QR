# Frontend React Native - Gestor de Inventario con QR Dinámico

Frontend móvil en **React Native + Expo + TypeScript** para el proyecto de **Gestor de Inventario con Control de Acceso mediante Códigos QR Dinámicos**.

## Qué incluye

- Login con dos roles: **trabajador** y **administrador**
- **Credencial QR dinámica** con refresco automático
- **Lógica TOTP** implementada en frontend
- Panel de **préstamos activos** del trabajador
- Panel de **inventario** para administración
- **Escáner QR** con cámara para validar accesos, retiradas y devoluciones
- **Historial de movimientos**
- Persistencia segura de sesión con `expo-secure-store`
- Modo **mock** para demo y modo preparado para **backend real**

## Stack

- Expo / React Native
- TypeScript
- React Navigation
- expo-camera
- expo-secure-store
- react-native-qrcode-svg
- crypto-js

## Estructura

```text
inventory-qr-frontend/
├── App.tsx
├── app.json
├── package.json
├── src/
│   ├── components/
│   ├── config/
│   ├── context/
│   ├── data/
│   ├── navigation/
│   ├── screens/
│   ├── services/
│   ├── theme/
│   └── types/
```

## Credenciales demo

- Trabajador: `worker@demo.com` / `123456`
- Administrador: `admin@demo.com` / `123456`

## Cómo ejecutar

```bash
npm install
npm run start
```

Después:

- `a` para Android
- `i` para iOS
- `w` para web

## Cambiar de mock a backend real

Edita:

```ts
// src/config/app.ts
export const APP_CONFIG = {
  apiBaseUrl: 'http://10.0.2.2:8080/api',
  useMockApi: true,
  qrRefreshIntervalSeconds: 10,
  qrGraceWindowSeconds: 5,
  defaultZone: 'Zona restringida A',
};
```

Pon `useMockApi: false` y ajusta `apiBaseUrl` a tu backend Java.

## Endpoints esperados para backend real

- `POST /auth/login`
- `GET /workers/:userId/loans`
- `GET /inventory`
- `GET /movements`
- `GET /dashboard`
- `POST /access/validate`

## Ficheros clave

- `src/services/totp.ts`: generación TOTP
- `src/services/qr.ts`: construcción y validación del QR dinámico
- `src/screens/worker/DynamicBadgeScreen.tsx`: credencial dinámica
- `src/screens/admin/ScannerScreen.tsx`: lector QR con cámara
- `src/services/api.ts`: capa de integración mock/real

## Observaciones

- El frontend está diseñado para cubrir las evidencias móviles del enunciado.
- El flujo de validación real debe cerrarse en backend para seguridad completa.
- El modo mock permite defender el proyecto aunque el backend Java todavía no esté conectado.
