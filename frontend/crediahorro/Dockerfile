# Etapa de construcción
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build -- --configuration=production

# Etapa de producción con nginx
FROM nginx:1.29-alpine
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/dist/crediahorro/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
