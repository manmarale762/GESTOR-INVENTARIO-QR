# Etapa 1: Construcción
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Etapa 2: Servidor de producción (Nginx)
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Copia una configuración básica de nginx si la tienes, si no, usa la por defecto
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]