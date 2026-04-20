# Frontend React Native - Gestor de Inventario con QR Dinámico

## Credenciales demo

- Trabajador: `worker@demo.com` / `123456`
- Administrador: `admin@demo.com` / `123456`

## API

El frontend resuelve la URL del backend así:

- Web: `http://<host-actual>:8080/api`
- Android Emulator: `http://10.0.2.2:8080/api`
- Override opcional con `EXPO_PUBLIC_API_URL`

## Ejecución

```bash
npm install
npm run start
```

Para build web exportable:

```bash
npx expo export --platform web --output-dir dist
```
