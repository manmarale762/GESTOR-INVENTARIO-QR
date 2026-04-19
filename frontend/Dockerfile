# Dockerfile para Expo - Frontend
# Este Dockerfile sirve la app web compilada con Expo
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm install --legacy-peer-deps

# Copiar el código fuente
COPY . .

# Compilar la app web con Expo
RUN npx expo export --platform web --output-dir dist

# Etapa 2: Servidor con Nginx
FROM nginx:stable-alpine

# Copiar archivos compilados al directorio de nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Crear archivo de configuración para nginx (para SPA)
RUN echo 'server { \
  listen 80; \
  location / { \
    root /usr/share/nginx/html; \
    try_files $uri $uri/ /index.html; \
  } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]